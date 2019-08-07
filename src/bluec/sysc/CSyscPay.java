package bluec.sysc;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bluec.base.CJson;
import bluec.base.CURLConnection;
import bluec.base.CUtil;
import bluec.base.CFile;
import bluec.base.CInitParam;

/*支付类*/
public class CSyscPay extends CJson {
	public CSyscPay(HttpServletRequest request) {
		super(request);
	}

	// 微信支付函数
	private int _wxpaystate = -1; // 0-成功,-1-失败,1-账单不存在

	/**
	 * @param url
	 *            ///
	 * @param billid
	 * @param log
	 * @return
	 */
	private boolean doSavePayLog(String url, String billid, String log, boolean bCall) {
		boolean result = true;

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String data = (bCall) ? url + log : log;
			String[] vparams = { "dh_in", billid, "log_in", CUtil.escape(data) };

			P(CUtil.escape(data));
			String inresult = crequest.sendPostEx(url, vparams).trim();
			JSONObject ingmp = new JSONObject(inresult);

			P(inresult);
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					result = true;
				} else {
					_errorInf = ingmp.getString("fhxx_out");
				}
			} else {
				_errorInf = inresult;
			}
		} catch (Exception e) {
			_errorInf = e.getMessage();
		} finally {
			closeConn();
		}

		return result;
	}

	// 微信支付日志
	private boolean saveWxPayLog(String billid, String log, boolean bCall) {
		return doSavePayLog(CInitParam.ErpServerUrl + "ShoppePay/wx/wxLog.ihtml", billid, log, bCall);
	}

	// 支付宝支付日志
	private boolean saveAliPayLog(String billid, String log, boolean bCall) {
		return doSavePayLog(CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbLog.ihtml", billid, log, bCall);
	}

	private void getOtBillInfo(String billkey, JSONObject outgmp) {
		try {
			queryBySqlInner("select sysy,zqzkje from posbill where billkey=%s", billkey);
			if (next()) {
				outgmp.put("sysy", getFloat("sysy", 0.0));
				outgmp.put("zqzkje", getFloat("zqzkje", 0.0));
			} else {
				outgmp.put("sysy", 0.0);
				outgmp.put("zqzkje", 0.0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConn();
		}
	}

	// 记录支付结果
	private void WxPayRecord(JSONObject ingmp, JSONObject outgmp) {
		try {
			outgmp.put("sub_code", "1");
			if (ingmp.has("trade_state"))
				outgmp.put("trade_state", ingmp.getString("trade_state"));
			else
				outgmp.put("trade_state", "SUCCESS");

			String payfee = String.valueOf(Double.parseDouble(ingmp.getString("total_fee")) / 100.0);
			outgmp.put("paymentcode", "0052");
			outgmp.put("paymentname", "微信");
			outgmp.put("amount", payfee);
			outgmp.put("avalues", 0);
			outgmp.put("tradecode", ingmp.getString("transaction_id"));
			outgmp.put("paymentdescription", ingmp.getString("openid"));
			outgmp.put("paymentmethod", ingmp.getString("fee_type"));
			outgmp.put("transactiontime", "");
			outgmp.put("paymenttime", formatWxFmt(ingmp.getString("time_end")));
			outgmp.put("paymentstatus", 2);
			outgmp.put("orderno", __Request.getParameter("gmporderno").toString());
			outgmp.put("sqye", 0);
			outgmp.put("quantity", 0);
			outgmp.put("price", 0);

			if (__Request.getParameter("billkey") != null) {
				String[] sqlParams = { __Request.getParameter("storeid").toString(),
						__Request.getParameter("counterid").toString(), __Request.getParameter("cashierid").toString(),
						__Request.getParameter("billkey").toString(), ingmp.getString("appid"),
						ingmp.getString("mch_id"), ingmp.getString("bank_type"), ingmp.getString("fee_type"),
						ingmp.getString("openid"), ingmp.getString("transaction_id"), ingmp.getString("transaction_id"),
						payfee, ingmp.getString("time_end"), "", "0052", "微信", ingmp.getString("out_trade_no") };

				int[] paramType = { 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1 };
				if (executeOracleProc("GmpPayAliWx", sqlParams, paramType, new int[] { 2, 2, 2, 2 }, 4) != 0)
					outgmp.put("procresult", _errorInf);
				P("gmppayrecord=" + _errorInf);
				outgmp.put("billstate", vPrcoReturn[2]);
				outgmp.put("billkey", Integer.parseInt(__Request.getParameter("billkey").toString()));
				outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
				outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
				outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
				getOtBillInfo(__Request.getParameter("billkey").toString(), outgmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConn();
		}
	}

	/* 微信支付查询 */
	public String WxPayQuery(int qatt) {

		JSONObject outgmp = new JSONObject();
		String url = CInitParam.WxServerUrl + "wxscanpay/proxy/orderquery";
		boolean isOk = false;
		String outresult = "";
		_wxpaystate = -1;

		try {
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String[] vparams = { "out_trade_no", __Request.getParameter("gmporderno").toString(), "department_no",
					__Request.getParameter("department_no").toString() };

			if (qatt == 1
					&& !saveWxPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			// P("vparams="+vparams[1]+","+vparams[3]);
			String inresult = crequest.sendPostEx(url, vparams).trim();

			if (qatt == 1) {
				saveWxPayLog(__Request.getParameter("orderno").toString(), inresult, false);

				url = CInitParam.ErpServerUrl + "ShoppePay/wx/wxJudge.ihtml";

				String jnresult = crequest.sendPostEx(url,
						new String[] { "dh_in", __Request.getParameter("orderno").toString(), "skyh_in",
								__Request.getParameter("cashierid").toString(), "zdh_in",
								__Request.getParameter("counterid").toString(), "type_in", "query", "je_in", "0",
								"json_in", inresult })
						.trim();
				P("jnresult=");
				P(jnresult);
				JSONObject jngmp = new JSONObject(jnresult);
				if (jngmp.has("yxbz_out")) {
					if (jngmp.getString("yxbz_out").equals("1")) {
						isOk = true;
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", jngmp.getString("fhxx_out"));
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", jnresult);
				}
			} else {
				isOk = true;
			}

			if (isOk) {
				JSONObject ingmp = new JSONObject(inresult);

				if (ingmp.has("success")) {
					if (ingmp.getBoolean("success")) {
						ingmp = ingmp.getJSONObject("detail");
						if (ingmp.getString("return_code").equals("SUCCESS")) {
							if (ingmp.getString("result_code").equals("SUCCESS")) {
								if (!ingmp.has("trade_state") || ingmp.getString("trade_state").equals("SUCCESS")) {
									WxPayRecord(ingmp, outgmp);
									_wxpaystate = 0;
								} else {
									outgmp.put("sub_code", "0");
									outgmp.put("sub_msg", "支付被撤销。");
								}
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("err_code_des"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("result_msg"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", ingmp.getString("return_msg"));
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", inresult);
				}
			}
			outgmp.put("wxcall", (_wxpaystate == 0) ? 2 : 1);
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
			// P(outresult);
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	/* 微信支付 */
	private String doWxPay() {
		String url = CInitParam.WxServerUrl + "wxscanpay/proxy/micropay";

		String outresult = "";
		_wxpaystate = -1;
		try {
			JSONObject outgmp = new JSONObject();

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String totalfee = __Request.getParameter("totalfee").toString();
			String payfee = "" + Math.round(Double.parseDouble(totalfee) * 100);

			String[] vparams = { "device_info", __Request.getParameter("counterid").toString(), "out_trade_no",
					__Request.getParameter("gmporderno").toString(), "ttach",
					__Request.getParameter("ttach").toString(), "body", __Request.getParameter("subject").toString(),
					"department_no", __Request.getParameter("department_no").toString(), "fee_type",
					__Request.getParameter("fee_type").toString(), "auth_code",
					__Request.getParameter("barcode").toString(), "total_fee", payfee };

			if (!saveWxPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			executeUpdate("update posbill set wxcall=1,billstate=1 where billkey=%s",
					new String[] { __Request.getParameter("billkey").toString() });

			String inresult = crequest.sendPostEx(url, vparams).trim();

			saveWxPayLog(__Request.getParameter("orderno").toString(), inresult, false);

			url = CInitParam.ErpServerUrl + "ShoppePay/wx/wxJudge.ihtml";

			String jnresult = crequest.sendPostEx(url,
					new String[] { "dh_in", __Request.getParameter("orderno").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString(), "zdh_in",
							__Request.getParameter("counterid").toString(), "type_in", "pay", "je_in", totalfee,
							"json_in", inresult })
					.trim();
			P("jnresult=");
			P(jnresult);

			JSONObject jngmp = new JSONObject(jnresult);

			if (jngmp.has("yxbz_out")) {
				if (jngmp.getString("yxbz_out").equals("1")) {
					JSONObject ingmp = new JSONObject(inresult);

					if (ingmp.has("success")) {
						if (ingmp.getBoolean("success")) {
							ingmp = ingmp.getJSONObject("detail");
							if (ingmp.getString("return_code").equals("SUCCESS")) {
								if (ingmp.getString("result_code").equals("SUCCESS")) {
									if (!ingmp.has("trade_state") || ingmp.getString("trade_state").equals("SUCCESS")) {
										WxPayRecord(ingmp, outgmp);
										_wxpaystate = 0;
									} else {
										outgmp.put("sub_code", "0");
										outgmp.put("sub_msg", "支付被撤销。");
									}
								} else {
									outgmp.put("sub_code", "0");
									outgmp.put("sub_msg", ingmp.getString("err_code_des"));
								}
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("return_msg"));
							}
						} else {
							ingmp = ingmp.getJSONObject("detail");
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("err_code_des"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", inresult);
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", jngmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", jnresult);
			}

			outgmp.put("wxcall", (_wxpaystate == 0) ? 2 : 1);
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String WxPay() {
		String outresult = "";
		// boolean isNotTest = __Request.getParameter("billkey") != null;
		try {
			/*
			 * if (isNotTest) { // 不是测试状态 String[] sqlParams = {
			 * __Request.getParameter("storeid").toString(),
			 * __Request.getParameter("counterid").toString(),
			 * __Request.getParameter("cashierid").toString(),
			 * __Request.getParameter("billkey").toString(),
			 * __Request.getParameter("seq").toString(), "0052",
			 * __Request.getParameter("totalfee").toString() };
			 * 
			 * int[] paramType = { 1, 1, 1, 2, 2, 1, 2 };
			 * executeOracleProc("GmpReqRecord", sqlParams, paramType, null, 4);
			 * // }
			 */
			// String payfee =
			// ""+((int)(Double.parseDouble(__Request.getParameter("totalfee").toString())*100));
			// P("payfee="+__Request.getParameter("totalfee").toString());
			int qi = 0; // 支付查询计数
			outresult = WxPayQuery(0);

			if (_wxpaystate != 0) {
				String t = doWxPay();
				outresult = t;
				while (qi < 1 && _wxpaystate != 0) {
					qi++;
					outresult = WxPayQuery(1);
				}
				if (_wxpaystate != 0)
					outresult = t;
			}

		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/* 微信退货查询 */

	// 记录退货结果
	private void WxRefoudRecord(JSONObject ingmp, JSONObject outgmp) {
		try {
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "ok");
			outgmp.put("transaction_id", ingmp.getString("transaction_id"));
			outgmp.put("out_trade_no", ingmp.getString("out_trade_no"));
			outgmp.put("refund_fee", String.valueOf(Double.parseDouble(ingmp.getString("refund_fee")) / 100));

			executeOracleProc("GmpPayReturn", new String[] { __Request.getParameter("billkey").toString(), "0052", "" },
					new int[] { 2, 1, 1 }, new int[] { 2 }, 4);
			outgmp.put("procresult", _errorInf);
			outgmp.put("billstate", vPrcoReturn[2]);

			outgmp.put("billkey", __Request.getParameter("billkey").toString());
			outgmp.put("paymentcode", "0052");
			outgmp.put("time", CUtil.getOrigTime());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String WxRefundQuery(int qatt) {

		String url = CInitParam.WxServerUrl + "wxscanpay/proxy/refundquery";
		String outresult = "";
		_wxpaystate = -1;
		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "out_trade_no", __Request.getParameter("gmporderno").toString(), "department_no",
					__Request.getParameter("department_no").toString() };

			if (qatt == 1
					&& !saveWxPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			String inresult = crequest.sendPostEx(url, vparams).trim();

			if (qatt == 1) {
				saveWxPayLog(__Request.getParameter("orderno").toString(), inresult, false);
			}

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.has("success"))
				if (ingmp.getBoolean("success")) {
					ingmp = ingmp.getJSONObject("detail");
					if (ingmp.getString("return_code").equals("SUCCESS")) {
						if (ingmp.getString("result_code").equals("SUCCESS")) {
							WxRefoudRecord(ingmp, outgmp);
							_wxpaystate = 0;
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("err_code_des"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", ingmp.getString("return_msg"));
					}
				} else {
					ingmp = ingmp.getJSONObject("detail");
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("err_code_des"));
				}
			else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	/* 微信退货 */
	public String doWxRefund() {

		String url = CInitParam.WxServerUrl + "wxscanpay/proxy/refund";

		String outresult = "";
		try {
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String totalfee = String.valueOf(Integer.parseInt(__Request.getParameter("refundfee").toString()) / 100.0);

			String[] vparams = { "device_info", __Request.getParameter("counterid").toString(), "out_trade_no",
					__Request.getParameter("gmporderno").toString(), "transaction_id",
					__Request.getParameter("oorderno").toString(), "out_refund_no",
					__Request.getParameter("norderno").toString(), "department_no",
					__Request.getParameter("department_no").toString(), "fee_type",
					__Request.getParameter("fee_type").toString(), "refund_fee",
					__Request.getParameter("refundfee").toString(), "total_fee",
					__Request.getParameter("totalfee").toString(), "op_user_id",
					__Request.getParameter("cashierid").toString() };

			if (!saveWxPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			String inresult = crequest.sendPostEx(url, vparams).trim();

			saveWxPayLog(__Request.getParameter("orderno").toString(), inresult, false);

			url = CInitParam.ErpServerUrl + "ShoppePay/wx/wxJudge.ihtml";

			String jnresult = crequest.sendPostEx(url,
					new String[] { "dh_in", __Request.getParameter("orderno").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString(), "zdh_in",
							__Request.getParameter("counterid").toString(), "type_in", "cancel", "je_in", totalfee,
							"json_in", inresult })
					.trim();
			P("jnresult=");
			P(jnresult);

			JSONObject jngmp = new JSONObject(jnresult);

			if (jngmp.has("yxbz_out")) {
				if (jngmp.getString("yxbz_out").equals("1")) {
					JSONObject ingmp = new JSONObject(inresult);

					if (ingmp.has("success")) {
						if (ingmp.getBoolean("success")) {
							ingmp = ingmp.getJSONObject("detail");
							if (ingmp.getString("return_code").equals("SUCCESS")) {
								if (ingmp.getString("result_code").equals("SUCCESS")) {
									WxRefoudRecord(ingmp, outgmp);
									_wxpaystate = 0;
								} else {
									outgmp.put("sub_code", "0");
									outgmp.put("sub_msg", ingmp.getString("err_code_des"));
								}
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("return_msg"));
							}
						} else {
							ingmp = ingmp.getJSONObject("detail");
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("err_code_des"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", inresult);
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", jngmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", jnresult);
			}

			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	public String WxRefund() {
		String outresult = "";

		try {
			int qi = 0; // 退货查询计数
			outresult = WxRefundQuery(0);
			if (_wxpaystate != 0) {
				outresult = doWxRefund();
				/*
				 * while (qi < 1 && _wxpaystate != 0) { qi++; outresult =
				 * WxRefundQuery(0); }
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 支付宝函数
	// 记录支付结果
	private void AliPayRecord(JSONObject ingmp, JSONObject outgmp) {
		try {
			outgmp.put("sub_code", "1");
			outgmp.put("sub_mag", "ok");
			outgmp.put("paymentcode", "0042");
			outgmp.put("paymentname", "支付宝");
			outgmp.put("amount", Double.parseDouble(ingmp.getString("C7")) / 100.0);
			outgmp.put("avalues", 0);
			outgmp.put("tradecode", ingmp.getString("C6"));
			outgmp.put("paymentdescription", "CNY");
			outgmp.put("paymentmethod", (ingmp.has("C9")) ? ingmp.getString("C9") : "");
			outgmp.put("transactiontime", "");
			outgmp.put("paymenttime", ingmp.getString("C12"));
			outgmp.put("paymentstatus", 2);
			outgmp.put("orderno", ingmp.getString("C5"));
			outgmp.put("sqye", 0);
			outgmp.put("quantity", 0);
			outgmp.put("price", 0);

			P(outgmp.toString());
			if (__Request.getParameter("billkey") != null) {
				String[] sqlParams = { __Request.getParameter("storeid").toString(),
						__Request.getParameter("counterid").toString(), __Request.getParameter("cashierid").toString(),
						__Request.getParameter("billkey").toString(), "", // appid
						"", // merid
						"", // banktype
						"CNY", // feetype
						"", // openid
						ingmp.getString("C9"), ingmp.getString("C6"),
						String.valueOf(Double.parseDouble(ingmp.getString("C7")) / 100.0), ingmp.getString("C12"), "",
						"0042", "支付宝", ingmp.getString("C5") };

				int[] paramType = { 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1 };

				if (executeOracleProc("GmpPayAliWx", sqlParams, paramType, new int[] { 2, 2, 2, 2 }, 4) != 0)
					outgmp.put("procresult", _errorInf);

				outgmp.put("billstate", vPrcoReturn[2]);
				outgmp.put("billkey", Integer.parseInt(__Request.getParameter("billkey").toString()));
				outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
				outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
				outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
				getOtBillInfo(__Request.getParameter("billkey").toString(), outgmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 支付宝接口检查 */
	public String AliPayCheck() {
		return "";
	}

	/* 支付宝支付查询 */
	public String AliPayQuery(int qatt) {

		String url = CInitParam.AliServerUrl + "Miya/proxy/orderquery.ihtml";

		// "http://120.236.153.203:8088/wxscanpay/proxy/orderquery";
		_wxpaystate = -1;
		String outresult = "";
		boolean isOk = false;
		try {

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			String totalfee = __Request.getParameter("totalfee").toString();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "A1", "A", "A2", CInitParam.AlipayA2, "A3",
					__Request.getParameter("department_no").toString() + "C", "A4",
					__Request.getParameter("counterid").toString(), "A5",
					__Request.getParameter("cashierid").toString(), "A6", "B", "A7", "1.5", "B1",
					__Request.getParameter("gmporderno").toString() };
			if (qatt == 1
					&& !saveAliPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			String inresult = crequest.sendPostEx(url, vparams).trim();
			if (qatt == 1) {
				saveAliPayLog(__Request.getParameter("orderno").toString(), inresult, false);
				url = CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbJudge.ihtml";

				String jnresult = crequest.sendPostEx(url,
						new String[] { "dh_in", __Request.getParameter("orderno").toString(), "skyh_in",
								__Request.getParameter("cashierid").toString(), "zdh_in",
								__Request.getParameter("counterid").toString(), "type_in", "pay", "je_in", totalfee,
								"json_in", inresult })
						.trim();
				P("jnresult=");
				P(jnresult);
				JSONObject jngmp = new JSONObject(jnresult);

				if (jngmp.has("yxbz_out")) {
					if (jngmp.getString("yxbz_out").equals("1")) {
						isOk = true;
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", jngmp.getString("fhxx_out"));
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", jnresult);
				}
			} else {
				isOk = false;
			}

			if (isOk) {
				// P(inresult);
				JSONObject ingmp = new JSONObject(inresult);

				if (ingmp.has("C1")) {
					if (ingmp.getString("C1").equals("SUCCESS")) {
						if (ingmp.getString("C2").equals("PAYSUCCESS")) {
							AliPayRecord(ingmp, outgmp);
							_wxpaystate = 0;
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("C4"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", ingmp.getString("C4"));
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", inresult);
				}
			}
			outgmp.put("alicall", (_wxpaystate == 0) ? 2 : 1);
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());

			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	/* 支付宝支付 */
	public String doAliPay() {

		String url = CInitParam.AliServerUrl + "Miya/proxy/createPay.ihtml";

		String outresult = "";
		_wxpaystate = -1;
		try {
			JSONObject outgmp = new JSONObject();

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			String totalfee = __Request.getParameter("totalfee").toString();
			String payfee = "" + (Math.round(Double.parseDouble(totalfee) * 100));
			P("payfee=" + payfee);

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String[] vparams = { "A1", "A", "A2", CInitParam.AlipayA2, "A3",
					__Request.getParameter("department_no").toString() + "C", "A4",
					__Request.getParameter("counterid").toString(), "A5",
					__Request.getParameter("cashierid").toString(), "A6", "A", "A7", "1.5", "B1",
					__Request.getParameter("gmporderno").toString(), "B2", __Request.getParameter("barcode").toString(),
					"B4", payfee };

			if (!saveAliPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			executeUpdate("update posbill set alicall=1,billstate=1 where billkey=%s",
					new String[] { __Request.getParameter("billkey").toString() });

			String inresult = crequest.sendPostEx(url, vparams).trim();

			saveAliPayLog(__Request.getParameter("orderno").toString(), inresult, false);

			url = CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbJudge.ihtml";

			String jnresult = crequest.sendPostEx(url,
					new String[] { "dh_in", __Request.getParameter("orderno").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString(), "zdh_in",
							__Request.getParameter("counterid").toString(), "type_in", "pay", "je_in", totalfee,
							"json_in", inresult })
					.trim();
			P("jnresult=");
			P(jnresult);
			JSONObject jngmp = new JSONObject(jnresult);

			if (jngmp.has("yxbz_out")) {
				if (jngmp.getString("yxbz_out").equals("1")) {

					JSONObject ingmp = new JSONObject(inresult);

					if (ingmp.has("C1")) {
						if (ingmp.getString("C1").equals("SUCCESS")) {
							if (ingmp.getString("C2").equals("PAYSUCCESS")) {
								AliPayRecord(ingmp, outgmp);
								_wxpaystate = 0;
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("C4"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("C4"));
						}

					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", inresult);
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", jngmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", jnresult);
			}

			outgmp.put("alicall", (_wxpaystate == 0) ? 2 : 1);
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	public String AliPay() {
		String outresult = "";
		_wxpaystate = -1;
		// boolean isNotTest = __Request.getParameter("billkey") != null;
		try {
			/*
			 * if (isNotTest) { // 不是测试状态 String[] sqlParams = {
			 * __Request.getParameter("storeid").toString(),
			 * __Request.getParameter("counterid").toString(),
			 * __Request.getParameter("cashierid").toString(),
			 * __Request.getParameter("billkey").toString(),
			 * __Request.getParameter("seq").toString(), "0042",
			 * __Request.getParameter("totalfee").toString() };
			 * 
			 * int[] paramType = { 1, 1, 1, 2, 2, 1, 2 };
			 * executeOracleProc("GmpReqRecord", sqlParams, paramType, null, 4);
			 * // }
			 */
			int qi = 0; // 支付查询计数
			// outresult = doWxPay();
			outresult = AliPayQuery(0);
			if (_wxpaystate != 0) {
				String t = doAliPay();
				outresult = t;
				while (qi < 1 && _wxpaystate != 0) {
					qi++;
					outresult = AliPayQuery(1);
				}
				if (_wxpaystate != 0)
					outresult = t;
			}

			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/* 支付宝退货 */
	public String AliReFund() {

		String url = CInitParam.AliServerUrl + "Miya/proxy/reverse.ihtml";
		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {
			String totalfee = __Request.getParameter("totalfee");
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String[] vparams = { "A1", "A", "A2", CInitParam.AlipayA2, "A3",
					__Request.getParameter("department_no").toString() + "C", "A4",
					__Request.getParameter("counterid").toString(), "A5",
					__Request.getParameter("cashierid").toString(), "A6", "E", "A7", "1.5", "B1",
					__Request.getParameter("gmporderno").toString() };

			if (!saveAliPayLog(__Request.getParameter("orderno").toString(), CUtil.join(vparams, ""), true)) {
				return errorMsgToJson(_errorInf);
			}

			String inresult = crequest.sendPostEx(url, vparams).trim();
			saveAliPayLog(__Request.getParameter("orderno").toString(), inresult, false);

			url = CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbJudge.ihtml";

			String jnresult = crequest.sendPostEx(url,
					new String[] { "dh_in", __Request.getParameter("orderno").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString(), "zdh_in",
							__Request.getParameter("counterid").toString(), "type_in", "cancel", "je_in", totalfee,
							"json_in", inresult })
					.trim();

			P("jnresult=");
			P(jnresult);

			JSONObject jngmp = new JSONObject(jnresult);
			if (jngmp.has("yxbz_out")) {
				if (jngmp.getString("yxbz_out").equals("1")) {
					JSONObject ingmp = new JSONObject(inresult);

					if (ingmp.has("C1")) {
						if (ingmp.getString("C1").equals("SUCCESS")) {
							if (ingmp.getString("C2").equals("CANCELSUCCESS")) {
								executeOracleProc("GmpPayReturn",
										new String[] { __Request.getParameter("billkey").toString(), "0042", "" },
										new int[] { 2, 1, 1 }, new int[] { 2 }, 4);

								outgmp.put("sub_code", "1");
								outgmp.put("sub_mag", "ok");

								outgmp.put("transaction_id", ingmp.getString("C5"));
								outgmp.put("refund_fee", Double.parseDouble(ingmp.getString("C7")) / 100);
								outgmp.put("procresult", _errorInf);
								outgmp.put("billstate", vPrcoReturn[2]);
								outgmp.put("paymentcode", "0042");
								outgmp.put("time", CUtil.getOrigTime());

								_wxpaystate = 0;
							} else {
								outgmp.put("sub_code", "0");
								outgmp.put("sub_msg", ingmp.getString("C4"));
							}
						} else {
							outgmp.put("sub_code", "0");
							outgmp.put("sub_msg", ingmp.getString("C4"));
						}
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", inresult);
					}
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", jngmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", jnresult);
			}

			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	/* 电子券函数 */
	// 查询一个会员可用的指定类电子券
	public String ECouponQuery() {
		String url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/getDZQInfo.ihtml";
		String outresult = "";

		try {
			String storeid = __Request.getParameter("storeid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String sNow = CUtil.getOrigTime();
			String cTypes = __Request.getParameter("coupontype").toString();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String[] vparams = { "promo_id_in", __Request.getParameter("promo_id").toString(), "kh_in",
					__Request.getParameter("kh").toString() };

			String inresult = crequest.sendPostEx(url, vparams).trim();
			P(inresult);

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");
					JSONArray iArray = ingmp.getJSONArray("dzqinfo_out");
					JSONArray oArray = new JSONArray();

					String cpList = "";

					for (int i = 0; i < iArray.length(); i++) {
						JSONObject io = iArray.getJSONObject(i);
						String ct = io.getString("ct_code");

						if ((cTypes.equals("") || cTypes.indexOf(ct) > -1)
								&& sNow.compareTo(io.getString("use_beg_time")) >= 0
								&& sNow.compareTo(io.getString("use_end_time")) <= 0) {
							JSONObject oo = null;
							int x = cpList.indexOf(ct);
							if (x < 0) {
								oo = new JSONObject();

								oo.put("ct_code", ct);
								oo.put("ct_name", io.getString("ct_name"));
								oo.put("ct_payvalue", io.getString("ct_payvalue"));
								oo.put("ct_amt", io.getString("ct_amt"));
								oo.put("ct_qty", io.getString("ct_qty"));
								oo.put("ct_no", io.getString("ct_code") + CUtil.getTimeS());
								oArray.put(oo);

								cpList += ct;
							} else {
								oo = oArray.getJSONObject(x);
								int sl = Integer.parseInt(io.getString("ct_qty"))
										+ Integer.parseInt(oo.getString("ct_qty"));
								oo.put("ct_qty", "" + sl);
								double je = Double.parseDouble(io.getString("ct_amt"))
										+ Double.parseDouble(oo.getString("ct_amt"));
								oo.put("ct_amt", "" + je);
							}
						}
					}

					outgmp.put("dzqinfo", oArray);
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
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	/* 电子券消费 */
	public String ECouponPay() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQuse.ihtml";
		String outresult = "";

		try {
			String storeid = __Request.getParameter("storeid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String billkey = __Request.getParameter("billkey").toString();
			String sl = __Request.getParameter("sl").toString();
			String dj = __Request.getParameter("dj").toString();
			String je = __Request.getParameter("je").toString();
			String ct_code = __Request.getParameter("ct_code").toString();
			String ct_name = __Request.getParameter("ct_name").toString();
			String vipid = __Request.getParameter("kh").toString();
			String viptype = __Request.getParameter("klx").toString();
			String payfee = __Request.getParameter("payfee").toString();
			String ct_no = __Request.getParameter("ct_no").toString();

			queryBySqlInner(
					"select billid,promo_id,promo_name,promo_id_vip,promo_name_vip " + "from posbill where billkey=%s",
					billkey);
			next();

			String promo_id = getString("promo_id");
			String promo_name = getString("promo_name");
			String billid = getString("billid");

			if (__Request.getParameter("isvip") != null) {
				promo_id = getString("promo_id_vip");
				promo_name = getString("promo_name_vip");
			}

			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String inresult = crequest.sendPostEx(url,
					new String[] { "dh_in", billid, "kh_in", vipid, "klx_in", viptype, "promo_id_in", promo_id,
							"promo_name_in", promo_name, "zdh_in", counterid, "skyh_in", cashierid, "ct_code_in",
							ct_code, "je_in", je })
					.trim();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			P(inresult);

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					outgmp.put("scmc", ingmp.getString("scmc_out"));
					String sv_id = "" + ingmp.getInt("v_sv_id_out");
					if (sv_id.equals(""))
						sv_id = "0";
					P("sv_id=" + sv_id);

					if (executeOracleProc("GmpPayECoupon",
							new String[] { storeid, counterid, cashierid, billkey, vipid, ct_code + ct_name, sl, dj, je,
									payfee, ingmp.getString("scmc_out"), ct_no, sv_id },
							new int[] { 1, 1, 1, 2, 1, 1, 2, 3, 3, 3, 1, 1, 2 }, new int[] { 2, 2, 2, 2 }, 4) != 0) // 记录
						outgmp.put("procerror", _errorInf);

					outgmp.put("billstate", vPrcoReturn[2]);
					outgmp.put("billkey", Integer.parseInt(billkey));
					outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
					outgmp.put("paymentcode", "0005");
					outgmp.put("paymentname", "电子赠券");
					outgmp.put("amount", Double.parseDouble(payfee));
					outgmp.put("avalues", je);
					outgmp.put("tradecode", vipid);
					outgmp.put("paymentdescription", ingmp.getString("scmc_out"));
					outgmp.put("paymentmethod", ct_code + ct_name);
					outgmp.put("transactiontime", "");
					outgmp.put("paymenttime", CUtil.getOrigTime());
					outgmp.put("paymentstatus", 2);
					outgmp.put("orderno", ct_no);
					outgmp.put("sqye", 0);
					outgmp.put("quantity", Double.parseDouble(sl));
					outgmp.put("price", Double.parseDouble(dj));
					outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
					outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
					getOtBillInfo(billkey, outgmp);
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}

			outgmp.put("storeid", "0001");
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

	/* 电子券撤单 */
	public String ECouponRefoud() {
		String url = CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQCancel.ihtml";
		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {
			String storeid = __Request.getParameter("storeid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String billkey = __Request.getParameter("billkey").toString();
			String billid = __Request.getParameter("billid").toString();
			String sv_id = "0";

			queryBySqlInner("select sv_id from posbill where billkey=%s", billkey);
			next();
			sv_id = getString("sv_id");

			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			String inresult = crequest
					.sendPostEx(url, new String[] { "dh_in", billid, "skyh_in", cashierid, "vsv_id_in", sv_id }).trim();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					executeOracleProc("GmpPayReturn", new String[] { billkey, "0005", "" }, new int[] { 1, 1, 1 },
							new int[] { 2 }, 4); // 记录
					outgmp.put("procerror", _errorInf);

					outgmp.put("billstate", vPrcoReturn[2]);
					outgmp.put("billkey", Integer.parseInt(billkey));
					outgmp.put("paymentcode", "0005");

					outgmp.put("dyxx", ingmp.getJSONArray("dyxx_out"));

					P(inresult);
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

	/* 消费卡函数 */
	// 查询消费卡
	public String XfkQuery() {
		String url = CInitParam.ErpServerUrl + "ShoppePay/ExpCard/getXFKInfo.ihtml";
		String outresult = "";

		try {
			String storeid = __Request.getParameter("storeid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String sNow = CUtil.getOrigTime();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String[] vparams = { "zdh_in", counterid, "kh_in", __Request.getParameter("kh").toString() };

			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					outgmp.put("kh", ingmp.getString("kh_out"));
					outgmp.put("kye", ingmp.getString("kye_out"));
					outgmp.put("kzt", ingmp.getString("kzt_out"));
					outgmp.put("qyrq", ingmp.getString("qyrq_out"));
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
			outgmp.put("time", sNow);
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 消费卡支付
	public String XfkPay() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/ExpCard/XFKCommit.ihtml";
		String outresult = "";

		try {

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			JSONObject io = dataFromRequestToJson();

			String inresult = crequest.sendPostEx(url,
					new String[] { "kh_in", io.getString("kh"), "zdh_in", io.getString("counterid"), "je_in",
							io.getString("payfee"), "dh_in", io.getString("billid"), "skyh_in",
							io.getString("cashierid") })
					.trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					if (io.has("billkey")) {
						P("storeid=" + io.getString("storeid"));

						executeOracleProc("GmpPayOther",
								new String[] { ingmp.getString("jsh_out"), io.getString("counterid"),
										io.getString("cashierid"), io.getString("billkey"), "0010", "消费卡",
										io.getString("payfee"), io.getString("kh"), io.getString("values"), "0", "0" },
								new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 3, 3, 3 }, new int[] { 2, 2, 2, 2 }, 4); // 执行存储过程
						outgmp.put("procresult", _errorInf);
						P("_errorInf=" + _errorInf);

						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "成功");
						outgmp.put("billstate", vPrcoReturn[2]);

						outgmp.put("billstate", vPrcoReturn[2]);
						outgmp.put("billkey", Integer.parseInt(io.getString("billkey")));
						outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
						outgmp.put("paymentcode", "0010");
						outgmp.put("paymentname", "消费卡");
						outgmp.put("amount", Double.parseDouble(io.getString("payfee")));
						outgmp.put("avalues", 0);
						outgmp.put("tradecode", ingmp.getString("jsh_out"));
						outgmp.put("paymentdescription", ingmp.getString("jsh_out"));
						outgmp.put("paymentmethod", "");
						outgmp.put("transactiontime", "");
						outgmp.put("paymenttime", CUtil.getOrigTime());
						outgmp.put("paymentstatus", 2);
						outgmp.put("orderno", io.getString("kh"));
						outgmp.put("sqye", 0);
						outgmp.put("quantity", 0);
						outgmp.put("price", ingmp.getDouble("fkhye_out"));
						outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
						outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
						getOtBillInfo(io.getString("billkey"), outgmp);

						P(outgmp.toString());
					}

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", inresult);
			}
			outgmp.put("storeid", io.getString("storeid"));
			outgmp.put("counterid", io.getString("counterid"));
			outgmp.put("cashierid", io.getString("cashierid"));
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 消费卡取消
	public String XfkRefund() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/ExpCard/XFKCancel.ihtml";
		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(url,
					new String[] { "zdh_in", __Request.getParameter("counterid").toString(), "dh_in",
							__Request.getParameter("billid").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString() })
					.trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			P(inresult);
			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					executeOracleProc("GmpPayReturn",
							new String[] { __Request.getParameter("billkey").toString(), "0010", "" },
							new int[] { 1, 1, 1 }, new int[] { 2 }, 4); // 记录

					outgmp.put("procerror", _errorInf);
					outgmp.put("billstate", vPrcoReturn[2]);

					outgmp.put("dyxx", ingmp.getJSONArray("dyxx_out"));
					outgmp.put("scmc", ingmp.getString("scmc_out"));
					outgmp.put("paymentcode", "0010");
					outgmp.put("time", CUtil.getOrigTime());
					// P(inresult);

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}

			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 条码券函数
	// 条码券查询
	public String TmqQuery() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/BarcodeCoupon/getTMQInfo.ihtml";
		String outresult = "";

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			JSONObject io = dataFromRequestToJson();

			P(io.toString());
			// System.out.println("o="+io.toString());
			String[] vparams = { "zdh_in", io.getString("counterid"), "kh_in", io.getString("tmqno") };

			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("kh", io.getString("tmqno"));
					outgmp.put("kzt", ingmp.getString("kzt_out"));
					outgmp.put("kye", ingmp.getDouble("kye_out"));
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "查询失败:" + inresult);
			}
			outgmp.put("storeid", io.getString("storeid"));
			outgmp.put("counterid", io.getString("counterid"));
			outgmp.put("cashierid", io.getString("cashierid"));
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 条码券支付
	public String TmqPay() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/BarcodeCoupon/TMQCommit.ihtml";
		String outresult = "";

		try {

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			JSONObject io = dataFromRequestToJson();

			String inresult = crequest.sendPostEx(url,
					new String[] { "kh_in", io.getString("tmqno"), "zdh_in", io.getString("counterid"), "je_in",
							io.getString("values"), "dh_in", io.getString("billid"), "skyh_in",
							io.getString("cashierid") })
					.trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					if (io.has("billkey")) {

						if (executeOracleProc("GmpPayOther",
								new String[] { io.getString("storeid"), io.getString("counterid"),
										io.getString("cashierid"), io.getString("billkey"), "0031", "条码现金券",
										io.getString("payfee"), io.getString("tmqno"), io.getString("values"), "0",
										"0" },
								new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 3, 3, 3 }, new int[] { 2, 2, 2, 2 }, 4) == 0) { // 执行存储过程

							outgmp.put("sub_code", "1");
							outgmp.put("sub_msg", "成功");

							outgmp.put("billstate", vPrcoReturn[2]);
							outgmp.put("billkey", Integer.parseInt(io.getString("billkey")));
							outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
							outgmp.put("paymentcode", "0031");
							outgmp.put("paymentname", "条码现金券");
							outgmp.put("amount", Double.parseDouble(io.getString("payfee")));
							outgmp.put("avalues", Double.parseDouble(io.getString("values")));
							outgmp.put("tradecode", io.getString("tmqno"));
							outgmp.put("paymentdescription", "");
							outgmp.put("paymentmethod", "");
							outgmp.put("transactiontime", "");
							outgmp.put("paymenttime", CUtil.getOrigTime());
							outgmp.put("paymentstatus", 2);
							outgmp.put("orderno", io.getString("billid"));
							outgmp.put("sqye", 0);
							outgmp.put("quantity", 0);
							outgmp.put("price", 0);
							outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
							outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
							getOtBillInfo(io.getString("billkey"), outgmp);
						} else {
							outgmp.put("procresult", _errorInf);
						}
					}

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}
			outgmp.put("storeid", io.getString("storeid"));
			outgmp.put("counterid", io.getString("counterid"));
			outgmp.put("cashierid", io.getString("cashierid"));
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
			P(outresult);
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 条码券支付取消
	public String TmqCancel() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/BarcodeCoupon/TMQCancel.ihtml";
		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(url,
					new String[] { "zdh_in", __Request.getParameter("counterid").toString(), "dh_in",
							__Request.getParameter("billid").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString() })
					.trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					executeOracleProc("GmpPayReturn",
							new String[] { __Request.getParameter("billkey").toString(), "0031", "" },
							new int[] { 1, 1, 1 }, new int[] { 2 }, 4); // 记录

					outgmp.put("procerror", _errorInf);
					outgmp.put("billstate", vPrcoReturn[2]);
					outgmp.put("paymentcode", "0031");

					P(inresult);
					// outgmp.put("dyxx", ingmp.getString("dyxx_out"));
					// outgmp.put("scmc", ingmp.getString("scmc_out"));

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}

			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 积分支付函数
	// 积分支付信息查询
	public String JfzfQuery() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfGetinfo.ihtml";
		String outresult = "";

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String[] vparams = { "zdh_in", __Request.getParameter("counterid").toString(), "kh_in",
					__Request.getParameter("kh").toString() };

			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("kh", __Request.getParameter("kh").toString());
					outgmp.put("points", ingmp.getString("points_out"));
					outgmp.put("points_min", ingmp.getDouble("points_min_out"));
					outgmp.put("penew_multiple", ingmp.getDouble("penew_multiple_out"));
					outgmp.put("dhl", ingmp.getDouble("dhl_out"));
					outgmp.put("amt", ingmp.getDouble("amt_out"));
					outgmp.put("points2", ingmp.has("points2_out") ? ingmp.getDouble("points2_out") : 0);
				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "查询失败:" + inresult);
			}
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 积分预用
	public String JfzfPreuse() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfPreuse.ihtml";
		String outresult = "";

		try {
			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String[] vparams = { "zdh_in", __Request.getParameter("counterid").toString(), "v_deal_code_in",
					__Request.getParameter("v_deal_code").toString(), "v_kh_in",
					__Request.getParameter("kh").toString() };

			String inresult = crequest.sendPostEx(url, vparams).trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

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
			outgmp.put("storeid", __Request.getParameter("storeid").toString());
			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 积分支付消费
	public String JfzfPay() {
		String outresult = "";

		try {
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfUse.ihtml",
					new String[] { "kh_in", __Request.getParameter("kh").toString(), "zdh_in",
							__Request.getParameter("counterid").toString(), "je_in",
							__Request.getParameter("payvalues").toString(), "dh_in",
							__Request.getParameter("billid").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString() })
					.trim();

			JSONObject ingmp = new JSONObject(inresult);

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					if (__Request.getParameter("billkey") != null) {
						if (executeOracleProc("GmpPayOther",
								new String[] { __Request.getParameter("kh").toString(),
										__Request.getParameter("counterid").toString(),
										__Request.getParameter("cashierid").toString(),
										__Request.getParameter("billkey").toString(), "0055", "会员积分支付",
										__Request.getParameter("payfee").toString(),
										__Request.getParameter("billid").toString(),
										__Request.getParameter("payvalues").toString(), "0",
										__Request.getParameter("kjjf").toString() },
								new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 3, 3, 3 }, new int[] { 2, 2, 2, 2 }, 4) == 0) { // 执行存储过程

							outgmp.put("sub_code", "1");
							outgmp.put("sub_msg", "成功");

							outgmp.put("billstate", vPrcoReturn[2]);
							outgmp.put("billkey", Integer.parseInt(__Request.getParameter("billkey").toString()));
							outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
							outgmp.put("paymentcode", "0055");
							outgmp.put("paymentname", "会员积分支付");
							outgmp.put("amount", Double.parseDouble(__Request.getParameter("payfee").toString()));
							outgmp.put("avalues", Double.parseDouble(__Request.getParameter("payvalues").toString()));
							outgmp.put("tradecode", __Request.getParameter("kh").toString());
							outgmp.put("paymentdescription", ingmp.getString("fhxx_out"));
							outgmp.put("paymentmethod", "");
							outgmp.put("transactiontime", "");
							outgmp.put("paymenttime", CUtil.getOrigTime());
							outgmp.put("paymentstatus", 2);
							outgmp.put("orderno", __Request.getParameter("billid").toString());
							outgmp.put("sqye", 0);
							outgmp.put("quantity", 0);
							outgmp.put("price", Double.parseDouble(__Request.getParameter("kjjf").toString()));
							outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
							outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
							getOtBillInfo(__Request.getParameter("billkey"), outgmp);
						} else {
							outgmp.put("procresult", _errorInf);
						}
					}

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}

			outgmp.put("storeid", __Request.getParameter("storeid"));
			outgmp.put("counterid", __Request.getParameter("counterid"));
			outgmp.put("cashierid", __Request.getParameter("cashierid"));
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
			P(outresult);
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 积分支付取消
	public String JfzfCancel() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfCancel.ihtml";
		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(url,
					new String[] { "zdh_in", __Request.getParameter("counterid").toString(), "dh_in",
							__Request.getParameter("billid").toString(), "kh_in",
							__Request.getParameter("kh").toString(), "je_in",
							__Request.getParameter("payfee").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString() })
					.trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					executeOracleProc("GmpPayReturn",
							new String[] { __Request.getParameter("billkey").toString(), "0055", "" },
							new int[] { 1, 1, 1 }, new int[] { 2 }, 4); // 记录

					// outgmp.put("procerror", _errorInf);
					outgmp.put("billstate", vPrcoReturn[2]);
					outgmp.put("paymentcode", "0055");

					P(inresult);
					// outgmp.put("dyxx", ingmp.getString("dyxx_out"));
					// outgmp.put("scmc", ingmp.getString("scmc_out"));

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}

			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 电子账户支付消费
	public String DzzhPay() {
		String outresult = "";

		try {
			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/DzzhPay/dzzhUse.ihtml",
					new String[] { "kh_in", __Request.getParameter("kh").toString(), "zdh_in",
							__Request.getParameter("counterid").toString(), "je_in",
							__Request.getParameter("payfee").toString(), "dh_in",
							__Request.getParameter("billid").toString(), "v_deal_code_in",
							__Request.getParameter("barcode").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString() })
					.trim();

			JSONObject ingmp = new JSONObject(inresult);

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getInt("yxbz_out") == 1) {
					double payfee = Double.parseDouble(__Request.getParameter("payfee").toString());
					double rpayfee = Double.parseDouble(ingmp.getString("v_cash_amt_out"))
							+ Double.parseDouble(ingmp.getString("v_card_amt"))
							+ Double.parseDouble(ingmp.getString("v_giveaway_amt"));
					if (payfee != rpayfee) {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg",
								"Gmp支付数据返回异常：\n" + "v_cash_amt_out:" + ingmp.getString("v_cash_amt_out") + "\n"
										+ "v_card_amt:" + ingmp.getString("v_card_amt") + "\n" + "v_giveaway_amt:"
										+ ingmp.getString("v_giveaway_amt"));
					} else {
						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "成功");

						if (__Request.getParameter("billkey") != null) {
							if (executeOracleProc("GmpPayOther",
									new String[] { __Request.getParameter("kh").toString(),
											__Request.getParameter("counterid").toString(),
											__Request.getParameter("cashierid").toString(),
											__Request.getParameter("billkey").toString(), "0054", "会员电子钱包",
											__Request.getParameter("payfee").toString(),
											__Request.getParameter("billid").toString(),
											ingmp.getString("v_cash_amt_out"), ingmp.getString("v_card_amt"),
											ingmp.getString("v_giveaway_amt") },
									new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 3, 3, 3 }, new int[] { 2, 2, 2, 2 }, 4) == 0) { // 执行存储过程

								outgmp.put("sub_code", "1");
								outgmp.put("sub_msg", "成功");

								outgmp.put("billstate", vPrcoReturn[2]);
								outgmp.put("billkey", Integer.parseInt(__Request.getParameter("billkey").toString()));
								outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
								outgmp.put("paymentcode", "0054");
								outgmp.put("paymentname", "会员电子钱包");
								outgmp.put("amount", Double.parseDouble(__Request.getParameter("payfee").toString()));
								outgmp.put("avalues", ingmp.getDouble("v_cash_amt_out"));
								outgmp.put("tradecode", __Request.getParameter("kh").toString());
								outgmp.put("paymentdescription", "");
								outgmp.put("paymentmethod", "");
								outgmp.put("transactiontime", "");
								outgmp.put("paymenttime", CUtil.getOrigTime());
								outgmp.put("paymentstatus", 2);
								outgmp.put("orderno", __Request.getParameter("billid").toString());
								outgmp.put("sqye", ingmp.getDouble("v_card_amt"));
								outgmp.put("quantity", 0);
								outgmp.put("price", ingmp.getDouble("v_giveaway_amt"));
								outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
								outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
								getOtBillInfo(__Request.getParameter("billkey"), outgmp);
							} else {
								outgmp.put("procresult", _errorInf);
							}
						}
					}

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}

			outgmp.put("storeid", __Request.getParameter("storeid"));
			outgmp.put("counterid", __Request.getParameter("counterid"));
			outgmp.put("cashierid", __Request.getParameter("cashierid"));
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
			P(outresult);
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	// 电子账户支付取消
	public String DzzhCancel() {

		String url = CInitParam.ErpServerUrl + "ShoppePay/DzzhPay/dzzhCancel.ihtml";
		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");

			String inresult = crequest.sendPostEx(url,
					new String[] { "zdh_in", __Request.getParameter("counterid").toString(), "dh_in",
							__Request.getParameter("billid").toString(), "kh_in",
							__Request.getParameter("kh").toString(), "je_in",
							__Request.getParameter("payfee").toString(), "skyh_in",
							__Request.getParameter("cashierid").toString() })
					.trim();

			JSONObject outgmp = new JSONObject();

			JSONObject ingmp = new JSONObject(inresult);
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("1")) {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "成功");

					executeOracleProc("GmpPayReturn",
							new String[] { __Request.getParameter("billkey").toString(), "0054", "" },
							new int[] { 1, 1, 1 }, new int[] { 2 }, 4); // 记录

					// outgmp.put("procerror", _errorInf);
					outgmp.put("billstate", vPrcoReturn[2]);
					outgmp.put("paymentcode", "0054");

					P(inresult);
					// outgmp.put("dyxx", ingmp.getString("dyxx_out"));
					// outgmp.put("scmc", ingmp.getString("scmc_out"));

				} else {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", ingmp.getString("fhxx_out"));
				}
			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "支付失败:" + inresult);
			}

			outgmp.put("counterid", __Request.getParameter("counterid").toString());
			outgmp.put("cashierid", __Request.getParameter("cashierid").toString());
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		}
		return outresult;
	}

	/* 银联卡支付数据保存 */
	public String BankPay() {
		String outresult = "";
		try {

			JSONArray dArray = dataFromRequestToJson().getJSONArray("data");
			JSONObject o = dArray.getJSONObject(0);

			String billkey = o.getString("billkey");

			P(dArray.toString());
			int[] paramType = new int[43];
			for (int i = 0; i < 43; i++)
				paramType[i] = 1;

			if (executeOracleProc("GmpPayBank", new String[] { billkey, o.getString("IssuerName"),
					o.getString("TransIndexCode"), o.getString("TransTime"), o.has("ARQC") ? o.getString("ARQC") : "",
					o.getString("OptCode"), o.getString("EnterMode"), o.getString("MID"), o.getString("TrxID"),
					o.has("AIP") ? o.getString("AIP") : "", o.getString("BatchNum"), o.getString("RespCode"),
					o.getString("TransAmount"), o.getString("RespDesc"), o.has("TermCap") ? o.getString("TermCap") : "",
					o.getString("AcquirerName"), o.getString("TransType"), o.has("CSN") ? o.getString("CSN") : "",
					o.has("UNPRNUM") ? o.getString("UNPRNUM") : "", o.getString("AppName"), o.getString("CertNum"),
					o.getString("CardNum"), o.getString("FeeAmount"), o.getString("TransDate"),
					o.getString("ReqTransDate"), o.getString("ReqTransTime"), o.has("ATC") ? o.getString("ATC") : "",
					o.getString("AppVersion"), o.has("TVR") ? o.getString("TVR") : "",
					o.has("APPLAB") ? o.getString("APPLAB") : "", o.has("IAD") ? o.getString("IAD") : "",
					o.getString("AppID"), o.getString("Expiry"), o.has("TSI") ? o.getString("TSI") : "",
					o.has("HostText2") ? o.getString("HostText2") : "", o.has("TID") ? o.getString("TID") : "",
					o.has("HostText1") ? o.getString("HostText1") : "", o.getString("AuthCode"),
					o.has("HostText3") ? o.getString("HostText3") : "", o.has("AID") ? o.getString("AID") : "",
					o.getString("ReferCode"), o.getString("Reference"), o.getString("paymentcode") }, paramType,
					new int[] { 2, 2, 2, 2, 1 }, 4) == 0) {
				JSONObject outgmp = new JSONObject();
				outgmp.put("code", "1");
				outgmp.put("msg", "sucess");
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功");
				outgmp.put("billstate", vPrcoReturn[2]);
				outgmp.put("billkey", Integer.parseInt(o.getString("billkey")));
				outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
				outgmp.put("paymentcode", o.getString("paymentcode"));
				outgmp.put("paymentname", vPrcoReturn[6]);
				outgmp.put("amount",
						Double.parseDouble(o.getString("TransAmount")) + Double.parseDouble(o.getString("FeeAmount")));
				outgmp.put("avalues", Double.parseDouble(o.getString("TransAmount")));
				outgmp.put("tradecode", o.getString("CertNum"));
				outgmp.put("paymentdescription", o.getString("TransIndexCode"));
				outgmp.put("paymentmethod", o.getString("TransType"));
				outgmp.put("transactiontime", o.getString("ReqTransDate"));
				outgmp.put("paymenttime", o.getString("TransDate"));
				outgmp.put("paymentstatus", 2);
				outgmp.put("orderno", o.getString("CardNum"));
				outgmp.put("sqye", 0);
				outgmp.put("quantity", 0);
				outgmp.put("price", Double.parseDouble(o.getString("FeeAmount")));
				outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
				outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
				getOtBillInfo(o.getString("billkey"), outgmp);

				outresult = outgmp.toString();
			} else {
				P(_errorInf);
				outresult = errorMsgToJson(_errorInf);
			}

		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult).trim();
	}

	/* 银行支付退款 */
	public String BankRefund() {

		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {

			String billkey = __Request.getParameter("billkey").toString();
			String tradno = __Request.getParameter("tradno").toString();
			String payno = __Request.getParameter("paymentcode").toString();
			JSONObject outgmp = new JSONObject();

			executeOracleProc("GmpPayReturn", new String[] { billkey, payno, tradno }, new int[] { 1, 1, 1 },
					new int[] { 2 }, 4); // 记录

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			outgmp.put("tradno", tradno);

			outgmp.put("billstate", vPrcoReturn[2]);
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "成功");
			outgmp.put("paymentcode", payno);

			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 其它支付方式调用 */
	public String OtherPay(String url) {

		String outresult = "";

		try {

			String storeid = __Request.getParameter("storeid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String billkey = __Request.getParameter("billkey").toString();

			JSONObject outgmp = new JSONObject();

			String[] sqlParams = { storeid, counterid, cashierid, billkey,
					__Request.getParameter("paymentcode").toString(), __Request.getParameter("paymentname").toString(),
					__Request.getParameter("payfee").toString(), "----", "0", "0", "0" };

			int[] paramType = { 1, 1, 1, 2, 1, 1, 3, 1, 3, 3, 3 };
			int[] paramOut = { 2, 2, 2, 2 };

			int nresult = executeOracleProc("GmpPayOther", sqlParams, paramType, paramOut, 4); // 执行存储过程

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			if (nresult == 0) {
				outgmp.put("sub_code", "1");
				outgmp.put("sub_msg", "成功");
				outgmp.put("billstate", vPrcoReturn[2]);

				outgmp.put("billkey", Integer.parseInt(billkey));
				outgmp.put("seq", Integer.parseInt(vPrcoReturn[3]));
				outgmp.put("paymentcode", __Request.getParameter("paymentcode").toString());
				outgmp.put("paymentname", __Request.getParameter("paymentname").toString());
				outgmp.put("amount", Double.parseDouble(__Request.getParameter("payfee").toString()));
				outgmp.put("avalues", 0);
				outgmp.put("tradecode", "");
				outgmp.put("paymentdescription", "");
				outgmp.put("paymentmethod", "");
				outgmp.put("transactiontime", "");
				outgmp.put("paymenttime", CUtil.getOrigTime());
				outgmp.put("paymentstatus", 2);
				outgmp.put("orderno", "");
				outgmp.put("sqye", 0);
				outgmp.put("quantity", 0);
				outgmp.put("price", 0);
				outgmp.put("fsbl", Integer.parseInt(vPrcoReturn[4]));
				outgmp.put("fslh", Integer.parseInt(vPrcoReturn[5]));
				getOtBillInfo(billkey, outgmp);

			} else {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", _errorInf);
			}
			outgmp.put("storeid", "0001");
			outgmp.put("counterid", counterid);
			outgmp.put("cashierid", cashierid);
			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 其它支付方式调用 */
	public String OtherRefund(String url) {

		String outresult = "";

		outresult = checkErpBillExists(__Request.getParameter("billid").toString());
		if (!outresult.equals("")) {
			return errorMsgToJson(outresult);
		}

		try {
			String billkey = __Request.getParameter("billkey").toString();
			String payno = __Request.getParameter("paymentcode").toString();

			JSONObject outgmp = new JSONObject();

			executeOracleProc("GmpPayReturn", new String[] { billkey, payno, "" }, new int[] { 1, 1, 1 },
					new int[] { 2 }, 4); // 记录

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			outgmp.put("procresult", vPrcoReturn[1]);
			outgmp.put("billstate", vPrcoReturn[2]);
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "成功");

			outgmp.put("paymentcode", payno);

			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	/* 其它支付方式调用 */
	public String RefreshBillState(String url) {

		String outresult = "";
		try {
			String billkey = __Request.getParameter("billkey").toString();

			JSONObject outgmp = new JSONObject();

			executeOracleProc("BillSetStatus", new String[] { billkey }, new int[] { 1 }, new int[] { 2 }, 4); // 记录

			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			outgmp.put("procresult", vPrcoReturn[1]);
			outgmp.put("billstate", vPrcoReturn[2]);
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "成功");

			outgmp.put("time", CUtil.getOrigTime());
			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;
	}

	public String ShiftPrintData(String shiftid) {
		String result = "";
		try {
			queryBySqlInner("select counterid,cashierid,printcount from posshift where shiftid=%s", shiftid);
			if (next()) {
				String fileName = CInitParam.realFilePath1() + "shiftdata" + getString("counterid") + "-"
						+ getString("cashierid") + "-" + shiftid + ".txt";

				JSONObject o = new JSONObject(CFile.loadFromFile(fileName));
				o.put("printcount", getInt("printcount"));
				result = o.toString();

			} else {
				result = errorMsgToJson("未找到日结数据");
			}
		} catch (Exception e) {
			result = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return CUtil.formatJson(result);
	}

	// 执行日结
	public String CheckSum() {
		String outresult = "";
		String dzqid = "0", hyqid = "0", zgqid = "0", jfkid = "0", tmqid = "0", zfbid = "0", wxid = "0";

		try {

			String storeid = __Request.getParameter("storeid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String cashier = __Request.getParameter("cashier").toString();

			String[] vparams = new String[] { "skyh_in", cashierid, "zdh_in", counterid };

			JSONObject outgmp = new JSONObject();

			// 检查是否有需要日结的数据
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (executeOracleProc("CheckShift", new String[] { counterid, cashierid }, new int[] { 1, 1 },
					new int[] { 2 }, 4) != 0) { // 执行存储过程
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", vPrcoReturn[1]);
			} else {
				int days = Integer.parseInt(vPrcoReturn[2]);

				if (days == 0) {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", "不存在未日结的账单");
				} else if (days < 0) {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", "存在未处理完成的订单，不能日结");
				} else {
					queryBySqlIDWithParamInner("R_SHIFTINFO0_SUM", new String[] { counterid, cashierid });
					JSONObject head = formatResultToJsonObject();
					outgmp.put("head", head);

					queryBySqlIDWithParamInner("R_SHIFTINFO0", new String[] { counterid, cashierid });
					JSONArray payment = formatResultToJsonArray();

					outgmp.put("payment", payment);

					CURLConnection crequest = new CURLConnection();
					crequest.setDefaultContentEncoding("utf-8");
					// 电子券结算
					JSONObject ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQCheckSum.ihtml", vparams)
							.trim());
					outgmp.put("dzq", ingmp);
					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1")
							&& ingmp.has("coupon_close_id_out"))
						dzqid = ingmp.getString("coupon_close_id_out");

					// 支付宝
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbCheckSum.ihtml", vparams).trim());
					outgmp.put("zfb", ingmp);
					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1") && ingmp.has("jsbz_out"))
						zfbid = ingmp.getString("jsbz_out");

					// 微信
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/wx/wxCheckSum.ihtml", vparams).trim());
					outgmp.put("wx", ingmp);
					if (ingmp.has("yxbz_out") && ingmp.getString("yxbz_out").equals("1") && ingmp.has("jsbz_out"))
						wxid = ingmp.getString("jsbz_out");

					// 消费券
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/ExpCard/XFKCheckSum.ihtml", vparams)
							.trim());
					outgmp.put("xfk", ingmp);

					// 条码券
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/BarcodeCoupon/TMQCheckSum.ihtml", vparams)
							.trim());
					outgmp.put("tmq", ingmp);

					// 积分支付
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfChecksum.ihtml", vparams)
							.trim());
					outgmp.put("jfzf", ingmp);

					// 电子账户支付
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/DzzhPay/dzzhChecksum.ihtml", vparams)
							.trim());
					outgmp.put("dzzh", ingmp);

					if (executeOracleProc("CallShift",
							new String[] { storeid, counterid, cashierid, cashier, dzqid, hyqid, zgqid, jfkid, tmqid,
									zfbid, wxid },
							new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, new int[] { 2 }, 4) == 0) { // 执行存储过程
						outgmp.put("shiftid", vPrcoReturn[2]);
						// outgmp.put("procresult", _errorInf);
						outgmp.put("counterid", counterid);
						outgmp.put("cashierid", cashierid);
						outgmp.put("time", CUtil.getOrigTime());
						outgmp.put("sub_code", "1");
						outgmp.put("sub_msg", "ok");

						String fileName = CInitParam.realFilePath1() + "shiftdata" + counterid + "-" + cashierid + "-"
								+ outgmp.getString("shiftid") + ".txt";

						P(fileName);
						CFile.saveToFile(fileName, outgmp.toString());
					} else {
						outgmp.put("sub_code", "0");
						outgmp.put("sub_msg", _errorInf);
					}
				}
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

	// 广百ERP未结算信息查询
	public String UnCheckList() {
		String outresult = "";

		try {
			String cashierid = __Request.getParameter("cashierid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String[] vparams = new String[] { "skyh_in", cashierid, "zdh_in", counterid };

			JSONObject outgmp = new JSONObject();
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			// 检查是否有需要日结的数据
			executeOracleProc("CheckShift", new String[] { counterid, cashierid }, new int[] { 1, 1 }, new int[] { 2 },
					4); // 执行存储过程
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");

			if (vPrcoReturn[2].equals("-20000")) {
				outgmp.put("sub_code", "0");
				outgmp.put("sub_msg", "存在未处理完成的订单");
			} else {
				if (vPrcoReturn[2].equals("0")) {
					outgmp.put("sub_code", "0");
					outgmp.put("sub_msg", "不存在未日结的账单");
				} else {
					outgmp.put("sub_code", "1");
					outgmp.put("sub_msg", "ok");

					queryBySqlIDWithParamInner("R_SHIFTINFO0_SUM", new String[] { counterid, cashierid });
					JSONObject head = formatResultToJsonObject();
					outgmp.put("head", head);

					queryBySqlIDWithParamInner("R_SHIFTINFO0", new String[] { counterid, cashierid });
					JSONArray payment = formatResultToJsonArray();

					outgmp.put("payment", payment);

					CURLConnection crequest = new CURLConnection();
					crequest.setDefaultContentEncoding("utf-8");

					// 电子券
					JSONObject ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQUncheckList.ihtml", vparams)
							.trim());
					outgmp.put("dzq", ingmp);

					// 支付宝
					ingmp = new JSONObject(
							crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbUncheckList.ihtml", vparams)
									.trim());
					outgmp.put("zfb", ingmp);

					// 微信
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/wx/wxUncheckList.ihtml", vparams).trim());
					outgmp.put("wx", ingmp);

					// 消费卡
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/ExpCard/XFKUncheckList.ihtml", vparams)
							.trim());
					outgmp.put("xfk", ingmp);

					// 条码券
					ingmp = new JSONObject(crequest.sendPostEx(
							CInitParam.ErpServerUrl + "ShoppePay/BarcodeCoupon/TMQUncheckList.ihtml", vparams).trim());
					outgmp.put("tmq", ingmp);

					// 积分支付
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfUnchecklist.ihtml", vparams)
							.trim());
					outgmp.put("jfzf", ingmp);

					// 电子账户支付
					ingmp = new JSONObject(crequest
							.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/DzzhPay/dzzhUnchecklist.ihtml", vparams)
							.trim());
					outgmp.put("dzzh", ingmp);
				}
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

	// 广百ERP已结算信息查询
	public String CheckList() {
		String outresult = "";
		try {
			// String storeid = __Request.getParameter("storeid").toString();
			String cashierid = __Request.getParameter("cashierid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String[] vparams = new String[] { "skyh_in", cashierid, "zdh_in", counterid };

			JSONObject outgmp = new JSONObject();

			CURLConnection crequest = new CURLConnection();
			crequest.setDefaultContentEncoding("utf-8");
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", "ok");

			// 电子券
			JSONObject ingmp = new JSONObject(crequest
					.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/EleCoupon/DZQCheckList.ihtml", vparams).trim());
			outgmp.put("dzq", ingmp);

			// 支付宝
			ingmp = new JSONObject(
					crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/Ali/zfbCheckList.ihtml", vparams).trim());
			outgmp.put("zfb", ingmp);

			// 微信
			ingmp = new JSONObject(
					crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/wx/wxCheckList.ihtml", vparams).trim());
			outgmp.put("wx", ingmp);

			// 消费券
			ingmp = new JSONObject(crequest
					.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/ExpCard/XFKCheckList.ihtml", vparams).trim());
			outgmp.put("xfk", ingmp);

			// 条码券
			ingmp = new JSONObject(
					crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/BarcodeCoupon/TMQCheckList.ihtml", vparams)
							.trim());
			outgmp.put("tmq", ingmp);

			// 积分支付
			ingmp = new JSONObject(crequest
					.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/PointPay/jfzfChecklist.ihtml", vparams).trim());
			outgmp.put("jfzf", ingmp);

			// 电子账户支付
			ingmp = new JSONObject(crequest
					.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/DzzhPay/dzzhChecklist.ihtml", vparams).trim());
			outgmp.put("dzzh", ingmp);

			outresult = CUtil.formatJson(outgmp.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}

		return outresult;

	}

	private String checkErpBillExists(String billid) {
		String result = "未知错误,不能执行撤销";
		CURLConnection crequest = new CURLConnection();
		crequest.setDefaultContentEncoding("utf-8");
		try {
			String inresult = crequest.sendPostEx(CInitParam.ErpServerUrl + "ShoppePay/spay/queryDh.ihtml?",
					new String[] { "dh_in", billid }).trim();
			JSONObject ingmp = new JSONObject(inresult);

			if (ingmp.has("yxbz_out")) {
				if (ingmp.getString("yxbz_out").equals("0")) {
					result = ingmp.getString("fhxx_out");
				} else {
					result = "";
				}
			} else {
				result = getGmpErrorMsg(ingmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage();
		} finally {
			closeConn();
		}
		return result;
	}
}
