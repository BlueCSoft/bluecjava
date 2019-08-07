package bluec.sms;

import bluec.base.*;
import bluec.base.CQuery;

public class CLkSms extends CQuery {
	private static String smsurl = CAppListener.getParam("SMSURL");
	private static String uid = CAppListener.getParam("UID");
	private static String pwd = CUtil.GetPassWord(CAppListener.getParam("PWD"));

	private CURLConnection sms = new CURLConnection("GB2312");

	private boolean checkUser(String MerchantId, String mpwd, int telcount) {
		boolean result = false;
		try {
			queryBySqlInner("select MPWD,SMSCOUNT,STATUS from SMS_USER where MERCHANTID='%s'", MerchantId);
			if (next()) {
				if (!getString("MPWD").equals(CUtil.MD5(mpwd))) {
					_errorInf = "密码不正确";
					return result;
				}
				if (getInt("STATUS") == 0) {
					_errorInf = "短信帐号不可用";
					return result;
				}
				if (getInt("SMSCOUNT") < telcount) {
					_errorInf = "短信剩余条数不足";
					return result;
				}
				result = true;
			} else {
				_errorInf = "短信帐号不存在或者未注册";
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
		return result;
	}

	/**
	 * 短信发送
	 * @param MerchantId 	帐号
	 * @param ShopId 		门店代码
	 * @param mpwd			密码
	 * @param Mobile		电话
	 * @param Content		短信内容
	 * @param Cell			
	 * @param SendTime
	 * @return
	 */
	public int BatchSend2(String MerchantId, String ShopId, String mpwd, String Mobile, String Content, String Cell,
			String SendTime) {
		int result = -1;
		int telcount = Mobile.split(",").length; // 电话数
		if (checkUser(MerchantId, mpwd, telcount)) {
			String v = sms.sendPostEx(smsurl, new String[] { "CorpID", uid, "Pwd", pwd, "Mobile", Mobile, "Content",
					Content, "Cell", Cell, "SendTime", SendTime });
			if (sms.errorCode != 0) { // 执行异常
				_errorInf = sms.errorMsg;
			} else {
				result = Integer.parseInt(v);
				try {
					updateBySqlWithParamInner(
							"INSERT INTO [SMS_RECORD]([MERCHANTID],[SHOPID],[MOBILE],[CONTENT],"
									+ "[CCOUNT],[CREATETIME],[SENDTIME],[STATUS]) "
									+ "values('%s','%s','%s','%s',%s,getdate(),getdate(),%s)",
							new String[] { MerchantId, ShopId, Mobile, Content, telcount + "", result + "" });
				} catch (Exception e) {

				}
				switch (result) {
				case -1:
					_errorInf = "账号未注册";
					break;
				case -2:
					_errorInf = "其他错误";
					break;
				case -3:
					_errorInf = "密码错误";
					break;
				case -5:
					_errorInf = "余额不足，请充值";
					break;
				case -6:
					_errorInf = "定时发送时间不是有效的时间格式";
					break;
				case -7:
					_errorInf = "提交信息末尾未签名，请添加中文的企业签名【 】";
					break;
				case -8:
					_errorInf = "发送内容需在1到300字之间";
					break;
				case -9:
					_errorInf = "发送号码为空";
					break;
				case -10:
					_errorInf = "定时时间不能小于系统当前时间";
					break;
				case -100:
					_errorInf = "IP黑名单";
					break;
				case -102:
					_errorInf = "账号黑名单";
					break;
				case -103:
					_errorInf = "IP未导白";
					break;
				}
			}

		}
		return result;
	}
	
	public int SelSum(String MerchantId, String mpwd) {
		int result = -1;
		if (checkUser(MerchantId, mpwd, -1)) {
			String v = sms.sendPostEx(smsurl, new String[] { "CorpID", uid, "Pwd", pwd});
			if (sms.errorCode != 0) { // 执行异常
				_errorInf = sms.errorMsg;
			} else {
				result = Integer.parseInt(v);
				switch (result) {
				case -1:
					_errorInf = "账号未注册";
					break;
				case -2:
					_errorInf = "其他错误";
					break;
				case -3:
					_errorInf = "密码错误";
					break;
				case -100:
					_errorInf = "IP黑名单";
					break;
				case -101:
					_errorInf = "调用接口频率过快";
					break;
				case -102:
					_errorInf = "账号黑名单";
					break;
				case -103:
					_errorInf = "IP未导白";
					break;
				}
			}

		}
		return result;
	}
}
