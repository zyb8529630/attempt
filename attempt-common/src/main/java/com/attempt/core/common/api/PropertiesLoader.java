package com.attempt.core.common.api;

import java.util.Properties;

/**
 * 装载配置项的接口规范.
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:30:18
 *
 */
public interface PropertiesLoader {
 
	/**
	 * 载入配置项信息，并放入prop中
	 * @param props 待放入的Properties
	 */
	void loadProperties(Properties props);
}
