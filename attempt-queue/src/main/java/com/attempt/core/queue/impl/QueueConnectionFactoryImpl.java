package com.attempt.core.queue.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.attempt.core.common.SpringContextHolder;
import com.attempt.core.common.util.PropertiesUtils;
import com.attempt.core.queue.api.QueueConnectionFactory;
import com.attempt.core.queue.api.QueueWarpperBuilder;
import com.attempt.core.queue.models.QueueWarpper;
import com.attempt.core.queue.support.QueueConnectionHolder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 *
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:28:13
 *
 */
public class QueueConnectionFactoryImpl  implements QueueConnectionFactory, InitializingBean, DisposableBean {

	
	/**
     * 日志管理
     */
    private static Logger logger = LoggerFactory.getLogger(QueueConnectionFactoryImpl.class);
    /**
     * 默认的消息队列名称
     */
    public static final String DEFAULT_MQ_FLAG = "default";
    /**
     * 消息队列的类型，该类型标识会与WarpperBuilder拼接，用来查找对应的bean实例。默认为rabbit
     */
    private static final String QUEUE_TYPE_FLAG = "%squeue.type";
    /**
     * 消息队列的名称列表,多个以,分割
     */ 
    private static final String QUEUE_LIST_FLAG = "queue.list";

    /**
     * 消息队列的warpper集合
     */
    private Map<String, QueueWarpper> warpperMap = new HashMap<>();
    
    /**
     * 获取用于消息发送者的消息队列连接
     *
     * @return {@link Connection}
     */
    @Override
    public Connection getProducerConnection() {
        String queueName = StringUtils.defaultIfEmpty(QueueConnectionHolder.get(), DEFAULT_MQ_FLAG);
        return this.getProducerConnection(queueName);   
    }

    /**
     * 获取用于消息发送者的消息队列连接
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Connection}
     */
    @Override
    public Connection getProducerConnection(String queueName) {
        QueueWarpper warpper = warpperMap.get(queueName);
        if (warpper.getProducerAccessable())
            return warpper.getProducerConnection();
        throw new UnsupportedOperationException("The Producer Connection from " + queueName + " is unaccessable!");
    }

    /**
     * 获取用于消息消费者的消息队列连接
     *
     * @return {@link Connection}
     */
    @Override
    public Connection getConsumerConnection() {
        String queueName = StringUtils.defaultIfEmpty(QueueConnectionHolder.get(), DEFAULT_MQ_FLAG);
        return warpperMap.get(queueName).getConsumerConnection();
    }

    /**
     * 获取用于消息消费者的消息队列连接
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Connection}
     */
    @Override
    public Connection getConsumerConnection(String queueName) {
        QueueWarpper warpper = warpperMap.get(queueName);
        if (warpper.getConsumerAccessable())
            return warpper.getConsumerConnection();
        throw new UnsupportedOperationException("The Consumer Connection from " + queueName + " is unaccessable!");
    }

    /**
     * 创建用于消息生产者的消息队列虚拟通道
     * 推荐每个线程保持独立Chanel实例
     *
     * @return {@link Channel}
     */
    @Override
    public Channel createProducerChannel() {
        try {
            return this.getProducerConnection().createChannel();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建用于消息生产者的消息队列虚拟通道 推荐每个线程保持独立Channel实例
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Channel}
     */
    @Override
    public Channel createProducerChannel(String queueName) {
        try {
            return this.getProducerConnection(queueName).createChannel();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建用于消息消费者的消息队列虚拟通道
     * 推荐每个线程保持独立Chanel实例
     *
     * @return {@link Channel}
     */
    @Override
    public Channel createConsumerChannel() {
        try {
            return this.getConsumerConnection().createChannel();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建用于消息消费者的消息队列虚拟通道 推荐每个线程保持独立Chanel实例
     *
     * @param queueName 路由到对应的mq连接上
     * @return {@link Channel}
     */
    @Override
    public Channel createConsumerChannel(String queueName) {
        try {
            return this.getConsumerConnection(queueName).createChannel();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 该实例销毁时，需要将生产者和消费者连接关闭.
     */
    @Override
    public void destroy() {
        warpperMap.forEach((k, v) -> v.destory());
    }

    /**
     * 初始化{@link ConnectionFactory},并构造{@link Connection}
     *
     * @throws Exception 创建连接失败，则抛出异常
     */
    
    @Override
    public void afterPropertiesSet() throws Exception {
        String queueNameStr = PropertiesUtils.getProperty(QUEUE_LIST_FLAG);
        if (StringUtils.isEmpty(queueNameStr))
            queueNameStr = DEFAULT_MQ_FLAG;
        else
            queueNameStr = queueNameStr + "," + DEFAULT_MQ_FLAG;
        Arrays.stream(queueNameStr.split(",")).forEach(queueName -> {
            String prefix = "";
            if (!queueName.equals(QueueConnectionFactoryImpl.DEFAULT_MQ_FLAG))
                prefix = queueName + ".";
            /**
             * 尝试查找queueName下的queueType，如果未配置，则默认为rabbit
             */
            String queueType = PropertiesUtils.getProperty(String.format(QUEUE_TYPE_FLAG, prefix));
            if (queueType == null)
                queueType = "rabbit";
            /**
             * 根据queueType，在spring中查找{@link QueueWarpperBuilder}实例，并构造{@link QueueWarpper}
             */
            QueueWarpperBuilder warpperBuilder = SpringContextHolder.getBean(queueType.toLowerCase() + "WarpperBuilder");
            if (warpperBuilder == null) {
                logger.error("Can't find " + queueType.toLowerCase() + "WarpperBuilder Bean from SpringContext!");
                return;
            }
            try {
                warpperMap.put(queueName, warpperBuilder.create(queueName));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
    }
}
