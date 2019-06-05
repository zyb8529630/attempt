package com.attempt.core.cache.provider;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.attempt.core.cache.api.CacheService;
import com.attempt.core.common.util.PropertiesUtils;

/**
 * CacheService对象工厂.
 * @author zhouyinbin
 * @date 2019年6月5日 下午3:10:45
 *
 */
public class CacheServiceProvider implements FactoryBean<CacheService>,  InitializingBean {
	
	
	/**
     * CacheService实例.
     */
    private CacheService cacheService;
    
	@Autowired
    private ApplicationContext applicationContext;

    /**
     * 注册的{@link CacheService}类型实例的Spring BeanName后缀.
     */
    public static final String BEAN_REG_SUFFIX_STR = "CacheService";
    


    /**
     * 获取{@link CacheService}实例
     */
	@Override
	public CacheService getObject() throws Exception {
		return cacheService;
	}

	/**
	 *  声明提供的{@link CacheService}对象类型
	 */
	@Override
	public Class<?> getObjectType() {
		return CacheService.class;
	}

	/**
	 * 声明为单例对象
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}
    
     
	/**
	 * 从Spring和配置文件中动态的获取当前工厂的{@link CacheService}实例
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		String cacheType =PropertiesUtils.getProperty("cache.type");
		if(StringUtils.isNoneEmpty(cacheType)) {
			String beanName = Arrays.stream(applicationContext.getBeanDefinitionNames()).filter(str -> str.equalsIgnoreCase(cacheType + BEAN_REG_SUFFIX_STR))
			.findFirst().get();
			cacheService = (CacheService)applicationContext.getBean(beanName);
		}else
            throw new NullPointerException("Can't find a vaild CacheService instance. Please config the property [cache.type]!");  
	}






}