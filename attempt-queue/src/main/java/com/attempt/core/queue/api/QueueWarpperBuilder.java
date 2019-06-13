package com.attempt.core.queue.api;

import com.attempt.core.queue.models.QueueWarpper;

/**
 * 创建AMQP协议消息队列的配置和连接实例.
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:21:00
 *
 */
public interface QueueWarpperBuilder {

	/**
	 * 创建warpper
	 * 
	 * @param queueName 标识的队列名称。如果为null则取默认值。
	 * @return warpper实例
	 */
	 QueueWarpper create(String queueName);
}