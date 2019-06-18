package com.attempt.core.cache.api;

import java.io.Serializable;

import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.ExpireConfig;

/**  
* @Description: 为不同的数据类型生成{@link CacheMetaInfo}.
* @author zhouyinbin  
* @date  
* @version V1.0  
*/        
public interface CacheMetaInfoFactory {
	/**
     * 生成{@link org.apache.shiro.session.Session}的{@link CacheMetaInfo}.
     *
     * @param sessionId {@link org.apache.shiro.session.Session#getId()}
     */
    CacheMetaInfo generateSessionMetaInfo(String sessionId);

    /**
     * 生成{@link Countable}类型的计数器的{@link CacheMetaInfo}.
     *
     * @param orginMetaInfo {@link Countable}类型的元数据
     * @return {@link CacheMetaInfo} 计数器元数据
     */
    CacheMetaInfo generateCounterMetaInfo(CacheMetaInfo orginMetaInfo);

    /**
     * 生成{@link CacheMetaInfo}的{@link CacheMetaInfo}.
     *
     * @param metaInfoToSave 待保存的{@link CacheMetaInfo}
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo generateMetaMetaInfo(CacheMetaInfo metaInfoToSave);

    /**
     * 生成Mutex的{@link CacheMetaInfo}.
     *
     * @param mutexName    信号量名称
     * @param expireConfig 过期与刷新策略
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo generateMutexMetaInfo(String mutexName, ExpireConfig expireConfig);

    /**
     * 生成List的{@link CacheMetaInfo}.
     *
     * @param listName     列表名称
     * @param itemType     容器内数据类型
     * @param expireConfig 过期与刷新策略
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo generateListMetaInfo(String listName, Class<? extends Serializable> itemType, ExpireConfig expireConfig);

    /**
     * 生成Map的{@link CacheMetaInfo}.
     *
     * @param mapName      Map名称
     * @param itemType     容器内数据类型
     * @param expireConfig 过期与刷新策略
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo generateMapMetaInfo(String mapName, Class<? extends Serializable> itemType, ExpireConfig expireConfig);

    /**
     * 生成Rank的{@link CacheMetaInfo}.
     *
     * @param rankName     Rank名称
     * @param itemType     容器内数据类型
     * @param expireConfig 过期与刷新策略
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo generateRankMetaInfo(String rankName, Class<? extends Serializable> itemType, ExpireConfig expireConfig);

    /**
     * 生成Table的{@link CacheMetaInfo}.
     *
     * @param itemType     容器内数据类型
     * @param expireConfig 过期与刷新策略
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo generateTableMetaInfo(Class<? extends Serializable> itemType, ExpireConfig expireConfig);
}
