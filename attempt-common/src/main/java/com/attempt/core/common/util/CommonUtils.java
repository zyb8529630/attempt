package com.attempt.core.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 公共方法类
 * @author zhouyinbin
 * @date 2019年6月5日 下午12:36:17
 *
 */
public interface CommonUtils {

	
	/**
     * 日期格式yyyy-MM-dd HH:mm:ss,SSS
     */
    String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss,SSS";

    /**
     * 日期格式yyyyMMddHHmmssSSS
     */
    String YYYYMMDD_HHMMSS_SSS = "yyyyMMddHHmmssSSS";

    /**
     * 日期格式yyyy-MM-dd HH:mm:ss.SSS
     */
    String YYYY_MM_DD_HH_MM_SS_FF = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 日期格式yyyy-MM-dd HH:mm:ss
     */
    String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    
	/**
	 * 生成UUID（全局唯一） 用于主键生成
	 * @return uuid
	 */
	 static String uuid() {
	        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	 }
	 
	 /**
	  * 将Date 转化为字符串
	  * @param date 
	  * @param dateFormat 指定日期格式
	  * @return 转化后的字符串
	  */
     static String convertDateToStr(Date date, String dateFormat) {
        String result = "";
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        if (date != null) {
            result = df.format(date);
        }
        return result;
    }
     
     /**
      * 将字符串转化为日期
      * @param dateStr
      * @param dateFormat 指定日期格式
      * @return 日期
      */
     static Date convertStrToDate(String dateStr, String dateFormat) {
         Date result = null;
         SimpleDateFormat df = new SimpleDateFormat(dateFormat);
         if (dateStr != null) {
             try {
                 result = df.parse(dateStr);
             } catch (ParseException e) {
                 result = null;
             }
         }
         return result;
     }
     
     /**
      * 将流转换为字符串.
      * @param in 输入流
      * @return 字符串
      * @throws Exception
      */
     static String inputStreamToString(InputStream in) throws Exception {
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         StringBuilder buffer = new StringBuilder();
         String line;
         while ((line = br.readLine()) != null) {
             buffer.append(line);
         }
         return buffer.toString();
     }
     
     /**
      * 对象的深度拷贝
      * @param srcObj
      * @return
      * @throws Exception
      */
     static Object deepClone(Serializable srcObj) throws Exception {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ObjectOutputStream oo = new ObjectOutputStream(out);
         oo.writeObject(srcObj);
         ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
         ObjectInputStream oi = new ObjectInputStream(in);
         return oi.readObject();
     }
}
