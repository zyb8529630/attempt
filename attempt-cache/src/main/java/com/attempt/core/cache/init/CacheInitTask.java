package com.attempt.core.cache.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.attempt.core.cache.api.CacheManageService;
import com.attempt.core.cache.models.CacheMetaInfo;
import com.attempt.core.cache.suppore.redis.RedisCommander;
import com.attempt.core.cache.util.CacheUtil;
import com.attempt.core.fastjson.FastJsonInitTask;
import com.attempt.core.fastjson.support.FastJsonParser;
import com.attempt.core.init.InitializingTask;

/**
 * 构建缓存数据初始化任务类.
 * @author zhouyinbin
 * @date 2019年6月5日 下午5:08:32
 *
 */
public class CacheInitTask  extends InitializingTask  {

	/**
	 * 日志管理
	 */
    private static final Logger logger = LoggerFactory.getLogger(CacheInitTask.class);
	
	/**
	 * 作业执行
	 */
	@Override
	public void execute(ApplicationContext context) {
		
        context.getBean(FastJsonInitTask.class).initFastJsonParsers(context.getBeansOfType(FastJsonParser.class));
        context.getBeansOfType(CacheManageService.class).forEach((beanName, cacheManageService) -> {
            if (!cacheManageService.autoInit())
                return;
            CacheMetaInfo metaInfo = cacheManageService.getCacheMetaInfo();
            if (metaInfo == null)
                logger.error("Please make sure the returnValue of the method [CacheManageService#getCacheMetaInfo]" +
                        " of Spring Bean [{}] is not null!", beanName);
            String lockKey = String.format("%s%s", CacheUtil.generateKey(metaInfo), ".lock");
            if (lock(lockKey)) {
                try {
                    cacheManageService.init();
                } catch (Exception e) {
                    logger.error("Spring Bean [" + beanName + "] method init() occur an error!", e);
                }
                release(lockKey);
            }
        });
	}
	
    private boolean lock(String lockKey) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            if (!commander.nativeCommander().exists(lockKey)) {
                commander.nativeCommander().setex(lockKey, 60, "1");
                return true;
            }
            throw new Exception("Can't acquire the lock of " + lockKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }	

    
    private void release(String lockKey) {
        try (RedisCommander commander = CacheUtil.createRedisCommonder()) {
            commander.nativeCommander().del(lockKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
