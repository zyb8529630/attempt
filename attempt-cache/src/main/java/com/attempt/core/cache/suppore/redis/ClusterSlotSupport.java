package com.attempt.core.cache.suppore.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.util.Assert;

import com.attempt.core.cache.models.CacheDataType;
import com.attempt.core.cache.models.ClusterSlot;
import com.attempt.core.cache.util.CacheSourceHolder;
import com.attempt.core.cache.util.CacheUtil;
import com.attempt.core.common.util.PropertiesUtils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * Redis集群模式下，对于哈希槽信息的管理.
 * @author zhouyinbin
 * @date 2019年6月6日 下午4:05:40
 *
 */
public class ClusterSlotSupport {

	/**
	 * 当 Redis 模式为{@link RedisMode#CLUSTER}时, 对各个节点的哈希槽缓存
	 */
	private static Map<String, ClusterSlot> slotHostMap = new HashMap<>();
	
	/**
	 *  添加redis集群的哈希槽信息
	 * @param cacheSourceName redis数据源名称
	 * @param commander  redis连接(使用后在本方法内关闭)
	 * @throws Exception 初始化哈希槽信息异常时
	 */
	public static void initClusterSlot(String cacheSourceName, RedisCommander commander) throws Exception {
        try {
            ClusterSlot slot = new ClusterSlot();
            slot.setSlotCacheKey(CacheUtil.generateKey(CacheDataType.MUTEX, PropertiesUtils.getProperty("cache.name"), CacheUtil.CLUSTER_SLOT_MUTEX_FLAG));
            if (commander.nativeCommander().exists(slot.getSlotCacheKey()))
                slot.setLocalRefreshValue(Long.parseLong(commander.nativeCommander().get(slot.getSlotCacheKey())));
            else
                commander.nativeCommander().set(slot.getSlotCacheKey(), "0");
            refreshSlotHostMap(slot, commander);
            slotHostMap.put(cacheSourceName, slot);
        } catch (Exception e) {
            throw e;
        } finally {
            commander.close();
        }
    }
	
	/**
	 * 获取集群的哈希槽信息
	 * 如果Redis的信号量提示有更新，则更新后返回
	 * @return 集群的哈希槽信息
	 * @throws Exception
	 */
    public TreeMap<Long, String> getSlotHostMap() throws Exception {
        if (slotHostMap.size() == 0)
            throw new UnsupportedOperationException("{cache.redis.mode} is not CLUSTER");
        ClusterSlot slot = slotHostMap.get(CacheSourceHolder.get());
        Assert.notNull(slot);
        RedisCommander commander = CacheUtil.createRedisCommonder();
        try {
            long redisValue = Long.parseLong(commander.nativeCommander().get(slot.getSlotCacheKey()));
            if (redisValue > slot.getLocalRefreshValue()) {
                refreshSlotHostMap(slot, commander);
                slot.setLocalRefreshValue(redisValue);
            }
            return slot.getSlotHostMap();
        } catch (Exception e) {
            throw e;
        } finally {
            commander.close();
        }
    }
    
    /**
     * 刷新哈希槽信息
     * @param clusterSlot
     * @param commander
     */
    @SuppressWarnings("unchecked")
    private synchronized static void refreshSlotHostMap(ClusterSlot clusterSlot, RedisCommander commander) {
        String parts[] = ((JedisCluster) commander.nativeCommander()).getClusterNodes().keySet().iterator().next().split(":");
        HostAndPort anyHostAndPort = new HostAndPort(parts[0], Integer.parseInt(parts[1]));
        try (Jedis jedis = new Jedis(anyHostAndPort.getHost(), anyHostAndPort.getPort());) {
            List<Object> list = jedis.clusterSlots();
            for (Object object : list) {
                List<Object> list1 = (List<Object>) object;
                List<Object> master = (List<Object>) list1.get(2);
                String hostAndPort = new String((byte[]) master.get(0)) + ":" + master.get(1);
                clusterSlot.getSlotHostMap().put((Long) list1.get(0), hostAndPort);
                clusterSlot.getSlotHostMap().put((Long) list1.get(1), hostAndPort);
            }
        } catch (Exception e) {
            throw e;
        }
    }
    
	
}
