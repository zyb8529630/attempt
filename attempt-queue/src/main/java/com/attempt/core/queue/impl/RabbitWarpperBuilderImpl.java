package com.attempt.core.queue.impl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attempt.core.common.util.PropertiesUtils;
import com.attempt.core.queue.api.QueueWarpperBuilder;
import com.attempt.core.queue.models.QueueWarpper;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 创建RabbitMQ的配置和连接实例.
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:25:55
 *
 */
public class RabbitWarpperBuilderImpl implements QueueWarpperBuilder {
	
	/**
     * 日志管理
     */
    private static Logger logger = LoggerFactory.getLogger(RabbitWarpperBuilderImpl.class);

    /**
     * 消息队列broker的地址.用;分割
     */
    private static final String QUEUE_ADDRESS_FLAG = "%squeue.addresses";
    /**
     * 消息队列上的虚拟主机标识
     */
    private static final String QUEUE_VHOST_FLAG = "%squeue.vhost";
    /**
     * 连接时的用户名
     */
    private static final String QUEUE_USERNAME_FLAG = "%squeue.username";
    /**
     * 连接时的密码
     */
    private static final String QUEUE_PASSWORD_FLAG = "%squeue.password";
    /**
     * 是否自动恢复连接
     */
    private static final String QUEUE_RECOVERY_ENABLED_FLAG = "%squeue.recovery.enabled";
    /**
     * 连接自动恢复的执行间隔(毫秒)
     */
    private static final String QUEUE_RECOVERY_INTERVAL_FLAG = "%squeue.recovery.interval";
    /**
     * 对broker的心跳检测间隔(秒)
     */
    private static final String QUEUE_HEARTBEAT_INTERVAL_FLAG = "%squeue.heartbeat.interval";
    
	/**
	 * 创建RabbitMQ的warpper实例
	 * queueName 标识的队列名称。
	 */
	@Override
	public QueueWarpper create(String queueName) {
		
		QueueWarpper warpper = new QueueWarpper();		
        String prefix = "";
        if (!queueName.equals(QueueConnectionFactoryImpl.DEFAULT_MQ_FLAG))
            prefix = queueName + ".";
        Address[] addresses = Address.parseAddresses(PropertiesUtils.getProperty(String.format(QUEUE_ADDRESS_FLAG, prefix)));
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(PropertiesUtils.getProperty(String.format(QUEUE_USERNAME_FLAG, prefix)));
        connectionFactory.setPassword(PropertiesUtils.getProperty(String.format(QUEUE_PASSWORD_FLAG, prefix)));
        connectionFactory.setVirtualHost(PropertiesUtils.getProperty(String.format(QUEUE_VHOST_FLAG, prefix)));  
        
        /**
         * 是否自动恢复连接
         */
        String recoveryEnabledStr = PropertiesUtils.getProperty(String.format(QUEUE_RECOVERY_ENABLED_FLAG, prefix));
        if (StringUtils.isEmpty(recoveryEnabledStr))
            recoveryEnabledStr = PropertiesUtils.getProperty(String.format(QUEUE_RECOVERY_ENABLED_FLAG, ""));
        connectionFactory.setAutomaticRecoveryEnabled(Boolean.parseBoolean(recoveryEnabledStr));
        
       
        /**
         * 连接自动恢复的执行间隔(毫秒)
         */
        String recoveryIntervalStr = PropertiesUtils.getProperty(String.format(QUEUE_RECOVERY_INTERVAL_FLAG, prefix));
        if (StringUtils.isEmpty(recoveryIntervalStr))
            recoveryIntervalStr = PropertiesUtils.getProperty(String.format(QUEUE_RECOVERY_INTERVAL_FLAG, ""));
        connectionFactory.setNetworkRecoveryInterval(Integer.parseInt(recoveryIntervalStr));	
        
        /**
         * 对broker的心跳检测间隔(秒)
         */
        String heartbeatIntervalStr = PropertiesUtils.getProperty(String.format(QUEUE_HEARTBEAT_INTERVAL_FLAG, prefix));
        if (StringUtils.isEmpty(heartbeatIntervalStr))
            heartbeatIntervalStr = PropertiesUtils.getProperty(String.format(QUEUE_HEARTBEAT_INTERVAL_FLAG, ""));
        connectionFactory.setRequestedHeartbeat(Integer.parseInt(heartbeatIntervalStr));
        warpper.setConnectionFactory(connectionFactory);
        
        try {
            /**
             * 设定消费者连接
             */
            Connection consumerConnection = connectionFactory.newConnection(addresses);
            consumerConnection.addBlockedListener(new BlockedListener() {
                @Override
                public void handleBlocked(String reason) throws IOException {
                    warpper.setConsumerAccessable(false);
                }

                @Override
                public void handleUnblocked() throws IOException {
                    warpper.setConsumerAccessable(true);
                }
            });
            /**
             * 设定生产者连接
             */
            warpper.setConsumerConnection(consumerConnection);
            Connection producerConnection = connectionFactory.newConnection(addresses);
            producerConnection.addBlockedListener(new BlockedListener() {
                @Override
                public void handleBlocked(String reason) throws IOException {
                    warpper.setProducerAccessable(false);
                }

                @Override
                public void handleUnblocked() throws IOException {
                    warpper.setProducerAccessable(true);
                }
            });
            warpper.setProducerConnection(producerConnection);
        } catch (Exception e) {
            throw new RuntimeException("Can't connect RabbitMQ [" + queueName + "]", e);
        }
        return warpper;        
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
