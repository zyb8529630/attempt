package com.attempt.core.cache.util;

import java.util.Arrays;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;

import com.attempt.core.cache.models.CacheDataType;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.provider.RedisCommonderProvider;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.common.SpringContextHolder;

import redis.clients.jedis.ScanParams;
/**
 * 缓存操作工具类.
 * @author zhouyinbin
 * @date 2019年6月5日 下午5:40:50
 *
 */
public interface CacheUtil {

    /**
     * 区域分隔符.
     */
    String SPLITFLAG_AREA = ":";

    /**
     * 属性分隔符.
     */
    String SPLITFLAG_PROPERTY = ".";

    /**
     * 哈希槽信号量在Redis中的key
     */
    String CLUSTER_SLOT_MUTEX_FLAG = "_REDIS_SLOT";
    
	/**
	 * 生成合法的Redis的Key.
	 * @param metaInfo
	 * @return
	 */
    static Object generateKey(CacheMetaInfo metaInfo) {
		 Assert.notNull(metaInfo);
	     return generateKey(metaInfo.getDataType(), metaInfo.getOwner(), metaInfo.getName());
	}
    
    /**
     * 生成合法的Redis的Key.
     * @param dataType
     * @param owner
     * @param name
     * @return
     */
    static String generateKey(CacheDataType dataType, String owner, String name) {

        Assert.notNull(dataType);
        Assert.notNull(owner);
        Assert.notNull(name);

        StringBuilder sb = new StringBuilder();
        sb.append(owner);
        sb.append(SPLITFLAG_AREA);
        sb.append(dataType.name());
        sb.append(SPLITFLAG_AREA);
        sb.append(name);
        return sb.toString();
    }

	/**
	 * 根据当前线程指定的缓存数据源
	 * 创建Redis客户端.
	 * @return
	 */
    static RedisCommander createRedisCommonder() {
        return SpringContextHolder.getBean(ObjectUtils.defaultIfNull(CacheSourceHolder.get(), RedisCommonderProvider.DEFAULT_CACHE_SOURCE_NAME));
    }
}
