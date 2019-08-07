package bluec.base;

import java.util.Arrays;
import java.util.Enumeration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.lang.model.element.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bluec.base.WxApp.CWxMedia;
import bluec.base.WxApp.CWxMenu;
import bluec.base.WxApp.WxPayData;
import bluec.base.WxApp.WxReqObj;

public class CWeiXin extends CQuery {

	public String gHomeUrl = "http://www.bluecsoft.cn";

	private String gUrl = "http://www.bluecsoft.cn";

	private String gAccessToken = "UlLirF3a92Z3LhM_Qx1dxt1fDlWED3ZIMpLtN1xC_acredea8lvrNKY_Q6WR9zDrfMe2bN7Tbcq23hxyvKgVrQ";
	private int gExpiresin = 7200;

	private String gJsapi_ticket = "";
	private int gJsapi_ticketIn = 7200;

	private String gTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
	private String gWxUrl = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
	private String gWxDoUrl = ""; // ���ںŷ�������ַ

	private String gToken = "my_weixin_token"; // ΢��Token
	private String gWxNo = "likebluec"; // ΢�ź�
	private String gAppId = "";
	private String gAppSecret = "";
	private String gMchId = "";
	private String gKey = "";

	private String gFromUserName = ""; // �����ߵ�΢�ź�
	private String gOpenId = ""; // �����ߵ�openid

	private String gMerchantId = ""; // �̼ұ��
	private String gMerchantUrl = "";
	private Boolean gIsRz = false; // ��ͨ����֤

	private int wxErrorCode = 0; // ΢�Ŵ������
	private String wxErrorMsg = ""; // ΢�Ŵ�����Ϣ

	public String lastInfo = "";
	private String __DwCode = "";
	private String __DcAtt = "0";
	private String __TableId = "";

	private static int DebugStep = 0;
	public boolean isRedirect = false;
	private WxReqObj wxreqobj = null;

	public CWeiXin() {
		gToken = CAppListener.getParam("weixintoken");
		gWxNo = CAppListener.getParam("weixinno");
		gWxDoUrl = CAppListener.getParam("weidourl");
		gHomeUrl = CAppListener.getParam("homeurl");
		gUrl = gHomeUrl;
	}

	private void SetWxReqObj(String MerchantId, Boolean bSet) {
		gMerchantId = MerchantId;

		gMerchantUrl = gHomeUrl + "/mdir/" + gMerchantId + "/";

		try {
			queryBySqlInner("select WxNo,WxToken,Url,AppId,AppSecret,WxKey,MChid,IsRz,PMerchantId,UserParentGzh "
					+ "from wxMerchant where MerchantID='%s'", gMerchantId);

			if (next()) {

				gWxNo = getString("WxNo");
				if (gWxNo.equals(""))
					gWxNo = "likebluec";

				gToken = getString("WxToken");
				gWxDoUrl = getString("Url");
				gAppId = getString("AppId");
				gAppSecret = getString("AppSecret");

				gKey = getString("WxKey");
				gMchId = getString("MChid");
				gIsRz = getInt("IsRz") == 1;

				String pmerid = getString("PMerchantId");
				int un = getInt("UserParentGzh");

				if ((gToken.equals("") || !gIsRz) && !pmerid.equals("") && un == 1) {
					queryBySqlInner(
							"select WxNo,WxToken,Url,AppId,AppSecret " + "from wxMerchant where MerchantID='%s'",
							pmerid);
					if (next()) {
						gWxNo = getString("WxNo");
						if (gWxNo.equals(""))
							gWxNo = "likebluec";

						gToken = getString("WxToken");
						gWxDoUrl = getString("Url");
						gAppId = getString("AppId");
						gAppSecret = getString("AppSecret");
						gIsRz = getInt("IsRz") == 1;
					}
				}
				if (bSet) {
					wxreqobj = new WxReqObj();
					wxreqobj.gWxNo = gWxNo;
					wxreqobj.gToken = gToken;
					wxreqobj.gWxDoUrl = gWxDoUrl;
					wxreqobj.gAppId = gAppId;
					wxreqobj.gAppSecret = gAppSecret;
					wxreqobj.gKey = gKey;
					wxreqobj.gMchId = gMchId;
					wxreqobj.gHomeUrl = gHomeUrl;
					wxreqobj.gMerchantUrl = gMerchantUrl;
					wxreqobj.NOTIFY_URL = gHomeUrl + "/wxmain/wxpaynotify.jsp";
				}
			}
		} catch (SQLException ex) {
			_errorInf = ex.getMessage();
			P("SetWxReqObj:" + _errorInf);
		} finally {
			closeConn();
		}
	}

	public CWeiXin(String MerchantId) {
		gHomeUrl = CAppListener.getParam("homeurl");
		gUrl = gHomeUrl;
		gMerchantId = MerchantId;
	}

	public CWeiXin(HttpServletRequest request) {
		gHomeUrl = CAppListener.getParam("homeurl");
		gUrl = gHomeUrl;
		initRequest(request);
	}

	public CWeiXin(HttpServletRequest request, HttpServletResponse response, String MerchantId, Boolean bGetOpenId) {
		gHomeUrl = CAppListener.getParam("homeurl");
		gUrl = gHomeUrl;

		initRequest(request, response);

		SetWxReqObj(MerchantId, true);
		if (bGetOpenId) {
			try {
				GetOpenIdByPage();
			} catch (Exception ex) {
			}
		}
	}

	public CWeiXin(HttpServletRequest request, HttpServletResponse response, String MerchantId) {
		gHomeUrl = CAppListener.getParam("homeurl");
		gUrl = gHomeUrl;

		initRequest(request, response);

		SetWxReqObj(MerchantId, true);
	}

	public CWeiXin(HttpServletRequest request, HttpServletResponse response) {
		gHomeUrl = CAppListener.getParam("homeurl");
		gUrl = gHomeUrl;

		initRequest(request, response);

		try {
			gOpenId = "";
			P("Hear...=" + DebugStep);
			DebugStep++;
			if (__Request.getParameter("openid") != null) {
				gOpenId = __Request.getParameter("openid").toString();
				if (__Session.getAttribute("gOpenId") == null
						|| !__Session.getAttribute("gOpenId").toString().equals(gOpenId)) {
					__Session.setAttribute("gMerchantId", null);
					__Session.setAttribute("gIsLogin", null);
					__Session.setAttribute("gOpenId", null);
					try {
						executeQuery3("select MerchantID from wxUserInfo where OpenId='%s'", gOpenId);

						next();
						SetWxReqObj(getString("MerchantID"), true);
					} catch (Exception ex) {
					}
				}
			} else {

				if (__Session.getAttribute("gOpenId") != null)
					gOpenId = __Session.getAttribute("gOpenId").toString();

				if (__Session.getAttribute("gMerchantId") != null)
					gMerchantId = __Session.getAttribute("gMerchantId").toString();

				String dMerchantId = CAppListener.getParam("merchantid");
				if (dMerchantId == null)
					dMerchantId = "";

				if (__Request.getParameter("merchantid") != null || !dMerchantId.equals("")) {
					String merid = (__Request.getParameter("merchantid") != null)
							? __Request.getParameter("merchantid").toString() : dMerchantId;

					SetWxReqObj(merid, true);

					if (__Session.getAttribute("gMerchantId") == null
							|| !__Session.getAttribute("gMerchantId").toString().equals(merid)) {
						__Session.setAttribute("gIsLogin", null);
						__Session.setAttribute("gOpenId", null);
						try {
							P("Hear...2=" + DebugStep);
							DebugStep++;
							GetOpenIdByPage();
						} catch (Exception ex) {
						}
					}
				}

			}
			GetOpenidAndAccessTokenTest();

			if (gMerchantId.equals("")) {
				try {
					executeQuery3("select MerchantID from wxUserInfo where OpenId='%s'", gOpenId);
					next();
					gMerchantId = getString("MerchantID");
				} catch (Exception ex) {
				}
			}
			P("Hear...3=" + DebugStep);
			DebugStep++;
			__Session.setAttribute("gMerchantId", gMerchantId);
			__Session.setAttribute(CInitParam.safeField, gOpenId);
			__Session.setAttribute(CInitParam.safeFieldValue, "0");
			__Session.setAttribute("gLoginId", gOpenId);
			__Session.setAttribute("gOpenId", gOpenId);

		} catch (Exception ex) {

		} finally {
			closeConn();
		}
	}

	// �ж��Ƿ��ȡopenid����
	public boolean IsGetOpenIdIng() {
		return __Session == null && __Session.getAttribute("gOpenId") == null
				|| __Session.getAttribute("gOpenId").toString().equals("");
	}

	// �ж��Ƿ���΢�����������
	public boolean IsWxBrowserCall(HttpServletRequest request) {
		return request.getHeader("HTTP_USER_AGENT").indexOf("MicroMessenger") > 0;
	}

	// �ж��Ƿ���΢�ŷ���������
	public boolean IsWxHostCall(HttpServletRequest request) {
		return (request.getParameter("signature") != null) && (request.getParameter("timestamp") != null)
				&& (request.getParameter("nonce") != null);
	}

