package com.attempt.core.log.models;
/**
 * 标识日志的ACT位.
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:53:50
 *
 */
public enum ACT {

	 /**
     * 标识当前日志为交易流水号转换
     */
    TRAN_TCODE,
    /**
     * 标识当前日志为常规内容输出
     */
    LOG,
    /**
     * 当前服务方法的起始标识位
     */
    MED_START,
    /**
     * 当前服务方法的结束标识位
     */
    MED_END,
    /**
     * 当前服务方法的参数列表
     */
    MED_ARGS,
    /**
     * 当前服务方法的返回值数据
     */
    MED_RESP,
    /**
     * 当前服务方法的异常信息
     */
    MED_EXP,
    /**
     * 接口输出日志
     */
    ENV_OUT,
    /**
     * 接口输入日志
     */
    ENV_IN,
    /**
     * 接口异常日志
     */
    ENV_EXP,
    /**
     * WEB异常日志
     */
    WEB_EXP
}
