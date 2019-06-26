package com.attempt.core.dasc.api.retry;

import java.io.Serializable;

/**
 * 用于DASC服务重试策略的封装定义
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:19:56
 *
 */
public abstract class DascRetryPolicy <T extends Serializable> implements RetryPolicy {

}
