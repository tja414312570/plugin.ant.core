# plugin.ant
## 提供Plugin的RPC能力，基于SOAP的架构方式，目前使用nacos实现服务中心
## 我们的目标是高性能，低延迟，高准确率，高容错
* 基于nio socket通信，byte的传输方式
* 自定义的消息传输协议
* 可扩展的序列化，跨编程语言的消息协议
* 可以同时作为客户端和服务端（注册中心需要使用扩展插件）
* 高可用:只要节点还有一个实例存活系统就可以正常访问
* 基于nacos的服务中心实现
* 自动化端口配置
![avatar](https://ufomedia.oss-cn-beijing.aliyuncs.com/ant.png)
# ANT协议
 * 包头+包体
 * 包头 固定长度12byte，byte[0] 服务名长度，1位，byte[1 -4]请求号，4位，byte[5-6]消息头长度，2位，byte[7-10]消息体长度，4位，byte[11]消息类型,1位
 * 包体 可变长度，格式:服务名+调用信息（类:方法:方法参数...)+参数
 * 全包 [服务名长度+请求号+消息头长度+消息体长度+调用类型+服务名+调用信息+调用参数]
# 计划
* 链路追踪
* 全局锁
* 监控
* 分布式事物
## 简单测试
* 服务中心，客户端，服务端均在同一机器，双核8g，单线程7k，多线程3-4w的吞吐率，调用内容为传入a,b，返回a+b

# 20211019
* 重新实现建构,最新测试效率如下
```java
串行执行耗时:20173ms,吞吐率:4 个/ms，平均耗时:0ms
并行执行耗时:7315ms,吞吐率:13 个/ms，平均耗时:0ms
直接执行耗时:33ms,吞吐率:3030 个/ms，平均耗时:0ms
```
# 20200608
* 基于nacos的服务中心
# 20200615
* 新增AbstractProcess池化逻辑，对AntChanelProcess和MessageProcess进行池化管理，极大程度的减少gc
* 对多任务处理细节的优化，如池化处理，抽象任务等
# 20200617
* 将连接层抽象出来，将Ant协议的连接实现作为一个默认连接层实现，可以扩展更多连接协议
* ant协议中，将Selector中的SelectionKeySet依照netty的SelectionKeySet的优化方式，以提高Selector的执行效率
* Discovery ==> AntRuntimeService ==> ClientService ==> ClientInstance ==> AntServiceInstance
* 服务调用流程：service invoke ==> AntInvokeProxy ==> AntRuntimeService ==> DiscoveryService ==> ClientService ==> ClientInstance ==> AntServiceInstance ==> ClientInstance ==> transport message ==> AntRuntimeService ==> AntInvokeProxy ==> return result
服务端
```xml
Ant:{ 
	##	基础配置
	bufferType:"heap",	## 内存类型  heap 或 direct
	timeout:30000,  ## 超时
	process:10,	##处理消息时的线程数
	checkTime:1000,	##每次消息检查时间间隔 毫秒
	bufferSize:1024	,	## buffer大小
	bufferMaxSize:20480 		##最大buffer大小
	##	作为服务提供者必须填写
	port:4280,##	对位服务端口
	host:127.0.0.1,	## 对外服务ip
	name:"queue", ##对外服务名
	
}
nacos:{
	host:"127.0.0.1",
	port:"8858",
	group:"queue",
	name:"queue",
	namespace:"",
}
```
```java
        PlugsFactory.getInstance().addPlugs(AntMeessageSerialHandler.class);//消息序列化
	PlugsFactory.getInstance().addPlugs(DefaultAntClientServiceImpl.class);
	AntNacosRuntime antNacosRuntime = new AntNacosRuntime("classpath:Ant.yc");
	//启动Ant
	AntContext antContext = AntFactory.build("classpath:Ant.yc");
	antContext.init();
	antContext.start();
```
消费端:
```xml
Ant:{ 
	##	基础配置
	bufferType:"heap",	## 内存类型  heap 或 direct
	timeout:30000,  ## 超时
	process:10,	##处理消息时的线程数
	checkTime:1000,	##每次消息检查时间间隔 毫秒
	bufferSize:1024	,	## buffer大小
	bufferMaxSize:2048 		##最大buffer大小
}
nacos:{
	host:"127.0.0.1",
	port:"8858",
	group:"queue",
	name:"queue",
	namespace:"",
}
```
```java
       PlugsFactory.getInstance().addPlugs(AntMeessageSerialHandler.class);
	PlugsFactory.getInstance().addPlugs(AntProxyMapper.class);
	AntNacosRuntime antNacosRuntime = new AntNacosRuntime("classpath:Ant.yc");
	//		registerInstance：注册实例。
	//启动Ant
	AntContext antContext = AntFactory.build("classpath:Ant.yc");
	antContext.init();
	antContext.start();
	System.out.println("启动完成");
	System.out.println("========================");
	AntService1 antService1 = PlugsFactory.getPlugsInstance(AntService1.class);
	System.out.println("调用服务1:"+antService1);
	System.out.println("调用服务1结果:"+antService1.add(111, 222));
	AntService2 antService2 = PlugsFactory.getPlugsInstance(AntService2.class);
	System.out.println("调用服务2:"+antService2);
	System.out.println("调用服务2结果:"+antService2.add(333, 222));

```
antService1接口：客户端和服务端各一份
```java
package com.YaNan.test.ant;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.plugin.annotations.Service;

@Ant("queue")
@Service
public interface AntService1 {
	public int add(int a,int b);
}
```
antService1实现:服务端
```java
package com.YaNan.test.ant;

import com.YaNan.frame.plugin.annotations.Register;

@Register
public class AntTest1 implements AntService1{

	@Override
	public int add(int a, int b) {
		System.out.println("传送:"+a+"  "+b);
		return a+b;
	}

}

```
