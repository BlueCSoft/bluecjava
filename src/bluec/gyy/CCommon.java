package bluec.gyy;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import bluec.base.CAppListener;
import bluec.base.CHttpService;
import bluec.base.CInitParam;
import bluec.base.CJson;
import bluec.base.CUtil;

public class CCommon extends CJson {

	private String mcashierid = "";
	private String mcashier = "";

	private String morgid = "";
	private String mstoreid = "";
	private String mcounterid = "";
	private String msupplyid = "";
	private String mmposid = "";
	private String merpcashid = "";

	public CCommon(){
		
	}
	public CCommon(HttpServletRequest request) {
		super(request);
	}

	/* 获取基本参数 */
	private void getBaseParam() {
		if (__Request.getParameter("cashierid") != null)
			mcashierid = __Request.getParameter("cashierid").toString();
		if (__Request.getParameter("cashier") != null)
			mcashier = __Request.getParameter("cashier").toString();

		if (__Request.getParameter("orgid") != null)
			morgid = __Request.getParameter("orgid").toString();
		if (__Request.getParameter("storeid") != null)
			mstoreid = __Request.getParameter("storeid").toString();
		if (__Request.getParameter("counterid") != null)
			mcounterid = __Request.getParameter("counterid").toString();
		if (__Request.getParameter("supplyid") != null)
			msupplyid = __Request.getParameter("supplyid").toString();
		if (__Request.getParameter("mposid") != null)
			mmposid = __Request.getParameter("mposid").toString();
		if (__Request.getParameter("erpcashid") != null)
			merpcashid = __Request.getParameter("erpcashid").toString();
	}

