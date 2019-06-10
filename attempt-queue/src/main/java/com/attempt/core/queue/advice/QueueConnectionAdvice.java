package com.attempt.core.queue.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

import com.attempt.core.queue.annotion.QueueSource;
import com.attempt.core.queue.support.QueueConnectionHolder;
/**
 * 消息队列动态路由的AOP切面.
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:11:54
 *
 */
public class QueueConnectionAdvice {

    public void doBefore(JoinPoint joinPoint) throws Exception {
    	
        // 获取方法签名
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method instanceMethod = joinPoint.getTarget().getClass().getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
        QueueSource annotation = AnnotationUtils.findAnnotation(instanceMethod, QueueSource.class);
        if (annotation == null)
            return;
        QueueConnectionHolder.set(annotation.value());
        
    }

    public void doAfter(JoinPoint joinPoint) {
        QueueConnectionHolder.clear();
    }

    public void afterThrowing(JoinPoint joinPoint, RuntimeException throwable) {
        QueueConnectionHolder.clear();
    }

}
