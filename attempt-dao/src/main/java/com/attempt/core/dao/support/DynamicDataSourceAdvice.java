package com.attempt.core.dao.support;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    
    
    
}
