package com.attempt.core.cache.models;
/**  
* @Description: 缓存数据的超时过期策略.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public enum ExpireStrategy {
	/**
     * 创建者应用退出时，数据过期.
     */
    EXPIRE_ON_OWNER_EXIT,
    /**
     * 数据永不过期（不推荐使用）.
     */
    NO_EXPIRE,
    /**
     * 特定时间后，数据过期.
     */
    EXPIRE_ON_TIME_LIMIT,
    /**
     * 读取指定次数后，数据过期.
     */
    EXPIRE_ON_GET_LIMIT,
    /**
     * 写入指定次数后，数据过期.
     */
    EXPIRE_ON_SET_LIMIT
}
