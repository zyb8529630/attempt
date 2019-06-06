package com.attempt.core.cache.suppore.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.util.Assert;
import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.attempt.core.cache.util.CacheUtil;
import com.attempt.core.common.SpringContextHolder;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.JedisClusterCRC16;

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

    /**
     * 构造方法
     *
     * @param connection Jedis原生连接
     */
    public RedisCommander(JedisCommands connection) {
        Assert.notNull(connection);
        this.jedisCommander = connection;
    }

    /**
     * 批量插入或更新一个Redis的HashMap
     * <p>
     * 使用{@link Pipeline}管道模式
     *
     * @param key     Redis的Key
     * @param dataSet HashMap数据集
     * @param <T>     HashMap数据集包含的元素类型
     * @throws Exception 
     */
    public <T extends Serializable> void hset(String key, Map<String, T> dataSet) throws Exception {
        Assert.notNull(key);
        Assert.notEmpty(dataSet);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则需要通过key来推算可能存储的hash槽点，进而使用管道函数
         */
        if (jedisCommander instanceof JedisCluster) {
            JedisCluster cluster = (JedisCluster) jedisCommander;
            int slot = JedisClusterCRC16.getSlot(key);
            Map.Entry<Long, String> entry = SpringContextHolder.getBean(ClusterSlotSupport.class).getSlotHostMap().lowerEntry((long) slot);
            Jedis commander = cluster.getClusterNodes().get(entry.getValue()).getResource();
            Pipeline pipeline = commander.pipelined();
            dataSet.forEach((k, v) -> pipeline.hset(key, k, JSON.toJSONString(v)));
            pipeline.sync();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接强转
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            Pipeline pipeline = commander.pipelined();
            dataSet.forEach((k, v) -> pipeline.hset(key, k, JSON.toJSONString(v)));
            pipeline.sync();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接强转
         */
        else if (jedisCommander instanceof ShardedJedis) {
            ShardedJedisPipeline shardedPipeline = ((ShardedJedis) jedisCommander).pipelined();
            dataSet.forEach((k, v) -> shardedPipeline.hset(key, k, JSON.toJSONString(v)));
            shardedPipeline.sync();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 使用hscan函数重新封装hgetall
     *
     * @param key 键
     * @return list
     */
    public List<Map.Entry<String, String>> hgetall(String key) {
        List<Map.Entry<String, String>> resultList = new ArrayList<>();
        ScanResult<Map.Entry<String, String>> scanResult = null;
        do {
            scanResult = jedisCommander.hscan(key,
                    scanResult == null ? ScanParams.SCAN_POINTER_START : scanResult.getStringCursor(),
                    CacheUtil.createScanParams());
            resultList.addAll(scanResult.getResult());
        } while (!"0".equals(scanResult.getStringCursor()));
        return resultList;
    }

    /**
     * 批量删除多个Key
     * <p>
     * 如果Redis是{@code CLUSTER}和{@code SINGLETON}模式，使用默认
     * <p>
     * 如果是{@code SHAREED} 使用管道
     *
     * @param keyStream 待删除Key的{@link Stream}对象
     */
    public void delBatch(Stream<String> keyStream) {
        Assert.notNull(keyStream);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则不必走管道方式。
         *  因为key可能分布的节点不同。效率与直接插入一样。
         */
        if (jedisCommander instanceof JedisCluster) {
            keyStream.forEach(jedisCommander::del);
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接强转
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            commander.del(keyStream.toArray(String[]::new));
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SHAREED}，则直接强转
         */
        else if (jedisCommander instanceof ShardedJedis) {
            ShardedJedis shardedJedis = (ShardedJedis) jedisCommander;
            ShardedJedisPipeline shardedPipeline = shardedJedis.pipelined();
            keyStream.forEach(shardedPipeline::del);
            shardedPipeline.sync();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 批量更新多个key的超期时间
     * <p>
     * 如果Redis是{@code CLUSTER}模式，依然使用默认
     * <p>
     * 如果是{@code SINGLETON}或{@code SHAREED} 则使用管道
     *
     * @param keyStream    待删除Key的{@link Stream}对象
     * @param milliseconds 毫秒数
     */
    public void pexpireBatch(Stream<String> keyStream, long milliseconds) {
        Assert.notNull(keyStream);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则不必走管道方式。
         *  因为key可能分布的节点不同。效率与直接插入一样。
         */
        if (jedisCommander instanceof JedisCluster) {
            keyStream.forEach(key -> jedisCommander.pexpire(key, milliseconds));
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接强转
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            Pipeline pipeline = commander.pipelined();
            keyStream.forEach(key -> pipeline.pexpire(key, milliseconds));
            pipeline.sync();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SHAREED}，则直接强转
         */
        else if (jedisCommander instanceof ShardedJedis) {
            ShardedJedisPipeline shardedPipeline = ((ShardedJedis) jedisCommander).pipelined();
            keyStream.forEach(key -> shardedPipeline.pexpire(key, milliseconds));
            shardedPipeline.sync();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 使用sscan函数重新封装smembers
     *
     * @param key 键
     * @return list
     */
    public List<String> smembers(String key) {
        List<String> resultList = new ArrayList<>();
        ScanResult<String> scanResult = null;
        do {
            scanResult = jedisCommander.sscan(key,
                    scanResult == null ? ScanParams.SCAN_POINTER_START : scanResult.getStringCursor(),
                    CacheUtil.createScanParams());
            resultList.addAll(scanResult.getResult());
        } while (!"0".equals(scanResult.getStringCursor()));
        return resultList;
    }

    /**
     * 批量插入更新多个Redis的Set
     * <p>
     * 如果Redis是{@code CLUSTER}模式，依然使用默认
     * <p>
     * 如果是{@code SINGLETON}或{@code SHAREED} 则使用管道
     *
     * @param keyAndData 包含多个key与数据的集合

     */
    public void ssetBatch(Map<String, List<String>> keyAndData) {
        Assert.notEmpty(keyAndData);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则不必走管道方式。
         *  因为key可能分布的节点不同。效率与直接插入一样。
         */
        if (jedisCommander instanceof JedisCluster) {
            keyAndData.forEach((k, data) -> jedisCommander.sadd(k, data.toArray(new String[0])));
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接强转
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            Pipeline pipeline = commander.pipelined();
            keyAndData.forEach((k, data) -> pipeline.sadd(k, data.toArray(new String[0])));
            pipeline.sync();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SHAREED}，则直接强转
         */
        else if (jedisCommander instanceof ShardedJedis) {
            ShardedJedisPipeline shardedPipeline = ((ShardedJedis) jedisCommander).pipelined();
            keyAndData.forEach((k, data) -> shardedPipeline.sadd(k, data.toArray(new String[0])));
            shardedPipeline.sync();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 批量删除多个RedisKey中的Set内元素
     * <p>
     * 如果Redis是{@code CLUSTER}模式，依然使用默认
     * <p>
     * 如果是{@code SINGLETON}或{@code SHAREED} 则使用管道
     *
     * @param sKeyAndSMemKey 包含多个RedisKey中的Set内Key集合

     */
    public void sremBatch(Map<String, String> sKeyAndSMemKey) {
        Assert.notEmpty(sKeyAndSMemKey);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则不必走管道方式。
         *  因为key可能分布的节点不同。效率与直接插入一样。
         */
        if (jedisCommander instanceof JedisCluster) {
            sKeyAndSMemKey.forEach(jedisCommander::srem);
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接强转
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            Pipeline pipeline = commander.pipelined();
            sKeyAndSMemKey.forEach(pipeline::srem);
            pipeline.sync();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SHAREED}，则直接强转
         */
        else if (jedisCommander instanceof ShardedJedis) {
            ShardedJedisPipeline shardedPipeline = ((ShardedJedis) jedisCommander).pipelined();
            sKeyAndSMemKey.forEach(shardedPipeline::srem);
            shardedPipeline.sync();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 取两个set集合的合集
     *
     * @param keys key集合
     */
    @SuppressWarnings("unchecked")
    public Stream<String> sunion(String... keys) {
        Assert.notEmpty(keys);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则直接使用{@code sunion}。
         */
        if (jedisCommander instanceof JedisCluster) {
            JedisCluster cluster = (JedisCluster) jedisCommander;
            return cluster.sunion(keys).stream();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接使用{@code sunion}。
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            return commander.sunion(keys).stream();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SHAREED}，只能在java端使用Set来做并集
         */
        else if (jedisCommander instanceof ShardedJedis) {
            return Arrays.stream(keys).map(key -> (Collection<String>) smembers(key))
                    .reduce(Collections.emptySet(), CollectionUtils::union).stream();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 取两个set集合的交集
     *
     * @param keys key集合
     */
    @SuppressWarnings("unchecked")
    public Stream<String> sinter(String... keys) {
        Assert.notEmpty(keys);
        /**
         *  如果Redis 模式为{@link RedisMode#CLUSTER}，则直接使用{@code sinter}。
         */
        if (jedisCommander instanceof JedisCluster) {
            JedisCluster cluster = (JedisCluster) jedisCommander;
            return cluster.sinter(keys).stream();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SINGLETON}，则直接使用{@code sinter}。
         */
        else if (jedisCommander instanceof Jedis) {
            Jedis commander = (Jedis) jedisCommander;
            return commander.sinter(keys).stream();
        }
        /**
         *  如果Redis 模式为{@link RedisMode#SHAREED}，只能在java端使用Set来做交集
         */
        else if (jedisCommander instanceof ShardedJedis) {
            return Arrays.stream(keys).map(key -> (Collection<String>) smembers(key))
                    .reduce(Collections.emptySet(), CollectionUtils::intersection).stream();
        } else {
            throw new NullPointerException("{cache.redis.mode} is incorrect");
        }
    }

    /**
     * 对在Redis中创建的索引信息进行清理。
     * <p>
     * 如果在索引内容为空，则可以清除索引Key，同时清除索引表中存放的索引Key
     *
     * @param indexKey 索引表Key
     */
    public void indexTableClearup(String indexKey) {
        Assert.notNull(indexKey);
        this.smembers(indexKey).forEach(index -> {
            if (jedisCommander.scard(index) == 0) {
                jedisCommander.del(index);
                jedisCommander.srem(indexKey, index);
            }
        });
    }


    /**
     * 返回通用的Jedis原生连接
     *
     * @return Jedis原生连接
     */
    public JedisCommands nativeCommander() {
        return jedisCommander;
    }

    /**
     * 关闭Redis连接.
     */
    @Override
    public void close() {
        try {
            if (jedisCommander instanceof Jedis) {
                ((Jedis) jedisCommander).close();
            } else if (jedisCommander instanceof ShardedJedis) {
                ((ShardedJedis) jedisCommander).close();
            }
            /**
             else if (jedisCommander instanceof JedisCluster) {
             // 集群模式下, 实例相当于是连接池,是不能关闭的
             }
             **/
        } catch (Exception e) {
            throw new JedisException(e);
        }
    }
}
