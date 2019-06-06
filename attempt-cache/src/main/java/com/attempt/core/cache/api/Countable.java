package com.attempt.core.cache.api;
/**  
* @Description: 描述可计数缓存数据对象的行为
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface Countable {
	 /**
     * 缓存数据对象的续期次数.
     *
     * @return the long
     */
    long renewalCount();

    /**
     * 缓存数据对象的读次数.
     *
     * @return the long
     */
    long readCount();

    /**
     * 缓存数据对象的写次数.
     *
     * @return the long
     */
    long writeCount();

    /**
     * 缓存数据对象的剩余存活时间(毫秒).
     * {@link com.attemp.core.cache.models.ExpireConfig#expireStrategy} != {@link com.attemp.core.cache.models.ExpireStrategy#EXPIRE_ON_TIME_LIMIT}时, 会返回-1
     *
     * @return the long
     */
    long remainTime();
}