	// 收银员登录
	public String YmpCashierLogin() {
		String outresult = "";
		try {
			// P(__Request.getParameter("cashierid").toString());
			String cashierid = __Request.getParameter("cashierid").toString();
			String mposid = __Request.getParameter("mposid").toString();
			String pwd = CUtil.MD5(__Request.getParameter("pwd").toString());
			String osid = CAppListener.getParam("osid");
            String erpcashid = "",counterid = "";
            int ispaycash = 0;
            
			JSONObject outgmp = new JSONObject();
			executeQuery3("select erpcashid,counterid,ispaycash from base_mpos where sno='%s' ",
					new String[] { mposid });
			if (!next()) {
				throw new Exception("错误或不存在的机具号");
			}
			
			erpcashid = getString("erpcashid");
			counterid = getString("counterid");
			ispaycash = getInt("ispaycash");
			
			executeQuery3(
					"select a.sid,a.sno,a.sname,a.pwd,a.status,b.sno counterid,b.sname counter,"
							+ " b.supplyid,c.sno storeid,c.sname store,"
							+ " d.sno orgid,d.sname org,b.receiptnote,b.lockscreen,b.roundbit "
							+ " from base_sales a,base_counter b,base_store c,base_org d "
							+ " where a.sno='%s' and a.osno='%s' and a.iscash=1 and b.sno=a.csno and c.sno=a.ssno and d.sno=a.osno",
					new String[] { cashierid, osid });

			if (!next()) {
				throw new Exception("错误或不存在的工号");
			} else {
				if (!pwd.equals(getString("pwd"))) {
					throw new Exception("密码不正确");
				} else {
					if (!counterid.equals(getString("counterid"))) {
						throw new Exception("当前账号禁止在此机具登录");
					} else {
						if (getInt("status") == 0) {
							throw new Exception("当前账号被禁用");
						}
					}
				}
			}
			
			//同步销售码数据
			new CGymp().readRrpPoscode(counterid);
			
			outgmp.put("sid", getInt("sid"));

			outgmp.put("cashierid", cashierid);
			outgmp.put("cashier", getString("sname"));

			outgmp.put("counterid", counterid);
			outgmp.put("counter", getString("counter"));

			outgmp.put("supplyid", getString("supplyid"));
			outgmp.put("storeid", getString("storeid"));
			outgmp.put("store", getString("store"));
			outgmp.put("orgid", getString("orgid"));
			outgmp.put("org", getString("org"));

			String receiptnote = CUtil.formatStr(getString("receiptnote"), "\n", "&#x000A;");
			outgmp.put("receiptnote", receiptnote);
			outgmp.put("lockscreen", getInt("lockscreen"));
			outgmp.put("roundbit", getInt("roundbit"));

			outgmp.put("erpcashid", erpcashid);
			outgmp.put("ispaycash", ispaycash);
			
			// 销售码
			executeQuery3("select sno,sname,price,priceatt,shortcut from base_poscode where osno='%s' and csno='%s' "
					+ "order by shortcut,sno", new String[] { osid, counterid });

			JSONArray codeArray = new JSONArray();

			while (next()) {
				JSONObject obj = new JSONObject();
				obj.put("code", getString("sno"));
				obj.put("name", getString("sname"));
				obj.put("price", getFloat("price"));
				obj.put("priceatt", getInt("priceatt"));
				obj.put("shortcut", getInt("shortcut"));
				codeArray.put(obj);
			}
			outgmp.put("poscode", codeArray);

			// 营业员
			executeQuery3("select sno,sname from base_sales where osno='%s' and csno='%s' and status=1 ",
					new String[] { osid, counterid });

			JSONArray salesArray = new JSONArray();

			while (next()) {
				JSONObject obj = new JSONObject();
				obj.put("sno", getString("sno"));
				obj.put("sname", getString("sname"));
				salesArray.put(obj);
			}

			outgmp.put("sales", salesArray);

			// 订单状态
			executeQuery("select billstate,statename from sys_billstate order by isort");

			JSONArray billstateArray = new JSONArray();

			while (next()) {
				JSONObject obj = new JSONObject();
				obj.put("sno", getString("billstate"));
				obj.put("sname", getString("statename"));
				billstateArray.put(obj);
			}

			outgmp.put("billstates", billstateArray);

			// 订单状态
			executeQuery("select code,cname from pub_payment");

			JSONArray paymentArray = new JSONArray();

			while (next()) {
				JSONObject obj = new JSONObject();
				obj.put("sno", getString("code"));
				obj.put("sname", getString("cname"));
				paymentArray.put(obj);
			}

			outgmp.put("payments", paymentArray);

			// 订单状态
			executeQuery("select viptypeid,viptypename from sys_viptype");

			JSONArray viptypeArray = new JSONArray();

			while (next()) {
				JSONObject obj = new JSONObject();
				obj.put("sno", getString("viptypeid"));
				obj.put("sname", getString("viptypename"));
				viptypeArray.put(obj);
			}

			outgmp.put("viptypes", viptypeArray);

			if (executeOracleProc("LoginOrOut",
					new String[] { cashierid, outgmp.getString("cashier"), outgmp.getString("orgid"),
							outgmp.getString("storeid"), outgmp.getString("counterid"), mposid,
							outgmp.getString("supplyid"), "0" },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 2 }, null, 4) != 0) { // 记录登录信息
				throw new Exception(_errorInf);
			}
			outgmp.put("time", CUtil.getOrigTime());

			outgmp.put("workdate", CUtil.getOrigDate());
			outgmp.put("code", "0");
			outgmp.put("msg", "sucess");

			outgmp.put("sub_code", "0");
			outgmp.put("sub_msg", "ok");

			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}

