package com.attempt.core.dasc.client.impl;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.attempt.core.common.TransactionCodeHolder;
import com.attempt.core.common.util.TransactionUtils;
import com.attempt.core.dasc.annotation.AsynCall;
import com.attempt.core.dasc.support.CallerIdHolder;
import com.attempt.core.dasc.support.RequestHolder;
import com.attempt.core.dasc.utils.DascUtils;


/**
 * 用于对动态数据源进行配置的切面AOP拦截器.
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:36:16
 *
 */
public class AsynCallAdvice {	
	
	 /**
     * 日志管理.
     */
    private static Logger logger = LoggerFactory.getLogger(AsynCallAdvice.class);
    
    /**
     * 请求消息的生产者
     */
    @Autowired
    private RequestProducer requestProducer;

    /**
     * 在每个带有{@link AsynCall}注解的方法调用之前
     * 对要声明的消息队列和Exchange的相关配置信息进行填充
     *
     */
    public void doBefore(JoinPoint joinPoint) throws Exception {
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method instanceMethod = joinPoint.getTarget().getClass()
                .getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
        doBeforeMethod(instanceMethod);
    }

    /**
     * 在每个带有{@link AsynCall}注解的方法调用之后
     * 清除当前线程的相关配置信息
     */
    public void doAfter(JoinPoint joinPoint) throws Exception {
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method instanceMethod = joinPoint.getTarget().getClass()
                .getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
        doAfterMethod(instanceMethod);
    }

	/**
     * 在每个带有{@link AsynCall}注解的方法调用之前
     * 对要声明的消息队列和Exchange的相关配置信息进行填充
     */
    public void doBeforeMethod(Method method) {
    	/**
         * 确认交易码存在,如果不存在，则生成
         */
        if (StringUtils.isEmpty(TransactionCodeHolder.get()))
            TransactionCodeHolder.set(TransactionUtils.generateTransactionCode());
        /**
         * 根据method，定义callId，判断并填入当前线程中
         */
        String callId = DascUtils.generateCallerId(method);
        AsynCall methodAnnotation = AnnotationUtils.findAnnotation(method, AsynCall.class);	
        
        /**
         * 如果{@link CallerIdHolder}不存在值，可以定义{@link CallerIdHolder}的callerId。
         */
        if (CallerIdHolder.get() == null) {
            CallerIdHolder.set(callId);
        }
        
        /**
         * 如果@AsynCall的transactional设置为false, 可以重新定义{@link CallerIdHolder}的callerId。
         * 注：该判断会在transcation-outside的情况下清除掉@Transcation注解声明的callerId信息。
         * 因此，transactional属性设置谨慎使用!!
         */
        else if (!methodAnnotation.transactional()) {
            if (logger.isDebugEnabled())
                logger.debug("Will remove DascMessage[{}]'s callerId !", CallerIdHolder.get());
            CallerIdHolder.set(callId);
        } else {
            if (logger.isDebugEnabled())
                logger.debug("Parent DascMessage[{}] has been there!", CallerIdHolder.get());
            callId = CallerIdHolder.get();
        }
    
    }
    
    /**
     * 在每个带有{@link AsynCall}注解的方法调用之后
     * 清除当前线程的相关配置信息
     */
    private void doAfterMethod(Method instanceMethod) {
		// TODO Auto-generated method stub
		
	}
    
    
    /**
     * 在抛出异常后，清除线程绑定变量.
     */
    public void afterThrowing(JoinPoint joinPoint, RuntimeException throwable) {
        String currentCallerId = CallerIdHolder.get();
        /**
         * 判断当前线程的callerId是否是本次闭合的事务声明，按需清除{@link CallerIdHolder}
         */
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        try {
            Method instanceMethod = joinPoint.getTarget().getClass()
                    .getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
            if (DascUtils.generateCallerId(instanceMethod).equals(currentCallerId)) {
                if (logger.isDebugEnabled())
                    logger.debug("Cause by throwing exception, clear thread values!");
                requestProducer.clearThreadValues();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    

}
