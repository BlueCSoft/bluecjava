package bluec.gmp;

import java.sql.SQLException;

import bluec.base.*;
//import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import org.json.JSONStringer;  

public class CGmpObject extends CJson {

	public CGmpObject(HttpServletRequest request) {
		super(request);
	}

	/*
	 * 登录
	 */
	public String GmpLogin(String url) {
		String outresult = "";

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String cashierid = __Request.getParameter("cashierid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String storeid = "";

			String mm = __Request.getParameter("pwd").toString();
			mm = CUtil.MD5(mm);

			String[] vparams = { "skyh_in", __Request.getParameter("cashierid").toString(), "skymm_in", mm, "qrcode_in",
					__Request.getParameter("barcode").toString(), "zdh_in", counterid, "ipaddress_in", "127.0.0.1",
					"program_in", "广百移动收款", "version_in", "1.0.0.0" };

			url = CInitParam.ErpServerUrl + "ShoppePay/spay/getLogin.ihtml";
			String inresult = crequest.sendPostEx(url, vparams).trim();
			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					// outgmp.put("sub_code", "1");
					// outgmp.put("sub_msg", "成功");
					String dlatt = "0";
					if (cashierid.equals("")) {
						cashierid = ingmp.getString("skyh_out");
						dlatt = "1";
						vparams[1] = cashierid;
					}
					outgmp.put("cashier", ingmp.getString("skym_out"));
					outgmp.put("gysbh", (ingmp.has("gysbh_out")) ? ingmp.getString("gysbh_out") : "");

					url = CInitParam.ErpServerUrl + "ShoppePay/spay/getInfo.ihtml";

					inresult = crequest.sendPostEx(url, new String[] { "skyh_in", cashierid }).trim();

					ingmp = new JSONObject(inresult);

					/*
					 * if(!ingmp.has("yxbz_out")||
					 * ingmp.getString("yxbz_out").equals("0")){
					 * logger.info(inresult); }
					 */
					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "成功");

						outgmp.put("ct_str", (ingmp.has("ct_str_out")) ? ingmp.getString("ct_str_out") : "");
						outgmp.put("ct_str_vip",
								(ingmp.has("ct_str_vip_out")) ? ingmp.getString("ct_str_vip_out") : "");

						storeid = ingmp.getString("scbh_out");

						// sqlParams[2] = storeid;
						outgmp.put("scbh", storeid);
						outgmp.put("storeid", storeid);

						outgmp.put("gwbh", ingmp.getString("gwbh_out"));
						outgmp.put("bmbh", ingmp.getString("bmbh_out"));

						String[] bm = ingmp.getString("bmmc_out").split(",");
						outgmp.put("bmmc", bm[0]);
						outgmp.put("gwmc", bm[1]);

						outgmp.put("promo_id", (ingmp.has("promo_id_out")) ? ingmp.getString("promo_id_out") : "0");
						outgmp.put("promo_name",
								(ingmp.has("promo_name_out")) ? ingmp.getString("promo_name_out") : "");
						outgmp.put("promo_id_vip",
								(ingmp.has("promo_id_vip_out")) ? ingmp.getString("promo_id_vip_out") : "0");
						outgmp.put("promo_vip_name",
								(ingmp.has("promo_vip_name_out")) ? ingmp.getString("promo_vip_name_out") : "");
						outgmp.put("zfbbz", ingmp.getString("zfbbz_out"));
						outgmp.put("zfbpaytoc", ingmp.getString("zfbpaytoc_out"));

						outgmp.put("wxbz", ingmp.getString("wxbz_out"));
						outgmp.put("wxpaytoc", ingmp.getString("wxpaytoc_out"));

						outgmp.put("dzqbz", ingmp.getString("dzqbz_out"));
						outgmp.put("skjjkbz", ingmp.getString("skjjkbz_out"));

						JSONArray gmpArray = ingmp.getJSONArray("spml_out");

						// 操作码
						JSONArray sycsArray = new JSONArray();
						for (int i = 0; i < gmpArray.length(); i++) {
							JSONObject gmpobj = gmpArray.getJSONObject(i);
							JSONObject sycsobj = new JSONObject();
							sycsobj.put("code", gmpobj.getString("czm"));
							// sycsobj.put("codeType", "1");
							sycsobj.put("codeType", gmpobj.getString("pm"));
							sycsobj.put("ucount", "0");

							sycsArray.put(sycsobj);
						}
						outgmp.put("poscode", sycsArray);

						// 营业员

						JSONArray salesArray = new JSONArray();

						inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/getUser.ihtml",
								new String[] { "scbh_in", outgmp.getString("scbh"), "gwbh_in", outgmp.getString("gwbh"),
										"skyjb_in", "2" })
								.trim();
						P(inresult);
						ingmp = new JSONObject(inresult);

