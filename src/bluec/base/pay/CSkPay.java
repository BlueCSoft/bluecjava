package bluec.base.pay;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;

import bluec.base.CHttpService;
import bluec.base.CUtil;

public class CSkPay extends CBasePay {
	public CSkPay(String shopid) {
		super(shopid);
		gPayatt = "SKPAY";
	}

	protected String WxSkPay(String payfee, String currcd, String pay_type, String out_trade_no, String auth_code,
			String goods_name) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("auth_code", auth_code); // ��Ȩ��
		data.put("body", goods_name); // ��Ʒ����
		data.put("total_fee", payfee); // �ܽ��
		data.put("out_trade_no", out_trade_no); // �̻�������

		data.put("spbill_create_ip", WXPayConfig.IP);// �ն�ip

		if (gPlatform == 2)
			data.put("sub_mch_id", gCMchid); // ���̻���

		try {
			
			Map<String, String> out = wxpay.microPay(data);
			int result = WxResult(out, out_trade_no, false, 0);
			int qcount = 1;
			WXPayUtil.getLogger().info("result: "+result);
			
			while (result == 2) {
				result = WxQuery(out_trade_no, qcount);
				qcount ++;
				if(qcount<5)
					Thread.sleep(1*1000);
				else
					Thread.sleep(3*1000);
			}
		} catch (Exception ex) {
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage());
		}

		return JsonResult;
	}

	/*
	 * ΢���˵�������� -1 - ʧ��, 1 - �ɹ�, 2 - δ֪״̬
	 */
	protected int WxRefundResult(Map<String, String> obj, Boolean isQuery) {
		int result = -1;
		try {
			// ����ύ��ɨ֧���ӿڵ���ʧ�ܣ������쳣
			if (!obj.containsKey("return_code") || obj.get("return_code").equals("FAIL")) {
				_errorInf = obj.containsKey("return_msg") ? obj.get("return_msg").toString() : "";
				logger.info("WxRefund WxRefund API interface call failure, result : " + WXPayUtil.mapToXml(obj));
			} else {
				// ˢ��֧��ֱ�ӳɹ�
				if (obj.get("return_code").equals("SUCCESS") && obj.get("result_code").equals("SUCCESS")) {
					updateBySqlWithParamInner(
							"update wx_PayRec set Status=-2,Note='���˿�',ROrderNo='%s',RefundNo='%s',RefundTime=getdate() "
									+ "where OrderNo='%s'",
							new String[] { obj.get("out_refund_no").toString(), obj.get("refund_id").toString(),
									obj.get("out_trade_no").toString() });
					result = 1;
				} else {
					_errorInf = obj.get("err_code").toString() + "\n" + obj.get("err_code_des").toString();
				}
			}
		} catch (Exception ex) {
			result = -1;
		}
		return result;
	}

	/*
	 * ΢�Ŷ����˿�
	 */
	public String WxRefund(String TradeNo, String OrderNo, String ROrderNo, String PayFee) {
		// ����ʱ��
		Map<String, String> data = new HashMap<String, String>();
		if (TradeNo != null && !TradeNo.equals(""))// ΢�Ŷ����Ŵ��ڵ������£�����΢�Ŷ�����Ϊ׼
			data.put("transaction_id", TradeNo);
		else
			data.put("out_trade_no", OrderNo);

		data.put("total_fee", PayFee); // �ܽ��
		data.put("refund_fee", PayFee);

		data.put("out_refund_no",
				(TradeNo != null && !TradeNo.equals("")) ? CUtil.GenerateOutTradeNo(gMchid) : TradeNo); // �˻�����
		data.put("op_user_id", gMchid);

		if (gPlatform == 2)
			data.put("sub_mch_id", gCMchid); // ���̻���

		try {
			Map<String, String> out = wxpay.refund(data);
			if (WxRefundResult(out, false) == 1) {
				JsonResult = CUtil.SucessMsgToJson(new String[] { "tradeno", out.get("refund_id").toString(),
						"refundtime", CUtil.getOrigTime(), "orderno", OrderNo });
			} else {
				JsonResult = CUtil.ErrorMsgToJson(_errorInf);
			}
		} catch (Exception ex) {
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage());
		}

		return JsonResult;
	}

	/*
	 * Ǯ��֧��������� -1 - ʧ��, 1 - �ɹ�, 2 - δ֪״̬
	 */
	protected int QfResult(String jsondata, int ncount) {
		int result = -1;
		try {
			JSONObject reader = new JSONObject(jsondata);
			if (reader.has("respcd")) {
				String respcd = reader.getString("respcd");
				if (!respcd.equals("0000") && !respcd.equals("1143") && !respcd.equals("1145")) // ֱ��ʧ��
				{
					if (reader.has("syssn")) {
						RecordResult(reader.getString("out_trade_no"), "NONE", "NONE", "", "0",
								reader.getString("syssn"), "", -1, paymothed, reader.getString("resperr"));
					} else {
						JsonResult = CUtil.ErrorMsgToJson(reader.getString("resperr"));
					}

				} else {
					if (reader.has("data")) {
						reader = reader.getJSONArray("data").getJSONObject(0);
						respcd = reader.getString("respcd");
					}

					String errmsg = "";
					if (respcd.equals("0000")) {
						errmsg = "֧���ɹ�";
						result = 1;
					} else {
						errmsg = (reader.has("errmsg")) ? reader.getString("errmsg") : reader.getString("resperr");

						if (respcd.equals("1143") || respcd.equals("1145")) {
							result = (ncount < MAX_QCOUNT) ? 2 : -1;
							if (result < 0)
								errmsg = "֧�����ɹ�";
							else
								errmsg += ",���Ե�...";
						} else {
							result = -1;
						}
					}

					RecordResult(reader.getString("out_trade_no"), "NONE", "NONE", reader.getString("txcurrcd"), "0",
							reader.getString("syssn"), reader.getString("sysdtm"), result, paymothed, errmsg);
				}
			}
		} catch (Exception ex) {
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage() + ':' + jsondata);
		}
		return result;
	}

	/*
	 * ������ˢ��֧��
	 */
	protected String DlbSkPay(String payfee, String txcurrcd, String pay_type, String out_trade_no, String auth_code,
			String goods_name) {
		// ����ʱ��
		String timestamp = CUtil.getTimeFmt("yyMMddHHmmss");

		String[] data = (IsSelfPay)
				? new String[] { "customerNum", gCMchid, // is
						"authCode", auth_code, // is
						"machineNum", gPayMchId, // is
						"shopNum", gPayShopId, // is
						"requestNum", out_trade_no, // is
						"amount", Double.parseDouble(payfee) / 100 + "", "source", "API", "tableNum", "" }
				: new String[] { "agentNum", gMchid, "customerNum", gCMchid, // is
						"authCode", auth_code, // is
						"machineNum", gPayMchId, // is
						"shopNum", gPayShopId, // is
						"requestNum", out_trade_no, // is
						"amount", Double.parseDouble(payfee) / 100 + "", "source", "API", "tableNum", "" };

		String ac = (IsSelfPay) ? "customer" : "agent";
		String url = "/v1/" + ac + "/passive/create";

		String sign = CUtil.SHA1(new String[] { "secretKey=", gKey, "&timestamp=", timestamp, "&path=", url, "&body=",
				CUtil.ToJson(data) }).toUpperCase();

		// http://openapi.duolabao.cn/v1/customer/passive/create

		String[] headvars = { "accessKey", gAppId, "timestamp", timestamp, "token", sign };
		String jsondata = CHttpService.sendPostFmt(gPayUrl + url, data, headvars, true);

		try {
			JSONObject reader = new JSONObject(jsondata);
			if (reader.has("result")) {
				String respcd = reader.getString("result");
				if (respcd.equals("success")) {
					Thread.sleep(2000);
					DlbQuery("", out_trade_no, 0);
				} else {
					reader = reader.getJSONObject("error");
					String err = reader.getString("errorCode");
					if (reader.has("errorMsg"))
						err += reader.getString("errorMsg");
					else
						err += "�����ˣ�����֧�����ò���";

					JsonResult = CUtil.ErrorMsgToJson(err);
				}
			} else {
				JsonResult = CUtil.ErrorMsgToJson("֧��ʧ��");
			}
		} catch (Exception ex) {
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage());
		}
		return JsonResult;
	}

	/*
	 * �������˵�������� -1 - ʧ��, 1 - �ɹ�, 2 - δ֪״̬
	 */
	protected int DlbRefundResult(String jsondata, String OrderNo) {
		int result = -1;
		try {
			JSONObject reader = new JSONObject(jsondata);
			if (reader.has("result")) {
				String respcd = reader.getString("result");
				if (respcd.equals("success")) {
					reader = reader.getJSONObject("data");
					JsonResult = CUtil.SucessMsgToJson(new String[] { "tradeno", reader.getString("orderNum"),
							"refundtime", CUtil.getOrigTime(), "orderno", OrderNo });

					updateBySqlWithParamInner(
							"update wx_PayRec set Status=-2,Note='���˿�',RefundNo='%s',RefundTime=getdate() "
									+ "where OrderNo='%s'",
							new String[] { reader.getString("orderNum"), OrderNo });
					result = 1;
				} else {
					reader = reader.getJSONObject("error");
					JsonResult = CUtil.ErrorMsgToJson(reader.getString("errorMsg"));
				}
			}
		} catch (Exception ex) {
			JsonResult = CUtil.ErrorMsgToJson(ex.getMessage() + jsondata);
		}
		return result;
	}

	/*
	 * �����������˿�
	 */
	public String DlbRefund(String TradeNo, String OrderNo, String PayFee) {
		// ����ʱ��
		String timestamp = CUtil.getTimeFmt("yyMMddHHmmss");
		String[] keyparam = { "agentNum", gMchid, "customerNum", gCMchid, "shopNum", gPayShopId, "requestNum",
				OrderNo };

		String[] data = (IsSelfPay)
				? new String[] { "customerNum", gCMchid, "shopNum", gPayShopId, "requestNum", OrderNo }
				: new String[] { "agentNum", gMchid, "customerNum", gCMchid, "shopNum", gPayShopId, "requestNum",
						OrderNo };

		String url = "/v1/" + ((IsSelfPay) ? "customer" : "agent") + "/order/refund";
		String sign = CUtil.SHA1(new String[] { "secretKey=", gKey, "&timestamp=", timestamp, "&path=", url, "&body=",
				CUtil.ToJson(data) });

		String[] headvars = { "accessKey", gAppId, "timestamp", timestamp, "token", sign };

		String jsondata = CHttpService.sendPostFmt(gPayUrl + url, data, headvars, true);

		DlbRefundResult(jsondata, OrderNo);

		return JsonResult;
	}

	/*
	 * ������ѯ
	 */

	public String OrderQuery(String body, String SysId, String BillTypeId, String sVersion, String SyjNo,
			String sUserCode, String TradeNo, String OrderNo, String ShopId, String BillId, int ncount) {
		try {
			if (TradeNo.equals("") && OrderNo.equals("")) {
				queryBySqlInner(
						"select top 1 WxPayNo,OrderNo,SysId,BillTypeId,Version,SyjNo,UserCode,BillId,shopid from wx_PayRec "
								+ "where ShopId='%s' and BillId='%s' and isnull(Status,0)>-2 order by orderid desc",
						new String[] { ShopId, BillId });
				if (next()) {
					pbody = body;
					TradeNo = getString("WxPayNo");
					OrderNo = getString("OrderNo");

					pSysId = getString("SysId");
					pBillTypeId = getString("BillTypeId");
					psBillId = getString("BillId");
					pVersion = getString("Version");
					psyjno = getString("SyjNo");
					pusercode = getString("UserCode");
					pshopid = getString("shopid");
				}
			} else {
				pbody = body;
				pSysId = SysId;
				pBillTypeId = BillTypeId;
				psBillId = BillId;
				pVersion = sVersion;
				psyjno = SyjNo;
				pusercode = sUserCode;
				pshopid = ShopId;
			}

			if (!TradeNo.equals("") || !OrderNo.equals("")) {
				switch (gPlatform) {
				case 1:
					WxQuery(OrderNo, ncount);
					break;
				case 3:
					DlbQuery(TradeNo, OrderNo, ncount);
					break;
				case 4:
					break;
				default:
					JsonResult = CUtil.ErrorMsgToJson("δ��ͨ");
					break;
				}
			} else {
				JsonResult = CUtil.ErrorMsgToJson("������δ֧��");
			}
		} catch (Exception ex) {

		}
		return CUtil.unescape(JsonResult);
	}

	/*
	 * ˢ��֧��
	 */

	public String SwipeCardPay(String body, String subject, String sTradeNo, String sBillId, String BillTypeId,
			String SysId, String Version, String syjno, String usercode, String shopid, String goodsid,
			String goodsname, String sPayFee, String auth_code) {
		String result = "";

		if (PayInit(body, subject, sTradeNo, sBillId, BillTypeId, SysId, Version, syjno, usercode, shopid, goodsid,
				goodsname, sPayFee)) {
			paymothed = (auth_code.substring(0, 1).equals("1")) ? "WX" : "ZFB";
			paytype = (paymothed.equals("WX")) ? "800208" : "800108";

			switch (gPlatform) {
			case 1:
				result = WxSkPay(sPayFee, "CNY", paytype, sTradeNo, auth_code, goodsname);
				break;
			case 2:
				result = WxSkPay(sPayFee, "CNY", paytype, sTradeNo, auth_code, goodsname);
				break;
			case 3:
				result = DlbSkPay(sPayFee, "CNY", paytype, sTradeNo, auth_code, goodsname);
				break;
			case 4:
				break;
			default:
				result = CUtil.ErrorMsgToJson("δ��ͨ");
				break;
			}
		} else {
			result = CUtil.ErrorMsgToJson(_errorInf);
		}
		return CUtil.unescape(result);
	}

	/*
	 * ����ȡ��
	 */
	public String OrderRefund(String ShopId, String BillId, String SysPlatForm) {
		String result = "";

		try {
			queryBySqlInner(
					"select top 1 WxPayNo,OrderNo,ROrderNo,PayFee from wx_PayRec "
							+ "where ShopId='%s' and BillId='%s' and SysPlatForm=%s and isnull(Status,0)>0 order by orderid desc",
					new String[] { ShopId, BillId, SysPlatForm });
			if (next()) {
				String TradeNo = getString("WxPayNo");
				String OrderNo = getString("OrderNo");
				String ROrderNo = getString("ROrderNo");
				String PayFee = Math.ceil(getFloat("PayFee") * 100) + "";

				switch (gPlatform) {
				case 1:
					result = WxRefund(TradeNo, OrderNo, ROrderNo, PayFee);
					break;
				case 2:
					result = WxRefund(TradeNo, OrderNo, ROrderNo, PayFee);
					break;
				case 3:
					result = DlbRefund(TradeNo, OrderNo, PayFee);
					break;
				case 4:
					break;
				default:
					result = CUtil.ErrorMsgToJson("δ��ͨ");
					break;
				}
			} else {
				result = CUtil.ErrorMsgToJson(0, "���������ڻ��ѱ��������˻�");
			}
		} catch (Exception ex) {

		}
		return CUtil.unescape(result);
	}
}
