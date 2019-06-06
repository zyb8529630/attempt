package com.attempt.core.cache.suppore.redis.holder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.attempt.core.cache.api.MapOperations;
import com.attempt.core.cache.models.Behavior;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.suppore.DataRefreshSupport;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;

/**  
* @Description: Map缓存单元.
* @author zhouyinbin  
* @date  
* @version V1.0  
*/
public class MapHolder<T extends Serializable> extends DataHolder implements MapOperations<T> {
	 /**
     * 放入List中的数据类型
     */
    private Class<? extends T> itemClassType;

    /**
     * 构造方法.
     *
     * @param metaInfo 缓存数据单元的元数据信息
     */
    public MapHolder(CacheMetaInfo metaInfo, Class<? extends T> itemClassType) {
        super(metaInfo);
        this.itemClassType = itemClassType;
    }

    /**
     * 删除列表.
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
     * @return the session
     */
    @Override
    public Map<String, T> get() {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        Map<String, T> result = new HashMap<>();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.hgetall(key).forEach(entry -> {
                result.put(entry.getKey(), JSON.parseObject(entry.getValue(), itemClassType));
            });
        }catch (Exception e) {
			e.printStackTrace();
		}
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 获取指定元素.
     *
     * @param fieldKey Map中的Key
     */
    @Override
    public T get(String fieldKey) {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        String valueStr = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            valueStr = commander.nativeCommander().hget(key, fieldKey);
        }catch (Exception e) {
			e.printStackTrace();
		}
        T value = JSON.parseObject(valueStr, itemClassType);
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return value;
    }

    /**
     * 添加元素.
     *
     * @param fieldKey Map中的Key
     * @param item     the item
     */
    @Override
    public void add(String fieldKey, T item) {
        Assert.notNull(fieldKey);
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
            commander.nativeCommander().hset(key, fieldKey, JSON.toJSONString(item));
        }catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 删除某个key.
     *
     * @param fieldKey Map中的Key
     */
    @Override
    public void delete(String fieldKey) {
        Assert.notNull(fieldKey);
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
            commander.nativeCommander().hdel(key, fieldKey);
        }catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 是否存在某个key.
     *
     * @param fieldKey Map中的Key
     * @return the boolean
     */
    @Override
    public boolean contains(String fieldKey) {
        Assert.notNull(fieldKey);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET)) {
            renewal();
        }
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            return commander.nativeCommander().hexists(key, fieldKey);
        }catch (Exception e) {
			e.printStackTrace();
		}
        return true;
    }
}
