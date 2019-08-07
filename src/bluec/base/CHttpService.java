package bluec.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Vector;

public class CHttpService {
	private static String defaultContentEncoding = "utf-8";

	public CHttpService() {
		defaultContentEncoding = Charset.defaultCharset().name();
	}

	/**
	 * ����GET����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendGet(String urlString) {
		String sResult = "";
		try {
			sResult = send(urlString, "GET", null, null).content;
		} catch (Exception E) {
			sResult = CJson.errorMsgToJson(0, "����(" + urlString + ")ʱ,���֣�" + E.getMessage());
		}
		return sResult;
	}

	public static String webRequestGet(String url, String paramStr) {
		return sendGet(url + (paramStr == "" ? "" : "?") + paramStr);
	}

	/**
	 * ����GET����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendGet(String urlString, String[] params) {
		String sResult = "";
		try {
			sResult = send(urlString, "GET", params, null).content;
		} catch (Exception E) {
			sResult = CJson.errorMsgToJson(0, "����(" + urlString + ")ʱ,���֣�" + E.getMessage());
		}
		return sResult;
	}

	public static String sendGetFmt(String urlString, String[] params) {
		return CUtil.formatJson(sendGet(urlString, params));
	}

	/**
	 * ����GET����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @param propertys
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendGet(String urlString, String[] params, String[] propertys) throws Exception {
		return send(urlString, "GET", params, propertys).content;
	}

	/**
	 * ����POST����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendPost(String urlString) throws Exception {
		return send(urlString, "POST", null, null).content;
	}

	/**
	 * ����POST����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendPost(String urlString, String[] params) throws Exception {
		return send(urlString, "POST", params, null).content;
	}

	public static String sendPostEx(String urlString, String[] params, boolean useJson) {
		String sResult = "";
		try {
			sResult = send(urlString, "POST", params, null, useJson).content;
		} catch (Exception E) {
			sResult = CJson.errorMsgToJson0("����(" + urlString + ")ʱ,���֣�" + E.getMessage());
		}
		return sResult;
	}

	public static String sendPostExNew(String urlString, String[] params, boolean useJson) {
		String sResult = "";
		try {
			sResult = send(urlString, "POST", params, null, useJson).content;
		} catch (Exception E) {
			sResult = CJson.errorMsgToJson0("����(" + urlString + ")ʱ,���֣�" + E.getMessage());
		}
		return sResult;
	}
	
	public static String sendPostEx(String urlString, String[] params) {
		return sendPostEx(urlString, params, false);
	}

	public static String sendPostFmt(String urlString, String[] params, boolean useJson) {
		return CUtil.formatJson(sendPostEx(urlString, params, useJson));
	}

	public static String sendPostJsonFmt(String urlString, String param) {
		return CUtil.formatJson(sendPostEx(urlString, new String[] { param }, true));
	}

	public static String sendPostJson(String urlString, String param) {
		return sendPostExNew(urlString, new String[] { param }, true);
	}
	
	public static String sendPostJsonFmtEx(String urlString, String param) {
		return CUtil.formatJson(sendPostExNew(urlString, new String[] { param }, true));
	}
	
	public static String sendPostFmt(String urlString, String[] params) {
		return CUtil.formatJson(sendPostEx(urlString, params, false));
	}

	/**
	 * ����POST����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @param propertys
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static CHttpRespons sendPost(String urlString, String[] params, String[] propertys) throws Exception {
		return send(urlString, "POST", params, propertys);
	}

	public static String sendPostEx(String urlString, String[] params, String[] propertys, boolean useJson) {
		String sResult = "";
		try {
			sResult = send(urlString, "POST", params, propertys, useJson).content;
		} catch (Exception E) {
			sResult = CJson.errorMsgToJson(0, "����(" + urlString + ")ʱ,���֣�" + E.getMessage());
		}
		return sResult;
	}

	public static String sendPostEx(String urlString, String[] params, String[] propertys) {
		return sendPostEx(urlString, params, propertys, false);
	}

	public static String sendPostFmt(String urlString, String[] params, String[] propertys, boolean useJson) {
		return CUtil.formatJson(sendPostEx(urlString, params, propertys, useJson));
	}

	public static String sendPostFmt(String urlString, String[] params, String[] propertys) {
		return CUtil.formatJson(sendPostEx(urlString, params, propertys, false));
	}

	public static String webRequestPost(String url, String paramStr, String postparam) {
		String[] params = (postparam.equals("")) ? null : new String[] { postparam };
		return sendPostEx(url + (paramStr == "" ? "" : "?" + paramStr), params);
	}

	public static String webRequestJsonPost(String url, String paramStr, String postparam) {
		String[] params = (postparam.equals("")) ? null : new String[] { postparam };
		return sendPostEx(url + (paramStr == "" ? "" : "?" + paramStr), params,true);
	}
	
	public static String webRequestJsonPost(String url, String paramStr, String postparam,String[] propertys) {
		String[] params = (postparam.equals("")) ? null : new String[] { postparam };
		return sendPostEx(url + (paramStr == "" ? "" : "?" + paramStr), params,propertys,true);
	}
	
	private static CHttpRespons send(String urlString, String method, String[] parameters, String[] propertys)
			throws Exception {
		return send(urlString, method, parameters, propertys, false);
	}

	/**
	 * ����HTTP����
	 * 
	 * @param urlString
	 * @return ��ӳ����
	 * @throws IOException
	 */
	private static CHttpRespons send(String urlString, String method, String[] parameters, String[] propertys,
			boolean useJson) throws Exception {
		HttpURLConnection urlConnection = null;

		try {
			if (method.equalsIgnoreCase("GET") && parameters != null) {
				StringBuffer param = new StringBuffer();
				int i = 0;
				for (; i < parameters.length; i += 2) {
					if (i == 0)
						param.append("?");
					else
						param.append("&");
					param.append(parameters[i]).append("=").append(parameters[i + 1]);
				}
				urlString += param;

			}

			URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod(method);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);

			// urlConnection.connect();
			// urlConnection.setRequestProperty("Accept-Charset", "utf-8");
			if (useJson)
				urlConnection.setRequestProperty("Content-type", "application/json;charset=UTF-8");

			if (propertys != null)
				for (int i = 0; i < propertys.length; i += 2) {
					urlConnection.addRequestProperty(propertys[i], propertys[i + 1]);
				}

			if (method.equalsIgnoreCase("POST") && parameters != null) {
				String postStr = "";
				if (parameters != null) {
					if (useJson||parameters.length==1) {
						postStr = parameters[0];
					} else {
						StringBuffer param = new StringBuffer();
						for (int i = 0; i < parameters.length; i += 2) {
							if (i > 0)
								param.append("&");
							param.append(parameters[i]).append("=").append(parameters[i + 1]);
						}
						postStr = param.toString();
					}
				}

				// System.out.println(param.toString());
				urlConnection.getOutputStream().write(postStr.getBytes("utf-8"));
				urlConnection.getOutputStream().flush();
				urlConnection.getOutputStream().close();

				if (urlConnection.getResponseCode() != 200)
					throw new Exception("�쳣����:" + urlConnection.getResponseCode() + ",�쳣��Ϣ:"
							+ urlConnection.getResponseMessage() + ",ҳ��:" + url);
				/*
				 * PrintWriter out = new PrintWriter(new
				 * OutputStreamWriter(urlConnection.getOutputStream(),"utf-8"));
				 * out.print(param.toString()); out.close();
				 */

			}
		} catch (Exception ex) {
			System.out.println("send=" + ex.getMessage());
			throw ex;
		}

