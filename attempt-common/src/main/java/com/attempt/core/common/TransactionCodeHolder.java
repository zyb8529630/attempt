package com.attempt.core.common;

import org.slf4j.MDC;

import com.attempt.core.common.util.TransactionUtils;

/**
 * 对当前线程的交易码的Holder.
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:44:51
 *
 */
public class TransactionCodeHolder {
	
    /**
     * ThradLocal的交易码变量.
     */
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 私有化构造函数
     */
    private TransactionCodeHolder() {
    }

    /**
     * 获取当前线程的交易码,如果未赋值,则返回NULL..
     *
     * @return the transcation info
     */
    public static String get() {
        return contextHolder.get();
    }

    /**
     * 设置当前线程交易码.
     * 向slf4j的MDC对象中放入交易码.
     *
     * @param tranCode the tran code
     */
    public static void set(String tranCode) {
        contextHolder.set(tranCode);
        MDC.put(TransactionUtils.TRANSCATION_CODE_FLAG, TransactionCodeHolder.get());
    }

    /**
     * 清除交易码
     */
    public static void clear() {
        contextHolder.remove();
    }
}