	/// <summary>
	/// ��֤΢��ǩ��
	/// ��token��timestamp��nonce�������������ֵ�������
	/// �����������ַ���ƴ�ӳ�һ���ַ�������sha1����
	/// �����߻�ü��ܺ���ַ�������signature�Աȣ���ʶ��������Դ��΢�š�
	private boolean CheckSignature(String token) {
		String signature = __Request.getParameter("signature").toString();
		String timestamp = __Request.getParameter("timestamp").toString();
		String nonce = __Request.getParameter("nonce").toString();

		// ����/У�����̣�
		// 1. ��token��timestamp��nonce�������������ֵ�������
		String[] ArrTmp = { token, timestamp, nonce };
		Arrays.sort(ArrTmp);// �ֵ�����

		// 2.�����������ַ���ƴ�ӳ�һ���ַ�������sha1����
		String tmpStr = String.join("", ArrTmp);

		tmpStr = CUtil.SHA1(tmpStr).toLowerCase();

		// 3.�����߻�ü��ܺ���ַ�������signature�Աȣ���ʶ��������Դ��΢�š�

		return tmpStr.equals(signature);
	}

	private boolean CheckSignature() {
		return CheckSignature(gToken);
	}

	/// �����֤
	///
	/// </summary>
	public void Auth(String token) {
		String result = "ʲô��û��.";
		String echoStr = "";
		if (__Request.getParameter("echostr") != null) {
			echoStr = __Request.getParameter("echostr").toString();
			if (CheckSignature(token)) // У��ǩ���Ƿ���ȷ
			{
				if (echoStr != null && !echoStr.equals("")) {
					result = echoStr; // ����ԭֵ��ʾУ��ɹ�
					// __Response.End();
				}
			} else {
				result = "΢����֤:" + token;
				// __Response.End();
			}
		} else {
			outPrint("ʲô��û��.");
		}
		// P("echoStr="+result);
		outln(echoStr);
	}

	public void Auth() {
		Auth(gToken);
	}

	private class RequestXML {
		public String ToUserName = ""; // ��Ϣ���շ�΢�źţ�һ��Ϊ����ƽ̨�˺�΢�ź�
		public String FromUserName = ""; // ��Ϣ���ͷ�΢�ź�openid
		public String CreateTime = ""; // ����ʱ��
		public String MsgType = ""; // ��Ϣ���� ����λ��:location,�ı���Ϣ:text,��Ϣ����:image
		public String Content = ""; // ��Ϣ����
		public String PicUrl = ""; // ͼƬ���ӣ������߿�����HTTP GET��ȡ
		public String MediaId = ""; // ý��ID(ͼƬ������)
		public String Format = ""; // ������ʽ

		public String ThumbMediaId = ""; // ��Ƶ��Ϣ����ͼ��ý��id
		public String Location_X = ""; // ����λ��γ��
		public String Location_Y = ""; // ����λ�þ���

		public String Scale = ""; // ��ͼ���Ŵ�С
		public String Label = ""; // ����λ����Ϣ
		public String Title = ""; // ����ƽ̨��������
		public String Description = ""; // ����ƽ̨��������

		public String Url = ""; // ����ƽ̨��������
		public String Event = ""; // �¼�����
		public String EventKey = ""; // �¼�KEYֵ
		public String Ticket = ""; // ��ά���ticket
		public String Latitude = ""; // ����λ��γ��
		public String Longitude = ""; // ����λ�þ���

		public String Precision = ""; // ����λ�þ���
		public long MsgId = 0; // ��ϢID
	}

	private class UserObject {
		public int subscribe = 0; // ˵��
		public String openid = ""; //
		public String nickname = ""; // �ǳ�
		public String sex = "0"; // �Ա�
		public String language = ""; // ����
		public String city = ""; // ����
		public String province = ""; // ʡ��
		public String country = ""; // ����
		public String headimgurl = ""; // ͷ����Դurl
		public String subscribe_time = ""; // ����ʱ��
		public String unionid = ""; // ����openid
		public String sresult = "";

		public UserObject(String userinfo) {
			try {
				JSONObject reader = new JSONObject(userinfo);

				if (!reader.has("errcode")) {
					subscribe = reader.getInt("subscribe");
					openid = reader.getString("openid");
					nickname = reader.getString("nickname");

					sex = reader.getString("sex");
					language = reader.getString("language");
					city = reader.getString("city");

					province = reader.getString("province");
					country = reader.getString("country");
					headimgurl = reader.getString("headimgurl");

					subscribe_time = reader.getString("subscribe_time");
				}
			} catch (Exception ex) {
				logger.info(ex.getMessage());
			}

		}
	}

	private int AnalyzeErrorMsg(String errorMsg) {
		try {
			JSONObject reader = new JSONObject(errorMsg);

			if (reader.has("errcode")) {
				wxErrorCode = reader.getInt("errcode");
				wxErrorMsg = reader.getString("errmsg");
			} else {
				wxErrorCode = 0;
				wxErrorMsg = errorMsg;
				lastInfo = errorMsg;
			}
		} catch (Exception ex) {
			logger.info(ex.getMessage());
		}
		logger.info(wxErrorMsg + "=" + wxErrorCode);
		return wxErrorCode;
	}

	// ��ȡaccesstoken
	public boolean getAccessToken(Boolean bClose) {
		boolean isYx = true;

		String wxappid, wxappsecret;

		try {
			queryBySqlInner("select AccessToken,AppId,Appsecret, "
					+ "       isYx=case when ExpiresTime is null or ExpiresTime<getdate() then 0 else 1 end "
					+ "from wxMerchant where MerchantID='%s'  ", gMerchantId);

			next();

			gAccessToken = getString("AccessToken");
			wxappid = getString("AppId");
			wxappsecret = getString("Appsecret");

			// logger.info("gAccessToken:" + gMerchantId + ":" + wxappid);

			if (isNullOrEmpty(wxappid)) {
				_errorInf = "δ����AppId";
				return false;
			}

			if (isNullOrEmpty(wxappsecret)) {
				_errorInf = "δ����Appsecret";
				return false;
			}

			isYx = getInt("isYx") == 1;

			if (!isYx) {

				String ToKen = CHttpService.webRequestGet(gTokenUrl,
						"grant_type=client_credential&appid=" + wxappid + "&secret=" + wxappsecret);

				_errorInf = ToKen;
				P(_errorInf);
				try {
					JSONObject reader = new JSONObject(ToKen);

					wxErrorCode = 0;

					if (reader.has("errcode")) {
						wxErrorCode = reader.getInt("errcode");
						wxErrorMsg = reader.getString("errmsg") + gMerchantId;
					} else {
						gAccessToken = reader.getString("access_token");
						gExpiresin = reader.getInt("expires_in");
					}

					if (wxErrorCode == 0) {

						gExpiresin -= 20;

						String[] vparams = { gAccessToken, String.valueOf(gExpiresin), String.valueOf(gExpiresin),
								gMerchantId };

						updateBySqlWithParamInner(
								"update wxMerchant set AccessToken='%s',ExpiresIn=%s,ExpiresTime=dateadd(ss,%s,getdate()) "
										+ "where MerchantID = '%s'",
								vparams);

						isYx = true;
					} else
						_errorInf = "getAccessToken:" + wxErrorMsg;
					lastInfo = wxErrorMsg;
				} catch (Exception ex) {
					logger.info(ex.getMessage());
				}
			}
		} catch (Exception ex) {
			logger.info("getAccessToken : " + ex.getMessage() + "," + _errorInf);
		} finally {
			if (bClose || !isYx)
				closeConn();
		}
		return isYx;
	}

	public boolean getAccessToken() {
		return getAccessToken(true);
	}

	public String getJsapi_ticket() {
		boolean isYx = true;
		gJsapi_ticket = "";

		if (getAccessToken(false)) {
			try {
				queryBySqlInner(
						"select Jsapi_ticket,isYx=case when isnull(Jsapi_ticket,'')='' or Jsapi_ticketTime<getdate() then 0 else 1 end "
								+ "from wxMerchant where MerchantID='%s' ",
						gMerchantId);

				next();

				gJsapi_ticket = getString("Jsapi_ticket");

				isYx = getInt("isYx") == 1;

				if (!isYx) {

					String ToKen = CHttpService.webRequestGet("https://api.weixin.qq.com/cgi-bin/ticket/getticket",
							"access_token=" + gAccessToken + "&type=jsapi");

					_errorInf = ToKen;

					JSONObject reader = new JSONObject(ToKen);

					wxErrorCode = 0;

					if (reader.has("errcode")) {
						wxErrorCode = reader.getInt("errcode");
						wxErrorMsg = reader.getString("errmsg");
					}

					if (wxErrorCode == 0) {

						gJsapi_ticket = reader.getString("ticket");
						gJsapi_ticketIn = reader.getInt("expires_in");

						gJsapi_ticketIn -= 20;

						String[] vparams = { gJsapi_ticket, gJsapi_ticketIn + "", gJsapi_ticketIn + "", gMerchantId };

						updateBySqlWithParamInner(
								"update wxMerchant set Jsapi_ticket='%s',Jsapi_ticketIn=%s,Jsapi_ticketTime=dateadd(ss,%s,getdate()) "
										+ "where MerchantID = '%s'",
								vparams);

						logger.info("getJsapi_ticketUpdate : " + gJsapi_ticket);

						isYx = true;
					} else
						_errorInf = "getJsapi_ticket:" + wxErrorMsg;

				}
			} catch (Exception ex) {
				logger.info("getJsapi_ticket : " + ex.getMessage() + "," + _errorInf);
			} finally {
				closeConn();
			}
		}
		return gJsapi_ticket;
	}