		return makeContent(urlString, urlConnection);
	}

	/**
	 * �õ���Ӧ����
	 * 
	 * @param urlConnection
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	private static CHttpRespons makeContent(String urlString, HttpURLConnection urlConnection) throws IOException {

		CHttpRespons httpResponser = new CHttpRespons();

		try {
			String ecod = urlConnection.getContentEncoding();
			if (ecod == null)
				ecod = defaultContentEncoding;

			InputStream in = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, ecod));

			httpResponser.contentCollection = new Vector<String>();
			StringBuffer temp = new StringBuffer();

			String line = bufferedReader.readLine();
			while (line != null) {
				httpResponser.contentCollection.add(line);
				temp.append(line);// .append("\r\n");
				// temp.append(new String(line.toString().getBytes(), ecod));
				line = bufferedReader.readLine();
			}
			bufferedReader.close();

			httpResponser.urlString = urlString;

			httpResponser.defaultPort = urlConnection.getURL().getDefaultPort();
			httpResponser.file = urlConnection.getURL().getFile();
			httpResponser.host = urlConnection.getURL().getHost();
			httpResponser.path = urlConnection.getURL().getPath();
			httpResponser.port = urlConnection.getURL().getPort();
			httpResponser.protocol = urlConnection.getURL().getProtocol();
			httpResponser.query = urlConnection.getURL().getQuery();
			httpResponser.ref = urlConnection.getURL().getRef();
			httpResponser.userInfo = urlConnection.getURL().getUserInfo();

			// String iso = new
			// String(temp.toString().getBytes("utf-8"),"ISO-8859-1");

			httpResponser.content = temp.toString();// new
													// String(temp.toString().getBytes(),"GBK");

			// httpResponser.content = new
			// String(temp.toString().getBytes("utf-8"), ecod);

			//System.out.println("UrlJson=" + httpResponser.content);

			httpResponser.contentEncoding = ecod;
			httpResponser.code = urlConnection.getResponseCode();
			httpResponser.message = urlConnection.getResponseMessage();
			httpResponser.contentType = urlConnection.getContentType();
			httpResponser.method = urlConnection.getRequestMethod();
			httpResponser.connectTimeout = urlConnection.getConnectTimeout();
			httpResponser.readTimeout = urlConnection.getReadTimeout();

			return httpResponser;
		} catch (IOException e) {
			System.out.println("makeContent\n" + e.getMessage());
			throw e;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}

	/**
	 * Ĭ�ϵ���Ӧ�ַ���
	 */
	public static String getDefaultContentEncoding() {
		return defaultContentEncoding;
	}

	/**
	 * ����Ĭ�ϵ���Ӧ�ַ���
	 */
	public static void setDefaultContentEncoding(String contentEncoding) {
		defaultContentEncoding = contentEncoding;
	}
}
