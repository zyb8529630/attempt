package com.attempt.core.cache.api;

import java.io.Serializable;
import java.util.List;

/** @Description: List缓存操作接口.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface ListOperations<T extends Serializable> { 

    /**
     * 删除列表.
     */  
    void delete();

    /**
     * 获取列表.
     *
     */
    List<T> get();

    /**
     * 添加元素.
     */
    void add(T item);

    /**
     * 删除某个key.
     */
    void delete(T item);

    /**
     * 是否存在某个key.
     *
     */
    boolean contains(T item);

    /**
     * 续期.
     */
    void renewal();
}
