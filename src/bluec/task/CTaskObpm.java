package bluec.task;

import java.util.Vector;

import org.json.JSONObject;

import bluec.base.CHttpService;
import bluec.base.CQuery;

public class CTaskObpm extends CQuery {
	public CTaskObpm() {
		try {
			Vector<String> userids = new Vector<String>();
			Vector<String> openids = new Vector<String>();
			queryBySqlIDInner("TASKUSERLIST");
			while (next()) {
				userids.add(getString("USER_ID"));
				openids.add(getString("OPENID"));
			}
			if (userids.size() > 0) {
				for (int i = 0; i < userids.size(); i++) {
					String msgStr = "";
					queryBySqlIDWithParamInner("TASKHINT", new String[] { userids.get(i), "" });
					while (next()) {
						msgStr += getString("TITLE") + "\n";
					}
					if (!msgStr.equals("")) {
						msgStr = "今日任务\n" + msgStr;
						String sresult = CHttpService.sendPostEx("http://127.0.0.1/wxtools/wxsendmsgto.jsp",
								new String[] { "merchantid", "2004003", "openid", openids.get(i), "msg", msgStr });
						JSONObject o = new JSONObject(sresult);
						if (o.getInt("code") == 1 && o.getInt("sub_code") == 1) {
							updateBySqlWithParamInner("update pub_user set sendtime=getdate() where user_id=%s",
									userids.get(i));
						} else {
							P(o.toString());
						}
					}
				}
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
			P(_errorInf);
		} finally {
			closeConn();
		}
	}
}
