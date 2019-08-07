/*
* Copyright Information JiaLing Group
* project ���������Դ�ƻ�����ϵͳ
* Comment ���ڳ��ò�����
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
 * ���ڴ���һЩ���������ڲ���
 */
public class DateUtil {

	/**
	 * ��־����
	 * 
	 */
	private static final Logger logger = Logger.getLogger("���ڳ��ò�����");
	
    /**
     * ���ڸ�ʽ�ַ���: 2006-09-11 23:59:59 
     */
    private static String DATE_FORMAT_24H = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * ���ڸ�ʽ�ַ���: 2006-09-11
     */
    private static String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * �·ݸ�ʽ�ַ�����2006-01
     */
    private static String MONTH_FORMAT = "yyyy-MM";
    
    /**
     * ���ڷָ����
     */
    private static String DATE_SPILT = "-";
    
    /**
     * �õ���ǰ������ʱ��,���ʽΪ��yyyy-MM-dd HH:mm:ss
     * @return String
     *         ��ǰʱ���ʽ���ַ���
     */
    public static String getCurDate24H() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT_24H);
        return df.format(new Date());
    }
    
    /**
     * �õ���ǰ������ʱ��,���ʽ: yyyy-MM-dd
     * @return String
     *         ��ǰʱ���ʽ�ַ���
     */
    public static String getCurDate() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(new Date());
    }
    /**
     * ת���ַ���Ϊ��ȷ�����ڸ�ʽ
     * @param mydate
     * @return
     */
    public static String getCurDate(String mydate) {
    	String format = null;
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		try{
			format = df.format(df.parse(mydate));
		}catch(ParseException pex){
			logger.fatal("�����������ͳ��ִ���");
			logger.fatal(pex.getMessage());
			format = "";
		}
        return format;
    }
    
    /**
     * ȡ�õ�ǰ������ʱ���·ݵĵ�һ��
     * @return ��ǰ�µĵ�һ��
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
            System.out.println("����ʱ����ִ���!");
            firstDay = bf.toString();
        }
        return firstDay;
    }
    
    /**
     * ȡ�õ�ǰ������ʱ���·ݵ����һ��
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
            System.out.println("����ʱ����ִ���!");
            lastDay = bf.toString();
        }
        return lastDay;
    }
    
    /**
     * ��õ�ǰ���·�
     * @return ��ǰ�·�
     */
    public static String getCurrentMonth() {
    	DateFormat df = new SimpleDateFormat(MONTH_FORMAT);
    	return df.format(new Date());
    }
    
    /**
     * ת���ַ���Ϊ��ȷ�������ַ�����ʽ
     * @param mydate
     * @return
     */
    public static String formatDateString(String aString,String dateFormat) {
    	String returnFormat = "0000-00-00 00:00:00";
        DateFormat df = new SimpleDateFormat(dateFormat);
		try{
			returnFormat = df.format(df.parse(aString));
		}catch(ParseException pex){
			logger.fatal("�����������ͳ��ִ���");
			logger.fatal(pex.getMessage());
		}
        return returnFormat;
    }
    public static String formatDateString(Date aDate,String dateFormat) {
        DateFormat df = new SimpleDateFormat(dateFormat);
        return df.format(aDate);
    }
}
