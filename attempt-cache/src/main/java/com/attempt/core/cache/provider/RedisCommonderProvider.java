package com.attempt.core.cache.provider;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.attempt.core.cache.models.RedisMode;
import com.attempt.core.cache.suppore.redis.ClusterSlotSupport;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.common.SpringContextHolder;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;
import redis.clients.util.Sharded;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redis连接池的构造工厂.
 * @author zhouyinbin
 * @date 2019年6月5日 下午5:46:09
 *
 */
public class RedisCommonderProvider implements FactoryBean<RedisCommander>, InitializingBean, DisposableBean{

    private static Logger logger = LoggerFactory.getLogger(RedisCommonderProvider.class);

	/**
	 * 默认的Redis数据源属性
	 */
	public static final String DEFAULT_CACHE_SOURCE_NAME = "defaultCacheSource";

	 /**
	  * Redis的池化通用配置, 用于集群环境
	  */
	private GenericObjectPoolConfig poolConfig;
	
	/**
     * 当前实例在Spring注册时的名称
     */
	private String beanName;
	
	/**
	 *  Redis服务地址. 格式: host1:port1;host2:port2.
	 */
	private String addresses;
	
	/**
	 * 连接超时时长
	 */
	private int timeout;
	
	/**
	 * redis访问密码
	 */
	private String password;
	
    /**
     * Redis运行模式.
     *
     * @See {@link RedisMode}
     */
    private RedisMode redisMode;
    
    /**
     *  最大可重定向次数.
     */
    private int maxRedirections;
    
    /**
     * 当{@link #redisMode}={@link RedisMode#CLUSTER}时, 所用的Redis操作实例.
     */
    private JedisCluster clusterPool;

    /**
     * 当{@link #redisMode}={@link RedisMode#SINGLETON}时, 所用的Redis操作实例.
     */
    private Pool<Jedis> singletonPool;
    
    /**
     * 当{@link #redisMode}={@link RedisMode#SENTINEL}时, 所用的Redis操作实例
     */
    private JedisSentinelPool sentinelPool;
    
    /**
     * 当{@link #redisMode}={@link RedisMode#SHAREED}时, 所用的Redis操作实例.
     */
    private ShardedJedisPool shardPool;
    
    
	 /**
	  * 获取RedisCommander的实例
	  */
	@Override
	public RedisCommander getObject() throws Exception {
		
		 if (redisMode == RedisMode.SINGLETON) {
	            return new RedisCommander(singletonPool.getResource());
	        } else if (redisMode == RedisMode.SENTINEL) {
	            return new RedisCommander(sentinelPool.getResource());
	        } else if (redisMode == RedisMode.CLUSTER) {
	            return new RedisCommander(clusterPool);
	        } else if (redisMode == RedisMode.SHAREED) {
	            return new RedisCommander(shardPool.getResource());
	        } else
	            throw new NullPointerException("Invalid property [cache.redis.mode]!");
	}

	/**
	 * 声明提供RedisCommander类型
	 */
	@Override
	public Class<?> getObjectType() {
		return RedisCommander.class;
	}

	/**
	 * 声明为单例
	 */
	@Override
	public boolean isSingleton() {
		return false;
	}
	
	/**
	 * 根据不同的配置项，实例化不同Redis策略的连接池
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		
		/**
		 * 识别Redis服务器运行模式
		 */
		Assert.notNull(redisMode, "Please config property [cache.redis.mode]");   
		
		/**
		 *  识别Redis服务器地址集合
		 */
		Assert.isTrue(!StringUtils.isEmpty(addresses), "Please config property [cache.redis.addresses]");
		
		/**
		 * 如果未配置{@link #poolConfig}实例信息,则使用默认的{@code redisDefaultPoolConfig}配置
		 */
		if (poolConfig == null)
	           poolConfig = SpringContextHolder.getBean("redisDefaultPoolConfig");
		
