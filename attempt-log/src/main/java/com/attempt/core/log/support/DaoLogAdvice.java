package com.attempt.core.log.support;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.attempt.core.log.models.ACT;
import com.attempt.core.log.models.SysLogType;
import com.attempt.core.log.util.LogUtils;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 对Spring的服务层做日志切面
 * @author zhouyinbin
 * @date 2019年6月26日 上午10:51:36
 *
 */
public class DaoLogAdvice extends AbstractLogAdvice {

    /**
     * 日志管理工具实例.
     */
    private static Logger logger = LoggerFactory.getLogger(SysLogType.ATTEMPT_DAO.toString());

    @Autowired
    private LogDescParser logDescParser;

    /**
     * 服务方法执行前的日志记录
     *
     * @param joinPoint 切入点
     */
	@Override
	public void beforeInvoke(JoinPoint joinPoint) {
		/**
         * 将{@code path}放入{@link MDC}中
         */
        Method instanceMethod = parseInstanceMethod(joinPoint);
        MDC.put(LogUtils.PATH_FLAG, LogUtils.generatePath(instanceMethod));
        /**
         * 将PinPoint的trackId共享给响应式线程
         */
        final String ptxId = MDC.get(LogUtils.PTXID_FLAG);
        Observable.just(logDescParser.generateLogDesc(instanceMethod))
                .observeOn(Schedulers.io()).subscribe(methodLogDesc -> {
            /**
             * 如果禁用日志，则不再输出日志
             */
            if (!methodLogDesc.isEnabled())
                return;
            /**
             * 将{@code transactionCode}放入{@link MDC}中
             */
            MDC.put(LogUtils.TRANSCATION_CODE_FLAG, methodLogDesc.getTransactionCode());
            /**
             * 将PinPoint的trackId放入{@link MDC}中
             */
            MDC.put(LogUtils.TRACKID_FLAG, ptxId);
            /**
             * 将{@code path}放入{@link MDC}中
             */
            MDC.put(LogUtils.PATH_FLAG, methodLogDesc.getLogPath());
            /**
             * 写入方法起始标识位
             * 1. 生成{@code act}，并填入{@link MDC}
             * 2. 输出日志
             */
            if (methodLogDesc.getLogConfig() == null || methodLogDesc.getLogConfig().counter()) {
                MDC.put(LogUtils.ACT_FLAG, ACT.MED_START.toString());
                logger.info(LogUtils.EMPTY_CONTENT);
            }
            /**
             * 切换为常规日志输出标识位
             */
            MDC.remove(LogUtils.TRACKID_FLAG);
            MDC.remove(LogUtils.PATH_FLAG);
            MDC.put(LogUtils.ACT_FLAG, ACT.LOG.toString());
        });
        
		
	}
	/**
     * 服务方法执行后的日志记录
     *
     * @param joinPoint 切入点
     * @param result    返回值信息
     */
	@Override
	public void afterInvoke(JoinPoint joinPoint, Object result) {
		MDC.remove(LogUtils.PATH_FLAG);
        /**
         * 将PinPoint的trackId共享给响应式线程
         */
        final String ptxId = MDC.get(LogUtils.PTXID_FLAG);
        Observable.just(logDescParser.generateLogDesc(parseInstanceMethod(joinPoint)))
                .observeOn(Schedulers.io()).subscribe(methodLogDesc -> {
            /**
             * 如果禁用日志，则不再输出日志
             */
            if (!methodLogDesc.isEnabled())
                return;
            /**
             * 将{@code transactionCode}放入{@link MDC}中
             */
            MDC.put(LogUtils.TRANSCATION_CODE_FLAG, methodLogDesc.getTransactionCode());
            /**
             * 将PinPoint的trackId放入{@link MDC}中
             */
            MDC.put(LogUtils.TRACKID_FLAG, ptxId);
            /**
             * 将{@code path}放入{@link MDC}中
             */
            MDC.put(LogUtils.PATH_FLAG, methodLogDesc.getLogPath());
            /**
             * 写入方法结束标识位
             * 1. 生成{@code act}，并填入{@link MDC}
             * 2. 输出日志
             */
            if (methodLogDesc.getLogConfig() == null || methodLogDesc.getLogConfig().counter()) {
                MDC.put(LogUtils.ACT_FLAG, ACT.MED_END.toString());
                logger.info(LogUtils.EMPTY_CONTENT);
            }
            /**
             * 切换为常规日志输出标识位
             */
            MDC.remove(LogUtils.TRACKID_FLAG);
            MDC.remove(LogUtils.PATH_FLAG);
            MDC.put(LogUtils.ACT_FLAG, ACT.LOG.toString());
        });
	}

    /**
     * 在抛出异常后，由统一方法进行日志记录.
     *
     * @param joinPoint 切入点
     * @param throwable 抛出的异常实例
     */
	@Override
	public void afterThrowing(JoinPoint joinPoint, RuntimeException throwable) {
		 /**
         * 将PinPoint的trackId共享给响应式线程
         */
        final String ptxId = MDC.get(LogUtils.PTXID_FLAG);
        Observable.just(logDescParser.generateLogDesc(parseInstanceMethod(joinPoint)))
                .observeOn(Schedulers.io()).subscribe(methodLogDesc -> {
            /**
             * 将{@code transactionCode}放入{@link MDC}中
             */
            MDC.put(LogUtils.TRANSCATION_CODE_FLAG, methodLogDesc.getTransactionCode());
            /**
             * 将PinPoint的trackId放入{@link MDC}中
             */
            MDC.put(LogUtils.TRACKID_FLAG, ptxId);
            /**
             * 将{@code path}放入{@link MDC}中
             */
            MDC.put(LogUtils.PATH_FLAG, methodLogDesc.getLogPath());
            /**
             * 写入方法结束标识位
             * 1. 生成{@code act}，并填入{@link MDC}
             * 2. 输出日志
             */
            if (methodLogDesc.getLogConfig() == null || methodLogDesc.getLogConfig().counter()) {
                MDC.put(LogUtils.ACT_FLAG, ACT.MED_END.toString());
                logger.info(LogUtils.EMPTY_CONTENT);
            }
            /**
             * 写入方法调用异常信息
             * 1. 生成{@code act}，并填入{@link MDC}
             * 2. 输出日志
             */
            if (methodLogDesc.isEnabled() && (methodLogDesc.getLogConfig() == null || methodLogDesc.getLogConfig().error())) {
                MDC.put(LogUtils.ACT_FLAG, ACT.MED_EXP.toString());
                logger.error(throwable.getMessage(), throwable);
            }
            /**
             * 切换为常规日志输出标识位
             */
            MDC.remove(LogUtils.TRACKID_FLAG);
            MDC.remove(LogUtils.PATH_FLAG);
            MDC.put(LogUtils.ACT_FLAG, ACT.LOG.toString());
        });
		
	}
    
    
}
