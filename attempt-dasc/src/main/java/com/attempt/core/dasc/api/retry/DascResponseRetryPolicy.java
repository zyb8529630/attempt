package com.attempt.core.dasc.api.retry;
/**
 * 用于发送{@link DascResopnse}消息的重试策略定义规范
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:19:15
 *
 */
public abstract class DascResponseRetryPolicy extends DascRetryPolicy<DascResopnse> {

    /**
     * 实现默认构造方法
     */
    public DascResponseRetryPolicy() {
        super(DascResopnse.class);
    }
}
