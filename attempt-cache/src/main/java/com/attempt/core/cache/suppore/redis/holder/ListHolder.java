package com.attempt.core.cache.suppore.redis.holder;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.attempt.core.cache.models.Behavior;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.models.ExpireStrategy;
import com.attempt.core.cache.models.ListOperations;
import com.attempt.core.cache.suppore.DataRefreshSupport;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;

/**  
* @Description: 列表缓存单元.
* @author zhouyinbin  
* @date 2019年6月2日
* @version V1.0  
*/
public class ListHolder<T extends Serializable> extends DataHolder implements ListOperations<T> {

    /**
     * 放入List中的数据类型
     */
    private Class<? extends T> itemClassType;

    /**
     * 构造方法.
     *
     * @param metaInfo 缓存数据单元的元数据信息
     */
    public ListHolder(CacheMetaInfo metaInfo, Class<? extends T> itemClassType) {
        super(metaInfo);
        this.itemClassType = itemClassType;
    }

    /**
     * 删除列表.
     *
     */
    @Override
    public void delete() {
        // 删除元数据
        if (metaInfoHolder != null)
            metaInfoHolder.delete();
        // 删除计数器
        if (counterHolder != null)
            counterHolder.delete();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().del(key);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 获取列表.
     *
     */
    @Override
    public List<T> get() {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        List<T> result = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            result = commander.smembers(key).stream()
                    .map(key -> JSON.parseObject(key, itemClassType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
			e.printStackTrace();
		}
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 添加元素.
     *
     */
    @Override
    public void add(T item) {
        Assert.notNull(item);
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.WRITE_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.SET))
            renewal();
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.SET)) {
            delete();
            return;
        }
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().sadd(key, JSON.toJSONString(item));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 删除某个key.
     *
     */
    @Override
    public void delete(T item) {
        Assert.notNull(item);
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.WRITE_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.SET))
            renewal();
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.SET)) {
            delete();
            return;
        }
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().srem(key, JSON.toJSONString(item));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 是否存在某个key.
     */
    @Override
    public boolean contains(T item) {
        Assert.notNull(item);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET)) {
            renewal();
        }
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            return commander.nativeCommander().sismember(key, JSON.toJSONString(item));
        } catch (Exception e) {
			e.printStackTrace();
		}
        return true;
    }

    /**
     * 续期.
     */
    @Override
    public void renewal() {
        // 如果过期策略不是按照时间策略的，则不需要续期
        if (getMetaInfo().getExpireConfig().getExpireStrategy() != ExpireStrategy.EXPIRE_ON_TIME_LIMIT)
            return;
        // 如果续期次数超过限定值，则不再续期
        long renewal = getMetaInfo().getExpireConfig().getRenewnalCountLimit();
        if (renewal != -1 && renewal < renewalCount())
            return;
        long milliseconds = super.getMetaInfo().getExpireConfig().getTimeLimit();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().pexpire(key, milliseconds);
        }  catch (Exception e) {
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

