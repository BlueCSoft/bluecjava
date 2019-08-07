package bluec.base.pay;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;

import bluec.base.CAppListener;
import bluec.base.CHttpService;
import bluec.base.CQuery;
import bluec.base.CUtil;

public class CBasePay extends CQuery {
	protected String gHomeUrl = CAppListener.getParam("homeurl");
	// "http://www.bluecsoft.cn";
	protected String gPayUrl = "https://openapi-test.qfpay.com";
	protected String gAppId = "4A7D18AF8496425193E510DEC8E9526F";
	protected String gCAppId = "4A7D18AF8496425193E510DEC8E9526F";

	protected String gKey = "6A91B52DBC1E45B4AC300850718EBADF";
	protected String gMchid = ""; // �̻���΢�Ż������ƽ̨�ĺ���
	protected String gCMchid = ""; // ���̻���

	protected String gDlsMchid = ""; // ������΢�Ż������ƽ̨�ĺ���
	protected String gVSyjId = ""; // ������߱��

	protected String gPMerchantId = ""; // �̻��ĸ��̻�������ƽ̨�ı��
	protected String gMerchantId = ""; // �̻�������ƽ̨�ı��
	protected String gShopId = ""; // �ŵ�������ƽ̨�ı��
	protected String gPayShopId = ""; // �ŵ���֧��ƽ̨�ĵ��
	protected String gPayMchId = ""; // �ŵ���֧��ƽ̨��������ߺ�
	protected int gPlatform = 0; // �̻�֧�������ƽ̨0-΢�ţ�1-������, 2-Ǯ��
	protected int SysPlatForm = 2;
	protected String JsonResult = ""; //
	protected String paytype = "800208";
	protected String paymothed = "";
	protected String gPayatt = "SKAPP";

	protected Boolean IsSelfPay = false; // ���̼���֧��

	protected int MAX_QCOUNT = 9;
	protected String pbody, psubject, psTradeNo, psBillId, pBillTypeId, pSysId, pVersion, psyjno, pusercode, pshopid,
			pgoodsid, pgoodsname, psPayFee;

	protected WXPay wxpay = null;

	public int getPlatform() { 
		return gPlatform; 
	}
	
