package com.attempt.core.cache.suppore.redis.holder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.attempt.core.cache.api.TableOperations;
import com.attempt.core.cache.models.Behavior;
import com.attempt.core.cache.models.CountType;
import com.attempt.core.cache.models.ExpireStrategy;
import com.attempt.core.cache.models.IndexItem;
import com.attempt.core.cache.suppore.DataRefreshSupport;
import com.attempt.core.cache.suppore.TableSupport;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;
import com.attempt.core.common.XOR;

/**  
* @Description: 索引表格缓存单元.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class TableHolder<T extends Serializable> extends DataHolder implements TableOperations<T> {


    /**
     * Table数据支持工具类
     */
    private TableSupport<T> supportTool;

    /**
     * 放入Table中的数据类型
     */
    private Class<T> itemClassType;

    /**
     * 构造方法
     *
     * @param supportTool 对Redis的Table表格存储的支持工具类
     */
    @SuppressWarnings("unchecked")
    public TableHolder(TableSupport<T> supportTool) {
        super(supportTool.getMetaInfo());
        this.supportTool = supportTool;
        this.itemClassType = (Class<T>) getMetaInfo().getItemType();
    }

    /**
     * 删除表格.
     */
    @Override
    public void delete() {
        // 删除元数据
        if (metaInfoHolder != null)
            metaInfoHolder.delete();
        // 删除计数器
        if (counterHolder != null)
            counterHolder.delete();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            // 删除Table记录数据
            commander.nativeCommander().del(supportTool.getTableRecordKey());
            // 删除Table索引数据
            commander.delBatch(commander.smembers(supportTool.getTableIndexKey()).stream());
            // 删除Table索引表
            commander.nativeCommander().del(supportTool.getTableIndexKey());
        }
    }

    /**
     * 删除表格中特定的单元
     *
     * @param item 待删除数据单元
     */
    @Override
    public void delete(T item) {
        Assert.notNull(item);
        String pk = supportTool.generatePrimaryKey(item);

        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            /** 如果键值不存在，则退出**/
            if (!commander.nativeCommander().hexists(supportTool.getTableRecordKey(), pk))
                return;
            /** 计数器计数**/
            if (counterHolder != null)
                counterHolder.increase(CountType.WRITE_COUNT);
            /** 判断是否需要续期**/
            if (DataRefreshSupport.needRenewalOn(this, Behavior.SET))
                renewal();
            /** 判断是否需要删除**/
            if (DataRefreshSupport.needDeleteOn(this, Behavior.SET)) {
                delete();
                return;
            }
            // 删除Table记录
            commander.nativeCommander().hdel(supportTool.getTableRecordKey(), pk);
            // 删除Table索引
            commander.sremBatch(supportTool.buildIndexData(item).collect(Collectors.toMap(index -> index, index -> pk)));

            /**
             * 目前该方法在调用后，未clearup索引信息。如若需要则执行：
             * commander.indexTableClearup(supportTool.getTableIndexKey());
             */
        }
    }

    /**
     * 根据索引，获取数据列表.
     * 当{@code indexItems}只有一个值时，{@code xor}该参数不起效
     *
     * @param xor        逻辑操作符（并，或）
     * @param indexItems 索引单元
     * @return 数据列表.
     */
    @Override
    public List<T> get(XOR xor, IndexItem... indexItems) {
        Assert.notNull(xor);
        Assert.notEmpty(indexItems);
        Assert.isTrue(supportTool.validateIndexKeys(indexItems));

        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();

        List<T> result = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            if (indexItems.length == 1) {
                // 生成索引Key
                String indexKey = supportTool.generateIndexKey(indexItems[0]);
                // 找到记录Key集合
                List<String> recordKeys = commander.smembers(indexKey);
                /**
                 * 找到数据集。如果元素实现了{@link Comparable}接口，则调用sorted做排序
                 */
                if (Comparable.class.isAssignableFrom(itemClassType))
                    return commander.nativeCommander().hmget(supportTool.getTableRecordKey(),
                            recordKeys.toArray(new String[0])).stream()
                            .map(value -> JSON.parseObject(value, itemClassType))
                            .sorted()
                            .collect(Collectors.toList());
                return commander.nativeCommander().hmget(supportTool.getTableRecordKey(),
                        recordKeys.toArray(new String[0])).stream()
                        .map(value -> JSON.parseObject(value, itemClassType))
                        .collect(Collectors.toList());
            }
            // 生成索引Key集合
            String[] indexKeys = Arrays.stream(indexItems)
                    .map(supportTool::generateIndexKey).toArray(String[]::new);
            String[] recordKeys;
            // 根据操作符的不同，找到记录Key集合
            if (xor == XOR.AND)
                recordKeys = commander.sinter(indexKeys).toArray(String[]::new);
            else
                recordKeys = commander.sunion(indexKeys).toArray(String[]::new);
            /**
             * 找到数据集。如果元素实现了{@link Comparable}接口，则调用sorted做排序
             */
            if (Comparable.class.isAssignableFrom(itemClassType))
                result = commander.nativeCommander().hmget(supportTool.getTableRecordKey(), recordKeys).stream()
                        .map(value -> JSON.parseObject(value, itemClassType))
                        .sorted()
                        .collect(Collectors.toList());
            else
                result = commander.nativeCommander().hmget(supportTool.getTableRecordKey(), recordKeys).stream()
                        .map(value -> JSON.parseObject(value, itemClassType))
                        .collect(Collectors.toList());
        }
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 根据主键信息获取唯一的数据单元.
     * 生成{@code primaryKey}的方法：
     * <p>
     * 1. 使用{@link TableSupport#generatePrimaryKey(Serializable)}生成
     * 2. 将所有在数据类型的{@link CacheTable#primaryKey()}对应的属性值，按照顺序排列，以“^”分割
     *
     * @param primaryKey 主键信息
     * @return 查询到的唯一数据元.
     */
    @Override
    public T get(String primaryKey) {
        Assert.isTrue(StringUtils.isNotEmpty(primaryKey));
        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.READ_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET))
            renewal();
        // 做查询
        T result = null;
        String value = null;
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            value = commander.nativeCommander().hget(supportTool.getTableRecordKey(), primaryKey);
        }
        if (StringUtils.isNotEmpty(value))
            result = JSON.parseObject(value, itemClassType);
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.GET))
            delete();
        return result;
    }

    /**
     * 向表格中添加数据单元
     *
     * @param item 数据单元
     */
    @Override
    public void add(T item) {
        Assert.notNull(item);
        String pk = supportTool.generatePrimaryKey(item);
        /** 断言主键不存在**/
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            Assert.isTrue(!commander.nativeCommander().hexists(supportTool.getTableRecordKey(), pk), pk + " 存在，无法再次添加");
            /** 计数器计数**/
            if (counterHolder != null)
                counterHolder.increase(CountType.WRITE_COUNT);
            /** 判断是否需要续期**/
            if (DataRefreshSupport.needRenewalOn(this, Behavior.SET))
                renewal();
            /** 判断是否需要删除**/
            if (DataRefreshSupport.needDeleteOn(this, Behavior.SET)) {
                delete();
                return;
            }
            /** 保存表格记录数据**/
            commander.nativeCommander().hset(supportTool.getTableRecordKey(), pk, JSON.toJSONString(item));
            /** 保存表格索引数据**/
            String[] indexData = supportTool.buildIndexData(item).toArray(String[]::new);
            /** 向索引表中添加信息**/
            commander.nativeCommander().sadd(supportTool.getTableIndexKey(), indexData);
            /** 添加索引信息**/
            commander.ssetBatch(Arrays.asList(indexData).stream().collect(Collectors.toMap(index -> index, index -> Collections.singletonList(pk))));
            /** 如果存在超期策略，则需要设置**/
            if (getMetaInfo().getExpireConfig().getExpireStrategy() == ExpireStrategy.EXPIRE_ON_TIME_LIMIT)
                commander.pexpireBatch(Arrays.asList(indexData).stream(), getMetaInfo().getExpireConfig().getTimeLimit());
        }
    }

    /**
     * 添加或更新某一个数据单元
     *
     * @param item 数据单元
     */
    @Override
    public void set(T item) {
        Assert.notNull(item);
        String pk = supportTool.generatePrimaryKey(item);

        /** 计数器计数**/
        if (counterHolder != null)
            counterHolder.increase(CountType.WRITE_COUNT);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.SET))
            renewal();
        /** 判断是否需要删除**/
        if (DataRefreshSupport.needDeleteOn(this, Behavior.SET)) {
            delete();
            return;
        }
        /** 如果主键不存在，则走add逻辑**/
        T orginItem = get(pk);
        if (orginItem == null) {
            add(item);
            return;
        }
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            /** 删除索引信息**/
            commander.sremBatch(supportTool.buildIndexData(orginItem)
                    .collect(Collectors.toMap(index -> index, index -> pk)));
            /** 保存表格记录数据**/
            commander.nativeCommander().hset(supportTool.getTableRecordKey(), pk, JSON.toJSONString(item));
            /** 构建表格索引数据**/
            String[] indexData = supportTool.buildIndexData(item).toArray(String[]::new);
            /** 向索引表中添加信息**/
            commander.nativeCommander().sadd(supportTool.getTableIndexKey(), indexData);
            /** 添加索引信息**/
            commander.ssetBatch(Arrays.asList(indexData).stream().collect(Collectors.toMap(index -> index, index -> Collections.singletonList(pk))));
            /** 如果存在超期策略，则需要设置**/
            if (getMetaInfo().getExpireConfig().getExpireStrategy() == ExpireStrategy.EXPIRE_ON_TIME_LIMIT)
                commander.pexpireBatch(Arrays.asList(indexData).stream(), getMetaInfo().getExpireConfig().getTimeLimit());
            /**
             * 目前该方法在调用后，未clearup索引信息。如若需要则执行：
             * commander.indexTableClearup(supportTool.getTableIndexKey());
             */
        }
    }

    /**
     * 根据索引单元，计算缓存数据单元数量.
     * 当{@code indexItems}只有一个值时，{@code xor}该参数不起效
     *
     * @param xor        逻辑操作符（并，或）
     * @param indexItems 索引单元
     */
    @Override
    public long count(XOR xor, IndexItem... indexItems) {
        Assert.notNull(xor);
        Assert.notEmpty(indexItems);
        Assert.isTrue(supportTool.validateIndexKeys(indexItems));
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.SET))
            renewal();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            /** 如果只有一个查询条件，直接统计即可**/
            if (indexItems.length == 1) {
                return commander.nativeCommander().scard(supportTool.generateIndexKey(indexItems[0]));
            }
            // 先要根据查询条件生成带检索的索引Key信息
            String[] indexKeys = Arrays.asList(indexItems).stream()
                    .map(supportTool::generateIndexKey).toArray(String[]::new);
            if (xor == XOR.AND)
                return commander.sinter(indexKeys).count();
            else
                return commander.sunion(indexKeys).count();
        }
    }

    /**
     * 根据{@code item}中的{@code primaryKeys}，在缓存中查询是否存在该记录
     *
     * @param item 待查询的数据单元
     */
    @Override
    public boolean contains(T item) {
        Assert.notNull(item);
        /** 判断是否需要续期**/
        if (DataRefreshSupport.needRenewalOn(this, Behavior.GET)) {
            renewal();
        }
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            return commander.nativeCommander().hexists(supportTool.getTableRecordKey(), supportTool.generatePrimaryKey(item));
        }
    }

    /**
     * 续期.
     *
     */
    @Override
    public void renewal() {
        // 如果过期策略不是按照时间策略的，则不需要续期
        if (getMetaInfo().getExpireConfig().getExpireStrategy() != ExpireStrategy.EXPIRE_ON_TIME_LIMIT)
            return;
        // 如果续期次数超过限定值，则不再续期
        if (getMetaInfo().getExpireConfig().getRenewnalCountLimit() != -1 && getMetaInfo().getExpireConfig().getRenewnalCountLimit() < renewalCount())
            return;
        long milliseconds = super.getMetaInfo().getExpireConfig().getTimeLimit();
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            // 续期Table数据记录
            commander.nativeCommander().pexpire(supportTool.getTableRecordKey(), milliseconds);
            // 续期Table索引表
            commander.nativeCommander().pexpire(supportTool.getTableIndexKey(), milliseconds);
            // 续期Table索引数据
            commander.pexpireBatch(commander.smembers(supportTool.getTableIndexKey()).stream(), milliseconds);
        }

        if (counterHolder != null) {
            counterHolder.renewal(milliseconds);
            counterHolder.increase(CountType.RENEWAL_COUNT);
        }
        if (metaInfoHolder != null) {
            metaInfoHolder.renewal(milliseconds);
        }
    }

    /**
     * 获取当前数据元类型的支持工具
     *
     * @return 当前数据元类型的支持工具
     */
    public TableSupport<T> getSupportTool() {
        return supportTool;
    }
}
