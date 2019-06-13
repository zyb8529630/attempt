package com.attempt.core.cache.api;

import org.apache.shiro.session.Session;

/**  
* @Description: 操作接口.
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface SessionOperations {
	  /**
     * 删除{@link Session}.
     * 
     */
    void delete();

    /**
     * 修改{@link Session}.
     *
     * @param newSession the new session
     */
    void update(Session newSession);

    /**
     * 获取{@link Session}.
     *
     * @return the session
     */
    Session get();

    /**
     * 续期.
     */
    void renewal();
}
