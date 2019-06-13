package com.attempt.core.cache.api;

import java.io.Serializable;
import java.util.List;

import com.attempt.core.cache.models.IndexItem;
import com.attempt.core.common.XOR;


/**  
* @Description: 索引表格缓存单元.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/ 
public interface TableOperations<T extends Serializable> {
	 /**
     * 删除表格.
     */
    void delete();


    /**
     * 删除表格中特定的单元
     *
     * @param item 待删除数据单元
     */
    void delete(T item);

    /**
     * 根据索引，获取数据列表.
     * 当{@code indexItems}只有一个值时，{@code xor}该参数不起效
     *
     * @param xor        逻辑操作符（并，或）
     * @param indexItems 索引单元
     * @return 数据列表.
     */
    List<T> get(XOR xor, IndexItem... indexItems);

    /**
     * 根据主键信息获取唯一的数据单元.
     * 生成{@code primaryKey}的方法：
     * <p>
     * 1. 使用{@link com.halo.core.cache.support.TableSupport#generatePrimaryKey(Serializable)}生成
     * 2. 将所有在数据类型的{@link CacheTable#primaryKey()}对应的属性值，按照顺序排列，以“^”分割
     *
     * @param primaryKey 主键信息
     * @return 查询到的唯一数据元.
     */
    T get(String primaryKey);

    /**
     * 向表格中添加数据单元
     *
     * @param item 数据单元
     */
    void add(T item);

    /**
     * 添加或更新某一个数据单元
     *
     * @param item 数据单元
     */
    void set(T item);

    /**
     * 根据索引单元，计算缓存数据单元数量.
     * 当{@code indexItems}只有一个值时，{@code xor}该参数不起效
     *
     * @param xor        逻辑操作符（并，或）
     * @param indexItems 索引单元
     */
    long count(XOR xor, IndexItem... indexItems);

    /**
     * 根据{@code item}中的{@code primaryKeys}，在缓存中查询是否存在该记录
     *
     * @param item 待查询的数据单元
     */
    boolean contains(T item);

    /**
     * 续期.
     */
    void renewal();

}
