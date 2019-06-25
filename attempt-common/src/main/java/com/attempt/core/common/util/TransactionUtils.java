package com.attempt.core.common.util;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import com.attempt.core.common.TransactionCodeHolder;

/**
 * 交易信息工具类.
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:45:48
 *
 */
public interface TransactionUtils {
	
	/**
     * 在日志格式中，代表交易码的标记
     */
    String TRANSCATION_CODE_FLAG = "tcode";
    /**
     * 在日志格式中，代表日志动作的标记
     */
    String ACT_FLAG = "act";
    
    /**
     * 生成全局唯一交易码: 32位随机数
     *
     * @return 交易码 string
     */
    static String generateTransactionCode() {
        return CommonUtils.uuid();
    }

    /**
     * 切换当前线程的交易码切换
     * 1. 将线程变量做切换
     * 2. 将切换体现在日志中
     *
     * @param newTransactionCode 新的交易码
     */
    static void switchTransactionCode(String newTransactionCode) {
        Assert.notNull(TransactionCodeHolder.get());
        Assert.notNull(newTransactionCode);
        MDC.put(ACT_FLAG, TRAN_ACT.TRAN_TCODE.toString());
        LoggerFactory.getLogger(TRAN_SysLogType.ATTEMPT_SEV.toString()).info(newTransactionCode);
        MDC.put(ACT_FLAG, TRAN_ACT.LOG.toString());
        TransactionCodeHolder.set(newTransactionCode);
    }

    enum TRAN_ACT {
        /**
         * 标识当前日志为交易流水号转换
         */
        TRAN_TCODE,
        /**
         * 标识当前日志为常规内容输出
         */
        LOG
    }

    enum TRAN_SysLogType {
        /**
         * Spring业务服务层日志
         */
    	ATTEMPT_SEV
    }
}
