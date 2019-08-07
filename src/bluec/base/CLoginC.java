package bluec.base;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import bluec.base.CInitParam;
import bluec.base.CQuery;
import bluec.base.CUtil;

public class CLoginC extends CQuery {
	private int _funid = 0;

	private HttpSession gSession = null;
	private HttpServletRequest gRequest = null;

	protected boolean invalidateOldSession(HttpServletRequest request, String loginId) {
		@SuppressWarnings("unchecked")
		Map<String, HttpSession> colusers = (Map<String, HttpSession>) request.getSession().getServletContext()
				.getAttribute("olusers");
		HttpSession usession = null;
		Iterator<String> it = colusers.keySet().iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			usession = (HttpSession) colusers.get(key);
			if (usession.getAttribute(CInitParam.getSafeField()) != null
					&& usession.getAttribute(CInitParam.getSafeField()).toString().equals(loginId)) {
				usession.setAttribute(CInitParam.getSafeField(), null);
				usession.invalidate();
				break;
			}
		}
		return true;
	}

	public int Login(HttpServletRequest request) {

		String pUid = request.getParameter("uid").toString(); // 用户代码
		String pPwd = request.getParameter("pwd").toString(); // 用户口令
		String pCid = request.getParameter("cid").toString();
		String systype = (request.getParameter("systype") != null) ? request.getParameter("systype").toString() : "";

		initRequest(request);
		gRequest = request;
		gSession = request.getSession();

		int loginMark = 0, onlines = CInitParam.online; // 限定的在线用户数

		if (CInitParam.IsCheckCode) // 需要检验校验码
		{
			if (gSession.getAttribute("checkCode") != null) {
				String pCwd = request.getParameter("cwd").toString(); // 用户输入的校验码
				String sCwd = gSession.getAttribute("checkCode").toString(); // 系统保存的校验码
				gSession.setAttribute("checkCode", null);
				if (pCwd.compareTo(sCwd) != 0) {
					loginMark = -4;
					_errorInf = "输入的校验码不正确";
				}
			} else {
				loginMark = -3;
				_errorInf = "校验码不存在";
			}
		}

		if (loginMark == 0) {
			try {
				executeQuery3(CAppListener.getParam("cloginsql"), pCid);
				if (next()) {
					String gcid = getString(1); 
					gSession.setAttribute("gCId", gcid);
					for (int i = 1; i <= getFieldCount(); i++) { // 设置商家信息到session中
						gSession.setAttribute(getFieldName(i), getString(i));
					}
					executeQuery3(CAppListener.getParam("loginsql"), new String[] { gcid, pUid });
					// 比较用户代码和口令
					if (next()) {
						String gUserCode = getString(CInitParam.UserCode);
						String gLoginCode = getString(CInitParam.LoginCode);
						String gPwd = getString(CInitParam.PassWord);

						if ((gUserCode.equals(pUid) || gLoginCode.equals(pUid)) && gPwd.equals(pPwd)) {
							if (getUserCount(request) > onlines) {
								loginMark = 102;
								invalidateOldSession(request, getString(CInitParam.UserId));
								_errorInf = "在线用户数不能超过" + onlines + "人";
							} else {
								gSession.setAttribute(CInitParam.safeField, pUid);
								gSession.setAttribute(CInitParam.safeFieldValue, "0");
								gSession.setAttribute("gLoginId", getString(CInitParam.UserId));
								gSession.setAttribute("gUId", getString(CInitParam.UserId));
								gSession.setAttribute("gLoginTime", CUtil.getOrigTime());

								String[] GlobalField = CInitParam.GlobalObject;

								for (int i = 0; i < GlobalField.length; i++)
									if (!CInitParam.PassWord.toUpperCase().equals(GlobalField[i].toUpperCase()))
										gSession.setAttribute(GlobalField[i], getString(GlobalField[i]));

								gSession.setAttribute("systype", systype);
							}
						} else {
							loginMark = -3;
							_errorInf = "不存在的账号或密码错误";
						}
					} else {
						loginMark = -3;
						_errorInf = "不存在的账号";
					}
				}else{
					loginMark = -4;
					_errorInf = "商家不存在或不可使用";
				}
			} catch (SQLException ex) {
				loginMark = -3;
				_errorInf = ex.getMessage();
				System.err.println("登录失败:" + ex.getMessage());
			} finally {
				closeConn();
			}
		}
		// ====================
		return loginMark;
	}

	public boolean isLogin(HttpSession session) {
		boolean b = true;
		if (!sessionValidated(session)) {
			b = false;
			_errorInf = "未登陆或页面已超时，请重新登录系统.";
		}
		gSession = session;
		return b;
	}

	public boolean isLogin(HttpServletRequest request) {
		initRequest(request);
		return isLogin(request.getSession());
	}

	public boolean isMobile() {
		return (__Session.getAttribute("systype") != null && __Session.getAttribute("systype").toString().equals("i"));
	}

	public String valueByMobile(String v1, String v2) {
		return (__Session.getAttribute("systype") != null && __Session.getAttribute("systype").toString().equals("i"))
				? v1 : v2;
	}

	public int checkLimit(HttpServletRequest request, String funid) throws SQLException {
		int result = 0;
		initRequest(request);
		gSession = request.getSession();
		gRequest = request;

		if (!sessionValidated(request.getSession())) {
			result = -1;
			_errorInf = "尚未登录或页面超时，请重新登录.";
		} else {
			if (funid.equals("")) {
				funid = (gRequest.getParameter("funid") != null) ? gRequest.getParameter("funid").toString() : "";
			}
			String url = request.getRequestURI();
			String app = request.getContextPath();
			if (!app.equals("")) {
				url = url.substring(app.length());
			}

			int si = 0;
			int ei = url.indexOf(".jsp");
			String pageName = "";

			if (si > 0) {
				pageName = url.substring(si + 5);
				url = url.substring(si + 5, ei);
			} else {
				pageName = url;
				url = url.substring(0, ei);
			}

			int ni = url.lastIndexOf("/");
			String s = url.substring(ni + 1);

			if (s.indexOf("i_") == 0) {
				url = url.substring(0, ni + 1) + s.substring(2);
			}

			if (!funid.equals(""))
				url = funid;

			String sql = CAppListener.getParam("limitsql"+gSession.getAttribute("gUserLevel").toString());
			String[] param = { gSession.getAttribute("gCId").toString(), gSession.getAttribute("gUId").toString(), url };

			// 检查是否有权限
			try {
				queryBySqlInner(sql, param);
				if (next()) {
					_funid = getInt("ID");

					if(executeMsSqlProc("SaveULog", new String[]{
							gSession.getAttribute("gUserId").toString(),
							gSession.getAttribute("gUserCode").toString(),
							gSession.getAttribute("gUserName").toString(),
							gSession.getId(), 
							getString("FUN_ID"),
							getString("FUN_NAME"), 
							pageName,
							request.getRemoteHost(),
							request.getRemoteAddr(),
							"0"})==0){
						request.setAttribute("pageid", __procReturn[0]);
						request.setAttribute("funid", url);
					}else{
						P(_errorInf);
					}
				} else {
					result = -2;
					_errorInf = "当前操作员没有访问该功能的权限.";
				}
			} finally {
				closeConn();
			}
		}
		return result;
	}

	public int checkLimit(HttpServletRequest request) throws SQLException {

		return checkLimit(request, "");
	}

	public String createGlobal(HttpServletRequest request) throws ServletException, IOException {
		gSession = request.getSession();
		gRequest = request;

		StringBuffer buffer = new StringBuffer();

		String[] GlobalField = CInitParam.GlobalObject;

		buffer.append("  var _global = new Object();\n");
		for (int i = 0; i < GlobalField.length; i++)
			buffer.append("  _global." + GlobalField[i] + " = \"" + gSession.getAttribute(GlobalField[i]).toString()
					+ "\";\n");

		if (Integer.parseInt(gSession.getAttribute("gUserId").toString()) < 0)
			buffer.append("  _global.gOutLimits = 2111;\n");
		else
			buffer.append("  _global.gOutLimits = 0001\n");
		return buffer.toString();
	}

	public int getFunId() {
		return _funid;
	}

	public String callFunId(String c) {
		return (__Request.getAttribute("funid") != null) ? "funid=" + __Request.getAttribute("funid").toString() + c
				: "";
	}

	public boolean hasLimit(String userid, String funIdOrPageId) throws SQLException {
		boolean result = Integer.parseInt(userid) < 0;
		if (!result) {
			try {
				queryBySqlInner("select fun_id from v_userfun where user_id=%s and fun_id='%s' or pageid='%s'",
						new String[] { userid, funIdOrPageId, funIdOrPageId });
				result = next();
			} finally {
				closeConn();
			}
		}
		return result;
	}

	public boolean hasLimit(String funIdOrPageId) throws SQLException {
		String userid = __Session.getAttribute("gUserId").toString();
		boolean result = Integer.parseInt(userid) < 0;
		if (!result) {
			try {
				queryBySqlInner("select fun_id from v_userfun where user_id=%s and(fun_id='%s' or pageid='%s')",
						new String[] { userid, funIdOrPageId, funIdOrPageId });
				result = next();
			} finally {
				closeConn();
			}
		}
		return result;
	}
}
