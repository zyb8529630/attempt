package com.attempt.core.cache.api;

import com.attempt.core.cache.models.CacheMetaInfo;

/**  
* @Description: 对缓存元数据的操作集合.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface MetaInfoOperations {
	  /**
     * 删除{@link CacheMetaInfo}.
     */
    void delete();

    /**
     * 获取{@link CacheMetaInfo}.
     *
     * @return {@link CacheMetaInfo}
     */
    CacheMetaInfo get();

    /**
     * 续期.
     *
     * @param milliseconds 续期的毫秒数
     */
    void renewal(long milliseconds);
}
