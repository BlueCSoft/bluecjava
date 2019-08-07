package bluec.gyy;

import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.json.JSONArray;

public class CGyerp extends CYmsInterFace {

	public CGyerp(HttpServletRequest request) {
		super(request);
	}

	public CGyerp() {

	}

	protected Connection getConnection() throws SQLException {
		return CGerpLink.getConnection();
	}

	public String test() {
		String result = "";
		try {
			executeQuery("select * from base_poscode where sno='201010010008'");
			if (next()) {
				result = sucessMsgToJson0("ok", new String[] { "sname", getString("sname") });
			}
		} catch (Exception ex) {
			P(ex.getMessage());
		} finally {
			closeConn();
		}
		return result;
	}

	// 根据门店获取商场信息
	public String pos_querylc(String vmkt) {
		String outresult = "";
		try {
			if (callProc("pos_interface.pos_querylc", new String[] { vmkt }, new int[] { 1 }, new int[] { 9 },
					false) != 0) {
				outresult = errorMsgToJsonCode0("调用失败");
			} else {
				JSONObject out = new JSONObject();
				JSONArray jarray = new JSONArray();
				int rcount = 0;
				while (next()) {
					JSONObject a = new JSONObject();
					a.put("sno", getString("mfcode"));
					a.put("sname", toGbk(getString("mfcname")));
					jarray.put(a);
					rcount++;
				}

				out.put("recordcount", rcount);

				out.put("data", jarray);
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

	// 根据商场获取专柜信息
	public String pos_queryzg(String vpcode) {
		String outresult = "";
		try {
			if (callProc("pos_interface.pos_queryzg", new String[] { vpcode }, new int[] { 1 }, new int[] { 9 },
					false) != 0) {
				outresult = errorMsgToJsonCode0("调用失败");
			} else {
				JSONObject out = new JSONObject();
				JSONArray jarray = new JSONArray();
				int rcount = 0;
				listField();
				while (next()) {
					JSONObject a = new JSONObject();
					a.put("sno", getString("mfcode"));
					a.put("sname", toGbk(getString("mfcname")));
					// a.put("supplyid", getString("supid"));
					jarray.put(a);
					rcount++;
				}

				out.put("recordcount", rcount);

				out.put("data", jarray);
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

	// 根据专柜获取商品信息
	public String pos_querygoods(String vpcode) {
		String outresult = "";
		try {
			if (callProc("pos_interface.pos_querygoods", new String[] { vpcode }, new int[] { 1 }, new int[] { 9 },
					false) != 0) {
				outresult = errorMsgToJsonCode0("调用失败");
			} else {
				JSONObject out = new JSONObject();
				JSONArray jarray = new JSONArray();
				listField();
				int rcount = 0;
				while (next()) {
					JSONObject a = new JSONObject();
					a.put("goodsid", getString("goodsid"));
					a.put("goodsname", toGbk(getString("goodsname")));
					a.put("unit", toGbk(getString("unit")));
					a.put("barcode", getString("barcode"));
					a.put("spec", getString("spec"));
					a.put("bzhl", getFloat("bzhl"));
					a.put("gz", getString("gz"));
					a.put("catid", getString("catid"));
					a.put("brandid", getString("brandid"));
					a.put("lsj", getFloat("lsj"));
					a.put("hyj", getFloat("hyj"));
					a.put("pfj", getFloat("pfj"));
					a.put("iszk", getString("iszk"));
					a.put("isvipzk", getString("isvipzk"));
					a.put("isdzc", getString("isdzc"));

					jarray.put(a);
					rcount++;
				}

				out.put("recordcount", rcount);

				out.put("data", jarray);
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

	// 查询商品资料
	public String pos_findgoods(String vsyjh, String vmkt, String vcode, String vgz, String vcardid, String vcxsj) {
		String outresult = "";
		try {
			if (callProc("pos_interface.pos_findgoods", new String[] { vsyjh, vmkt, vcode, vgz, vcardid, vcxsj, "" },
					new int[] { 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 1, 1, 3, 3, 3, 1, 3, 3, 3 }, true) != 0) {
				outresult = errorMsgToJson0("调用失败");
			} else {
				listField();

				JSONObject out = new JSONObject();
				JSONObject json = new JSONObject();
				json.put("ret", __procReturn[0]);
				json.put("rbarcode", __procReturn[1]);
				json.put("rcode", __procReturn[2]);
				json.put("rgz", __procReturn[3]);
				json.put("rdzxl", __procReturn[4]);
				json.put("rpp", __procReturn[5]);
				json.put("rtype", __procReturn[6].trim());
				json.put("rname", __procReturn[7]);
				json.put("runit", __procReturn[8]);
				json.put("rspec", __procReturn[9]);
				json.put("rbzhl", __procReturn[10]);
				json.put("rlsj", __procReturn[11]);
				json.put("rpopzk", __procReturn[12]);
				json.put("ryhdjbh", __procReturn[13]);
				json.put("ryhzkfd", __procReturn[14]);
				json.put("rcustzk", __procReturn[15]);
				json.put("rcustzkfd", __procReturn[16]);
				json.put("rcomzk", __procReturn[17]);
				json.put("rcomdjbh", __procReturn[18]);
				json.put("rcomzkfd", __procReturn[19]);
				json.put("rcjj", __procReturn[20]);
				json.put("rxxtax", __procReturn[21]);
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

	// 整单折扣金额
	public String pos_calsalelist(String vsyjh, String vmkt, String vcardid, String vcxsj, String vsalelist) {
		String outresult = "";
		Result = -1;
		try {
			JSONArray ia = new JSONArray(vsalelist);
			JSONArray oa = new JSONArray();
			for(int i=0;i<ia.length();i++){
				JSONObject oo = new JSONObject();
				JSONObject io = ia.getJSONObject(i);
				oo.put("listno", io.getString("seq"));
				oo.put("barcode", io.getString("barcode"));
				oo.put("name", "");
				oo.put("code", io.getString("poscode"));
				oo.put("type", io.getString("stype"));
				oo.put("gz", io.getString("groupno"));
				oo.put("xl", io.getString("catalog"));
				oo.put("pp", io.getString("brand"));
				oo.put("spec", io.getString("spec"));
				oo.put("unit", io.getString("unit"));
				oo.put("bzhl", io.getDouble("packsize"));
				
				oo.put("sl", io.getDouble("quantity"));
				oo.put("sj", io.getDouble("iprice"));
				oo.put("je", io.getDouble("amount"));
				oo.put("custzk", io.getDouble("vipdiscount"));
				oo.put("custzkfd", 0.0);
				oo.put("popzk", io.getDouble("popzk"));
				oo.put("popzkfd", 0.0);
				oo.put("popdjbh", "");
				
				oo.put("rulezk", io.getDouble("rulezk"));
				oo.put("rulezkfd",  io.getString("tsupplyshare"));
				oo.put("ruledjbh",  "");
				oo.put("zpzk",  io.getDouble("totaldiscount"));
				oo.put("dpzk",  io.getDouble("counterdiscount"));
				oo.put("grantno",  "");
				oa.put(oo);
			}
			if (callProc("pos_interface.pos_calsalelist", new String[] { vsyjh, vmkt, vcardid, vcxsj, oa.toString() },
					new int[] { 1, 1, 1, 1, 1 }, new int[] { 2, 1, 1 }, true) != 0) {
				outresult = errorMsgToJson0(__procReturn[1]);
			} else {
				JSONArray a = new JSONArray(__procReturn[2]);
				outresult = sucessMsgToJson0("ok", "goods", a);
				Result = 0;
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	// 上传小票
	public String pos_sendsale(String vsyjh, String vfphm, String vmkt, String vyyyh, String vsaletime, String vcardid,
			String vsaletype, String vysje, String vfkje, String vzl, String vsy, String vsalelist, String vpaylist,
			String vyfphm) {
		String outresult = "";
		Result = -1;
		try {
			if (callProc("pos_interface.pos_sendsale",
					new String[] { vsyjh, vfphm, vmkt, vyyyh, vsaletime, vcardid, vsaletype, vysje, vfkje, vzl, vsy, vsalelist,
							vpaylist, vyfphm },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 1, 1, 1 }, new int[] { 2, 1, 1 }, true) != 0) {
				outresult = errorMsgToJson0(__procReturn[1]);
			} else {
				outresult = sucessMsgToJson0("ok", new String[] { "erpbillid", __procReturn[2] });
				Result = 0;
			}
		} catch (Exception ex) {
			outresult = errorMsgToJson0(ex.getMessage());
		} finally {
			closeConn();
		}
		P(outresult);
		return outresult;
	}

	public String pos_sendsale() {
		return pos_sendsale(requestParam("vsyjh"), requestParam("vfphm"), requestParam("vmkt"), requestParam("vyyyh"),
				requestParam("vsaletime"), requestParam("vcardid"), requestParam("vsaletype"), requestParam("vysje"),
				requestParam("vfkje"), requestParam("vzl"), requestParam("vsy"), requestParam("vsalelist"),
				requestParam("vpaylist"), requestParam("vyfphm"));
	}
}