	/**
	 * 
	 * ��ȡ�ջ���ַjs������ڲ���,������ο��ջ���ַ����ӿڣ�http://pay.weixin.qq.com/wiki/doc/api/jsapi.
	 * php?chapter=7_9
	 * 
	 * @return String �����ջ���ַjs������Ҫ�Ĳ�����json��ʽ����ֱ��������ʹ��
	 */
	public String GetEditAddressParameters() throws Exception {
		String parameter = "";
		try {
			String host = __Request.getRemoteHost();
			String path = __Request.getRequestURI();
			String queryString = __Request.getQueryString();
			// ����ط�Ҫע�⣬����ǩ��������ҳ��Ȩ��ȡ�û���Ϣʱ΢�ź�̨�ش�������url
			String url = "http://" + host + path + queryString;

			// ������Ҫ��SHA1�㷨���ܵ�����
			WxPayData signData = new WxPayData();
			signData.SetValue("appid", gAppId);
			signData.SetValue("url", url);
			signData.SetValue("timestamp", CUtil.GenerateTimeStamp());
			signData.SetValue("noncestr", CUtil.GenerateNonceStr());
			signData.SetValue("accesstoken", gAccessToken);
			String param = signData.ToUrl();

			logger.info("SHA1 encrypt param : " + param);
			// SHA1����
			String addrSign = CUtil.SHA1(param);
			logger.info("SHA1 encrypt result : " + addrSign);

			// ��ȡ�ջ���ַjs������ڲ���
			WxPayData afterData = new WxPayData();
			afterData.SetValue("appId", gAppId);
			afterData.SetValue("scope", "jsapi_address");
			afterData.SetValue("signType", "sha1");
			afterData.SetValue("addrSign", addrSign);
			afterData.SetValue("timeStamp", signData.GetValue("timestamp"));
			afterData.SetValue("nonceStr", signData.GetValue("noncestr"));

			// תΪjson��ʽ
			parameter = afterData.ToJson();
		} catch (Exception ex) {
			// Log.Error(this.GetType().toString(), ex.toString());
			throw new Exception(ex.toString());
		}

		return parameter;
	}

	/**
	 * 
	 * ͨ��code��ȡ��ҳ��Ȩaccess_token��openid�ķ������ݣ���ȷʱ���ص�JSON���ݰ����£� {
	 * "access_token":"ACCESS_TOKEN", "expires_in":7200,
	 * "refresh_token":"REFRESH_TOKEN", "openid":"OPENID", "scope":"SCOPE",
	 * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL" } ����access_token�����ڻ�ȡ�����ջ���ַ
	 * openid��΢��֧��jsapi֧���ӿ�ͳһ�µ�ʱ����Ĳ���
	 * ����ϸ��˵����ο���ҳ��Ȩ��ȡ�û�������Ϣ��http://mp.weixin.qq.com/wiki/17/
	 * c0f37d5704f0b64713d5d2c37b468d75.html
	 * 
	 * @ʧ��ʱ���쳣WxPayException
	 */
	public void GetOpenidAndAccessTokenFromCode(String code) throws Exception {
		String surl = "";
		try {
			// �����ȡopenid��access_token��url
			WxPayData data = new WxPayData();
			data.SetValue("appid", gAppId);
			data.SetValue("secret", gAppSecret);
			data.SetValue("code", code);
			data.SetValue("grant_type", "authorization_code");

			String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" + data.ToUrl();
			surl = url;
			logger.info("Get code : " + url);
			// ����url�Ի�ȡ����
			// String result = HttpService.Get(url);
			String result = CHttpService.webRequestGet(url, "");

			logger.info("GetOpenidAndAccessTokenFromCode response : " + result);

			// ����access_token�������ջ���ַ��ȡ
			JSONObject jd = new JSONObject(result);
			gAccessToken = jd.getString("access_token");

			// ��ȡ�û�openid
			gOpenId = jd.getString("openid");
			__Session.setAttribute("gOpenId", gOpenId);

			executeMsSqlProc("SaveUserInfo", new String[] { gMerchantId, gOpenId });

		} catch (Exception ex) {
			// Log.Error(this.GetType().toString(), ex.toString());
			logger.info("GetOpenidAndAccessTokenFromCode : " + ex.getMessage());
			throw new Exception(ex.toString());
		}
	}

	public boolean GetOpenidAndAccessTokenTest() throws Exception {
		boolean result = false;
		if (!isNullOrEmpty(__Request.getParameter("code"))) {
			// ��ȡcode�룬�Ի�ȡopenid��access_token
			String code = __Request.getParameter("code").toString();
			logger.info("Get code : " + code);
			GetOpenidAndAccessTokenFromCode(code);
			result = true;
		}
		return result;
	}

	/**
	 * 
	 * ��ҳ��Ȩ��ȡ�û�������Ϣ��ȫ������ ������ο���ҳ��Ȩ��ȡ�û�������Ϣ��http://mp.weixin.qq.com/wiki/17/
	 * c0f37d5704f0b64713d5d2c37b468d75.html ��һ��������url��ת��ȡcode
	 * �ڶ���������codeȥ��ȡopenid��access_token
	 * 
	 */

	public boolean GetOpenidAndAccessToken() throws Exception {
		System.out.println("GetOpenidAndAccessToken");
		boolean result = false;
		if (!isNullOrEmpty(__Request.getParameter("code"))) {
			// ��ȡcode�룬�Ի�ȡopenid��access_token
			String code = __Request.getParameter("code").toString();
			logger.info("Get code : " + code);
			GetOpenidAndAccessTokenFromCode(code);
			result = true;
		} else {
			// ������ҳ��Ȩ��ȡcode��URL
			String host = __Request.getRemoteHost();
			String path = __Request.getContextPath();
			String url = __Request.getRequestURL().toString();

			// ��������̼Ҳ����������̼Ҳ���
			if (url.indexOf("merchantid=") == -1)
				url = url + ((url.indexOf("?") > -1) ? "&merchantid=" + gMerchantId : "?merchantid=" + gMerchantId);

			if (url.indexOf("dwcode=") == -1)
				url += "&dwcode=" + __DwCode + "&dcatt=" + __DcAtt + "&tableid=" + __TableId;

			logger.info("SRC URL : " + url);

			System.out.println(url);
			String redirect_uri = URLEncoder.encode(url, "utf-8"); // "http://"
																	// + host +
																	// path+"?merchantid="+gMerchantId);
			System.out.println(redirect_uri);
			WxPayData data = new WxPayData();
			data.SetValue("appid", gAppId);
			data.SetValue("redirect_uri", redirect_uri);
			data.SetValue("response_type", "code");
			data.SetValue("scope", "snsapi_userinfo");
			/*
			 * if(gIsRz) data.SetValue("scope", "snsapi_userinfo");
			 * //snsapi_base,snsapi_userinfo else data.SetValue("scope",
			 * "snsapi_base");
			 */
			data.SetValue("state", "STATE" + "#wechat_redirect");

			url = "https://open.weixin.qq.com/connect/oauth2/authorize?" + data.ToUrl();
			P("Will Redirect to URL : " + url);

			try {
				// ����΢�ŷ���code��
				isRedirect = true;
				__Response.sendRedirect(url);// Redirect�������׳�ThreadAbortException�쳣�����ô�������쳣
			} catch (Exception ex) {
				logger.info("GetOpenidAndAccessToken : " + ex.getMessage());
			}
		}
		return result;
	}

	public void GetOpenIdByPage() {
		try {
			if (__Request.getParameter("openid") != null)
				gOpenId = __Request.getParameter("openid").toString();
			else if (__Session.getAttribute("gOpenId") != null)
				gOpenId = __Session.getAttribute("gOpenId").toString();
			else {
				__DwCode = (__Request.getParameter("dwcode") != null) ? __Request.getParameter("dwcode").toString()
						: "";
				__DcAtt = (__Request.getParameter("dcatt") != null) ? __Request.getParameter("dcatt").toString() : "";
				__TableId = (__Request.getParameter("tableid") != null) ? __Request.getParameter("tableid").toString()
						: "";
				GetOpenidAndAccessToken();
			}
		} catch (Exception ex) {
			P("GetOpenIdByPage:" + ex.getMessage());
		}
	}

	/*
	 * ΢�Ų˵����� �����û��Զ���˵�
	 */
	private int CreateUserMenu(String merchantid) {
		int nResult = -1;

		gMerchantId = merchantid;

		if (getAccessToken()) {
			CWxMenu wxmenu = new CWxMenu(wxreqobj);

			wxreqobj.gMerchantId = gMerchantId;
			wxreqobj.gAccessToken = gAccessToken;

			String sResult = wxmenu.CreateUserMenu();

			logger.info("CreateUserMenu:" + sResult);
			nResult = AnalyzeErrorMsg(sResult);

			lastInfo = wxreqobj.gMsg;
		}
		return nResult;
	}

	/*
	 * ��ѯ�û��Զ���˵�
	 */
	public int QueryUserMenu() {
		int nResult = -1;

		if (getAccessToken()) {
			CWxMenu wxmenu = new CWxMenu(wxreqobj);

			wxreqobj.gMerchantId = gMerchantId;
			wxreqobj.gAccessToken = gAccessToken;

			// logger.info("QueryUserMenu:"+gAccessToken);
			String sResult = wxmenu.QueryUserMenu();
			// logger.info("QueryUserMenu:"+sResult);

			nResult = AnalyzeErrorMsg(sResult);

		}
		return nResult;
	}

