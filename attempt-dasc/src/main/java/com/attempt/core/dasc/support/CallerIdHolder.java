package com.attempt.core.dasc.support;
/**
 * 对当前线程的DASC调用方标识Holder.
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:42:07
 *
 */
public class CallerIdHolder {
	
	 /**
     * ThradLocal的数据源变量.
     */
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 设置当前线程DASC调用方标识.
     *
     * @param callerId DASC调用方标识
     */
    public static void set(String callerId) {
        contextHolder.set(callerId);
    }

    /**
     * 获取当前线程的DASC调用方标识,如果未赋值,则返回NULL.
     *
     * @return DASC调用方标识
     */
    public static String get() {
        return contextHolder.get();
    }

    /**
     * 清除DASC调用方标识
     */
    public static void clear() {
        contextHolder.remove();
    }
}
