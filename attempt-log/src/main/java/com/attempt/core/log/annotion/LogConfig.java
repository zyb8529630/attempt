package com.attempt.core.log.annotion;
/**
 * 在Spring托管的Bean中，对特定的方法的日志配置.
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:25:23
 *
 */
public @interface LogConfig {

	  /**
     * 是否打开日志记录开关（默认为true）
     *
     * @return true
     */
    boolean enabled() default true;

    /**
     * 是否打开方法调用明细的日志写入。包括调用参数列表，调用后返回结果
     *
     * @return true
     */
    boolean detail() default true;

    /**
     * 是否对方法调用做计数日志写入。
     *
     * @return true
     */
    boolean counter() default true;

    /**
     * 是否对方法调用时间做日志统计（目前尚不支持）
     *
     * @return true
     */
    boolean timer() default true;

    /**
     * 是否对方法调用时产生异常时，做框架级自动日志记录
     *
     * @return true

     */
    boolean error() default true;
}
