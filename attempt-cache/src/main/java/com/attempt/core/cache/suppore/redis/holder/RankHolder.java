package com.attempt.core.cache.suppore.redis.holder;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.attempt.core.cache.api.RankOperations;
import com.attempt.core.cache.models.Behavior;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.suppore.DataRefreshSupport;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;

/**  
* @Description: 列表缓存单元.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class RankHolder <T extends Serializable> extends DataHolder implements RankOperations<T> {

	  /**
     * 放入List中的数据类型
     */
    private Class<? extends T> itemClassType;

    /**
     * 构造方法.
     *
     * @param metaInfo 缓存数据单元的元数据信息
     */
    public RankHolder(CacheMetaInfo metaInfo, Class<? extends T> itemClassType) {
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
     * 特定的分值区间，获取区间内元素列表.
     *
     * @param fromScore 分值下限
     * @param toScore   分值上限
     */
    @Override
    public List<Entry<T>> getByScore(double fromScore, double toScore) {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        List<Entry<T>> result = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            result = commander.nativeCommander()
                    .zrangeByScoreWithScores(key, fromScore, toScore).stream().map(item -> new Entry<T>() {
                        @Override
                        public double getScore() {
                            return item.getScore();
                        }

                        @Override
                        public T getItem() {
                            return JSON.parseObject(item.getElement(), itemClassType);
                        }
                    }).collect(Collectors.toList());
        } catch (Exception e) {
			e.printStackTrace();
		}
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 特定的排名区间（从0开始），获取区间内元素列表.
     *
     * @param fromIndex 排名下限
     * @param toIndex   排名上限
     */
    @Override
    public List<Entry<T>> getByRank(long fromIndex, long toIndex) {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        List<Entry<T>> result = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            result = commander.nativeCommander()
                    .zrangeWithScores(key, fromIndex, toIndex).stream().map(item -> new Entry<T>() {
                        @Override
                        public double getScore() {
                            return item.getScore();
                        }

                        @Override
                        public T getItem() {
                            return JSON.parseObject(item.getElement(), itemClassType);
                        }
                    }).collect(Collectors.toList());
        } catch (Exception e) {
			e.printStackTrace();
		}
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 查询元素的得分.
     *
     * @param item the item
     * @return the boolean
     */
    @Override
    public double getScore(T item) {
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        double score = 0;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            score = commander.nativeCommander().zscore(key, JSON.toJSONString(item));
        } catch (Exception e) {
			e.printStackTrace();
		}
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return score;
    }

    /**
     * 添加元素.
     *
     * @param score 得分
     * @param item  the item
     */
    @Override
    public void add(double score, T item) {
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
            commander.nativeCommander().zadd(key, score, JSON.toJSONString(item));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 删除某个key.
     *
     * @param item the item
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
            commander.nativeCommander().zrem(key, JSON.toJSONString(item));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
