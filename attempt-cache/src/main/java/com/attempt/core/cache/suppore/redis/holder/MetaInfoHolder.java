package com.attempt.core.cache.suppore.redis.holder;

import com.alibaba.fastjson.JSON;
import com.attempt.core.cache.api.MetaInfoOperations;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;

/**  
* @Description: 缓存单元元数据的Holder.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class MetaInfoHolder extends DataHolder implements MetaInfoOperations{

    /**
     * 构造方法.
     *
     * @param metaMetaInfo 元数据的元数据信息
     */
    MetaInfoHolder(CacheMetaInfo metaMetaInfo) {
        super(metaMetaInfo);
    }

    /**
     * 删除{@link CacheMetaInfo}.
     *
     */
    @Override
    public void delete() {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().del(key);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 获取{@link CacheMetaInfo}.
     *
     * @return {@link CacheMetaInfo}
     */
    @Override
    public CacheMetaInfo get() {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            return JSON.parseObject(commander.nativeCommander().get(key), CacheMetaInfo.class);
        }catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }

    /**
     * 续期.
     *
     * @param milliseconds 续期的毫秒数
     */
    @Override
    public void renewal(long milliseconds) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().pexpire(key, milliseconds);
        }catch (Exception e) {
			e.printStackTrace();
		}
    }


}
