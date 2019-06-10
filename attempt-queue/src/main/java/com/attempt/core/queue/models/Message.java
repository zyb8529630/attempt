package com.attempt.core.queue.models;

import java.io.Serializable;

/**
 * MQ中传递的消息内容.
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:59:43
 *
 */
public class Message <T extends Serializable> implements Serializable {

	 /**
     * 消息ID
     */
    private String messageId;
    /**
     * 交易码
     */
    private String transactionCode;
    /**
     * 来源系统
     */
    private String orginSystem;
    /**
     * 消息体
     */
    private T messageBody;
    
    
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getTransactionCode() {
		return transactionCode;
	}
	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}
	public String getOrginSystem() {
		return orginSystem;
	}
	public void setOrginSystem(String orginSystem) {
		this.orginSystem = orginSystem;
	}
	public T getMessageBody() {
		return messageBody;
	}
	public void setMessageBody(T messageBody) {
		this.messageBody = messageBody;
	}

    
}
