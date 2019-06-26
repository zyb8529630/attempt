package com.attempt.core.log.converter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.attempt.core.common.TransactionCodeHolder;
import com.attempt.core.log.util.LogUtils;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 用于logback输出当前方法交易码. 标识符号为%tcode
 * @author zhouyinbin
 * @date 2019年6月26日 上午11:01:38
 *
 */
public class TcodeConverter extends ClassicConverter {

    /**
     * 从{@link ILoggingEvent}中提取交易码标识，并输出
     * 从MDC中获取信息
     *
     * @param event 日志对象
     * @return 输出信息

     */
    public String convert(ILoggingEvent event) {
        if (StringUtils.isEmpty(TransactionCodeHolder.get())) {
            Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
            if (mdcPropertyMap == null
                    || !mdcPropertyMap.containsKey(LogUtils.TRANSCATION_CODE_FLAG)) {
                return LogUtils.NO_OUTPUT;
            } else
                return StringUtils.defaultIfEmpty(mdcPropertyMap.get(LogUtils.TRANSCATION_CODE_FLAG), LogUtils.NO_OUTPUT);
        } else
            return TransactionCodeHolder.get();
    }
}
