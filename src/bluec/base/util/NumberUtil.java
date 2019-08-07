/*
* Copyright Information JiaLing Group
* project ���������Դ�ƻ�����ϵͳ
* Comment ���ָ�ʽ������
* JDK Version 1.4.2
* Created on 2006-10-21
* version 1.0
* Modify history
*/

package bluec.base.util;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

/** 
 * ���ָ�ʽ��
 * @author Techie
 * @version 1.0
 */
public class NumberUtil {
	
	/**
	 * ��־���� 
	 */
	private static Logger logger = Logger.getLogger("���ָ�ʽ��");
	
	/**
	 * ���������ʽ
	 */
	private static String MONEY_NORMAL_FROMAT = "0.00";
	
	/**
	 * ����ʽ
	 */
	private static String MONEY_FORMAT = "#,###,##0.00";
	
	/**
	 * ������ʽ
	 */
	private static String NUM_FORMAT = "#,###,##0";
	
	/**
	 * �ۿ۸�ʽ
	 */
	private static String NUM_FLOAT_FORMAT = "#,###,##0.0000";
	
	/**
	 * ���ؽ���ʽ������λС��������ǧ�ַ�
	 * @param moneys
	 * @return ����ʽ
	 */
	public static String getMoneyNormalFormat(double moneys) {
		return new DecimalFormat(MONEY_NORMAL_FROMAT).format(moneys);
	}
	
	/**
	 * �Խ����и�ʽ��,�Ը�ʽ����Ϊ#,###,###.00
	 * @param moneys 
	 *        ��Ҫ��ʽ���Ľ��
	 * @return 
	 * 	      ��ʽ������ַ���
	 */
	public static String getMoneyFormat(double moneys) {
		DecimalFormat df = new DecimalFormat(MONEY_FORMAT);
		return df.format(moneys);
	}
	
	/**
	 * ���ַ��������и�ʽ��,�Ը�ʽ����Ϊ#,###,###.00
	 * @param moneys 
	 *        ��Ҫ��ʽ���Ľ��
	 * @return 
	 * 	      ��ʽ������ַ���
	 */
	public static String getMoneyFormat(String moneys) {
		String formatNum = null;
		try {
			double temp = Double.parseDouble(moneys);
			DecimalFormat df = new DecimalFormat(MONEY_FORMAT);
			formatNum = df.format(temp);
		} 
		catch (NumberFormatException nfex) {
			logger.fatal("�ַ���ת��ΪDouble���ִ���, num = " + moneys);
			formatNum = "";
		}
		return formatNum;
	}
	
	/**
	 * ���������и�ʽ������ʽ������#,###,##0
	 * @param num 
	 * 	      ��Ҫ��ʽ��������
	 * @return
	 *        ��ʽ������ַ��� 
	 */
	public static String getNumberFormat(double num) {
		DecimalFormat df = new DecimalFormat(NUM_FORMAT);
		
		return df.format(num);
	}
	
	/**
	 * ���������и�ʽ������ʽ������#,###,##0.0000
	 * @param num
	 * 		  ��Ҫ��ʽ��������
	 * @return
	 *        ��ʽ������ַ��� 
	 */
	public static String getFloatFormat(double num) {
		DecimalFormat df = new DecimalFormat(NUM_FLOAT_FORMAT);
		return df.format(num);
	}
	
	/**
	 * ���ַ������и�ʽ������ʽ������#,###,##0.0000
	 * @param num
	 * 		  ��Ҫ��ʽ�����ַ���
	 * @return
	 *        ��ʽ������ַ��� 
	 */
	public static String getFloatFormat(String num) {
		String formatNum = null;
		try {
			double temp = Double.parseDouble(num);
			DecimalFormat df = new DecimalFormat(NUM_FLOAT_FORMAT);
			formatNum = df.format(temp);
		} 
		catch (NumberFormatException nfex) {
			logger.fatal("�ַ���ת��ΪDouble���ִ���, num = " + num);
			formatNum = "";
		}
		return formatNum;
	}
}
