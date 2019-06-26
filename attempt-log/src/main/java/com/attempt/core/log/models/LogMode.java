package com.attempt.core.log.models;
/**
 * 框架日志的运行模式.
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:27:14
 *
 */
public enum LogMode {

	 /**
     * 黑名单模式：仅针对特定的类路径方法调用明细做记录记录
     */
    BLACK_LIST,
    /**
     * 白名单模式：除了特定的类路径外，其它方法的调用明细都要记录
     */
    WHITE_LIST
}
