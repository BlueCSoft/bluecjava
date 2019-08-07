package bluec.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
//import java.util.Map;  

import java.util.*;

public class CURLConnection {
	private String defaultContentEncoding;
	private String charCode = "utf-8";
	
	public int errorCode = 0;      //异常代码
	public String errorMsg = "";   //异常信息
	
	CHttpRespons httpResponser;

	public CURLConnection() {
		this.defaultContentEncoding = Charset.defaultCharset().name();
	}

	public CURLConnection(String charCode) {
		this.defaultContentEncoding = Charset.defaultCharset().name();
		this.charCode = charCode;
	}
	
	public CHttpRespons getHttpResponser() {
		return httpResponser;
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public String sendGet(String urlString) throws Exception {
		String sResult = "";
		try {
			sResult = this.send(urlString, "GET", null, null).content;
		} catch (Exception E) {
			sResult = "{\"yxbz_out\":\"0\",\"fhxx_out\":\"请求("+urlString+")时,出现："
			+ E.getMessage() + "\"}";
		}
		return sResult;
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public String sendGet(String urlString, String[] params) {
		String sResult = "";
		try {
			sResult = this.send(urlString, "GET", params, null).content;
		} catch (Exception E) {
			sResult = "{\"yxbz_out\":\"0\",\"fhxx_out\":\"请求("+urlString+")时,出现："
			+ E.getMessage() + "\"}";
		}
		return sResult;
	}

	public String sendGetFmt(String urlString, String[] params) {
		return CUtil.formatJson(sendGet(urlString, params));
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @param propertys
	 *            请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public String sendGet(String urlString, String[] params, String[] propertys)
			throws Exception {
		return this.send(urlString, "GET", params, propertys).content;
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public String sendPost(String urlString) throws Exception {
		return this.send(urlString, "POST", null, null).content;
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public String sendPost(String urlString, String[] params) throws Exception {
		return this.send(urlString, "POST", params, null).content;
	}

	public String sendPostEx(String urlString, String[] params, boolean useJson) {
		String sResult = "";
		try {
			sResult = this.send(urlString, "POST", params, null,useJson).content;
		} catch (Exception E) {
			sResult = "{\"yxbz_out\":\"0\",\"fhxx_out\":\"请求("+urlString+")时,出现："
			+ E.getMessage() + "\"}";
		}
		return sResult;
	}
	
	public String sendPostEx(String urlString, String[] params) {
	    return sendPostEx(urlString,params,false);
	}

	public String sendPostFmt(String urlString, String[] params, boolean useJson) {
		return CUtil.formatJson(sendPostEx(urlString, params,useJson));
	}

	public String sendPostJsonFmt(String urlString, String param) {
		return CUtil.formatJson(sendPostEx(urlString, new String[]{param},true));
	}
	
	public String sendPostFmt(String urlString, String[] params) {
		return CUtil.formatJson(sendPostEx(urlString, params,false));
	}
	
	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @param propertys
	 *            请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public CHttpRespons sendPost(String urlString, String[] params,
			String[] propertys) throws Exception {
		return this.send(urlString, "POST", params, propertys);
	}

	public String sendPostEx(String urlString, String[] params,
			String[] propertys, boolean useJson) {
		String sResult = "";
		try {
			sResult = this.send(urlString, "POST", params, propertys, useJson).content;
		} catch (Exception E) {
			sResult = "{\"yxbz_out\":\"0\",\"fhxx_out\":\"请求("+urlString+")时,出现："
					+ E.getMessage() + "\"}";
		}
		return sResult;
	}

	public String sendPostEx(String urlString, String[] params,
			String[] propertys) {
		return sendPostEx(urlString, params, propertys, false);
	}

	public String sendPostFmt(String urlString, String[] params,
			String[] propertys, boolean useJson) {
		return CUtil.formatJson(sendPostEx(urlString, params, propertys,
				useJson));
	}

	public String sendPostFmt(String urlString, String[] params,
			String[] propertys) {
		return CUtil
				.formatJson(sendPostEx(urlString, params, propertys, false));
	}

	private CHttpRespons send(String urlString, String method,
			String[] parameters, String[] propertys) throws Exception {
		return send(urlString, method, parameters, propertys, false);
	}

	/**
	 * 发送HTTP请求
	 * 
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	private CHttpRespons send(String urlString, String method,
			String[] parameters, String[] propertys, boolean useJson)
			throws Exception {
		HttpURLConnection urlConnection = null;

		errorCode = -1;
		
		if (method.equalsIgnoreCase("GET") && parameters != null) {
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (; i < parameters.length; i += 2) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(parameters[i]).append("=").append(
						parameters[i + 1]);
			}
			urlString += param;
			
		}

		URL url = new URL(urlString);
		urlConnection = (HttpURLConnection) url.openConnection();

		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		
		// urlConnection.connect();
		urlConnection.setRequestProperty("Accept-Charset", "utf-8");
		if(useJson)
		  urlConnection.setRequestProperty("Content-type","text/json;charset=UTF-8");

		if (propertys != null)
			for (int i = 0; i < propertys.length; i += 2) {
				urlConnection.setRequestProperty(propertys[i], propertys[i + 1]);
			}

		if (method.equalsIgnoreCase("POST") && parameters != null) {
			String postStr = "";
			if (!useJson) {
				StringBuffer param = new StringBuffer();
				for (int i = 0; i < parameters.length; i += 2) {
					if (i > 0)
						param.append("&");
					param.append(parameters[i]).append("=").append(
							parameters[i + 1]);
				}
				postStr = param.toString();
				//System.out.println(postStr);  
			} else {
				/*
				JSONObject json = new JSONObject();
				for (int i = 0; i < parameters.length; i += 2) {
					json.put(parameters[i],CUtil.replaceStr(CUtil.replaceStr(parameters[i+1],"'","\""),"","\r\n"));
				}*/
				
				postStr = parameters[0];
				//System.out.println(postStr);
			}
  
			// System.out.println(param.toString());
			DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
			writer.write(postStr);
			writer.flush();
			writer.close();
			wr.close();
			/*
			int responseCode = urlConnection.getResponseCode();

			InputStream inputStream;

			System.out.println("HttpURLConnection.HTTP_OK="+HttpURLConnection.HTTP_OK);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				inputStream = urlConnection.getErrorStream();
			} else {
				inputStream = urlConnection.getInputStream();
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			httpResponser.content = response.toString();
			*/
			/*
			urlConnection.getOutputStream().write(postStr.getBytes(this.charCode));
			urlConnection.getOutputStream().flush();
			urlConnection.getOutputStream().close();*/

			if (urlConnection.getResponseCode() != 200){
				errorMsg = urlConnection.getResponseMessage();
				throw new Exception("异常代码:" + urlConnection.getResponseCode()
						+ ",异常信息:" + urlConnection.getResponseMessage()
						+ ",页面:" + url);
			} 
			/*
			 * PrintWriter out = new PrintWriter(new
			 * OutputStreamWriter(urlConnection.getOutputStream(),"utf-8"));
			 * out.print(param.toString()); out.close();
			 */

		}

		return this.makeContent(urlString, urlConnection);
	}

	/**
	 * 得到响应对象
	 * 
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private CHttpRespons makeContent(String urlString,
			HttpURLConnection urlConnection) throws IOException {

		httpResponser = new CHttpRespons();

		try {
			String ecod = urlConnection.getContentEncoding();
			if (ecod == null)
				ecod = this.defaultContentEncoding;

			InputStream in = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in, ecod));

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

			httpResponser.content = temp.toString();//new String(temp.toString().getBytes(),"GBK");

			// httpResponser.content = new
			// String(temp.toString().getBytes("utf-8"), ecod);

			System.out.println("UrlJson="+httpResponser.content);

			httpResponser.contentEncoding = ecod;
			httpResponser.code = urlConnection.getResponseCode();
			httpResponser.message = urlConnection.getResponseMessage();
			httpResponser.contentType = urlConnection.getContentType();
			httpResponser.method = urlConnection.getRequestMethod();
			httpResponser.connectTimeout = urlConnection.getConnectTimeout();
			httpResponser.readTimeout = urlConnection.getReadTimeout();
			errorCode = 0;   //成功
			return httpResponser;
		} catch (IOException e) {
			errorMsg = e.getMessage();
			throw e;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}

	/**
	 * 默认的响应字符集
	 */
	public String getDefaultContentEncoding() {
		return this.defaultContentEncoding;
	}

	/**
	 * 设置默认的响应字符集
	 */
	public void setDefaultContentEncoding(String defaultContentEncoding) {
		this.defaultContentEncoding = defaultContentEncoding;
	}
}
