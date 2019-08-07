package bluec.keg;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import bluec.base.CAppListener;
import bluec.base.CHttpService;
import bluec.base.CJson;
import bluec.base.CMap;
import bluec.base.CMapPoint;
import bluec.base.CUtil;

public class CKegss extends CJson {
	public CKegss() {

	}

	/**
	 * 
	 */
	public String updateInfo(HttpServletRequest request) {
		try {
			if (executeMsSqlProc("Kss_UpdateImg", new String[] { request.getParameter("kh_id").toString(),
					request.getParameter("yyzz_path").toString() }) == 0) {
				_errorInf = sucessMsgToJson0("success");
			} else {
				_errorInf = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		}
		// P(_errorInf);
		return _errorInf;
	}

	/**
	 * 保存订单询价信息
	 * 
	 * @return
	 */
	public String orderAskSave(HttpServletRequest request) {
		try {
			JSONObject obj = dataFromRequestToJson(request);
			JSONArray gArray = obj.getJSONArray("order_res");

			for (int i = 0; i < gArray.length(); i++) {
				gArray.getJSONObject(i).put("noid", i + 1);
				gArray.getJSONObject(i).put("res_content",
						gArray.getJSONObject(i).getString("res_content").replaceAll("https", "http"));
			}

			dataAllSqls = new String[2 + gArray.length()];

			int index = createInsertValues("kss_orderaskinfo", obj, 0,
					new String[] { "lastuptime", "isread", "status", "opstatus" },
					new String[] { "getdate()", "0", "0", "0" });
			index = createInsertValues("kss_orderaskinfo_d", gArray, index, gArray.length());

			dataAllSqls[dataAllSqls.length - 1] = String.format("exec kss_AssignUser '%s'",
					obj.getString("order_number"));

			P(obj.toString());
			for (int i = 0; i < dataAllSqls.length; i++) {
				P(dataAllSqls[i]);
			}

			int n = executeSqls(dataAllSqls);
			if (n == 0) {
				_errorInf = sucessMsgToJson0("success");
			} else {
				_errorInf = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		}
		// P(_errorInf);
		return _errorInf;
	}

	public String addOrderCarDetails(HttpServletRequest request) {
		try {
			JSONObject kssobj = dataFromRequestToJson(request);
			Boolean b = true;
			if (kssobj.getInt("cxnum") <= 0) { // 手动录入的车型
				if (saveJsonObjectToDb(
						"BlueCQp3Bak..BASE_CX_MXBINP@SNAME,CXBM,PPNAME,CXNAME,FDJXH,PL,QDFS,BSQLX,ZCZZLX,GL,SCNF,NK,PZDJ,"
								+ "CJH,CJH0,CJH1,CJH2,CJH3,CJH4,CJH5,CJH6,CJH7,CJH8,CJH9,CJH10,CJH11,CJH12,CJH13,CJH14,CJH15,CJH16,CJH17",
						kssobj.getJSONObject("data"))) {
					_errorInf = sucessMsgToJson0("ok");
				} else {
					b = false;
					_errorInf = errorMsgToJson0(_errorInf);
				}
			}
			if (b && !kssobj.getString("order_number").equals("")) {
				executeUpdate(
						"update kss_orderaskinfo set vin_number='%s',brand='%s',brand_url='%s',"
								+ "engine_number='%s',car_details='%s',pai_liang='%s',nian_fen='%s' where order_number='%s'",
						new String[] { kssobj.getString("vin_number"), kssobj.getString("brand"),
								kssobj.getString("brand_url"), kssobj.getString("engine_number"),
								kssobj.getString("car_details"), kssobj.getString("pai_liang"),
								kssobj.getString("nian_fen"), kssobj.getString("order_number") });
				_errorInf = CHttpService.sendPostJsonFmtEx(
						CAppListener.getParam("kssurl") + "/userOrder/addOrderCarDetails", kssobj.toString());

				JSONObject robj = new JSONObject(_errorInf);
				if (!robj.has("sub_code")) {
					if (robj.getInt("code") == 200) {
						_errorInf = sucessMsgToJson0("success");
					} else {
						_errorInf = errorMsgToJson0(robj.getString("msg"));
					}
				}
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
			P(_errorInf);
		} finally {
			closeConn();
		}
		return _errorInf;

	}

	/**
	 * 订单报价信息推送
	 * 
	 * @return
	 */
	public String orderPriceTo(HttpServletRequest request) {
		try {
			JSONObject obj = dataFromRequestToJson(request);
			JSONArray gArray = obj.getJSONArray("detail");

			String idkey = obj.getString("id_key");

			dataAllSqls = new String[4 + gArray.length()];

			dataAllSqls[0] = String.format("DELETE FROM XS_YHD WHERE ID_KEY=%s", idkey);
			dataAllSqls[1] = String.format("DELETE FROM XS_YHDD WHERE ID_KEY=%s", idkey);

			int index = createInsertValues("XS_YHD", obj, 2);
			index = createInsertValues("XS_YHDD", gArray, index, gArray.length());

			dataAllSqls[index] = String.format(
					"update kss_orderaskinfo set status=2,opstatus=0,id_key=%s where order_number='%s'", idkey,
					obj.getString("ydjh"));
			for (int i = 0; i < dataAllSqls.length; i++)
				P(dataAllSqls[i]);
			int n = executeSqls(dataAllSqls);
			if (n == 0) {
				_errorInf = sucessMsgToJson0("success");
				// 推送到小程序

				JSONObject kssobj = new JSONObject();
				JSONArray kssdetail = new JSONArray();

				kssobj.put("order_id", obj.getInt("id_key"));
				kssobj.put("order_number", obj.getString("ydjh"));
				kssobj.put("erporder_number", obj.getString("dd_key"));
				kssobj.put("total_price", obj.getDouble("sje"));
				kssobj.put("receive_user_phone", obj.getString("sh_dh"));
				kssobj.put("self_company_id", obj.getString("kh_id"));
				kssobj.put("kh_id", obj.getString("kh_id"));
				kssobj.put("sh_dh", obj.getString("sh_dh"));
				kssobj.put("create_user_phone", obj.getString("sh_dh"));
				kssobj.put("receive_user_name", obj.getString("sh_lxr"));
				kssobj.put("receive_user_phone", obj.getString("sh_dh"));
				kssobj.put("express_delivery_name", "");
				kssobj.put("express_delivery_phone", "");
				kssobj.put("transport_company", "");
				kssobj.put("transport_number", "");
				kssobj.put("store_code", obj.getString("kf_code"));
				kssobj.put("store_name", obj.getString("kf_name"));
				kssobj.put("store_addr", obj.getString("kf_addr"));
				kssobj.put("store_tel", obj.getString("kf_tel"));

				for (int i = 0; i < gArray.length(); i++) {
					JSONObject o = new JSONObject();
					JSONObject s = gArray.getJSONObject(i);
					o.put("product_id", s.getString("cp_id"));
					o.put("noid", s.getString("f_key"));
					o.put("product_code", s.getString("cp_code"));
					o.put("product_code2", s.getString("cp_code2"));
					o.put("product_code3", s.getString("cp_code3"));
					o.put("product_format", s.getString("gg_name"));
					o.put("product_brand", s.getString("pp_name"));
					o.put("product_name", s.getString("cdefine1"));
					o.put("product_count", s.getDouble("sl"));
					o.put("product_year", s.getString("slshow"));
					o.put("product_engine", "");
					o.put("product_chaiss", "");
					o.put("single_price", s.getDouble("sj"));
					o.put("total_price", s.getDouble("sje"));
					o.put("discount", "0");
					kssdetail.put(o);
				}
				kssobj.put("orderEnsureList", kssdetail);
				P(CUtil.formatJson(kssobj.toString()));
				_errorInf = CHttpService.sendPostJsonFmtEx(CAppListener.getParam("kssurl") + "/userOrder/getOfferLists",
						kssobj.toString());

				P(_errorInf);
				JSONObject robj = new JSONObject(_errorInf);
				if (!robj.has("sub_code")) {
					if (robj.getInt("code") == 200) {
						_errorInf = sucessMsgToJson0("success");
					} else {
						_errorInf = errorMsgToJson0(robj.getString("msg"));
					}
				}

			} else {
				_errorInf = errorMsgToJson0(_errorInf);
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
			P(_errorInf);
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/**
	 * 订单接单
	 */
	public String orderAskReceive(HttpServletRequest request) {
		try {
			executeUpdateEx(String.format("update kss_orderaskinfo set status=1,opstatus=0 where order_number='%s'",
					request.getParameter("orderno").toString()));

			JSONObject kssobj = new JSONObject();
			kssobj.put("order_number", request.getParameter("orderno").toString());
			_errorInf = CHttpService.sendPostJsonFmtEx(CAppListener.getParam("kssurl") + "/userOrder/waitingForOrder",
					kssobj.toString());
			JSONObject robj = new JSONObject(_errorInf);
			if (!robj.has("sub_code")) {
				if (robj.getInt("code") == 200) {
					_errorInf = sucessMsgToJson0("success");
				} else {
					_errorInf = errorMsgToJson0(robj.getString("msg"));
				}
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		}
		return _errorInf;
	}

	/**
	 * 订单确认
	 * 
	 * @param request
	 * @return
	 */
	public String orderConfirm(HttpServletRequest request) {
		try {
			JSONObject obj = dataFromRequestToJson(request);
			JSONArray gArray = obj.getJSONArray("products");
			P(CUtil.formatJson(obj.toString()));
			String id_key = "0";
			String orderno = obj.getString("order_number");
			String couponje = (obj.has("discount_price")) ? obj.getString("discount_price") : "0";
			String couponinfo = (obj.has("lottery_desc")) ? obj.getString("lottery_desc") : "";

			executeQuery3("select id_key from xs_yhd where ydjh='%s'", orderno);
			if (!next())
				throw new Exception("找不到订单:" + orderno);
			id_key = getString("id_key");

			dataAllSqls = new String[gArray.length() + 4];

			dataAllSqls[0] = String.format("update xs_yhdd set sl=0,sje=0,ssje=0 where id_key=%s", id_key);

			for (int i = 0; i < gArray.length(); i++) {
				JSONObject s = gArray.getJSONObject(i);
				dataAllSqls[i + 1] = String.format("update xs_yhdd set sl=%s where id_key=%s and f_key=%s",
						s.getString("quantity"), id_key, s.getString("noid"));
			}

			dataAllSqls[gArray.length() + 1] = String.format("update xs_yhdd set sje=sl*sj,ssje=sl*sj where id_key=%s",
					id_key);

			dataAllSqls[gArray.length() + 2] = String.format(
					"update xs_yhd set dd_state=2,couponje=%s,couponinfo='%s' where id_key=%s", couponje, couponinfo,
					id_key);

			dataAllSqls[gArray.length() + 3] = String.format(
					"update kss_orderaskinfo set status=3,opstatus=0,isread=0,lastuptime=getdate() where order_number='%s'",
					orderno);

			int n = executeSqls(dataAllSqls);
			if (n == 0) {
				_errorInf = sucessMsgToJson0("success");
				// 推送到小程序
			} else {
				_errorInf = errorMsgToJson0(_errorInf);
			}
			P(_errorInf);
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
			P(_errorInf);
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/**
	 * 订单发货 getOfferLists
	 * 
	 * @return
	 */
	public String orderSendTo(HttpServletRequest request) {
		try {
			JSONObject obj = dataFromRequestToJson(request);
			String id_key = obj.getString("id_key");

			executeQuery3("select a.id_key,a.dd_key,a.ydjh,a.ssje,b.kh_id,a.sh_dh,a.sh_lxr,a.sh_dz,b.openid "
					+ "from BlueCQp3..xs_pfd a,BlueCQp3..yx_kh b " + "where a.id_key=%s and b.kh_code=a.kh_code",
					id_key);
			if (!next()) {
				throw new Exception("找不到单据：\n " + obj.getString("id_key"));
			}

			String erporder_number = getString("dd_key");
			String username = obj.getString("username");
			String tel = obj.getString("tel");
			String company = obj.getString("company");
			String number = obj.getString("fydh");
			String ono = getString("ydjh");
			String hybno = obj.getString("hybno");
			P(obj.getString("price"));
			if (getString("OPENID").equals("")) { // 非小程序客户
				if (executeMsSqlProc("Kss_billReceipt",
						new String[] { obj.getString("id_key"), erporder_number, ono, obj.getString("kf_code"), hybno,
								obj.getString("num"), obj.getString("price"), obj.getString("amount"),
								obj.getString("weight"), obj.getString("length"), obj.getString("width"),
								obj.getString("height"), obj.getString("timelen"), obj.getString("shfs_no"),
								obj.getString("shfs_name"), obj.getString("fysj"), obj.getString("fydh"),
								obj.getString("status"), obj.getString("lr_id"), obj.getString("lr_name") }) == 0) {

					_errorInf = sucessMsgToJson0("success");
				} else {
					_errorInf = errorMsgToJson0(_errorInf);
				}
			} else {
				JSONObject kssobj = new JSONObject();
				JSONArray kssdetail = new JSONArray();

				kssobj.put("order_id", getInt("id_key"));
				kssobj.put("order_number", getString("ydjh"));
				kssobj.put("erporder_number", getString("dd_key"));
				kssobj.put("total_price", getFloat("ssje"));
				kssobj.put("self_company_id", getString("kh_id"));
				kssobj.put("kh_id", getString("kh_id"));
				kssobj.put("sh_dh", getString("sh_dh"));
				kssobj.put("create_user_phone", getString("sh_dh"));
				kssobj.put("receive_user_name", getString("sh_lxr"));
				kssobj.put("receive_user_phone", getString("sh_dh"));
				kssobj.put("receive_address", getString("sh_dz"));
				kssobj.put("express_delivery_name", username);
				kssobj.put("express_delivery_phone", tel);
				kssobj.put("transport_company", company);
				kssobj.put("transport_number", number);
				kssobj.put("store_code", obj.getString("kf_code"));
				kssobj.put("store_name", obj.getString("kf_name"));
				kssobj.put("store_addr", obj.getString("kfaddr"));
				kssobj.put("store_tel", obj.getString("kftel"));

				if (!ono.equals("")) {
					executeQuery3(
							"select receive_locationx,receive_locationy,receive_name,receive_phone,receive_address "
									+ " from kss_orderaskinfo where order_number='%s'",
							ono);
					if (next()) {
						CMapPoint mp = CMap.bMapTransQQMap(getString("receive_locationx"),
								getString("receive_locationy"));
						kssobj.put("receive_locationx", mp.lon);
						kssobj.put("receive_locationy", mp.lat);
						kssobj.put("receive_user_phone", getString("receive_phone"));
						kssobj.put("receive_address", getString("receive_address"));
					}
				}

				executeQuery3("select a.id_key,a.f_key,a.cp_id,a.cdefine1,a.sl,a.slshow,a.sj,a.sje,"
						+ "b.cp_code,b.cp_code2,b.cp_code3,b.gg_name,c.pp_name "
						+ "from BlueCQp3..xs_pfdd a,BlueCQp3..yx_cpml b,BlueCQp3..pub_ppb c "
						+ "where a.id_key=%s and b.cp_id=a.cp_id and c.pp_code=b.pp_code", id_key);
				while (next()) {
					JSONObject o = new JSONObject();
					o.put("product_id", getString("cp_id"));
					o.put("noid", getString("f_key"));
					o.put("product_code", getString("cp_code"));
					o.put("product_code2", getString("cp_code2"));
					o.put("product_code3", getString("cp_code3"));
					o.put("product_format", getString("gg_name"));
					o.put("product_brand", getString("pp_name"));
					o.put("product_name", getString("cdefine1"));
					o.put("product_count", getFloat("sl"));
					o.put("product_year", getString("slshow"));
					o.put("product_engine", "");
					o.put("product_chaiss", "");
					o.put("single_price", getFloat("sj"));
					o.put("total_price", getFloat("sje"));
					o.put("discount", "0");
					kssdetail.put(o);
				}
				kssobj.put("orderEnsureList", kssdetail);
				P(CUtil.formatJson(kssobj.toString()));
				_errorInf = CHttpService.sendPostJsonFmtEx(CAppListener.getParam("kssurl") + "/userOrder/getOfferPay",
						kssobj.toString());

				P(_errorInf);
				JSONObject robj = new JSONObject(_errorInf);
				if (!robj.has("sub_code")) {
					if (robj.getInt("code") == 200) {
						company = sucessMsgToJson0("success");
						executeMsSqlProc("Kss_billReceipt",
								new String[] { obj.getString("id_key"), erporder_number, ono, obj.getString("kf_code"),
										hybno, obj.getString("num"), obj.getString("price"), obj.getString("amount"),
										obj.getString("weight"), obj.getString("length"), obj.getString("width"),
										obj.getString("height"), obj.getString("timelen"), obj.getString("shfs_no"),
										obj.getString("shfs_name"), obj.getString("fysj"), obj.getString("fydh"),
										obj.getString("status"), obj.getString("lr_id"), obj.getString("lr_name") });
						_errorInf = company;
					} else {
						_errorInf = errorMsgToJson0(robj.getString("msg"));
					}
				}
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
			P("orderSendTo:" + _errorInf);
		}
		return _errorInf;
	}

	public String orderCarDetails(HttpServletRequest request) {
		try {
			// String id_key = request.getParameter("id_key").toString();
			String ono = request.getParameter("ono").toString();
			String note = request.getParameter("note").toString();
			String status = request.getParameter("status").toString();
			String op = request.getParameter("op").toString();
			String emid = request.getParameter("emid").toString();

			if (executeMsSqlProc("kss_AssignOrder", new String[] { ono, status, op, emid }) != 0) {
				_errorInf = "<xml><errors><error>" + _errorInf + "</error></errors></xml>";
			} else {

				executeUpdate("update kss_orderaskinfo set status=%s,isread=1 where order_number='%s'",
						new String[] { op, ono });
				JSONObject kssobj = new JSONObject();

				kssobj.put("ono", ono);
				kssobj.put("msg", note);
				kssobj.put("op", op);

				kssobj.put("order_number", request.getParameter("orderno").toString());
				_errorInf = CHttpService.sendPostJsonFmtEx(
						CAppListener.getParam("kssurl") + "/userOrder/waitingForOrder", kssobj.toString());

				_errorInf = "<xml><ds><d>ok</d></ds></xml>";
			}
		} catch (Exception ex) {
			_errorInf = "<xml><errors><error>" + ex.getMessage() + "</error></errors></xml>";
		}
		return _errorInf;
	}

	/**
	 * 订单通知
	 * 
	 * @return
	 */
	public String orderNotify(HttpServletRequest request) {
		try {
			// String id_key = request.getParameter("id_key").toString();
			String kfcode = request.getParameter("kfcode").toString();
			String ono = request.getParameter("ono").toString();
			String note = request.getParameter("note").toString();
			String status = request.getParameter("status").toString();
			String op = request.getParameter("op").toString();
			String emid = request.getParameter("emid").toString();

			if (executeMsSqlProc("kss_AssignOrder", new String[] { kfcode, ono, status, op, emid }) != 0) {
				_errorInf = "<xml><errors><error>" + _errorInf + "</error></errors></xml>";
			} else {

				JSONObject kssobj = new JSONObject();

				kssobj.put("ono", ono);
				kssobj.put("msg", note);
				kssobj.put("op", Integer.parseInt(status) + Integer.parseInt(op));

				kssobj.put("order_number", ono);
				_errorInf = CHttpService.sendPostJsonFmtEx(
						CAppListener.getParam("kssurl") + "/userOrder/waitingForOrder", kssobj.toString());
				P(kssobj.toString());
				P(_errorInf);
				/*
				 * 
				 * _errorInf =
				 * CHttpService.sendPostJsonFmtEx(CAppListener.getParam(
				 * "kssurl") + "/userOrder/getOfferPay", kssobj.toString());
				 * 
				 * JSONObject robj = new JSONObject(_errorInf); if
				 * (robj.getInt("code") == 200) { _errorInf =
				 * sucessMsgToJson0("success"); } else { _errorInf =
				 * errorMsgToJson0(robj.getString("msg")); }
				 */
				_errorInf = "<xml><ds><d>ok</d></ds></xml>";
			}
		} catch (Exception ex) {
			_errorInf = "<xml><errors><error>" + ex.getMessage() + "</error></errors></xml>";
		}
		return _errorInf;
	}

	/**
	 * 根据坐标获取最近的库房
	 */
	public String getKfInfoByCoor(HttpServletRequest request) {
		try {
			String lon = request.getParameter("lon").toString();
			String lat = request.getParameter("lat").toString();

			JSONArray[] jsonArrays = new JSONArray[] { new JSONArray(), new JSONArray() };
			// = new JSONArray();

			executeQuery3(
					"select top 5 dr=SQRT(SQUARE(ISNULL(lon,0)-%s)+SQUARE(ISNULL(lat,0)-%s)), "
							+ "KF_CODE,KF_NAME,KF_XXDZ,KF_LXDH,lon,lat from BlueCQp3..pub_kf order by dr",
					new String[] { lon, lat });
			while (next()) {
				JSONObject o = new JSONObject();
				o.put("kf_code", getString("KF_CODE"));
				o.put("kf_name", getString("KF_NAME"));
				o.put("kf_xxdz", getString("KF_XXDZ"));
				o.put("kf_lxdh", getString("KF_LXDH"));
				o.put("lon", getString("lon"));
				o.put("lat", getString("lat"));
				jsonArrays[0].put(o);
			}

			executeQuery3(
					"select top 5 dr=SQRT(SQUARE(ISNULL(lon,0)-%s)+SQUARE(ISNULL(lat,0)-%s)), "
							+ "KF_CODE,KF_NAME,KF_XXDZ,KF_LXDH,lon,lat from BlueCQp3..pub_kf order by kf_code",
					new String[] { lon, lat });
			while (next()) {
				JSONObject o = new JSONObject();
				o.put("kf_code", getString("KF_CODE"));
				o.put("kf_name", getString("KF_NAME"));
				o.put("kf_xxdz", getString("KF_XXDZ"));
				o.put("kf_lxdh", getString("KF_LXDH"));
				o.put("lon", getString("lon"));
				o.put("lat", getString("lat"));
				jsonArrays[1].put(o);
			}

			_errorInf = sucessMsgToJson0("ok", new String[] { "data", "data1" }, jsonArrays);
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		}
		P(_errorInf);
		return _errorInf;
	}

	/**
	 * 上传并执行SQL语句
	 */

	public String sqlToExec(HttpServletRequest request) {
		Boolean b = false;
		try {
			JSONObject obj = dataFromRequestToJson(request);

			String isql = obj.getString("isql");
			String sql = obj.getString("sql");
			String csql = obj.getString("csql");
			if (!isql.equals("")) {
				executeQuery(isql);
				b = next();
			}

			if (!b) {
				if (executeUpdate(sql) >= 0) {
					if (!csql.equals("")) {
						executeQuery(csql);
						b = next();
					} else {
						b = true;
					}
				}
			}
			if (b) {
				_errorInf = sucessMsgToJson0("success");
			} else {
				_errorInf = errorMsgToJson0("fail");
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		}
		P(_errorInf);
		return _errorInf;
	}

	/**
	 * 订单idkey解析
	 */
	public String billkeyResolve(HttpServletRequest request) {
		try {
			JSONObject inJson = dataFromRequestToJson(request);

			String orderidstr = CUtil.GetPassWord(inJson.getString("orderidstr"));

			if (orderidstr.length() > 4 && orderidstr.subSequence(0, 3).equals("kss")) {
				String order_id = orderidstr.substring(3);
				executeQuery3("select dd_key,ydjh,kf_code,kh_code,kh_id,ywy_no from BlueCQp3..xs_pfd where id_key=%s",
						order_id);
				if (next()) {
					_errorInf = sucessMsgToJson0("ok",
							new String[] { "order_id", order_id, "erporder_number", getString("dd_key"), "ordernumber",
									getString("ydjh"), "kf_code", getString("kf_code"), "kh_code", getString("kh_code"),
									"kh_id", getString("kh_id") });
					if (inJson.has("khid")) { // 校验是否有查看订单的权限
						if (getInt("kh_id") != inJson.getInt("khid")
								&& !getString("ywy_no").equals(inJson.get("usercode"))) {
							_errorInf = errorMsgToJson0("无权查看该订单");
						}
					}
				} else {
					_errorInf = errorMsgToJson0("订单不存在");
				}
			} else {
				orderidstr = inJson.getString("orderidstr");
				if (!orderidstr.equals("")) {
					if (!inJson.has("type") || inJson.getInt("type") == 0) {
						executeQuery3(
								"select id_key,dd_key,ydjh,kf_code,kh_code,kh_id,ywy_no from BlueCQp3..xs_pfd where dd_key='%s'",
								orderidstr);
					} else {
						executeQuery3(
								"select id_key,dd_key,ydjh,kf_code,kh_code,kh_id,ywy_no from BlueCQp3..xs_pfd where id_key=%s",
								orderidstr);
					}
					if (next()) {
						_errorInf = sucessMsgToJson0("ok",
								new String[] { "order_id", getString("id_key"), "erporder_number", getString("dd_key"),
										"ordernumber", getString("ydjh"), "kf_code", getString("kf_code"), "kh_code",
										getString("kh_code"), "kh_id", getString("kh_id") });
						if (inJson.has("khid")) { // 校验是否有查看订单的权限
							if (getInt("kh_id") != inJson.getInt("khid")
									&& !getString("ywy_no").equals(inJson.get("usercode"))) {
								_errorInf = errorMsgToJson0("无权查看该订单");
							}
						}
					} else {
						_errorInf = errorMsgToJson0("订单不存在");
					}
				} else {
					_errorInf = errorMsgToJson0("数据无效");
				}
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(_errorInf);
		return _errorInf;
	}

	/**
	 * 检查销单状态
	 */
	public String billDespatchStatus(HttpServletRequest request) {
		try {
			Boolean b = true;
			String id_key = request.getParameter("order_id").toString();

			executeQuery3(
					"select a.id_key,status=a.status,statusname=b.sname from BlueCQp3..xs_pfd_wlxx a,BlueCQp3..base_dpstatus b "
							+ "where a.id_key = %s and a.status=b.sid",
					id_key);
			if (!next()) {
				executeQuery3("select status=0,statusname='待发单发货' from BlueCQp3..xs_pfd a where a.id_key=%s", id_key);
				b = next();
			}
			if (b) {
				_errorInf = sucessMsgToJson0("ok",
						new String[] { "status", getString("status"), "statusname", getString("statusname") });
			} else {
				_errorInf = sucessMsgToJson0("ok", new String[] { "status", "99", "statusname", "订单未存在" });
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/**
	 * 发单
	 */
	public String billReceipt(HttpServletRequest request) {
		try {
			String order_number = "", erporder_number = "";
			String khCode = "";
			String hybno = "";
			JSONObject inpobj = dataFromRequestToJson(request);

			executeQuery3("select * from BlueCQp3..xs_pfd where id_key=%s", inpobj.getString("id_key"));
			if (!next()) {
				throw new Exception("找不到单据：\n " + inpobj.getString("id_key"));
			}

			JSONObject kssobj = new JSONObject();
			kssobj.put("order_id", getInt("id_key"));

			order_number = getString("ydjh");
			erporder_number = getString("dd_key");
			khCode = getString("kh_code");

			kssobj.put("order_number", order_number);
			kssobj.put("erporder_number", erporder_number);
			kssobj.put("company_id", getString("kf_code"));
			kssobj.put("company_name", getString("kf_name"));
			kssobj.put("company_tel", getString("kftel"));
			kssobj.put("company_user", getString("kflxr"));
			kssobj.put("create_date", CUtil.getOrigTime());
			kssobj.put("update_date", CUtil.getOrigTime());
			kssobj.put("province", "重庆市");
			kssobj.put("city", "重庆市");
			kssobj.put("area", "九龙坡区");
			kssobj.put("address_details", getString("kfaddr"));

			kssobj.put("transport_no", "");
			kssobj.put("transport_company", "");
			kssobj.put("transport_addr", "");
			kssobj.put("transport_contact", "");
			kssobj.put("transport_tel", "");
			kssobj.put("transport_lon", "");
			kssobj.put("transport_lat", "");

			kssobj.put("delivery_type", 0);
			kssobj.put("delivery_price", Double.parseDouble(inpobj.getString("price")));
			kssobj.put("delivery_time", Integer.parseInt(inpobj.getString("timelen")));

			kssobj.put("delivery_num", Integer.parseInt(inpobj.getString("num")));
			kssobj.put("delivery_amount", Double.parseDouble(inpobj.getString("amount")));
			kssobj.put("delivery_weight", Double.parseDouble(inpobj.getString("weight")));
			kssobj.put("delivery_length", Integer.parseInt(inpobj.getString("length")));
			kssobj.put("delivery_width", Integer.parseInt(inpobj.getString("width")));
			kssobj.put("delivery_height", Integer.parseInt(inpobj.getString("height")));

			kssobj.put("lon", "106.463513");
			kssobj.put("lat", "29.52622");

			kssobj.put("receive_locationx", "0");
			kssobj.put("receive_locationy", "0");
			kssobj.put("receive_lon", "0");
			kssobj.put("receive_lat", "0");
			kssobj.put("receive_details_address", getString("sh_dz"));
			kssobj.put("receive_address", getString("sh_dz"));
			kssobj.put("receive_name", getString("sh_lxr"));
			kssobj.put("receive_phone", getString("sh_dh"));
			kssobj.put("receive_company_id", getString("kh_id"));
			kssobj.put("receive_company_name", getString("kh_name"));
			kssobj.put("receive_company_phone", getString("sh_dh"));

			executeQuery3("select province,city,area,lon,lat,skf_xxdz,skf_lxdh,skf_lxr,slon,slat " + ""
					+ " from BlueCQp3..pub_kf where kf_code=%s", getString("kf_code"));
			if (next()) {
				kssobj.put("province", getString("province"));
				kssobj.put("city", getString("city"));
				kssobj.put("area", getString("area"));

				CMapPoint mp = CMap.bMapTransQQMap(getString("lon"), getString("lat"));

				kssobj.put("lon", mp.lon);
				kssobj.put("lat", mp.lat);

				if (!getString("skf_xxdz").equals(""))
					kssobj.put("storeaddress", getString("skf_xxdz"));
				else
					kssobj.put("storeaddress", kssobj.getString("address_details"));

				if (!getString("skf_lxdh").equals(""))
					kssobj.put("storetel", getString("skf_lxdh"));
				else
					kssobj.put("storetel", kssobj.getString("company_tel"));

				if (!getString("skf_lxr").equals(""))
					kssobj.put("storecontact", getString("skf_lxr"));
				else
					kssobj.put("storecontact", kssobj.getString("company_user"));

				if (!getString("slon").equals("")) {
					mp = CMap.bMapTransQQMap(getString("lon"), getString("lat"));
					kssobj.put("storelon", mp.lon);
					kssobj.put("storelat", mp.lat);
				} else {
					kssobj.put("storelon", kssobj.getString("lon"));
					kssobj.put("storelat", kssobj.getString("lat"));
				}

			}

			executeQuery3("select lon,lat from BlueCQp3..yx_kh where kh_code='%s'", khCode);

			if (next() && !getString("lon").equals("") && !getString("lat").equals("")) {
				CMapPoint mp = CMap.bMapTransQQMap(getString("lon"), getString("lat"));
				kssobj.put("receive_locationx", mp.lon);
				kssobj.put("receive_locationy", mp.lat);
				kssobj.put("receive_lon", getString("lon"));
				kssobj.put("receive_lat", getString("lat"));
			}

			if (!order_number.equals("")) {
				executeQuery3("select receive_locationx,receive_locationy,receive_name,receive_phone,receive_address "
						+ " from kss_orderaskinfo where order_number='%s'", order_number);
				if (next()) {
					CMapPoint mp = CMap.bMapTransQQMap(getString("receive_locationx"), getString("receive_locationy"));
					kssobj.put("receive_locationx", mp.lon);
					kssobj.put("receive_locationy", mp.lat);
					kssobj.put("receive_lon", getString("receive_locationx"));
					kssobj.put("receive_lat", getString("receive_locationy"));
					kssobj.put("receive_phone", getString("receive_phone"));
					kssobj.put("receive_address", getString("receive_address"));
				}
			}

			if (inpobj.has("hybno") && !inpobj.getString("hybno").equals("")) {
				hybno = inpobj.getString("hybno");
				executeQuery3(
						"select hybno,hybname,hybaddr,hyblxr,hyblxdh,lon,lat from BlueCQp3..pub_hyb where hybno='%s'",
						hybno);
				if (next()) {
					kssobj.put("transport_no", getString("hybno"));
					kssobj.put("transport_company", getString("hybname"));
					kssobj.put("transport_addr", getString("hybaddr"));
					kssobj.put("transport_contact", getString("hyblxr"));
					kssobj.put("transport_tel", getString("hyblxdh"));
					kssobj.put("transport_lon", getString("lon"));
					kssobj.put("transport_lat", getString("lat"));
				}
			}

			JSONArray kssdetail = new JSONArray();

			executeQuery3(
					"select a.id_key,a.f_key,a.cp_id,a.cdefine1,a.sl,a.slshow,a.sj,a.sje,"
							+ "b.cp_code,b.cp_code2,b.cp_code3,b.gg_name,c.pp_name "
							+ "from BlueCQp3..xs_pfdd a,BlueCQp3..yx_cpml b,BlueCQp3..pub_ppb c "
							+ "where a.id_key=%s and b.cp_id=a.cp_id and c.pp_code=b.pp_code",
					inpobj.getString("id_key"));
			while (next()) {
				JSONObject o = new JSONObject();
				o.put("product_id", getString("cp_id"));
				o.put("noid", getString("f_key"));
				o.put("product_code", getString("cp_code"));
				o.put("product_code2", getString("cp_code2"));
				o.put("product_code3", getString("cp_code3"));
				o.put("product_format", getString("gg_name"));
				o.put("product_brand", getString("pp_name"));
				o.put("product_name", getString("cdefine1"));
				o.put("product_count", getFloat("sl"));
				o.put("product_year", getString("slshow"));
				o.put("product_engine", "");
				o.put("product_chaiss", "");
				o.put("single_price", getFloat("sj"));
				o.put("total_price", getFloat("sje"));
				o.put("discount", "0");
				kssdetail.put(o);
			}
			kssobj.put("orderEnsureList", kssdetail);
			P(CUtil.formatJson(kssobj.toString()));

			_errorInf = CHttpService.sendPostJsonFmtEx(CAppListener.getParam("kwlurl") + "/lwj/addOrder",
					kssobj.toString());

			JSONObject robj = new JSONObject(_errorInf);
			if (!robj.has("sub_code")) {
				if (robj.getInt("code") == 200) {
					if (executeMsSqlProc("Kss_billReceipt",
							new String[] { inpobj.getString("id_key"), erporder_number, order_number,
									inpobj.getString("kf_code"), hybno, inpobj.getString("num"),
									inpobj.getString("price"), inpobj.getString("amount"), inpobj.getString("weight"),
									inpobj.getString("length"), inpobj.getString("width"), inpobj.getString("height"),
									inpobj.getString("timelen"), inpobj.getString("shfs_no"),
									inpobj.getString("shfs_name"), inpobj.getString("fysj"), inpobj.getString("fydh"),
									inpobj.getString("status"), inpobj.getString("lr_id"),
									inpobj.getString("lr_name") }) == 0) {
						_errorInf = sucessMsgToJson0("ok");
					} else {
						_errorInf = errorMsgToJson0(_errorInf);
					}
				} else {
					if (robj.getInt("code") != 0) {
						_errorInf = errorMsgToJson0(robj.getString("msg"));
					} else {
						_errorInf = errorMsgToJson0(robj.getString("sub_msg"));
					}
				}
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}

		P("_errorInf=" + _errorInf);
		return _errorInf;
	}

	/**
	 * 抢单调用
	 */
	public String billReceive(HttpServletRequest request) {
		try {
			JSONObject inJson = dataFromRequestToJson(request);
			String keySql = (inJson.has("order_id") && !inJson.getString("order_id").equals(""))
					? String.format("a.id_key=%s", inJson.getString("order_id"))
					: String.format("a.dd_key='%s'", inJson.getString("erporder_number"));

			String orderkey = "";
			String usercode = inJson.getString("usercode");
			String username = inJson.getString("username");
			String mtel = inJson.getString("mtel");

			P(keySql);
			executeQuery3(
					"select a.id_key,a.status,statusname=b.sname from BlueCQp3..xs_pfd_wlxx a,BlueCQp3..base_dpstatus b "
							+ "where %s and b.sid=a.status",
					keySql);
			if (next()) {
				orderkey = getString("id_key");
				if (getInt("status") == 1) {
					updateBySqlWithParamInner(
							"update BlueCQp3..xs_pfd set dd_send=2 where id_key=%s \n" + "update BlueCQp3..xs_pfd_wlxx "
									+ "set shr_no='%s',shr_name='%s',shr_dh='%s',status=2,qdtime=getdate() where id_key=%s",
							new String[] { orderkey, usercode, username, mtel, orderkey });
					_errorInf = sucessMsgToJson0("ok");
				} else {
					_errorInf = errorMsgToJson0("不能抢单",
							new String[] { "status", getString("status"), "statusname", getString("statusname") });
				}
			} else {
				_errorInf = errorMsgToJson0("不能抢单", new String[] { "status", "99", "statusname", "账单不存在" });
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(_errorInf);
		return _errorInf;
	}

	/**
	 * 取货调用
	 */
	public String billPickup(HttpServletRequest request) {
		try {
			JSONObject inJson = dataFromRequestToJson(request);
			String keySql = (inJson.has("order_id") && !inJson.getString("order_id").equals(""))
					? String.format("a.id_key=%s", inJson.getString("order_id"))
					: String.format("a.dd_key='%s'", inJson.getString("erporder_number"));

			String orderkey = "";
			String usercode = inJson.getString("usercode");

			executeQuery3(
					"select a.id_key,a.status,statusname=b.sname,a.shr_no from BlueCQp3..xs_pfd_wlxx a,BlueCQp3..base_dpstatus b "
							+ "where %s and b.sid=a.status",
					keySql);
			if (next()) {
				orderkey = getString("id_key");
				if (getInt("status") == 2) {
					if (getString("shr_no").equals(usercode)) {
						updateBySqlWithParamInner("update BlueCQp3..xs_pfd set dd_send=2 where id_key=%s \n"
								+ "update BlueCQp3..xs_pfd_wlxx " + "set status=3,fytime=getdate() where id_key=%s",
								new String[] { orderkey, orderkey });
						_errorInf = sucessMsgToJson0("ok");
					} else {
						_errorInf = errorMsgToJson0(-2, "人员信息不匹配",
								new String[] { "status", getString("status"), "statusname", getString("statusname") });
					}
				} else {
					_errorInf = errorMsgToJson0("调用失败",
							new String[] { "status", getString("status"), "statusname", getString("statusname") });
				}
			} else {
				_errorInf = errorMsgToJson0("调用失败", new String[] { "status", "99", "statusname", "账单不存在" });
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(_errorInf);
		return _errorInf;
	}

	/**
	 * 送达调用
	 */
	public String billService(HttpServletRequest request) {
		try {
			JSONObject inJson = dataFromRequestToJson(request);
			String keySql = (inJson.has("order_id") && !inJson.getString("order_id").equals(""))
					? String.format("a.id_key=%s", inJson.getString("order_id"))
					: String.format("a.dd_key='%s'", inJson.getString("erporder_number"));

			String orderkey = "";
			// String usercode = inJson.getString("usercode");

			executeQuery3(
					"select a.id_key,a.status,statusname=b.sname from BlueCQp3..xs_pfd_wlxx a,BlueCQp3..base_dpstatus b "
							+ "where %s and b.sid=a.status",
					keySql);
			if (next()) {
				orderkey = getString("id_key");
				if (getInt("status") == 3) {
					updateBySqlWithParamInner(
							"update BlueCQp3..xs_pfd set dd_send=2 where id_key=%s \n" + "update BlueCQp3..xs_pfd_wlxx "
									+ "set status=4,sdtime=getdate() where id_key=%s",
							new String[] { orderkey, orderkey });
					_errorInf = sucessMsgToJson0("ok");
				} else {
					_errorInf = errorMsgToJson0("调用失败",
							new String[] { "status", getString("status"), "statusname", getString("statusname") });
				}
			} else {
				_errorInf = errorMsgToJson0("调用失败", new String[] { "status", "99", "statusname", "账单不存在" });
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(_errorInf);
		return _errorInf;
	}

	private void billDespatchCancel(String order_id, String erporder_number, String order_number, int opt)
			throws Exception {
		try {
			Boolean isOk = false;
			if (opt != -3) {
				JSONObject kssobj = new JSONObject();
				kssobj.put("order_id", order_id);
				kssobj.put("erporder_number", order_number);
				kssobj.put("order_number", order_number);
				_errorInf = CHttpService.sendPostJsonFmtEx(CAppListener.getParam("kwlurl") + "/lwj/cancelOrder",
						kssobj.toString());

				P(_errorInf);
				JSONObject robj = new JSONObject(_errorInf);
				if (!robj.has("sub_code")) {
					if (robj.getInt("code") == 200) {
						isOk = true;
					} else {
						_errorInf = errorMsgToJson0(robj.getString("msg"));
					}
				}
			} else {
				isOk = true;
			}
			if (isOk) {
				updateBySqlWithParamInner(
						"update BlueCQp3..xs_pfd set dd_send=%s where id_key=%s \n" + "update BlueCQp3..xs_pfd_wlxx "
								+ "set status=%s where id_key=%s",
						new String[] { opt + "", order_id, opt + "", order_id });
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * 送货取消，opt = -3..-1
	 */
	public String billDespatchCancel(HttpServletRequest request) {
		try {
			JSONObject inJson = dataFromRequestToJson(request);

			String keySql = (inJson.has("order_id") && !inJson.getString("order_id").equals(""))
					? String.format("a.id_key=%s", inJson.getString("order_id"))
					: String.format("a.dd_key='%s'", inJson.getString("erporder_number"));

			String orderkey = "";
			int opt = inJson.getInt("opt");

			if (opt >= -3 && opt <= -1) {
				executeQuery3(
						"select a.status,statusname=b.sname,a.id_key,a.dd_key,a.ydjh from BlueCQp3..xs_pfd_wlxx a,BlueCQp3..base_dpstatus b "
								+ "where %s and b.sid=a.status",
						keySql);
				if (next()) {
					orderkey = getString("id_key");
					if (getInt("status") == 2 || getInt("status") == 3) {
						billDespatchCancel(orderkey, getString("dd_key"), getString("ydjh"), opt);
						_errorInf = sucessMsgToJson0("ok");
					} else {
						_errorInf = errorMsgToJson0("调用失败",
								new String[] { "status", getString("status"), "statusname", getString("statusname") });
					}
				} else {
					_errorInf = errorMsgToJson0("调用失败", new String[] { "status", "99", "statusname", "账单不存在" });
				}
			} else {
				_errorInf = errorMsgToJson0("调用失败", new String[] { "status", "-10", "statusname", "无效的操作码" });
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/**
	 * 超时自动取消
	 */
	public void billDespatchCancel() {
		try {
			executeQuery("select id_key,dd_key,ydjh,status=-status from BlueCQp3..xs_pfd_wlxx "
					+ "where (status=1 and DATEDIFF(mi,fdtime,getdate())>30) or(status=2 and DATEDIFF(mi,qdtime,getdate())>60)");
			while (next()) {
				billDespatchCancel(getString("id_key"), getString("dd_key"), getString("ydjh"), getInt("status"));
			}

		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
	}

	/**
	 * 货运单回写
	 */
	public String setWaybill(HttpServletRequest request) {
		try {
			JSONObject inJson = dataFromRequestToJson(request);

			String keySql = (inJson.has("order_id") && !inJson.getString("order_id").equals(""))
					? String.format("a.id_key=%s", inJson.getString("order_id"))
					: String.format("a.dd_key='%s'", inJson.getString("erporder_number"));

			String orderkey = inJson.getString("order_id");
			String waybill = inJson.getString("waybill");
			if (!waybill.equals("")) {
				executeQuery3(
						"select a.status,statusname=b.sname,a.id_key,a.dd_key,a.ydjh from BlueCQp3..xs_pfd_wlxx a,BlueCQp3..base_dpstatus b "
								+ "where %s and b.sid=a.status",
						keySql);
				if (next()) {
					orderkey = getString("id_key");
					if (getInt("status") >= 0) {
						executeUpdate("update BlueCQp3..xs_pfd_wlxx set hyb_dh='%s' where id_key=%s",
								new String[] { waybill, orderkey });
						_errorInf = sucessMsgToJson0("ok");
					} else {
						_errorInf = errorMsgToJson0("调用失败",
								new String[] { "status", getString("status"), "statusname", getString("statusname") });
					}
				} else {
					_errorInf = errorMsgToJson0("调用失败", new String[] { "status", "99", "statusname", "账单不存在" });
				}
			} else {
				_errorInf = errorMsgToJson0("调用失败", new String[] { "status", "-1", "statusname", "货运单号不能为空" });
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/* 获取收货地址 */
	public String getKwlAddressInfo(HttpServletRequest request) {
		try {
			_errorInf = CHttpService.sendPost(CAppListener.getParam("kwlurl") + "/store/queryForReceives",
					new String[] { "fuzzy_str", request.getParameter("fuzzy_str").toString(), "fuzzy_type",
							request.getParameter("fuzzy_type").toString() });
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	public String getKwlAddressInfoXml(HttpServletRequest request) {
		try {
			JSONObject json = new JSONObject(getKwlAddressInfo(request));
			_errorInf = jsonArrayToBlueCXml(new String[] { "id", "receive_name", "receive_phone", "receive_user_name",
					"receive_user_phone", "receive_address", "receive_type", "receive_lon", "receive_lat",
					"create_time", "create_user_id" }, json.getJSONArray("data"));
		} catch (Exception ex) {
			_errorInf = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/* 更新收货地址 */
	public String setKwlAddressInfo(HttpServletRequest request) {
		try {
			String[] vparams = null;
			if (request.getParameter("id").toString().endsWith("0")) { // 新增
				vparams = new String[16];
			} else {
				vparams = new String[18];
				vparams[16] = "id";
				vparams[17] = request.getParameter("id").toString();
			}
			vparams[0] = "receive_name";
			vparams[1] = request.getParameter("receive_name").toString();
			vparams[2] = "receive_phone"; // 收货单位电话
			vparams[3] = request.getParameter("receive_phone").toString();
			vparams[4] = "receive_user_name"; // 收货单位联系人名字
			vparams[5] = request.getParameter("receive_user_name").toString();
			vparams[6] = "receive_user_phone"; // 收货单位联系人电话
			vparams[7] = request.getParameter("receive_user_phone").toString();
			vparams[8] = "receive_address"; // 收货单位地址
			vparams[9] = request.getParameter("receive_address").toString();
			vparams[10] = "receive_type"; // 收货单位类别，0为修理厂，1为货运部
			vparams[11] = request.getParameter("receive_type").toString();
			vparams[12] = "receive_lon"; // 收货单位经度
			vparams[13] = request.getParameter("receive_lon").toString();
			vparams[14] = "receive_lat"; // 收货单位纬度
			vparams[15] = request.getParameter("receive_lat").toString();

			_errorInf = CHttpService.sendPost(CAppListener.getParam("kwlurl") + "/store/createOrUpdateReceiveInfo",
					vparams);
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	public String setKwlAddressInfoXml(HttpServletRequest request) {
		try {
			JSONObject json = new JSONObject(setKwlAddressInfo(request));
			if (json.getInt("code") == 200) {
				_errorInf = sucessMsgToXml("ok");
			} else {
				_errorInf = errorMsgToXml(json.getString("msg"));
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/* 获取商家信息 */
	public String getKwlClientInfo(HttpServletRequest request) {
		try {
			_errorInf = CHttpService.sendPost(CAppListener.getParam("kwlurl") + "/store/queryForStore",
					new String[] {});
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	public String getKwlClientInfoXml(HttpServletRequest request) {
		try {
			JSONObject json = new JSONObject(getKwlClientInfo(request));
			_errorInf = jsonArrayToBlueCXml(new String[] { "id", "store_name", "store_phone", "create_id", "store_code",
					"store_address", "store_lat", "store_lon", "create_time", "update_time", "man_name", "man_phone",
					"man_id", "gender" }, json.getJSONArray("data"));
		} catch (Exception ex) {
			_errorInf = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	/* 更新商家地址 */
	public String setKwlClientInfo(HttpServletRequest request) {
		try {
			String[] vparams = null;
   		    vparams = new String[22];
			vparams[0] = "id";
			vparams[1] = request.getParameter("id").toString();
			vparams[2] = "store_name";
			vparams[3] = request.getParameter("store_name").toString();
			vparams[4] = "store_phone"; 
			vparams[5] = request.getParameter("store_phone").toString();
			vparams[6] = "store_address"; 
			vparams[7] = request.getParameter("store_address").toString();
			vparams[8] = "store_code"; 
			vparams[9] = request.getParameter("store_code").toString();
			vparams[10] = "store_lat"; 
			vparams[11] = request.getParameter("store_lat").toString();
			vparams[12] = "store_lon"; 
			vparams[13] = request.getParameter("store_lon").toString();
			vparams[14] = "man_name"; 
			vparams[15] = request.getParameter("man_name").toString();
			vparams[16] = "man_phone"; 
			vparams[17] = request.getParameter("man_phone").toString();
			vparams[18] = "man_id"; 
			vparams[19] = request.getParameter("man_id").toString();
			vparams[20] = "gender"; 
			vparams[21] = request.getParameter("gender").toString();
			

			_errorInf = CHttpService.sendPost(CAppListener.getParam("kwlurl") + "/store/createOrUpdateStore", vparams);
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}

	public String setKwlClientInfoXml(HttpServletRequest request) {
		try {
			JSONObject json = new JSONObject(setKwlClientInfo(request));
			if (json.getInt("code") == 200) {
				_errorInf = sucessMsgToXml("ok");
			} else {
				_errorInf = errorMsgToXml(json.getString("msg"));
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}
	
	/*更新快运单号*/
	public String updateTransNo(HttpServletRequest request) {
		try {
			_errorInf = CHttpService.sendPost(CAppListener.getParam("kwlurl") + "/store/updateTransNo",
					new String[] {"order_number",request.getParameter("order_number").toString(),
							"transport_no",request.getParameter("transport_no").toString()});
		} catch (Exception ex) {
			_errorInf = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}
	
	public String updateTransNoXml(HttpServletRequest request) {
		try {
			JSONObject json = new JSONObject(updateTransNo(request));
			if (json.getInt("code") == 200) {
				_errorInf = sucessMsgToXml("ok");
			} else {
				_errorInf = errorMsgToXml(json.getString("msg"));
			}
		} catch (Exception ex) {
			_errorInf = errorMsgToXml(ex.getMessage());
		} finally {
			closeConn();
		}
		return _errorInf;
	}
}
