package com.yanan.framework.ant.serial;

import java.nio.ByteBuffer;

import com.yanan.framework.ant.core.MessageSerialization;

//@Register(attribute = {"*.String","STR"})
public class DefaultMessageConverter implements MessageSerialization{

	@Override
	public ByteBuffer serial(Object serailBean) {
		return null;
	}

	@Override
	public <T> T deserial(ByteBuffer byteBuffer, int position, int limit, Class<T> type) {
		return null;
	}

}
