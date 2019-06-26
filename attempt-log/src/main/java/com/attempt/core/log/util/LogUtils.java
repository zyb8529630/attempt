package com.attempt.core.log.util;

import java.lang.reflect.Method;

/**
 * 日志相关配置
 * @author zhouyinbin
 * @date 2019年6月26日 上午9:37:49
 *
 */
public interface LogUtils {

	 /**
     * 日志运行模式
     */
    String MODE_PROPERTY_FLAG = "log.detail.mode";
    
    /**
     * 黑名单的日志记录。以分号分隔
     */
    String BLACKLIST_PROPERTY_FLAG = "log.detail.blacklist";
        
    /**
     * 白名单的日志记录。以分号分隔
     */
    String WHITELIST_PROPERTY_FLAG = "log.detail.whitelist";
    
    /**
     * 核心框架的类路径
     */
    String HALO_ATTEMPT_CLASS_PATH = "com.attempt.core";
    
    
    /**
     * PinPoint监控在MDC中注入的trackId标识
     */
    String PTXID_FLAG = "PtxId";
    
    /**
     * 在日志格式中，代表交易码的标记
     */
    String TRANSCATION_CODE_FLAG = "tcode";
    /**
     * 将PinPoint监控在MDC中注入的trackId换一个名字来存储
     */
    String TRACKID_FLAG = "trackid";
    
    /**
     * 在日志格式中，代表方法调用路径的标记
     */
    String PATH_FLAG = "path";
    /**
     * 在日志格式中，代表日志动作的标记
     */
    String ACT_FLAG = "act";
    /**
     * 输出空日志内容的占位符
     */
    String EMPTY_CONTENT = "";
    /**
     * 用于标识无输出内容
     */
    String NO_OUTPUT = "NONE";
    
    /**
     * 生成方法调用路径
     *
     * @param method 方法实例
     * @return 方法调用路径
     */
	static String generatePath(Method method) {
		 return String.format("%s#%s", method.getDeclaringClass().getName(), method.getName());
	}

    /**
     * 生成方法调用路径
     *
     * @param stackTrace 调用堆栈信息
     * @return 方法调用路径
     */
    static String generatePath(StackTraceElement stackTrace) {
        return String.format("%s#%s", stackTrace.getClassName(), stackTrace.getMethodName());
    }

}
