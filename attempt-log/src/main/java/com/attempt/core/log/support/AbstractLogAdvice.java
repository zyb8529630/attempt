package com.attempt.core.log.support;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.alibaba.fastjson.JSON;

/**
 *    日志切面的抽象类.
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:12:38
 *
 */
public abstract class AbstractLogAdvice{

	/**
     * 服务方法执行前的日志记录
     *
     * @param joinPoint 切入点
     */
    public abstract void beforeInvoke(JoinPoint joinPoint);
    
    /**
     * 服务方法执行后的日志记录
     *
     * @param joinPoint 切入点
     * @param result    返回值信息
     */
    public abstract void afterInvoke(JoinPoint joinPoint, Object result);
    
    
    /**
     * 在抛出异常后，由统一方法进行日志记录.
     *
     * @param joinPoint 切入点
     * @param throwable 抛出的异常实例
     */
    public abstract void afterThrowing(JoinPoint joinPoint, RuntimeException throwable);
    
    /**
     * 解析服务方法调用的参数列表数据，拼装为JSON字符串
     *
     * @param joinPoint 切入点信息
     * @return 参数列表的JSON字符串
     */
    String parseArgsData(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (ArrayUtils.isEmpty(args))
            return "";
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = declareMethod.getParameters();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            try {
                sb.append(parameters[i].getName());
                sb.append(" : ");
                sb.append(JSON.toJSONString(args[i]));
                sb.append("\n");
            } catch (Exception ignored) {
                // 不做操作
            }
        }
        return sb.toString();
    }

    /**
     * 解析服务方法调用的返回值数据，拼装为JSON字符串
     *
     * @param result 返回值信息
     * @return 返回值的JSON字符串
     */
    String parseReturnData(Object result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Return Value : ");
        try {
            sb.append(JSON.toJSONString(result));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 从{@link JoinPoint}中解析出实例对应的方法
     *
     * @param joinPoint 切入点信息
     * @return 实例方法对象
     * @throws RuntimeException 解析异常
     */
    Method parseInstanceMethod(JoinPoint joinPoint) {
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        try {
            return joinPoint.getTarget().getClass()
                    .getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
}
