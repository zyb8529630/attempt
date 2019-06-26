package com.attempt.core.log.converter;

import java.util.Map;

import com.attempt.core.log.util.LogUtils;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 用于logback输出当前方法调用路径. 标识符号为%path
 * @author zhouyinbin
 * @date 2019年6月26日 上午11:00:12
 *
 */
public class PathConverter extends ClassicConverter {

    /**
     * 从{@link ILoggingEvent}中提取方法路径信息，并输出
     * 1. 从MDC中获取信息
     * 2. 从调用堆栈中获取信息
     *
     * @param event 日志对象
     * @return 输出信息
     */
	@Override
    public String convert(ILoggingEvent event) {

        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

        if (mdcPropertyMap == null || !mdcPropertyMap.containsKey(LogUtils.PATH_FLAG)) {
            StackTraceElement[] cda = event.getCallerData();
            if (cda != null && cda.length > 0)
                return LogUtils.generatePath(cda[0]);
            else
                return LogUtils.NO_OUTPUT;
        }
        return mdcPropertyMap.get(LogUtils.PATH_FLAG);
    }


}
