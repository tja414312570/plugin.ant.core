package com.yanan.framework.ant.proxy;

import com.yanan.framework.ant.dispatcher.DispatcherContext;

/**
 * 回调
 * @author tja41
 * @param <T>
 */
public interface Callback<T> {
	/**
	 * 当响应成功
	 * @param ctx
	 * @param message
	 */
	void onMessage(DispatcherContext<T> ctx,T message);
	
	
	default void onError(DispatcherContext<T> ctx,Exception error) {};
}