		/**
		 * 根据Redis运行模式不同, 构造不同的连接池
		 */
        if (redisMode == RedisMode.SINGLETON) {
            HostAndPort oneAddr = Arrays.stream(addresses.split(";")).map(address -> {
                String[] hostAndPort = address.split(":");
                return new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            }).findFirst().orElseThrow(NullPointerException::new);
            singletonPool = new JedisPool(poolConfig, oneAddr.getHost(), oneAddr.getPort(), timeout, password);
        }
        
        /**
         * 当模式为 RedisMode.SENTINEL 时，需要在addresses配置项中设置masterName
         */
        else if (redisMode == RedisMode.SENTINEL) {
            String[] splitStrArr = addresses.split("@");
            // 哨兵模式下，一定要设置masterName
            Assert.isTrue(splitStrArr.length == 2, "Need set master name (split with @).");
            String masterName = splitStrArr[0];
            Set<String> sentinels = Arrays.stream(splitStrArr[1].split(";")).collect(Collectors.toSet());
            sentinelPool = new JedisSentinelPool(masterName, sentinels, poolConfig, timeout, password);
        }
        /**
         * 	当模式为 RedisMode.CLUSTER 时，需要计算哈希槽
         */
        else if (redisMode == RedisMode.CLUSTER) {
            // Redis地址列表.
            Set<HostAndPort> jedisClusterNodes = Arrays.stream(addresses.split(";")).map(address -> {
                String[] hostAndPort = address.split(":");
                return new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            }).collect(Collectors.toSet());
            clusterPool = new JedisCluster(jedisClusterNodes, timeout, timeout, maxRedirections, password, poolConfig);
            /**
             	集群模式下,需要初始化哈希槽信息
             */
            ClusterSlotSupport.initClusterSlot(beanName, new RedisCommander(clusterPool));
        }
        else if (redisMode == RedisMode.SHAREED) {
            // Redis地址列表.
            List<JedisShardInfo> shardInfoList = Arrays.stream(addresses.split(";")).map(address -> {
                String[] hostAndPort = address.split(":");
                JedisShardInfo shardInfo = new JedisShardInfo(hostAndPort[0], Integer.parseInt(hostAndPort[1]), hostAndPort[0]);
                shardInfo.setPassword(password);
                shardInfo.setConnectionTimeout(timeout);
                shardInfo.setSoTimeout(timeout);
                return shardInfo;
            }).collect(Collectors.toList());
            shardPool = new ShardedJedisPool(poolConfig, shardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
        } else
            throw new NullPointerException("Invalid property [cache.redis.mode]!");

		
	}
	
	
    /**
     * Redis的池化通用配置.
     *
     * @param poolConfig the pool config
     */
	public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}
	
    /**
     * Redis服务. 格式: host1:port1;host2:port2.
     *
     * @param addresses the addresses
     */
	public void setAddresses(String addresses) {
		this.addresses = addresses;
	}

    /**
     * 连接超时时长.
     *
     * @param timeout the timeout
     */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
    /**
     * 访问Redis的密码
     *
     * @param password 密码
     */
	public void setPassword(String password) {
		this.password = password;
	}

    /**
     * * Redis运行模式.
     *
     * @param redisMode
     * @See {@link RedisMode}
     */
	public void setRedisMode(RedisMode redisMode) {
		this.redisMode = redisMode;
	}

    /**
     * 最大可重定向次数.
     *
     * @param maxRedirections the max redirections
     */
	public void setMaxRedirections(int maxRedirections) {
		this.maxRedirections = maxRedirections;
	}
	
    /**
     * 获取当前实例对应的Bean名称
     *
     * @param name
     */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	@Override
	public void destroy() throws Exception {
		try {
            if (redisMode == RedisMode.SINGLETON) {
                singletonPool.close();
            } else if (redisMode == RedisMode.CLUSTER) {
                clusterPool.close();
            } else if (redisMode == RedisMode.SHAREED) {
                shardPool.close();
            } else if(redisMode == RedisMode.SENTINEL) {
            	sentinelPool.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }		
	}
}
