package com.attempt.core.cache.suppore.redis.holder;

import org.springframework.util.Assert;

import com.attempt.core.cache.api.CacheMetaInfoFactory;
import com.attempt.core.cache.api.Countable;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.models.ExpireStrategy;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;
import com.attempt.core.common.SpringContextHolder;

/**  
* @Description: 对于缓存数据单元Holder的抽象.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public abstract class DataHolder implements Countable{
	 /**
     * 缓存数据的计数器.
     */
    protected CounterHolder counterHolder;

    /**
     * 元数据信息的Holder
     */
    protected MetaInfoHolder metaInfoHolder;

    /**
     * 缓存数据的Key.
     */
    protected String key;

    /**
     * 元数据的本地缓存
     */
    private CacheMetaInfo metaInfoLocalCache;

    /**
     * 构造方法.
     *
     * @param metaInfo 缓存数据单元的元数据信息
     */
    public DataHolder(CacheMetaInfo metaInfo) {
        Assert.notNull(metaInfo);
        key = CacheUtil.generateKey(metaInfo);
        // 初始化计数器和元数据Holder
        initCounterAndMetaInfo(metaInfo);
    }

    /**
     * 缓存数据对象的续期次数.
     *
     * @return the long
     */
    @Override
    public long renewalCount() {
        if (counterHolder == null)
            return -1;
        return counterHolder.getValue(CountType.RENEWAL_COUNT);
    }

    /**
     * 缓存数据对象的读次数.
     *
     * @return the long
     */
    @Override
    public long readCount() {
        if (counterHolder == null)
            return -1;
        return counterHolder.getValue(CountType.READ_COUNT);
    }

    /**
     * 缓存数据对象的写次数.
     *
     * @return the long
     */
    @Override
    public long writeCount() {
        if (counterHolder == null)
            return -1;
        return counterHolder.getValue(CountType.WRITE_COUNT);
    }

    /**
     * 缓存数据对象的剩余存活时间(毫秒).
     * <p>
     * {@link ExpireConfig#expireStrategy} != {@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT}时, 会返回-1
     *
     * @return the long
     */
    @Override
    public long remainTime() {
        if (counterHolder == null)
            return Integer.MAX_VALUE;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            return commander.nativeCommander().pttl(key);
        } catch (Exception e) {
			e.printStackTrace();
		}
        return 0;
    }

    /**
     * 获取缓存单元的元数据
     * <p>
     * 如果本地缓存幼稚，则读本地缓存值，不从redis中刷新读取
     * <p>
     * 如果本地缓存不存在，则需要通过{@link MetaInfoHolder}读取
     *
     * @return {@link CacheMetaInfo}
     */
    public CacheMetaInfo getMetaInfo() {
        return metaInfoLocalCache == null ? metaInfoHolder.get() : metaInfoLocalCache;
    }

    /**
     * 初始化计数器和元数据Holder
     *
     * @param dataMetaInfo 缓存单元的元数据
     */
    private void initCounterAndMetaInfo(CacheMetaInfo dataMetaInfo) {
        CacheMetaInfoFactory cacheMetaInfoFactory = SpringContextHolder.getBean(CacheMetaInfoFactory.class);
        /**
         * 构造缓存单元的计数器
         */
        if (CacheUtil.needCounter(dataMetaInfo)) {
            counterHolder = new CounterHolder(cacheMetaInfoFactory.generateCounterMetaInfo(dataMetaInfo));
        }
        /**
         * 构造缓存单元的元数据Holder
         */
        if (CacheUtil.needMetaInfo(dataMetaInfo)) {
            metaInfoHolder = new MetaInfoHolder(cacheMetaInfoFactory.generateMetaMetaInfo(dataMetaInfo));
        } else {
            metaInfoLocalCache = dataMetaInfo;
        }
    }

    /**
     * 续期.
     */
    public void renewal() {
        // 如果过期策略不是按照时间策略的，则不需要续期
        if (getMetaInfo().getExpireConfig().getExpireStrategy() != ExpireStrategy.EXPIRE_ON_TIME_LIMIT)
            return;
        // 如果续期次数超过限定值，则不再续期
        long renewal = getMetaInfo().getExpireConfig().getRenewnalCountLimit();
        if (renewal != -1 && renewal < renewalCount())
            return;
        long milliseconds = getMetaInfo().getExpireConfig().getTimeLimit();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().pexpire(key, milliseconds);
        } catch (Exception e) {
			e.printStackTrace();
		}

        if (counterHolder != null) {
            counterHolder.renewal(milliseconds);
            counterHolder.increase(CountType.RENEWAL_COUNT);
        }
        if (metaInfoHolder != null) {
            metaInfoHolder.renewal(milliseconds);
        }
    }
}
