package com.attempt.core.log.appender;

import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.util.Assert;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * kafka日志记录appender.
 * @author zhouyinbin
 * @date 2019年6月26日 上午11:04:19
 *
 */
public class KafkaAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	/**
     * kafka连接实例
     */
    private KafkaProducer<String, String> producer;
    /**
     * 对日志内容的格式化布局实例
     */
    private PatternLayout patternLayout;
    /**
     * 连接的topic
     */
    private String topic;
    /**
     * kafka地址清单，逗号分割
     */
    private String brokerList;
    /**
     * kafka连接其他配置属性。以key=value形式，逗号分割
     */
    private String properties;
    /**
     * 日志内容格式化表达式
     */
    private String pattern;
    
	@Override
	protected void append(ILoggingEvent eventObject) {
		 if (producer == null)
	            return;
	        String payload = patternLayout.doLayout(eventObject);
	        producer.send(new ProducerRecord<>(topic, null, payload));
		
	}
	/**
     * 覆盖启动方法
     * 连接kafka，建立长连接
     *
     * @author maeagle
     * @date 2017-3-22 15:23:12
     */
    @Override
    public void start() {

        /**
         * 检查kafka的配置
         */
        Assert.notNull(brokerList, "The kafka broker can't be empty!");
        Assert.notNull(topic, "The kafka topic can't be empty!");
        /**
         * 检查消息表达式配置
         */
        Assert.notNull(pattern, "The pattern can't be empty!");

        /**
         * 初始化patternLayout对象
         */
        patternLayout = new PatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(pattern);
        patternLayout.setOutputPatternAsHeader(false);
        patternLayout.start();

        /**
         * 建立kafka连接
         */
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        if (StringUtils.isNotEmpty(properties))
            Arrays.stream(properties.split(",")).forEach(itemStr -> {
                String[] item = itemStr.split("=");
                props.put(item[0], item[1]);
            });
        try {
            producer = new KafkaProducer<>(props);
            // 通过判断是否存在设置的topic，作为kafka连通性的判断条件
            Assert.notEmpty(producer.partitionsFor(topic));
            super.start();
        } catch (KafkaException e) {
            // 如果抛出异常，则将producer置为null
            if (producer != null) {
                producer.close();
                producer = null;
            }
            throw e;
        }

    }

    /**
     * 覆盖关闭方法
     *
     * @author maeagle
     * @date 2017-3-22 15:23:12
     */
    @Override
    public void stop() {
        super.stop();
        patternLayout.stop();
        if (producer != null)
            producer.close();
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBrokerList(String brokerList) {
        this.brokerList = brokerList;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getTopic() {
        return topic;
    }

    public String getBrokerList() {
        return brokerList;
    }

    public String getProperties() {
        return properties;
    }

    public String getPattern() {
        return pattern;
    }
	

}
