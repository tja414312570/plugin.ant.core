package com.YaNan.frame.ant.implement;

import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.handler.AntRegisterHandler;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.service.AntClientRegisterService;
import com.YaNan.frame.ant.service.AntRegisterService;
import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.ant.type.ClientType;

public class AntChannelProcess extends AbstractProcess{
	private AntClientHandler handler;
	private SelectionKey key;
	private int ops;
	private static Logger logger = LoggerFactory.getLogger(AntChannelProcess.class);
	public AntChannelProcess(AntClientHandler handler, int ops, SelectionKey key) {
		this.handler = handler;
		this.ops = ops;
		this.key = key;
	}
	@Override
	public void execute() {
		if(ops == SelectionKey.OP_READ) {
			handler.handleRead(key);
		}else if(ops == SelectionKey.OP_WRITE) {
			
		}else if(ops == SelectionKey.OP_CONNECT) {
        	AntProviderSummary antProviderSummary = (AntProviderSummary) key.attachment();
            handler.setAttribute(AntProviderSummary.class, antProviderSummary);
        	logger.debug("Socket channel connected:"+handler);
        	if(AntRuntimeService.getAntRuntimeService().getQueenHandler() == null) {
        		AntRuntimeService.getAntRuntimeService().setQueenHandler(handler);
        	}
        	if(AntContext.getContext().getContextConfigure().getServerPort()>0) {
        		handler.setClientType(ClientType.Provider);
        	}else {
        		handler.setClientType(ClientType.Customer);
        	}
        	AntRegisterHandler registerHandler = new AntRegisterHandler(handler);
        	AntRegisterService.getInstance().register(registerHandler);
		}else if(ops == SelectionKey.OP_ACCEPT) {
			AntClientRegisterService.getInstance().register(handler);
		}
	}

}