package com.yanan.framework.a.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.yanan.framework.a.channel.socket.LockSupports;
import com.yanan.framework.a.channel.socket.server.MessageChannelCreateListener;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.a.proxy.Invoker;
import com.yanan.framework.ant.type.MessageType;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.utils.CacheHashMap;
import com.yanan.utils.reflect.TypeToken;

@Register
public class ChannelDispatcherServer<K> implements ChannelDispatcher<K>{
	
	private CacheHashMap<K, MessageChannel<MessagePrototype<?>>> messagelChannel = new CacheHashMap<>();
	private Map<Class<?>, Invoker<DispatcherContext<Object>>> invokerMapping = new HashMap<>();
	private ThreadLocal<DispatcherContext<?>> dispatcherContextLocal = new InheritableThreadLocal<>();
	private ChannelManager<?> channelManager;
	private AtomicInteger count = new AtomicInteger();
	@Service
	private Logger logger;
	@Override
	public void bind(ChannelManager<?> server) {
		this.channelManager = server;
		logger.debug("绑定通道管理:"+server);
		if(server.getServerChannel() != null) {
			ServerMessageChannel<MessagePrototype<?>> serverMessageChannel = server.getServerChannel();
			logger.debug("绑定通道消息监听:"+serverMessageChannel);
			serverMessageChannel.onChannelCreate(new MessageChannelCreateListener<MessagePrototype<?>>() {
				@Override
				public void onCreate(MessageChannel<MessagePrototype<?>> messageChannel) {
					messageChannel.accept(message->{
						doExecute(messageChannel,message);
					});				
				}
			});
		}
	}
	void doExecute(MessageChannel<?> messageChannel, MessagePrototype<?> message) {
		Object invokerMessage = message.getInvoker();
		if(invokerMessage==null) {
			//空请求
			return;
		}
		DefaultDispatcherContext<Object> dispatcherContext = new DefaultDispatcherContext<>();
		dispatcherContext.setMessageChannel(messageChannel);
		dispatcherContext.setMessagePrototype(message);
		dispatcherContextLocal.set(dispatcherContext);
		if(message.getType()==MessageType.REQUEST) {
			logger.debug("调用信息:"+message);
			Class<?> invokerClass = message.getInvoker().getClass();
			logger.debug("消息类"+invokerClass);
			Invoker<DispatcherContext<Object>> invoker = invokerMapping.get(invokerClass);
			if(invoker == null) {
				synchronized (invokerClass) {
					if(invoker == null) {
						invoker = PlugsFactory.getPluginsInstanceByAttributeStrict(new TypeToken<Invoker<DispatcherContext<Object>>>() {}.getTypeClass(), invokerClass.getName());
						invokerMapping.put(invokerClass, invoker);
					}
				}
			}
			invoker.execute(dispatcherContext);
		}else {
			logger.debug("响应信息:"+message);
			LockSupports.set(message.getRID(), message.getRID(), message.getInvoker());
			LockSupports.unLock(message.getRID());
		}
	}
	@Override
	public Object request(K channel,Object message) {
		MessageChannel<MessagePrototype<?>> messageChannel = getChannel(channel);
		logger.debug("请求数据:"+messageChannel);
		Request<Object> request = new Request<Object>();
		request.setInvoker(message);
		request.setRID(count.getAndIncrement());
		messageChannel.transport(request);
		//加锁
		LockSupports.lock(request.getRID());
		logger.debug("返回数据:"+LockSupports.get(request.getRID(), request.getRID()));
		return LockSupports.get(request.getRID(), request.getRID());
	}

	private MessageChannel<MessagePrototype<?>> getChannel(K channel) {
		MessageChannel<MessagePrototype<?>> messageChannel = messagelChannel.get(channel);
		if(messageChannel == null) {
			final MessageChannel<MessagePrototype<?>> newMessageChannel = channelManager.getChannel(channel);
			newMessageChannel.accept(message->{
				doExecute(newMessageChannel,message);
			});
			messageChannel = newMessageChannel;
			messagelChannel.puts(channel, messageChannel);
		}
		return messageChannel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void bind(Invoker<?> invoker) {
//		this.invoker = (Invoker<DefaultDispatcherContext<Object>>) invoker;
	}

	@Override
	public void requestAsync(K channel, Object request, Callback callBack) {
		
	}

}
