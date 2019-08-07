package bluec.gyy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public class CGyvip extends CYmsInterFace {
	protected Connection getConnection() throws SQLException {
		return CGvipLink.getConnection();
	}

	public CGyvip() {

	}

	public CGyvip(HttpServletRequest request) {
		super(request);
	}

	// 查询会员信息
	public String card_findcust(String vtrack, String vnote) {
		String outresult = "";
		try {
			if (callProc("card_interface.card_findcust", new String[] { vtrack, vnote }, new int[] { 1, 1 },
					new int[] { 2, 1, 1, 1, 1, 1, 1, 1 }, true) != 0) {
				outresult = errorMsgToJson0("卡不存在或不能使用");
			} else {
				JSONObject out = new JSONObject();
				JSONObject json = new JSONObject();
				json.put("ret", __procReturn[0]);
				json.put("sno", __procReturn[1]);
				json.put("sname", __procReturn[2]);
				json.put("typeid", __procReturn[3]);
				json.put("typename", __procReturn[4]);
				json.put("mtel", __procReturn[5]);
				json.put("apoints", __procReturn[6]);
				json.put("points", __procReturn[7]);
				json.put("yxq", "2099/12/31");
				json.put("isyx", 1);

				out.put("data", json);
				out.put("recordcount", 1);
				out.put("code", 0);
				out.put("msg", "ok");
				out.put("sub_code", 0);
				out.put("sub_msg", "ok");
				outresult = out.toString();
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 查询积分卡/券
	public String card_findmzk(String vpaytype, String vtrack, String vmfid, String vsalelist) {
		String outresult = "";
		try {
			if (callProc("card_interface.card_findmzk", new String[] { vpaytype, vtrack, vmfid, vsalelist },
					new int[] { 2, 1, 1, 1 }, new int[] { 2, 1, 1, 3, 1, 3, 1 }, true) != 0) {
				outresult = errorMsgToJson0(__procReturn[1], new String[] { "retmsg", __procReturn[1] });
			} else {
				JSONObject out = new JSONObject();
				JSONObject json = new JSONObject();
				json.put("ret", __procReturn[0]);
				json.put("retmsg", __procReturn[1]);
				json.put("rcardno", __procReturn[2]);
				json.put("rye", __procReturn[3]);
				json.put("rstatus", __procReturn[4]);
				json.put("rmaxje", __procReturn[5]);
				json.put("rtype", __procReturn[6]);

				out.put("data", json);
				out.put("recordcount", 1);
				out.put("code", 0);
				out.put("msg", "ok");
				out.put("sub_code", 0);
				out.put("sub_msg", "ok");
				outresult = out.toString();
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 核销积分卡
	public String card_sendmzk(String vpaycode, String vcardid, String vbillno, String vmkt, String vsyjh, String vfphm,
			String vhxje, String vsalelist) {
		String outresult = "";

		try {
			if (callProc("card_interface.card_sendmzk",
					new String[] { vpaycode, vcardid, vbillno, vmkt, vsyjh, vfphm, vhxje, vsalelist },
					new int[] { 1, 1, 1, 1, 1, 1, 3, 1 }, new int[] { 2, 1 }, true) != 0) {
				outresult = errorMsgToJson0(__procReturn[1]);
			} else {
				outresult = sucessMsgToJson0("ok", new String[] { "ye", "0" });
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String card_sendmzk() {
		return card_sendmzk(__Request.getParameter("paycode").toString(), __Request.getParameter("kh").toString(),
				__Request.getParameter("billid").toString(), __Request.getParameter("orgid").toString(),
				__Request.getParameter("erpcashid").toString(), __Request.getParameter("billid").toString(),
				__Request.getParameter("payje").toString(), "");
	}

	// 撤销积分卡
	public String card_sendmzkcancel(String vpaycode, String vcardid, String vbillno, String vmkt, String vsyjh,
			String vfphm, String vcxje, String vsalelist) {
		String outresult = "";
		try {
			if (callProc("card_interface.card_sendmzkcancel",
					new String[] { vpaycode, vcardid, vbillno, vmkt, vsyjh, vfphm, vcxje, vsalelist },
					new int[] { 1 , 1, 1, 1, 1, 1, 3, 1 }, new int[] { 2, 1 }, true) != 0) {
				outresult = errorMsgToJson0(__procReturn[1]);
			} else {
				outresult = sucessMsgToJson0("ok", new String[] { "ye", "0" });
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String card_sendmzkcancel() {
		return card_sendmzkcancel(__Request.getParameter("paycode").toString(), __Request.getParameter("kh").toString(),
				__Request.getParameter("billid").toString(), __Request.getParameter("orgid").toString(),
				__Request.getParameter("erpcashid").toString(), __Request.getParameter("billid").toString(),
				__Request.getParameter("payje").toString(), "");
	}
}