	/*
	 * ɾ���û��Զ���˵�
	 */
	public int DeleteUserMenu() {
		int nResult = -1;

		if (getAccessToken()) {
			CWxMenu wxmenu = new CWxMenu(wxreqobj);

			wxreqobj.gMerchantId = gMerchantId;
			wxreqobj.gAccessToken = gAccessToken;

			String sResult = wxmenu.DeleteUserMenu();

			nResult = AnalyzeErrorMsg(sResult);

		}
		return nResult;
	}
	/// <summary>
	/// ���ðٶȵ�ͼ������������Ϣ
	/// </summary>
	/// <param name="y">����</param>
	/// <param name="x">γ��</param>
	/// <returns></returns>

	private String GetMapInfo(String x, String y) {

		try {
			String parame = "";
			String url = "http://maps.googleapis.com/maps/api/geocode/xml";

			parame = "latlng=" + x + "," + y + "&language=zh-CN&sensor=false";

			String res = CHttpService.webRequestPost(url, "", parame);

			InputStream in = new ByteArrayInputStream(res.getBytes("UTF-8"));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(in); // �����ĵ�

			Element rootElement = (Element) doc.getDocumentElement();

			String Status = doc.getElementById("status").getNodeValue();

			if (Status.equals("OK")) { // ����ȡ����
				NodeList xmlResults = doc.getElementById("/GeocodeResponse").getChildNodes();

				for (int i = 0; i < xmlResults.getLength(); i++) {
					Node childNode = xmlResults.item(i);
					if (childNode.getNodeName().equals("status")) {
						continue;
					}
					String city = "0";
					NodeList wNodeList = childNode.getChildNodes();

					for (int w = 0; w < wNodeList.getLength(); w++) {
						Node wNode = wNodeList.item(w);
						for (int q = 0; q < wNode.getChildNodes().getLength(); q++) {
							Node childeTwo = wNode.getChildNodes().item(q);
							if (childeTwo.getNodeName().equals("long_name")) {
								city = childeTwo.getNodeValue();
							} else if (childeTwo.getNodeValue().equals("locality")) {
								return city;
							}
						}
					}
					return city;
				}
			}
		} catch (Exception ex) {
			// WriteTxt("map�쳣:" + ex.Message.toString() + "Struck:" +
			// ex.StackTrace.toString());
			return ex.getMessage();
		}
		return "���ڻ�����?";
	}

	public void GetUserInfo() {

		if (getAccessToken()) {

			String userinfo = CHttpService.webRequestGet("https://api.weixin.qq.com/cgi-bin/user/info",
					"access_token=" + gAccessToken + "&openid=" + gOpenId + "&lang=zh_CN");

			UserObject userObject = new UserObject(userinfo);

			String[] vparams = { gMerchantId, gOpenId, userObject.subscribe + "", userObject.nickname, userObject.sex,
					userObject.language, userObject.city, userObject.province, userObject.country,
					userObject.headimgurl, userObject.subscribe_time, userObject.unionid, userObject.sresult };

			// Log.Debug(this.GetType().toString(), "gOpenId : " + gOpenId);

			executeMsSqlProc("SaveUserInfoMx", vparams);

		}
	}

	public String GetOpenIdList(String merchantid, String nextopenid) {
		String result = "";
		gMerchantId = merchantid;
		if (getAccessToken()) {
			result = CHttpService.webRequestGet("https://api.weixin.qq.com/cgi-bin/user/get",
					"access_token=" + gAccessToken + "&next_openid=" + nextopenid);
		}
		return CUtil.formatJson(result);
	}

	public String GetUserInfo(String merchantid, String openid) {
		String result = "";
		gMerchantId = merchantid;
		if (getAccessToken()) {
			result = CHttpService.webRequestGet("https://api.weixin.qq.com/cgi-bin/user/info",
					"access_token=" + gAccessToken + "&openid=" + openid + "&lang=zh_CN");
		}
		return CUtil.formatJson(result);
	}

	/// �Ǽǹ�ע��openid

	private void SaveUserInfo() {
		String[] vparams = { gMerchantId, gOpenId };
		executeMsSqlProc("SaveUserInfo", vparams);
	}

	/// <summary>
	/// ����΢�Ź�ע��ķ��ص���Ϣ
	/// </summary>
	/// <param name="requestXML">�������</param>
	/// <returns>���ؽ���ַ���</returns>

	private String GetRealUrl(String CType, String rurl) {
		String url = rurl;
		switch (CType) {
		case "ABOUT_HTML":
			url = gMerchantUrl + rurl + "&openid=" + gFromUserName;
			break;
		case "VW_URL":
			break;
		case "VR_START":
			url = gHomeUrl + "/wxmain/wxstart.jsp?openid=" + gFromUserName;
			break;
		case "VR_IMQUERY":
			url = gHomeUrl + "/wxtools/wximquery.jsp?openid=" + gFromUserName;
			break;
		case "CR_VIP_MEMBER_BILL":
			url = gHomeUrl + "/wxmain/wxvipbills.jsp?openid=" + gFromUserName;
			break;
		case "VR_VIP":
			url = gHomeUrl + "/wxmain/wxvip.jsp?openid=" + gFromUserName;
			break;
		case "CR_VIP_MEMBER_CZ":
			url = gHomeUrl + "/wxmain/wxvipczlist.jsp?openid=" + gFromUserName;
			break;
		case "CR_VIP_MEMBER_INFO":
			url = gHomeUrl + "/wxmain/wxvipinfo.jsp?openid=" + gFromUserName;
			break;
		case "CR_VIP_MEMBER_OP":
			url = gHomeUrl + "/wxmain/wxvip.jsp?openid=" + gFromUserName;
			break;
		default:
			if (CType.substring(0, 6).equals("VR_DEF")) {
				try {
					executeQuery3("select url from wx_MenuType where SNo='%s'", CType);
					if (next()) {
						url = getString("URL");
						url = CUtil.formatStr(url, gMerchantId, "{merchantid}");
					}
				} catch (Exception ex) {

				}
			}
			break;
		}
		return url;
	}

	private String WelcomeBlueC(RequestXML requestXML, int nitem) {
		StringBuilder sResult = new StringBuilder();
		try {
			queryBySqlInner("select WxDir,MerchantName,LogoFile0,WelCome1,WelCome2,WxDir "
					+ "from wxMerchant where MerchantId='%s'", gMerchantId);

			if (next()) {
				String mername = getString("MerchantName");
				String picurl = gUrl + getString("LogoFile0");
				String title = getString("WelCome1");
				String desc = getString("WelCome2");
				String url = getString("WxDir");

				nitem = 1;

				sResult.append("<xml>");
				sResult.append("<ToUserName><![CDATA[" + requestXML.FromUserName + "]]></ToUserName>");
				sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
				sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
				sResult.append("<MsgType><![CDATA[news]]></MsgType>");
				sResult.append("<ArticleCount>" + nitem + "</ArticleCount>");
				sResult.append("<Articles>");

				sResult.append("<item>");
				sResult.append("<Title><![CDATA[" + title + "]]></Title>");
				sResult.append("<Description><![CDATA[" + desc + "]]></Description>");
				sResult.append("<PicUrl><![CDATA[" + picurl + "]]></PicUrl>");
				sResult.append("<Url><![CDATA[" + url + "]]></Url>");
				sResult.append("</item>");

				sResult.append("</Articles>");
				sResult.append("</xml>");

				// logger.info("WelCome\n" + sResult.toString());
			} else {
				sResult.append("<xml>");
				sResult.append("<ToUserName><![CDATA[" + requestXML.FromUserName + "]]></ToUserName>");
				sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
				sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
				sResult.append("<MsgType><![CDATA[text]]></MsgType>");
				sResult.append("<Content><![CDATA[��ӭ����]]></Content>");
				sResult.append("</xml>");
			}
		} catch (Exception ex) {

		}
		return sResult.toString();
	}

	private String doReturn(RequestXML requestXML, String KeyCode) {

		String fid = KeyCode.substring(10);

		StringBuilder sResult = new StringBuilder();

		try {
			String[] vparams = { gMerchantId, fid };

			queryBySqlInner("select pagename from wx_Menu where MerchantId='%s' and id=%s", vparams);
			String content = next() ? getStringX("pagename") : "δ���ûظ�����";

			sResult.append("<xml>");
			sResult.append("<ToUserName><![CDATA[" + requestXML.FromUserName + "]]></ToUserName>");
			sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
			sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
			sResult.append("<MsgType><![CDATA[text]]></MsgType>");
			sResult.append("<Content><![CDATA[" + content + "]]></Content>");
			sResult.append("</xml>");
		} catch (Exception ex) {

		} finally {
			closeConn();
		}
		return sResult.toString();
	}