	protected void InitParam(String shopid) {
		String treecode = "", submchid = "";
		gShopId = shopid;
		gMerchantId = gShopId.substring(0, 7);
		try {
			queryBySqlInner(
					"select TreeCode,PayMerId,AppId,MChid,PayShopId,PayMchId from wxMerchant where MerchantId='%s'",
					gMerchantId);
			next();
			treecode = getString("TreeCode");
			gCMchid = getString("PayMerId");
			gCAppId = getString("AppId");
			submchid = getString("MChid");
			gPayShopId = getString("PayShopId");
			gPayMchId = getString("PayMchId");

			queryBySqlInner(
					"select MerchantId,AppId,MChid,WxKey,PayPlatForm,PayMerId,PayAppCode,PayKey,PayMerId0,PayAppCode0,PayKey0 "
							+ "from wxMerchant " + "where '%s' like TreeCode+'%' and PayPlatForm>0 "
							+ "order by TreeCode desc",
					treecode);

			if (next()) {
				gPlatform = getInt("PayPlatForm");

				switch (gPlatform) {
				case 1: // ��֧��
					gPayUrl = "https://api.mch.weixin.qq.com/pay/micropay";
					gPMerchantId = getString("MerchantId");
					gAppId = getString("AppId");
					gMchid = getString("MChid");
					gKey = getString("WxKey");
					break;
				case 2: // ����΢��֧��
					gPayUrl = "https://api.mch.weixin.qq.com/pay/micropay";
					gPMerchantId = getString("MerchantId");
					gAppId = getString("AppId");
					gMchid = getString("MChid");
					gKey = getString("WxKey");
					gCMchid = submchid;
					break;
				case 3: // ���������֧��
					gPayUrl = CAppListener.getParam("dlbpayurl");
					gPMerchantId = getString("MerchantId");

					IsSelfPay = gPMerchantId.equals(gMerchantId);

					gAppId = (IsSelfPay) ? getString("PayAppCode0") : getString("PayAppCode");
					gMchid = (IsSelfPay) ? getString("PayMerId0") : getString("PayMerId");
					gKey = (IsSelfPay) ? getString("PayKey0") : getString("PayKey");

					if (IsSelfPay)
						gCMchid = gMchid;
					queryBySqlInner("select PayShopId,PayMchId from wxUnit " + "where ShopId='%s' ", gShopId);
					if (next()) {
						if (!getString("PayShopId").equals(""))
							gPayShopId = getString("PayShopId");
						if (!getString("PayMchId").equals(""))
							gPayMchId = getString("PayMchId");
					}

					break;
				case 4: // ����Ǯ��֧��
					gPayUrl = CAppListener.getParam("qfpayurl");
					gAppId = getString("PayAppCode");
					gMchid = getString("PayMerId");
					gKey = getString("PayKey");
					// gCMchid = "";
					gPayMchId = "";
					IsSelfPay = gPMerchantId.equals(gMerchantId);
					break;
				}

				if (gPlatform <= 2) {
					HashMap<String, String> vparams = new HashMap<String, String>();
					vparams.put("AppID", gAppId);
					vparams.put("MchID", gMchid);
					vparams.put("Key", gKey);
					vparams.put("certPath", "");
					wxpay = new WXPay(new WXPayConfig(vparams));
				}
			} else {
				throw new Exception("֧���������ò���ȷ");
			}
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
	}

	public CBasePay(String shopid) {
		InitParam(shopid);
	}

	public CBasePay(HttpServletRequest request, HttpServletResponse response) {
		initRequest(request, response);
	}

	protected Boolean PayInit(String body, String subject, String sTradeNo, String sBillId, String BillTypeId,
			String SysId, String Version, String syjno, String usercode, String shopid, String goodsid,
			String goodsname, String sPayFee) {
		String appid = (gPlatform <= 2) ? gAppId : gPayatt;
		String mchid = (gPlatform <= 2) ? gMchid : gMchid;

		pbody = body;
		psubject = subject;
		psTradeNo = sTradeNo;
		psBillId = sBillId;
		pBillTypeId = BillTypeId;
		pSysId = SysId;
		pVersion = Version;
		psyjno = syjno;
		pusercode = usercode;
		pshopid = shopid;
		pgoodsid = goodsid;
		pgoodsname = goodsname;
		psPayFee = sPayFee;

		try {
			if (gPlatform == 2 && !gCAppId.equals(""))
				appid = gCAppId;
			if (gPlatform == 2)
				mchid = gCMchid;
			String[] vparams = { appid, mchid, sTradeNo, sBillId, BillTypeId, SysId, Version,
					sPayFee, SysPlatForm + "", gPlatform + "", shopid, syjno, usercode,
					goodsname, "" };
			if (executeMsSqlProc("WxSkPayInit", vparams) == 0) {
				return true;
			} else {
				_errorInf = __procReturn[0];
				logger.info("PayInit" + _errorInf + "(" + sTradeNo + ")");
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
			logger.info("PayInit" + _errorInf);
		}
		return false;
	}

	protected void RecordResult(String OrderNo, String OpenId, String Bank_type, String Fee_type, String Is_subscribe,
			String WxPayNo, String PayTime, int Status, String PayMothed, String Note) {

		boolean isCz = pBillTypeId.equals("CZ") && !pbody.equals("");
		String ye = "0", jf = "0", qye = "0", qjf = "0";
		logger.info("pBillTypeId" + pBillTypeId);
		logger.info("pbody" + pbody);

		if (Status == 1 || Status == -1) {
			executeMsSqlProc("WxSkPayOk", new String[] { OrderNo, OpenId, Bank_type, Fee_type, Is_subscribe, WxPayNo,
					PayTime, Status + "", PayMothed, Note, "" });
		}

		switch (Status) {
		case 0:
			JsonResult = CUtil.ErrorMsgToJson(2, Note, new String[] { "tradeno", WxPayNo, "orderno", OrderNo });
			break;
		case 1:
			if (isCz) // ��Ա��ֵ
			{
				String[] vipInfo = pbody.split(",");
				executeMsSqlProc("WxVipCzQkNew",
						new String[] { vipInfo[0], // ����
								"", vipInfo[1], // ��ֵ���
								vipInfo[2], // ���ͽ��
								vipInfo[3], // @XsyCode varchar(10),
								vipInfo[4], // @JbrNo varchar(10),
								vipInfo[5], // @JbrName nvarchar(20),
								"10303", // @JsfsNo varchar(10),
								psBillId, // @BillId varchar(32),
								OrderNo, // @OrderNo varchar(32),
								WxPayNo, // @PayNo varchar(32),
								"", // @ksrq varchar(20),
								psyjno, // @SyjNo varchar(10),
								gShopId, // @ShopId varchar(10),
								OpenId, // @Openid varchar(50),
								"0", "0", "0", "0", "" });
				ye = __procReturn[0];
				jf = __procReturn[1];
				qye = __procReturn[2];
				qjf = __procReturn[3];
			}

			JsonResult = CUtil.SucessMsgToJson(new String[] { "tradeno", WxPayNo, "orderno", OrderNo, "openid", OpenId,
					"currtype", Fee_type, "paytime", PayTime, "ye", ye, "jf", jf, "qye", qye, "qjf", qjf });
			break;
		case 2:
			JsonResult = CUtil.ErrorMsgToJson(2, Note, new String[] { "tradeno", WxPayNo, "orderno", OrderNo });
			break;
		case -1:
			JsonResult = CUtil.ErrorMsgToJson(Note);
			break;
		}

	}

	/*
	 * ΢����֧���ӿ�
	 */
	protected String wxTimStrToDateTimeStr(String sTime) {
		return sTime.substring(0, 4) + "-" + sTime.substring(4, 6) + "-" + sTime.substring(6, 8) + " "
				+ sTime.substring(8, 10) + ":" + sTime.substring(10, 12) + ":" + sTime.substring(12);
	}

	/*
	 * ΢��֧��������� -1 - ʧ��, 1 - �ɹ�, 2 - δ֪״̬
	 */
	protected int WxChkResult(Map<String, String> obj, Boolean isQuery) {
		try {
			if (isQuery) // ��ѯģʽ
			{
				if (obj.get("return_code").toString().equals("SUCCESS") && obj.get("result_code").toString().equals("SUCCESS")) {
					// ֧���ɹ�
					if (obj.get("trade_state").toString().equals("SUCCESS")) {
						return 1;
					}
					// �û�֧���У���Ҫ������ѯ
					else if (obj.get("trade_state").toString().equals("USERPAYING")) {
						return 2;
					}
				}

				// ������ش�����Ϊ���˽��׶����Ų����ڡ���ֱ���϶�ʧ��
				if (obj.get("err_code").toString().equals("ORDERNOTEXIST")) {
					return -1;
				} else {
					return 2;
				}
			} else {
				// ����ύ��ɨ֧���ӿڵ���ʧ�ܣ������쳣
				if (!obj.containsKey("return_code") || obj.get("return_code").toString().equals("FAIL")) {
					_errorInf = obj.containsKey("return_msg") ? obj.get("return_msg").toString() : "";
					logger.info("WxPay API interface call failure, result : " + WXPayUtil.mapToXml(obj));
					return -2;
				}

				logger.info("WxPay API interface result : " + WXPayUtil.mapToXml(obj));
				// ˢ��֧��ֱ�ӳɹ�
				if (obj.get("return_code").equals("SUCCESS") && obj.get("result_code").equals("SUCCESS")) {
					if (!obj.containsKey("trade_state") || obj.get("trade_state").equals("SUCCESS")) { // ���ǲ�ѯ״̬����֧���ɹ�
						return 1;
					} else {
						_errorInf = obj.get("trade_state_desc").toString();
						if (obj.get("trade_state").toString().equals("USERPAYING")) {
							return 2;
						} else {
							return -1;
						}
					}
				}
				logger.info("err_code="+obj.get("err_code").toString());
				// ҵ������ȷʧ��
				if (obj.get("err_code").equals("USERPAYING") && obj.get("err_code").equals("SYSTEMERROR")) {
					logger.info(
							"WxPay API interface call success, business failure, result : " + WXPayUtil.mapToXml(obj));
					_errorInf = obj.get("err_code") + "\n" + obj.get("err_code_des");
					return -1;
				}
			}
		} catch (Exception ex) {

		}
		return 2;
	}

	protected int WxResult(Map<String, String> obj, String OrderNo, Boolean isQueryOnly, int ncount) {
		int result = WxChkResult(obj, isQueryOnly);

		try {

			if (!isQueryOnly) {
				switch (result) {
				case 1:
					RecordResult(obj.get("out_trade_no"), "NONE", "NONE", obj.get("fee_type"),
							obj.get("is_subscribe"), obj.get("transaction_id"),
							wxTimStrToDateTimeStr(obj.get("time_end")), 1, paymothed, "֧���ɹ�");
					break;
				case 2:
					if (ncount >= MAX_QCOUNT)
						result = -1;
					String err = _errorInf;
					if (result == -1)
						err = "֧��ʧ��";
					RecordResult(OrderNo, "NONE", "NONE", "NONE", "0", "", "", result, paymothed, err);
					break;
				case -1:
					RecordResult(OrderNo, "NONE", "NONE", "NONE", "0", "", "", -1, paymothed, _errorInf);
					break;
				case -2:
					result = -1;
					JsonResult = CUtil.ErrorMsgToJson(_errorInf);
					break;
				}
			}
		} catch (Exception ex) {
			result = -1;
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage() + "(WxResult)");
		}

		return result;
	}

	public int WxQuery(String out_trade_no, int ncount) {
		Map<String, String> data = new HashMap<String, String>();
		int result = -1;
		data.put("out_trade_no", out_trade_no);
		// data.put("transaction_id", "4008852001201608221962061594");
		try {
			Map<String, String> r = wxpay.orderQuery(data);
			result = WxResult(r, out_trade_no, false, ncount);
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}

		return result;
	}

	/*** ������֧���ӿ� */

	/*
	 * ������֧��������� -1 - ʧ��, 1 - �ɹ�, 2 - δ֪״̬
	 */
	protected int DlbResult(String jsondata, String OrderNo, int ncount) {
		int result = -1;
		try {
			JSONObject reader = new JSONObject(jsondata);
			if (reader.has("result")) {
				String respcd = reader.getString("result");
				String errmsg = "";
				if (respcd.equals("success")) {
					reader = reader.getJSONObject("data");
					String payStatus = reader.getString("status");
					switch (payStatus) {
					case "SUCCESS":
						result = 1;
						errmsg = "֧���ɹ�";
						break;
					case "CANCEL":
						result = -1;
						errmsg = "��ȡ��";
						break;
					case "INIT":
						result = (ncount >= MAX_QCOUNT) ? -1 : 2;
						errmsg = (result == 2) ? "֧����,���Ժ�..." : "֧��ʧ��";
						break;

					}

					RecordResult(reader.getString("requestNum"), "NONE", "NONE", "DEFAULT", "0",
							reader.getString("orderNum"), reader.getString("completeTime"), result, paymothed, errmsg);

				} else {
					reader = reader.getJSONObject("error");
					String err = reader.getString("errorCode").toString();
					if (reader.has("errorMsg"))
						err += reader.getString("errorMsg").toString();
					else
						err += "�����ˣ�����֧�����ò���";

					RecordResult(OrderNo, "NONE", "NONE", "", "0", "", "", -1, paymothed, err);
				}
			} else {
				JsonResult = CUtil.ErrorMsgToJson("֧��ʧ��");
			}
		} catch (Exception ex) {
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage());
		}
		return result;
	}

	/*
	 * ������������ѯ
	 */
	protected int DlbQuery(String TradeNo, String OrderNo, int ncount) {
		int result = -1;

		String timestamp = CUtil.getTimeFmt("yyMMddHHmmss");

		String data = (IsSelfPay) ? String.format("/v1/customer/order/payresult/%s/%s/%s", gCMchid, gPayShopId, OrderNo)
				: String.format("/v1/agent/order/payresult/%s/%s/%s/%s", gMchid, gCMchid, gPayShopId, OrderNo);

		String sign = CUtil.SHA1(new String[] { "secretKey=", gKey, "&timestamp=", timestamp, "&path=", data });

		String[] headvars = { "accessKey", gAppId, "timestamp", timestamp, "token", sign };

		JsonResult = CUtil.unescape(CHttpService.sendGet(gPayUrl + data, headvars));

		if (ncount > -1)
			result = DlbResult(JsonResult, OrderNo, ncount);

		return result;
	}
}
