package com.attempt.core.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 缓存数据源的配置注释. 可以定义在类中的数据源注解.
 * @author zhouyinbin
 * @date 2019年6月5日 下午1:52:05
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CacheSource {

	/** 
	 * 指定数据源的值.
	 * @Description 数据源注解
	 * @return the string
	 */
	String value() default "defaultCacheSource";
}