	/**
	 * ��Ӧclick�¼�
	 */
	private String VipDoClick(String keyStr) {
		StringBuilder sResult = new StringBuilder();

		String url = "";
		String stitle = "";
		String sdescription = "";
		String spic = "";

		String[] vparams = { gOpenId, gMerchantId, "" };

		try {
			sResult.append("<xml>");
			sResult.append("<ToUserName><![CDATA[" + gOpenId + "]]></ToUserName>");
			sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
			sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
			sResult.append("<MsgType><![CDATA[news]]></MsgType>");
			sResult.append("<ArticleCount>1</ArticleCount>");
			sResult.append("<Articles>");
			sResult.append("<item>");

			logger.info("VipClick return1:\n" + sResult.toString());

			queryBySqlIDwithParamInner("VIP_MEMBER_EXISTS", vparams);
			if (!next()) // ��Ա������
			{
				queryBySqlIDwithParamInner("CR_VIP_MEMBER_INFO_NO", vparams);
				next();
				stitle = getString("title");
				sdescription = getString("description");
				spic = getString("pic");
				url = getString("url");
			} else {
				queryBySqlIDwithParamInner(keyStr + "_OK", vparams);

				if (!next()) {
					queryBySqlIDwithParamInner(keyStr + "_NO", vparams);
					next();
				}
				stitle = getString("title");
				sdescription = getStringX("description");
				spic = getString("pic");
				url = getString("url");
			}

			queryBySqlInner(" select top 1 HEADPHOTO from wxCatalog where CTYPE='%s'", keyStr);
			if (next() && !getString("HEADPHOTO").equals(""))
				spic = gUrl + getString("HEADPHOTO");
			else
				spic = gHomeUrl + spic;

			if (url.indexOf("http://") != 0)
				url = gHomeUrl + url + "?openid=" + gOpenId;

			sResult.append("<Title><![CDATA[" + stitle + "]]></Title>");
			sResult.append("<Description><![CDATA[" + sdescription + "]]></Description>");
			sResult.append("<PicUrl><![CDATA[" + spic + "]]></PicUrl>");

			sResult.append("<Url><![CDATA[" + url + "]]></Url>");

			sResult.append("</item>");
			sResult.append("</Articles>");
			sResult.append("</xml>");
		} catch (Exception ex) {

		}
		logger.info("VipClick return:\n" + sResult.toString());

		return sResult.toString();
	}

	/// <summary>
	/// ��Ϣ�ظ�(΢����Ϣ����)
	/// </summary>
	/// <param name="requestXML">The request XML.</param>

	private void ResponseMsg(RequestXML requestXML) {
		StringBuilder sResult = new StringBuilder();
		String sStr = "";
		logger.info("start:" + requestXML.FromUserName + "," + requestXML.MsgType);
		String url = (gWxDoUrl.equals("")) ? __Request.getRemoteHost() : gWxDoUrl;

		switch (requestXML.MsgType) {
		case "text": // �ı���Ϣ
			// GetUserInfo();
			sStr = WelcomeBlueC(requestXML, 1);
			break;

		case "location": // λ��
			sResult.append("<xml>");
			sResult.append("<ToUserName><![CDATA[" + requestXML.FromUserName + "]]></ToUserName>");
			sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
			sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
			sResult.append("<MsgType><![CDATA[text]]></MsgType>");
			sResult.append(
					"<Content><![CDATA[" + GetMapInfo(requestXML.Location_X, requestXML.Location_Y) + "]]></Content>");
			sResult.append("</xml>");
			sStr = sResult.toString();
			break;

		case "event":
			switch (requestXML.Event) {
			case "subscribe":
				GetUserInfo();
				sStr = WelcomeBlueC(requestXML, 5); // �����¼�
				break;
			case "SCAN":
				break;
			case "LOCATION":
				String[] vparams = { gOpenId, requestXML.Latitude, requestXML.Longitude, requestXML.Precision };
				executeMsSqlProc("WxUserLocation", vparams);
				break;
			case "CLICK":
				P("CLICK KEY:\n" + requestXML.EventKey);
				switch (requestXML.EventKey) {
				case "VR_MAIN":
					sStr = WelcomeBlueC(requestXML, 1);
					break;
				default:
					if (requestXML.EventKey.indexOf("CW_RETURN#") == 0)
						sStr = doReturn(requestXML, requestXML.EventKey);
					else
						sStr = VipDoClick(requestXML.EventKey);
					break;

				}
				break;
			case "VIEW":
				break;
			}
			break;
		}

		logger.info("end:" + sStr);
		outPrint(sStr);
		// __Response.End();
	}

	private void wxHandle(String postStr) {
		try {
			// P(postStr);
			InputStream in = new ByteArrayInputStream(postStr.getBytes("utf-8"));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(in); // �����ĵ�

			// MsgType
			Node MsgType = doc.getElementsByTagName("MsgType").item(0).getFirstChild();

			// ���յ�ֵ--->������Ϣ��(Ҳ��Ϊ��Ϣ����)
			RequestXML requestXML = new RequestXML();

			requestXML.MsgType = MsgType.getNodeValue();
			requestXML.ToUserName = doc.getElementsByTagName("ToUserName").item(0).getFirstChild().getNodeValue();
			requestXML.FromUserName = doc.getElementsByTagName("FromUserName").item(0).getFirstChild().getNodeValue();

			gOpenId = requestXML.FromUserName;
			gFromUserName = requestXML.FromUserName;

			requestXML.CreateTime = doc.getElementsByTagName("CreateTime").item(0).getFirstChild().getNodeValue();

			P("MsgType=" + requestXML.MsgType);
			// ���ݲ�ͬ�����ͽ��в�ͬ�Ĵ���

			switch (requestXML.MsgType) {
			case "text": // �ı���Ϣ
				requestXML.Content = doc.getElementsByTagName("Content").item(0).getFirstChild().getNodeValue();
				break;
			case "image": // ͼƬ
				requestXML.PicUrl = doc.getElementsByTagName("PicUrl").item(0).getFirstChild().getNodeValue();
				requestXML.MediaId = doc.getElementsByTagName("MediaId").item(0).getFirstChild().getNodeValue();
				break;
			case "voice": // ����
				requestXML.MediaId = doc.getElementsByTagName("MediaId").item(0).getFirstChild().getNodeValue();
				requestXML.Format = doc.getElementsByTagName("Format").item(0).getFirstChild().getNodeValue();
				break;
			case "video":
				requestXML.MediaId = doc.getElementsByTagName("MediaId").item(0).getFirstChild().getNodeValue();
				requestXML.ThumbMediaId = doc.getElementsByTagName("ThumbMediaId").item(0).getFirstChild()
						.getNodeValue();
				break;
			case "location": // λ��
				requestXML.Location_X = doc.getElementsByTagName("Location_X").item(0).getFirstChild().getNodeValue();
				requestXML.Location_Y = doc.getElementsByTagName("Location_Y").item(0).getFirstChild().getNodeValue();
				requestXML.Scale = doc.getElementsByTagName("Scale").item(0).getFirstChild().getNodeValue();
				requestXML.Label = doc.getElementsByTagName("Label").item(0).getFirstChild().getNodeValue();
				break;
			case "link": // ����
				requestXML.Title = doc.getElementsByTagName("Title").item(0).getFirstChild().getNodeValue();
				requestXML.Description = doc.getElementsByTagName("Description").item(0).getFirstChild().getNodeValue();
				requestXML.Url = doc.getElementsByTagName("Url").item(0).getFirstChild().getNodeValue();
				break;
			case "event": // �¼�����
				requestXML.Event = doc.getElementsByTagName("Event").item(0).getFirstChild().getNodeValue();
				switch (requestXML.Event) {
				case "subscribe":
					break;
				case "SCAN":
					requestXML.EventKey = doc.getElementsByTagName("EventKey").item(0).getFirstChild().getNodeValue();
					requestXML.Ticket = doc.getElementsByTagName("Ticket").item(0).getFirstChild().getNodeValue();
					break;
				case "LOCATION":
					requestXML.Latitude = doc.getElementsByTagName("Latitude").item(0).getFirstChild().getNodeValue();
					requestXML.Longitude = doc.getElementsByTagName("Longitude").item(0).getFirstChild().getNodeValue();
					requestXML.Precision = doc.getElementsByTagName("Precision").item(0).getFirstChild().getNodeValue();
					break;
				case "CLICK":
					requestXML.EventKey = doc.getElementsByTagName("EventKey").item(0).getFirstChild().getNodeValue();
					break;
				case "VIEW":
					requestXML.EventKey = doc.getElementsByTagName("EventKey").item(0).getFirstChild().getNodeValue();
					break;
				}
				break;
			}
			if (CheckSignature()) {
				ResponseMsg(requestXML);
			} else {
				StringBuilder sResult = new StringBuilder();
				sResult.append("<xml>");
				sResult.append("<ToUserName><![CDATA[" + gFromUserName + "]]></ToUserName>");
				sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
				sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
				sResult.append("<MsgType><![CDATA[text]]></MsgType>");
				sResult.append("<Content><![CDATA[�����֤����Ӵ(" + gToken + ")��]]></Content>");
				sResult.append("</xml>");
				outPrint(sResult.toString());
				P(sResult.toString());
			}
		} catch (Exception ex) {
			P("wxHandle:" + ex.getMessage());
		}
	}

