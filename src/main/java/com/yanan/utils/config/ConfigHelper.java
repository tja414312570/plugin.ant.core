package com.yanan.utils.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.utils.ArrayUtils;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.reflect.ParameterUtils;
import com.yanan.utils.reflect.cache.ClassHelper;

public class ConfigHelper {
	@SuppressWarnings("unchecked")
	public static <T> T decode(Config config,Class<T> type) {
		if(config == null || type == null)
			throw new IllegalArgumentException("config or type is null");
		AppClassLoader loader = new AppClassLoader(type);
			//遍历当前节点
			ClassHelper classHelper = ClassHelper.getClassHelper(type);
			//获取类所有Field
			Field[] fields  = classHelper.getAllFields();
			Object object = null;
			for(Field field : fields) {
				String name = getCofigName(field);
				//如果获取当前配置对象
				if(field.getAnnotation(Self.class)!=null) {
					try {
						loader.set(field, config);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
					continue;
				}
				//判断是否有当前节点
				if(!config.hasPath(name)) {
					continue;
				}
				//忽略该节点
				if(field.getAnnotation(Ignore.class)!=null)
					continue;
				Decoder decoder = field.getAnnotation(Decoder.class);
				if(decoder != null) {
					try {
						ConfigResolver resolver = PlugsFactory.getPluginsInstance(decoder.value());
						object = resolver.decode(field, config, loader.getLoadedObject());
						loader.set(field, object);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
					continue;
				}
				//判断当前节点是否是一个基本对象
				if(ParameterUtils.isBaseType(field.getType())) {
					
					try {
						object = getBaseType(name,field.getType(),config);
						loader.set(field, object);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}else {
					System.out.println(name);
					Config childConfig  = config.getConfig(name);
					System.out.println(childConfig);
				}
			}
//			Iterator<Entry<String, Config>> configSetIterator = config.configEntrySet().iterator();
//			while(configSetIterator.hasNext()) {
//				Entry<String, Config> entry = configSetIterator.next();
//				System.out.println(entry.getKey()+"    "+entry.getValue());
//			}
		return (T) loader.getLoadObject();
	}

	public static String getCofigName(Field field) {
		Name nameMapping = field.getAnnotation(Name.class);
		if(nameMapping != null) {
			return nameMapping.value();
		}
		return field.getName();
	}

	private static Object getBaseType(String name, Class<?> type, Config config) {
		if(type.isArray()) {
			if(config.hasPath(name)) {
				Class<?> baseType = ArrayUtils.getArrayType(type);
				if(config.isList(name)) {
					if(baseType.equals(String.class)) {
						return config.getStringList(name);
					}
					if(baseType.equals(int.class)) {
						return config.getIntList(name);
					}
					if(baseType.equals(long.class)) {
						return config.getLongList(name);
					}
					if(baseType.equals(boolean.class)) {
						return config.getBooleanList(name);
					}
					if(baseType.equals(double.class)) {
						return config.getDoubleList(name);
					}
				}else if (config.getType(name).equals(ConfigValueType.STRING)) {
					String[] strValues = config.getString(name).split(",");
					Object values;
					values = ParameterUtils.parseBaseTypeArray(type, strValues, null);
					return values;
				} 
			}
			return null;
		}
		if(type.equals(String.class)) {
			return config.getString(name);
		}else if(type.equals(int.class)) {
			return config.getInt(name);
		}else if(type.equals(long.class)) {
			return config.getLong(name);
		}else if(type.equals(short.class)) {
			return (short)config.getInt(name);
		}else if(type.equals(boolean.class)) {
			return config.getBoolean(name);
		}else if(type.equals(double.class)) {
			return config.getDouble(name);
		}
		return null;
	}
}