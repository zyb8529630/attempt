package com.attempt.core.cache.api;

import java.io.Serializable;
import java.util.Map;
/**  
* @Description: Map缓存操作接口.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface MapOperations <T extends Serializable> {
	 /** 
     * 删除列表.
     */
    void delete();

    /**
     * 获取列表.
     */
    Map<String, T> get();

    /**
     * 获取指定元素.
     *
     * @param fieldKey Map中的Key
     */
    T get(String fieldKey);

    /**
     * 添加元素.
     *
     * @param fieldKey Map中的Key
     * @param item     the item
     */
    void add(String fieldKey, T item);

    /**
     * 删除某个key.
     *
     * @param fieldKey Map中的Key
     */
    void delete(String fieldKey);

    /**
     * 是否存在某个key.
     *
     * @param fieldKey Map中的Key
     * @return the boolean
     */
    boolean contains(String fieldKey);

    /**
     * 续期.
     */
    void renewal();
}
