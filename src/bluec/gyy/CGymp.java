package bluec.gyy;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import bluec.base.CHttpService;
import bluec.base.CInitParam;
import bluec.base.CJson;
import bluec.base.CUtil;

public class CGymp extends CJson {
	public CGymp(HttpServletRequest request) {
		super(request);
	}

	public CGymp() {

	}

	/* 保存订单数据 */
	public String BillSave() {
		String billkey = __Request.getParameter("billkey").toString();
		String billid = __Request.getParameter("billid").toString();
		String issaved = __Request.getParameter("issaved").toString(); // 挂单标记
		// boolean ischange =
		// __Request.getParameter("ischange").toString().equals("1"); // 数据有变化
		String outresult = "";
		try {
			P(__Request.getParameter("data").toString());
			JSONObject inpJson = new JSONObject(__Request.getParameter("data").toString());

			JSONArray hArray = inpJson.getJSONArray("head");
			JSONArray gArray = inpJson.getJSONArray("products");
			JSONArray pArray = inpJson.getJSONArray("payment");

			hArray.getJSONObject(0).put("issaved", issaved);

			int rcount = hArray.length() + gArray.length() + pArray.length();

			dataAllSqls = new String[rcount + 3];

			// 先删除临时表数据
			dataAllSqls[0] = "delete from PosBill0 where billkey=" + billkey;
			dataAllSqls[1] = "delete from PosBillProducts0 where billkey=" + billkey;
			dataAllSqls[2] = "delete from PosBillPayment0 where billkey=" + billkey;

			// 生成执行的SQL
			int index = createInsertValues("PosBill0", hArray, 3, hArray.length());
			index = createInsertValues("PosBillProducts0", gArray, index, gArray.length());
			createInsertValues("PosBillPayment0", pArray, index, pArray.length());

			P(dataAllSqls);
			int nok = executeSqls(dataAllSqls);
			if (nok == 0) {
				if (executeOracleProc("BillSave",
						new String[] { __Request.getParameter("storeid").toString(),
								__Request.getParameter("counterid").toString(),
								__Request.getParameter("cashierid").toString(), billkey },
						new int[] { 1, 1, 1, 2 }, null, 4) != 0) { // 执行存储过程
					throw new Exception("保存订单失败。");
				}
			} else {
				throw new Exception(_errorInf);
			}
			outresult = sucessMsgToJson0("ok", new String[] { "billkey", billkey, "billid", billid });
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 撤销订单 */
	public String BillUndo() {
		String billkey = __Request.getParameter("billkey").toString();
		String billid = __Request.getParameter("billid").toString();
		String outresult = "";
		try {
			if (executeOracleProc("BillUndo",
					new String[] { __Request.getParameter("storeid").toString(),
							__Request.getParameter("counterid").toString(),
							__Request.getParameter("cashierid").toString(), billkey },
					new int[] { 1, 1, 1, 2 }, null, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			}
			outresult = sucessMsgToJson0("ok", new String[] { "billkey", billkey, "billid", billid });
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 订单完成确定 */
	public String BillConfirm() {
		String billkey = __R$("billkey");
		String billid = __R$("billid");
		Boolean isys = __R$("billtype").equals("1"); // 是预售单
		int toErp = 0;
		String toerpmsg = "";
		String outresult = "";
		try {
			if (executeOracleProc("BillConfirm",
					new String[] { __R$("counterid"), __R$("erpcashid"), __R$("cashierid"), __R$("cashier"), billkey },
					new int[] { 1, 1, 1, 1, 2 }, null, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			}
			if (!isys) { // 非预售单马上上传erp
				JSONObject inJson = new JSONObject(sendBillToErp(billkey, ""));
				if (inJson.getInt("sub_code") != 0) {
					toErp = -1;
					toerpmsg = inJson.getString("sub_msg") + ",请到办理中心重新上传订单";
				}
			}
			outresult = sucessMsgToJson0("ok",
					new String[] { "billkey", billkey, "billid", billid, "toerp", toErp + "", "toerpmsg", toerpmsg });
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode0(e.getMessage());
		} finally {
			closeConn();
		}
		P(outresult);
		return CUtil.formatJson(outresult);
	}

	/* 设置订单为款台收款 */
	public String BillPashPay() {
		String billkey = __R$("billkey");
		String billid = __R$("billid");
		String outresult = "";
		try {
			if (executeOracleProc("BillPashPay",
					new String[] { __R$("storeid"), __R$("counterid"), __R$("cashierid"), billkey },
					new int[] { 1, 1, 1, 2 }, null, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			}
			outresult = sucessMsgToJson0("ok", new String[] { "billkey", billkey, "billid", billid });
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 获取订单明细 */
	private String BillInfo(String billkey, Boolean nissaved, Boolean isprint, Boolean ispaycash) {

		String outresult = "";
		try {
			if (ispaycash) {
				queryBySqlInner("select billstate,paidbycash from PosBill where billkey='%s'", billkey);
				next();
				if (getInt("billstate") != 1) {
					throw new Exception("订单不在待支付状态");
				}
				if (getInt("paidbycash") != 1) {
					throw new Exception("订单不允许款台代收");
				}
			}
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

			o.put("code", "0");
			o.put("msg", "sucess");
			o.put("sub_code", "0");
			o.put("sub_msg", "ok");

			if (nissaved) {
				executeUpdate("update posbill set issaved=0 where billkey=%s", new String[] { billkey });
			}

			if (isprint) {
				executeUpdate("update posbill set printcount=printcount+1 where billkey=%s", new String[] { billkey });
			}
			outresult = o.toString();
			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public String BillInfo() {
		return BillInfo(__R$("billkey"), __R$("nissaved") != null, __R$("isprint") != null, __R$("paycash") != null);
	}

	public String BillInfoById() {
		String billkey = "0";
		String outresult = "";
		try {
			queryBySqlInner("select billkey from PosBill where billid='%s'", __R$("billid"));
			if (next()) {
				billkey = getString("billkey");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConn();
		}
		if(billkey.equals("0")){
			outresult = errorMsgToJson0("订单不存在");
		}else{
			outresult = BillInfo(billkey, false, false, false);
		}
		return outresult;
	}

	/* 获取订单明细 */
	public String BillInfoEx() {
		String billkey = __R$("billkey").toString();

		String outresult = "";
		try {

			queryBySqlInner("select * from PosBill where billkey='%s'", billkey);
			JSONArray mjson = formatResultToJsonArray(); // 主表
			switch (requestInt("op", 0)) {
			case 1:
				JSONObject head = mjson.getJSONObject(0);
				if (!head.getString("vipid").equals("")) {
					head.put("vipinfo",
							String.format("%s(%s)", head.getString("vipid"), head.getString("viptypename")));
				}
				if (!head.getString("customer").equals("")) {
					head.put("customerinfo",
							String.format("%s(%s)", head.getString("customer"), head.getString("mtel")));
				}
				break;
			}

			queryBySqlInner("select * from PosBillProducts where billkey='%s'", billkey);
			JSONArray gjson = formatResultToJsonArray(); // 商品表
			queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
			JSONArray pjson = formatResultToJsonArray(); // 支付表

			JSONObject o = new JSONObject();
			o.put("head", mjson);
			o.put("products", gjson);
			o.put("payment", pjson);

			o.put("code", "0");
			o.put("msg", "sucess");
			o.put("sub_code", "0");
			o.put("sub_msg", "ok");

			outresult = o.toString();
			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 银联支付 */
	public String PayBank() {
		String billkey = __Request.getParameter("billkey").toString();
		String outresult = "";
		try {
			P(__Request.getParameter("data").toString());
			JSONObject json = new JSONObject(__Request.getParameter("data").toString());
			String paymentcode = "03";
			String paymentname = "银联卡";
			String payfee = Double.parseDouble(json.getString("transAmount")) / 100.0 + "";
			String paytime = json.getString("transTime");
			paytime = CUtil.getOrigTime();

			if (executeOracleProc("PayBank",
					new String[] { __Request.getParameter("storeid").toString(),
							__Request.getParameter("counterid").toString(),
							__Request.getParameter("cashierid").toString(), billkey, paymentcode, paymentname, payfee,
							json.getString("cardNo"), json.getString("refNo"), json.getString("voucherNo"), paytime,
							json.getString("merchId"), json.getString("merchName") },
					new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 1, 1, 1, 1, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			} else {
				queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
				JSONArray pjson = formatResultToJsonArray(); // 支付表
				outresult = sucessMsgToJson0("ok",
						new String[] { "billkey", billkey, "billstate", __procReturn[0], "paystate", __procReturn[1] },
						"payment", pjson);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 集合支付 */
	public String PayMarge() {
		String billkey = __Request.getParameter("billkey").toString();
		String outresult = "";
		try {
			P(__Request.getParameter("data").toString());
			JSONObject json = new JSONObject(__Request.getParameter("data").toString());
			Boolean isZfb = json.getString("cardNo").substring(0, 1).equals("2");
			String paymentcode = isZfb ? "0204" : "0301";
			String paymentname = isZfb ? "支付宝" : "微信";
			String payfee = Double.parseDouble(json.getString("transAmount")) / 100.0 + "";
			String paytime = json.getString("transTime");
			paytime = paytime.substring(0, 4) + "-" + paytime.substring(4, 6) + "-" + paytime.substring(6, 8) + " "
					+ paytime.substring(8, 10) + ":" + paytime.substring(10, 12) + ":" + paytime.substring(12);

			if (executeOracleProc("PayMarge",
					new String[] { __Request.getParameter("storeid").toString(),
							__Request.getParameter("counterid").toString(),
							__Request.getParameter("cashierid").toString(), billkey, paymentcode, paymentname, payfee,
							json.getString("cardNo"), json.getString("transactionId"), json.getString("voucherNo"),
							paytime, json.getString("bank_type"), json.getString("trade_channel") },
					new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 1, 1, 1, 1, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			} else {
				queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
				JSONArray pjson = formatResultToJsonArray(); // 支付表
				outresult = sucessMsgToJson0("ok",
						new String[] { "billkey", billkey, "billstate", __procReturn[0], "paystate", __procReturn[1] },
						"payment", pjson);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 支付补录 */
	public String PayOther() {
		String billkey = __Request.getParameter("billkey").toString();
		String outresult = "";
		try {
			if (executeOracleProc("PayOther",
					new String[] { __R$("storeid"), __R$("counterid"), __R$("cashierid"), billkey, __R$("paymentcode"),
							__R$("paymentname"), __R$("payfee"), "补录", __R$("tradecode"), __R$("uid"), __R$("uname") },
					new int[] { 1, 1, 1, 2, 1, 1, 3, 1, 1, 1, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			} else {
				queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
				JSONArray pjson = formatResultToJsonArray(); // 支付表
				outresult = sucessMsgToJson0("ok",
						new String[] { "billkey", billkey, "billstate", __procReturn[0], "paystate", __procReturn[1] },
						"payment", pjson);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 现金支付 */
	public String PayCash() {
		String billkey = __Request.getParameter("billkey").toString();
		String outresult = "";
		try {
			if (executeOracleProc("PayCash",
					new String[] { __R$("storeid"), __R$("counterid"), __R$("cashierid"), billkey, "01", "现金",
							__R$("payfee"), __R$("payvalues"), CUtil.getOrigTime() },
					new int[] { 1, 1, 1, 2, 1, 1, 3, 3, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			} else {
				queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
				JSONArray pjson = formatResultToJsonArray(); // 支付表
				outresult = sucessMsgToJson0("ok",
						new String[] { "billkey", billkey, "billstate", __procReturn[0], "paystate", __procReturn[1] },
						"payment", pjson);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 积分卡/券支付 */
	public String PayScoreOrCoupon() {
		String billkey = __R$("billkey");
		String kh = __R$("kh");
		String billid = __R$("billid");
		String orgid = __R$("orgid");
		String erpcashid = __R$("erpcashid");
		String payje = __R$("payje");

		String outresult = "";
		Boolean isScore = __R$("type").equals("1");
		String paycode = (isScore) ? "04" : "05";
		String payname = (isScore) ? "积分卡" : "礼券";
		try {

			CGyvip vip = new CGyvip();
			if (CInitParam.erpServerUrl.equals("")) {
				outresult = vip.card_sendmzk(paycode, kh, billid, orgid, erpcashid, billid, payje, "");
			} else {
				outresult = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/iymp/payscoreorcoupon2.jsp",
						new String[] { "billkey", billkey, "kh", kh, "billid", billid, "orgid", orgid, "erpcashid",
								erpcashid, "billid", billid, "payje", payje, "paycode", paycode });
			}

			P(outresult);
			P("payprice=" + __Request.getParameter("payprice").toString());
			JSONObject o = new JSONObject(outresult);
			if (o.getInt("code") == 0 && o.getInt("sub_code") == 0) {
				if (executeOracleProc("PayScore",
						new String[] { __R$("storeid"), __R$("counterid"), __R$("cashierid"), billkey, paycode, payname,
								__R$("payje"), __R$("payvalues"), __R$("payprice"), __R$("kh"), CUtil.getOrigTime(),
								__R$("memo") },
						new int[] { 1, 1, 1, 2, 1, 1, 3, 3, 3, 1, 1, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
					throw new Exception(_errorInf);
				} else {
					queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
					JSONArray pjson = formatResultToJsonArray(); // 支付表
					outresult = sucessMsgToJson0("ok", new String[] { "billkey", billkey, "billstate", __procReturn[0],
							"paystate", __procReturn[1] }, "payment", pjson);
				}
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(outresult);
		return CUtil.formatJson(outresult);
	}

	/* 积分卡/券支付撤销 */
	public String PayScoreOrCouponCancel() {
		String billkey = __R$("billkey");
		String seq = __R$("seq");
		String kh = __R$("kh");
		String billid = __R$("billid");
		String orgid = __R$("orgid");
		String erpcashid = __R$("erpcashid");
		String payje = __R$("payje");

		Boolean isScore = __R$("type").equals("1");
		String paycode = (isScore) ? "04" : "05";

		String outresult = "";
		try {
			CGyvip vip = new CGyvip();
			if (CInitParam.erpServerUrl.equals("")) {
				outresult = vip.card_sendmzkcancel(paycode, kh, billid, orgid, erpcashid, billid, payje, "");
			} else {
				outresult = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/iymp/payscoreorcoupon2.jsp",
						new String[] { "billkey", billkey, "kh", kh, "billid", billid, "orgid", orgid, "erpcashid",
								erpcashid, "billid", billid, "payje", payje, "paycode", paycode });
			}
			JSONObject o = new JSONObject(outresult);
			if (o.getInt("code") == 0 && o.getInt("sub_code") == 0) {
				if (executeOracleProc("PayRefund",
						new String[] { __R$("counterid"), __R$("erpcashid"), __R$("cashierid"), __R$("cashier"),
								billkey, seq, "", "", __R$("authuid"), __R$("authuname") },
						new int[] { 1, 1, 1, 2, 1, 1, 3, 3, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
					throw new Exception(_errorInf);
				} else {
					queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
					JSONArray pjson = formatResultToJsonArray(); // 支付表
					outresult = sucessMsgToJson0("ok", new String[] { "billkey", billkey, "billstate", __procReturn[0],
							"paystate", __procReturn[1] }, "payment", pjson);
				}
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 支付撤销 */
	public String PayRefund() {
		String billkey = __Request.getParameter("billkey").toString();
		String seq = __Request.getParameter("seq").toString();
		String outresult = "";
		try {
			if (executeOracleProc("PayRefund",
					new String[] { __R$("counterid"), __R$("erpcashid"), __R$("cashierid"), __R$("cashier"), billkey,
							seq, __R$("tradecode"), __R$("lsh"), __R$("authuid"), __R$("authuname") },
					new int[] { 1, 1, 1, 1, 2, 2, 1, 1, 1, 1 }, new int[] { 2, 2 }, 4) != 0) { // 执行存储过程
				throw new Exception(_errorInf);
			} else {
				queryBySqlInner("select * from PosBillPayment where billkey='%s'", billkey);
				JSONArray pjson = formatResultToJsonArray(); // 支付表
				outresult = sucessMsgToJson0("ok",
						new String[] { "billkey", billkey, "billstate", __procReturn[0], "paystate", __procReturn[1] },
						"payment", pjson);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	/* 整单折扣计算 */
	private String calErpDiscount(String vsyjh, String vmkt, String vcardid, String vcxsj, String vsalelist) {
		String outresult = "";
		try {
			if (CInitParam.erpServerUrl.equals("")) {
				outresult = new CGyerp().pos_calsalelist(vsyjh, vmkt, vcardid, vcxsj, vsalelist);
			} else {
				String[] vparams = new String[] { "erpcashid", vsyjh, "orgid", vmkt, "vcardid", vcardid, "vcxsj", vcxsj,
						"vsalelist", vsalelist };
				outresult = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/ierp/calerpdiscount.jsp", vparams);
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String calErpDiscount() {
		String outresult = "";
		try {
			outresult = calErpDiscount(requestParam("erpcashid"), requestParam("orgid"), requestParam("vcardid"),
					requestParam("vcxsj"), requestParam("vsalelist"));
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		}
		P(outresult);
		return outresult;
	}

	/* 上传小票 */
	private String sendBillToErp(String billkey, String belongdate) {
		String outresult = "";
		try {
			if (!belongdate.equals("")) { // 更新预售交货时间
				executeUpdate("update posbill set belongdate=to_date('%s','yyyy-MM-dd HH24:mi:ss') where billkey=%s",
						new String[] { belongdate, billkey });
			}
			queryBySqlInner(
					"select listno,barcode,name,code,type,gz,xl,pp,spec,unit,bzhl, "
							+ "sl,sj,je,custzk,custzkfd,popzk,popzkfd,popdjbh,"
							+ "rulezk, rulezkfd, ruledjbh,zpzk,dpzk,grantno from ymsbillproducts where billkey='%s'",
					billkey);
			JSONArray gjson = formatResultToJsonArray(); // 商品表
			queryBySqlInner("select listno,paycode,je,hl,payno,memo from ymsbillpayment where billkey='%s'", billkey);
			JSONArray pjson = formatResultToJsonArray(); // 支付表

			queryBySqlInner("select * from ymsbill where billkey='%s'", billkey);
			next();
			String vsyjh = getString("vsyjh");
			String vfphm = getString("vfphm");
			String vmkt = getString("vmkt");
			String vyyyh = getString("vyyyh");
			String vsaletime = getString("vsaletime");
			String vcardid = getString("vcardid");
			String vsaletype = getString("vsaletype");
			String vysje = getString("vysje");
			String vfkje = getString("vfkje");
			String vzl = getString("vzl");
			String vsy = getString("vsy");
			String vsalelist = gjson.toString();
			String vpaylist = pjson.toString();
			String vyfphm = getString("vyfphm");

			if (CInitParam.erpServerUrl.equals("")) {
				outresult = new CGyerp().pos_sendsale(vsyjh, vfphm, vmkt, vyyyh, vsaletime, vcardid, vsaletype, vysje,
						vfkje, vzl, vsy, vsalelist, vpaylist, vyfphm);
			} else {
				String[] vparams = new String[] { "vsyjh", vsyjh, "vfphm", vfphm, "vmkt", vmkt, "vyyyh", vyyyh,
						"vsaletime", vsaletime, "vcardid", vcardid, "vsaletype", vsaletype, "vysje", vysje, "vfkje",
						vfkje, "vzl", vzl, "vsy", vsy, "vsalelist", vsalelist, "vpaylist", vpaylist, "vyfphm", vyfphm };
				outresult = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/ierp/sendbilltoerp2.jsp", vparams);
			}
			JSONObject o = new JSONObject(outresult);
			if (o.getInt("code") == 0 && o.getInt("sub_code") == 0) {
				if (updateBySqlWithParamInner("update posbill set erpbillid='%s' where billkey=%s",
						new String[] { o.getString("erpbillid"), billkey }) == 0) {
					o.put("sub_code", -1);
					o.put("sub_code", _errorInf);
					outresult = o.toString();
				}
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(outresult);
		return outresult;
	}

	public String sendBillToErp() {
		return sendBillToErp(requestParam("billkey"), requestParam("belongdate", ""));
	}

	/* 数据同步 */
	// 商场数据
	public String readRrpStore() {
		String outresult = "";
		try {
			String orgid = __Request.getParameter("orgid").toString();
			JSONObject json = new JSONObject((new CCommon()).YmpGetMarketInfo(orgid));
			if (json.getInt("code") != 0 || json.getInt("sub_code") != 0) {
				outresult = errorMsgToXml(json.getString("sub_msg"));
			} else {
				if (json.getInt("recordcount") == 0) {
					outresult = errorMsgToXml("查询不到Erp数据");
				} else {
					if (saveJsonArrayToDb("tran_store", json.getJSONArray("data"), "-1", new String[] { "osno" },
							new String[] { orgid })
							&& executeOracleProc("erp_tran.tran_store", new String[] { orgid }, new int[] { 1 }, null,
									4) == 0) {
						outresult = sucessMsgToXml("ok");
					} else {
						outresult = errorMsgToXml(_errorInf);
					}
				}
			}
		} catch (Exception ex) {
			outresult = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 专柜数据
	public String readRrpCounter() {
		String outresult = "";
		try {
			String storeid = __Request.getParameter("storeid").toString();
			JSONObject json = new JSONObject((new CCommon()).YmpGetCounterInfo(storeid));
			if (json.getInt("code") != 0 || json.getInt("sub_code") != 0) {
				outresult = errorMsgToXml(json.getString("sub_msg"));
			} else {
				if (json.getInt("recordcount") == 0) {
					outresult = errorMsgToXml("查询不到Erp数据");
				} else {
					if (saveJsonArrayToDb("tran_counter", json.getJSONArray("data"), "-1",
							new String[] { "ssno", "osno", "supplyid" },
							new String[] { storeid, storeid.substring(0, 3), "" })
							&& executeOracleProc("erp_tran.tran_counter", new String[] { storeid }, new int[] { 1 },
									null, 4) == 0) {
						outresult = sucessMsgToXml("ok");
					} else {
						outresult = errorMsgToXml(_errorInf);
					}
				}
			}
		} catch (Exception ex) {
			outresult = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 商品数据
	public String readRrpPoscode(String counterid) {
		String outresult = "";
		try {
			JSONObject json = new JSONObject((new CCommon()).YmpGetGoodsInfo(counterid));
			if (json.getInt("code") != 0 || json.getInt("sub_code") != 0) {
				outresult = errorMsgToXml(json.getString("sub_msg"));
			} else {
				if (json.getInt("recordcount") == 0) {
					outresult = errorMsgToXml("查询不到Erp数据");
				} else {
					if (saveJsonArrayToDb("tran_poscode", json.getJSONArray("data"), "-1",
							new String[] { "csno", "ssno", "osno" },
							new String[] { counterid, counterid.substring(0, 5), counterid.substring(0, 3) })
							&& executeOracleProc("erp_tran.tran_poscode", new String[] { counterid }, new int[] { 1 },
									null, 4) == 0) {
						outresult = sucessMsgToXml("ok");
					} else {
						outresult = errorMsgToXml(_errorInf);
					}
				}
			}
		} catch (Exception ex) {
			outresult = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String readRrpPoscode() {
		return readRrpPoscode(__Request.getParameter("counterid").toString());
	}
}
