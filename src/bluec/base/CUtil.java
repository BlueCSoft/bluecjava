package bluec.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.net.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class CUtil {
	/**
	 * formatStr 使用字符串param替换字符串dstr中为rStr的所有子字符串
	 * 
	 * @param dstr
	 *            原字符串
	 * @param param
	 *            参数
	 * @param rStr
	 *            被替换的子字符串
	 * @return 替换后的字符串
	 */
	static Logger logger = Logger.getLogger(CUtil.class.getName());

	public static String formatStr(String dstr, String param, String rStr) {
		StringBuffer temp = new StringBuffer();
		int i = 0; // 寻找子字符串的开始位置
		int l = dstr.length();
		int k = rStr.length();

		while ((i = dstr.indexOf(rStr)) != -1) {
			temp.append(dstr.substring(0, i));
			temp.append(param);
			l -= i + k;
			dstr = dstr.substring(i + k);
		}
		if (!dstr.equals(""))
			temp.append(dstr);
		return temp.toString();
	}

	/**
	 * formatStr 使用数组sstr中的项顺序替换字符串dstr中为rStr的子字符串
	 * 
	 * @param dstr
	 *            原字符串
	 * @param params
	 *            参数数组
	 * @param rStr
	 *            被替换的子字符串
	 * @return 替换后的字符串
	 */
	public static String formatStr(String dstr, String[] params, String rStr) {
		StringBuffer temp = new StringBuffer();
		int i = 0; // 寻找子字符串的开始位置
		int j = 0; // 数组下标
		int l = dstr.length();
		int k = rStr.length();
		// System.out.println("l="+l);
		while (j < params.length && (i = dstr.indexOf(rStr)) != -1) {
			temp.append(dstr.substring(0, i));
			temp.append(params[j]);
			l -= i + k;
			dstr = dstr.substring(i + k);
			j++;
		}
		if (!dstr.equals(""))
			temp.append(dstr);
		return temp.toString();
	}

	/**
	 * 获取当前日期
	 * 
	 * @return String，返回当前日期字符串，格式为yyyy.mm.dd
	 */
	public static String getOrigDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);
		int D = calendar.get(Calendar.DAY_OF_MONTH);
		return Y + "-" + Integer.toString(M) + "-" + Integer.toString(D);
	}

	/**
	 * 获取当前日期
	 * 
	 * @param dChar
	 *            String，年月日的分割符
	 * @return String 返回当前日期字符串，格式为yyyy dChar mm dChar dd
	 */
	public static String getOrigDate(String dChar) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);
		int D = calendar.get(Calendar.DAY_OF_MONTH);
		return Y + dChar + Integer.toString(100 + M).substring(1) + dChar + Integer.toString(100 + D).substring(1);
	}

	public static String getMonthFirstDate(String dChar) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);
		return Y + "-" + Integer.toString(100 + M).substring(1) + "-" + Integer.toString(101).substring(1);
	}

	public static String getYearFirstDate(String dChar) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int Y = calendar.get(Calendar.YEAR);
		return Y + "-" + Integer.toString(101).substring(1) + "-" + Integer.toString(101).substring(1);
	}

	/**
	 * 2013-01-17
	 * 
	 * @return
	 */
	public static String getChinaDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);
		int D = calendar.get(Calendar.DAY_OF_MONTH);
		return Y + "年" + Integer.toString(M) + "月" + Integer.toString(D) + "日";
	}

	/**
	 * 格式化指定日期为中文格式
	 * 
	 * @param sDate
	 *            String 要格式化的日期
	 * @param dChar
	 *            String 年月日间的分割符
	 * @return String 中文格式的日期字符串 yyyy年mm月dd日
	 */
	public static String dateToChinaFormat(String sDate, String dChar) {
		int[] ymd = new int[3];
		ymd[0] = Integer.parseInt(sDate.substring(0, 4));
		ymd[1] = Integer.parseInt(sDate.substring(5, 7));
		ymd[2] = Integer.parseInt(sDate.substring(8, 10));
		return ymd[0] + "年" + ymd[1] + "月" + ymd[2] + "日";
	}

	/**
	 * 获取当前时间
	 * 
	 * @return String 返回当前时间的字符串，格式为:yyyy.mm.dd H:mm:s
	 */
	public static String getOrigTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());

		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);
		int D = calendar.get(Calendar.DAY_OF_MONTH);
		int H = calendar.get(Calendar.HOUR_OF_DAY);
		int MM = calendar.get(Calendar.MINUTE);
		int S = calendar.get(Calendar.SECOND);
		return Y + "-" + Integer.toString(100 + M).substring(1) + "-" + Integer.toString(100 + D).substring(1) + " " + H
				+ ":" + MM + ":" + S;
	}

	/**
	 * 获取当前日期
	 * 
	 * @param dChar
	 *            String，年月日的分割符
	 * @return String
	 */
	public static String getOrigTime(String dChar) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);
		int D = calendar.get(Calendar.DAY_OF_MONTH);
		int H = calendar.get(Calendar.HOUR_OF_DAY);
		int MM = calendar.get(Calendar.MINUTE);
		int S = calendar.get(Calendar.SECOND);
		return Y + "-" + Integer.toString(100 + M).substring(1) + "-" + Integer.toString(100 + D).substring(1) + " " + H
				+ ":" + MM + ":" + S;
	}

	public static String getTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		int H = calendar.get(Calendar.HOUR_OF_DAY);
		int MM = calendar.get(Calendar.MINUTE);
		int S = calendar.get(Calendar.SECOND);
		int MS = calendar.get(Calendar.MILLISECOND);
		return H + ":" + MM + ":" + S + "." + MS;
	}

	public static String getTimeS() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH:mm:ss");
		return formatter.format(new java.util.Date());
	}

	public static String getTimeT() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		return formatter.format(new java.util.Date());
	}

	public static String getTimeT(int addMinute) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		calendar.add(Calendar.MINUTE, addMinute);
		return formatter.format(calendar.getTime());
	}

	public static String getTimeSSS() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		return formatter.format(new java.util.Date());
	}

	public static String getTimeFmt(String fmt) {
		SimpleDateFormat formatter = new SimpleDateFormat(fmt);
		return formatter.format(new java.util.Date());
	}

	/* 返回分钟数 */
	public static int getMinuteByDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		return calendar.get(Calendar.HOUR) * 60 + calendar.get(Calendar.MINUTE);
	}

	/**
	 * 替换字符串
	 * 
	 * @param dstr
	 * @param param
	 * @param rStr
	 * @return
	 */
	public static String replaceStr(String dstr, String param, String rStr) {
		return formatStr(dstr, param, rStr);
	}

	/**
	 * 重复子字符串的次数组成一个字符串
	 * 
	 * @param subStr
	 *            String
	 * @param rCount
	 *            int
	 * @return String
	 */
	public static String StringOfChar(String subStr, int rCount) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < rCount; i++)
			buffer.append(subStr);
		return buffer.toString();
	}

	public static String replaceXmlXA(String s) {
		return s.replaceAll("&#x000A;", "\n");
	}

	public static String replaceXmlSpChar(String s) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < s.length(); i++)
			switch (s.charAt(i)) {
			case '&':
				buffer.append("&amp;");
				break;
			case '<':
				buffer.append("&lt;");
				break;
			case '>':
				buffer.append("&gt;");
				break;
			case '"':
				buffer.append("&quot;");
				break;
			case '\n':
				buffer.append("&#x000A;");
				break;
			default:
				buffer.append(s.charAt(i));
				break;
			}
		return buffer.toString();
	}

	public static String replaceXmlSpCharX(String s) {
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
			switch (s.charAt(i)) {
			case '&':
				buffer.append("&amp;");
				break;
			case '<':
				buffer.append("&lt;");
				break;
			case '>':
				buffer.append("&gt;");
				break;
			case '"':
				buffer.append("&quot;");
				break;
			default:
				buffer.append(s.charAt(i));
				break;
			}
		return buffer.toString();
	}

	public static String replaceJsonSpChar(String s) {
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
			switch (s.charAt(i)) {
			case '\"':
				buffer.append("\\\"");
				break;
			case '\\':
				buffer.append("\\\\");
				break;
			case '/':
				buffer.append("\\/");
				break;
			case '\b':
				buffer.append("\\b");
				break;
			case '\f':
				buffer.append("\\f");
				break;
			case '\n':
				buffer.append("\\n");
				break;
			case '\r':
				buffer.append("\\r");
				break;
			case '\t':
				buffer.append("\\t");
				break;
			default:
				buffer.append(s.charAt(i));
				break;

			}
		return buffer.toString();
	}

	public static String NVL(String value) {
		if (value == null)
			return "";
		else
			return replaceXmlSpChar(value); // value.trim();
	}

	public static String NVLX(String value) {
		if (value == null)
			return "";
		else
			return replaceXmlSpCharX(value); // value.trim();
	}

	public static String rightTrim(String value, char c) {
		int i = value.length();
		for (; i > 0; i--)
			if (value.charAt(i - 1) != c)
				break;
		return value.substring(0, i);
	}

	public static String rightTrim(String value) {
		return rightTrim(value, ' ');
	}

	public static String removeCR(String sql) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < sql.length(); i++)
			if (sql.charAt(i) != '\r')
				buffer.append(sql.charAt(i));
		return buffer.toString();
	}

	/**
	 * 处理特殊字符
	 * 
	 * @param map
	 * @return
	 */
	public static String replaceSpChar(String s) {
		StringBuffer buffer = new StringBuffer();
		s = formatStr(s, "\n", "ん");
		char c;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (c == '\'')
				buffer.append('\'');
			buffer.append(c);
		}
		return buffer.toString();
	}

	public static String SetPassWord(String PassWord) {
		// return PassWord;
		int i, j, n, x, y;
		char c;
		String passs = "1234567890-=+_)(*&^%$#@!{}|\\][:\";?Z,./~`qazxswedcvfrtgbnhyujmkiolpPOLKIMNJUYHBVGTRFCXDEWSQA";
		String Result = "";
		String stemp = "               ";
		if (PassWord.equals(""))
			return "";
		Random ran1 = new Random();
		PassWord += stemp.substring(PassWord.length());
		for (i = 0; i < PassWord.length(); i++) {
			c = PassWord.charAt(i);
			n = 1000 + c;
			j = 0;
			while (j < 3) {
				x = n % 10;
				y = ran1.nextInt(9);
				n = n / 10;
				c = passs.charAt(y * 10 + x);
				Result = Result + c + y;
				j++;
			}
		}
		return Result;
	}

	public static String GetPassWord(String PassWord) {
		// return PassWord;
		int i, k, j, n, kl, l, x, y;
		char c, cn;
		// System.out.println(PassWord);
		String Result = "";
		String passs = "1234567890-=+_)(*&^%$#@!{}|\\][:\";?Z,./~`qazxswedcvfrtgbnhyujmkiolpPOLKIMNJUYHBVGTRFCXDEWSQA";
		if (PassWord == null || PassWord.equals(""))
			return "";
		if (PassWord.charAt(0) == '!')
			return PassWord.substring(1);
		k = PassWord.length();
		i = 0;
		while (i < k - 1) {
			l = 0;
			kl = 0;
			while (l < 3) {
				c = PassWord.charAt(i); // 密文
				cn = PassWord.charAt(i + 1); // 随机数
				i += 2;
				// 求口令的1位
				n = 0;
				for (j = 0; j < 90; j++)
					if (c == passs.charAt(j)) {
						n = j;
						break;
					}
				y = n / 10; // 随机数
				x = n % 10; // 口令位
				if (y != cn - '0') // 口令错误
					return "error password";

				kl = kl + x * (int) (Math.pow(10, l));
				// System.out.print("c:"+c+"("+n+"),y="+y+",x="+x+",kl="+kl+",10^l="+(10^l)+"
				// ");
				l++;
			}
			// System.out.print("\n");
			Result = Result + (char) kl;
		}
		return rightTrim(Result);
		// return PassWord;
	}

	public static String SetPassWordEx(String PassWord) {
		// return PassWord;
		int i, j, n, x, y;
		char c;
		String passs = "1234567890qazxswedcvfrtgbnhyujmkiolpPOLKIMNJUYHBVGTRFCXDEWS@";

		String Result = "";

		if (PassWord.equals(""))
			return "";

		Random ran1 = new Random();

		for (i = 0; i < PassWord.length(); i++) {
			c = PassWord.charAt(i);
			n = 1000 + c;
			j = 0;
			while (j < 3) {
				x = n % 10;
				y = ran1.nextInt(6);
				n = n / 10;
				c = passs.charAt(y * 10 + x);
				Result = Result + c + y;
				j++;
			}
		}
		return Result;
	}

	public static String GetPassWordEx(String PassWord) {

		int i, k, j, n, kl, l, x, y;
		char c, cn;

		String Result = "";
		String passs = "1234567890qazxswedcvfrtgbnhyujmkiolpPOLKIMNJUYHBVGTRFCXDEWS@";

		if (PassWord == null || PassWord.equals(""))
			return "";

		if (PassWord.charAt(0) == '!')
			return PassWord.substring(1);

		k = PassWord.length();
		i = 0;

		while (i < k - 1) {
			l = 0;
			kl = 0;
			while (l < 3) {
				c = PassWord.charAt(i); // 密文
				cn = PassWord.charAt(i + 1); // 随机数
				i += 2;
				// 求口令的1位
				n = 0;
				for (j = 0; j < passs.length(); j++)
					if (c == passs.charAt(j)) {
						n = j;
						break;
					}
				y = n / 10; // 随机数
				x = n % 10; // 口令位
				if (y != cn - '0') // 口令错误
					return "error password";

				kl = kl + x * (int) (Math.pow(10, l));
				// System.out.print("c:"+c+"("+n+"),y="+y+",x="+x+",kl="+kl+",10^l="+(10^l)+"
				// ");
				l++;
			}
			// System.out.print("\n");
			Result = Result + (char) kl;
		}
		return rightTrim(Result);
		// return PassWord;
	}

	/**
	 * 生成时间戳，标准北京时间，时区为东八区，自1970年1月1日 0点0分0秒以来的秒数
	 * 
	 * @return 时间戳
	 */
	public static String GenerateTimeStamp() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	/**
	 * 生成随机串，随机串包含字母或数字
	 * 
	 * @return 随机串
	 */
	public static String GenerateNonceStr() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 生成GUID
	 */
	public static String GenerateGuid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 根据当前系统时间加随机序列来生成单号
	 * 
	 * @return 单号
	 */
	public static String GenerateOutTradeNo(String pMchId) {
		Random ran = new Random();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String dtime = formatter.format(new java.util.Date());

		return String.format("%s%s%03d", pMchId, dtime, ran.nextInt(999));
	}

	public static String formatStr(String str) {
		String stred = str;
		try {
			if (CInitParam.webAppType.equals("websphere"))
				stred = new String(str.getBytes("ISO8859-1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stred;
	}

	public static String formatChinaMonth(String pubyf) {
		return pubyf.substring(0, 4) + "年" + pubyf.substring(5, 7) + "月";
	}

	public static String formatToChinaDate(String sDate) {
		return sDate.substring(0, 4) + "年" + Integer.parseInt(sDate.substring(5, 7)) + "月"
				+ Integer.parseInt(sDate.substring(8, 10)) + "日";
	}

	public static String combinate(String subs, String cbs, int slen) {
		StringBuffer sbuffer = new StringBuffer();
		for (int i = 0; i < slen; i++) {
			if (i > 0)
				sbuffer.append(cbs);
			sbuffer.append(subs);
		}
		return sbuffer.toString();
	}

	public static String combinate(String[] subs, String cbs) {
		StringBuffer sbuffer = new StringBuffer();
		for (int i = 0; i < subs.length; i++) {
			if (i > 0)
				sbuffer.append(cbs);
			sbuffer.append(subs[i]);
		}
		return sbuffer.toString();
	}

	public static String extractFileName(String sFile) {
		sFile = sFile.replace('\\', '/');
		int n = sFile.lastIndexOf("/");
		if (n > -1)
			sFile = sFile.substring(n + 1);
		return sFile;
	}

	public static String escape(String src) {
		try {
			return URLEncoder.encode(src, "utf-8");
		} catch (Exception ex) {
			return ex.getMessage();
		}
	}

	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0, nLen = src.length();
		char ch;
		while (lastPos < nLen) {
			pos = src.indexOf("%u", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = nLen;
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	public static String string2Unicode(String src) {

		StringBuffer unicode = new StringBuffer();

		for (int i = 0; i < src.length(); i++) {

			// 取出每一个字符
			char c = src.charAt(i);

			// 转换为unicode
			unicode.append("\\u" + Integer.toHexString(c));
		}

		return unicode.toString();
	}

	public static String getChinaMonth(String pubyf) {
		return pubyf.substring(0, 4) + "年" + pubyf.substring(5, 7) + "月";
	}

	public static int findElementInArray(String[] Arrays, String element) {
		int result = -1;
		element = element.toUpperCase();
		for (int i = 0; i < Arrays.length; i++) {
			if (element.equals(Arrays[i])) {
				result = i;
				break;
			}
		}
		return result;
	}

	public static String formatNumber(double n, String sFormat, boolean __zeroToSpace) {

		if (__zeroToSpace && n == 0)
			return "";

		java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.DecimalFormat.getInstance();
		df.applyPattern(sFormat);

		return df.format(n);
	}

	public final static String MD5(String data) {
		String Result = "";
		try {
			java.security.MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(data.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (byte item : array) {
				sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
			}
			Result = sb.toString().toUpperCase();
		} catch (Exception ex) {
			Result = ex.getMessage();
		}
		return Result;
	}

	public static String HMACSHA256(String data, String key) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (byte item : array) {
				sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString().toUpperCase();
		} catch (Exception ex) {
			return "";
		}
	}

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	private static String getFormattedText(byte[] bytes) {
		int len = bytes.length;
		StringBuilder buf = new StringBuilder(len * 2);
		// 把密文转换成十六进制的字符串形式
		for (int j = 0; j < len; j++) {
			buf.append(HEX[(bytes[j] >> 4) & 0x0f]);
			buf.append(HEX[bytes[j] & 0x0f]);
		}
		return buf.toString();
	}

	public final static String SHA1(String str) {
		if (str == null) {
			return null;
		}
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
			messageDigest.update(str.getBytes());
			return getFormattedText(messageDigest.digest());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final static String SHA1(String[] vparams) {
		return SHA1(String.join("", vparams));
	}

	public static String SucessMsgToJson(String msg) {
		JSONObject data = new JSONObject();
		try {
			data.put("code", "1");
			data.put("msg", "成功");
			data.put("sub_code", "1");
			data.put("sub_msg", CUtil.replaceStr((msg == null) ? "空异常" : msg, "'", "\""));
		} catch (Exception ex) {

		}
		return data.toString();
	}

	public static String SucessMsgToJson(String[] vkeyvalues) {
		JSONObject data = new JSONObject();
		try {
			for (int i = 0; i < vkeyvalues.length; i += 2) {
				data.put(vkeyvalues[i], vkeyvalues[i + 1]);
			}

			data.put("code", "1");
			data.put("msg", "成功");
			data.put("sub_code", "1");
			data.put("sub_msg", "成功");
		} catch (Exception ex) {

		}
		return data.toString();
	}

	public static String ErrorMsgToJsonCode(String errmsg) {
		return "{\"code\":\"0\",\"msg\":\"" + CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\"")
				+ "\",\"sub_code\":\"0\",\"sub_msg\":\"失败\"}";
	}

	public static String ErrorMsgToJson(int subcode, String errmsg, String[] vkeyvalues) {
		JSONObject data = new JSONObject();
		try {
			data.put("code", "1");
			data.put("msg", "成功");
			data.put("sub_code", subcode + "");
			data.put("sub_msg", (errmsg == null) ? "空异常" : replaceJsonSpChar(errmsg));

			if (vkeyvalues != null) {
				for (int i = 0; i < vkeyvalues.length; i += 2) {
					data.put(vkeyvalues[i], vkeyvalues[i + 1]);
				}
			}
		} catch (Exception ex) {

		}
		return data.toString();
	}

	public static String ErrorMsgToJson(String errmsg) {
		return ErrorMsgToJson(0, errmsg, null);
	}

	public static String ErrorMsgToJson(int subcode, String errmsg) {
		return ErrorMsgToJson(subcode, errmsg, null);
	}

	public static String ErrorMsgToJson(String errmsg, String[] vkeyvalues) {
		return ErrorMsgToJson(0, errmsg, vkeyvalues);
	}

	public static String ErrorMsgToXml(String errmsg) {
		StringBuilder sResult = new StringBuilder();
		sResult.append("<xml><errors><error>");
		sResult.append(errmsg);
		sResult.append("</error></errors></xml>");

		return sResult.toString();
	}

	public static String SucessMsgToXml(String msg) {
		StringBuilder sResult = new StringBuilder();
		sResult.append("<xml><ok><r>");
		sResult.append(msg);
		sResult.append("</r></ok></xml>");

		return sResult.toString();
	}

	public static String join(String[] vStrings, String cjoin) {
		StringBuffer param = new StringBuffer();
		int i = 0;
		for (; i < vStrings.length; i += 2) {
			if (i == 0)
				param.append("?");
			else
				param.append(cjoin);
			param.append(vStrings[i] + "=" + escape(vStrings[i + 1]));
		}
		return param.toString();
	}

	public static String join(String join, String[] strAry) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strAry.length; i++) {
			if (i == (strAry.length - 1)) {
				sb.append(strAry[i]);
			} else {
				sb.append(strAry[i]).append(join);
			}
		}

		return new String(sb);
	}

	public static String ToJson(String[] vkeyparams) {
		JSONObject m_values = new JSONObject();
		try {
			for (int i = 0; i < vkeyparams.length; i += 2)
				m_values.put(vkeyparams[i], vkeyparams[i + 1]);
		} catch (Exception ex) {

		}
		return m_values.toString();
	}

	public static String formatJson(String jsonStr) {
		if (null == jsonStr || "".equals(jsonStr))
			return "";

		String[] array = jsonStr.split("\n");
		StringBuilder buf = new StringBuilder();
		for(int i = 0;i<array.length;i++){
			buf.append(array[i].trim());
		}
		jsonStr = buf.toString();
		
		StringBuilder sb = new StringBuilder();
		char last = '\0';
		boolean bmark = false; // 存在未完成的字符串
		char current = '\0';
		int indent = 0;
		
		for (int i = 0; i < jsonStr.length(); i++) {
			last = current;
			current = jsonStr.charAt(i);
			if (current == '"' && last != '\\')
				bmark = !bmark;
			switch (current) {
			case '{':
			case '[':
				if (!bmark) {
					sb.append(current);
					sb.append('\n');
					indent++;
					addIndentBlank(sb, indent);
				}
				break;
			case '}':
			case ']':
				if (!bmark) {
					sb.append('\n');
					indent--;
					addIndentBlank(sb, indent);
					sb.append(current);
				}
				break;
			case ',':
				sb.append(current);
				if (last != '\\' && !bmark) {
					sb.append('\n');
					addIndentBlank(sb, indent);
				}
				break;
			default:
				sb.append(current);
			}
		}

		return sb.toString();
	}

	/**
	 * 添加space
	 * 
	 * @param sb
	 * @param indent
	 * @author lizhgb
	 * @Date 2015-10-14 上午10:38:04
	 */
	private static void addIndentBlank(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
	}

	public static String floatToJsonFmt(double v) {
		String result = String.valueOf(v);
		if (result.indexOf(".") < 0)
			result += ".0";
		if (result.indexOf(".") == 0)
			result = "0" + result;
		return result;
	}

	public static String big(double d) {
        NumberFormat nf = NumberFormat.getInstance();
        // 是否以逗号隔开, 默认true以逗号隔开,如[123,456,789.128]
        nf.setGroupingUsed(false);
        // 结果未做任何处理
        return nf.format(d);
    }

	
	public static String getEncoding(String str) {
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "";
	}

	public static String GetExtension(String filePath, int n) {
		int k = filePath.lastIndexOf(".");
		return (k > -1) ? filePath.substring(k + n) : "";
	}

	public static String GetExtension(String filePath) {
		return GetExtension(filePath, 0);
	}

	public static String GetFileNameWithoutExtension(String filePath) {
		filePath = filePath.replaceAll("\\\\", "/");
		int k = filePath.lastIndexOf(".");
		if (k > -1)
			filePath = filePath.substring(0, k);
		filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
		return filePath;
	}

	public static void CreateNewDir(String dir) {
		try {
			File file = new File(dir);
			if (!file.exists()) // 如果文件存在,则创建File.AppendText对象
			{
				file.mkdir();
			}
		} catch (Exception ex) {
		}
	}

	public static void FileCopy(String dFilePath, String sFilePath) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(sFilePath);
			output = new FileOutputStream(dFilePath);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			input.close();
			output.close();
		} catch (Exception ex) {
		} finally {
		}
	}

	public static String getAppName(String contextPath) {
		return contextPath.equals("") ? "/" : contextPath;
	}

	public static String getAppName(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		return contextPath.equals("") ? "/" : contextPath;
	}

	public static String getLocalIp() {
		String ip = "8.8.8.8";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception ex) {
		}
		return ip;
	}

	public static String getUrl(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		if (!request.getQueryString().equals(""))
			url += "?" + request.getQueryString();
		return url;
	}

	public static String cssFileImport(HttpServletRequest request, String path, String[] fileNames) {
		StringBuilder buffer = new StringBuilder();
		try {
			String rpath = request.getSession().getServletContext().getRealPath(path);
			for (int i = 0; i < fileNames.length; i++) {
				buffer.append("<link rel=\"stylesheet\" href=" + path + fileNames[i] + "?version="
						+ (new File(rpath + fileNames[i]).lastModified()) + " type=\"text/css\">\n");
			}
		} catch (Exception ex) {

		}
		return buffer.toString();
	}

	public static String jsFileImport(HttpServletRequest request, String path, String[] fileNames) {
		StringBuilder buffer = new StringBuilder();
		try {
			String rpath = request.getSession().getServletContext().getRealPath(path);
			for (int i = 0; i < fileNames.length; i++) {
				buffer.append("<script src=" + path + fileNames[i] + "?version="
						+ (new File(rpath + fileNames[i]).lastModified()) + "></script>\n");
			}
		} catch (Exception ex) {

		}
		return buffer.toString();
	}

	public static int winCalltype(HttpServletRequest request) {
		int result = 0;
		if (request.getParameter("wincalltype") != null) {
			result = Integer.parseInt(request.getParameter("wincalltype").toString());
		}
		return result;
	}

	public static String showOnpopState(HttpServletRequest request) {
		StringBuilder buffer = new StringBuilder();
		if (request.getParameter("wincalltype") != null && request.getParameter("wincalltype").toString().equals("1")) {
			buffer.append("history.pushState('a', null);\n");
			buffer.append("window.onpopstate = function(){\n");
			buffer.append("  parent._$closeWindow(__$windowcallresult);\n");
			buffer.append("};\n");
		}
		return buffer.toString();
	}
}
