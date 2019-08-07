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

		String pUid = request.getParameter("uid").toString(); // �û�����
		String pPwd = request.getParameter("pwd").toString(); // �û�����
		String pCid = request.getParameter("cid").toString();
		String systype = (request.getParameter("systype") != null) ? request.getParameter("systype").toString() : "";

		initRequest(request);
		gRequest = request;
		gSession = request.getSession();

		int loginMark = 0, onlines = CInitParam.online; // �޶��������û���

		if (CInitParam.IsCheckCode) // ��Ҫ����У����
		{
			if (gSession.getAttribute("checkCode") != null) {
				String pCwd = request.getParameter("cwd").toString(); // �û������У����
				String sCwd = gSession.getAttribute("checkCode").toString(); // ϵͳ�����У����
				gSession.setAttribute("checkCode", null);
				if (pCwd.compareTo(sCwd) != 0) {
					loginMark = -4;
					_errorInf = "�����У���벻��ȷ";
				}
			} else {
				loginMark = -3;
				_errorInf = "У���벻����";
			}
		}

		if (loginMark == 0) {
			try {
				executeQuery3(CAppListener.getParam("cloginsql"), pCid);
				if (next()) {
					String gcid = getString(1); 
					gSession.setAttribute("gCId", gcid);
					for (int i = 1; i <= getFieldCount(); i++) { // �����̼���Ϣ��session��
						gSession.setAttribute(getFieldName(i), getString(i));
					}
					executeQuery3(CAppListener.getParam("loginsql"), new String[] { gcid, pUid });
					// �Ƚ��û�����Ϳ���
					if (next()) {
						String gUserCode = getString(CInitParam.UserCode);
						String gLoginCode = getString(CInitParam.LoginCode);
						String gPwd = getString(CInitParam.PassWord);

						if ((gUserCode.equals(pUid) || gLoginCode.equals(pUid)) && gPwd.equals(pPwd)) {
							if (getUserCount(request) > onlines) {
								loginMark = 102;
								invalidateOldSession(request, getString(CInitParam.UserId));
								_errorInf = "�����û������ܳ���" + onlines + "��";
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
							_errorInf = "�����ڵ��˺Ż��������";
						}
					} else {
						loginMark = -3;
						_errorInf = "�����ڵ��˺�";
					}
				}else{
					loginMark = -4;
					_errorInf = "�̼Ҳ����ڻ򲻿�ʹ��";
				}
			} catch (SQLException ex) {
				loginMark = -3;
				_errorInf = ex.getMessage();
				System.err.println("��¼ʧ��:" + ex.getMessage());
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
			_errorInf = "δ��½��ҳ���ѳ�ʱ�������µ�¼ϵͳ.";
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
			_errorInf = "��δ��¼��ҳ�泬ʱ�������µ�¼.";
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

			// ����Ƿ���Ȩ��
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
					_errorInf = "��ǰ����Աû�з��ʸù��ܵ�Ȩ��.";
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
