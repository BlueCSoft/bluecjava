/*
* Copyright Information JiaLing Group
* project 嘉陵分销资源计划管理系统
* Comment 数字格式创建类
* JDK Version 1.4.2
* Created on 2006-10-21
* version 1.0
* Modify history
*/

package bluec.base.util;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

/** 
 * 数字格式类
 * @author Techie
 * @version 1.0
 */
public class NumberUtil {
	
	/**
	 * 日志对象 
	 */
	private static Logger logger = Logger.getLogger("数字格式类");
	
	/**
	 * 金额正常格式
	 */
	private static String MONEY_NORMAL_FROMAT = "0.00";
	
	/**
	 * 金额格式
	 */
	private static String MONEY_FORMAT = "#,###,##0.00";
	
	/**
	 * 数量格式
	 */
	private static String NUM_FORMAT = "#,###,##0";
	
	/**
	 * 折扣格式
	 */
	private static String NUM_FLOAT_FORMAT = "#,###,##0.0000";
	
	/**
	 * 返回金额格式，带两位小数，不带千分符
	 * @param moneys
	 * @return 金额格式
	 */
	public static String getMoneyNormalFormat(double moneys) {
		return new DecimalFormat(MONEY_NORMAL_FROMAT).format(moneys);
	}
	
	/**
	 * 对金额进行格式化,对格式后结果为#,###,###.00
	 * @param moneys 
	 *        需要格式化的金额
	 * @return 
	 * 	      格式化后的字符串
	 */
	public static String getMoneyFormat(double moneys) {
		DecimalFormat df = new DecimalFormat(MONEY_FORMAT);
		return df.format(moneys);
	}
	
	/**
	 * 对字符串金额进行格式化,对格式后结果为#,###,###.00
	 * @param moneys 
	 *        需要格式化的金额
	 * @return 
	 * 	      格式化后的字符串
	 */
	public static String getMoneyFormat(String moneys) {
		String formatNum = null;
		try {
			double temp = Double.parseDouble(moneys);
			DecimalFormat df = new DecimalFormat(MONEY_FORMAT);
			formatNum = df.format(temp);
		} 
		catch (NumberFormatException nfex) {
			logger.fatal("字符串转换为Double出现错误, num = " + moneys);
			formatNum = "";
		}
		return formatNum;
	}
	
	/**
	 * 对数量进行格式化，格式化后结果#,###,##0
	 * @param num 
	 * 	      需要格式化的数字
	 * @return
	 *        格式化后的字符串 
	 */
	public static String getNumberFormat(double num) {
		DecimalFormat df = new DecimalFormat(NUM_FORMAT);
		
		return df.format(num);
	}
	
	/**
	 * 对数量进行格式化，格式化后结果#,###,##0.0000
	 * @param num
	 * 		  需要格式化的数字
	 * @return
	 *        格式化后的字符串 
	 */
	public static String getFloatFormat(double num) {
		DecimalFormat df = new DecimalFormat(NUM_FLOAT_FORMAT);
		return df.format(num);
	}
	
	/**
	 * 对字符串进行格式化，格式化后结果#,###,##0.0000
	 * @param num
	 * 		  需要格式化的字符串
	 * @return
	 *        格式化后的字符串 
	 */
	public static String getFloatFormat(String num) {
		String formatNum = null;
		try {
			double temp = Double.parseDouble(num);
			DecimalFormat df = new DecimalFormat(NUM_FLOAT_FORMAT);
			formatNum = df.format(temp);
		} 
		catch (NumberFormatException nfex) {
			logger.fatal("字符串转换为Double出现错误, num = " + num);
			formatNum = "";
		}
		return formatNum;
	}
}
