package com.attempt.core.cache.models;
/**  
* @Description:  缓存的超时配置.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class ExpireConfig {
	 /**
     * 超时时间（毫秒）.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     */
    private long timeLimit;
 
    /**
     * 读取次数.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_GET_LIMIT} 有效
     */
    private long readCountLimit;

    /**
     * 写入次数.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_SET_LIMIT} 有效
     */
    private long writeCountLimit;

    /**
     * 续期次数极限.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     * 如果续期次数超过限制，则不再续期（-1为无限制）
     */
    private long renewnalCountLimit = -1;

    /**
     * 超时策略.
     *
     * @see {@link ExpireStrategy}
     */
    private ExpireStrategy expireStrategy = ExpireStrategy.NO_EXPIRE;

    /**
     * 时间的刷新策略.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     *
     * @see {@link ExpireStrategy}
     */
    private RenewalStrategy renewalStrategy = RenewalStrategy.RENEWAL_ON_SET;

    /**
     * 超时时间（毫秒）.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     *
     * @return the time limit
     */
    public long getTimeLimit() {
        return timeLimit;
    }

    /**
     * 超时时间（毫秒）.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     *
     * @param timeLimit the time limit
     */
    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * 读取次数.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_GET_LIMIT} 有效
     *
     * @return the read count limit
     */
    public long getReadCountLimit() {
        return readCountLimit;
    }

    /**
     * 读取次数.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_GET_LIMIT} 有效
     *
     * @param readCountLimit the read count limit
     */
    public void setReadCountLimit(long readCountLimit) {
        this.readCountLimit = readCountLimit;
    }

    /**
     * 写入次数.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_SET_LIMIT} 有效
     *
     * @return the write count limit
     */
    public long getWriteCountLimit() {
        return writeCountLimit;
    }

    /**
     * 写入次数.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_SET_LIMIT} 有效
     *
     * @param writeCountLimit the write count limit
     */
    public void setWriteCountLimit(long writeCountLimit) {
        this.writeCountLimit = writeCountLimit;
    }


    /**
     * 续期次数极限.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     * 如果续期次数超过限制，则不再续期（-1为无限制）
     *
     * @return the refresh count limit
     */
    public long getRenewnalCountLimit() {
        return renewnalCountLimit;
    }

    /**
     * 续期次数极限.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     * 如果续期次数超过限制，则不再续期（-1为无限制）
     *
     * @param renewnalCountLimit the refresh count limit
     */
    public void setRenewnalCountLimit(long renewnalCountLimit) {
        this.renewnalCountLimit = renewnalCountLimit;
    }

    /**
     * 超时策略.
     *
     * @return expire strategy
     */
    public ExpireStrategy getExpireStrategy() {
        return expireStrategy;
    }

    /**
     * 超时策略.
     *
     * @param expireStrategy the expire strategy
     */
    public void setExpireStrategy(ExpireStrategy expireStrategy) {
        this.expireStrategy = expireStrategy;
    }

    /**
     * 时间的刷新策略.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     *
     * @return refresh strategy
     */
    public RenewalStrategy getRenewalStrategy() {
        return renewalStrategy;
    }

    /**
     * 时间的刷新策略.
     * <p>
     * 当{@link #expireStrategy}={@link ExpireStrategy#EXPIRE_ON_TIME_LIMIT} 有效
     *
     * @param renewalStrategy the refresh strategy
     */
    public void setRenewalStrategy(RenewalStrategy renewalStrategy) {
        this.renewalStrategy = renewalStrategy;
    }
}
