package com.attempt.core.cache.models;
/**  
* @Description: Redis运行模式.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public enum RedisMode {

    /**
     * 单例模式.
     */
    SINGLETON,

    /**
     * 哨兵模式.
     */
    SENTINEL,

    /** 
     * 集群模式.
     */
    CLUSTER,

    /**
     * 分片模式.
     */
    SHAREED;
	
}
