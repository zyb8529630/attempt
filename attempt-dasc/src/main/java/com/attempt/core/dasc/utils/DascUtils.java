package com.attempt.core.dasc.utils;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.attempt.core.common.util.PropertiesUtils;

/**
 * 用于DASC协议的工具集合.
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:53:05
 *
 */
public interface DascUtils {

    /**
     * 调用方唯一标识格式：testid.CALLER:调用服务方接口类的类路径#方法名
     */
    String CALL_ID_FORMAT = "%sCALLER:%s#%s";
	
	/**
     * 根据方法签名对象，生成调用方唯一标识
     *
     * @param method 调用方接口方法实例
     * @return callerId
     */
	static String generateCallerId(Method method) {
		return String.format(CALL_ID_FORMAT, getTestId(), method.getDeclaringClass().getName(), method.getName());
	}

    /**
     * 用于在开发和测试场景下，隔离不同主机的标示符
     *
     * @return 隔离主机的标识符
     */
    static String getTestId() {
        String testId = PropertiesUtils.getProperty("dasc.test.id");
        if (StringUtils.isEmpty(testId))
            return "";
        return testId + ".";
    }
}