	public void processRequest(HttpServletRequest request, HttpServletResponse response, String MerchantId) {
		initRequest(request, response);

		SetWxReqObj(MerchantId, false);

		gMerchantUrl = gHomeUrl + "/mdir/" + gMerchantId + "/";

		// P(gMerchantUrl);
		try {
			if (__Request.getParameter("code") != null) // ��ҳ��Ȩ����
			{
				if (__Session.getAttribute("gMerchantId") != null
						&& !__Session.getAttribute("gMerchantId").toString().equals(gMerchantId)) {

					@SuppressWarnings("unchecked")
					Enumeration<String> em = (Enumeration<String>) __Session.getAttributeNames();
					while (em.hasMoreElements()) {
						__Session.removeAttribute(em.nextElement().toString());
					}

				}

				if (__Session.getAttribute("gOpenId") == null
						|| __Session.getAttribute("gOpenId").toString().equals("")) {
					GetOpenIdByPage();
					if (!isRedirect) {
						SaveUserInfo();
					}
				} else
					gOpenId = __Session.getAttribute("gOpenId").toString();

				if (!isRedirect) {
					__Session.setAttribute("glastInfo", lastInfo);

					__Session.setAttribute("gdwcode", null);
					__Session.setAttribute("gtableid", null);
					if (__Request.getParameter("dwcode") != null) {
						__Response.sendRedirect(gHomeUrl + "/wxmain/wxstart.jsp?openid=" + gOpenId + "&dwcode="
								+ __Request.getParameter("dwcode").toString());
					} else {
						__Response.sendRedirect(gHomeUrl + "/wxmain/wxstart.jsp?openid=" + gOpenId);
					}
					isRedirect = true;
				}
			} else {

				if (__Request.getMethod().toUpperCase().equals("GET")) {
					Auth();
				} else if (__Request.getMethod().toUpperCase().equals("POST")) {

					BufferedReader in = new BufferedReader(new InputStreamReader(__Request.getInputStream(), "utf-8"));

					String line = null;
					String postStr = "";
					while ((line = in.readLine()) != null) {
						postStr += line;
					}
					// P(postStr);
					if (!isNullOrEmpty(postStr)) // ������
					{
						try {
							// P(postStr);
							wxHandle(postStr);
						} catch (Exception ex) {
							StringBuilder sResult = new StringBuilder();
							sResult.append("<xml>");
							sResult.append("<ToUserName><![CDATA[" + gFromUserName + "]]></ToUserName>");
							sResult.append("<FromUserName><![CDATA[" + gWxNo + "]]></FromUserName>");
							sResult.append("<CreateTime>" + CUtil.getOrigTime() + "</CreateTime>");
							sResult.append("<MsgType><![CDATA[text]]></MsgType>");
							sResult.append("<Content><![CDATA[������Ӵ:" + ex.getMessage() + "(" + ex.toString()
									+ ")]]></Content>");
							sResult.append("</xml>");
							outPrint(sResult.toString());
							// __Response.End();
						}

					}
				}
			}
		} catch (Exception ex) {
			logger.info("processRequest:" + ex.getMessage());

		} finally {
			closeConn();
		}
	}

	// ִ����Ϣ����
	private Boolean doSendMsg(String openid, String msgType, String msgStr) {
		StringBuilder buf = new StringBuilder();

		if (getAccessToken()) {
			P(msgStr);
			switch (msgType) {
			case "text":
				buf.append("{");
				buf.append("\"touser\": \"" + openid + "\",");
				buf.append("\"msgtype\": \"text\",");
				buf.append("\"text\": {");
				buf.append("\"content\": \"" + msgStr + "\"");
				buf.append("}");
				buf.append("}");
				break;
			}

			String result = CHttpService.webRequestPost(gWxUrl, "access_token=" + gAccessToken, buf.toString());
			_errorInf = result;
			AnalyzeErrorMsg(result);

			// _errorInf = wxErrorMsg;

			return wxErrorCode == 0;
		}
		return false;
	}

	private void doBatchSendMsg(String datasql, String successSql, String failSql) {
		if (getAccessToken()) {
			StringBuilder buf = new StringBuilder();
			StringBuilder sqlBuf = new StringBuilder();

			buf.append("{");
			buf.append("\"touser\": \"%s\",");
			buf.append("\"msgtype\": \"text\",");
			buf.append("\"text\": {");
			buf.append("\"content\": \"%s\"");
			buf.append("}");
			buf.append("}");
			String tmp = buf.toString();

			try {
				executeQuery(datasql);
				while (next()) {
					String text = CUtil.formatStr(tmp, new String[] { getString(1), getString(2) }, "%s");
					AnalyzeErrorMsg(CHttpService.webRequestPost(gWxUrl, "access_token=" + gAccessToken, text));
					sqlBuf.append(
							CUtil.formatStr((wxErrorCode == 0) ? successSql : failSql, getString(0), "%s") + "\n");
				}
				if (sqlBuf.length() > 0) {
					executeUpdate(sqlBuf.toString());
				}
			} catch (Exception ex) {

			} finally {
				closeConn();
			}
		}
	}

	// �����ı���Ϣ
	public Boolean SendText(String merchantid, String openid, String msgStr) {
		gMerchantId = merchantid;
		return doSendMsg(openid, "text", msgStr);
	}

	public void BatchSendText(String merchantid, String dataSql, String successSql, String failSql) {
		gMerchantId = merchantid;
		doBatchSendMsg(dataSql, successSql, failSql);
	}

	// ������Ϣ����Ա
	public Boolean SendMassageToVip(String merchantid, String vipid, String msgStr) {
		gMerchantId = merchantid;

		String[] vparams = { merchantid, vipid };
		try {
			executeQuery3("select OpenId from wxUserInfo " + "where Merchantid='%s' and MemberId='%s'", vparams);

			if (next()) {
				return doSendMsg(getString("OpenId"), "text", msgStr);
			}
		} catch (Exception ex) {

		} finally {
			closeConn();
		}

		_errorInf = "δ�ҵ���Ա���߻�Աδ��΢�źš�";
		return false;
	}

	// ����û��Ƿ��Ѿ��ǻ�Ա

	public Boolean OpenIsMember(String MerchantId, String openid) {
		String[] vparams = { MerchantId, openid };
		try {
			executeQuery3("select MemberId from wxUserInfo where MerchantId='%s' and OpenId='%s'", vparams);
			return next() && !(getString("MemberId").equals(""));
		} catch (Exception ex) {

		} finally {
			closeConn();
		}
		return false;
	}

	/*
	 * ΢��ע��,����΢���̼һ���
	 */

	public int WxCreateMerchantDir(HttpServletRequest request, HttpServletResponse response, String MerchartId) {

		initRequest(request, response);

		gMerchantId = MerchartId;

		int nResult = -1;
		try {
			String fileName = MerchartId + ".jsp";

			fileName = __Session.getServletContext().getRealPath("/" + fileName);
			File file = new File(fileName);

			if (file.exists()) // ����ļ�����,�򴴽�File.AppendText����
			{
				file.delete();
			}

			FileWriter fw = new FileWriter(fileName, true);

			fw.write("<%@page contentType=\"text/html;charset=gb2312\"%>\n");
			fw.write("<%@page language=\"java\" import=\"java.sql.*,javax.servlet.http.*,bluec.base.*\"%>\n");
			fw.write("<%\n");
			fw.write("  String gMerchantID = CUtil.GetFileNameWithoutExtension(request.getRequestURI());\n");
			fw.write("  CWeiXin weixin = new CWeiXin();\n");
			fw.write("  weixin.processRequest(request,response,gMerchantID);\n");
			fw.write("%>");
			fw.close();

			CUtil.CreateNewDir(__Session.getServletContext().getRealPath("/pics/" + MerchartId));
			String sPath = __Session.getServletContext().getRealPath("/mdir/" + MerchartId);
			CUtil.CreateNewDir(sPath); // �̼Ҹ�Ŀ¼

			CUtil.CreateNewDir(sPath + "/images"); // �̼�ͼƬĿ¼
			CUtil.CreateNewDir(sPath + "/pics"); // �̼�ͼƬĿ¼2
			CUtil.CreateNewDir(sPath + "/album"); // �̼����Ŀ¼
			CUtil.CreateNewDir(sPath + "/article"); // �̼�����Ŀ¼
			CUtil.CreateNewDir(sPath + "/article/pics"); // �̼�����ͼƬĿ¼

			// ΢��֧������ҳ��
			CUtil.FileCopy(sPath + "/wxpaycall.jsp", __Session.getServletContext().getRealPath("/mdir/wxpaycall.jsp"));
			fileName = sPath + "/wxpaycall.jsp";
			nResult = 0; // CreateUserMenu(MerchartId);

			_errorInf = wxErrorMsg;
		} catch (Exception ex) {
			_errorInf = "ִ���쳣:" + ex.getMessage();

		}
		return nResult;
	}

	/*
	 * �����̼Ҳ˵�
	 */
	public int WxCreateMerchantMenu() {
		int nResult = -1;
		try {
			nResult = CreateUserMenu(gMerchantId);
			_errorInf = wxErrorMsg;
		} catch (Exception ex) {
			_errorInf = "ִ���쳣:" + ex.getMessage();

		}
		return nResult;
	}

