package com.attempt.core.queue.support;
/**
 * <code>QueueConnectionHolder</code>当前线程的mqconnection的holder
 * @author zhouyinbin
 * @date 2019年6月10日 上午9:18:31
 *
 */
public class QueueConnectionHolder {

	
	
	  private QueueConnectionHolder() {
	        // nothing todo 不允许实例化
	    }

	    /**
	     * ThradLocal的连接变量.
	     */
	    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

	    /**
	     * 设置当前线程mqconnection类型
	     *
	     * @param mqType
	     *  the mq connection type
	     */
	    public static void set(String mqType) {
	        contextHolder.set(mqType);
	    }

	    /**
	     * 获取当前线程mqconnection类型,如果未赋值,则返回默认的.
	     *
	     * @return the data source type
	     */
	    public static String get() {
	        return contextHolder.get();
	    }

	    /**
	     * 清除mqconnection
	     *
	     */
	    public static void clear() {
	        contextHolder.remove();
	    }
}
