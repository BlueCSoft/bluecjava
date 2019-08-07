/*
* Copyright Information JiaLing Group
* project 嘉陵分销资源计划管理系统
* Comment 日期常用操作类
* JDK Version 1.4.2
* Created on Sep 12, 2006
* version 1.0
* Modify history
*/
 
package bluec.base.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

/**
 * @author Techie lg_techie@163.com
 * @version 1.0
 * 用于处理一些常见的日期操作
 */
public class DateUtil {

	/**
	 * 日志对象
	 * 
	 */
	private static final Logger logger = Logger.getLogger("日期常用操作类");
	
    /**
     * 日期格式字符串: 2006-09-11 23:59:59 
     */
    private static String DATE_FORMAT_24H = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期格式字符串: 2006-09-11
     */
    private static String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 月份格式字符串：2006-01
     */
    private static String MONTH_FORMAT = "yyyy-MM";
    
    /**
     * 日期分割符号
     */
    private static String DATE_SPILT = "-";
    
    /**
     * 得到当前服务器时间,其格式为：yyyy-MM-dd HH:mm:ss
     * @return String
     *         当前时间格式的字符串
     */
    public static String getCurDate24H() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT_24H);
        return df.format(new Date());
    }
    
    /**
     * 得到当前服务器时间,其格式: yyyy-MM-dd
     * @return String
     *         当前时间格式字符串
     */
    public static String getCurDate() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(new Date());
    }
    /**
     * 转换字符串为正确的日期格式
     * @param mydate
     * @return
     */
    public static String getCurDate(String mydate) {
    	String format = null;
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		try{
			format = df.format(df.parse(mydate));
		}catch(ParseException pex){
			logger.fatal("处理日期类型出现错误");
			logger.fatal(pex.getMessage());
			format = "";
		}
        return format;
    }
    
    /**
     * 取得当前服务器时间月份的第一天
     * @return 当前月的第一天
     */
    public static String getFirstDayOfMonth() {
        String firstDay = null;
        Calendar calendar = new GregorianCalendar();
        int firstDayOfMonth = 
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int currectMonth = calendar.get(Calendar.MONTH) + 1;
        int currectYear = calendar.get(Calendar.YEAR);
        StringBuffer bf = new StringBuffer();
        bf.append(currectYear).append(DATE_SPILT).append(currectMonth)
                .append(DATE_SPILT).append(firstDayOfMonth);
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {            
            firstDay = df.format(df.parse(bf.toString()));
        } 
        catch (ParseException pe) {
            System.out.println("解析时间出现错误!");
            firstDay = bf.toString();
        }
        return firstDay;
    }
    
    /**
     * 取得当前服务器时间月份的最后一天
     * @return
     */
    public static String getLastDayOfMonth() {
        String lastDay = null;
        Calendar calendar = new GregorianCalendar();
        int lastDayOfMonth = 
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currectMonth = calendar.get(Calendar.MONTH) + 1;
        int currectYear = calendar.get(Calendar.YEAR);
        StringBuffer bf = new StringBuffer();
        bf.append(currectYear).append(DATE_SPILT).append(currectMonth)
                .append(DATE_SPILT).append(lastDayOfMonth);
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {
            lastDay = df.format(df.parse(bf.toString()));
        }
        catch (ParseException pe) {
            System.out.println("解析时间出现错误!");
            lastDay = bf.toString();
        }
        return lastDay;
    }
    
    /**
     * 获得当前的月份
     * @return 当前月份
     */
    public static String getCurrentMonth() {
    	DateFormat df = new SimpleDateFormat(MONTH_FORMAT);
    	return df.format(new Date());
    }
    
    /**
     * 转换字符串为正确的日期字符串格式
     * @param mydate
     * @return
     */
    public static String formatDateString(String aString,String dateFormat) {
    	String returnFormat = "0000-00-00 00:00:00";
        DateFormat df = new SimpleDateFormat(dateFormat);
		try{
			returnFormat = df.format(df.parse(aString));
		}catch(ParseException pex){
			logger.fatal("处理日期类型出现错误");
			logger.fatal(pex.getMessage());
		}
        return returnFormat;
    }
    public static String formatDateString(Date aDate,String dateFormat) {
        DateFormat df = new SimpleDateFormat(dateFormat);
        return df.format(aDate);
    }
}
