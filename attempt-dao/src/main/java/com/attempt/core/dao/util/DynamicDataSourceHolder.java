package com.attempt.core.dao.util;
/**
 * 对当前线程的数据源类型的Holder.
 * @author zhouyinbin
 * @date 2019年6月10日 下午1:25:06
 *
 */
public class DynamicDataSourceHolder {
	
	/**
     * ThradLocal的数据源变量.
     */
	
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 设置当前线程数据源类型.
     *
     * @param dataSourceType the data source type
     */
    public static void set(String dataSourceType) {
        // 空字符串不接受
        if ("".equals(dataSourceType))
            return;
        contextHolder.set(dataSourceType);
    }

    /**
     * 获取当前线程的数据源类型,如果未赋值,则返回NULL.
     *
     * @return the data source type
     */
    public static String get() {
        return contextHolder.get();
    }

    /**
     * 清除数据源类型
     *
     * @author maeagle
     * @date 2016-1-19 16 :24:06
     */
    public static void clear() {
        contextHolder.remove();
    }
}
