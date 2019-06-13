package com.attempt.core.cache.models;
/**  
* @Description: 计数类型.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public enum CountType {
	 /**
     * 缓存续期计数.
     */
    RENEWAL_COUNT,
    /** 
     * 缓存写计数.
     */
    WRITE_COUNT,
    /**
     * 缓存读计数.
     */
    READ_COUNTCountType,
    
    READ_COUNT
}