		return CUtil.formatJson(outresult.trim());
	}

	// 收银员注销退出
	public String YmpCashierLoginOut() {
		String result = "";
		try {
			getBaseParam();

			if (executeOracleProc("LoginOrOut",
					new String[] { mcashierid, mcashier, morgid, mstoreid, mcounterid, mmposid, msupplyid, "1" },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 2 }, null, 4) == 0) {
				result = sucessMsgToJson0("ok");
			} else {
				result = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			result = errorMsgToJson0(ex.getMessage());
		}
		return CUtil.formatJson(result);
	}

	// 营业员登录
	public String YmpSalesLogin() {
		String outresult = "";
		try {
			String salesid = __Request.getParameter("salesid").toString();
			String counterid = __Request.getParameter("counterid").toString();
			String pwd = CUtil.MD5(__Request.getParameter("pwd").toString());
			String osid = CAppListener.getParam("osid");

			executeQuery3("select a.sid,a.sno,a.sname,a.pwd,a.status,b.sno counterid "
					+ " from base_sales a,base_counter b " + " where a.sno='%s' and a.osno='%s' and b.sno=a.csno",
					new String[] { salesid, osid });

			if (!next()) {
				throw new Exception("错误或不存在的工号");
			} else {
				if (!pwd.equals(getString("pwd"))) {
					throw new Exception("密码不正确");
				} else {
					if (!counterid.equals(getString("counterid"))) {
						throw new Exception("当前工号不属于本专柜");
					} else {
						if (getInt("status") == 0) {
							throw new Exception("当前工号被禁用");
						}
					}
				}
			}
			outresult = sucessMsgToJson0("ok", new String[] { "uid", salesid, "uname", getString("sname") });
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}

		return CUtil.formatJson(outresult.trim());
	}

	// 管理员登录
	public String YmpUserLogin() {
		String outresult = "";
		try {
			getBaseParam();
			String uid = __Request.getParameter("uid").toString();
			String pwd = CUtil.MD5(__Request.getParameter("pwd").toString());

			executeQuery3("select username,user_pass pwd,psid orgid,status from pub_user\n"
					+ "where usercode='%s'", new String[] { uid });

			if (!next()) {
				throw new Exception("错误或不存在的工号");
			} else {
				if (!pwd.equals(getString("pwd"))) {
					throw new Exception("密码不正确");
				} else {
					if (!morgid.equals(getString("orgid"))) {
						throw new Exception("不是本门店工号");
					} else {
						if (getInt("status") == 0) {
							throw new Exception("当前账号被禁用");
						}
					}
				}
			}
			outresult = sucessMsgToJson0("ok", new String[] { "uid", uid, "uname", getString("username") });
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}

		return CUtil.formatJson(outresult.trim());
	}

	// 收银营业员修改密码
	public String YmpCashierChangePwd() {
		String result = "";
		try {
			String oldpwd = __Request.getParameter("oldpwd").toString();
			String newpwd = __Request.getParameter("newpwd").toString();

			getBaseParam();

			if (!CAppListener.getParam("md5").equals("0")) {
				oldpwd = CUtil.MD5(oldpwd);
				newpwd = CUtil.MD5(newpwd);
			}
			if (executeOracleProc("ChangeCashierPwd",
					new String[] { __Request.getParameter("userid").toString(), oldpwd, newpwd }, new int[] { 1, 1, 1 },
					null, 4) == 0) {
				result = sucessMsgToJson0("ok");
			} else {
				result = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			result = errorMsgToJson0(ex.getMessage());
		}
		return CUtil.formatJson(result);
	}

	// 管理人员修改密码
	public String YmpUserChangePwd() {
		String result = "";
		try {
			String oldpwd = __Request.getParameter("oldpwd").toString();
			String newpwd = __Request.getParameter("newpwd").toString();

			getBaseParam();

			if (!CAppListener.getParam("md5").equals("0")) {
				oldpwd = CUtil.MD5(oldpwd);
				newpwd = CUtil.MD5(newpwd);
			}
			if (executeOracleProc("ChangeManagerPwd",
					new String[] { __Request.getParameter("userid").toString(), oldpwd, newpwd }, new int[] { 1, 1, 1 },
					null, 4) == 0) {
				result = sucessMsgToJson0("ok");
			} else {
				result = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			result = errorMsgToJson0(ex.getMessage());
		}
		return CUtil.formatJson(result);
	}

	/* 获取单据key和id */
	public String YmpGetBillKeyAndId() {
		String result = "";
		try {
			getBaseParam();

			String billtype = __Request.getParameter("billtype").toString();

			if (executeOracleProc("GetBillKeyAndId", new String[] { morgid, mmposid }, new int[] { 1, 1 },
					new int[] { 2, 1 }, 4) == 0) {

				result = sucessMsgToJson0("ok",
						new String[] { "billkey", __procReturn[0], "billid", __procReturn[1], "billtype", billtype });
			} else {
				result = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}
		P(result);
		return CUtil.formatJson(result);
	}

	/* 获取商场信息 */
	public String YmpGetMarketInfo(String orgid) {
		String result = "";
		try {
			if (CInitParam.erpServerUrl.equals("")) {
				result = new CGyerp().pos_querylc(orgid);
			} else {
				result = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/icommon/getmarketinfo.jsp",
						new String[] { "orgid", orgid });
			}
		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}

		return CUtil.formatJson(result);
	}

	public String YmpGetMarketInfo() {
		return YmpGetMarketInfo(__Request.getParameter("orgid").toString());
	}
	
	/* 获取专柜信息 */
	public String YmpGetCounterInfo(String storeid) {
		String result = "";
		try {
			if (CInitParam.erpServerUrl.equals("")) {
				result = new CGyerp().pos_queryzg(storeid);
			} else {
				result = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/icommon/getcounterinfo.jsp",
						new String[] { "storeid", storeid });
			}
		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}

		return CUtil.formatJson(result);
	}

	public String YmpGetCounterInfo() {
		return YmpGetCounterInfo(__Request.getParameter("storeid").toString());
	}
	/* 获取专柜商品信息 */
	public String YmpGetGoodsInfo(String counterid) {
		String result = "";
		try {
			if (CInitParam.erpServerUrl.equals("")) {
				result = new CGyerp().pos_querygoods(counterid);
			} else {
				result = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/icommon/getgoodsinfo.jsp",
						new String[] { "counterid", counterid });
			}
		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}

		return CUtil.formatJson(result);
	}

	public String YmpGetGoodsInfo() {
		return YmpGetGoodsInfo(__Request.getParameter("counterid").toString());
	}
	
	/* 获取会员信息 */
	public String YmpGetVipInfo() {
		String result = "";
		try {
			getBaseParam();

			String vipid = __Request.getParameter("vipid").toString();
			if (CInitParam.erpServerUrl.equals("")) {
				result = new CGyvip().card_findcust(vipid, "");
			} else {
				result = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/icommon/getvipinfo.jsp",
						new String[] { "vipid", __Request.getParameter("vipid").toString() });
			}

		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}

		return CUtil.formatJson(result);
	}

	/* 获取商品信息 */
	public String YmpGetProductInfo() {
		String result = "";
		try {
			getBaseParam();

			String code = __Request.getParameter("code").toString();
			String vipid = __Request.getParameter("vipid").toString();
			String stime = __Request.getParameter("stime").toString();
			
			if (CInitParam.erpServerUrl.equals("")) {
				result = new CGyerp().pos_findgoods(merpcashid, morgid, code, mcounterid, vipid, stime);
			} else {
				result = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/icommon/findgoodsinfo.jsp",
						new String[] { "erpcashid", merpcashid, "mposid", mmposid, "orgid", morgid, "counterid",
								mcounterid, "code", code, "vipid", vipid, "stime", stime });
			}

		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}
		P(result);
		return CUtil.formatJson(result);
	}

	/* 获取积分卡信息 */
	public String YmpGetSCardInfo() {
		String result = "";
		try {
			getBaseParam();

			String type = __Request.getParameter("type").toString();
			String kh = __Request.getParameter("kh").toString();

			if (CInitParam.erpServerUrl.equals("")) {
				result = new CGyvip().card_findmzk(type, kh, mcounterid, "");
			} else {
				result = CHttpService.sendPostFmt(CInitParam.erpServerUrl + "/bin/icommon/getscardinfo.jsp",
						new String[] { "type", type, "kh", kh, "counterid", mcounterid });
			}

		} catch (Exception ex) {
			P(ex.getMessage());
			result = errorMsgToJson0(ex.getMessage());
		}

		return CUtil.formatJson(result);
	}
}
