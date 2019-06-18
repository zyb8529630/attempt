package com.attempt.core.cache.api;
/**  
* @Description: 
* @author zhouyinbin  
* @date 
* @version V1.0  
*/
public interface MutexOperations {
	/**
    * 删除.
    * 
    */
   void delete();

   /**
    * 获取值.
    *      
    * @return the string
    */
   String get();

   /**
    * 设置值.
    */
   void set(String value);

   /**
    * Increase by 1.
    *
    */
   void increase();

   /**
    * Increase by step.
    */
   void increase(long step);

   /**
    * Decrease by 1.
    */
   void decrease();

   /**
    * Decrease by step.
    *
    */
   void decrease(long step);

   /**
    * 续期.
    */
   void renewal();
}
