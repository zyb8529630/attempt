package com.attempt.core.cache.api;

import com.attempt.core.cache.models.CountType;

/**  
* @Description: 计数器操作接口.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface CounterOperations { 
	 /**
     * 获取值.
     *
     * @param type {@link CountType}
     * @return the string
     */
    long getValue(CountType type);

    /**
     * Increase by 1.
     *
     * @param type {@link CountType}
     */
    void increase(CountType type);

    /**
     * Decrease by 1.
     *
     * @param type {@link CountType}
     */
    void decrease(CountType type);

    /**
     * 删除.
     *
     */
    void delete();

    /**
     * 续期.
     *
     * @param milliseconds 续期的毫秒数
     */
    void renewal(long milliseconds);
}
