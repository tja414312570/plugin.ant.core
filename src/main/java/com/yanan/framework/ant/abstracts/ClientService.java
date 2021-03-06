package com.yanan.framework.ant.abstracts;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.yanan.framework.ant.model.AntProviderSummary;
import com.yanan.framework.ant.service.AntRuntimeService;

/**
 * 连接服务，提供RPC通信层支持
 */
public interface ClientService {
	/**
	 * 连接到目标服务器
	 * @param providerSummary
	 * @throws IOException 
	 */
	void clientService(AntProviderSummary providerSummary) throws IOException;
	/**
	 * 获取运行时服务
	 * @return
	 */
	AntRuntimeService getRuntimeService();
	/**
	 * 开启服务提供
	 */
	void startProvider();
	/**
	 * 获取服务端口
	 * @return
	 */
	int getServerPort();
	
	ClientInstance getClientHandler(SocketChannel socketChannel);

}