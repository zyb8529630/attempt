package com.attempt.core.cache.api;

import com.attempt.core.cache.models.CacheMetaInfo;

/**  
* @Description: 针对某一块业务缓存数据的操作服务
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface CacheManageService {
	/**
     * 获取或构建待缓存元素的元数据。
     * 如果缓存数据需要通过多个元数据描述，则返回具有唯一性约束的元数据信息即可。
     *
     * @return 元数据信息
     */
    CacheMetaInfo getCacheMetaInfo();

    /**
     * 新增缓存元素操作
     */
    void init();

    /**
     * 清除缓存元素操作
     *
     */
    void clear();

    /**
     * 刷新缓存数据操作。用于通过web端管理缓存数据
     */
    void refresh();

    /**
     * 声明系统启动时是否自动初始化缓存数据
     *
     */
    boolean autoInit();

    /**
     * 声明是否允许通过对缓存数据手动刷新和删除
     *
     */
    boolean manageEnabled();
}
