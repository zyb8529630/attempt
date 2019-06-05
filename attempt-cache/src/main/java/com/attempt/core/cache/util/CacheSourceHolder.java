package com.attempt.core.cache.util;
/**
 * 对当前线程缓存数据源类型的Holder.
 * @author zhouyinbin
 * @date 2019年6月5日 下午1:56:11
 *
 */
public class CacheSourceHolder {
	
	/**
	 * ThreadLocal的数据源变量
	 */
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    
    private CacheSourceHolder() {
    	
    }

    /**
     * 设置当前线程的数据源类型
     * @param dataSourceType
     */
    public static void set(String dataSourceType) {    	
    	if("".equals(dataSourceType)) 
    		return;
    	contextHolder.set(dataSourceType);
    }
    
    /**
     * 获取当前线程的数据源类型，如果未赋值则返回null
     * @return
     */
    public static String get() {
    	return contextHolder.get();
    }
    
    /**
     * 清楚当前线程的数据源类型
     */
    public static void clear() {
    	contextHolder.remove();
    }
}