package com.attempt.core.cache.api;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.shiro.session.Session;

import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.suppore.redis.holder.ListHolder;
import com.attempt.core.cache.suppore.redis.holder.MapHolder;
import com.attempt.core.cache.suppore.redis.holder.MutexHolder;
import com.attempt.core.cache.suppore.redis.holder.RankHolder;
import com.attempt.core.cache.suppore.redis.holder.SessionHolder;
import com.attempt.core.cache.suppore.redis.holder.TableHolder;
/**  
* @Description: 缓存服务接口.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface CacheService {
	   /**
     * 验证指定的缓存资源是否存在.
     *
     * @param metaInfo 待查询的元数据，会忽略对 {@link CacheMetaInfo#expireConfig}的验证
     * @return 是否存在 boolean
     */
    boolean exist(CacheMetaInfo metaInfo);

    /**
     * 创建信号量缓存
     *
     * @param metaInfo 缓存数据单元的元数据信息
     * @return 持有信号量的 {@link MutexHolder}实例
     */
    MutexHolder createMutex(CacheMetaInfo metaInfo);

    /**
     * 创建信号量缓存
     *
     * @param metaInfo  缓存数据单元的元数据信息
     * @param initValue 初始值
     * @return {@link MutexHolder}实例
     */
    MutexHolder createMutex(CacheMetaInfo metaInfo, String initValue);

    /**
     * 根据 mutexName，获取指定应用所持有的特定名称的{@link MutexHolder}.
     *
     * @param owner     数据单元拥有者 @see {@code cache.properties#cache.name}
     * @param mutexName 信号量名称
     * @return {@link MutexHolder}实例
     */
    MutexHolder getMutex(String owner, String mutexName);

    /**
     * 根据 mutexName，获取当前应用所持有的特定名称的{@link MutexHolder}.
     *
     * @param mutexName 信号量名称
     * @return 持有信号量的 {@link MutexHolder}实例
     */
    MutexHolder getMutex(String mutexName);
    /**
     * 创建{@link Session}会话缓存
     *
     * @param session 待缓存数据{@link Session}
     * @return {@link Session}的{@link SessionHolder}实例
     */
    SessionHolder createSession(Session session);

    /**
     * 根据 sessionId，获取当前应用所持有的特定名称的{@link SessionHolder}.
     *
     * @param sessionId {@link Session#getId()}
     * @return {@link Session}的{@link SessionHolder}实例
     */
    SessionHolder getSession(String sessionId);

    /**
     * 创建无重复列表缓存
     *
     * @param <T>      集合中的元素类型
     * @param metaInfo 缓存数据单元的元数据信息
     * @param listData 无重复的列表数据
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> ListHolder<T> createList(CacheMetaInfo metaInfo, List<T> listData);

    /**
     * 根据 listName，获取指定应用所持有的特定名称的{@link ListHolder}.
     *
     * @param <T>           集合中的元素类型
     * @param owner         数据单元拥有者 @see {@code cache.properties#cache.name}
     * @param listName      列表名称
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> ListHolder<T> getList(String owner, String listName, Class<T> itemClassType);

    /**
     * 根据 listName，获取当前应用所持有的特定名称的{@link ListHolder}.
     *
     * @param <T>           集合中的元素类型
     * @param listName      列表名称
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> ListHolder<T> getList(String listName, Class<T> itemClassType);

    /**
     * 创建Map缓存
     *
     * @param <T>      集合中的元素类型
     * @param metaInfo 缓存数据单元的元数据信息
     * @param mapData  map数据
     * @return {@link T}的{@link MapHolder}实例
     */
    <T extends Serializable> MapHolder<T> createMap(CacheMetaInfo metaInfo, Map<String, T> mapData);

    /**
     * 根据 owner, mapName，获取指定应用所持有的特定名称的{@link MapHolder}.
     *
     * @param <T>           集合中的元素类型
     * @param owner         数据单元拥有者 @see {@code cache.properties#cache.name}
     * @param mapName       Map名称
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link MapHolder}实例
     */
    <T extends Serializable> MapHolder<T> getMap(String owner, String mapName, Class<T> itemClassType);

    /**
     * 根据 mapName，获取当前应用所持有的特定名称的{@link MapHolder}.
     *
     * @param <T>           集合中的元素类型
     * @param mapName       Map名称
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link MapHolder}实例
     */
    <T extends Serializable> MapHolder<T> getMap(String mapName, Class<T> itemClassType);

    /**
     * 创建有序排名缓存
     *
     * @param <T>      集合中的元素类型
     * @param metaInfo 缓存数据单元的元数据信息
     * @param rankData 每个元素的排名数据
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> RankHolder<T> createRank(CacheMetaInfo metaInfo, Map<T, Double> rankData);

    /**
     * 根据 rankName，获取指定应用所持有的特定名称的{@link RankHolder}.
     *
     * @param <T>           集合中的元素类型
     * @param owner         数据单元拥有者 @see {@code cache.properties#cache.name}
     * @param rankName      rank名称
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> RankHolder<T> getRank(String owner, String rankName, Class<T> itemClassType);

    /**
     * 根据 rankName，获取当前应用所持有的特定名称的{@link RankHolder}.
     *
     * @param <T>           集合中的元素类型
     * @param rankName      rank名称
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> RankHolder<T> getRank(String rankName, Class<T> itemClassType);

    /**
     * 创建表格缓存
     *
     * @param <T>       集合中的元素类型
     * @param metaInfo  缓存数据单元的元数据信息
     * @param tableData 表格数据
     * @return {@link T}的{@link TableHolder}实例
     */
    <T extends Serializable> TableHolder<T> createTable(CacheMetaInfo metaInfo, List<T> tableData);

    /**
     * 根据 itemClassType信息，获取指定应用所持有的特定名称的{@link TableHolder}.
     *
     * @param <T>           Table中存放的数据类型
     * @param owner         缓存拥有者
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> TableHolder<T> getTable(String owner, Class<T> itemClassType);

    /**
     * 根据 itemClassType信息，获取指定应用所持有的特定名称的{@link TableHolder}.
     *
     * @param <T>           Table中存放的数据类型
     * @param itemClassType 集合中的元素类信息
     * @return {@link T}的{@link ListHolder}实例
     */
    <T extends Serializable> TableHolder<T> getTable(Class<T> itemClassType);

}
