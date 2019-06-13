package com.attempt.core.exception;

import org.springframework.util.Assert;

/**
 * 抽象的业务异常对象.
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:46:01
 *
 */
public class BusinessException  extends RuntimeException {
 
	private static final long serialVersionUID = 1L;

    /**
     * 业务错误码. 默认0000
     */
    private String errorCode = "00000";

    /**
     * 构造方法.
     *
     * @param message 异常消息
     * @param cause   关联异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造方法.
     *
     * @param cause 关联异常
     */
    public BusinessException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * 构造方法.
     *
     * @param errorCode 错误码
     * @param message   异常消息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        Assert.notNull(errorCode);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码.
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
}