	// ͳһ�����ŵ��¼��ʽ
	public int userLogin(boolean bRLogin) {

		String gMerchantName = "";

		int loginMark = -1;

		if (__Session.getAttribute("gIsLogin") == null || bRLogin) {
			try {
				executeQuery3("select a.MerchantId,b.LogoFile,b.StartLogo,b.WxNo,a.Nickname,b.MerchantName, "
						+ "       b.SoftName,b.CopyRight1,b.CopyRight2,a.Headimgurl "
						+ "from wxUserInfo a,wxMerchant b " + "where a.openid='%s' and b.MerchantId=a.MerchantId",
						gOpenId);
				if (next()) {

					gMerchantId = getString("MerchantId");
					gMerchantName = getString("MerchantName");

					__Session.setAttribute("gMerchantId", gMerchantId);
					__Session.setAttribute("gMerchantName", gMerchantName);

					__Session.setAttribute("gWxNo", getString("WxNo"));
					__Session.setAttribute("gNickname", getString("Nickname"));

					__Session.setAttribute("gSoftName", getString("SoftName"));
					__Session.setAttribute("gCopyRight1", getString("CopyRight1"));
					__Session.setAttribute("gCopyRight2", getString("CopyRight2"));

					__Session.setAttribute(CInitParam.safeField, gOpenId);
					__Session.setAttribute(CInitParam.safeFieldValue, "0");
					__Session.setAttribute("gLoginId", gOpenId);
					__Session.setAttribute("gOpenId", gOpenId);

					String LogoFile = getString("LogoFile");
					__Session.setAttribute("gLogoFile", (LogoFile.equals("")) ? "defaultlogo.png" : LogoFile);

					LogoFile = getString("StartLogo");
					__Session.setAttribute("gPicLogo", (LogoFile.equals("")) ? "defaultstart.png" : LogoFile);

					__Session.setAttribute("gHeadimgurl", getString("Headimgurl"));

					__Session.setAttribute("gIsLogin", "1");

					loginMark = 0;

					String[] vparams = { gMerchantId, gOpenId };

					executeQuery3("select kh=dbo.GetMemberId('%s','%s')", vparams);
					if (next() && !getString("kh").equals("")) {
						executeQuery3("select klxNo=dbo.GetMemberTypeId('%s','%s')", vparams);
						next();

						vparams[1] = getString("klxNo");
						__Session.setAttribute("gIsHy", 1);

						executeQuery3("select dzfs=case DiscountID when 'hyj' then 0 "
								+ "                            when 'nzk' then 2 else 1 end, " + "       dzl=LDiscRate "
								+ "from mb_MemberType where MerchantId='%s' and MemberTypeId='%s' ", vparams);
						if (next()) {
							__Session.setAttribute("gDzfs", getString("dzfs"));
							__Session.setAttribute("gDzl", getString("dzl"));
						} else {
							__Session.setAttribute("gDzfs", "2");
							__Session.setAttribute("gDzl", "0");
						}
					} else {
						__Session.setAttribute("gDzfs", "2");
						__Session.setAttribute("gDzl", "0");
						__Session.setAttribute("gIsHy", 0);
					}
					/* ����Ĭ���ŵ� */
					executeQuery3("select top 1 ShopId from wxUnit where MerchantId='%s'", gMerchantId);
					if (next())
						__Session.setAttribute("gShopId", getString("ShopId"));
				}

			} catch (Exception ex) {
				loginMark = -1;
				logger.info("userLogin Error : " + ex.getMessage());
				_errorInf = "��¼�쳣:" + ex.getMessage();

			} finally {
				closeConn();
			}
			logger.info("userLogin Ok : " + gOpenId);
		} else {
			loginMark = 0;
		}
		// ====================
		return loginMark;
	}

	public int userLogin() {
		return userLogin(false);
	}

	// ͳһ�����ŵ��¼��ʽ
	public int setLoginInfo() {
		gMerchantId = __Session.getAttribute("gMerchantId").toString();
		gOpenId = __Session.getAttribute("gOpenId").toString();
		String dwCode = (__Request.getParameter("dwcode") == null)
				? ((__Session.getAttribute("gDwCode") != null) ? __Session.getAttribute("gDwCode").toString() : "")
				: __Request.getParameter("dwcode").toString();

		int loginMark = -1;

		try {
			if (dwCode.equals(""))
				executeQuery3("select top 1 workdate=convert(varchar,getdate(),102),\n"
						+ "       nn=datepart(yy,getdate()),nq=datepart(qq,getdate()),\n"
						+ "       ny=datepart(mm,getdate()),nd=datepart(dd,getdate()),\n"
						+ "       dw_code,branch_name,address,telephone\n" + "from wxUnit \n"
						+ "where MerchantId='%s' and Status=1", gMerchantId);
			else
				executeQuery3("select workdate=convert(varchar,getdate(),102),\n"
						+ "       nn=datepart(yy,getdate()),nq=datepart(qq,getdate()),\n"
						+ "       ny=datepart(mm,getdate()),nd=datepart(dd,getdate()),\n"
						+ "       dw_code,branch_name,address,telephone\n" + "from wxUnit \n" + "where shopid='%s'",
						gMerchantId + dwCode);

			next();

			dwCode = getString("dw_code");
			__Session.setAttribute("gLoginTime", CUtil.getOrigTime());
			__Session.setAttribute("gShopId", gMerchantId + dwCode);
			__Session.setAttribute("gDwCode", getString("DW_CODE"));
			__Session.setAttribute("gDwName", getString("branch_name"));
			__Session.setAttribute("gDwAddr", getString("address"));
			__Session.setAttribute("gDwTel", getString("telephone"));
			__Session.setAttribute("gWorkDate", getString("workdate"));

			__Session.setAttribute("gYear", getString("NN"));
			__Session.setAttribute("gMonth", getString("NY"));
			__Session.setAttribute("gDay", getString("ND"));

			String[] vparams = { gMerchantId, dwCode, gOpenId, "0", "" };

			executeMsSqlProc("WxCreateBook", vparams);

			__Session.setAttribute("gBookId", __procReturn[0]);
			loginMark = 0;

		} catch (Exception ex) {
			loginMark = -1;

			_errorInf = "��¼�쳣:" + ex.getMessage();

		} finally {
			closeConn();
		}
		// ====================
		return loginMark;
	}

	/*
	 * �����̼���Ϣ
	 */
	public int setMerchantInfo(String ShopId) {
		int loginMark = 0;
		try {
			executeQuery3(
					"select a.MerchantId,a.MerchantName,a.StartLogo,b.dw_code,a.LogoFile,b.branch_name,b.address,b.telephone "
							+ "from wxMerchant a,wxUnit b " + "where b.ShopId='%s' and a.MerchantId=b.MerchantId",
					ShopId);
			next();

			__Session.setAttribute("gLoginTime", CUtil.getOrigTime());
			__Session.setAttribute("gMerchantId", getString("MerchantId"));
			__Session.setAttribute("gMerchantName", getString("MerchantName"));
			__Session.setAttribute("gShopId", ShopId);
			__Session.setAttribute("gDwCode", getString("dw_code"));
			__Session.setAttribute("gDwName", getString("branch_name"));
			__Session.setAttribute("gDwAddr", getString("address"));
			__Session.setAttribute("gDwTel", getString("telephone"));

			String LogoFile = getString("LogoFile");
			__Session.setAttribute("gLogoFile", (LogoFile.equals("")) ? "defaultlogo.png" : LogoFile);

			LogoFile = getString("StartLogo");
			__Session.setAttribute("gPicLogo", (LogoFile.equals("")) ? "defaultstart.png" : LogoFile);
		} catch (Exception ex) {
			loginMark = -1;

			_errorInf = "��ȡ��Ϣ�쳣:" + ex.getMessage();

		} finally {
			closeConn();
		}
		// ====================
		return loginMark;
	}

	/**
	 * �̼Һ�̨��½
	 */
	public Boolean MerchantLogin(HttpServletRequest request, HttpServletResponse response) {
		initRequest(request, response);
		boolean IsOk = false;
		try {
			executeQuery3("select wxLoginPwd,MerchantName,CopyRight1,CopyRight2,RoleId "
					+ "from wxMerchant where MerchantId='%s'", __Request.getParameter("merid").toString());

			IsOk = next();

			if (IsOk) {
				IsOk = getString("wxLoginPwd").endsWith(__Request.getParameter("mpwd").toString());
				if (!IsOk)
					_errorInf = "��¼�������";
			} else {
				_errorInf = "�����ڵ��̼ұ��";
			}
			if (IsOk) {
				__Session.setAttribute("gMerchantId", __Request.getParameter("merid").toString());
				__Session.setAttribute("gMerchantName", getString("MerchantName"));
				__Session.setAttribute("gLoginId", __Request.getParameter("merid").toString());
				__Session.setAttribute("gLoginCode", __Request.getParameter("merid").toString());
				__Session.setAttribute("gLoginName", getString("MerchantName"));

				__Session.setAttribute("gCopyRight1", getString("CopyRight1"));
				__Session.setAttribute("gCopyRight2", getString("CopyRight2"));

				__Session.setAttribute("gRoleId", getString("RoleId"));
				__Session.setAttribute("gUserLevel", 1);

				__Session.setAttribute(CInitParam.safeField, __Request.getParameter("merid").toString());
				__Session.setAttribute(CInitParam.safeFieldValue, "0");
			}

		} catch (Exception ex) {

		} finally {
			closeConn();
		}
		return IsOk;
	}

	public void MerchantInfo(HttpServletRequest request, HttpServletResponse response) {
		initRequest(request, response);
		try {
			executeQuery3("select MerchantName,CopyRight1,CopyRight2,SoftName,PicLogo "
					+ "from wxMerchant where MerchantId='%s'", __Request.getParameter("merid").toString());

			next();

			__Session.setAttribute("gMerchantId", __Request.getParameter("merid").toString());
			__Session.setAttribute("gMerchantName", getString("MerchantName"));
			__Session.setAttribute("gCopyRight1", getString("CopyRight1"));
			__Session.setAttribute("gCopyRight2", getString("CopyRight2"));

			__Session.setAttribute("gSoftName", getString("SoftName"));

			String piclogo = getString("PicLogo");

			if (piclogo.equals(""))
				piclogo = "home_logo.png";

			__Session.setAttribute("gPicLogo", piclogo);
		} catch (Exception ex) {

		} finally {
			closeConn();
		}
	}

