package com.attempt.core.dasc.support;

import java.util.HashMap;
import java.util.Map;

import com.attempt.core.dasc.models.DascRequest;
import com.attempt.core.queue.models.Message;

/**
 * 对当前线程的DASC调用的消息内容.
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:03:00
 *
 */
public class RequestHolder {
	
	 /**
     * ThradLocal的数据源变量.
     */
    private static final ThreadLocal<Map<String, Message<DascRequest>>> contextHolder = new ThreadLocal<>();

    /**
     * 向当前线程中添加待发送的消息.
     *
     * @param queueSource MQ数据源名称
     * @param message     DASC调用方消息
     */
    public static void set(String queueSource, Message<DascRequest> message) {
        if (contextHolder.get() == null)
            contextHolder.set(new HashMap<>());
        contextHolder.get().put(queueSource, message);
    }

    /**
     * 判断当前线程是否存在未发送的消息
     */
    public static boolean exist() {
        return contextHolder.get() != null
                && !contextHolder.get().isEmpty();
    }

    /**
     * 判断当前线程是否存在未发送的消息
     *
     */
    public static boolean exist(String queueSource) {
        return contextHolder.get() != null
                && !contextHolder.get().isEmpty()
                && contextHolder.get().containsKey(queueSource);
    }

    /**
     * 获取当前线程的DASC调用的消息
     *
     */
    public static Message<DascRequest> get(String queueSource) {
        if (contextHolder.get() == null)
            return null;
        return contextHolder.get().get(queueSource);
    }

    /**
     * 获取当前线程的DASC调用的消息
     *
     */
    public static Map<String, Message<DascRequest>> get() {
        return contextHolder.get();
    }

    /**
     * 清除DASC调用消息列表
     */
    public static void clear() {
        contextHolder.remove();
    }
}
