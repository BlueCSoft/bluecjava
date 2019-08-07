package bluec.base;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.io.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CJson extends CQuery {

	static String[] __dateFormat = { "TO_DATE(%s,'yyyy.mm.dd hh24:mi:ss')", "'%s'" };

	protected JSONArray __gsycsArray = null;

	protected JSONObject jsonobject = null;

	protected JSONObject njsonobj = null;

	protected String[] dataAllSqls = null;

	public CJson() {
	}

	public CJson(HttpServletRequest request) {
		this.__Request = request;
	}

	public int jsonObjCount() {
		return (__gsycsArray == null) ? 0 : __gsycsArray.length();
	}

	public String getJsonString(String key) {
		String Result = "";
		try {
			Result = jsonobject.getString(key);
		} catch (JSONException e) {
			Result = e.getMessage();
		}
		return Result;
	}

	public int getJsonInt(String key) {
		int Result = 0;
		try {
			Result = jsonobject.getInt(key);
		} catch (JSONException e) {
			Result = 0;
		}
		return Result;
	}

	public double getJsonDouble(String key) {
		double Result = 0;
		try {
			Result = jsonobject.getDouble(key);
		} catch (JSONException e) {
			Result = 0;
		}
		return Result;
	}

	public String getJsonString(int index, String key) {
		String Result = "";
		try {
			Result = __gsycsArray.getJSONObject(index).getString(key);
		} catch (JSONException e) {
			Result = e.getMessage();
		}
		return Result;
	}

	public int getJsonInt(int index, String key) {
		int Result = 0;
		try {
			Result = __gsycsArray.getJSONObject(index).getInt(key);
		} catch (JSONException e) {
			Result = 0;
		}
		return Result;
	}

	public double getJsonDouble(int index, String key) {
		double Result = 0;
		try {
			Result = __gsycsArray.getJSONObject(index).getDouble(key);
		} catch (JSONException e) {
			Result = 0;
		}
		return Result;
	}

	public String JsonArrayToOption(String valueKey, String textValue) {
		StringBuilder buffer = new StringBuilder();
		try {
			for (int i = 0; i < __gsycsArray.length(); i++) {
				JSONObject obj = __gsycsArray.getJSONObject(i);
				String selected = (i == 0) ? " selected" : "";
				buffer.append("<option value='" + obj.getString(valueKey) + "'" + selected + ">"
						+ obj.getString(textValue) + "</option>");
			}
		} catch (JSONException e) {
			buffer.append(e.getMessage());
		}
		return buffer.toString();
	}

	protected int createInsertValues(String tableName, JSONArray inpArray, int index, int rcount) {
		if (rcount > 0) {
			try {
				String usql = createInsertSql(tableName);
				int i = 0;
				String vStr = "";
				int fieldCount = __dynFieldNames.length;
				String[] fvalues = new String[fieldCount];
				String datefmt = __dateFormat[_databaseType];

				JSONObject jobj = inpArray.getJSONObject(0);
				__jsonFieldNames = new String[fieldCount];

				@SuppressWarnings("rawtypes")
				Iterator iterator = jobj.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					for (int y = 0; y < fieldCount; y++) {
						if (__dynFieldNames[y].compareToIgnoreCase(key) == 0) {
							__jsonFieldNames[y] = key;
						}
					}
				}

				while (i < rcount) {
					JSONObject robj = inpArray.getJSONObject(i);
					for (int j = 0; j < fieldCount; j++) {
						String fName = __jsonFieldNames[j];
						if (robj.isNull(fName) || robj.getString(fName).equals(""))
							vStr = "null";
						else {
							switch (__dynFieldTypes[j]) {
							case -1: // sqlserver text类型
							case 1:
							case 12:
								vStr = "'" + robj.getString(fName).replace("'", "''") + "'";
								break;
							case 91:
							case 93:
								vStr = robj.getString(fName);
								if (!vStr.toLowerCase().equals("getdate()") && !vStr.toUpperCase().equals("SYSDATE")) {
									if (vStr.length() > 19)
										vStr = vStr.substring(0, 19);
									vStr = "'" + vStr + "'";
								}
								vStr = String.format(datefmt, vStr);
								break;
							default:
								vStr = robj.getString(fName).replace("'", "''");
								if (vStr.equals(""))
									vStr = "null";
								break;
							}
						}
						fvalues[j] = vStr;
					}

					dataAllSqls[index] = CUtil.formatStr(usql, fvalues, "%s");
					i++;
					index++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	protected int createInsertValues(String tableName, JSONArray inpArray, int index, int rcount, String xhField,
			String[] addFields, String[] addValues) {
		if (rcount > 0) {
			try {
				String usql = createInsertSql(tableName);
				int i = 0;
				String vStr = "";

				int fieldCount = __dynFieldNames.length;
				int[] isAddField = new int[fieldCount];

				for (int j = 0; j < fieldCount; j++) {
					isAddField[j] = -1;
					if (addFields != null)
						for (int x = 0; x < addFields.length; x++)
							if (__dynFieldNames[j].toUpperCase().equals(addFields[x].toUpperCase()))
								isAddField[j] = x;
				}

				String datefmt = __dateFormat[_databaseType];
				String[] fvalues = new String[fieldCount];
				JSONObject jobj = inpArray.getJSONObject(0);
				__jsonFieldNames = new String[fieldCount];

				@SuppressWarnings("rawtypes")
				Iterator iterator = jobj.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					for (int y = 0; y < fieldCount; y++) {
						if (__dynFieldNames[y].compareToIgnoreCase(key) == 0) {
							__jsonFieldNames[y] = key;
						}
					}
				}

				while (i < rcount) {
					JSONObject robj = inpArray.getJSONObject(i);

					for (int j = 0; j < __dynFieldNames.length; j++) {
						String fName = __jsonFieldNames[j];
						String dName = __dynFieldNames[j];
						if (xhField.compareToIgnoreCase(dName) == 0) {
							vStr = (i + 1) + "";
						} else {
							if (isAddField[j] < 0
									&& (fName == null || robj.isNull(fName) || robj.getString(fName).equals("")))
								vStr = "null";
							else {
								vStr = (isAddField[j] >= 0) ? addValues[isAddField[j]] : robj.getString(fName);
								if (vStr.equals("")) {
									vStr = "null";
								} else {
									switch (__dynFieldTypes[j]) {
									case -1: // sqlserver text类型
									case 1:
									case 12:

										vStr = "'" + vStr.replaceAll("'", "''") + "'";
										break;
									case 91:
									case 93:
										if (!vStr.toLowerCase().equals("getdate()")
												&& !vStr.toUpperCase().equals("SYSDATE")) {
											vStr = "'" + vStr + "'";
										}
										vStr = String.format(datefmt, vStr);
										break;
									default:
										vStr = robj.getString(fName).replaceAll("'", "''");
										if (vStr.equals(""))
											vStr = "null";
										break;
									}
								}
							}
						}
						fvalues[j] = vStr;
					}

					dataAllSqls[index] = CUtil.formatStr(usql, fvalues, "%s");
					// P(index+"="+dataAllSqls[index]);
					i++;
					index++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	protected int createInsertValues(String tableName, JSONArray inpArray, int index, int rcount, String[] addFields,
			String[] addValues) {
		return createInsertValues(tableName, inpArray, index, rcount, "", addFields, addValues);
	}

	protected int createInsertValues(String tableName, JSONObject robj, int index, String[] addFields,
			String[] addValues) {
		try {
			String usql = createInsertSql(tableName);
			String vStr = "";

			int fieldCount = __dynFieldNames.length;
			int[] isAddField = new int[fieldCount];

			for (int j = 0; j < fieldCount; j++) {
				isAddField[j] = -1;
				if (addFields != null)
					for (int x = 0; x < addFields.length; x++)
						if (__dynFieldNames[j].toUpperCase().equals(addFields[x].toUpperCase()))
							isAddField[j] = x;
			}

			String datefmt = __dateFormat[_databaseType];
			String[] fvalues = new String[fieldCount];
			__jsonFieldNames = new String[fieldCount];

			@SuppressWarnings("rawtypes")
			Iterator iterator = robj.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				for (int y = 0; y < fieldCount; y++) {
					if (__dynFieldNames[y].compareToIgnoreCase(key) == 0) {
						__jsonFieldNames[y] = key;
					}
				}
			}

			for (int j = 0; j < __dynFieldNames.length; j++) {
				String fName = __jsonFieldNames[j];
				if (isAddField[j] < 0 && (robj.isNull(fName) || robj.getString(fName).equals("")))
					vStr = "null";
				else {
					vStr = (isAddField[j] >= 0) ? addValues[isAddField[j]] : robj.getString(fName);
					if (!vStr.equals("")) {
						switch (__dynFieldTypes[j]) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							vStr = "'" + vStr + "'";
							break;
						case 91:
						case 93:
							if (!vStr.toLowerCase().equals("getdate()") && !vStr.toUpperCase().equals("SYSDATE"))
								vStr = String.format(datefmt, vStr); // "to_date('"
																		// +
																		// vStr
																		// +
																		// "','yyyy-MM-dd
																		// HH24:mi:ss')";
							break;
						default:
							break;
						}
					}
				}
				fvalues[j] = vStr;
			}

			dataAllSqls[index] = CUtil.formatStr(usql, fvalues, "%s");

			index++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
	}

	protected int createInsertValues(String tableName, JSONObject robj, int index) {
		return createInsertValues(tableName, robj, index, null, null);
	}

	protected int createInsertValues(String tableName, JSONObject robj) {
		return createInsertValues(tableName, robj, 0, null, null);
	}

	protected static String sucessMsgToJson(String msg) {
		return "{\"sub_code\":\"1\",\"sub_msg\":\"" + CUtil.replaceStr((msg == null) ? "成功" : msg, "'", "\"")
				+ "\",\"code\":\"1\",\"msg\":\"成功\"}";
	}

	protected static String sucessMsgToJson0(String msg) {
		return "{\"sub_code\":\"0\",\"sub_msg\":\"" + CUtil.replaceStr((msg == null) ? "成功" : msg, "'", "\"")
				+ "\",\"code\":\"0\",\"msg\":\"成功\"}";
	}

	protected String sucessMsgToJson(String msg, String[] params) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "1");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "1");
			outgmp.put("sub_msg", CUtil.replaceStr((msg == null) ? "空异常" : msg, "'", "\""));
			outgmp.put("time", CUtil.getOrigTime());
			for (int i = 0; i < params.length; i += 2) {
				outgmp.put(params[i], params[i + 1]);
			}
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	protected String sucessMsgToJson0(String msg, String[] params, String objName, JSONArray jsonArray) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "0");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "0");
			outgmp.put("sub_msg", CUtil.replaceStr((msg == null) ? "ok" : msg, "'", "\""));
			outgmp.put("time", CUtil.getOrigTime());
			for (int i = 0; i < params.length; i += 2) {
				outgmp.put(params[i], params[i + 1]);
			}
			if (objName != null) {
				outgmp.put(objName, jsonArray);
			}
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	protected String sucessMsgToJson0(String msg, String[] params) {
		return sucessMsgToJson0(msg, params, null, null);
	}

	protected String sucessMsgToJson0(String msg, String objName, JSONObject jsonObj) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "0");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "0");
			outgmp.put("sub_msg", CUtil.replaceStr((msg == null) ? "ok" : msg, "'", "\""));
			outgmp.put(objName, jsonObj);
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	protected String sucessMsgToJson0(String msg, String objName, JSONArray jsonArray) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "0");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "0");
			outgmp.put("sub_msg", CUtil.replaceStr((msg == null) ? "ok" : msg, "'", "\""));
			outgmp.put(objName, jsonArray);
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	protected String sucessMsgToJson0(String msg, String[] objNames, JSONArray[] jsonArrays) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "0");
			outgmp.put("msg", "sucess");
			outgmp.put("sub_code", "0");
			outgmp.put("sub_msg", CUtil.replaceStr((msg == null) ? "ok" : msg, "'", "\""));
			for (int i = 0; i < objNames.length; i++) {
				outgmp.put(objNames[i], jsonArrays[i]);
			}
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	public static String errorMsgToJsonCode(String errmsg) {
		return "{\"code\":\"0\",\"msg\":\"" + CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\"")
				+ "\",\"sub_code\":\"0\",\"sub_msg\":\"失败\"}";
	}

	public static String errorMsgToJsonCode0(String errmsg) {
		return "{\"code\":\"-1\",\"msg\":\"" + CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\"")
				+ "\",\"sub_code\":\"-1\",\"sub_msg\":\"失败\"}";
	}

	public static String errorMsgToJson(String errmsg) {
		return "{\"sub_code\":\"0\",\"sub_msg\":\"" + CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\"")
				+ "\",\"code\":\"1\",\"msg\":\"成功\"}";
	}

	public static String errorMsgToJson0(int subCode, String errmsg, String[] params) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "0");
			outgmp.put("msg", "ok");
			outgmp.put("sub_code", subCode);
			outgmp.put("sub_msg", CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\""));
			for (int i = 0; i < params.length; i += 2) {
				outgmp.put(params[i], params[i + 1]);
			}
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	public static String errorMsgToJson0(String errmsg, String[] params) {
		return errorMsgToJson0(-1, errmsg, params);
	}

	public static String errorMsgToJson0(int subCode, String errmsg) {
		String outresult = "";
		JSONObject outgmp = new JSONObject();
		try {
			outgmp.put("code", "0");
			outgmp.put("msg", "ok");
			outgmp.put("sub_code", subCode);
			outgmp.put("sub_msg", CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\""));
			outgmp.put("time", CUtil.getOrigTime());
			outresult = outgmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJsonCode(e.getMessage());
		}
		return outresult;
	}

	public static String errorMsgToJson0(String errmsg) {
		return errorMsgToJson0(-1, errmsg);
	}

	public static String errorMsgToJson(int errCode, String errmsg) {
		return "{\"errcode\":\"" + errCode + "\",\"errmsg\":\""
				+ CUtil.replaceStr((errmsg == null) ? "空异常" : errmsg, "'", "\"") + "\"}";
	}

	protected String sucessMsgToXml(String msg) {
		return "<xml><ok><r>" + msg + "</r></ok></xml>";
	}

	public static String errorMsgToXml(String errmsg) {
		return "<xml><errors><error>" + errmsg + "</error></errors></xml>";
	}

	public static String formatWxFmt(String time_end) {
		return time_end.substring(0, 4) + "-" + time_end.substring(4, 6) + "-" + time_end.substring(6, 8) + " "
				+ time_end.substring(8, 10) + ":" + time_end.substring(10, 12) + ":" + time_end.substring(12);
	}

	protected JSONArray formatResultToJsonArray() throws SQLException {

		JSONArray sArray = new JSONArray();
		String tStr = null;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();

			int recno = 1;

			while (rs.next()) {
				JSONObject row = new JSONObject();
				JSONObject o = null;
				row.put("recno", recno++);
				for (int i = 1; i <= fieldCount; i++) {
					int dataType = mData.getColumnType(i);
					int nScale = mData.getScale(i);
					String fname = mData.getColumnName(i).toLowerCase();
					tStr = rs.getString(i);

					if (rs.wasNull()) {
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
						case 91:
						case 93:
							row.put(fname, "");
							break;
						default:
							row.put(fname, "0");
							break;
						}
					} else {
						// tStr = tStr.trim();
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = CUtil.rightTrim(rs.getString(i));
							row.put(fname, CUtil.replaceXmlSpChar(tStr));
							break;
						case 91:
						case 93:
							tStr = CUtil.rightTrim(rs.getString(i));
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							row.put(fname, tStr);
							break;
						default:
							tStr = rs.getString(i);
							if (tStr.indexOf(".") == 0)
								tStr = "0" + tStr;
							row.put(fname, tStr);
							break;
						}
					}
				}
				sArray.put(row);
			}

		} catch (Exception ex) {
			E("格式化数据产生异常ToJsonArray：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sArray;
	}

	protected JSONObject dataFromRequestToJson() {
		StringBuilder sb = new StringBuilder();
		try {

			BufferedReader br = new BufferedReader(
					new InputStreamReader((ServletInputStream) __Request.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return new JSONObject(sb.toString());
		} catch (Exception ex) {
			E(ex.getMessage());
			return null;
		}
	}

	protected JSONObject dataFromRequestToJson(HttpServletRequest request) {
		this.__Request = request;
		return dataFromRequestToJson();
	}

	public void requestDataToJson(HttpServletRequest request) {
		this.__Request = request;
		jsonobject = dataFromRequestToJson();
	}

	public void requestDataToJson() {
		jsonobject = dataFromRequestToJson();
	}

	protected JSONObject formatResultToJsonObject() throws SQLException {
		try {
			return new JSONObject("{" + formatResultToJson() + "}");
		} catch (Exception ex) {
			E(ex.getMessage());
			return null;
		}
	}

	public static String ToJson(String[] vkeyparams) {
		JSONObject obj = new JSONObject();
		try {
			for (int i = 0; i < vkeyparams.length; i += 2)
				obj.put(vkeyparams[i], vkeyparams[i + 1]);
		} catch (Exception ex) {
		}
		return obj.toString();
	}

	public static String toJson(int code, String msg, int sub_code, String sub_msg, String[] vkeyparams) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("code", code);
			obj.put("msg", msg);
			obj.put("sub_code", sub_code);
			obj.put("sub_msg", sub_msg);
			JSONObject data = new JSONObject();
			if (vkeyparams != null)
				for (int i = 0; i < vkeyparams.length; i += 2)
					data.put(vkeyparams[i], vkeyparams[i + 1]);
			obj.put("data", data);

		} catch (Exception ex) {
		}
		return obj.toString();
	}

	public static String toJson(int sub_code, String sub_msg, String[] vkeyparams) {
		return toJson(0, "ok", sub_code, sub_msg, vkeyparams);
	}

	public static String toJson(String[] vkeyparams) {
		return toJson(0, "ok", 0, "success", vkeyparams);
	}

	/**
	 * 数据集数据格式化为json串,data是含数据的子对象
	 * 
	 * @param onlyOne
	 *            true仅第一条记录data是对象，false按json数组返回，data是数组
	 * @return json串
	 */
	public String rsToJson(int code, String msg, int sub_code, String sub_msg, Boolean onlyOne) {
		njsonobj = new JSONObject();
		try {
			njsonobj.put("code", code);
			njsonobj.put("msg", msg);
			njsonobj.put("sub_code", sub_code);
			njsonobj.put("sub_msg", sub_msg);

			JSONArray a = formatResultToJsonArray();
			if (a != null && a.length() > 0) {
				if (onlyOne) {
					njsonobj.put("data", a.getJSONObject(0));
					njsonobj.put("recordcount", 1);
				} else {
					njsonobj.put("data", a);
					njsonobj.put("recordcount", a.length());
				}
			} else {
				if (onlyOne) {
					njsonobj.put("data", new JSONObject());
				} else {
					njsonobj.put("data", new JSONArray());
				}
				njsonobj.put("recordcount", 0);
			}
		} catch (Exception ex) {
		}
		return njsonobj.toString();
	}

	public String rsToJson(int sub_code, String sub_msg, Boolean onlyOne) {
		return rsToJson(0, "ok", sub_code, sub_msg, onlyOne);
	}

	public String rsToJson(Boolean onlyOne) {
		return rsToJson(0, "ok", 0, "success", onlyOne);
	}

	public String rsToJson() {
		return rsToJson(0, "ok", 0, "success", false);
	}

	/**
	 * 数据集数据格式化为json串
	 * 
	 * @return json串
	 */
	public String rsToJson2(int code, String msg, int sub_code, String sub_msg) {
		try {
			njsonobj = formatResultToJsonObject();
			njsonobj.put("code", code);
			njsonobj.put("msg", msg);
			njsonobj.put("sub_code", sub_code);
			njsonobj.put("sub_msg", sub_msg);
		} catch (Exception ex) {
		}
		return njsonobj.toString();
	}

	public String rsToJson2(int sub_code, String sub_msg) {
		return rsToJson2(0, "ok", sub_code, sub_msg);
	}

	public String rsToJson2() {
		return rsToJson2(0, "ok", 0, "success");
	}

	public String rsAddToJson(String name) {
		try {
			JSONArray a = formatResultToJsonArray();
			if (a != null) {
				njsonobj.put(name, a);
			} else {
				njsonobj.put(name, new JSONArray());
			}
		} catch (Exception ex) {
		}
		return njsonobj.toString();
	}

	public String rsAddToJson() {
		try {
			JSONArray a = formatResultToJsonArray();
			if (a != null) {
				njsonobj.put("data", a);
				njsonobj.put("recordcount", a.length());
			} else {
				njsonobj.put("data", new JSONArray());
				njsonobj.put("recordcount", 0);
			}
		} catch (Exception ex) {
		}
		return njsonobj.toString();
	}

	/**
	 * 保存json数组数据到数据库
	 * 
	 * @param tableName
	 *            目标表名
	 * @param jsonArray
	 *            json数组记录
	 * @param xhField
	 *            序号字段，自动增长的字段,等于-1时，表示先清空目标表数据
	 * @param addFields
	 *            附加字段
	 * @param addValues
	 *            附加字段的值
	 * @return
	 */
	protected Boolean saveJsonArrayToDb(String tableName, JSONArray jsonArray, String xhField, String[] addFields,
			String[] addValues) {
		Boolean result = false;
		int n = 0;
		if (xhField.equals("-1")) {
			n = 1;
			xhField = "";
		}
		dataAllSqls = new String[jsonArray.length() + n];
		if (n > 0) {
			dataAllSqls[0] = "delete from " + tableName;
		}
		createInsertValues(tableName, jsonArray, n, jsonArray.length(), xhField, addFields, addValues);
		P(dataAllSqls, ";");
		try {
			result = executeSqls(dataAllSqls) == 0;
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
		return result;
	}

	protected Boolean saveJsonArrayToDb(String tableName, JSONArray jsonArray, String xhField) {
		return saveJsonArrayToDb(tableName, jsonArray, xhField, null, null);
	}

	protected Boolean saveJsonArrayToDb(String tableName, JSONArray jsonArray, String[] addFields, String[] addValues) {
		return saveJsonArrayToDb(tableName, jsonArray, "", addFields, addValues);
	}

	protected Boolean saveJsonArrayToDb(String tableName, JSONArray jsonArray) {
		return saveJsonArrayToDb(tableName, jsonArray, "", null, null);
	}

	/**
	 * 保存json数据到数据库
	 * 
	 * @param tableName
	 *            目标表名
	 * @param jsonObj
	 *            json数据
	 * @param initSql
	 *            初始化SQL,保存前执行的语句
	 * @param addFields
	 *            附加字段
	 * @param addValues
	 *            附加字段的值
	 * @return
	 */
	protected Boolean saveJsonObjectToDb(String tableName, JSONObject jsonObj, String initSql, String[] addFields,
			String[] addValues) {
		Boolean result = false;
		if (initSql.equals("")) {
			dataAllSqls = new String[1];
		} else {
			dataAllSqls = new String[2];
			dataAllSqls[0] = initSql;
		}
		createInsertValues(tableName, jsonObj, (initSql.equals("")) ? 0 : 1, addFields, addValues);
		try {
			P(dataAllSqls[0]);
			result = executeSqls(dataAllSqls) == 0;
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
		return result;
	}

	protected Boolean saveJsonObjectToDb(String tableName, JSONObject jsonObj, String initSql) {
		return saveJsonObjectToDb(tableName, jsonObj, initSql, null, null);
	}

	protected Boolean saveJsonObjectToDb(String tableName, JSONObject jsonObj) {
		return saveJsonObjectToDb(tableName, jsonObj, "", null, null);
	}

	/**
	 * 查询数据
	 * 
	 * @param sqlid
	 *            查询语句id
	 * @param vparams
	 *            参数数组
	 * @return 返回json结果串
	 */
	protected String QueryBySql(String sql, String[] vparams, Boolean onlyOne) {
		String outresult = "";
		try {
			queryBySqlInner(sql, vparams);
			outresult = rsToJson(onlyOne);
			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return outresult;
	}

	public String Query(String sqlid, String[] vparams, Boolean onlyOne) {
		String outresult = "";
		try {
			queryBySqlIDWithParamInner(sqlid, vparams);
			outresult = rsToJson(onlyOne);
			P(outresult);
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public String Query(String sqlid, String[] vparams) {
		return Query(sqlid, vparams, false);
	}

	public String QueryEx(String sqlid, String paramStr, Boolean onlyOne) {
		return Query(sqlid, paramStr.split(","), onlyOne);
	}

	public String QueryEx(String sqlid, String paramStr) {
		return QueryEx(sqlid, paramStr);
	}

	public String Query(String sqlid, Boolean onlyOne) {
		return Query(sqlid, null, onlyOne);
	}

	public String Query(String sqlid) {
		return Query(sqlid, null, false);
	}

	/**
	 * 查询汇总数据和明细数据
	 * 
	 * @param sqlidsum
	 *            汇总数据sqlid
	 * @param sqlid
	 *            明细数据sqlid
	 * @param vparams
	 *            查询条件
	 * @return
	 */
	public String QueryS(String sqlidsum, String sqlid, String[] vparams) {
		String outresult = "";
		try {
			queryBySqlIDWithParamInner(sqlidsum, vparams);
			rsToJson2();
			queryBySqlIDWithParamInner(sqlid, vparams);

			outresult = rsAddToJson("data");
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public String QueryS(String sqlidsum, String sqlid) {
		return QueryS(sqlidsum, sqlid, null);
	}

	/* 执行sqlid */
	public String Update(String sqlid, String[] vparams) {
		String outresult = "";
		try {
			String sql = getContraint(sqlid, vparams);
			int k = 0;
			if ((k = executeUpdate(sql)) > 0) {
				outresult = sucessMsgToJson0("ok", new String[] { "recordcount", k + "" });
			} else {
				outresult = errorMsgToJson0(sqlid);
			}
		} catch (Exception e) {
			e.printStackTrace();
			outresult = errorMsgToJson0(e.getMessage());
		} finally {
			closeConn();
		}
		return CUtil.formatJson(outresult);
	}

	public String Update(String sqlid) {
		return Update(sqlid, null);
	}

	/* Json数据转换为xml格式 */

	public String jsonArrayToBlueCXml(String[] fieldNames,JSONArray rows){
		StringBuffer sResult = new StringBuffer();
		RecordCount = rows.length();
		int fieldCount = fieldNames.length;
		try {
			sResult.append("<xml id=\"data\">\n");
			sResult.append("<RS>" + _XmlCr);
			for(int i = 0;i<RecordCount;i++){
				JSONObject row = rows.getJSONObject(i);
				sResult.append("<R");
				for(int j = 0;j<fieldCount;j++){
					sResult.append(String.format(" F%d=\"%s\"", j+1, CUtil.NVL(row.getString(fieldNames[j]))));
				}
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);
			sResult.append("<FS>" + _XmlCr);

			for (int i = 0; i < fieldCount; i++) {
				sResult.append("<F");
				sResult.append(" A=\"" + fieldNames[i].toUpperCase() + "\""); // 字段名称
				sResult.append(" O=\"F" + (i + 1) + "\""); // 字段别名
				sResult.append(" B=\"" + 12 + "\"/>" + _XmlCr); // 字段类型
			}
			sResult.append("</FS>");
			sResult.append("</xml>\n");
		} catch (Exception ex) {
			System.out.println("jsonArrayToBlueCXml：\n " + ex.getMessage());
		}
		return sResult.toString();
	}
}
