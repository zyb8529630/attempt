package com.attempt.core.queue.annotion;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息路由的注解
 * mq.propertiest的格式：prefix+.queue.[xxx]，
 * 举例：prefix=myname，则mq的address为myname.queue.addresses对应的值
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:12:15
 *
 */   

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueSource {

	String value();	
}
