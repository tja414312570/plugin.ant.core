package com.yanan.framework.a.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.a.dispatcher.DispatcherContext;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;

@Register
public class AbstractInvoker implements Invoker<DispatcherContext<Invokers>>{
	@Service
	private ProxyInvokerMapper proxyInvokerMapper;
	@Service
	private Logger logger;
	@PostConstruct
	public void init() {
		System.err.println("init");
	}

	@Override
	public void execute(DispatcherContext<Invokers> dispatcherContext) {
		logger.debug("invoker:"+dispatcherContext.getMessage()+":"+dispatcherContext);
		Invokers invoker = dispatcherContext.getMessage();
		Class<?> invokerClass = invoker.getInvokeClass();
		Object instance = PlugsFactory.getPluginsInstance(invokerClass);
		Method method = invoker.getInvokeMethod();
		Object result;
		try {
			result = method.invoke(instance, invoker.getInvokeParmeters());
			System.err.println(result);
			dispatcherContext.response(result);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			dispatcherContext.response(e.getMessage());
		}
		
	}
	
}
