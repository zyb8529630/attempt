package com.attempt.core.dasc.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 用于对动态数据源进行配置的切面AOP拦截器.
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:36:16
 *
 */
public class AsynCallAdvice {	
	 /**
     * 日志管理.
     */
    private static Logger logger = LoggerFactory.getLogger(AsynCallAdvice.class);
    /**
     * 请求消息的生产者
     */
    @Autowired
    private RequestProducer requestProducer;

	

}
