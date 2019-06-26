package com.attempt.core.log.models;
/** 
 * 标识框架日志打印的类型.
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:22:21
 *
 */
public enum SysLogType {

	 /**
     * Spring业务服务层日志
     */
    ATTEMPT_SEV,
    /**
     * Spring的DAO层日志
     */
    ATTEMPT_DAO,
    /**
     * 接口报文日志
     */
    ATTEMPT_ENV,
    /**
     * Spring Controller层日志
     */
    ATTEMPT_WEB
}
