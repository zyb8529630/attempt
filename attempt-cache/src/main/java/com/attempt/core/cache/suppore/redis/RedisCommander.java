package com.attempt.core.cache.suppore.redis;

import org.springframework.util.Assert;

import redis.clients.jedis.JedisCommands;

/**
 * Redis的命令操作工具封装.由于Redis的不同运行模式下的命令集合不同，因此需要基于 JedisCommands再封装一层
 * @author zhouyinbin
 * @date 2019年6月5日 下午5:48:20
 *
 */
public class RedisCommander  implements AutoCloseable{ 

	/**
     * Redis连接
     */
	private JedisCommands jedisCommander;
	
    public RedisCommander(JedisCommands connection) {
        Assert.notNull(connection);
        this.jedisCommander = connection;
    }
    
    /**
     * 构造方法
     * connection Jedis原生连接
     */
	@Override
	public void close() throws Exception {
		
	}
	
	/**
	 * 返回通用的Jedis原生连接
	 * @return Jedis原生连接
	 */
    public JedisCommands nativeCommander() {
        return jedisCommander;
    }		
	
}
