package com.attempt.core.cache.models;
/**  
* @Description: 缓存数据的续期策略.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public enum RenewalStrategy {
	 /**
     * 无续期策略.
     */
    NO_RENEWAL,
    /**
     * 每次读取数据,可进行续期.
     */
    RENEWAL_ON_GET,

    /**
     * 每次更新数据,可进行续期. 
     */
    RENEWAL_ON_SET,

    /**
     * 每次读取/写入数据,可进行续期.
     */
    RENEWAL_ON_ALL
}
