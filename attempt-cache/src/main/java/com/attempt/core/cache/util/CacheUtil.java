package com.attempt.core.cache.util;

import java.util.Arrays;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;

import com.attempt.core.cache.models.CacheDataType;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.ExpireStrategy;
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
     * 系统区域标识.
     */
    String SYS_FLAG = "SYS";

    /**
     * 表数据类型的索引区域标识.
     */
    String TABLE_INDEX = "IDX";

    /**
     * 表数据类型的数据区域标识.
     */
    String TABLE_RECORD = "REC";

    /**
     * 哈希槽信号量在Redis中的key
     */
    String CLUSTER_SLOT_MUTEX_FLAG = "_REDIS_SLOT";

    /**
     * 使用SCAN一次读取的
     */
    int MAX_COUNT_PRE_SCAN = 500;

    /**
     * 生成合法的Redis的Key.
     *
     * @param dataType 缓存数据类型
     * @param owner    数据单元拥有者 @see {@code cache.properties#cache.name}
     * @param name     数据资源名称
     * @return Redis的Key.
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
     * 生成合法的Redis的Key.
     *
     * @param metaInfo 元数据
     * @return Redis的Key
     */
    static String generateKey(CacheMetaInfo metaInfo) {

        Assert.notNull(metaInfo);
        return generateKey(metaInfo.getDataType(), metaInfo.getOwner(), metaInfo.getName());
    }

    /**
     * 生成合法的Redis的Key.
     *
     * @param metaInfo   元数据
     * @param attachKeys 外挂的属性key集合
     * @return Redis的Key
     */
    static String generateKey(CacheMetaInfo metaInfo, String... attachKeys) {

        Assert.notNull(metaInfo);
        Assert.notEmpty(attachKeys);

        StringBuilder sb = new StringBuilder();
        sb.append(metaInfo.getOwner());
        sb.append(SPLITFLAG_AREA);
        sb.append(metaInfo.getDataType().name());
        sb.append(SPLITFLAG_AREA);
        sb.append(metaInfo.getName());
        Arrays.stream(attachKeys).forEach(str -> {
            sb.append(SPLITFLAG_PROPERTY);
            sb.append(str);
        });
        return sb.toString();
    }

    /**
     * 判断是否需要创建计数器.
     * <p>
     * {@link CacheMetaInfo#getDataType()}=={@link CacheDataType#META} 不需要
     * <p>
     * {@link CacheMetaInfo#getDataType()}=={@link CacheDataType#COUNTER} 不需要
     * <p>
     * {@link ExpireStrategy#NO_EXPIRE} 不需要
     * <p>
     * {@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 不需要
     * <p>
     * {@link ExpireStrategy#EXPIRE_ON_OWNER_EXIT} 不需要
     *
     * @param cacheMetaInfo 元数据
     * @return 是否需要创建
     */
    static boolean needCounter(CacheMetaInfo cacheMetaInfo) {
        if (cacheMetaInfo.getDataType() == CacheDataType.COUNTER || cacheMetaInfo.getDataType() == CacheDataType.META)
            return false;
        ExpireStrategy expireStrategy = cacheMetaInfo.getExpireConfig().getExpireStrategy();
        if (expireStrategy == ExpireStrategy.NO_EXPIRE || expireStrategy == ExpireStrategy.EXPIRE_ON_TIME_LIMIT
                || expireStrategy == ExpireStrategy.EXPIRE_ON_OWNER_EXIT)
            return false;
        return true;
    }

    /**
     * 判断是否需要创建元数据.
     * <p>
     * {@link CacheMetaInfo#getDataType()}=={@link CacheDataType#META} 不需要
     * <p>
     * {@link CacheMetaInfo#getDataType()}=={@link CacheDataType#SESSION} 不需要
     *
     */
    static boolean needMetaInfo(CacheMetaInfo cacheMetaInfo) {
        if (cacheMetaInfo.getDataType() == CacheDataType.META || cacheMetaInfo.getDataType() == CacheDataType.SESSION)
            return false;
        return true;
    }

    /**
     * 根据当前线程指定的缓存数据源
     * 创建Redis客户端.
     */
    static RedisCommander createRedisCommonder() {
        return SpringContextHolder.getBean(ObjectUtils.defaultIfNull(CacheSourceHolder.get(), RedisCommonderProvider.DEFAULT_CACHE_SOURCE_NAME));
    }

    /**
     * 创建用于执行SSCAN，HSCAN的参数
     */
    static ScanParams createScanParams() {
        return new ScanParams().count(MAX_COUNT_PRE_SCAN);
    }
}
