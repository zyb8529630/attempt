package com.attempt.core.dasc.client.api;

import com.attempt.core.dasc.models.DascRequest;
import com.attempt.core.queue.models.Message;

/**
 * DASC服务调用的客户端拦截接口规范.
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:17:08
 *
 */
public interface CallerInterceptor {

	/**
    * 在调用MQ发送消息前，拦截调用
    *
    * @param messageToSend 待发送到MQ的消息体
    * @param args          本次调用的参数列表
    * @throws Exception 处理出现错误时抛出异常。如果在xml配置中将<interrupt-allow/>设置为true，则会阻断消息的发出。默认为false
    */
   void before(Message<DascRequest> messageToSend, Object[] args) throws Exception;

   /**
    * 已经发送至MQ后，拦截调用
    *
    * @param messageSended 已经发送到MQ的消息体
    * @param args          本次调用的参数列表
    */
   void after(Message<DascRequest> messageSended, Object[] args);
}
