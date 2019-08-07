package bluec.sysc;

import java.util.Vector;

import bluec.base.*;
//import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class CSyscObject extends CJson {

	public CSyscObject(HttpServletRequest request) {
		super(request);
	}

	/* 获取签到签退最大同步记录ID */
	public String SyscLoginMaxRid() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		try {
			queryBySqlInner("select max(rid) maxrid from sessionhistory where storeid='%s'", storeid);
			next();

			int rjson = getInt2("maxrid", 0);
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"maxrid\":" + rjson
					+ "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 待上传签到签退数据 */
	public String SyscLoginQuery() {
		String outresult = "";

		try {
			String maxid = "0";
			queryBySqlInner("select maxvalue from sys_tranlog where dataid='LOGINLOG'");
			if (next()) {
				maxid = getString(1);
			}
			queryBySqlIDWithParamInner("R_LOGINQUERY_SYCS", maxid);

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			outgmp.put("data", formatResultToJsonArray());

			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "ok");

			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 签到数据更新 */
	public String SyscLoginLogUpdate() {
		String outresult = "";
		try {
			JSONObject outgmp = new JSONObject();

			String maxid = "0";
			queryBySqlInner("select maxvalue from sys_tranlog where dataid='LOGINLOG'");
			if (next()) {
				maxid = getString(1);
			}
			queryBySqlIDWithParamInner("R_LOGINQUERY_SYCS", maxid);

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			JSONArray dArray = formatResultToJsonArray();
			String data = dArray.toString();
			if (dArray.length() > 0) {
				CURLConnection crequest = new CURLConnection();
				crequest.setDefaultContentEncoding("utf-8");

				String inresult = crequest
						.sendPostEx(CInitParam.SyscServerUrl + "PostSessionlog", new String[] { data }, true).trim();

				JSONObject ingmp = new JSONObject(inresult);

				P(inresult);
				if (ingmp.has("code")) {
					if (ingmp.getString("code").equals("000")) {
						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "ok");

						String maxsid = dArray.getJSONObject(dArray.length() - 1).getString("sessionid");

						executeUpdate("update sys_tranlog set maxvalue='%s' where dataid='LOGINLOG'",
								new String[] { maxsid });
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", ingmp.getString("message"));
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "没有未传输的数据");
			}
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取专柜表最大更新时间 */
	public String SyscCounterMaxUpTime() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		try {
			queryBySqlInner("select max(lastuptime) maxuptime from counter where storeid='%s'", storeid);
			next();
			String rjson = "\"" + getDate2("maxuptime", "yyyy-MM-dd HH:mm:ss", "") + "\"";
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"maxuptime\":"
					+ rjson + "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取专柜数据 */
	public String SyscCounterQuery() {
		String outresult = "";
		String[] vparams = { __Request.getParameter("storeid").toString(),
				__Request.getParameter("maxuptime").toString() };
		try {
			queryBySqlIDWithParamInner("R_COUNTERQUERY", vparams);
			String rjson = formatResultToJson("");
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"data\":" + rjson
					+ "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 专柜数据更新 */
	public String SyscCounterUpdate(String scbh, String gwbh) {
		String outresult = "";
		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "scbh_in", scbh, "gwbh_in", gwbh };
			JSONObject outgmp = new JSONObject();

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/getCounter.ihtml", vparams)
					.trim();

			JSONObject ingmp = new JSONObject(inresult);

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			//
			if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
				JSONArray cout = ingmp.getJSONArray("counter_out");
				if (cout.length() > 0) {
					JSONArray counters = new JSONArray();
					for (int i = 0; i < cout.length(); i++) {
						JSONObject o = new JSONObject();
						JSONObject p = cout.getJSONObject(i);
						o.put("storeid", p.getString("scbh").toString());
						o.put("counterid", p.getString("gwbh"));
						o.put("countername", p.getString("gwmc"));
						o.put("countertel", "");
						o.put("supplyid", (ingmp.has("gysbh")) ? ingmp.getString("gysbh") : "");
						o.put("departid", (ingmp.has("bmbh")) ? ingmp.getString("bmbh") : "");
						counters.put(o);
					}

					int rcount = counters.length();

					dataAllSqls = new String[rcount + 1];

					dataAllSqls[0] = "delete from counter_tran";

					createInsertValues("counter_tran", counters, 1, rcount);

					if (executeSqls(dataAllSqls) == 0) {

						executeOracleProc("TranCounter", "systemauto", 1, null, 4);

						inresult = crequest.sendPostFmt(CInitParam.SyscServerUrl + "PostCounters",
								new String[] { counters.toString() }, true).trim();

						JSONObject sysco = new JSONObject(inresult);
						if (sysco.has("code")) {
							if (sysco.getString("code").equals("000")) {
								outgmp.put("sub_code", "1");
								outgmp.put("sub_msg", "专柜信息传输成功");
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", sysco.getString("message"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", sysco.getString("fhxx_out"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", _errorInf);
					}
					// P(inresult);
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			}

			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public String SyscCounterUpdate() {
		return SyscCounterUpdate(__Request.getParameter("scbh").toString(), __Request.getParameter("gwbh").toString());
	}

	/* 获取专柜管理员最大更新时间 */
	public String SyscCounterUserMaxUpTime() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		try {
			queryBySqlInner("select max(lastuptime) maxuptime from countermanager where storeid='%s'", storeid);
			next();
			String rjson = "\"" + getDate2("maxuptime", "yyyy-MM-dd HH:mm:ss", "") + "\"";
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"maxuptime\":"
					+ rjson + "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取专柜管理员数据 */
	public String SyscCounterUserQuery() {
		String outresult = "";
		String[] vparams = { __Request.getParameter("storeid").toString(),
				__Request.getParameter("maxuptime").toString() };
		try {
			queryBySqlIDWithParamInner("R_COUNTERUSERQUERY", vparams);
			String rjson = formatResultToJson("");
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"data\":" + rjson
					+ "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 专柜管理员数据更新 */
	public String SyscCounterUserUpdate() {
		String outresult = "";

		try {
			JSONObject inpJson = new JSONObject(__Request.getParameter("data").toString());
			JSONArray inpArray = inpJson.getJSONArray("data");

			int i = 0;
			int rcount = inpArray.length();

			dataAllSqls = new String[rcount + 1];

			dataAllSqls[0] = "delete from countermanager_tran";

			createInsertValues("countermanager_tran", inpArray, 1, rcount);

			i = executeSqls(dataAllSqls);
			if (i == 0) {
				i = 1;
				outresult = "更新成功 " + rcount + " 条记录";
				executeOracleProc("trancountermanager", "systemauto", 1, null, 4);
			} else {
				i = 0;
				outresult = _errorInf;
			}

			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"" + i + "\",\"sub_msg\":\"" + outresult
					+ "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取专柜管理员最大更新时间 */
	public String SyscSalesMaxUpTime() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		try {
			queryBySqlInner("select max(lastuptime) maxuptime from cashier where storeid='%s'", storeid);
			next();
			String rjson = "\"" + getDate2("maxuptime", "yyyy-MM-dd HH:mm:ss", "") + "\"";
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"maxuptime\":"
					+ rjson + "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取专柜管理员数据 */
	public String SyscSalesQuery() {
		String outresult = "";
		String[] vparams = { __Request.getParameter("storeid").toString(),
				__Request.getParameter("maxuptime").toString() };
		try {
			queryBySqlIDWithParamInner("R_SALESQUERY", vparams);
			String rjson = formatResultToJson("");
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"data\":" + rjson
					+ "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 营业员数据更新 */
	public String SyscSalesUpdate(String scbh, String gwbh, String rylb) {
		String outresult = "";
		String faceName = (rylb.equals("2")) ? "PostSales" : "PostCashier";
		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "scbh_in", scbh, "gwbh_in", gwbh, "skyjb_in", rylb };
			JSONObject outgmp = new JSONObject();

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/getUser.ihtml", vparams)
					.trim();

			P(inresult);
			JSONObject ingmp = new JSONObject(inresult);

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1") || ingmp.has("user_out")) {
					JSONArray user = ingmp.getJSONArray("user_out");
					if (user.length() > 0) {
						JSONArray sales = new JSONArray();
						for (int i = 0; i < user.length(); i++) {
							JSONObject o = new JSONObject();
							JSONObject p = user.getJSONObject(i);
							o.put("storeid", p.getString("SCBH"));
							o.put("counterid", p.getString("gwbh"));
							o.put("departid", (p.has("bmbh")) ? p.getString("bmbh") : "");
							o.put("supplyid", (p.has("gysbh")) ? p.getString("gysbh") : "");
							o.put("snumber", p.getString("skyh"));
							o.put("name", p.getString("skym"));
							o.put("createdtime", p.getString("GXRQ"));
							o.put("status", "1");
							o.put("barcode", "");
							o.put("lastupdatedtime", p.getString("GXRQ"));
							sales.put(o);
						}
						inresult = crequest.sendPostFmt(CInitParam.SyscServerUrl + faceName,
								new String[] { sales.toString() }, true).trim();

						ingmp = new JSONObject(inresult);

						if (ingmp.has("code")) {
							if (ingmp.getString("code").equals("000")) {
								outgmp.put("sub_code", "1");
								outgmp.put("sub_msg", "ok");
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("message"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", inresult);
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", "没有需要传输的数据");
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/* 营业员数据更新 */
	public String SyscSalesUpdate(String rylb) {
		return SyscSalesUpdate(__Request.getParameter("scbh").toString(), __Request.getParameter("gwbh").toString(),
				rylb);
	}

	/* 获取商品最大更新时间 */
	public String SyscProductsMaxUpTime() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		try {
			queryBySqlInner("select max(lastuptime) maxuptime from products where storeid='%s'", storeid);
			next();
			String rjson = "\"" + getDate2("maxuptime", "yyyy-MM-dd HH:mm:ss", "") + "\"";
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"maxuptime\":"
					+ rjson + "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取商品数据 */
	public String SyscProductsQuery() {
		String outresult = "";
		String[] vparams = { __Request.getParameter("storeid").toString(), __Request.getParameter("maxuptime").toString() };
		try {
			queryBySqlIDWithParamInner("R_PRODUCTSQUERY", vparams);
			String rjson = formatResultToJson("");
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\",\"data\":" + rjson
					+ "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 商品数据更新 */
	public String SyscProductsUpdate() {
		String outresult = "";

		try {
			String maxtime = "";

			queryBySqlInner(
					"select max(to_char(lastupdateddate,'yyyyMMddHH24miss')||to_char(1000000000+id)) t from products");
			if (next()) {
				maxtime = getString(1);
			}
			if (maxtime.equals(""))
				maxtime = "201601010000000000000000";

			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			P(CInitParam.SyscServerUrl + "GetProducts/" + maxtime);

			String inResult = crequest.sendGet(CInitParam.SyscServerUrl + "GetProducts/" + CUtil.escape(maxtime));

			P(inResult);
			JSONObject inpJson = new JSONObject(inResult);

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (inpJson.has("code")) {
				if (inpJson.getString("code").equals("000")) {
					if (inpJson.getInt("count") > 0) {
						JSONArray inpArray = inpJson.getJSONArray("products");

						int rcount = inpArray.length();

						dataAllSqls = new String[rcount + 1];

						dataAllSqls[0] = "delete from Products_tran";

						createInsertValues("Products_tran", inpArray, 1, rcount);

						if (executeSqls(dataAllSqls) == 0) {
							outgmp.put("sub_code", "1");
							outgmp.put("sub_msg", "更新成功 " + rcount + " 条记录");
							executeOracleProc("tranproducts", "systemauto", 1, null, 4);
						} else {
							outgmp.put("sub_code", "1");
							outgmp.put("sub_msg", _errorInf);
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", "没有需要更新的商品");
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", inpJson.getString("message"));
				}
			} else {
				outgmp.put("sub_code", "0");
				if (inpJson.has("yxbz_out")) {
					outgmp.put("sub_msg", inpJson.getString("fhxx_out"));
				} else {
					outgmp.put("sub_msg", inResult);
				}
			}

			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 日结接口 */
	public String SycsShift() {
		String outresult = "";
		String[] vparams = { __Request.getParameter("storeid").toString(), __Request.getParameter("counterid").toString(),
				__Request.getParameter("cashierid").toString(), __Request.getParameter("cashier").toString(),
				__Request.getParameter("shiftdate").toString() };
		int[] paramType = { 1, 1, 1, 1, 1 };
		int[] paramOut = { 1 };
		try {
			if (executeOracleProc("CallShift", vparams, paramType, paramOut, 4) == 0) {
				String shiftid = vPrcoReturn[2];

				queryBySqlInner("select * from PosShift where ShiftId=%s", shiftid);
				String mjson = formatResultToJson(); // 主表

				queryBySqlInner("select * from PosShiftPayment where ShiftId=%s", shiftid);
				String pjson = formatResultToJson("", false); // 支付表

				outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\"," + mjson
						+ ",\"data\":" + pjson + "}";

			} else {
				outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"" + "0\",\"sub_msg\":\"" + _errorInf
						+ "\"}";
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取订单明细 */
	public String SycsBillInfo(String billid) {
		String outresult = "";
		try {
			queryBySqlInner("select * from PosBill where BillId='%s'", billid);
			String mjson = formatResultToJson("", false); // 主表

			queryBySqlInner("select * from PosBillProducts where BillId='%s'", billid);
			String gjson = formatResultToJson("", false); // 商品表

			queryBySqlInner("select * from PosBillPayment where BillId='%s'", billid);
			String pjson = formatResultToJson("", false); // 支付表

			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\"," + "\"head\":"
					+ mjson + ",\"products\":" + gjson + ",\"payment\":" + pjson + "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public String CheckErpBillIdIsExists(String billid) {
		String outresult = "";
		try {
			CURLConnection crequest0 = new CURLConnection();
			crequest0.setDefaultContentEncoding("utf-8");

			String inresult0 = crequest0.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/queryDh.ihtml?",
					new String[] { "dh_in", billid }).trim();
			JSONObject ingmp0 = new JSONObject(inresult0);

			if (ingmp0.has("yxbz_out")) {
				if (ingmp0.getString("yxbz_out").equals("0")) {
					outresult = errorMsgToJson("ERP已存在订单号" + billid + ",请撤单后重新过机");
				}
			} else {
				outresult = errorMsgToJson("检查ERP订单号是否重复失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
		}
		return outresult;
	}

	/* 订单数据更新 */
	public String BillUpdate() {
		String billkey = __Request.getParameter("billkey").toString();

		String gd = __Request.getParameter("gd").toString(); // 挂单标记
		boolean isdc = __Request.getParameter("dc").toString().equals("1"); // 数据是否有变化
		boolean hascp = __Request.getParameter("cp").toString().equals("1");

		String outresult = "";

		try {

			int b = 1;

			if (isdc) { // 数据有变化，先保存数据
				b = 0;
				P(__Request.getParameter("data").toString());

				JSONObject inpJson = new JSONObject(__Request.getParameter("data").toString());

				JSONArray hArray = inpJson.getJSONArray("head");

				outresult = CheckErpBillIdIsExists(hArray.getJSONObject(0).getString("billid"));

				if (!outresult.equals("")) {
					return CUtil.formatJson(outresult);
				}

				JSONArray gArray = inpJson.getJSONArray("products");
				JSONArray pArray = inpJson.getJSONArray("payment");

				int rcount = hArray.length() + gArray.length() + pArray.length();

				dataAllSqls = new String[rcount + 3];

				dataAllSqls[0] = "delete from PosBill0 where billkey=" + billkey;
				dataAllSqls[1] = "delete from PosBillProducts0 where billkey=" + billkey;
				dataAllSqls[2] = "delete from PosBillPayment0 where billkey=" + billkey;

				int index = createInsertValues("PosBill0", hArray, 3, hArray.length());
				index = createInsertValues("PosBillProducts0", gArray, index, gArray.length());
				createInsertValues("PosBillPayment0", pArray, index, pArray.length());

				b = executeSqls(dataAllSqls);
				if (b == 0) {
					b = 1;
					String[] sqlParams = { __Request.getParameter("storeid").toString(),
							__Request.getParameter("counterid").toString(), __Request.getParameter("cashierid").toString(),
							billkey, gd };

					int[] paramType = { 1, 1, 1, 2 };

					if (executeOracleProc("BillSave", sqlParams, paramType, null, 4) != 0) { // 执行存储过程
						throw new Exception("执行过程异常：\n " + _errorInf);
					}
					outresult = "更新成功 " + rcount + " 条记录";
				} else {
					b = 0;
					outresult = _errorInf;
				}
			} else {
				queryBySqlInner("select billid from posbill where billkey=%s", billkey);
				next();

				outresult = CheckErpBillIdIsExists(getString("billid"));

				if (!outresult.equals("")) {
					return CUtil.formatJson(outresult);
				}
			}

			P("cp=" + __Request.getParameter("cp").toString());

			if (b == 1) { // 数据保存成功

				JSONObject outgmp = new JSONObject();

				if (hascp && gd.equals("0")) { // 计算电子优惠券,选择了券种并且不是挂单

					outgmp.put("code", "1");
					outgmp.put("msg", "sucess");

					queryBySqlIDWithParamInner("CalCouponQpreuse", billkey);
					JSONArray jArray = new JSONArray();
					while (next()) {
						JSONObject o = new JSONObject();
						o.put("dh", getString("dh"));
						o.put("ct_code", getString("ct_code"));
						o.put("yqxsje", getString("yqxsje"));
						o.put("fqxsje", getString("fqxsje"));
						jArray.put(o);
					}

					JSONObject po = new JSONObject();

					queryBySqlInner("select count(*) r " + "from posbillproducts where billkey=%s and "
							+ "     (USEDCOUPONTYPE='M' or GAINCOUPONTYPE='M')", billkey);
					next();
					int r = getInt("R");

					queryBySqlInner("select vipid,viptype,promo_id,promo_id_vip " + "from posbill where billkey=%s",
							billkey);
					next();

					po.put("kh_in", getString("vipid"));
					po.put("klx_in", getString("viptype"));

					if (r > 0) // 会员券
						po.put("promo_id_in", getString("promo_id_vip"));
					else
						po.put("promo_id_in", getString("promo_id"));

					po.put("dzquse_in", jArray);

					CURLConnection crequest = new CURLConnection();
					crequest.setDefaultContentEncoding("utf-8");

					String url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQpreuse.ihtml";
					String inresult = crequest.sendPostEx(url, new String[] { po.toString() }, true).trim();

					P(inresult);
					try {
						JSONObject ingmp = new JSONObject(inresult);
						if (ingmp.has("yxbz_out")) {
							if (ingmp.getInt("yxbz_out") == 1) {
								outgmp.put("sub_code", "1");
								outgmp.put("sub_msg", "成功");
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", inresult);
						}
						outresult = outgmp.toString();
					} catch (Exception e) {
						outresult = errorMsgToJson(inresult + "(DZQpreuse)");
					}
				} else
					outresult = sucessMsgToJson(outresult);
			} else {
				outresult = errorMsgToJson(outresult);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 订单数据更新 */
	public String BillUpdateNew() {

		String obillkey = __Request.getParameter("billkey").toString();
		String billkey = __Request.getParameter("billkey").toString();
		String billid = __Request.getParameter("billid").toString();

		// String counterid = request.getParameter("counterid").toString();
		// String cashierid = request.getParameter("cashierid").toString();

		String gd = __Request.getParameter("gd").toString(); // 挂单标记

		String outresult = CheckErpBillIdIsExists(billid);

		if (!outresult.equals("")) {
			return CUtil.formatJson(outresult);
		}

		try {

			boolean isdc = __Request.getParameter("dc").toString().equals("1"); // 数据是否有变化
			boolean hascp = __Request.getParameter("cp").toString().equals("1");
			boolean isnewbillid = false;
			int b = 1;
			/*
			 * CURLConnection crequest0 = new CURLConnection();
			 * crequest0.setDefaultContentEncoding("utf-8");
			 * 
			 * String inresult0 = crequest0.sendPostEx( CInitParam.ErpServerUrl
			 * + "ShoppePay/spay/queryDh.ihtml?", new String[] { "dh_in", billid
			 * }).trim(); JSONObject ingmp0 = new JSONObject(inresult0);
			 * 
			 * if (ingmp0.has("yxbz_out")) { if
			 * (ingmp0.getString("yxbz_out").equals("0")) { isnewbillid = true;
			 * if(executeOracleProc("GetBillKey", new String[] { counterid,
			 * cashierid, "0" }, new int[] { 1, 1, 2 }, new int[] { 2, 1 },
			 * 4)!=0){ throw new Exception("刷新订单号失败。"); } billkey =
			 * vPrcoReturn[2]; billid = vPrcoReturn[3]; } } else { b = 0;
			 * outresult = getGmpErrorMsg(ingmp0); }
			 */
			if (b == 1) {
				if (isdc) { // 数据有变化，先保存数据
					b = 0;
					P(__Request.getParameter("data").toString());
					JSONObject inpJson = new JSONObject(__Request.getParameter("data").toString());

					JSONArray hArray = inpJson.getJSONArray("head");
					JSONArray gArray = inpJson.getJSONArray("products");
					JSONArray pArray = inpJson.getJSONArray("payment");

					int rcount = hArray.length() + gArray.length() + pArray.length();

					dataAllSqls = new String[rcount + 3];

					dataAllSqls[0] = "delete from PosBill0 where billkey=" + obillkey;
					dataAllSqls[1] = "delete from PosBillProducts0 where billkey=" + obillkey;
					dataAllSqls[2] = "delete from PosBillPayment0 where billkey=" + obillkey;

					int index = createInsertValues("PosBill0", hArray, 3, hArray.length());
					index = createInsertValues("PosBillProducts0", gArray, index, gArray.length());
					createInsertValues("PosBillPayment0", pArray, index, pArray.length());

					b = executeSqls(dataAllSqls);
					if (b == 0) {
						b = 1;
						if (executeOracleProc("BillSave",
								new String[] { __Request.getParameter("storeid").toString(),
										__Request.getParameter("counterid").toString(),
										__Request.getParameter("cashierid").toString(), billkey },
								new int[] { 1, 1, 1, 2 }, null, 4) != 0) { // 执行存储过程
							throw new Exception("保存订单失败。");
						}
						outresult = "更新成功 " + rcount + " 条记录";
						/*
						 * if (isnewbillid) {
						 * if(executeOracleProc("BillSaveNew", new String[] {
						 * request.getParameter("storeid").toString(),
						 * request.getParameter("counterid") .toString(),
						 * request.getParameter("cashierid") .toString(),
						 * obillkey, billkey, billid }, new int[] { 1, 1, 1, 2,
						 * 2, 1 }, null, 4)!=0){ // 执行存储过程 throw new
						 * Exception("保存订单失败。"); } outresult = "更新成功 " + rcount
						 * + " 条记录"; } else { if(executeOracleProc("BillSave",
						 * new String[] {
						 * request.getParameter("storeid").toString(),
						 * request.getParameter("counterid") .toString(),
						 * request.getParameter("cashierid") .toString(),
						 * billkey }, new int[] { 1, 1, 1, 2 }, null, 4)!=0){ //
						 * 执行存储过程 throw new Exception("保存订单失败。"); } outresult =
						 * "更新成功 " + rcount + " 条记录"; }
						 */
					} else {
						b = 0;
						outresult = _errorInf;
					}
				} else {
					if (isnewbillid) {
						if (executeOracleProc("BillUpdateBillId",
								new String[] { __Request.getParameter("storeid").toString(),
										__Request.getParameter("counterid").toString(),
										__Request.getParameter("cashierid").toString(), obillkey, billkey, billid },
								new int[] { 1, 1, 1, 2, 2, 1 }, null, 4) != 0) { // 执行存储过程
							throw new Exception("更新订单号失败。");
						}
					}
				}
			}

			P("cp=" + __Request.getParameter("cp").toString());

			if (b == 1) { // 数据保存成功

				JSONObject outgmp = new JSONObject();

				if (hascp && gd.equals("0")) { // 计算电子优惠券,选择了券种并且不是挂单

					outgmp.put("code", "1");
					outgmp.put("msg", "sucess");
					outgmp.put("billkey", billkey);
					outgmp.put("billid", billid);

					queryBySqlIDWithParamInner("CalCouponQpreuse", billkey);
					JSONArray jArray = new JSONArray();
					while (next()) {
						JSONObject o = new JSONObject();
						o.put("dh", getString("dh"));
						o.put("ct_code", getString("ct_code"));
						o.put("yqxsje", getString("yqxsje"));
						o.put("fqxsje", getString("fqxsje"));
						jArray.put(o);
					}

					JSONObject po = new JSONObject();

					queryBySqlInner("select count(*) r " + "from posbillproducts where billkey=%s and "
							+ "     (USEDCOUPONTYPE='M' or GAINCOUPONTYPE='M')", billkey);
					next();
					int r = getInt("R");

					queryBySqlInner("select vipid,viptype,promo_id,promo_id_vip " + "from posbill where billkey=%s",
							billkey);
					next();

					po.put("kh_in", getString("vipid"));
					po.put("klx_in", getString("viptype"));

					if (r > 0) // 会员券
						po.put("promo_id_in", getString("promo_id_vip"));
					else
						po.put("promo_id_in", getString("promo_id"));

					po.put("dzquse_in", jArray);

					CURLConnection crequest = new CURLConnection();
					crequest.setDefaultContentEncoding("utf-8");

					String url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQpreuse.ihtml";
					String inresult = crequest.sendPostEx(url, new String[] { po.toString() }, true).trim();

					P(inresult);
					try {
						JSONObject ingmp = new JSONObject(inresult);
						if (ingmp.has("yxbz_out")) {
							if (ingmp.getInt("yxbz_out") == 1) {
								outgmp.put("sub_code", "1");
								outgmp.put("sub_msg", "成功");
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", inresult);
						}
						outresult = outgmp.toString();
					} catch (Exception e) {
						outresult = errorMsgToJson(inresult + "(DZQpreuse)");
					}
				} else
					outresult = sucessMsgToJson(outresult, new String[] { "billkey", billkey, "billid", billid });
			} else {
				outresult = errorMsgToJson(outresult);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 订单传递到供应量 */
	public String BillToSysc(String billkey) {
		String outresult = "";
		try {
			queryBySqlInner("select * from PosBill where billkey='%s'", billkey);
			JSONArray mjson = formatResultToJsonArray(); // 主表
			queryBySqlInner("select * from PosBillProducts where billkey='%s'", billkey);
			JSONArray gjson = formatResultToJsonArray(); // 商品表
			queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
			JSONArray pjson = formatResultToJsonArray(); // 支付表

			JSONObject o = new JSONObject();
			o.put("head", mjson);
			o.put("products", gjson);
			o.put("payment", pjson);

			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest
					.sendPostEx(CInitParam.SyscServerUrl + "PostBill", new String[] { o.toString() }, true).trim();

			P(inresult);
			JSONObject ingmp = new JSONObject(inresult);

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("code")) {
				if (ingmp.getString("code").equals("000")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "ok");

					executeUpdate("update posbill set rid=-1 where billkey=%s", new String[] { billkey });
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("code") + ingmp.getString("message"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "未知错误");
			}

			outresult = outgmp.toString();

		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 订单传递到供应量 */
	public String BillToSysc() {
		return CUtil.formatJson(BillToSysc(__Request.getParameter("billkey")));
	}

	/* 电子用券信息计算 */
	public String BillECalUse() {
		String outresult = "";
		try {
			String billkey = __Request.getParameter("billkey").toString();
			String kh = __Request.getParameter("kh").toString();
			String klx = __Request.getParameter("klx").toString();
			String promo_id = __Request.getParameter("promo_id").toString();

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			queryBySqlIDWithParamInner("CalCouponQpreuse", billkey);
			JSONArray jArray = new JSONArray();
			while (next()) {
				JSONObject o = new JSONObject();
				o.put("dh", getString("dh"));
				o.put("ct_code", getString("ct_code"));
				o.put("yqxsje", getString("yqxsje"));
				o.put("fqxsje", getString("fqxsje"));
				jArray.put(o);
			}

			JSONObject po = new JSONObject();
			po.put("dzquse_in", jArray);
			po.put("kh_in", kh);
			po.put("klx_in", klx);
			po.put("promo_id_in", promo_id);

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQpreuse.ihtml";
			String inresult = crequest.sendPostEx(url, new String[] { po.toString() }, true).trim();
			JSONObject ingmp = new JSONObject(inresult);
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					outgmp.put("dzquse", ingmp.getJSONArray("dzquse_cur_out"));
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}
			outresult = outgmp.toString();
			P(inresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(outresult);
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 商品信息补录更新 */
	public String BillProductsUpdate() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();

		String[] addFields = { "storeid", "counterid", "cashierid" };
		String[] addValues = { storeid, counterid, cashierid };

		try {
			JSONObject inpJson = new JSONObject(__Request.getParameter("data").toString());
			JSONArray dArray = inpJson.getJSONArray("data");

			int rcount = dArray.length();

			dataAllSqls = new String[rcount + 1];

			dataAllSqls[0] = "delete from posbillproducts_d_tran where counterid='" + counterid + "'";

			createInsertValues("posbillproducts_d_tran", dArray, 1, rcount, addFields, addValues);

			int i = executeSqls(dataAllSqls);
			if (i == 0) {
				i = 1;
				int[] paramType = { 1, 1, 1 };
				executeOracleProc("TranBillProducts", addValues, paramType, null, 4);
				outresult = "更新成功 " + rcount + " 条记录";
			} else {
				i = 0;
				outresult = _errorInf;
			}

			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"" + i + "\",\"sub_msg\":\"" + outresult
					+ "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\"" + outresult + "\"}";
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 商品库存变动保存 */
	public String StockChangeUpdate() {
		String outresult = "";
		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String gysid = __Request.getParameter("gysid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();

		String[] addFields = { "storeid", "counterid", "gysid", "cashierid" };
		String[] addValues = { storeid, counterid, gysid, cashierid };

		try {
			JSONObject inpJson = new JSONObject(__Request.getParameter("data").toString());
			JSONArray dArray = inpJson.getJSONArray("data");

			int rcount = dArray.length();

			dataAllSqls = new String[rcount + 1];

			dataAllSqls[0] = "delete from stockhis_tran " + "where counterid='" + counterid + "' and gysid='" + gysid
					+ "'";

			createInsertValues("stockhis_tran", dArray, 1, rcount, addFields, addValues);

			int i = executeSqls(dataAllSqls);
			if (i == 0) {
				i = 1;
				int[] paramType = { 1, 1, 1, 1 };
				executeOracleProc("TranStockHis", addValues, paramType, null, 4);
				outresult = "更新成功 " + rcount + " 条记录";
			} else {
				i = 0;
				outresult = _errorInf;
			}

			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"" + i + "\",\"sub_msg\":\"" + outresult
					+ "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public void BillFromErp() {
		Vector<String> vector = new Vector<String>();
		P("现金订单检查");
		try {
			queryBySqlInner("select billid,billstate from posbill where billstate=1 and paidbycash=1");
			while (next()) {
				vector.add(getString("billid"));
			}
		} catch (Exception e) {
			_errorInf = CUtil.getOrigTime() + "\n" + e.getMessage() + "\n";
			CFile.appendToFile(CInitParam.appPath + "/bin/logs/" + CUtil.getOrigDate("") + ".txt", _errorInf);
		} finally {
			closeConn();
		}

		if (vector.size() > 0) {
			try {
				CURLConnection crequest = new CURLConnection();
				crequest.setDefaultContentEncoding("utf-8");

				String[] billids = new String[vector.size()];
				vector.copyInto(billids);
				for (int x = 0; x < billids.length; x++) {

					String billid = billids[x];

					String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/query.ihtml",
							new String[] { "dh_in", billid });
					JSONObject ingmp = new JSONObject(inresult);

					P(inresult);
					if (ingmp.getString("yxbz_out").equals("1")) {
						// 数据库处理
						JSONArray hArray = ingmp.getJSONArray("ecr0001_cur_out");
						JSONArray pArray = ingmp.getJSONArray("ecr0002_cur_out");
						if (pArray.length() > 0) {

							String gmpbillid = hArray.getJSONObject(0).getString("dh");
							int rcount = hArray.length() + pArray.length();

							dataAllSqls = new String[rcount + 2];

							dataAllSqls[0] = "delete from gmpbilltemp where dh='" + gmpbillid + "'";
							dataAllSqls[1] = "delete from gmpbilltemppayment where dh='" + gmpbillid + "'";

							int index = createInsertValues("gmpbilltemp", hArray, 2, hArray.length());
							createInsertValues("gmpbilltemppayment", pArray, index, pArray.length());

							if (executeSqls(dataAllSqls) == 0) {
								executeOracleProc("TranBillPaidPay", new String[] { billid, gmpbillid },
										new int[] { 1, 1 }, null, 4);
							}
						}
					}
				}
			} catch (Exception e) {
				_errorInf = e.getMessage();
			} finally {
				closeConn();
			}
		}
	}

	public void SyscBillToSysc() {
		Vector<String> vector = new Vector<String>();
		try {
			queryBySqlInner("select billkey from posbill where billstate>=3 and nvl(rid,0)>=0");
			while (next()) {
				vector.add(getString("billkey"));
			}
		} catch (Exception e) {
			_errorInf = e.getMessage();
		} finally {
			closeConn();
		}

		if (vector.size() > 0) {
			try {
				String[] billids = new String[vector.size()];
				vector.copyInto(billids);
				for (int x = 0; x < billids.length; x++) {
					String billid = billids[x];
					BillToSysc(billid);
				}
			} finally {
				closeConn();
			}
		}
	}

	public void DataAutoToSysc(boolean bJustTran) {
		try {
			if (!CSyscTran.IsInit) {
				CSyscTran.IsInit = true;
				try {
					queryBySqlInner("select dataid,timelen*isenabled timelen " + "from sys_trancfg order by dataid");
					while (next()) {
						switch (Integer.parseInt(getString("dataid"))) {
						case 1:
							CSyscTran.LoginTimeLen = getInt("timelen");
							break;
						case 2:
							CSyscTran.CounterTimeLen = getInt("timelen");
							break;
						case 3:
							CSyscTran.CashierTimeLen = getInt("timelen");
							break;
						case 4:
							CSyscTran.SalesTimeLen = getInt("timelen");
							break;
						case 5:
							CSyscTran.BillTimeLen = getInt("timelen");
							break;
						case 6:
							CSyscTran.GoodsTimeLen = getInt("timelen");
							break;
						}
					}
				} catch (Exception e) {
					_errorInf = CUtil.getOrigTime() + "\n" + e.getMessage() + "\n";
					CFile.appendToFile(CInitParam.appPath + "/bin/logs/" + CUtil.getOrigDate("") + ".txt", _errorInf);
				} finally {
					closeConn();
				}
			}

			CSyscTran.mtCount = CUtil.getMinuteByDay();

			if (bJustTran || CSyscTran.mtCount % 3 == 0)
				BillFromErp();

			if (bJustTran || (CSyscTran.BillTimeLen > 0 && CSyscTran.mtCount % CSyscTran.BillTimeLen == 0)) {
				P("自动订单数据");
				SyscBillToSysc();
			}
			if (bJustTran || (CSyscTran.LoginTimeLen > 0 && CSyscTran.mtCount % CSyscTran.LoginTimeLen == 0)) {
				P("自动上传签到签退数据");
				SyscLoginLogUpdate();
			}

			if (bJustTran || (CSyscTran.CashierTimeLen > 0 && CSyscTran.mtCount % CSyscTran.CashierTimeLen == 0)) {
				P("自动上传收银员数据");
				SyscSalesUpdate("", "", "1");
			}

			if (bJustTran || (CSyscTran.SalesTimeLen > 0 && CSyscTran.mtCount % CSyscTran.SalesTimeLen == 0)) {
				P("自动上传营业员数据");
				SyscSalesUpdate("", "", "2");
			}

			if (bJustTran || (CSyscTran.CounterTimeLen > 0 && CSyscTran.mtCount % CSyscTran.CounterTimeLen == 0)) {
				P("自动上传专柜数据");
				SyscCounterUpdate("", "");
			}

			if (bJustTran || (CSyscTran.GoodsTimeLen > 0 && CSyscTran.mtCount % CSyscTran.GoodsTimeLen == 0)) {
				P("自动获取商品资料");
				SyscProductsUpdate();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConn();
			// CSyscTran.mtCount++;
			// CSyscTran.mtCount %= 1440;
		}
	}
}
