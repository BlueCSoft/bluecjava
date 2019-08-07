package bluec.base.WxApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.github.wxpay.sdk.WXPayUtil;

import bluec.base.CQuery;

public class CWxResult extends CQuery {
	Map<String, String> obj;

	public CWxResult(HttpServletRequest request) {
		initRequest(request);
	}

	public Boolean IsSet(String key)
    {
        return obj.get(key) != null;
    }
	
	public String GetValue(String key) {
		return obj.get(key);
	}

	private String getDocString(String nodeName) {
		return GetValue(nodeName);
	}

	private int getDocInt(String nodeName) {
		return Integer.parseInt(GetValue(nodeName));
	}

	private float getDocFloat(String nodeName) {
		return Float.parseFloat(GetValue(nodeName));
	}

	private double getDocDouble(String nodeName) {
		return Double.parseDouble(GetValue(nodeName));
	}

	private void ReturnResult(String ReturnCode, String ReturnMsg) {
		outPrint("<xml><return_code><![CDATA[" + ReturnCode + "]]></return_code>" + "<return_msg><![CDATA[" + ReturnMsg
				+ "]]></return_msg></xml>");
	}

	private void LoadXmlDoc() {
		StringBuilder sb = new StringBuilder();
		try {

			BufferedReader br = new BufferedReader(
					new InputStreamReader((ServletInputStream) __Request.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			obj = WXPayUtil.xmlToMap(sb.toString());
		} catch (Exception ex) {
			P(ex.getMessage());
		}
	}

	protected String wxTimStrToDateTimeStr(String sTime) {
		return sTime.substring(0, 4) + "-" + sTime.substring(4, 6) + "-" + sTime.substring(6, 8) + " "
				+ sTime.substring(8, 10) + ":" + sTime.substring(10, 12) + ":" + sTime.substring(12);
	}

	/*
	 * 微信支付回调处理
	 */
	public Boolean WxPayCallBack() {
		Boolean bResult = true;
		logger.info("WxPayCallBack:WxPayCallBack Begin");
		Boolean isSubCall = false; // 代理调用
		try {
			LoadXmlDoc();

			bResult = getDocString("return_code").equals("SUCCESS");

			if (!bResult) // 协议级错误
				logger.info("CWxResult:" + getDocString("return_msg"));
			else {
				bResult = getDocString("result_code").equals("SUCCESS");
				logger.info("WxPayCallBack:result_code bResult  = " + bResult);
				isSubCall = IsSet("sub_appid");

				if (!bResult) { // 业务级错误
					logger.info("CWxResult" + getDocString("result_msg"));
					executeMsSqlProc("WxSkPayOk",
							new String[] { getDocString("out_trade_no"),
									(isSubCall) ? getDocString("sub_openid") : getDocString("openid"), "", "", "", "",
									"", "-1", "WX", getDocString("result_msg"), "" });
				} else {
					String[] vparams = { (isSubCall) ? getDocString("sub_appid") : getDocString("appid"),
							(isSubCall) ? getDocString("sub_mch_id") : getDocString("mch_id"),
							getDocString("bank_type"), getDocString("fee_type"),
							(isSubCall) ? getDocString("sub_is_subscribe") : getDocString("is_subscribe"),
							getDocString("out_trade_no"),
							(isSubCall) ? getDocString("sub_openid") : getDocString("openid"),
							getDocString("transaction_id"), wxTimStrToDateTimeStr(getDocString("time_end")),
							getDocString("attach"), getDocString("total_fee"), "2", "" };
					logger.info("attach=" + getDocString("attach").substring(0, 4));
					switch (getDocString("attach").substring(0, 2)) {
					case "DC":// 点餐支付
						executeMsSqlProc("WxBookPayOk", vparams);
						logger.info("WxBookPayOk" + _errorInf);
						break;
					case "CZ":// 充值支付
						executeMsSqlProc("WxCzPayOk", vparams);
						logger.info("WxCzPayOk" + _errorInf);
						break;
					case "QT":
					case "XF":// 消费支付
						executeMsSqlProc("WxBillPayOk", vparams);
						logger.info("WxBillPayOk" + vparams.toString());
						break;
					}
				}
			}
		} catch (Exception ex) {
			logger.info("WxBookPayOk Error:\n" + ex.getMessage());
		} finally {
			logger.info("WxPayCallBack : Report response : SUCCESS");
			ReturnResult("SUCCESS", "OK");
		}
		return bResult;
	}
}