	// �̼ҵ�¼
	public int sjLogin(HttpServletRequest request, HttpServletResponse response) {
		initRequest(request, response);

		String openid = __Request.getParameter("openid").toString(); // �û�
																		// openid
		String uid = "", pwd = "";
		if (__Request.getParameter("uid") != null) {
			uid = __Request.getParameter("uid").toString();
			pwd = __Request.getParameter("pwd").toString();
		}

		int loginMark = -1;

		try {
			String[] vparams = { openid, uid };

			executeQuery3("select user_id,user_code,user_name,user_passe,kf_code,kf_name,\n"
					+ "       workdate=convert(varchar,getdate(),102),\n"
					+ "       nn=datepart(yy,getdate()),nq=datepart(qq,getdate()),\n"
					+ "       ny=datepart(mm,getdate()),nd=datepart(dd,getdate())\n"
					+ "from pub_user where login_id='%s' or user_code='%s'", vparams);

			if (next()) {
				if (uid.equals("") || pwd.equals(getString("user_passe"))) {
					__Session.setAttribute(CInitParam.safeField, openid);
					__Session.setAttribute(CInitParam.safeFieldValue, "0");
					__Session.setAttribute("gLoginId", getString("USER_ID"));
					__Session.setAttribute("gUserId", getString("USER_ID"));
					__Session.setAttribute("gUserCode", getString("USER_CODE"));
					__Session.setAttribute("gUserName", getString("USER_NAME"));
					__Session.setAttribute("gLoginTime", CUtil.getOrigTime());
					__Session.setAttribute("gDwCode", getString("KF_CODE"));
					__Session.setAttribute("gDwName", getString("KF_NAME"));
					__Session.setAttribute("gWorkDate", getString("workdate"));

					__Session.setAttribute("gYear", getString("NN"));
					__Session.setAttribute("gMonth", getString("NY"));
					__Session.setAttribute("gDay", getString("ND"));

					__Session.setAttribute("gSjLogin", "1");

					if (!uid.equals("")) {
						executeUpdateEx("update pub_user set login_id='" + openid + "' where user_code='" + uid + "'");
					}

					loginMark = 0;
				} else
					_errorInf = "��¼ʧ��:�������";

			} else {
				if (!uid.equals(""))
					_errorInf = "��¼ʧ��:�û�������";
				else
					_errorInf = "";
			}
		} catch (Exception ex) {
			loginMark = -1;

			_errorInf = "��¼�쳣:" + ex.getMessage();

		} finally {
			closeConn();
		}
		// ====================
		return loginMark;
	}

	public String CreateJsApiConfig(Boolean isDebug) {

		StringBuilder buffer = new StringBuilder();

		getJsapi_ticket();
		if (!gJsapi_ticket.equals("")) {

			// SetWxReqObj(__Session.getAttribute("gMerchantId").toString(),
			// false);

			try {
				String url = __Request.getRequestURL().toString();

				if (__Request.getQueryString() != null && !__Request.getQueryString().equals(""))
					url += "?" + __Request.getQueryString();

				String timestamp = CUtil.getTimeFmt("MMddHHmmss");
				String nonceStr = CUtil.GenerateNonceStr().substring(0, 16);

				WxPayData inputObj = new WxPayData();
				inputObj.SetValue("jsapi_ticket", gJsapi_ticket);
				inputObj.SetValue("noncestr", nonceStr);
				inputObj.SetValue("timestamp", timestamp);
				inputObj.SetValue("url", url);

				P("jsapi_ticket=" + gJsapi_ticket + "\n");
				P("noncestr=" + nonceStr + "\n");
				P("timestamp=" + timestamp + "\n");
				P("url=" + url + "\n");

				String signature = inputObj.MakeJsApiSign();

				buffer.append("wx.config({\n");
				if (isDebug)
					buffer.append(" debug: true,\n"); // ��������ģʽ,���õ�����api�ķ���ֵ���ڿͻ���alert��������Ҫ�鿴����Ĳ�����������pc�˴򿪣�������Ϣ��ͨ��log���������pc��ʱ�Ż��ӡ��
				buffer.append(" appId: '" + gAppId + "',\n"); // ������ںŵ�Ψһ��ʶ
				// buffer.append(" url: '" + url + "',\n"); // ������ںŵ�Ψһ��ʶ
				buffer.append(" timestamp: '" + timestamp + "',\n"); // �������ǩ����ʱ���
				buffer.append(" nonceStr: '" + nonceStr + "',\n"); // �������ǩ���������
				buffer.append(" signature: '" + signature + "',\n");// ���ǩ��������¼1
				buffer.append(" jsApiList: [\n");
				buffer.append("   'checkJsApi',\n");
				buffer.append("   'shareTimeline',\n");
				buffer.append("   'onMenuShareTimeline',\n");
				buffer.append("   'onMenuShareAppMessage',\n");
				buffer.append("   'onMenuShareQQ',\n");
				buffer.append("   'onMenuShareWeibo',\n");
				buffer.append("   'openLocation',\n");
				buffer.append("   'getLocation',\n");
				buffer.append("   'chooseImage',\n");
				buffer.append("   'uploadImage',\n");
				buffer.append("   'previewImage',\n");
				buffer.append("   'downloadImage',\n");
				buffer.append("   'scanQRCode',\n");
				buffer.append("   'closeWindow',\n");
				buffer.append("   'chooseWXPay'\n");

				buffer.append(" ]\n"); // �����Ҫʹ�õ�JS�ӿ��б�����JS�ӿ��б����¼2
				buffer.append("});\n");

				P(buffer.toString());
			} catch (Exception ex) {
				P("CreateJsApiConfig:" + ex.getMessage());
			}
		}
		return buffer.toString();

	}

	public String GetNewsList(String type, int offset, int count) {
		String sResult = "";
		// Log.Info("gAccessToken", gAccessToken);
		if (getAccessToken()) {
			CWxMedia wxmedia = new CWxMedia(wxreqobj);
			wxreqobj.gAccessToken = gAccessToken;

			sResult = wxmedia.GetNewsList(type, offset, count);
		} else {
			sResult = CUtil.ErrorMsgToXml(_errorInf);
		}
		return sResult;
	}

	public Boolean CheckSign(HttpServletRequest request) {
		String shopid = request.getParameter("shopid").toString();
		String sign = request.getParameter("sign").toString();
		Boolean result = false;
		try {
			queryBySqlInner("select sname=b.MerchantName+a.branch_name from wxUnit a,wxMerchant b "
					+ "where a.shopid = '%s' and b.MerchantId=a.MerchantId", shopid);
			if (next()) {
				String sign0 = CUtil.MD5(shopid + getString("SNAME"));
				result = sign.equals(sign0);
				if (!result)
					_errorInf = "΢���ŵ���ϢУ��ʧ��.\n" + sign0;
			} else {
				_errorInf = "΢��ƽ̨�����ڶ�Ӧ���ŵ���Ϣ.";
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		} finally {
			closeConn();
		}
		return result;
	}

	public Boolean GetMaxVipKh(HttpServletRequest request) {
		Boolean result = false;
		result = CheckSign(request);
		if (result) {
			try {
				String shopid = request.getParameter("shopid").toString();
				queryBySqlInner("select mkh = Max(MemberId) from mb_Member where MerchantId='%s' and kf_code='%s' ",
						new String[] { shopid.substring(0, 7), shopid.substring(7) });
				if (next()) {
					_errorInf = getString("MKH");
				} else {
					_errorInf = "";
				}
			} catch (Exception ex) {
				result = false;
				_errorInf = ex.getMessage();
			} finally {
				closeConn();
			}
		}
		return result;
	}

	public String WxVipCz(HttpServletRequest request) {
		String result = "";
		try {
			int n = executeMsSqlProc("WxVipCzQkNew",
					new String[] { request.getParameter("skh").toString(), request.getParameter("czid").toString(),
							request.getParameter("czje").toString(), request.getParameter("zsje").toString(),
							request.getParameter("xsycode").toString(), request.getParameter("jbrno").toString(),
							request.getParameter("jbrname").toString(), request.getParameter("jsfsno").toString(),
							request.getParameter("billid").toString(), request.getParameter("orderno").toString(),
							request.getParameter("payno").toString(), request.getParameter("ksrq").toString(),
							request.getParameter("syjno").toString(), request.getParameter("shopid").toString(),
							request.getParameter("openid").toString(), "0", "0", "0", "0", "" });
			if (n != 0)
				result = CUtil.ErrorMsgToJson(__procReturn[5]);
			else
				result = CUtil.SucessMsgToJson(new String[] { "kh", request.getParameter("skh").toString(), "billid",
						request.getParameter("billid").toString(), "ye", __procReturn[0], "jf", __procReturn[1], "qye",
						__procReturn[2], "qjf", __procReturn[3], "openid", __procReturn[4] });

		} catch (Exception ex) {
			result = CUtil.ErrorMsgToJson(ex.getMessage());
		} finally {
			closeConn();
		}
		return result;
	}

	public String WxVipXf(HttpServletRequest request) {
		String result = "";
		try {
			int n = executeMsSqlProc("WxVipXf",
					new String[] { request.getParameter("skh").toString(), request.getParameter("att").toString(),
							request.getParameter("iswx").toString(), request.getParameter("isjc").toString(),
							request.getParameter("xfje").toString(), request.getParameter("bcjf").toString(),
							request.getParameter("jbrno").toString(), request.getParameter("jbrname").toString(),
							request.getParameter("syjno").toString(), request.getParameter("shopid").toString(), "0",
							"0", "0", "0", "", "" });
			if (n != 0)
				result = CUtil.ErrorMsgToJson(__procReturn[5]);
			else
				result = CUtil.SucessMsgToJson(new String[] { "kh", request.getParameter("skh").toString(), "ye",
						__procReturn[0], "jf", __procReturn[1], "qye", __procReturn[2], "qjf", __procReturn[3],
						"openid", __procReturn[4] });
			logger.info("WxVipXf" + result);
		} catch (Exception ex) {
			result = CUtil.ErrorMsgToJson(ex.getMessage());
		} finally {
			closeConn();
		}
		return result;
	}
}
