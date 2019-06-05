package com.attempt.core.cache.models;

import java.io.Serializable;

/**
 *
 * @author zhouyinbin
 * @date 2019年6月5日 下午5:16:17
 *
 */
public class CacheMetaInfo {
	
    /**
     * 缓存数据名称.
     */
    private String name;

    /**
     * 缓存数据拥有者名称.
     */
    private String owner;

    /**
     * 缓存数据类型.
     */
    private CacheDataType dataType;

    /**
     * 当{@link CacheDataType}是容器级数据类型时，声明内部放置的数据类型
     */
    private Class<? extends Serializable> itemType;

    /**
     * 缓存数据过期策略.
     */
    private ExpireConfig expireConfig;

    /**
     * 缓存数据名称.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 缓存数据名称.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 缓存数据拥有者名称.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 缓存数据拥有者名称.
     *
     * @param owner the owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 缓存数据类型.
     *
     * @return the data type
     */
    public CacheDataType getDataType() {
        return dataType;
    }

    /**
     * 缓存数据类型.
     *
     * @param dataType the data type
     */
    public void setDataType(CacheDataType dataType) {
        this.dataType = dataType;
    }

    /**
     * 当{@link CacheDataType}是容器级数据类型时，声明内部放置的数据类型
     *
     * @return the item type
     */
    public Class<? extends Serializable> getItemType() {
        return itemType;
    }

    /**
     * 当{@link CacheDataType}是容器级数据类型时，声明内部放置的数据类型
     *
     * @param itemType the item type
     */
    public void setItemType(Class<? extends Serializable> itemType) {
        this.itemType = itemType;
    }

    /**
     * 缓存数据过期策略.
     *
     * @return the expire config
     */
    public ExpireConfig getExpireConfig() {
        return expireConfig;
    }

    /**
     * 缓存数据过期策略.
     *
     * @param expireConfig the expire config
     */
    public void setExpireConfig(ExpireConfig expireConfig) {
        this.expireConfig = expireConfig;
    }
}
