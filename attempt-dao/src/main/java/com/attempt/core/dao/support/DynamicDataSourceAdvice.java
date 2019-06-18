package com.attempt.core.dao.support;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;


import com.attempt.core.dao.annotation.DataSource;
import com.attempt.core.dao.util.DynamicDataSourceHolder;

/**
 * 用于对动态数据源进行配置的切面AOP拦截器
 * @author zhouyinbin
 * @date 2019年6月10日 下午1:23:09
 *
 */
public class DynamicDataSourceAdvice {

    /**
     * jdbctemplate与数据源的匹配.
     */
    private Map<String, String> dataSourceMappingSet;

    /**
     * 是否启动动态数据源功能.
     */
    private boolean dynamic;
    
    
    public void beforeJdbcInvoke(JoinPoint joinPoint) {

        //如果未打开动态开关，则对数据源不再进行定制，Null值默认会走默认数据源
        if (!dynamic)
            return;
        if (dataSourceMappingSet == null)
            return;
        // 当识别到当前线程有活动事务时,则jdbcTemplate层数据路由失效
        if (TransactionSynchronizationManager.isActualTransactionActive())
            return;
        //如果数据源类型不为Null，则认为客户端代码已经进行过级别更高的数据源配置（通过注解），那么就不在jdbcTemplate层修改数据源配置
        if (DynamicDataSourceHolder.get() != null)
            return;
        String methodName = joinPoint.getSignature().getName();
        Optional<String> findKey = dataSourceMappingSet.keySet().stream().filter(key -> methodName.toUpperCase().contains(key.toUpperCase())).findFirst();
        DynamicDataSourceHolder.set(findKey.isPresent() ? dataSourceMappingSet.get(StringUtils.defaultIfEmpty(findKey.get(), null)) : null);
    }
    
    /**
     * 在方法调用后，将当前线程的数据源声明置回原始Null值.
     * <p>
     * 作用范围：
     * 2. JdbcTemplate方法调用
     *
     * @param joinPoint 切入点信息
     */
    public void afterJdbcInvoke(JoinPoint joinPoint) {

        if (!dynamic)
            return;
        if (dataSourceMappingSet == null)
            return;
        //当识别到当前线程有活动事务时, 不要重置数据源
        if (TransactionSynchronizationManager.isActualTransactionActive())
            return;
        DynamicDataSourceHolder.clear();
    }

    /**
     * 在每个com.*.*.service.impl包中方法调用前：
     * 1. 检查每个方法上是否有@DataSource注解，以此来映射到具体的数据源上（该拦截器优先于doBeforeForJdbc）
     * 2. 该拦截器的有效与否,取决与是否启用了动态数据源管理。
     *
     * @param joinPoint 切入点信息
     */
    public void beforeServiceInvoke(JoinPoint joinPoint) throws Exception {

        //如果未打开动态开关，则对数据源不再进行定制，Null值默认会走默认数据源
        if (!dynamic)
            return;
        //当识别到当前线程有活动事务，则不要再更改数据源
        if (TransactionSynchronizationManager.isActualTransactionActive())
            return;
        // 获取方法签名
        Method declareMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method instanceMethod = joinPoint.getTarget().getClass()
                .getMethod(declareMethod.getName(), declareMethod.getParameterTypes());
        DataSource methodAnnotation = AnnotationUtils.findAnnotation(instanceMethod, DataSource.class);
        if (methodAnnotation == null)
            return;        
        /**      
         * 如果数据库路由是静态方式，则直接填充值
         */
        if (methodAnnotation.type() == ElementType.METHOD)
            DynamicDataSourceHolder.set(methodAnnotation.value());
        /**
         * 如果数据库路由是动态方式，则需要通过方法请求的参数列表动态获取
         */
        else if (methodAnnotation.type() == ElementType.PARAMETER) {
            Object routeKey;
            Object routerObj = joinPoint.getArgs()[methodAnnotation.parameterIndex()];
            /**
             * 如果{@link DataSource#propertyName()}不为空，则表示需要从参数列表中某个值做内省获取值
             */
            if (StringUtils.isNotEmpty(methodAnnotation.propertyName()))
                routeKey = PropertyUtils.getProperty(routerObj, methodAnnotation.propertyName());
            else
                routeKey = routerObj;
            if (routeKey == null)
                throw new NullPointerException("The RouterKey is null!");
            DynamicDataSourceHolder.set(routeKey.toString());
        }

    }

    /**
     * 在方法调用后，将当前线程的数据源声明置回原始Null值.
     * <p>
     * 作用范围：
     * 1. com.*.*.service.impl包中方法
     *
     * @param joinPoint 切入点信息
     */
    public void afterServiceInvoke(JoinPoint joinPoint) {
        if (!dynamic)
            return;
        //当识别到当前线程有活动事务时, 不要重置数据源
        if (TransactionSynchronizationManager.isActualTransactionActive())
            return;
        DynamicDataSourceHolder.clear();
    }

    /**
     * 在方法调用抛出异常时，将当前线程的数据源声明置回原始Null值.
     * <p>
     * 作用范围：
     * com.*.*.service.impl包中方法
     *
     * @param joinPoint 切入点信息
     */
    public void afterServiceThrowing(JoinPoint joinPoint, RuntimeException throwable) {
        if (!dynamic)
            return;
        DynamicDataSourceHolder.clear();
    }

    /**
     * 在方法调用抛出异常时，将当前线程的数据源声明置回原始Null值.
     * <p>
     * 作用范围：
     * JdbcTemplate方法调用
     *
     * @param joinPoint 切入点信息
     */
    public void afterJdbcThrowing(JoinPoint joinPoint, RuntimeException throwable) {
        if (!dynamic)
            return;
        if (dataSourceMappingSet == null)
            return;
        DynamicDataSourceHolder.clear();
    }

    /**
     * 是否启动动态数据源功能.
     *
     * @param dynamic the dynamic
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * 是否启动动态数据源功能.
     *
     * @param dataSourceMappingSet the data source mapping set
     */
    public void setDataSourceMappingSet(Map<String, String> dataSourceMappingSet) {
        if (dataSourceMappingSet.values().stream().filter(StringUtils::isNotEmpty).findFirst().isPresent())
            this.dataSourceMappingSet = dataSourceMappingSet;
    }

}
