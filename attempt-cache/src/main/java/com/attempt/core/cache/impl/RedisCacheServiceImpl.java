package com.attempt.core.cache.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.shiro.session.Session;

import com.attempt.core.cache.api.CacheService;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.suppore.redis.holder.ListHolder;
import com.attempt.core.cache.suppore.redis.holder.MapHolder;
import com.attempt.core.cache.suppore.redis.holder.MutexHolder;
import com.attempt.core.cache.suppore.redis.holder.RankHolder;
import com.attempt.core.cache.suppore.redis.holder.SessionHolder;
import com.attempt.core.cache.suppore.redis.holder.TableHolder;

/**
 * Redis缓存服务的实现.
 * @author zhouyinbin
 * @date 2019年6月5日 下午3:51:27
 *
 */
public class RedisCacheServiceImpl implements CacheService{
 
	@Override
	public boolean exist(CacheMetaInfo metaInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MutexHolder createMutex(CacheMetaInfo metaInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MutexHolder createMutex(CacheMetaInfo metaInfo, String initValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MutexHolder getMutex(String owner, String mutexName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MutexHolder getMutex(String mutexName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionHolder createSession(Session session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionHolder getSession(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> ListHolder<T> createList(CacheMetaInfo metaInfo, List<T> listData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> ListHolder<T> getList(String owner, String listName, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> ListHolder<T> getList(String listName, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> MapHolder<T> createMap(CacheMetaInfo metaInfo, Map<String, T> mapData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> MapHolder<T> getMap(String owner, String mapName, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> MapHolder<T> getMap(String mapName, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> RankHolder<T> createRank(CacheMetaInfo metaInfo, Map<T, Double> rankData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> RankHolder<T> getRank(String owner, String rankName, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> RankHolder<T> getRank(String rankName, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> TableHolder<T> createTable(CacheMetaInfo metaInfo, List<T> tableData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> TableHolder<T> getTable(String owner, Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> TableHolder<T> getTable(Class<T> itemClassType) {
		// TODO Auto-generated method stub
		return null;
	}

}
