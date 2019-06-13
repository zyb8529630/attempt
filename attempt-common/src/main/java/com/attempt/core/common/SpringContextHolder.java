package com.attempt.core.common;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * 持有Spring的上下文
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:08:26
 *
 */
public class SpringContextHolder implements InitializingBean{
	 
	/**
	 * spring的上下文实例
	 */
	private static ApplicationContext  ac;
	
	/**
	 * 容器中的上下文对象
	 */
	@Autowired
	ApplicationContext applicationContext;
	

	@Override
	public void afterPropertiesSet() throws Exception {	
		SpringContextHolder.ac = applicationContext;
	}	
	
	/**
	 * 获取spring上下文对象
	 * @return
	 */
	public ApplicationContext get() {
		Assert.notNull(ac, "applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		return ac;
	}
	
	/**
	 * 从容器中获取Bean信息
	 * @param name Bean的name
	 * @return T 实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		Assert.notNull(ac, "applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		return (T)ac.getBean(name);
	}
	
	/**
	 * 从spring容器中获取Bean信息
	 * @param requiredType Bean的Class信息
	 * @return
	 */
	public static <T> T getBean(Class<T> requiredType) {
		Assert.notNull(ac, "applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		return ac.getBean(requiredType);
	}
	
	/**
	 * 清除
	 */
	public static void clear() {
	   ac = null;
	}
	

}
