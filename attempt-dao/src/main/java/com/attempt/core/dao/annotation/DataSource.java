package com.attempt.core.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 *
 * @author zhouyinbin
 * @date 2019年6月10日 下午1:20:16
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataSource {
	
    /**
     * 定义数据源的注解类型.
     * <p>
     * {@code type}={@code ElementType.METHOD}：在DataSource的value查找数据源名称
     * <p>
     * {@code type}={@code ElementType.PARAMETER}：在DataSource指定的parameterIndex值，查询对应方法的参数值作为数据源名
     *
     * @return the element type
     */
    ElementType type() default ElementType.METHOD;

    /**
     * 指定数据源的值.
     * <p>
     * 该属性在{@code type}={@code ElementType.METHOD} 有效
     *
     * @return the string
     */
    String value() default "";

    /**
     * 当{@code type}={@code ElementType.PARAMETER}时，设定获取数据源名称的参数索引.
     *
     * @return index
     */
    int parameterIndex() default -1;

    /**
     * 当{@code type}={@code ElementType.PARAMETER}时，设定获取数据源名称的参数里面的属性变量数据.
     *
     * @return propertyName
     */
    String propertyName() default "";

}
