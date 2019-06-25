package com.attempt.core.dasc.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 标识某个服务代码中包含对其他异步服务的调用.
 * 框架会通过DASC保证数据的最终一致性
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:34:51
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AsynCall {
	
	 /**
     * 约定DASC服务调用是否考虑本地事务性要求
     *
     * @return true: 考虑本地事务性要求，在事务提交后再发送消息
     */
    boolean transactional() default true;

}
