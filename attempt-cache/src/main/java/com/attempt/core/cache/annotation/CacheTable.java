package com.attempt.core.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识可缓存为表格的Java对象.
 * @author zhouyinbin
 * @date 2019年6月6日 下午5:02:29
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheTable {

	 /**
     * 缓存表格在缓存中的名称
     *
     * @return 表格在缓存中的名称
     */
    String value();

    /**
     * 缓存表格上的主键策略。填入作为主键的属性名称
     */
    String[] primaryKey() default {};

    /**
     * 缓存表格上的索引字段数组，填入作为索引的属性名称
     *
     * @return 索引数组
     */
    String[] index() default {};
}
