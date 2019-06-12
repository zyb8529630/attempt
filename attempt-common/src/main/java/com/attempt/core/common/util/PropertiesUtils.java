package com.attempt.core.common.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.Assert;

import com.attempt.core.common.api.PropertiesLoader;

/**
 * 获取全局属性信息.
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:26:49
 *
 */
public class PropertiesUtils extends PropertyPlaceholderConfigurer {

	/**
	 * 最终的全局Properties对象
	 */
	 private static Properties props;
	 
	 @Override
	    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	        try {
	            /**
	             * 从本地配置中读取*.properties文件资源
	             */
	            Properties mergedProps = mergeProperties();
	            /**
	             * 识别Spring中已经实现了{@link PropertiesLoader}接口的服务，调用并填充properties
	             */
	            Arrays.stream(beanFactory.getBeanNamesForType(PropertiesLoader.class, false, false)).map(beanName -> {
	                try {
	                    return (PropertiesLoader) Class.forName(beanFactory.getBeanDefinition(beanName).getBeanClassName()).newInstance();
	                } catch (Exception e) {
	                    return null;
	                }
	            }).filter(Objects::nonNull).forEach(loader -> loader.loadProperties(mergedProps));
	            /**
	             * 对配置项中的占位符做转义
	             */
	            convertProperties(mergedProps);
	            /**
	             * 将配置项信息开放至全局
	             */
	            props = mergedProps;
	            /**
	             * 将配置项提交至Spring容器来解析和识别
	             */
	            processProperties(beanFactory, mergedProps);
	        } catch (IOException ex) {
	            throw new BeanInitializationException("Could not load properties", ex);
	        }
	    }
	 
	 /**
	  * 获取配置项信息.
	  * @param key 配置项的key
	  * @return 
	  */
	 public static String getProperty(String key) {
		 Assert.notNull(props, "从Spring ApplicationContext获取到的配置信息中, 未找到可读的properties对象信息. 请检查applicationContext-*,xml文件");
	     Object obj = props.get(key);
	     return obj == null ? null : obj.toString();
	 }
	 
	 /**
	  * 判断是否存在某个属性的key
	  * @param key
	  * @return
	  */
	 public static boolean existProperty(String key) {
	        return props.containsKey(key);
	 }
	 
	 /**
	  * 获取所有key-value集合
	  * @return 所有键值集合{@link Properties}对象返回
	  */
	 public static Properties getAllProperties() {
	        return new Properties(props);
	 }	 
	 
}
