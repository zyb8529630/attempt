package com.attempt.core.cache.suppore.redis.holder;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.attempt.core.cache.api.SessionOperations;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;
import com.attempt.core.common.util.CommonUtils;

/**  
* @Description: 
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public class SessionHolder  extends DataHolder implements SessionOperations {
    /**
     * 用于存储当前Session的权限算法集合的Key
     */
    private static final String PRINCIPALS_REALM_NAMES_FLAG = DefaultSubjectContext.PRINCIPALS_SESSION_KEY + ":REALM_NAMES";
    /**
     * 当前Session的权限算法集合的检索Key
     */
    private static final String PRINCIPALS_REALM_NAMES_FORMAT = DefaultSubjectContext.PRINCIPALS_SESSION_KEY + ":REALM_NAMES:%s";
    /**
    * 构造方法.
    *
    * @param metaInfo the meta info
    */
   public SessionHolder(CacheMetaInfo metaInfo) {
       super(metaInfo);
   }

   /**
    * 删除{@link Session}.
 * @throws Exception 
    *
    */
   @Override
   public void delete()  {
       try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
           commander.nativeCommander().del(key);
       } catch (Exception e) {
		e.printStackTrace();
	}
   }

   /**
    * 修改{@link Session}.
    *
    * @param newSession the new session
    */
   @Override
   public void update(Session newSession) {

       Assert.notNull(newSession);
       Assert.isTrue(getMetaInfo().getName().equals(newSession.getId().toString()));
       try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
           commander.nativeCommander().hmset(key, serialize(newSession));
       } catch (Exception e) {
		e.printStackTrace();
	}
       renewal();

   }

   /**
    * 获取{@link Session}.
    *
    * @return the session
    */
   @Override
   public Session get() {
       renewal();
       try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
           return deserialize(commander.hgetall(key).stream()
                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
       } catch (Exception e) {
		e.printStackTrace();
	}
       return null;
   }

   /**
    * 续期.
    *
    */
   @Override
   public void renewal() {
       try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
           commander.nativeCommander().pexpire(key, getMetaInfo().getExpireConfig().getTimeLimit());
       } catch (Exception e) {
		e.printStackTrace();
	}
   }

   /**
    * @return
    * @deprecated
    */
   @Deprecated
   @Override
   public long renewalCount() {
       throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
   }

   /**
    * @return
    * @deprecated
    */
   @Deprecated
   @Override
   public long readCount() {
       throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
   }

   /**
    * @return
    * @deprecated
    */
   @Deprecated
   @Override
   public long writeCount() {
       throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
   }

   /**
    * 剩余时间
    *
    * @return 剩余时间
    * @deprecated
    */
   @Deprecated
   @Override
   public long remainTime() {
       throw new UnsupportedOperationException("CacheDataType.SESSION doesn't support Counter!");
   }

   /**
    * 序列化{@link Session}.
    *
    * @param session the session
    * @return the map
    */
   public static Map<String, String> serialize(Session session) {

       Map<String, String> sessionData = session.getAttributeKeys().stream()
               .filter(key -> !DefaultSubjectContext.PRINCIPALS_SESSION_KEY.equals(key))
               .collect(Collectors.toMap(Object::toString, k -> session.getAttribute(k).toString()));
       /**
        * 为{@link SimplePrincipalCollection}做序列化操作
        */
       if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) != null) {
           SimplePrincipalCollection principalCollection = (SimplePrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
           principalCollection.getRealmNames().forEach(realmName -> sessionData.put(String.format(PRINCIPALS_REALM_NAMES_FORMAT, realmName),
                   JSON.toJSONString(principalCollection.fromRealm(realmName), SerializerFeature.WriteClassName)));
           sessionData.put(PRINCIPALS_REALM_NAMES_FLAG, JSON.toJSONString(principalCollection.getRealmNames(), SerializerFeature.WriteClassName));
       }
       sessionData.put("_HOST", session.getHost());
       sessionData.put("_ID", session.getId().toString());
       sessionData.put("_LAST_ACCESS_TIME", CommonUtils.convertDateToStr(new Date(), CommonUtils.YYYY_MM_DD_HH_MM_SS_SSS));
       sessionData.put("_START_TIMESTAMP", CommonUtils.convertDateToStr(session.getStartTimestamp(), CommonUtils.YYYY_MM_DD_HH_MM_SS_SSS));
       sessionData.put("_TIMEOUT", String.valueOf(session.getTimeout()));
       return sessionData;
   }

   /**
    * 反序列化至{@link Session}.
    *
    * @param sessionData the session data
    * @return the session
    */
   private static Session deserialize(Map<String, String> sessionData) {

       SimpleSession session = new SimpleSession();
       session.setId(sessionData.remove("_ID"));
       session.setHost(sessionData.remove("_HOST"));
       session.setLastAccessTime(new Date());
       sessionData.remove("_LAST_ACCESS_TIME");
       session.setStartTimestamp(CommonUtils.convertStrToDate(sessionData.remove("_START_TIMESTAMP"), CommonUtils.YYYY_MM_DD_HH_MM_SS_SSS));
       session.setTimeout(Long.parseLong(sessionData.remove("_TIMEOUT")));
       /**
        * 对{@link DefaultSubjectContext.AUTHENTICATED_SESSION_KEY}的值单独处理
        */
       if (sessionData.containsKey(DefaultSubjectContext.AUTHENTICATED_SESSION_KEY))
           session.setAttribute(DefaultSubjectContext.AUTHENTICATED_SESSION_KEY,
                   Boolean.parseBoolean(sessionData.remove(DefaultSubjectContext.AUTHENTICATED_SESSION_KEY)));
       /**
        * 为{@link SimplePrincipalCollection}做反序列化操作
        */
       if (sessionData.containsKey(PRINCIPALS_REALM_NAMES_FLAG)) {
           SimplePrincipalCollection principalCollection = new SimplePrincipalCollection();
           JSON.parseArray(sessionData.remove(PRINCIPALS_REALM_NAMES_FLAG))
                   .forEach(realmName -> principalCollection.addAll(
                           JSON.parseArray(sessionData.remove(String.format(PRINCIPALS_REALM_NAMES_FORMAT, realmName.toString()))),
                           realmName.toString()));
           session.setAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY, principalCollection);
       }
       sessionData.forEach((k, v) -> session.setAttribute(k, v));
       return session;
   }
}
