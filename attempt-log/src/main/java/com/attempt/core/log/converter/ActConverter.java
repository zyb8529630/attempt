package com.attempt.core.log.converter;

import java.util.Map;

import com.attempt.core.log.models.ACT;
import com.attempt.core.log.util.LogUtils;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 用于logback输出当前日志动作标识. 标识符号为%act
 * @author zhouyinbin
 * @date 2019年6月26日 上午10:59:15
 *
 */
public class ActConverter extends ClassicConverter {

    /**
     * 从{@link ILoggingEvent}中提取日志动作标识，并输出
     * 从MDC中获取信息
     *
     * @param event 日志对象
     * @return 输出信息
     */
    public String convert(ILoggingEvent event) {

        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

        if (mdcPropertyMap == null || !mdcPropertyMap.containsKey(LogUtils.ACT_FLAG)) {
            return ACT.LOG.toString();
        }
        return mdcPropertyMap.get(LogUtils.ACT_FLAG);
    }
}
