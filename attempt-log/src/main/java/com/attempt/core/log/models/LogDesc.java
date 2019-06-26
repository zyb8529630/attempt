package com.attempt.core.log.models;

import java.lang.reflect.Method;

import com.attempt.core.log.annotion.LogConfig;

/**
 *用于描述被做日志记录的{@link Method}的元数据.
 * 用于以下系统日志：
 * 1. {@link SysLogType#ATTEMPT_DAO}
 * 2. {@link SysLogType#ATTEMPT_SEV}
 * 3. {@link SysLogType#ATTEMPT_ENV}
 * 4. {@link SysLogType#ATTEMPT_WEB}
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:24:10
 *
 */
public class LogDesc {

	/**
     * 当前流水交易码
     */
    private String transactionCode;
    /**
     * 执行的method方法
     */
    private Method method;
    /**
     * 日志上的路径
     */
    private String logPath;
    /**
     * 是否启用日志
     */
    private boolean enabled = true;
    /**
     * 方法上的日志配置注解
     */
    private LogConfig logConfig;

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
    }
}