						if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
							JSONArray users = ingmp.getJSONArray("user_out");
							for (int i = 0; i < users.length(); i++) {
								JSONObject salesobj = new JSONObject();
								JSONObject o = users.getJSONObject(i);
								salesobj.put("salesid", o.getString("skyh"));
								salesobj.put("sales", o.getString("skym"));
								salesArray.put(salesobj);
							}
						} else {
							throw new Exception("获取营业员数据失败");
						}

						outgmp.put("sales", salesArray);

						url = CInitParam.ErpServerUrl + "ShoppePay/spay/getTicketInfo";

						inresult = crequest.sendPostEx(url, new String[] { "scbh_in", outgmp.getString("scbh"),
								"gwbh_in", outgmp.getString("gwbh") }).trim();
						ingmp = new JSONObject(inresult);

						if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
							JSONObject ticket = new JSONObject();
							ticket.put("head", ingmp.getJSONArray("head_out"));
							ticket.put("body", ingmp.getJSONArray("qty_out"));
							ticket.put("foot", ingmp.getJSONArray("foot_out"));
							outgmp.put("ticket", ticket);
						} else {
							throw new Exception("获取小票格式数据失败");
						}

						// 获取专柜促销信息

						JSONArray spromoArray = new JSONArray();

						inresult = crequest
								.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/counterpromoList.ihtml",
										new String[] { "scbh_in", outgmp.getString("scbh"), "gwbh_in",
												outgmp.getString("gwbh") })
								.trim();

						ingmp = new JSONObject(inresult);

						if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
							JSONArray splist = ingmp.getJSONArray("list_out");
							for (int i = 0; i < splist.length(); i++) {
								JSONObject sp = new JSONObject();
								JSONObject o = splist.getJSONObject(i);
								sp.put("spid", o.getString("ycp_promid"));
								sp.put("spdesc", o.getString("ycp_desc"));
								spromoArray.put(sp);
							}
						} else {
							throw new Exception("获取专柜促销数据失败");
						}

						outgmp.put("spinfo", spromoArray);

						// 支付方式
						JSONArray paymentArray = new JSONArray();

						try {
							executeQuery("select code,cname from pub_payment order by code");
							while (next()) {
								JSONObject aobj = new JSONObject();
								aobj.put("paymentcode", getString(1));
								aobj.put("payment", getString(2));
								paymentArray.put(aobj);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							closeConn();
						}

						outgmp.put("payment", paymentArray);

						// 商品资料
						JSONArray goodsArray = new JSONArray();

						try {
							executeQuery3("select pnumber,pname from products where supplyid='%s' order by pnumber",
									outgmp.getString("gysbh"));
							while (next()) {
								JSONObject aobj = new JSONObject();
								aobj.put("pnumber", getString(1));
								aobj.put("pname", getString(2));
								goodsArray.put(aobj);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							closeConn();
						}

						outgmp.put("products", goodsArray);

						String[] sqlParams = { cashierid, outgmp.getString("cashier"), storeid, counterid,
								outgmp.getString("gysbh"), outgmp.getString("gwbh"), outgmp.getString("bmbh"), "0",
								dlatt };

						executeOracleProc("LoginOrOut", sqlParams, new int[] { 1, 1, 1, 1, 1, 1, 1, 2, 2 }, null, 4); // 记录登录信息

						if (executeOracleProc("GetBillKey", new String[] { counterid, cashierid, "0" },
								new int[] { 1, 1, 2 }, new int[] { 2, 1 }, 4) != 0) { // 执行存储过程
							throw new Exception("获取单据号失败。");
						}

						String bidkey = vPrcoReturn[2];

						outgmp.put("origbillkey", bidkey); // 当前订单key
						outgmp.put("origbillid", vPrcoReturn[3]); // 当前订单号

						// 查询未处理完订单数
						executeQuery3("select count(*) r from PosBill "
								+ "where counterid='%s' and cashierid='%s' and billstate between -2 and 2 and "
								+ "      billkey<>%s", new String[] { counterid, cashierid, bidkey });
						next();
						outgmp.put("savebillnum", getString(1));

						if (executeOracleProc("CheckYesterDayShift", new String[] { counterid, cashierid },
								new int[] { 1, 1 }, new int[] { 2 }, 4) != 0) { // 执行存储过程
							throw new Exception("执行日结检查失败。");
						}

						outgmp.put("endofday", vPrcoReturn[2]); // 作日日结状态

						executeQuery3("select count(*) r from PosBillProducts where billkey=%s", bidkey);
						next();
						outgmp.put("billgoodsnum", getString(1)); // 当前订单商品数

						outgmp.put("paramdef1", "1");
						outgmp.put("paramdef2", "1");
						outgmp.put("paramdef3", "");
						outgmp.put("paramdef4", "");
						outgmp.put("paramdef5", "");

					} else {
						if (ingmp.has("yxbz_out")) {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("msg") + "(gmp)");
						}
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", getGmpErrorMsg(ingmp));
			}

			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/*
	 * 注销
	 */
	public String GmpLogout(String url) {
		String cashier = (__Request.getParameter("cashierid") != null) ? __Request.getParameter("cashierid").toString()
				: "";
		String storeid = (__Request.getParameter("storeid") != null) ? __Request.getParameter("storeid").toString()
				: "";
		String gysbh = (__Request.getParameter("gysbh") != null) ? __Request.getParameter("gysbh").toString() : "";
		String gwbh = (__Request.getParameter("gwbh") != null) ? __Request.getParameter("gwbh").toString() : "";
		String bmbh = (__Request.getParameter("bmbh") != null) ? __Request.getParameter("bmbh").toString() : "";

		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "skyh_in", __Request.getParameter("cashierid").toString(), "zdh_in",
				__Request.getParameter("counterid").toString() };
		url = CInitParam.ErpServerUrl + "ShoppePay/spay/getLogoutInfo.ihtml";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";
		JSONObject outgmp = new JSONObject();

		try {
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));

				executeOracleProc("LoginOrOut",
						new String[] { __Request.getParameter("cashierid").toString(), cashier, storeid,
								__Request.getParameter("counterid").toString(), gysbh, gwbh, bmbh, "1", "0" },
						new int[] { 1, 1, 1, 1, 1, 1, 1, 2, 2 }, null, 4); // 执行存储过程

			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			}
			outgmp.put("storeid", "0001");
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/*
	 * 检查日结情况
	 */
	public String GmpCheckShift() {

		String outresult = "";
		// String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();

		JSONObject outgmp = new JSONObject();

		try {
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			String proc = (__Request.getParameter("att") != null) ? "CheckYesterDayShift" : "CheckShift";

			if (executeOracleProc(proc, new String[] { counterid, cashierid }, new int[] { 1, 1 }, new int[] { 2 },
					4) == 0) { // 执行存储过程
				outgmp.put("sub_code", "1");
				outgmp.put("endofday", vPrcoReturn[2]);
				int n = Integer.parseInt(vPrcoReturn[2]);
				if (n > 0) {
					outgmp.put("sub_msg", "ok");
				} else if (n < 0) {
					outgmp.put("sub_msg", "存在未处理完成的订单");
				} else {
					outgmp.put("sub_msg", "没有待日结的订单");
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("endofday", "0");
				outgmp.put("sub_msg", _errorInf);
			}
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/* 获取订单状态 */
	public String GmpBillStatus() {
		String outresult = "";
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();

		JSONObject outgmp = new JSONObject();

		try {
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "ok");

			queryBySqlInner("select * from sys_billstate order by billstate");
			outgmp.put("data", formatResultToJsonArray());
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);

			outresult = CUtil.formatJson(outgmp.toString());

		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/*
	 * 修改收款员密码
	 */
	public String GmpChangePwd(String url) {

		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "skyh_in", __Request.getParameter("cashierid").toString(), "pwd_old_in",
				CUtil.MD5(__Request.getParameter("oldpwd").toString()), "pwd_new_in",
				CUtil.MD5(__Request.getParameter("newpwd").toString()) };
		url = CInitParam.ErpServerUrl + "ShoppePay/spay/changePwd.ihtml";
		String inresult = crequest.sendPostEx(url, vparams).trim();

		String outresult = "";
		JSONObject outgmp = new JSONObject();

		try {
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			}
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = e.getMessage();
		}

		return outresult;
	}

	/*
	 * 专柜管理员授权登录
	 */
	public String GmpManagerLogin(String url) {

		String outresult = "";

		JSONObject outgmp = new JSONObject();

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "skyh_in", __Request.getParameter("managerid").toString(), "skymm_in",
					CUtil.MD5(__Request.getParameter("pwd").toString()) };
			url = CInitParam.ErpServerUrl + "ShoppePay/spay/athorize.ihtml";
			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			P(inresult);
			if (ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "ok");
				outgmp.put("managerid", __Request.getParameter("managerid").toString());
				outgmp.put("manager", ingmp.getString("skym_out"));
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx"));
			}
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = e.getMessage();
		}

		return outresult;
	}

	/*
	 * 营业员登录
	 */
	public String GmpSalesLogin(String url) {

		String outresult = "";

		JSONObject outgmp = new JSONObject();

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "skyh_in", __Request.getParameter("uid").toString(), "skymm_in",
					CUtil.MD5(__Request.getParameter("pwd").toString()), "scbh_in",
					__Request.getParameter("scbh").toString(), "gwbh_in", __Request.getParameter("gwbh").toString() };
			url = CInitParam.ErpServerUrl + "ShoppePay/spay/clerk.ihtml";
			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "ok");
				outgmp.put("salesid", __Request.getParameter("uid").toString());
				outgmp.put("sales", ingmp.getString("skym_out"));
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			}
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = e.getMessage();
		}

		return outresult;
	}

	/* 获取专柜的操作码和专柜促销数据 */
	public String GmpGetInfo(String cashierid, String counterid, String scbh, String gwbh) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "zdh_in", counterid, "skyh_in", cashierid };
		// http://120.236.153.203:18091/ShoppePay/spay/getInfo.ihtml
		String url = CInitParam.ErpServerUrl + "ShoppePay/spay/getInfo.ihtml";
		// String inresult =
		// "{\"fhxx_out\":\"成功！\",\"yxbz_out\":\"1\",\"spml_out\":"
		// +
		// "[{\"czm\":\"000000898001\"},{\"czm\":\"000000897688\"},{\"czm\":\"000000898123\"}]}";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			// System.out.println(inresult);
			P(inresult);

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					outgmp.put("ct_str", (ingmp.has("ct_str_out")) ? ingmp.getString("ct_str_out") : "");
					outgmp.put("ct_str_vip", (ingmp.has("ct_str_vip_out")) ? ingmp.getString("ct_str_vip_out") : "");

					outgmp.put("promo_id", (ingmp.has("promo_id_out")) ? ingmp.getString("promo_id_out") : "0");
					outgmp.put("promo_name", (ingmp.has("promo_name_out")) ? ingmp.getString("promo_name_out") : "");
					outgmp.put("promo_id_vip",
							(ingmp.has("promo_id_vip_out")) ? ingmp.getString("promo_id_vip_out") : "0");
					outgmp.put("promo_vip_name",
							(ingmp.has("promo_vip_name_out")) ? ingmp.getString("promo_vip_name_out") : "");

					/*
					 * JSONArray spromoArray1 = new JSONArray(); if
					 * (ingmp.has("spml_out")) { JSONArray splist1 =
					 * ingmp.getJSONArray("spml_out"); for (int i = 0; i <
					 * splist1.length(); i++) { JSONObject sp = new
					 * JSONObject(); JSONObject o = splist1.getJSONObject(i);
					 * sp.put("txm", (o.has("txm")) ? o .getString("txm") : "");
					 * sp.put("czm", (o.has("czm")) ? o .getString("czm") : "");
					 * sp.put("pm", (o.has("pm")) ? o .getString("pm") : "");
					 * spromoArray1.put(sp); } } outgmp.put("spml_out",
					 * spromoArray1);
					 */
					// 获取专柜促销信息

					JSONArray spromoArray = new JSONArray();

					inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/counterpromoList.ihtml",
							new String[] { "scbh_in", scbh, "gwbh_in", gwbh }).trim();

					ingmp = new JSONObject(inresult);

					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
						JSONArray splist = ingmp.getJSONArray("list_out");
						for (int i = 0; i < splist.length(); i++) {
							JSONObject sp = new JSONObject();
							JSONObject o = splist.getJSONObject(i);
							sp.put("spid", o.getString("ycp_promid"));
							sp.put("spdesc", o.getString("ycp_desc"));
							spromoArray.put(sp);
						}
					}

					outgmp.put("spinfo", spromoArray);

					// 获取最当前订单号
					if (executeOracleProc("GetBillKey", new String[] { counterid, cashierid, "0" },
							new int[] { 1, 1, 2 }, new int[] { 2, 1 }, 4) != 0) { // 执行存储过程
						throw new Exception("获取单据号失败。");
					}
					String bidkey = vPrcoReturn[2];

					outgmp.put("origbillkey", bidkey); // 当前订单号
					outgmp.put("origbillid", vPrcoReturn[3]); // 当前订单号

					// 查询挂单数量
					executeQuery3("select count(*) r from PosBill "
							+ "where counterid='%s' and cashierid='%s' and billstate between -2 and 2 and "
							+ "      billkey<>%s", new String[] { counterid, cashierid, bidkey });
					next();
					outgmp.put("savebillnum", getString(1));

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			// outgmp.put("counterid", counterid);
			// outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = "异常：" + e.getMessage() + "(" + inresult + ")";
		} finally {
			closeConn();
		}
		// System.out.println(outresult);
		return outresult;
	}

	public String GmpGetInfo() {
		return GmpGetInfo(__Request.getParameter("cashierid").toString(),
				__Request.getParameter("counterid").toString(), __Request.getParameter("scbh").toString(),
				__Request.getParameter("gwbh").toString());
	}

	// 获取信息（竖版）
	public String GmpGetShuBanInfo(String cashierid, String counterid, String scbh, String gwbh) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "zdh_in", counterid, "skyh_in", cashierid };
		String url = CInitParam.ErpServerUrl + "ShoppePay/spay/getInfoVertical.ihtml";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			P(inresult);
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			JSONArray spromoArray1 = new JSONArray();
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					JSONArray splist1 = ingmp.getJSONArray("spml_out");
					for (int i = 0; i < splist1.length(); i++) {
						JSONObject sp = new JSONObject();
						JSONObject o = splist1.getJSONObject(i);
						sp.put("txm", (o.has("txm")) ? o.getString("txm") : "");
						sp.put("czm", (o.has("czm")) ? o.getString("czm") : "");
						sp.put("pm", (o.has("pm")) ? o.getString("pm") : "");
						spromoArray1.put(sp);
					}
					outgmp.put("spml_out", spromoArray1);
					outgmp.put("zfbpaytoc_out", (ingmp.has("zfbpaytoc_out")) ? ingmp.getString("zfbpaytoc_out") : "0");
					outgmp.put("wxpaytoc_out", (ingmp.has("wxpaytoc_out")) ? ingmp.getString("wxpaytoc_out") : "0");

					outgmp.put("zfbbz_out", (ingmp.has("zfbbz_out")) ? ingmp.getString("zfbbz_out") : "0");
					outgmp.put("gwbh_out", (ingmp.has("gwbh_out")) ? ingmp.getString("gwbh_out") : "0");
					outgmp.put("bmmc_out", (ingmp.has("bmmc_out")) ? ingmp.getString("bmmc_out") : "");
					outgmp.put("promo_name_out",
							(ingmp.has("promo_name_out")) ? ingmp.getString("promo_name_out") : "");
					outgmp.put("scbh_out", (ingmp.has("scbh_out")) ? ingmp.getString("scbh_out") : "0");
					outgmp.put("promo_id_out", (ingmp.has("promo_id_out")) ? ingmp.getString("promo_id_out") : "0");
					outgmp.put("gwbz_out", (ingmp.has("gwbz_out")) ? ingmp.getString("gwbz_out") : "0");
					outgmp.put("wxbz_out", (ingmp.has("wxbz_out")) ? ingmp.getString("wxbz_out") : "0");
					outgmp.put("yxbz_out", (ingmp.has("yxbz_out")) ? ingmp.getString("yxbz_out") : "0");
					outgmp.put("fhxx_out", (ingmp.has("fhxx_out")) ? ingmp.getString("fhxx_out") : "0");
					outgmp.put("ct_str_out", (ingmp.has("ct_str_out")) ? ingmp.getString("ct_str_out") : "");
					outgmp.put("dzqbz_out", (ingmp.has("dzqbz_out")) ? ingmp.getString("dzqbz_out") : "0");
					outgmp.put("bmbh_out", (ingmp.has("bmbh_out")) ? ingmp.getString("bmbh_out") : "0");
					outgmp.put("skjjkbz_out", (ingmp.has("skjjkbz_out")) ? ingmp.getString("skjjkbz_out") : "0");
					outgmp.put("ct_str_vip_out",
							(ingmp.has("ct_str_vip_out")) ? ingmp.getString("ct_str_vip_out") : "0");
					outgmp.put("promo_id_vip_out",
							(ingmp.has("promo_id_vip_out")) ? ingmp.getString("promo_id_vip_out") : "0");

					// 获取专柜促销信息
					JSONArray spromoArray = new JSONArray();

					inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/counterpromoList.ihtml",
							new String[] { "scbh_in", scbh, "gwbh_in", gwbh }).trim();

					ingmp = new JSONObject(inresult);

					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
						JSONArray splist = ingmp.getJSONArray("list_out");
						for (int i = 0; i < splist.length(); i++) {
							JSONObject sp = new JSONObject();
							JSONObject o = splist.getJSONObject(i);
							sp.put("spid", o.getString("ycp_promid"));
							sp.put("spdesc", o.getString("ycp_desc"));
							spromoArray.put(sp);
						}
					}
					outgmp.put("spinfo", spromoArray);
					// 获取最当前订单号
					executeOracleProc("GetBillKey", new String[] { counterid, cashierid, "0" }, new int[] { 1, 1, 2 },
							new int[] { 2, 1 }, 4); // 执行存储过程

					String bidkey = vPrcoReturn[2];

					outgmp.put("origbillkey", bidkey); // 当前订单号
					outgmp.put("origbillid", vPrcoReturn[3]); // 当前订单号

					// 查询挂单数量
					executeQuery3("select count(*) r from PosBill "
							+ "where counterid='%s' and cashierid='%s' and billstate between -2 and 2 and "
							+ "      billkey<>%s", new String[] { counterid, cashierid, bidkey });
					next();
					outgmp.put("savebillnum", getString(1));

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();

			outresult = "异常：" + e.getMessage() + "(" + inresult + ")";
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String GmpGetShuBanInfo() {
		return GmpGetShuBanInfo(__Request.getParameter("cashierid").toString(),
				__Request.getParameter("counterid").toString(), __Request.getParameter("scbh").toString(),
				__Request.getParameter("gwbh").toString());
	}

	// 获取日报信息（竖版）
	public String GmpGetRiBaoMingXi(String skyh_in, String dh_in) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "dh_in", dh_in, "skyh_in", skyh_in };
		String url = CInitParam.ErpServerUrl + "ShoppePay/spay/getTicketDetail.ihtml";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			P(inresult);
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			JSONArray spromoArray1 = new JSONArray();
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					JSONArray splist1 = ingmp.getJSONArray("detail_out");
					for (int i = 0; i < splist1.length(); i++) {
						JSONObject sp = new JSONObject();
						JSONObject o = splist1.getJSONObject(i);
						sp.put("xssl", (o.has("xssl")) ? o.getString("xssl") : "");
						sp.put("xsdj", (o.has("xsdj")) ? o.getString("xsdj") : "");
						sp.put("spmc", (o.has("spmc")) ? o.getString("spmc") : "");
						sp.put("czm", (o.has("czm")) ? o.getString("czm") : "");
						sp.put("xsje", (o.has("xsje")) ? o.getString("xsje") : "");
						spromoArray1.put(sp);
					}
					outgmp.put("detail_out", spromoArray1);
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();

			outresult = "异常：" + e.getMessage() + "(" + inresult + ")";
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String GmpGetRiBaoMingXi() {
		return GmpGetRiBaoMingXi(__Request.getParameter("skyh_in").toString(),
				__Request.getParameter("dh_in").toString());
	}

	/* 获取并计算专柜促销券余额 */
	public String GmpCalPromo() {
		/*
		 * String cashierid = __Request.getParameter("cashierid").toString();
		 * String counterid = __Request.getParameter("counterid").toString();
		 */
		String scbh = __Request.getParameter("scbh").toString();
		String gwbh = __Request.getParameter("gwbh").toString();
		String billid = __Request.getParameter("billid").toString();
		String xsje = __Request.getParameter("xsje").toString();

		String outresult = "";

		try {
			// System.out.println(inresult);
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			JSONObject outgmp = new JSONObject();

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			JSONArray spromoArray = new JSONArray();

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/counterpromoList.ihtml",
					new String[] { "scbh_in", scbh, "gwbh_in", gwbh }).trim();

			JSONObject ingmp = new JSONObject(inresult);
			P(inresult);
			if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "ok");

				JSONArray splist = ingmp.getJSONArray("list_out");
				for (int i = 0; i < splist.length(); i++) {
					JSONObject sp = new JSONObject();
					JSONObject o = splist.getJSONObject(i);
					sp.put("spid", o.getString("ycp_promid"));
					sp.put("spdesc", o.getString("ycp_desc"));

					inresult = crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/counterpromoCal.ihtml",
									new String[] { "dh_in", billid, "lsh_in", "0", "ycp_promid_in",
											o.getString("ycp_promid"), "scbh_in", scbh, "gwbh_in", gwbh, "xsje_in",
											xsje })
							.trim();
					ingmp = new JSONObject(inresult);

					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
						sp.put("cxqje", ingmp.getString("cxqje_out"));
					} else
						sp.put("cxqje", "0");

					spromoArray.put(sp);
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			}

			outgmp.put("spdata", spromoArray);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		// System.out.println(outresult);
		return outresult;
	}

	/* 计算专柜促销券余额 */
	public String GmpCalPromoOne() {

		String scbh = __Request.getParameter("scbh").toString();
		String gwbh = __Request.getParameter("gwbh").toString();
		String promid = __Request.getParameter("promid").toString();
		String billid = __Request.getParameter("billid").toString();
		String xsje = __Request.getParameter("xsje").toString();

		String outresult = "";

		try {
			// System.out.println(inresult);
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			JSONObject outgmp = new JSONObject();

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/counterpromoCal.ihtml",
					new String[] { "dh_in", billid, "lsh_in", "0", "ycp_promid_in", promid, "scbh_in", scbh, "gwbh_in",
							gwbh, "xsje_in", xsje })
					.trim();
			JSONObject ingmp = new JSONObject(inresult);

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("cxqje", ingmp.getString("cxqje_out"));
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "ok");
				} else {
					outgmp.put("cxqje", "0");
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 获取营业员数据 */
	public String GmpGetSales(String scbh, String gwbh, String skyjb) {

		String outresult = "";
		JSONObject outgmp = new JSONObject();

		try {
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			JSONArray salesArray = new JSONArray();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/getUser.ihtml",
					new String[] { "scbh_in", scbh, "gwbh_in", gwbh, "skyjb_in", skyjb }).trim();
			JSONObject ingmp = new JSONObject(inresult);

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					JSONArray users = ingmp.getJSONArray("user_out");
					for (int i = 0; i < users.length(); i++) {
						JSONObject salesobj = new JSONObject();
						JSONObject o = users.getJSONObject(i);
						salesobj.put("salesid", o.getString("skyh"));
						salesobj.put("sales", o.getString("skym"));
						salesArray.put(salesobj);
					}
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "ok");
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outgmp.put("sales", salesArray);

			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		// System.out.println(outresult);
		return outresult;
	}

	public String GmpGetSales() {
		String ryjb = (__Request.getParameter("ryjb") != null) ? __Request.getParameter("ryjb").toString() : "2";
		return GmpGetSales(__Request.getParameter("scbh").toString(), __Request.getParameter("gwbh").toString(), ryjb);
	}

	/* 获取商品信息 */
	public String GmpProduct(String url, String counterid, String cashierid, String poscode, String mulzk, String ykjzk,
			String sdzk, String hybz, String spsl) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "skyh_in", cashierid, "inputtext_in", poscode, "mulzk_in", mulzk, "ykjzk_in", ykjzk,
				"zdh_in", counterid, "sdzk_in", sdzk, "hybz_in", hybz, "spsl_in", spsl };

		url = CInitParam.ErpServerUrl + "ShoppePay/spay/getSkuInfo.ihtml";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";
		JSONObject outgmp = new JSONObject();

		try {
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					outgmp.put("poscode", ingmp.getString("czm_out"));
					outgmp.put("zxjj", ingmp.getDouble("zxjj_out"));
					outgmp.put("pjjj", ingmp.getDouble("pjjj_out"));
					outgmp.put("hh", ingmp.getString("hh_out"));
					outgmp.put("scbh", ingmp.getString("scbh_out"));
					outgmp.put("gwbh", ingmp.getString("gwbh_out"));
					outgmp.put("pm", ingmp.getString("pm_out"));
					outgmp.put("jldw", ingmp.has("jldw_out") ? ingmp.getString("jldw_out") : "");
					outgmp.put("lsj", ingmp.getDouble("lsj_out"));
					outgmp.put("czlx", ingmp.getInt("czlx_out"));
					outgmp.put("dkl", ingmp.getDouble("dkl_out"));
					outgmp.put("found", ingmp.getInt("found_out"));
					outgmp.put("tjbz", ingmp.getInt("tjbz_out"));
					outgmp.put("zkbz", ingmp.getInt("zkbz_out"));
					outgmp.put("zhj", ingmp.getDouble("zhj_out"));
					outgmp.put("lowprofit", ingmp.getInt("lowprofit_out"));
					outgmp.put("dysl", ingmp.getInt("dysl_out"));
					outgmp.put("jybz", ingmp.getInt("jyxz_out"));
					outgmp.put("jfbl", ingmp.getDouble("jfbl_out"));
					outgmp.put("hyzk", ingmp.getDouble("hyzk_out"));
					outgmp.put("sdzk", ingmp.getDouble("sdzk_out"));
					outgmp.put("cxbz", ingmp.getInt("cxbz_out"));
					outgmp.put("xgsl", ingmp.getInt("xgsl_out"));
					outgmp.put("buygroup", ingmp.getString("buygroup_out"));
					outgmp.put("nodiscount", ingmp.getString("nodiscount_out"));
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult + "(获取商品信息)");
			}
			outgmp.put("storeid", "0001");
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = e.getMessage() + ":" + inresult;
		}
		// System.out.println(outresult);
		return outresult;
	}

	public String GmpProduct(String url) {
		return GmpProduct(url, __Request.getParameter("counterid").toString(),
				__Request.getParameter("cashierid").toString(), __Request.getParameter("poscode").toString(),
				__Request.getParameter("mulzk").toString(), __Request.getParameter("ykjzk").toString(),
				__Request.getParameter("sdzk").toString(), __Request.getParameter("hybz").toString(),
				__Request.getParameter("spsl").toString());
	}

	/* 获取最大订单号 */
	public String GmpGetMaxBillId(String counterid) {
		String[] vparams = { counterid };
		// http://120.236.153.203:18091/ShoppePay/spay/getLogin.ihtml
		String outresult = "";

		try {

			int[] paramType = { 1 };
			if (executeOracleProc("GetBillId", vparams, paramType, null, 4) == 0) { // 执行存储过程
				outresult = _errorInf;
			} else
				outresult = "";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = e.getMessage();
		}
		return outresult;
	}

	/* 获取支付方式 */
	public String GmpGetPayment(String url, String cashierid, String counterid) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "zdh_in", counterid, "skyh_in", cashierid };
		// http://120.236.153.203:18091/ShoppePay/spay/getInfo.ihtml
		url = CInitParam.ErpServerUrl + "ShoppePay/spay/getPayMode.ihtml";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";

		__gsycsArray = null;
		JSONObject outgmp = new JSONObject();

		try {
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功");
				JSONArray gmpArray = ingmp.getJSONArray("ecr0003_out");

				// 支付方式
				__gsycsArray = new JSONArray();
				for (int i = 0; i < gmpArray.length(); i++) {
					JSONObject gmpobj = gmpArray.getJSONObject(i);
					JSONObject sycsobj = new JSONObject();
					sycsobj.put("code", gmpobj.getString("fsm"));
					sycsobj.put("cname", gmpobj.getString("FSM"));
					sycsobj.put("type", gmpobj.getString("fslh"));
					sycsobj.put("rate", gmpobj.getString("fsbl"));
					sycsobj.put("canzs", gmpobj.getString("zqbz"));

					__gsycsArray.put(sycsobj);
				}
				outgmp.put("data", __gsycsArray);

				int rcount = __gsycsArray.length();

				dataAllSqls = new String[rcount + 1];

				dataAllSqls[0] = "delete from pub_payment";

				createInsertValues("pub_payment", __gsycsArray, 1, rcount);

				executeSqls(dataAllSqls);
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
			}
			outgmp.put("storeid", "0001");
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = e.getMessage() + ":" + inresult;
		} finally {
			closeConn();
		}
		// System.out.println(outresult);
		return outresult;
	}

	public String GmpGetPayment(String url) {
		return GmpGetPayment(url, __Request.getParameter("cashierid").toString(),
				__Request.getParameter("counterid").toString());
	}

	/* 会员卡查询 */
	public String GmpVipInfo(String url) {
		String outresult = "";

		try {
			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			P(__Request.getParameter("track").toString());
			String[] vparams = { "operid_in", __Request.getParameter("operid").toString(), "operno_in",
					__Request.getParameter("operno").toString(), "person_in",
					__Request.getParameter("cashierid").toString(), "termno_in",
					__Request.getParameter("counterid").toString(), "store_in",
					__Request.getParameter("storeid").toString(), "usemode_in",
					__Request.getParameter("usemode").toString(), "track2_in",
					CUtil.escape(__Request.getParameter("track").toString()), "track3_in",
					CUtil.escape(__Request.getParameter("track3").toString()) };

			url = CInitParam.ErpServerUrl + "ShoppePay/crm/spCustsr.ihtml";

			String inresult = crequest.sendPostEx(url, vparams).trim();
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {

					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					outgmp.put("cardno", ingmp.getString("cardno_out"));
					outgmp.put("name", ingmp.getString("name_out"));
					outgmp.put("track2", ingmp.has("track2_out") ? ingmp.getString("track2_out") : "");
					outgmp.put("track3", "");
					outgmp.put("type", ingmp.getString("type_out"));
					outgmp.put("isjf", ingmp.getString("isjf_out"));
					outgmp.put("isfq", ingmp.getString("isfq_out"));
					outgmp.put("ishyj", ingmp.getString("ishyj_out"));
					outgmp.put("iszk", ingmp.getString("iszk_out"));
					outgmp.put("zkl", ingmp.getDouble("zkl_out"));
					outgmp.put("thisjfye", ingmp.getDouble("thisjfye_out"));
					outgmp.put("lastjfye", ingmp.getDouble("lastjfye_out"));
					outgmp.put("ljjf", ingmp.getDouble("ljjf_out"));
					outgmp.put("xjqye", ingmp.getDouble("xjqye_out"));
					outgmp.put("dzqye", 0.0);
					outgmp.put("dzqstr", "");

					queryBySqlInner("select viptypename from sys_viptype where viptypeid='%s'",
							ingmp.getString("type_out"));
					next();
					outgmp.put("typename", getString("viptypename"));

					// 计算可用券数
					if (__Request.getParameter("promo_id") != null) {
						if (__Request.getParameter("billkey") != null) {
							queryBySqlIDWithParamInner("CalCouponQpreuse",
									__Request.getParameter("billkey").toString());
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
							po.put("kh_in", ingmp.getString("cardno_out"));
							po.put("klx_in", ingmp.getString("type_out"));

							if (!__Request.getParameter("promo_id").toString().equals("0")) {
								po.put("promo_id_in", __Request.getParameter("promo_id").toString());
							} else {
								po.put("promo_id_in", __Request.getParameter("promo_id_vip").toString());
							}

							url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQpreuse.ihtml";
							inresult = crequest.sendPostEx(url, new String[] { po.toString() }, true).trim();
							JSONObject ingmp0 = new JSONObject(inresult);
							if (ingmp0.has("yxbz_out")) {
								if (ingmp0.getInt("yxbz_out") == 1) {
									outgmp.put("dzquse", ingmp0.getJSONArray("dzquse_cur_out"));
								} else {
									outgmp.put("sub_code", "0");
									outgmp.put("sub_msg", ingmp0.getString("fhxx_out"));
								}
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", inresult);
							}
						} else { // 获取会员可用券
							String kh = ingmp.getString("cardno_out");
							inresult = crequest.sendPostFmt(
									CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/getDZQInfo.ihtml",
									new String[] { "kh_in", kh, "promo_id_in",
											__Request.getParameter("promo_id").toString() });

							ingmp = new JSONObject(inresult);

							if (ingmp.has("yxbz_out")) {
								if (ingmp.getString("yxbz_out").equals("1")) {
									JSONArray iArray = ingmp.getJSONArray("dzqinfo_out");
									String str = "";
									String cpList = "";
									String[] cpText = new String[10];
									String[] cpPv = new String[10];
									int[] cpQty = new int[10];

									for (int i = 0; i < iArray.length(); i++) {
										JSONObject io = iArray.getJSONObject(i);

										String c = io.getString("ct_code");
										int x = cpList.indexOf(c);
										if (x < 0) {
											cpList += c;
											x = cpList.length() - 1;
											cpText[x] = io.getString("ct_code") + io.getString("ct_name") + "\t";
											cpPv[x] = io.getString("ct_payvalue");
											cpQty[x] = Integer.parseInt(io.getString("ct_qty"));
										} else {
											cpQty[x] += Integer.parseInt(io.getString("ct_qty"));
										}
									}

									for (int i = 0; i < cpList.length(); i++) {
										str += cpText[i] + cpPv[i] + "元×" + cpQty[i] + "张\n";
									}

									outgmp.put("dzqstr", str.trim());
								} else {
									outgmp.put("dzqstr", ingmp.getString("fhxx_out"));
								}
							} else {
								outgmp.put("dzqstr", inresult);
							}

							inresult = crequest.sendPostFmt(
									CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/getDZQInfo.ihtml",
									new String[] { "kh_in", kh, "promo_id_in",
											__Request.getParameter("promo_id_vip").toString() });

							ingmp = new JSONObject(inresult);

							if (ingmp.has("yxbz_out")) {
								if (ingmp.getString("yxbz_out").equals("1")) {
									JSONArray iArray = ingmp.getJSONArray("dzqinfo_out");
									String str = "";
									String cpList = "";
									String[] cpText = new String[10];
									String[] cpPv = new String[10];
									int[] cpQty = new int[10];

									for (int i = 0; i < iArray.length(); i++) {
										JSONObject io = iArray.getJSONObject(i);

										String c = io.getString("ct_code");
										int x = cpList.indexOf(c);
										if (x < 0) {
											cpList += c;
											x = cpList.length() - 1;
											cpText[x] = io.getString("ct_code") + io.getString("ct_name") + "\t";
											cpPv[x] = io.getString("ct_payvalue");
											cpQty[x] = Integer.parseInt(io.getString("ct_qty"));
										} else {
											cpQty[x] += Integer.parseInt(io.getString("ct_qty"));
										}
									}

									for (int i = 0; i < cpList.length(); i++) {
										str += cpText[i] + cpPv[i] + "元×" + cpQty[i] + "张\n";
									}

									outgmp.put("dzqstr_vip", str.trim());
								} else {
									outgmp.put("dzqstr_vip", ingmp.getString("fhxx_out"));
								}
							} else {
								outgmp.put("dzqstr_vip", inresult);
							}

						}

					} else {
						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "成功");
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		// System.out.println(outresult);
		return outresult;
	}

	/* 小票发送过程 */
	public String GmpBillSend(String url) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		String[] vparams = { "vseqno_in", __Request.getParameter("billxh").toString() };
		// http://120.236.153.203:18091/ShoppePay/spay/getInfo.ihtml
		url = CInitParam.ErpServerUrl + "ShoppePay/crm/spcustSendok.ihtml";
		// String inresult =
		// "{\"fhxx_out\":\"成功！\",\"yxbz_out\":\"1\",\"spml_out\":"
		// +
		// "[{\"czm\":\"000000898001\"},{\"czm\":\"000000897688\"},{\"czm\":\"000000898123\"}]}";
		String inresult = crequest.sendPostEx(url, vparams).trim();
		String outresult = "";
		JSONObject outgmp = new JSONObject();

		try {
			// System.out.println(inresult);
			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功");

				outgmp.put("dzqye", ingmp.getString("dzqye_out"));
				outgmp.put("bcfqje", ingmp.getString("bcfqje_out"));
				outgmp.put("ljjf", ingmp.getString("ljjf_out"));
				outgmp.put("bcjf", ingmp.getString("bcjf_out"));
				outgmp.put("ybjfje", ingmp.getString("ybjfje_out"));
				outgmp.put("ybdzqje", ingmp.getString("ybdzqje_out"));

			} else {
				if (ingmp.has("yxbz_out")) {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("msg") + "(gmp)");
				}
			}
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();

			outresult = "异常：" + e.getMessage() + "(" + inresult + ")";
		}
		// System.out.println(outresult);
		return outresult;
	}

	/* 广百退货信息 */
	public String GmpThInfo() {
		String outresult = "";

		try {
			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "scbh_in", __Request.getParameter("scbh").toString(), "gwbh_in",
					__Request.getParameter("gwbh").toString(), "dh_in", __Request.getParameter("dh").toString(),
					"date_beg_in", __Request.getParameter("date_begin").toString(), "date_end_in",
					__Request.getParameter("date_end").toString() };

			String url = CInitParam.ErpServerUrl + "ShoppePay/spay/queryth.ihtml";

			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功");
				outgmp.put("data", ingmp.getJSONArray("ad2_cur_out"));
			} else {
				if (ingmp.has("yxbz_out")) {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("msg") + "(gmp)");
				}
			}
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		// System.out.println(outresult);
		return outresult;
	}

	/* 订单状态更新 */
	public String GmpOpLogRecord() {
		String outresult = "";
		String cashierid = __Request.getParameter("cashierid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String id = __Request.getParameter("id").toString();

		JSONObject outgmp = new JSONObject();

		try {
			String[] sqlParams = { counterid, cashierid, id, __Request.getParameter("opno").toString() };

			int[] paramType = { 1, 1, 2, 2 };

			int nresult = executeOracleProc("ChangeBillProperty", sqlParams, paramType, null, 4); // 执行存储过程

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (nresult == 0) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功:" + vPrcoReturn[1]);
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "失败:" + _errorInf);
			}
			outgmp.put("storeid", "0001");
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = e.getMessage();
		}

		return outresult;
	}

	/* 支付宝函数 */
	/* 支付宝查询 */
	public String GmpQueryAli(String url) {
		String outresult = "";

		String[] qparams = { __Request.getParameter("billid").toString(),
				__Request.getParameter("cashierid").toString() };
		try {
			boolean isOk = false;
			queryBySqlIDWithParamInner("ALI_PAYQUERY", qparams);

			isOk = next();
			if (!isOk) {
				CURLConnection crequest = new CURLConnection();
				crequest.setDefaultContentEncoding("utf-8");
				String[] vparams = { "operatorId", __Request.getParameter("cashierid").toString(), "outId",
						__Request.getParameter("billid").toString(), "mallCode",
						__Request.getParameter("storeid").toString(), "mode", "1" };
				// http://120.236.153.203:18091/ShoppePay/spay/getInfo.ihtml

				url = "http://120.236.153.203:8088/GBOO/pay/queryPayResl";
				JSONObject ingmp = new JSONObject(crequest.sendPostEx(url, vparams).trim());

				if (ingmp.getString("code").equals("1")) { // 查询成功
					// 记录支付情况

					queryBySqlIDWithParamInner("ALI_PAYQUERY", qparams); // 重新查询
					isOk = true;
					/*
					 * pbillid varchar2, pappid varchar2, pmerid varchar2,
					 * pbanktype varchar2, pmoneytype number, popenid varchar2,
					 * paccountid varchar2, ptradecode varchar2, ppayfee number,
					 * ppaymenttime varchar2, perrmsg varchar2, ppaymentcode
					 * varchar2, ppayatt number, pbilltradeno varchar2
					 */
				} else {
					outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"0\",\"sub_msg\":\""
							+ ingmp.getString("msg") + "\"}";
				}
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

	/* 签到签退报表 */
	public String GmpLoginQuery() {
		String outresult = "";
		String[] vparams = { __Request.getParameter("startdate").toString(),
				__Request.getParameter("enddate").toString(), __Request.getParameter("storeid").toString(),
				__Request.getParameter("counterid").toString(), __Request.getParameter("cashierid").toString() };
		try {
			queryBySqlIDWithParamInner("R_LOGINQUERY", vparams);
			JSONArray rjson = formatResultToJsonArray();
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "ok");
			outgmp.put("data", rjson);
			outresult = outgmp.toString();

		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 通用数据查询接口 */
	public String GmpQuery(String sqlid, String[] vparams) {
		String outresult = "";
		try {
			queryBySqlIDWithParamInner(sqlid, vparams);
			String rjson = formatResultToJson("", false);
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\"," + "\"recordcount\":"
					+ RecordCount + ",\"data\":" + rjson + "}";
			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 通用数据查询接口2 */
	public String GmpQuery(String sqlid, String paramStr) {
		return GmpQuery(sqlid, paramStr.split(","));
	}

	/* 通用数据查询接口2 */
	public String GmpQuery(String sqlid, String sqlid2, String[] vparams) {
		String outresult = "";
		try {
			queryBySqlIDWithParamInner(sqlid2, vparams);
			String tjson = formatResultToJson(); // 汇总数据

			queryBySqlIDWithParamInner(sqlid, vparams);

			String rjson = formatResultToJson("", false);
			outresult = "{\"code\":\"1\",\"msg\":\"sucess\",\"sub_code\":\"1\",\"sub_msg\":\"ok\"," + tjson + ","
					+ "\"recordcount\":" + RecordCount + ",\"data\":" + rjson + "}";
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取订单明细 */
	public String GmpBillInfo(String billkey) {
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

			executeQuery3(
					"select count(*) r from PosBill "
							+ "where counterid='%s' and cashierid='%s' and billstate between -2 and 2 and "
							+ "      billkey<>%s",
					new String[] { mjson.getJSONObject(0).getString("counterid"),
							mjson.getJSONObject(0).getString("cashierid"), billkey });
			next();
			o.put("savebillnum", getString(1));

			o.put("code", "1");
			o.put("msg", "sucess");
			o.put("sub_code", "1");
			o.put("sub_msg", "ok");
			outresult = o.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 订单结账确认 */
	public String GmpBillConfirm(String url) {
		String outresult = "";

		try {
			String storeid = __Request.getParameter("storeid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String billkey = __Request.getParameter("billkey").toString();

			queryBySqlInner("select billstate,paidbycash,vipid,viptype,lskh,lsklx,billid from posbill where billkey=%s",
					billkey);

			next();

			int nxjms = getInt("paidbycash");
			String lskh = getString("lskh");
			String lsklx = getString("lsklx");
			String billid = getString("billid");

			JSONArray mjson = null;
			JSONArray gjson = null;
			JSONArray pjson = null;

			if (getInt("paidbycash") == 0) {
				queryBySqlInner("select * from v_gmpbill where dh='%s'", billid);
				mjson = formatResultToJsonArray(); // 主表

				queryBySqlInner("select * from v_gmpbillpayment where dh='%s'", billid);
				pjson = formatResultToJsonArray(); // 支付表
			}

			queryBySqlInner("select * from v_gmpbillproducts where dh='%s'", billid);
			gjson = formatResultToJsonArray(); // 商品表

			JSONObject outgmp = new JSONObject();
			JSONObject o = new JSONObject();

			o.put("dh_in", billid);
			o.put("cash_mode_in", String.valueOf(nxjms));
			o.put("kh_in", lskh);
			o.put("klx_in", lsklx);

			o.put("ecr0001_cur_in", mjson);
			o.put("ecr0002_cur_in", pjson);
			o.put("ad2_cur_in", gjson);

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostFmt(CInitParam.ErpServerUrl + "ShoppePay/spay/committans.ihtml",
					new String[] { o.toString() }, true);

			JSONObject ingmp = new JSONObject(inresult);
			P(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.has("yxbz_out") && ingmp.has("fhxx_out")) {
				// if(true){
				if (ingmp.getString("yxbz_out").equals("1")) { // 先屏蔽让过
					// if(true){
					String crm_billno = (ingmp.has("crm_billno_out")) ? ingmp.getString("crm_billno_out") : "0";

					// 返券信息
					String fqxx = (ingmp.has("fqxx_out")) ? ingmp.getString("fqxx_out") : "";

					outgmp.put("fqxx", fqxx);

					if (!crm_billno.equals("0")) {

						inresult = crequest.sendPostFmt(CInitParam.ErpServerUrl + "ShoppePay/crm/spcustSendok.ihtml",
								new String[] { "vseqno_in", crm_billno }, false);

						ingmp = new JSONObject(inresult);
						P(inresult);
						if (ingmp.has("yxbz_out")) {
							if (ingmp.getString("yxbz_out").equals("1")) {
								outgmp.put("sub_code", "1");
								outgmp.put("sub_msg", "成功");
								outgmp.put("newbillid", "0");
								outgmp.put("gmpbillseq", Integer.parseInt(crm_billno));
								outgmp.put("pointsgain", ingmp.has("bcjf_out") ? ingmp.getDouble("bcjf_out") : 0.0);
								outgmp.put("points", ingmp.has("ljjf_out") ? ingmp.getDouble("ljjf_out") : 0.0);
								outgmp.put("dzqye", ingmp.has("dzqye_out") ? ingmp.getDouble("dzqye_out") : 0.0);
								outgmp.put("bqfqje", ingmp.has("bqfqje_out") ? ingmp.getDouble("bqfqje_out") : 0.0);

								executeOracleProc(
										"BillConfirm", new String[] { storeid, counterid, cashierid, billkey,
												crm_billno, fqxx, "0", "0", "0", "0" },
										new int[] { 1, 1, 1, 2, 2, 1, 2, 2, 2, 2 }, null, 4); //

								executeOracleProc("BillPointsUp",
										new String[] { billkey, crm_billno, "" + outgmp.getDouble("pointsgain"),
												"" + outgmp.getDouble("points"), "" + outgmp.getDouble("dzqye"),
												"" + outgmp.getDouble("bqfqje") },
										new int[] { 2, 2, 3, 3, 3, 3 }, null, 4); //

							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", inresult);
						}

						/*
						 * executeOracleProc("SysLog", new String[] { counterid,
						 * cashierid, billid, "11", "spcustSendok",
						 * ingmp.getString("fhxx_out")}, new int[] { 1, 1, 1, 2,
						 * 1, 1 }, null, 4);
						 */

					} else {
						outgmp.put("newbillid", "0");
						outgmp.put("gmpbillseq", 0);
						outgmp.put("pointsgain", 0.0);
						outgmp.put("points", 0.0);
						outgmp.put("dzqye", 0.0);
						outgmp.put("bqfqje", 0.0);
						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "成功");

						executeOracleProc("BillConfirm", new String[] { storeid, counterid, cashierid, billkey,
								crm_billno, fqxx, "0", "0", "0", "0" }, new int[] { 1, 1, 1, 2, 2, 1, 2, 2, 2, 2 },
								null, 4); //

					}

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outgmp.put("storeid", storeid);
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 订单取消,未支付的商品撤销时直接删除订单 */
	public String GmpBillUndo() {

		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		String billkey = __Request.getParameter("billkey").toString();
		String billid = "";
		String outresult = "";
		boolean isOk = false;
		try {
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			queryBySqlInner("select billid,paidbycash from posbill where billkey=%s", billkey);
			if (next()) {
				billid = getString("billid");
				isOk = getInt("paidbycash") == 0;
			} else {
				isOk = true;
			}

			if (!isOk) {
				// 现金订单先执行ERP撤单
				JSONObject o = new JSONObject();

				o.put("dh_in", billid);
				o.put("cash_mode_in", "3");
				o.put("kh_in", "");
				o.put("klx_in", "");

				CURLConnection crequest = new CURLConnection();
				crequest.setDefaultContentEncoding("utf-8");

				String inresult = crequest.sendPostFmt(CInitParam.ErpServerUrl + "ShoppePay/spay/committans.ihtml",
						new String[] { o.toString() }, true);

				P(inresult);
				JSONObject ingmp = new JSONObject(inresult);
				if (ingmp.has("yxbz_out")) {
					if (ingmp.getString("yxbz_out").equals("1")) {
						isOk = true;
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", _errorInf);
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", inresult);
				}
			}

			if (isOk) {
				if (executeOracleProc("BillUndo", new String[] { storeid, counterid, cashierid, billkey },
						new int[] { 1, 1, 1, 2 }, null, 4) == 0) { // 执行存储过程
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "订单撤销成功");

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", _errorInf);
				}
			}
			outgmp.put("storeid", storeid);
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 订单挂单 */
	public String GmpBillPutUp() {

		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		String billkey = __Request.getParameter("billkey").toString();

		String outresult = "";

		try {
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (executeOracleProc("BillPutUp", new String[] { storeid, counterid, cashierid, billkey },
					new int[] { 1, 1, 1, 2 }, null, 4) == 0) { // 执行存储过程
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "订单挂单成功");
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", _errorInf);
			}

			outgmp.put("storeid", storeid);
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 订单撤单确认 */
	public String GmpBillCancel(String url) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");

		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		String billkey = __Request.getParameter("billkey").toString();

		String outresult = "";
		String inresult = "{\"fhxx_out\":\"成功！\",\"yxbz_out\":\"1\"}"; // crequest.sendPostEx(url,

		try {
			JSONObject outgmp = new JSONObject();

			queryBySqlInner("select billstate from posbill where billkey=%s", billkey);
			next();
			if (getInt("billstate") != -2) {
				outgmp.put("code", "1");
				outgmp.put("msg", "sucess");
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "订单不在待撤单确认状态");
			} else {

				// 通知广百系统已撤单

				JSONObject ingmp = new JSONObject(inresult);
				outgmp.put("code", "1");
				outgmp.put("msg", "sucess");
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "订单撤单确认成功");

					// 执行撤单操作
					executeOracleProc("BillCancel", new String[] { storeid, counterid, cashierid, billkey },
							new int[] { 1, 1, 1, 2 }, null, 4); // 执行存储过程
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			}
			outgmp.put("storeid", storeid);
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 广百ERP读取现金订单数据 */
	public boolean GmpBillFromErp() {
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		boolean result = true;
		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			CQuery query = new CQuery();
			try {
				query.queryBySqlInner(
						"select billid,billstate,paidbycash from posbill "
								+ "where counterid='%s' and cashierid='%s' and billstate=1 and paidbycash=1",
						new String[] { counterid, cashierid });
				while (result && query.next()) {
					String billid = query.getString("billid");
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
								if (executeOracleProc("TranBillPaidPay", new String[] { billid, gmpbillid },
										new int[] { 1, 1 }, null, 4) != 0) {
									result = false;
									P(_errorInf);
								}
							} else {
								result = false;
							}
						}
					} else {
						result = false;
					}
				}
			} finally {
				query.closeConn();
			}
		} catch (Exception e) {
			result = false;
			_errorInf = e.getMessage();
		} finally {
			closeConn();
		}
		return result;
	}

	/* 订单发货确认 */
	public String GmpBillDeduct(String url) {

		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		String billid = __Request.getParameter("billid").toString();

		String outresult = "";

		try {

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			queryBySqlInner("select billkey,billstate,paidbycash from posbill where billid='%s'", billid);
			next();
			billid = getString("billkey");
			if (getInt("billstate") + getInt("paidbycash") <= 2) {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "订单不在待发货状态");
			} else {
				
				executeOracleProc("ChangeBillProperty", new String[] { counterid, cashierid, billid, "4" },
						new int[] { 1, 1, 2, 2 }, null, 4); // 修改订单为发货状态
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功发货");
				outgmp.put("procresult", _errorInf);
			}
			outgmp.put("storeid", storeid);
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 订单赠券补赠 */
	public String GmpBillCoupon(String url) {
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");

		String storeid = __Request.getParameter("storeid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		String billkey = (__Request.getParameter("billkey") != null) ? __Request.getParameter("billkey").toString()
				: "";
		String billid = __Request.getParameter("billid").toString();

		String outresult = "";
		String inresult = "{\"fhxx_out\":\"成功！\",\"yxbz_out\":\"1\"}";
		// crequest.sendPostEx(url,

		try {

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (billkey.equals(""))
				queryBySqlInner("select billkey,couponsent from posbill where billid='%s'", billid);
			else
				queryBySqlInner("select billkey,couponsent from posbill where billid='%s'", billkey);
			next();
			billkey = getString("billkey");

			if (getInt("couponsent") == 1) {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "订单已获赠券");
			} else {
				// 广百系统补赠请求
				JSONObject ingmp = new JSONObject(inresult);
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					// 数据库处理
					executeOracleProc("ChangeBillProperty",
							new String[] { storeid, counterid, cashierid, billkey, "9" }, new int[] { 1, 1, 1, 1, 2 },
							null, 4); // 执行存储过程
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			}
			outgmp.put("storeid", storeid);
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 生成广百订单提交接口数据 */
	public String GmpBillFmt(String billid) {
		String outresult = "";
		try {

			queryBySqlInner("select paidbycash,lskh,lsklx from posbill where billid='%s'", billid);
			if (!next())
				throw new SQLException("订单不存在(" + billid + ")");

			int nxjms = getInt("paidbycash");
			String lskh = getString("lskh");
			String lsklx = getString("lsklx");

			JSONArray mjson = null;
			JSONArray gjson = null;
			JSONArray pjson = null;

			if (getInt("paidbycash") == 0) {
				queryBySqlInner("select * from v_gmpbill where dh='%s'", billid);
				mjson = formatResultToJsonArray(); // 主表

				queryBySqlInner("select * from v_gmpbillpayment where dh='%s'", billid);
				pjson = formatResultToJsonArray(); // 支付表
			}

			queryBySqlInner("select * from v_gmpbillproducts where dh='%s'", billid);
			gjson = formatResultToJsonArray(); // 商品表

			JSONObject outgmp = new JSONObject();
			outgmp.put("dh_in", billid);
			outgmp.put("cash_mode_in", String.valueOf(nxjms));
			outgmp.put("kh_in", lskh);
			outgmp.put("klx_in", lsklx);

			outgmp.put("ecr0001_cur_in", mjson);
			outgmp.put("ecr0002_cur_in", pjson);
			outgmp.put("ad2_cur_in", gjson);

			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 发送订单到广百ERP，现金模式 */
	public String GmpPaidByCashSent() {
		String outresult = "";
		String billid = "";
		String billkey = __Request.getParameter("billkey").toString();
		String cashierid = __Request.getParameter("cashierid").toString();
		String counterid = __Request.getParameter("counterid").toString();
		String promid = __Request.getParameter("promid").toString();
		String note = (__Request.getParameter("note") != null) ? __Request.getParameter("note").toString() : "";
		/*
		 * String scbh = __Request.getParameter("scbh").toString(); String gwbh
		 * = __Request.getParameter("gwbh").toString(); String promid =
		 * __Request.getParameter("promid").toString();
		 */
		try {

			queryBySqlInner("select billid,lskh,lsklx from posbill where billkey=%s", billkey);
			next();
			billid = getString("billid");
			String lskh = getString("lskh");
			String lsklx = getString("lsklx");

			// JSONArray mjson = null;
			JSONArray gjson = null;
			// JSONArray pjson = null;

			queryBySqlInner("select * from v_gmpbillproducts where dh='%s'", billid);
			gjson = formatResultToJsonArray(); // 商品表

			JSONObject ingmp = new JSONObject();
			ingmp.put("dh_in", billid);
			ingmp.put("cash_mode_in", "1");
			ingmp.put("kh_in", lskh);
			ingmp.put("klx_in", lsklx);

			// ingmp.put("ecr0001_cur_in", mjson);
			// ingmp.put("ecr0002_cur_in", pjson);
			ingmp.put("ad2_cur_in", gjson);

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostFmt(CInitParam.ErpServerUrl + "ShoppePay/spay/committans.ihtml",
					new String[] { ingmp.toString() }, true);

			ingmp = new JSONObject(inresult);
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					executeOracleProc("GmpSetPaidbycash", new String[] { counterid, cashierid, billkey, promid, note },
							new int[] { 1, 1, 2, 1, 1 }, null, 4); // 执行存储过程

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", outresult);
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

	public String getGbOrderInfo(String orderNo, String casherNo, int saleFlag) {
		String outresult = "";
		try {
			String token = CUtil.MD5("74b87337454200d4d33f80c4663dc5e5");
			String tenantId = "40282ab366e2a43a0166e2a6c32a0001";
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			JSONObject ingmp = new JSONObject();
			ingmp.put("orderNo", orderNo);
			ingmp.put("casherNo", casherNo);
			ingmp.put("saleFlag", saleFlag);

			String inresult = crequest.sendPostEx(CInitParam.OrderCenterUrl + "order/getCasherOrder.ihtml",
					new String[] { ingmp.toString() }, new String[] { "token", token },
					true);

			ingmp = new JSONObject(inresult);

			outresult = ingmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

}
