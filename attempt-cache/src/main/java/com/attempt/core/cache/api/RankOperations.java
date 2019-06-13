package com.attempt.core.cache.api;

import java.io.Serializable;
import java.util.List;
/**  
* @Description: 有序排名缓存操作接口.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface RankOperations<T extends Serializable> {
	 /**
     * 删除列表.
     */ 
    void delete();

    /**
     * 特定的分值区间，获取区间内元素列表.
     *
     * @param fromScore 分值下限
     * @param toScore   分值上限
     */
    List<Entry<T>> getByScore(double fromScore, double toScore);

    /**
     * 特定的排名区间（从0开始），获取区间内元素列表.
     *
     * @param fromIndex 排名下限
     * @param toIndex   排名上限
     */
    List<Entry<T>> getByRank(long fromIndex, long toIndex);

    /**
     * 查询元素的得分.
     *
     * @param item the item
     * @return the boolean
     */
    double getScore(T item);

    /**
     * 添加元素.
     *
     * @param score 得分
     * @param item  the item
     */
    void add(double score, T item);

    /**
     * 删除某个key.
     *
     * @param item the item
     */
    void delete(T item);

    /**
     * 续期.
     */
    void renewal();

    /**
     * 带分数的查询结果对象
     *
     * @param <T>
     */
    interface Entry<T> {
        /**
         * 获取分数
         *
         * @return
         */
        double getScore();

        /**
         * 获取查询对象
         *
         * @return
         */
        T getItem();
    }
}
