package com.attempt.core.queue.api;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * 获取消息队列的连接.
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:20:40
 *
 */
public interface QueueConnectionFactory {
	
	 /**
     * 获取用于消息发送者的消息队列连接
     *
     * @return {@link Connection}
     */
    Connection getProducerConnection();

    /**
     * 获取用于消息发送者的消息队列连接
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Connection}
     */
    Connection getProducerConnection(String queueName);

    /** 
     * 获取用于消息消费者的消息队列连接
     *
     * @return {@link Connection}
     */
    Connection getConsumerConnection();

    /**
     * 获取用于消息消费者的消息队列连接
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Connection}
     */
    Connection getConsumerConnection(String queueName);

    /**
     * 创建用于消息生产者的消息队列虚拟通道
     * 推荐每个线程保持独立Chanel实例
     *
     *
     * @return {@link Channel}
     */
    Channel createProducerChannel();

    /**
     * 创建用于消息生产者的消息队列虚拟通道 推荐每个线程保持独立Channel实例
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Channel}
     */
    Channel createProducerChannel(String queueName);

    /**
     * 创建用于消息消费者的消息队列虚拟通道
     * 推荐每个线程保持独立Chanel实例
     *
     */
    Channel createConsumerChannel();

    /**
     * 创建用于消息消费者的消息队列虚拟通道 推荐每个线程保持独立Chanel实例
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Channel}
     */
    Channel createConsumerChannel(String queueName);
}
