package com.attempt.core.init;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 *
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:52:37
 *
 */
public abstract class InitializingTask implements ApplicationListener<ContextRefreshedEvent> {

	
	/**
     * 实现{@link ApplicationListener#onApplicationEvent(ApplicationEvent)}方法.
     * <p>
     * 只拦截根{@link ApplicationContext}的容器
     */
	@Override
	public final void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
            execute(event.getApplicationContext());
        }
		
	}

	/**
	 * 执行作业
	 * @param applicationContext
	 */
	public abstract void execute(ApplicationContext applicationContext); 

}
