package com.attempt.core.cache.models;

import java.util.TreeMap;

/**
 * Redis集群模式下，标识每个Redis容器的哈希槽信息,及管理它的一些配置信息
 * @author zhouyinbin
 * @date 2019年6月6日 下午4:06:16
 *
 */
public class ClusterSlot {

	/**
	 * 在Redis中用来缓存哈希槽的的Key
	 */
	private String slotCacheKey;
	
	/**
	 * 在本地存储的槽信息增量更新值
	 */
	private long localRefreshValue = 0;
	
	/**
	 * 当 Redis 模式为{@link RedisMode#CLUSTER}时, 对各个节点的哈希槽缓存
	 */
	private TreeMap<Long, String> slotHostMap = new TreeMap<>();

	public String getSlotCacheKey() {
		return slotCacheKey;
	}

	public void setSlotCacheKey(String slotCacheKey) {
		this.slotCacheKey = slotCacheKey;
	}

	public long getLocalRefreshValue() {
		return localRefreshValue;
	}

	public void setLocalRefreshValue(long localRefreshValue) {
		this.localRefreshValue = localRefreshValue;
	}

	public TreeMap<Long, String> getSlotHostMap() {
		return slotHostMap;
	}

	public void setSlotHostMap(TreeMap<Long, String> slotHostMap) {
		this.slotHostMap = slotHostMap;
	}
	
	
	
}
