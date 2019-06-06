package com.attempt.core.cache.suppore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.attempt.core.cache.annotation.CacheTable;
import com.attempt.core.cache.models.CacheDataType;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.models.IndexItem;
import com.attempt.core.cache.util.CacheUtil;

/**
 * 对Redis的Table表格存储的支持工具类.
 * @author zhouyinbin
 * @date 2019年6月6日 下午4:59:14
 *
 */
public class TableSupport <T extends Serializable> {

	 /**
     * 日志管理
     */
    private static Logger logger = LoggerFactory.getLogger(TableSupport.class);

    /**
     * 缓存数据元数据
     */
    private CacheMetaInfo metaInfo;

    /**
     * 声明的索引信息
     */
    private String[] indexArray;

    /**
     * 声明的主键信息
     */
    private String[] primaryKeyArray;

    /**
     * 表格数据存储的Key
     */
    private String tableRecordKey;

    /**
     * 表格数据索引Key的Key
     */
    private String tableIndexKey;

    /**
     * 索引存储的Key前缀
     */
    private String indexRecordKeyPrefix;

    /**
     * 构造方法
     *
     * @param metaInfo 缓存数据的元数据。要求包含{@link CacheMetaInfo#getItemType()}
     */
    public TableSupport(CacheMetaInfo metaInfo) {
        Assert.notNull(metaInfo);
        Assert.notNull(metaInfo.getItemType());
        this.metaInfo = metaInfo;
        CacheTable cacheTableAnn = metaInfo.getItemType().getAnnotation(CacheTable.class);
        Assert.notNull(cacheTableAnn);
        // 检索声明的索引信息
        indexArray = cacheTableAnn.index();
        Assert.notEmpty(indexArray);
        // 检索主键信息
        primaryKeyArray = cacheTableAnn.primaryKey();
        Assert.notEmpty(primaryKeyArray);
        // 生成Table在缓存中的记录key前缀
        tableRecordKey = CacheUtil.generateKey(CacheDataType.TABLE, metaInfo.getOwner(),
                String.format("%s%s%s", metaInfo.getName(), CacheUtil.SPLITFLAG_PROPERTY, CacheUtil.TABLE_RECORD));
        // 生成表格数据索引Key的Key
        tableIndexKey = CacheUtil.generateKey(CacheDataType.TABLE, metaInfo.getOwner(),
                String.format("%s%s%s", metaInfo.getName(), CacheUtil.SPLITFLAG_PROPERTY, CacheUtil.TABLE_INDEX));
        // 生成Table在缓存中的索引key前缀
        indexRecordKeyPrefix = tableIndexKey;

    }

    /**
     * 检查索引信息是否包含于Java类上声明的全部索引名称
     *
     * @param indexItems 索引信息
     * @return 是否能够匹配
     */
    public boolean validateIndexKeys(IndexItem... indexItems) {
        for (IndexItem item : indexItems) {
            if (!ArrayUtils.contains(indexArray, item.getName()))
                return false;
        }
        return true;
    }

    /**
     * 用索引信息构建在redis中的索引Key
     *
     * @param indexItem 索引信息
     * @return 返回在redis中的索引Key
     */
    public String generateIndexKey(IndexItem indexItem) {
        return String.format("%s%s%s%s%s", indexRecordKeyPrefix, CacheUtil.SPLITFLAG_PROPERTY,
                indexItem.getName().toUpperCase(), CacheUtil.SPLITFLAG_PROPERTY, indexItem.getValue());
    }

    /**
     * 从数据单元中抽取主键信息和数据，构建主键
     *
     * @param item 待缓存的数据单元
     * @return 返回以“^”分割的多键值主键信息

     */
    public String generatePrimaryKey(T item) {
        return Arrays.asList(primaryKeyArray).stream().map(pkName -> {
            try {
                return BeanUtils.getProperty(item, pkName);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return "";
            }
        }).collect(Collectors.joining("^"));
    }

    /**
     * 构建单个数据单元在缓存的索引信息
     *
     * @param item 待缓存数据单元
     * @return {@link Stream<String>}
     */
    public Stream<String> buildIndexData(T item) {
        return Arrays.asList(indexArray).stream().map(indexName -> {
            try {
                // 索引的RedisKey
                return String.format("%s%s%s%s%s", indexRecordKeyPrefix, CacheUtil.SPLITFLAG_PROPERTY,
                        indexName.toUpperCase(), CacheUtil.SPLITFLAG_PROPERTY, BeanUtils.getProperty(item, indexName));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }).filter(indexKey -> indexKey != null);
    }

    /**
     * 构建数据集在缓存中的Table数据
     * 主键作为Key，每条记录以Redis Map存储
     *
     * @param tableData 待缓存的数据集
     * @return 按照主键策略生成的Map
     */
    public Map<String, T> buildRecordData(List<T> tableData) {
        return tableData.stream().collect(Collectors.toMap(this::generatePrimaryKey, item -> item));
    }

    /**
     * 构建数据集在缓存的索引数据
     * 索引作为Key，主键信息以Redis Set存储
     *
     * @param tableData 待缓存的数据集
     * @return 按照索引信息排列的数据
     */
    public Map<String, List<String>> buildIndexData(List<T> tableData) {
        Map<String, List<String>> cacheTableData = new HashMap<>();
        for (T item : tableData) {
            // 主键！主键！
            String primaryKey = generatePrimaryKey(item);
            // 来一次stream形式的集合处理过程, 把索引的RedisKey生成出来
            Arrays.asList(indexArray).stream().map(indexName -> {
                try {
                    // 索引的RedisKey
                    return String.format("%s%s%s%s%s", indexRecordKeyPrefix, CacheUtil.SPLITFLAG_PROPERTY,
                            indexName.toUpperCase(), CacheUtil.SPLITFLAG_PROPERTY, BeanUtils.getProperty(item, indexName));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return null;
                }
            }).filter(indexKey -> indexKey != null).forEach(indexKey -> {
                // 如果map中的key不存在，则创建之
                if (!cacheTableData.containsKey(indexKey))
                    cacheTableData.put(indexKey, new ArrayList<>());
                // 塞进去...
                cacheTableData.get(indexKey).add(primaryKey);
            });
        }
        return cacheTableData;
    }

    /**
     * 缓存表格数据的元数据
     *
     * @return 缓存表格数据的元数据
     */
    public CacheMetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
     * 表格数据存储的Key
     *
     * @return 表格数据存储的Key

     */
    public String getTableRecordKey() {
        return tableRecordKey;
    }

    /**
     * 表格数据索引Key的Key
     *
     * @return 表格数据索引Key的Key
     */
    public String getTableIndexKey() {
        return tableIndexKey;
    }

}
