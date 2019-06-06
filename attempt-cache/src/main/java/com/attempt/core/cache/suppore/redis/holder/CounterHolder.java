package com.attempt.core.cache.suppore.redis.holder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.attempt.core.cache.api.CounterOperations;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;

/**  
* @Description: 
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class CounterHolder extends DataHolder implements CounterOperations {
	/**
     * 计数器中的计数类型
     */
    private Map<CountType, String> keyMap;

    /**
     * 构造方法.
     *
     * @param metaInfo 缓存数据单元的元数据信息
     */
    CounterHolder(CacheMetaInfo metaInfo) {
        super(metaInfo);
        keyMap = Arrays.stream(CountType.values()).collect(Collectors.toMap(type -> type, type -> CacheUtil.generateKey(metaInfo, type.name())));
    }


    /**
     * 获取值.
     *
     * @param type {@link CountType}
     * @return the string
     * @author maeagle
     * @date 2016-3-2 9 :48:37
     */
    @Override
    public long getValue(CountType type) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            return Long.parseLong(commander.nativeCommander().get(keyMap.get(type)));
        } catch (Exception e) {
			e.printStackTrace();
		}
        return 0;
    }

    /**
     * Increase by 1.
     *
     * @param type {@link CountType}
     * @author maeagle
     * @date 2016-3-3 13 :58:18
     */
    @Override
    public void increase(CountType type) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().incr(keyMap.get(type));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Decrease by 1.
     *
     * @param type {@link CountType}
     */
    @Override
    public void decrease(CountType type) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().decr(keyMap.get(type));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 删除.
     *
     */
    @Override
    public void delete() {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            keyMap.values().stream().forEach(key -> commander.nativeCommander().del(key));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 续期.
     *
     * @param milliseconds 续期的毫秒数
     * @author maeagle
     * @date 2016-3-3 13 :58:18
     */
    @Override
    public void renewal(long milliseconds) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            keyMap.values().stream().forEach(key -> commander.nativeCommander().pexpire(key, milliseconds));
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    @Override
    public long renewalCount() {
        throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    @Override
    public long readCount() {
        throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    @Override
    public long writeCount() {
        throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
    }

}
