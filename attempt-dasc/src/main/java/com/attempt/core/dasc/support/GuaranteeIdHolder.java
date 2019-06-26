package com.attempt.core.dasc.support;

import java.util.HashMap;
import java.util.Map;

/** 
 * 对当前线程的guaranteeId持有.
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:09:59
 *
 */
public class GuaranteeIdHolder {

	 /**
     * ThradLocal的数据源变量.
     */
    private static final ThreadLocal<Map<String, String>> contextHolder = new ThreadLocal<>();


    private static final String REQUEST_GUARANTEE_ID_KEY = "REQUEST:%s";

    private static final String MULTI_REQUEST_GUARANTEE_ID_KEY = "MULTI_REQUEST:%s";

    private static final String RESPONSE_GUARANTEE_ID_KEY = "RESPONSE:%s";

    /**
     * 设置请求消息发送线程的保障登记ID.
     *
     * @param queueSource MQ数据源名称
     * @param guaranteeId guaranteeId
     */
    public static void setRequestGuaranteeId(String queueSource, String guaranteeId) {
        if (contextHolder.get() == null)
            contextHolder.set(new HashMap<>());
        String requestGuaranteeIdKey = String.format(REQUEST_GUARANTEE_ID_KEY, queueSource);
        if (contextHolder.get().containsKey(requestGuaranteeIdKey))
            throw new UnsupportedOperationException(requestGuaranteeIdKey + " have been existed in threadLocal!");
        contextHolder.get().put(requestGuaranteeIdKey, guaranteeId);
    }

    /**
     * 设置多播请求消息发送线程的保障登记ID.
     *
     * @param queueSource MQ数据源名称
     * @param guaranteeId guaranteeId
     */
    public static void setMultiRequestGuaranteeId(String queueSource, String guaranteeId) {
        if (contextHolder.get() == null)
            contextHolder.set(new HashMap<>());
        String multiRequestGuaranteeIdKey = String.format(MULTI_REQUEST_GUARANTEE_ID_KEY, queueSource);
        if (contextHolder.get().containsKey(multiRequestGuaranteeIdKey))
            throw new UnsupportedOperationException(multiRequestGuaranteeIdKey + " have been existed in threadLocal!");
        contextHolder.get().put(multiRequestGuaranteeIdKey, guaranteeId);
    }

    /**
     * 设置响应消息线程的保障登记ID.
     *
     * @param queueSource MQ数据源名称
     * @param guaranteeId guaranteeId
     */
    public static void setResponseGuaranteeId(String queueSource, String guaranteeId) {
        if (contextHolder.get() == null)
            contextHolder.set(new HashMap<>());
        String responseGuaranteeId = String.format(RESPONSE_GUARANTEE_ID_KEY, queueSource);
        if (contextHolder.get().containsKey(responseGuaranteeId))
            throw new UnsupportedOperationException(responseGuaranteeId + " have been existed in threadLocal!");
        contextHolder.get().put(responseGuaranteeId, guaranteeId);
    }

    /**
     * 获取当前线程发送多播请求消息的登记ID,如果未赋值,则返回NULL.
     *
     * @param queueSource MQ数据源名称
     * @return guaranteeId
     */
    public static String getMultiRequestGuaranteeId(String queueSource) {
        if (contextHolder.get() == null)
            return null;
        String multiRequestGuaranteeIdKey = String.format(MULTI_REQUEST_GUARANTEE_ID_KEY, queueSource);
        return contextHolder.get().get(multiRequestGuaranteeIdKey);
    }

    /**
     * 获取当前线程发送响应消息的登记ID,如果未赋值,则返回NULL.
     *
     * @param queueSource MQ数据源名称
     * @return guaranteeId
     */
    public static String getResponseGuaranteeId(String queueSource) {
        if (contextHolder.get() == null)
            return null;
        String responseGuaranteeId = String.format(RESPONSE_GUARANTEE_ID_KEY, queueSource);
        return contextHolder.get().get(responseGuaranteeId);
    }

    /**
     * 获取当前线程发送请求消息的登记ID,如果未赋值,则返回NULL.
     *
     * @param queueSource MQ数据源名称
     * @return guaranteeId
     */
    public static String getRequestGuaranteeId(String queueSource) {
        if (contextHolder.get() == null)
            return null;
        String requestGuaranteeIdKey = String.format(REQUEST_GUARANTEE_ID_KEY, queueSource);
        return contextHolder.get().get(requestGuaranteeIdKey);
    }

    /**
     * 清除DASC调用方标识
     */
    public static void clear() {
        contextHolder.remove();
    }

    /**
     * 判断线程变量是否已清空
     */
    public static boolean isClear() {
        return contextHolder.get() == null;
    }

}
