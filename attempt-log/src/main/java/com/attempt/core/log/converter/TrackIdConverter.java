package com.attempt.core.log.converter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.attempt.core.log.util.LogUtils;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 *
 * @author zhouyinbin
 * @date 2019年6月26日 上午11:03:10
 *
 */
public class TrackIdConverter extends ClassicConverter {

    /**
     * 从{@link ILoggingEvent}中提取trackid，并输出
     * 从MDC中获取信息
     *
     * @param event 日志对象
     * @return 输出信息
     */
    public String convert(ILoggingEvent event) {
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        if (mdcPropertyMap == null
                || (!mdcPropertyMap.containsKey(LogUtils.TRACKID_FLAG)
                && !mdcPropertyMap.containsKey(LogUtils.PTXID_FLAG))) {
            return LogUtils.NO_OUTPUT;
        } else {
            if (StringUtils.isNotEmpty(mdcPropertyMap.get(LogUtils.TRACKID_FLAG))) {
                return mdcPropertyMap.get(LogUtils.TRACKID_FLAG);
            } else if (StringUtils.isNotEmpty(mdcPropertyMap.get(LogUtils.PTXID_FLAG))) {
                return mdcPropertyMap.get(LogUtils.PTXID_FLAG);
            } else {
                return LogUtils.NO_OUTPUT;
            }
        }
    }
}
