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
					_errorInf = "���벻��ȷ";
					return result;
				}
				if (getInt("STATUS") == 0) {
					_errorInf = "�����ʺŲ�����";
					return result;
				}
				if (getInt("SMSCOUNT") < telcount) {
					_errorInf = "����ʣ����������";
					return result;
				}
				result = true;
			} else {
				_errorInf = "�����ʺŲ����ڻ���δע��";
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
		return result;
	}

	/**
	 * ���ŷ���
	 * @param MerchantId 	�ʺ�
	 * @param ShopId 		�ŵ����
	 * @param mpwd			����
	 * @param Mobile		�绰
	 * @param Content		��������
	 * @param Cell			
	 * @param SendTime
	 * @return
	 */
	public int BatchSend2(String MerchantId, String ShopId, String mpwd, String Mobile, String Content, String Cell,
			String SendTime) {
		int result = -1;
		int telcount = Mobile.split(",").length; // �绰��
		if (checkUser(MerchantId, mpwd, telcount)) {
			String v = sms.sendPostEx(smsurl, new String[] { "CorpID", uid, "Pwd", pwd, "Mobile", Mobile, "Content",
					Content, "Cell", Cell, "SendTime", SendTime });
			if (sms.errorCode != 0) { // ִ���쳣
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
					_errorInf = "�˺�δע��";
					break;
				case -2:
					_errorInf = "��������";
					break;
				case -3:
					_errorInf = "�������";
					break;
				case -5:
					_errorInf = "���㣬���ֵ";
					break;
				case -6:
					_errorInf = "��ʱ����ʱ�䲻����Ч��ʱ���ʽ";
					break;
				case -7:
					_errorInf = "�ύ��Ϣĩβδǩ������������ĵ���ҵǩ���� ��";
					break;
				case -8:
					_errorInf = "������������1��300��֮��";
					break;
				case -9:
					_errorInf = "���ͺ���Ϊ��";
					break;
				case -10:
					_errorInf = "��ʱʱ�䲻��С��ϵͳ��ǰʱ��";
					break;
				case -100:
					_errorInf = "IP������";
					break;
				case -102:
					_errorInf = "�˺ź�����";
					break;
				case -103:
					_errorInf = "IPδ����";
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
			if (sms.errorCode != 0) { // ִ���쳣
				_errorInf = sms.errorMsg;
			} else {
				result = Integer.parseInt(v);
				switch (result) {
				case -1:
					_errorInf = "�˺�δע��";
					break;
				case -2:
					_errorInf = "��������";
					break;
				case -3:
					_errorInf = "�������";
					break;
				case -100:
					_errorInf = "IP������";
					break;
				case -101:
					_errorInf = "���ýӿ�Ƶ�ʹ���";
					break;
				case -102:
					_errorInf = "�˺ź�����";
					break;
				case -103:
					_errorInf = "IPδ����";
					break;
				}
			}

		}
		return result;
	}
}
