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

	// ���������Dictionary�ĺô��Ƿ�������ݰ�����ǩ����������ǩ��֮ǰ����һ������
	private HashMap<String, Object> m_values = new HashMap<String, Object>();

	/**
	 * ����ĳ���ֶε�ֵ
	 * 
	 * @param key
	 *            �ֶ���
	 * @param value
	 *            �ֶ�ֵ
	 */
	public void SetValue(String key, Object value) {
		m_values.put(key, value);
	}

	public void Remove(String key) {
		m_values.remove(key);
	}
	/**
	 * �����ֶ�����ȡĳ���ֶε�ֵ
	 * 
	 * @param key
	 *            �ֶ���
	 * @return key��Ӧ���ֶ�ֵ
	 */
	public Object GetValue(String key) {
		return (m_values.containsKey(key)) ? m_values.get(key) : null;
	}

	/**
	 * �ж�ĳ���ֶ��Ƿ�������
	 * 
	 * @param key
	 *            �ֶ���
	 * @return ���ֶ�key�ѱ����ã��򷵻�true�����򷵻�false
	 */
	public boolean IsSet(String key) {
		return m_values.containsKey(key);
	}

	/**
	 * @��Dictionaryת��xml
	 * @return ��ת���õ���xml��
	 * @throws WxPayException
	 **/

	public String ToXml() throws Exception {
		// ����Ϊ��ʱ����ת��Ϊxml��ʽ
		if (m_values.size() == 0) {
			logger.info("WxPayData����Ϊ��!");
			throw new Exception("WxPayData����Ϊ��!");
		}

		String xml = "<xml>";
		Iterator<String> iter = m_values.keySet().iterator();

		while (iter.hasNext()) {
			String key = iter.next().toString();
			Object value = m_values.get(key);

			// �ֶ�ֵ����Ϊnull����Ӱ���������
			if (value == null) {
				logger.info("WxPayData�ڲ�����ֵΪnull���ֶ�!");
				throw new Exception("WxPayData�ڲ�����ֵΪnull���ֶ�!");
			}

			if (value instanceof Integer) {
				xml += "<" + key + ">" + value.toString() + "</" + key + ">";
			} else if (value instanceof String) {
				xml += "<" + key + ">" + "<![CDATA[" + value.toString() + "]]></" + key + ">";
			} else// ����string��int���Ͳ��ܺ���������������
			{
				logger.info("WxPayData�ֶ��������ʹ���!");
				throw new Exception("WxPayData�ֶ��������ʹ���!");
			}
		}
		xml += "</xml>";
		return xml;
	}

	/**
	 * @��xmlתΪWxPayData���󲢷��ض����ڲ�������
	 * @param string
	 *            ��ת����xml��
	 * @return ��ת���õ���Dictionary
	 * @throws WxPayException
	 */
	public HashMap<String, Object> FromXml(String xml, String MKey) throws Exception {
		if (xml == null || xml.equals("")) {
			logger.info("���յ�xml��ת��ΪWxPayData���Ϸ�!");
			throw new Exception("���յ�xml��ת��ΪWxPayData���Ϸ�!");
		}

		InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(in); // �����ĵ�

		Node xmlNode = doc.getFirstChild();// ��ȡ�����ڵ�<xml>
		NodeList nodes = xmlNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			xmlNode = nodes.item(i);
			m_values.put(xmlNode.getNodeName(), xmlNode.getNodeValue());// ��ȡxml�ļ�ֵ�Ե�WxPayData�ڲ���������
		}
		try {
			CheckSign(MKey); // ��֤ǩ��,��ͨ�������쳣
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

		return m_values;
	}

	/**
	 * @Dictionary��ʽת����url������ʽ @ return url��ʽ��, �ô�������sign�ֶ�ֵ
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
					logger.info("WxPayData�ڲ�����ֵΪnull���ֶ�!");
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
					logger.info("WxPayData�ڲ�����ֵΪnull���ֶ�!");
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
	 * @Dictionary��ʽ����Json
	 * @return json������
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
	 * @����ǩ�������ǩ�������㷨
	 * @return ǩ��, sign�ֶβ��μ�ǩ��
	 */

	public String MakeSign(String MKey) {
		// תurl��ʽ
		String str = ToUrl();
		// ��string�����API KEY
		str += "&key=" + MKey;
		// MD5����
		// �����ַ�תΪ��д
		return CUtil.MD5(str).toUpperCase();
	}

	public String MakeHMACSHA256(String MKey) {
		String str = ToUrl();
		// ��string�����API KEY
		str += "&key=" + MKey;
		// MD5����
		// �����ַ�תΪ��д
		return CUtil.HMACSHA256(str,MKey);
	}

	/**
	 * @JSAPI����ǩ��
	 */

	public String MakeJsApiSign() {
		String str = ToUrl();
		return CUtil.SHA1(str).toLowerCase();
	}

	/**
	 * 
	 * ���ǩ���Ƿ���ȷ ��ȷ����true���������쳣
	 */
	public boolean CheckSign(String MKey) throws Exception{
		// ���û������ǩ�������������
		if (!IsSet("sign")) {
			return true;
		}
		// ���������ǩ������ǩ��Ϊ�գ������쳣
		else if (GetValue("sign") == null || GetValue("sign").toString().equals("")) {
			logger.info("WxPayDataǩ�����ڵ����Ϸ�!");
			throw new Exception("WxPayDataǩ�����ڵ����Ϸ�!");
		}

		// ��ȡ���յ���ǩ��
		String return_sign = GetValue("sign").toString();

		// �ڱ��ؼ����µ�ǩ��
		String cal_sign = MakeSign(MKey);

		if (cal_sign.equals(return_sign)) {
			return true;
		}

		logger.info("WxPayDataǩ����֤����!");
		throw new Exception("WxPayDataǩ����֤����!");
	}

	/**
	 * @��ȡDictionary
	 */
	public HashMap<String, Object> GetValues() {
		return m_values;
	}
}
