package com.attempt.core.fastjson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 框架内，用于控制特定数据类型的JSON序列化/反序列化的格式信息.
 * @author zhouyinbin
 * @date 2019年6月5日 下午1:20:21
 *
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonConfig {
	
	/**
     * 是否在JSON报文中写入类型信息
     *
     * @return true 写入类型信息
     * @see com.alibaba.fastjson.serializer.SerializerFeature#WriteClassName
     */
    boolean writeType() default false;
 
    /**
     * 用于反序列化操作场景。
     * 当JSON文本中包含{@code '@type'}内容时，可以选择忽略自动类型匹配的功能。直接尝试去做字段级映射。
     * 需要注意的是，打开这个开关意味着带有泛型的集合类型没有办法做正确解析了。
     *
     * @return true 忽略JSON文本中的类型信息
     * @see com.alibaba.fastjson.parser.Feature#IgnoreAutoType
     */
    boolean ignoreType() default false;

    /**
     * 当对象字段为null时，是否在JSON文本中输出空字段
     *
     * @return true 写入空字段
     * @see com.alibaba.fastjson.serializer.SerializerFeature#WriteMapNullValue
     * @see com.alibaba.fastjson.serializer.SerializerFeature#WriteNullListAsEmpty
     * @see com.alibaba.fastjson.serializer.SerializerFeature#WriteNullStringAsEmpty
     */
    boolean writeNull() default false;

    /**
     * {@link java.util.Date}类型字段的转换格式
     *
     * @return 转换日期格式
     */
    String dateFormat() default "";

    /**
     * 是否启动循环引用检测。默认为false
     * 如果启用，则相同的BO对象实例不能在一次JSON序列化过程中出现两次
     *
     * @return true 启动循环引用
     */
    boolean circularReferenceDetect() default false;
}
