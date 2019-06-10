package com.attempt.core.queue.models;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * RabbitMQ消息队列的配置集合.
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:22:29
 *
 */
public class QueueWarpper {
	
	 /**
     * 日志管理
     */
    private static Logger logger = LoggerFactory.getLogger(QueueWarpper.class);

    /**
     * RabbitMQ的连接构造工具
     */
    private ConnectionFactory connectionFactory;
    
    /**
     * 生产者连接
     */
    private Connection producerConnection;
    
    /**
     * 生产者连接是否可访问
     */
    private AtomicBoolean producerAccessable = new AtomicBoolean(true);
    
    /**
     * 消费者连接
     */
    private Connection consumerConnection;

    /**
     * 消费者连接是否可访问
     */
    private AtomicBoolean consumerAccessable = new AtomicBoolean(true);

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public Connection getProducerConnection() {
		return producerConnection;
	}

	public void setProducerConnection(Connection producerConnection) {
		this.producerConnection = producerConnection;
	}

	public boolean getProducerAccessable() {
		return producerAccessable.get();
	}

	public void setProducerAccessable(boolean accessable) {
		 this.producerAccessable.set(accessable);
	}

	public Connection getConsumerConnection() {
		return consumerConnection;
	}

	public void setConsumerConnection(Connection consumerConnection) {
		this.consumerConnection = consumerConnection;
	}

	public boolean getConsumerAccessable() {
		return consumerAccessable.get();
	}

	public void setConsumerAccessable(boolean accessable) {
	        this.consumerAccessable.set(accessable);
	 }
       
	 /**
     * 关闭MQ的连接
     */
    public void destory() {
        /**
         * 关闭生产者连接
         */
        try {
            if (producerConnection != null)
                producerConnection.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        /**
         * 关闭消费者连接
         */
        try {
            if (consumerConnection != null)
                consumerConnection.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }   
}
