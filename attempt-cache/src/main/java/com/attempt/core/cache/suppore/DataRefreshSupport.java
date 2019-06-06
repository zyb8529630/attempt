package com.attempt.core.cache.suppore;

import org.springframework.util.Assert;

import com.attempt.core.cache.models.Behavior;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.ExpireStrategy;
import com.attempt.core.cache.models.RenewalStrategy;
import com.attempt.core.cache.suppore.redis.holder.DataHolder;

/**
 * 数据刷新与超期等操作的支持工具.
 */
public interface DataRefreshSupport {


    /**
     * 判断是否需要续期数据.
     *
     * @param dataHolder 数据持有对象
     * @param behavior   行为
     * @return the boolean
     */
    static boolean needRenewalOn(DataHolder dataHolder, Behavior behavior) {

        Assert.notNull(dataHolder);
        Assert.notNull(behavior);
        CacheMetaInfo metaInfo = dataHolder.getMetaInfo();
        RenewalStrategy renewalStrategy = metaInfo.getExpireConfig().getRenewalStrategy();
        ExpireStrategy expireStrategy = metaInfo.getExpireConfig().getExpireStrategy();

        /** 如果元数据表明不会过期,或不需要刷新 则不需要续期**/
        if (expireStrategy == ExpireStrategy.NO_EXPIRE || renewalStrategy == RenewalStrategy.NO_RENEWAL)
            return false;
        /** 如果操作行为是GET, 则需要判断刷新策略是否支持**/
        if (behavior == Behavior.GET && (renewalStrategy == RenewalStrategy.RENEWAL_ON_GET || renewalStrategy == RenewalStrategy.RENEWAL_ON_ALL))
            return true;

        /** 如果操作行为是SET, 则需要判断刷新策略是否支持**/
        if (behavior == Behavior.SET && (renewalStrategy == RenewalStrategy.RENEWAL_ON_SET || renewalStrategy == RenewalStrategy.RENEWAL_ON_ALL))
            return true;

        return false;
    }

    /**
     * 是否需要删除数据对象.
     *
     * @param dataHolder 数据持有对象
     * @param behavior   行为
     * @return the boolean
     */
    static boolean needDeleteOn(DataHolder dataHolder, Behavior behavior) {

        Assert.notNull(dataHolder);
        Assert.notNull(behavior);
        CacheMetaInfo metaInfo = dataHolder.getMetaInfo();
        ExpireStrategy expireStrategy = metaInfo.getExpireConfig().getExpireStrategy();

        /** 如果元数据表明不会过期 则不需要考虑删除**/
        if (expireStrategy == ExpireStrategy.NO_EXPIRE)
            return false;

        /**
         * 如果是时间维度带有超期限制, 则查询当前的剩余时间
         */
        if (expireStrategy == ExpireStrategy.EXPIRE_ON_TIME_LIMIT && metaInfo.getExpireConfig().getTimeLimit() > 0) {
            return dataHolder.remainTime() <= 0;
        }

        /**
         * 如果是读操作带有超期次数, 则判断当前缓存数据被读取次数
         */
        if (behavior == Behavior.GET && expireStrategy == ExpireStrategy.EXPIRE_ON_GET_LIMIT) {
            return dataHolder.readCount() >= metaInfo.getExpireConfig().getReadCountLimit();
        }

        /**
         * 如果是写操作带有超期次数, 则判断当前缓存数据被更改次数
         */
        if (behavior == Behavior.SET && expireStrategy == ExpireStrategy.EXPIRE_ON_SET_LIMIT) {
            return dataHolder.writeCount() >= metaInfo.getExpireConfig().getWriteCountLimit();
        }

        return false;
    }
}
