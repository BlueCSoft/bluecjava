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

public class CLogin extends CQuery {

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
			if (usession.getAttribute(CInitParam.safeField) != null
					&& usession.getAttribute(CInitParam.safeField).toString().equals(loginId)) {
				usession.setAttribute(CInitParam.safeField, null);
				usession.invalidate();
				break;
			}
		}
		return true;
	}

	public int Login(HttpServletRequest request, String pUid, String pPwd, String systype, String pCwd) {

		initRequest(request);
		gRequest = request;
		gSession = request.getSession();

		int loginMark = 0, onlines = CInitParam.online; // 登陆成功

		if (CInitParam.IsCheckCode) // 检验校验码
		{
			if (gSession.getAttribute("checkCode") != null) {
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
				executeQuery3(CInitParam.LoginSql, pUid);
				// 比较用户代码和口令
				if (rs.next()) {
					String gUserCode = getString(CInitParam.UserCode);
					String gLoginCode = getString(CInitParam.LoginCode);
					String gPwd = getString(CInitParam.PassWord);
				
					if (!CAppListener.getParam("md5","0").equals("0")) {
						pPwd = CUtil.MD5(pPwd);
					}

					if ((gUserCode.equals(pUid) || gLoginCode.equals(pUid)) && gPwd.equals(pPwd)) {
						if (onlines > 0 && getUserCount(gRequest) > onlines) {
							loginMark = 102;
							invalidateOldSession(gRequest, getString(CInitParam.UserId));
							_errorInf = "在线用户数不能超过" + onlines + "人";
						} else {

							gSession.setAttribute(CInitParam.safeField, pUid);
							gSession.setAttribute(CInitParam.safeFieldValue, "0");
							gSession.setAttribute("gLoginId", getString(CInitParam.UserId));
							gSession.setAttribute("gLoginTime", CUtil.getOrigTime());

							String[] GlobalField = CInitParam.GlobalObject;

							for (int i = 0; i < GlobalField.length; i++)
								if (!CInitParam.PassWord.toUpperCase().equals(GlobalField[i].toUpperCase()))
									gSession.setAttribute(GlobalField[i], getString(GlobalField[i]));

							gSession.setAttribute("systype", systype);
						}
					} else {
						loginMark = -3;
						_errorInf = "不存在的用户或口令错误";
					}
				} else {
					loginMark = -3;
					_errorInf = "不存在的用户";
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

	public int Login(HttpServletRequest request) {

		String pUid = request.getParameter("uid").toString(); // 用户代码
		String pPwd = request.getParameter("pwd").toString(); // 用户口令

		String systype = (request.getParameter("systype") != null) ? request.getParameter("systype").toString() : "";
		String pCwd = (request.getParameter("cwd") != null) ? request.getParameter("cwd").toString() : "";

		return Login(request, pUid, pPwd, systype, pCwd);

	}

	public int LoginC(HttpServletRequest request) {

		String pUid = request.getParameter("uid").toString(); // 用户代码
		String pPwd = request.getParameter("pwd").toString(); // 用户口令
		String pCid = request.getParameter("cid").toString();
		String systype = (request.getParameter("systype") != null) ? request.getParameter("systype").toString() : "";

		initRequest(request);
		gSession = request.getSession();
		gRequest = request;

		int loginMark = 0, onlines = 1000; // 登陆成功

		if (CInitParam.IsCheckCode) // 检验校验码
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
				executeQuery3(CUtil.formatStr(CInitParam.LoginSql, pCid, "{CID}"), pUid);
				// 比较用户代码和口令
				if (rs.next()) {
					String gUserCode = getString(CInitParam.UserCode);
					String gLoginCode = getString(CInitParam.LoginCode);
					String gPwd = getString(CInitParam.PassWord);
					if (!CAppListener.getParam("md5").equals("0")) {
						pPwd = CUtil.MD5(pPwd);
					}
					if ((gUserCode.equals(pUid) || gLoginCode.equals(pUid)) && gPwd.equals(pPwd)) {
						if (getUserCount(request) > onlines) {
							loginMark = 102;
							invalidateOldSession(request, getString(CInitParam.UserId));
							_errorInf = "在线用户数不能超过" + onlines + "人";
						} else {
							gSession.setAttribute(CInitParam.safeField, pUid);
							gSession.setAttribute(CInitParam.safeFieldValue, "0");
							gSession.setAttribute("gLoginId", getString(CInitParam.UserId));
							gSession.setAttribute("gLoginTime", CUtil.getOrigTime());

							String[] GlobalField = CInitParam.GlobalObject;

							for (int i = 0; i < GlobalField.length; i++)
								if (!CInitParam.PassWord.toUpperCase().equals(GlobalField[i].toUpperCase()))
									gSession.setAttribute(GlobalField[i], getString(GlobalField[i]));

							gSession.setAttribute("systype", systype);
						}
					} else {
						loginMark = -3;
						_errorInf = "不存在的用户或口令错误";
					}
				} else {
					loginMark = -3;
					_errorInf = "不存在的用户";
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
			_errorInf = "页面已超时，请重新登录系统.";
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
			String url = request.getRequestURI().toLowerCase();
			String app = request.getContextPath().toLowerCase();
			// P("url=" + url);
			// P("app=" + app);
			if (!app.equals("")) {
				url = url.substring(app.length());
			}

			int si = url.indexOf("/bin/");
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

			String sql = "";
			String[] param = { gSession.getAttribute("gUserId").toString(), url };
			if (Integer.parseInt(gSession.getAttribute("gUserLevel").toString()) < 0)
				sql = CAppListener.getParam("adminsql");
			else
				sql = CAppListener.getParam("usersql");

			// 检查是否有权限
			try {
				queryBySqlInner(sql, param);
				if (next()) {
					String[] params = { "0", gSession.getAttribute("gLoginId").toString(),
							gSession.getAttribute("gUserId").toString(), gSession.getAttribute("gUserCode").toString(),
							gSession.getAttribute("gUserName").toString(), gSession.getId(), getString("FUN_ID"),
							getString("FUN_NAME"), pageName, request.getRemoteHost(), request.getRemoteAddr(),
							"getdate()", "0", "----", "----" };

					_funid = getInt("ID");

					executeUpdate("update sys_seq set value=value+1 where id='PAGEID'");
					queryBySqlInner("select value from sys_seq where id='PAGEID'");
					next();
					result = getInt(1);
					params[0] = getString(1);
					if (_databaseType == 0) {
						params[11] = "SYSDATE";
					}

					updateBySqlWithParam(
							"insert into sys_ulog_xt(xh,sid,userid,usercode,username,sessionid,fun_id,fun_name,"
									+ "                        pagename,host,ip,stime,olineuser,dq_code,dq_name)\n"
									+ "values(%s,%s,%s,'%s','%s','%s','%s','%s','%s','%s','%s',%s,%s,'%s','%s')",
							params, gSession);
					request.setAttribute("pageid", Integer.toString(result));

					request.setAttribute("funid", url);
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

	public int checkLimitM(HttpServletRequest request, String funid) throws SQLException {
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

			int si = url.indexOf("/bin/");
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

			result = executeMsSqlProc("CheckLimitM",
					new String[] { gSession.getAttribute("gMerchantId").toString(),
							gSession.getAttribute("gUserCode").toString(), url, pageName, request.getRemoteHost(), "" },
					gSession);
			if (result < 0) {
				result = -2;
				_errorInf = "当前操作员没有访问该功能的权限.";
			} else {
				request.setAttribute("funid", url);
			}
		}
		return result;
	}

	public int checkLimit(HttpServletRequest request) throws SQLException {

		return checkLimit(request, "");
	}

	public int checkLimitM(HttpServletRequest request) throws SQLException {

		return checkLimitM(request, "");
	}

	public int checkLimitC(HttpServletRequest request, String funid) throws SQLException {
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

			int si = url.indexOf("/bin/");
			int ei = url.indexOf(".jsp");
			String pageName = url.substring(si + 5);
			url = url.substring(si + 5, ei);

			int ni = url.lastIndexOf("/");
			String s = url.substring(ni + 1);

			if (s.indexOf("i_") == 0) {
				url = url.substring(0, ni + 1) + s.substring(2);
			}

			if (!funid.equals(""))
				url = funid;

			String sql = "";
			String[] param = { gSession.getAttribute("gUserId").toString(), url };
			if (Integer.parseInt(gSession.getAttribute("gUserLevel").toString()) <= 0)
				sql = CAppListener.getParam("adminsql");
			else
				sql = CAppListener.getParam("usersql");

			// 检查是否有权限
			try {
				queryBySqlInner(sql, param);
				if (next()) {
					String[] params = { "0", gSession.getAttribute("gCid").toString(),
							gSession.getAttribute("gLoginId").toString(), gSession.getAttribute("gUserId").toString(),
							gSession.getAttribute("gUserCode").toString(),
							gSession.getAttribute("gUserName").toString(), gSession.getId(), getString("FUN_ID"),
							getString("FUN_NAME"), pageName, request.getRemoteHost(), request.getRemoteAddr(), "0",
							"----", "----" };

					_funid = getInt("ID");

					executeUpdate("update sys_seq set value=value+1 where id='PAGEID'");
					queryBySqlInner("select value from sys_seq where id='PAGEID'");
					next();
					result = getInt(1);
					params[0] = getString(1);

					updateBySqlWithParamInner(
							"insert into sys_ulog_xt(xh,cid,sid,userid,usercode,username,sessionid,fun_id,fun_name,"
									+ "                        pagename,host,ip,stime,olineuser,dq_code,dq_name)\n"
									+ "values(%s,'%s',%s,%s,'%s','%s','%s','%s','%s','%s','%s','%s',getdate(),%s,'%s','%s')",
							params);
					request.setAttribute("pageid", Integer.toString(result));
					request.setAttribute("funid", url);
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

	public String changePwd(HttpServletRequest request) {
		String result = "";
		try {
			String oldpwd = request.getParameter("oldpwd").toString();
			String newpwd = request.getParameter("newpwd").toString();
			if (!CAppListener.getParam("md5").equals("0")) {
				oldpwd = CUtil.MD5(oldpwd);
				newpwd = CUtil.MD5(newpwd);
			}
			if (_databaseType == 0) {
				if (executeOracleProc("ChangeManagerPwd",
						new String[] { request.getParameter("userid").toString(), oldpwd, newpwd },
						new int[] { 2, 1, 1 }, null, 4) == 0) {
					result = CUtil.SucessMsgToXml("ok");
				} else {
					result = CUtil.ErrorMsgToXml(_errorInf);
				}
			} else {
				if (this.executeMsSqlProc("ChangeManagerPwd",
						new String[] { request.getParameter("userid").toString(), oldpwd, newpwd }) == 0) {
					result = CUtil.SucessMsgToXml("ok");
				} else {
					result = CUtil.ErrorMsgToXml(_errorInf);
				}
			}
		} catch (Exception ex) {
			result = CUtil.ErrorMsgToXml(ex.getMessage());
		}
		return result;
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
				queryBySqlInner("select fun_id from v_userfun where user_id=%s and(fun_id='%s' or pageid='%s')",
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

	private void CreateMenu1(CTreeN.CTreeNode cnode, StringBuilder buf) {
		for (int i = 0; i < cnode.cnodes.size(); i++) {
			CTreeN.CTreeNode ccnode = cnode.cnodes.elementAt(i);
			if (ccnode.cnodes.size() == 0) {
				if (ccnode.values[0].equals("-")) {
					buf.append("<li class=\"divider\"></li>\n");
				} else {
					String url = ccnode.values[1];
					if (url.indexOf("http") != 0 && !__Request.getContextPath().equals("")) {
						url = __Request.getContextPath() + url;
					}
					__Request.getContextPath();
					buf.append("<li><a href=\"javascript:OnCallFun('" + ccnode.key + "','" + ccnode.values[0] + "','"
							+ url + "','" + ccnode.values[2] + "');\">" + ccnode.values[0] + "</a></li>\n");
				}
			} else {
				buf.append("<li class=\"dropdown-submenu\">\n");
				buf.append("<a tabindex=\"-1\" href=\"javascript:;\">" + ccnode.values[0] + "</a>\n");
				buf.append("<ul class=\"dropdown-menu\">\n");
				CreateMenu1(ccnode, buf);
				buf.append("</ul>\n");
				buf.append("</li>\n");
			}
		}
	}

	public String CreateMainMenu(HttpServletRequest request, String sqlid, String[] vparams) {
		initRequest(request);
		StringBuilder buf = new StringBuilder();
		CTreeN tree = new CTreeN();
		try {
			queryBySqlIDWithParamInner(sqlid, vparams);
			while (next()) {
				tree.insertNode(getInt("ID"), getInt("PID"), getString("FUN_ID"), getString("PFUN_ID"),
						new String[] { getString("FUN_NAME"), getString("PAGENAME"), getString("TARGET") });
			}
			// P(tree.toString());
			for (int i = 0; i < tree.cnodes.size(); i++) {
				buf.append("<span class=\"dropdown\">\n");
				buf.append("<a id=\"dLabel\" role=\"button\" data-toggle=\"dropdown\" class=\"btn btn-primary\""
						+ " style=\"border:0px\" data-target=\"#\" href=\"javascript:;\">\n");
				buf.append(tree.cnodes.elementAt(i).values[0] + "<span class=\"caret\"></span></a>\n");
				buf.append("<ul class=\"dropdown-menu multi-level\" role=\"menu\" aria-labelledby=\"dropdownMenu\">\n");
				CreateMenu1(tree.cnodes.elementAt(i), buf);
				buf.append("</ul>\n");
				buf.append("</span>\n");
			}
		} catch (Exception ex) {
			P(ex.getMessage());
		} finally {
			closeConn();
		}

		return buf.toString();
	}
}
