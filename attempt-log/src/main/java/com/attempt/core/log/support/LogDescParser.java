package com.attempt.core.log.support;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;

import com.attempt.core.common.TransactionCodeHolder;
import com.attempt.core.common.util.PropertiesUtils;
import com.attempt.core.log.annotion.LogConfig;
import com.attempt.core.log.models.LogDesc;
import com.attempt.core.log.models.LogMode;
import com.attempt.core.log.util.LogUtils;

/**
 * 构建{@link LogDesc}实例的服务.
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:23:51
 *
 */
public class LogDescParser implements InitializingBean {
	
	/**
     * 框架日志模式
     */
    protected LogMode mode;
    
    /**
     * 日志明细记录黑名单
     */
    private List<String> blackList;
    /**
     * 日志明细记录白名单
     */
    private List<String> whiteList;
    
    /**
     * 从{@link Method}中解析并生成{@link LogDesc}实例
     *
     * @param instanceMethod 方法实例
     * @return 方法日志记录实例
     */
    public LogDesc generateLogDesc(Method instanceMethod) {
        LogDesc methodLogConfig = new LogDesc();
        methodLogConfig.setTransactionCode(TransactionCodeHolder.get());
        if (instanceMethod != null) {
            methodLogConfig.setLogConfig(AnnotationUtils.findAnnotation(instanceMethod, LogConfig.class));
            methodLogConfig.setEnabled(isLogEnabled(instanceMethod));
            methodLogConfig.setLogPath(LogUtils.generatePath(instanceMethod));
            methodLogConfig.setMethod(instanceMethod);
        }
        return methodLogConfig;
    }
    /**
     * 从classPath中解析并生成{@link LogDesc}实例
     *
     * @param classPath 方法类路径
     * @return 方法日志记录实例
     */
    public LogDesc generateLogDesc(String classPath) {
        LogDesc methodLogConfig = new LogDesc();
        methodLogConfig.setTransactionCode(TransactionCodeHolder.get());
        methodLogConfig.setEnabled(isLogEnabled(classPath));
        methodLogConfig.setLogPath(classPath);
        return methodLogConfig;
    }
    /**
     * 根据传入的服务方法实例，判断是否写入日志明细
     * 1. 如果为黑名单模式：默认都不写日志，只写入黑名单内的类路径的日志
     * 2. 如果为白名单模式：默认全写日志，在白名单内的类路径不写日志
     * 3. 无论是黑名单模式还是白名单模式，只要在方法上声明了{@link LogConfig}，按照声明的来
     *
     * @param instanceMethod 服务方法实例
     * @return true/false

     */
    public boolean isLogEnabled(Method instanceMethod) {
        /**
         * 先尝试解析方法上的注解来判断
         */
        LogConfig logConfigAnn = AnnotationUtils.findAnnotation(instanceMethod, LogConfig.class);
        if (logConfigAnn != null)
            return logConfigAnn.enabled();

        /**
         * 再使用黑名单/白名单模式配置来判断
         */
        return isLogEnabled(instanceMethod.getDeclaringClass().getName());
    }
    
    /**
     * 根据传入的服务方法类路径，判断是否写入日志明细
     * 1. 如果为黑名单模式：默认都不写日志，只写入黑名单内的类路径的日志
     * 2. 如果为白名单模式：默认全写日志，在白名单内的类路径不写日志
     *
     * @param classPath 服务方法类路径
     * @return true/false
     */
    public boolean isLogEnabled(String classPath) {
        if (mode == LogMode.BLACK_LIST) {
            if (blackList.stream().anyMatch(item -> classPath.indexOf(item) == 0))
                return true;
            else
                return false;
        } else {
            if (whiteList.stream().noneMatch(item -> classPath.indexOf(item) == 0))
                return true;
            else
                return false;
        }
    }
    
    
	@Override
	public void afterPropertiesSet() throws Exception {
		/**
         * 根据{@code MODE_PROPERTY_FLAG}设置日志模式。默认为白名单模式
         */
        try {
            mode = LogMode.valueOf(PropertiesUtils.getProperty(LogUtils.MODE_PROPERTY_FLAG));
        } catch (Exception e) {
            mode = LogMode.WHITE_LIST;
        }
		
        /**
         * 根据日志模式开关，分别设置白名单和黑名单
         */
        if (mode == LogMode.BLACK_LIST) {
            blackList = Arrays.stream(StringUtils.defaultString(PropertiesUtils.getProperty(LogUtils.BLACKLIST_PROPERTY_FLAG), "")
                    .split(";"))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
        } else if (mode == LogMode.WHITE_LIST) {
            whiteList = Arrays.stream(StringUtils.defaultString(PropertiesUtils.getProperty(LogUtils.WHITELIST_PROPERTY_FLAG), "")
                    .split(";"))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
            whiteList.add(LogUtils.HALO_ATTEMPT_CLASS_PATH);
        }
        
	}
	
	
	
	
	
	
	
	
	
	
	

}
