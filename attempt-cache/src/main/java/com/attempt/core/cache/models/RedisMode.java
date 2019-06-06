package com.attempt.core.cache.models;
/**
 * Redis运行模式.
 * @author zhouyinbin
 * @date 2019年6月6日 下午1:54:40
 *
 */
public enum RedisMode {
	
	/**
	 * 单例模式
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
