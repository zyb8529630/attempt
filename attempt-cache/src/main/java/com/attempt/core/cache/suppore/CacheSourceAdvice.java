package com.attempt.core.cache.suppore;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.springframework.core.annotation.AnnotationUtils;
import org.aspectj.lang.reflect.MethodSignature;
import com.attempt.core.cache.annotation.CacheSource;
import com.attempt.core.cache.util.CacheSourceHolder;

/**
 * 对动态数据源进行配置的切面AOP拦截器.
 * @author zhouyinbin
 * @date 2019年6月5日 下午1:54:05
 *
 */
public class CacheSourceAdvice {

	/**
	 * 是否启动动态数据源功能
	 */
	private boolean dynamic;

	/**
	 * 在每个com.*.*.service.impl和com.*.*.web包中方法被调用后前，会检查每个方法上是否有@CacheSource注解，以此来映射到具体的数据源上（该拦截器优先于doBeforeForJdbc）
	 * 该拦截器的有效与否取决于是否启动了动态数据源管理
	 * @param joinPoint
	 * @throws Exception
	 */
	public void doBefore(JoinPoint joinPoint) throws Exception {
		
		/**
		 * 如果未打开动态数据源开关，这不操作
		 */
		if(!dynamic)
			return;
		
		/**
		 * 如果当前线程已经设置了动态数据源，则继承父类的设定
		 */
		if(CacheSourceHolder.get() != null)
			return;
		
		/**
		 * 尝试在接口类上查找注解，并设置
		 */
		Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method instanceMethod = joinPoint.getTarget().getClass()
                .getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
        CacheSource annotation = AnnotationUtils.findAnnotation(instanceMethod, CacheSource.class);
        if (annotation == null)
            return;
        CacheSourceHolder.set(annotation.value());
	}
	
	
	
	/**
	 * 在每个com.*.*.service.impl和com.*.*.web包中方法被调用后，将当前线程的数据源声明置回原始Null值.
	 * @param joinPoint
	 */
	public void doAfter(JoinPoint joinPoint) {
		if(!dynamic)
			return;
		CacheSourceHolder.clear();
	}
	
	/**
	 * 在每个com.*.*.service.impl和com.*.*.web包中方法throwException后，将当前线程的数据源声明置回原始Null值.
	 * @param joinPoint
	 * @param throwable
	 */
	public void afterThrowing(JoinPoint joinPoint, RuntimeException throwable) {
		if(!dynamic)
			return;
		CacheSourceHolder.clear();
	}
	
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	
	
	
}
