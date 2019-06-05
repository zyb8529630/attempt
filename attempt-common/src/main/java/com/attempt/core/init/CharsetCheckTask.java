package com.attempt.core.init;

import org.springframework.context.ApplicationContext;

import com.attempt.core.common.util.PropertiesUtils;

/**
 * 对JVM字符集的检查.
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:52:04
 *
 */
public class CharsetCheckTask extends InitializingTask {

	/**
	 * 执行作业
	 */
	@Override
	public void execute(ApplicationContext applicationContext) {
		String systemEncoding = System.getProperty("file.encoding");
        String requireEncoding = PropertiesUtils.getProperty("system.encoding");
        if (!systemEncoding.equals(requireEncoding))
            throw new RuntimeException("The JVM charset must be " + requireEncoding);
		
	}

}
