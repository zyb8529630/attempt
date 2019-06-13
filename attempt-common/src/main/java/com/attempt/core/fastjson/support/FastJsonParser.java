package com.attempt.core.fastjson.support;

import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * 用于定制FastJson某些自定义的序列化和反序列化操作
 * @author zhouyinbin
 * @date 2019年6月5日 下午1:23:05
 *
 */
public interface FastJsonParser {
	
	 /**
     * 可用于FastJson做序列化时的{@link ObjectSerializer}
     * 
     */
    ObjectSerializer getSerializer();

    /**
     * 获取可用于FastJson反序列化时的{@link ObjectDeserializer}
     */
    ObjectDeserializer getDeserializer();

    /**
     * 获取要做转换的Class对象
     *
     */
    Class<?> getParseClass();
}
