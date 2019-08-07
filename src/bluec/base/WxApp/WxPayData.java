package bluec.base.WxApp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import java.util.logging.Logger;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import org.json.JSONObject;
import bluec.base.CUtil;

public class WxPayData {

	static Logger logger = Logger.getLogger(CUtil.class.getName());

	public WxPayData() {
	}

	// 采用排序的Dictionary的好处是方便对数据包进行签名，不用再签名之前再做一次排序
	private HashMap<String, Object> m_values = new HashMap<String, Object>();

	/**
	 * 设置某个字段的值
	 * 
	 * @param key
	 *            字段名
	 * @param value
	 *            字段值
	 */
	public void SetValue(String key, Object value) {
		m_values.put(key, value);
	}

	public void Remove(String key) {
		m_values.remove(key);
	}
	/**
	 * 根据字段名获取某个字段的值
	 * 
	 * @param key
	 *            字段名
	 * @return key对应的字段值
	 */
	public Object GetValue(String key) {
		return (m_values.containsKey(key)) ? m_values.get(key) : null;
	}

	/**
	 * 判断某个字段是否已设置
	 * 
	 * @param key
	 *            字段名
	 * @return 若字段key已被设置，则返回true，否则返回false
	 */
	public boolean IsSet(String key) {
		return m_values.containsKey(key);
	}

	/**
	 * @将Dictionary转成xml
	 * @return 经转换得到的xml串
	 * @throws WxPayException
	 **/

	public String ToXml() throws Exception {
		// 数据为空时不能转化为xml格式
		if (m_values.size() == 0) {
			logger.info("WxPayData数据为空!");
			throw new Exception("WxPayData数据为空!");
		}

		String xml = "<xml>";
		Iterator<String> iter = m_values.keySet().iterator();

		while (iter.hasNext()) {
			String key = iter.next().toString();
			Object value = m_values.get(key);

			// 字段值不能为null，会影响后续流程
			if (value == null) {
				logger.info("WxPayData内部含有值为null的字段!");
				throw new Exception("WxPayData内部含有值为null的字段!");
			}

			if (value instanceof Integer) {
				xml += "<" + key + ">" + value.toString() + "</" + key + ">";
			} else if (value instanceof String) {
				xml += "<" + key + ">" + "<![CDATA[" + value.toString() + "]]></" + key + ">";
			} else// 除了string和int类型不能含有其他数据类型
			{
				logger.info("WxPayData字段数据类型错误!");
				throw new Exception("WxPayData字段数据类型错误!");
			}
		}
		xml += "</xml>";
		return xml;
	}

	/**
	 * @将xml转为WxPayData对象并返回对象内部的数据
	 * @param string
	 *            待转换的xml串
	 * @return 经转换得到的Dictionary
	 * @throws WxPayException
	 */
	public HashMap<String, Object> FromXml(String xml, String MKey) throws Exception {
		if (xml == null || xml.equals("")) {
			logger.info("将空的xml串转换为WxPayData不合法!");
			throw new Exception("将空的xml串转换为WxPayData不合法!");
		}

		InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(in); // 建立文档

		Node xmlNode = doc.getFirstChild();// 获取到根节点<xml>
		NodeList nodes = xmlNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			xmlNode = nodes.item(i);
			m_values.put(xmlNode.getNodeName(), xmlNode.getNodeValue());// 获取xml的键值对到WxPayData内部的数据中
		}
		try {
			CheckSign(MKey); // 验证签名,不通过会抛异常
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

		return m_values;
	}

	/**
	 * @Dictionary格式转化成url参数格式 @ return url格式串, 该串不包含sign字段值
	 */
	public String ToUrl(){
		String buff = "";
		try {
			Object[] key_arr = m_values.keySet().toArray();     

			Arrays.sort(key_arr);  
			
			for(Object keys : key_arr) {
				String key = (String)keys;
				Object value = m_values.get(key);
				if (value == null) {
					logger.info("WxPayData内部含有值为null的字段!");
				}

				if (key != "sign" && key != "") {
					buff += key + "=" + value.toString() + "&";
				}
			}
			/*
			Iterator<String> iter = m_values.keySet().iterator();

			while (iter.hasNext()) {
				String key = iter.next().toString();
				Object value = m_values.get(key);
				if (value == null) {
					logger.info("WxPayData内部含有值为null的字段!");
				}

				if (key != "sign" && key != "") {
					buff += key + "=" + value.toString() + "&";
				}
			} */
		} catch (Exception ex) {

		}
		buff = CUtil.rightTrim(buff, '&');
		return buff;
	}

	/**
	 * @Dictionary格式化成Json
	 * @return json串数据
	 */
	public String ToJson() {
		JSONObject obj = new JSONObject();
		Iterator<String> iter = m_values.keySet().iterator();

		try {
			while (iter.hasNext()) {
				String key = iter.next().toString();
				Object value = m_values.get(key);
				obj.put(key, value);
			}
		} catch (Exception ex) {

		}
		return obj.toString();
	}

	/**
	 * @生成签名，详见签名生成算法
	 * @return 签名, sign字段不参加签名
	 */

	public String MakeSign(String MKey) {
		// 转url格式
		String str = ToUrl();
		// 在string后加入API KEY
		str += "&key=" + MKey;
		// MD5加密
		// 所有字符转为大写
		return CUtil.MD5(str).toUpperCase();
	}

	public String MakeHMACSHA256(String MKey) {
		String str = ToUrl();
		// 在string后加入API KEY
		str += "&key=" + MKey;
		// MD5加密
		// 所有字符转为大写
		return CUtil.HMACSHA256(str,MKey);
	}

	/**
	 * @JSAPI调用签名
	 */

	public String MakeJsApiSign() {
		String str = ToUrl();
		return CUtil.SHA1(str).toLowerCase();
	}

	/**
	 * 
	 * 检测签名是否正确 正确返回true，错误抛异常
	 */
	public boolean CheckSign(String MKey) throws Exception{
		// 如果没有设置签名，则跳过检测
		if (!IsSet("sign")) {
			return true;
		}
		// 如果设置了签名但是签名为空，则抛异常
		else if (GetValue("sign") == null || GetValue("sign").toString().equals("")) {
			logger.info("WxPayData签名存在但不合法!");
			throw new Exception("WxPayData签名存在但不合法!");
		}

		// 获取接收到的签名
		String return_sign = GetValue("sign").toString();

		// 在本地计算新的签名
		String cal_sign = MakeSign(MKey);

		if (cal_sign.equals(return_sign)) {
			return true;
		}

		logger.info("WxPayData签名验证错误!");
		throw new Exception("WxPayData签名验证错误!");
	}

	/**
	 * @获取Dictionary
	 */
	public HashMap<String, Object> GetValues() {
		return m_values;
	}
}
