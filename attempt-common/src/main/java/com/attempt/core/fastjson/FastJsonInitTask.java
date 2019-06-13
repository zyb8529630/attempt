package com.attempt.core.fastjson;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.attempt.core.fastjson.support.FastJsonParser;
import com.attempt.core.init.InitializingTask;

/**
 * Fastjson对自定义序列化&反序列化实现的初始化任务.
 * @author zhouyinbin
 * @date 2019年6月5日 下午1:18:54
 *
 */
public class FastJsonInitTask  extends InitializingTask {
	
	/**
     * 标记是否已经初始化
     */
    private boolean isInited = false;

    /**
     * 作业执行
     */
	@Override
	public void execute(ApplicationContext applicationContext) {
		/**
         * 在全局配置中开启autoType支持
         */
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        /**
         * 获取注册在Spring中全部的{@link FastJsonParser}
         */
        initFastJsonParsers(applicationContext.getBeansOfType(FastJsonParser.class));
		
	} 

	public void initFastJsonParsers(Map<String, FastJsonParser> beansOfType) {
		if (isInited)
        return;

	    // 如果map为空，则退出
	    if (MapUtils.isEmpty(beansOfType)) {
	        isInited = true;
	        return;
	    }
	    /**
	     * 遍历Map，并将序列化与反序列化实例注入到FastJson中
	     */
	    beansOfType.forEach((k, v) -> {
        /**
         * 将特定类型的序列化实例放入Fastjson中
         */
        ObjectSerializer serializer = v.getSerializer();
        if (serializer != null)
            SerializeConfig.getGlobalInstance().put(v.getParseClass(), serializer);
        /**
         *  将特定类型的反序列化实例放入Fastjson中
         */
        ObjectDeserializer deserializer = v.getDeserializer();
        if (deserializer != null)
            ParserConfig.getGlobalInstance().putDeserializer(v.getParseClass(), deserializer);
    });
    	isInited = true;
	}

}
