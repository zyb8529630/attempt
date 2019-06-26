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
import com.attempt.core.dasc.support.GuaranteeIdHolder;
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
     * 在客户端存在数据库事务时，DASC在事务与消息发送之间提供的发送保证服务
     */
    @Autowired
    private SendGuarantee sendGuarantee;
    
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
    	 /**
         * 判断并标识出当前方法路径是否与{@link CallerIdHolder}的callerId相同
         */
        boolean haveSameCallerId = DascUtils.generateCallerId(method).equals(CallerIdHolder.get());
        /**
         * 通过判断DASC服务调用次数和对象集合，判断当前线程是否调用过DASC服务
         */
        if (!CallCounterHolder.exist() || !RequestHolder.exist()) {
            if (logger.isDebugEnabled())
                logger.debug("DascMessage[{}] doesn't actual call any dasc service!", CallerIdHolder.get());
            /**
             * 为了避免出现清除掉@Transcation注解产生的CallerId，只在当前方法路径与CallerId相同的情况下，才清除线程变量。
             */
            if (haveSameCallerId)
                requestProducer.clearThreadValues();
            /**
             * 直接返回，不会做其他判断和逻辑尝试
             */
            return;
        }

        /**
         * 判断当前线程有没有活动事务
         * 没有：可以直接执行后续逻辑判断。
         * 有：说明@AsynCall方法代码被包含在外部的一个事务中，要继续通过{@link AsynCall#transactional()} 来判断是否继续发送消息
         *      {@link AsynCall#transactional()} == true: 需要考虑外部事务，通过doTransactionFinish对应的切面来完成消息发送
         *      {@link AsynCall#transactional()} == false: 不需要考虑外部事务，直接发送消息
         */
        boolean returnBecauseOfTranscation = false;
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            AsynCall methodAnnotation = AnnotationUtils.findAnnotation(method, AsynCall.class);
            if (methodAnnotation.transactional()) {
                if (logger.isDebugEnabled())
                    logger.debug("Can't send DascMessage[{}] immediately!", CallerIdHolder.get());
                returnBecauseOfTranscation = true;
                /**
                 * 如果存在事务，则先判断当前方法路径是否与{@link CallerIdHolder}的callerId相同，
                 * 如果相同，则尝试使用DASC自带的消息与事务间一致性保证服务，并将ID放入当前线程。
                 * 如果不同，则期望该逻辑在与callerId相同的方法路径切面中再执行。
                 */
                if (haveSameCallerId) {
                    RequestHolder.get().forEach((queueSource, requestMessage) -> {
                        GuaranteeIdHolder.setRequestGuaranteeId(queueSource,
                                sendGuarantee.hold(requestMessage,
                                        "*", queueSource, EventType.SEND_REQUEST));
                    });
                }

            }
        }

        /**
         * 通过对当前method生成的callId与{@link CallerIdHolder}中存储的callId比对，可以验证并跳过以下情况：
         * 1. {@link com.halo.core.dao.annotation.Transaction}注解在{@link AsynCall}注解之外，callId按照事务切面来声明，因此产生不一致。
         * 2. {@link AsynCall}注解出现嵌套，内部的{@link AsynCall}对应的dasc调用所用的callId会强制与外部{@link AsynCall}注解的callId保持一致，
         * 也会跟随外部的{@link AsynCall}注解的闭合而发送。
         * 3. 在DASC服务端中调用其他DASC服务时，会保证在DASC服务类逻辑执行域结束时，再进行统一发送逻辑的判断。
         */
        boolean returnBecauseOfCallerIdDiff = false;
        if (!haveSameCallerId) {
            if (logger.isDebugEnabled())
                logger.debug("Parent DascMessage[{}] has been existed!", CallerIdHolder.get());
            returnBecauseOfCallerIdDiff = true;
        }
        /**
         * 根据对当前事务的判断情况和callerId的差异判断，决定是否直接返回当前方法调用
         */
        if (returnBecauseOfTranscation || returnBecauseOfCallerIdDiff)
            return;
        /**
         * 首先，填充完善当前线程下的所有待发送消息的内容，然后立即发送消息
         */
        RequestHolder.get().forEach((queueSource, requestMessage) -> {
            try {
                /**
                 * 配置消息体中的服务参数列表数据访问方式
                 */
                requestProducer.buildMessageArgsData(queueSource, requestMessage);
                /**
                 * 配置消息体中服务多次调用过程做记录
                 */
                requestProducer.buildMessageLoopCallData(queueSource, requestMessage);
                /**
                 * 立即发送消息
                 */
                if (logger.isDebugEnabled())
                    logger.debug("Send DascMessage [{}] after @asyncall to Queue[{}]",
                            requestMessage.getMessageBody().getCallerId(),
                            queueSource);
                requestProducer.sendMessageImmediately(queueSource, requestMessage);
                /**
                 * 记录发送轨迹
                 */
                DascUtils.trackSendRequestMessage(queueSource, requestMessage, null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                /**
                 * 记录发送轨迹
                 */
                DascUtils.trackSendRequestMessage(queueSource, requestMessage, e);
            }
        });
        /**
         * 一定要清除线程上各种数据
         */
        requestProducer.clearThreadValues();
		
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
