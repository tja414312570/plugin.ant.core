package com.yanan.framework.ant.provider;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.ant.core.MessageChannel;
import com.yanan.framework.ant.core.MessageChannelListener;
import com.yanan.framework.ant.core.MessageHandler;
import com.yanan.framework.plugin.annotations.Service;

//@Register
public class Provider<T> implements MessageChannel<T>{
	@Service
	private Logger logger;
	@PostConstruct
	public void init() {
		logger.debug("初始化");
	}
	@Override
	public void close() {
		logger.debug("关闭");
	}
	@Override
	public void open() {
		logger.debug("初始化");
	}
	@Override
	public void transport(T message) {
		logger.debug("传输:"+message);
	}
	@Override
	public void accept(MessageHandler<T> message) {
		logger.debug("接收消息:"+message);
	}
	@Override
	public void setListener(MessageChannelListener<T> listener) {
		// TODO Auto-generated method stub
		
	}
}
