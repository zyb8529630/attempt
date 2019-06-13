package com.attempt.core.cache.models;

import java.util.Objects;

/**  
* @Description: 缓存表格的索引单元.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class IndexItem {
	/**
     * 索引名称.
     */
    private String name;
    /**
     * 索引值. 
     */
    private String value;

    /**
     * 构造方法
     */
    public IndexItem() {
        // 无操作
    }

    /**
     * 构造方法
     *
     * @param name
     * @param value
     */
    public IndexItem(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 索引名称.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 索引名称.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 索引值.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * 索引值.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 同时将{@link #name}与{@link #value}比较，两者完全相等，才认为相同
     *
     * @param o 待比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexItem indexItem = (IndexItem) o;
        return Objects.equals(name, indexItem.name) &&
                Objects.equals(value, indexItem.value);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
