package com.attempt.core.cache.models;
/**
 * 缓存数据类型.
 * @author zhouyinbin
 * @date 2019年6月5日 下午5:37:47
 *
 */
public enum CacheDataType {
	
	 /**
     * 信号量.
     */
    MUTEX,
    /**
     * 会话.
     */
    SESSION,
    /**
     * 结果集索引.
     */
    INDEX,
    /**
     * 结果集投影纵向排列方式.
     */
    PROJECTION_VER,
    /**
     * 结果集投影横向排列方式.
     */
    PROJECTION_HOR,
    /**
     * 列表.
     */
    LIST,
    /**
     * Map.
     */
    MAP,
    /**
     * 排名.
     */
    RANK,
    /**
     * 表格.
     */
    TABLE,
    /**
     * 缓存数据单元的元数据信息.
     */
    META,
    /**
     * 缓存数据单元的计数器
     */
    COUNTER,
    /**
     * 快照.
     */
    SNAPSHOT;
}
