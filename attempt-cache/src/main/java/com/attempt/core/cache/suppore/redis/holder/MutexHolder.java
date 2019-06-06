package com.attempt.core.cache.suppore.redis.holder;

import org.springframework.util.Assert;

import com.attempt.core.cache.api.MutexOperations;
import com.attempt.core.cache.models.Behavior;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.suppore.DataRefreshSupport;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;

/**  
* @Description: 信号量缓存单元.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class MutexHolder  extends DataHolder implements MutexOperations {

	 /**
     * 构造方法.
     *
     * @param metaInfo 缓存数据单元的元数据信息
     */
    public MutexHolder(CacheMetaInfo metaInfo) {
        super(metaInfo);
    }

    /**
     * 删除.
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
     * 获取值.
     *
     * @return the string
     */
    @Override
    public String get() {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        String result = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            result = commander.nativeCommander().get(key);
        } catch (Exception e) {
			e.printStackTrace();
		}
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 设置值.
     *
     * @param value the value
     */
    @Override
    public void set(String value) {
        Assert.notNull(value);
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
            commander.nativeCommander().set(key, value);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Increase by 1.
     */
    @Override
    public void increase() {
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
            commander.nativeCommander().incr(key);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Increase by step.
     *
     * @param step the step
     */
    @Override
    public void increase(long step) {
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
            commander.nativeCommander().incrBy(key, step);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Decrease by 1.
     *
     */
    @Override
    public void decrease() {
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
            commander.nativeCommander().decr(key);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Decrease by step.
     *
     * @param step the step
     */
    @Override
    public void decrease(long step) {
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
            commander.nativeCommander().decrBy(key, step);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
