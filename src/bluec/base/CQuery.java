package bluec.base;

import java.io.PrintWriter;

/**
 * <p>Title: 数据查询</p>
 * <p>所有数据的查询</p>
 * <p>Copyright: SunWay(c) 2003</p>
 * @author 蓝仕红
 * @version 1.0
 */

import java.sql.*;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import bluec.base.cache.*;

public class CQuery extends CDataSet {

	private HttpServletRequest prequest = null;
	protected HttpServletRequest __Request = null;
	protected HttpServletResponse __Response = null;
	protected HttpSession __Session = null;
	public String[] __procReturn = null;

	public String __arrayNo = "";
	public String __arrayName = "";
	private String __vOption = "";

	public CQuery() {
	}

	/**
	 * formatStr 使用数组params中的项顺序替换字符串dstr中为%s的子字符串
	 * 
	 * @param dstr
	 *            原字符串
	 * @param params
	 *            参数数组
	 * @return 替换后的字符串 此函数用来保持向后兼容
	 */
	protected String formatStr(String dstr, String[] params) {
		if (params == null)
			return dstr;
		return CUtil.formatStr(dstr, params, "%s");
	}

	/**
	 * formatSql 使用数组params中的项顺序替换字符串dstr中为%s的子字符串,
	 * 使用UserID替换字符串dstr中为%USERID的所有子字符串
	 * 
	 * @param dstr
	 *            原字符串
	 * @param params
	 *            参数数组
	 * @param UserID
	 *            一般指商家代码
	 * @return 替换后的字符串
	 */
	protected String formatSql(String dstr, String[] params, String UserID) {
		if (params == null)
			return dstr;
		String tStr = CUtil.formatStr(dstr, params, "%s");
		P(tStr);
		return tStr;
	}

	protected String getCubSql(String pSql) {
		return "";
	}

	private String replaceSessionParam(String sql, HttpSession session) {

		String pSql = sql;
		int x = pSql.indexOf("{");
		if (session != null && x > -1) {
			StringBuffer temp = new StringBuffer();
			x = 0;
			int y = 2; // 寻找子字符串的开始位置
			while (x > -1 && y > x + 1) {
				x = pSql.indexOf("{");
				y = pSql.indexOf("}");
				if (x > -1 && y > x + 1) {
					String ss = pSql.substring(x + 1, y);
					temp.append(pSql.substring(0, x));
					if (session.getAttribute(ss) != null) {
						temp.append(session.getAttribute(ss).toString());
						pSql = pSql.substring(y + 1);
					} else
						pSql = pSql.substring(x + 1);
				}
			}
			pSql = temp.toString() + pSql;
		}
		return pSql;
	}

	protected String getContraint(String SqlID, String[] Params, HttpSession session) {
		String pSql = getContraint0(SqlID, Params);

		/* 根据原SQL语句和参数构造查询SQL语句 */
		if (_sqlType != 3) {
			// 检查是否有初始化SQL语句存在
			// pSql = CUtil.formatStr(pSql, Params, "%s");
			// System.out.println("pSql=\n"+pSql);
			int pos = pSql.indexOf("----paraminitend----");
			if (pos > 5) {
				String bSql = pSql.substring(0, pos);
				pSql = pSql.substring(pos + 20);

				String[] subSqls = bSql.split(",");
				int i = 0, j = 0, pcount = 0;
				if (Params != null) { // 正常参数
					pcount = Params.length;
					while (i < subSqls.length && j < pcount) {
						if (subSqls[i].indexOf("%s") > -1) {
							subSqls[i] = CUtil.formatStr(subSqls[i], Params[j], "%s");
							j++;
						}
						i++;
					}
				} else { // request传递的参数
					String param = "";
					while (i < subSqls.length) {
						int k = subSqls[i].indexOf("=");
						if (k > -1) {
							param = subSqls[i].substring(0, k).trim();
						} else {
							param = subSqls[i].trim();
						}
						if (subSqls[i].indexOf("%s") > -1 && __Request.getParameter(param) != null) {
							subSqls[i] = CUtil.formatStr(subSqls[i], __Request.getParameter(param).toString(), "%s");
							j++;
						}
						i++;
					}
				}

				String _pName = "";
				String _pValue = "";
				i = 0;
				j = 0;
				while (i < subSqls.length) {
					int dpos = subSqls[i].indexOf("=");
					if (dpos > 0) {
						_pName = subSqls[i].substring(0, dpos).trim();
						_pValue = subSqls[i].substring(dpos + 1).trim();
						pSql = CUtil.formatStr(pSql, _pValue, "%" + _pName);
					}
					i++;
				}
			} else
				pSql = CUtil.formatStr(pSql, Params, "%s");
			// 替换session变量
			if (session != null)
				pSql = replaceSessionParam(pSql, session);
		}
		return pSql.replaceAll("%%", "%");
	}

	protected String getContraint(String SqlID, String[] Params) {
		return getContraint(SqlID, Params, null);
	}

	protected String getContraint(String SqlID) {
		return getContraint(SqlID, null);
	}

	/**
	 * 根据SqlID获取相关的SQL语句
	 * 
	 * @param SqlID
	 *            SQL语句标识
	 * @param Params
	 *            替换的参数
	 * @param session
	 *            当前对话
	 * @return 成功SQL语句，失败空字符串
	 */
	public String getSql(String SqlID, String[] Params, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return "";
		return getContraint(SqlID, Params, session);
	}

	public String getSql(String SqlID, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return "";
		return getContraint(SqlID, "", session);
	}

	// 根据表名称查询
	public ResultSet queryByTableName(String TableName, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return null;
		String pSql = new String();
		// 获取附加约束条件
		pSql = getContraint(TableName, "", session);
		return executeQuery(pSql);
	}

	public ResultSet queryByTableNameInner(String TableName) throws SQLException {
		String pSql = new String();
		// 获取附加约束条件
		pSql = getContraint(TableName, "", null);
		return executeQuery(pSql);
	}

	// 直接使用SqlID语句查询数据
	public ResultSet queryBySqlID(String SqlID, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return null;
		String pSql = new String();
		pSql = getContraint(SqlID, "", session);

		if (_sqlType == 4)
			rs = executeProcQuery(_procName, pSql);
		else
			rs = executeQuery(pSql);
		return rs;
	}

	public ResultSet queryBySqlIDInner(String SqlID) throws SQLException {
		String pSql = new String();
		pSql = getContraint(SqlID, "", null);

		if (_sqlType == 4)
			rs = executeProcQuery(_procName, pSql);
		else
			rs = executeQuery(pSql);
		return rs;
	}

	// 带参数的查询
	public ResultSet queryBySql(String Sql, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return null;
		return executeQuery(replaceSessionParam(Sql, session));
	}

	public ResultSet queryBySqlInner(String Sql) throws SQLException {
		return executeQuery(Sql);
	}

	/**
	 * 
	 * @param Sql
	 * @param Params
	 * @param session
	 * @return
	 * @throws SQLException
	 */
	public ResultSet queryBySql(String Sql, String[] Params, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return null;
		Sql = formatSql(Sql, Params, "");
		Sql = replaceSessionParam(Sql, session);
		if (_sqlType == 3)
			rs = executeQuery3(Sql, Params);
		else
			rs = executeQuery(Sql);
		return rs;
	}

	public ResultSet queryBySqlInner(String Sql, String[] Params) throws SQLException {
		Sql = formatSql(Sql, Params, "");
		if (_sqlType == 3)
			rs = executeQuery3(Sql, Params);
		else
			rs = executeQuery(Sql);
		return rs;
	}

	public ResultSet queryBySql(String Sql, String param, HttpSession session) throws SQLException {
		String[] params = { param };
		return queryBySql(Sql, params, session);
	}

	public ResultSet queryBySqlInner(String Sql, String param) throws SQLException {
		String[] params = { param };
		return queryBySqlInner(Sql, params);
	}

	// 带参数的查询
	public ResultSet queryBySqlIDWithParam(String SqlID, String[] Params, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return null;
		String pSql = getContraint(SqlID, Params, session);

		switch (_sqlType) {
		case 5:
			rs = executeQuery(pSql);
			break;
		case 3:
			rs = executeQuery3(pSql, Params);
			break;
		case 4:
			rs = executeProcQuery(_procName, pSql);
		}
		return rs;
	}

	public ResultSet queryBySqlIDWithParamInner(String SqlID, String[] Params) throws SQLException {
		String pSql = getContraint(SqlID, Params);

		switch (_sqlType) {
		case 5:
			rs = executeQuery(pSql);
			break;
		case 3:
			rs = executeQuery3(pSql, Params);
			break;
		case 4:
			rs = executeProcQuery(_procName, pSql);
		}
		return rs;
	}

	public ResultSet queryBySqlIDwithParam(String SqlID, String[] Params, HttpSession session) throws SQLException {
		return queryBySqlIDWithParam(SqlID, Params, session);
	}

	public ResultSet queryBySqlIDwithParamInner(String SqlID, String[] Params) throws SQLException {
		return queryBySqlIDWithParamInner(SqlID, Params);
	}

	public ResultSet queryBySqlIDWithParam(String SqlID, String Param, HttpSession session) throws SQLException {
		String[] Params = new String[1];
		Params[0] = Param;
		return queryBySqlIDWithParam(SqlID, Params, session);
	}

	public ResultSet queryBySqlIDWithParamInner(String SqlID, String Param) throws SQLException {
		String[] Params = new String[1];
		Params[0] = Param;
		return queryBySqlIDWithParamInner(SqlID, Params);
	}

	protected int setKeys() throws SQLException {
		Vector<String> keyC = new Vector<String>(); //
		Vector<String> keyN = new Vector<String>(); //
		Vector<String> keyL = new Vector<String>(); //
		int kc = 1, kn = 1, kl = 1, nresult = 0;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			if (mData.getColumnCount() > 1)
				kn = 2;
			if (mData.getColumnCount() > 2)
				kl = 3;
			while (rs.next()) {
				keyC.add(rs.getString(kc));
				keyN.add(rs.getString(kn));
				keyL.add(rs.getString(kl));
				nresult++;
			}
			__keyCodeValues = new String[keyC.size()];
			keyC.copyInto(__keyCodeValues);
			__keyNameValues = new String[keyN.size()];
			keyN.copyInto(__keyNameValues);
			__keyLevelValues = new String[keyL.size()];
			keyL.copyInto(__keyLevelValues);

		} catch (SQLException ex) {
			throw new SQLException("分析数据产生异常：\n " + ex.getMessage());
		}
		return nresult;
	}

	public int setKeys(String[] keyValues, String[] keyNames, String[] keyLevels) {
		__keyCodeValues = keyValues;
		__keyNameValues = (keyNames == null) ? keyValues : keyNames;
		__keyLevelValues = (keyLevels == null) ? keyValues : keyLevels;
		return keyValues.length;
	}

	public int setKeysBySql(String sql, HttpSession session) throws SQLException {
		queryBySql(sql, session);
		return setKeys();
	}

	public int setKeysBySql(String sql, String param, HttpSession session) throws SQLException {
		queryBySql(sql, param, session);
		return setKeys();
	}

	public int setKeysBySql(String sql, String[] params, HttpSession session) throws SQLException {
		queryBySql(sql, params, session);
		return setKeys();
	}

	public int setKeysBySqlId(String sqlId, HttpSession session) throws SQLException {
		queryBySqlID(sqlId, session);
		return setKeys();
	}

	public int setKeysBySqlId(String sqlId, String param, HttpSession session) throws SQLException {
		queryBySqlIDWithParam(sqlId, param, session);
		return setKeys();
	}

	public int setKeysBySqlId(String sqlId, String[] params, HttpSession session) throws SQLException {
		queryBySqlIDWithParam(sqlId, params, session);
		return setKeys();
	}

	public int setKeyValuesBySqlId(String sqlId, String[] params, HttpSession session) throws SQLException {
		queryBySqlIDWithParam(sqlId, params, session);
		return setKeys();
	}

	public String formatToXml(int pageLines, String lexiconID) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		// 构造主表Delta
		buffer.append("<xml id=\"mDelta\">\n");
		buffer.append(formatDeltaToXML2(rs));
		buffer.append("</xml>\n");
		buffer.append("<xml id=\"mMeta\">\n");
		buffer.append(formatMetaDataToXML(rs, lexiconID));
		buffer.append("</xml>\n");
		buffer.append("<xml id=\"mData\">\n");
		buffer.append(formatResultToXML2(rs, pageLines));
		buffer.append("</xml>\n");
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatToXml(int pageLines, String lexiconID, String datasetName) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		// 构造主表Delta
		buffer.append("<xml id=\"" + datasetName + "Delta\">\n");
		buffer.append(formatDeltaToXML2(rs));
		buffer.append("</xml>\n");
		buffer.append("<xml id=\"" + datasetName + "Meta\">\n");
		buffer.append(formatMetaDataToXML(rs, lexiconID));
		buffer.append("</xml>\n");
		buffer.append("<xml id=\"" + datasetName + "Data\">");
		buffer.append(formatResultToXML2(rs, pageLines));
		buffer.append("</xml>\n");
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatToXml(String lexiconID) throws SQLException {
		return formatToXml(0, lexiconID);
	}

	public String formatToXml(String lexiconID, String datasetName) throws SQLException {
		return formatToXml(0, lexiconID, datasetName);
	}

	public String showToXml(String datasetName) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<xml id=\"" + datasetName + "\">\n");
		buffer.append(formatResultToXMLEx("") + "\n");
		buffer.append("</xml>\n");
		return buffer.toString();
	}

	public String formatToXmlOnlyData(String datasetName) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<xml id=\"" + datasetName + "\">\n");
		buffer.append(formatResultToXMLOnlyData("") + "\n");
		buffer.append("</xml>\n");
		return buffer.toString();
	}

	public String formatToXmlData(String datasetName) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<xml id=\"" + datasetName + "\">\n");
		buffer.append(formatResultToXMLData("", true) + "\n");
		buffer.append("</xml>\n");
		return buffer.toString();
	}

	public String formatToXmlOnlyData(String datasetName, int pagesize) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<xml id=\"" + datasetName + "\">\n");
		buffer.append(formatResultToXMLOnlyData("", pagesize) + "\n");
		buffer.append("</xml>\n");
		return buffer.toString();
	}

	public String formatToXmlEx(int pageLines, String sqlId, String lexiconID, boolean useCache, String datasetName)
			throws SQLException {
		StringBuffer buffer = new StringBuffer();
		// 构造主表Delta
		CChildTree ccTree = CCache.getMData(lexiconID, sqlId);
		if (ccTree != null)
			buffer.append(ccTree.getValue());
		else {
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("<xml id=\"" + datasetName + "Delta\">\n");
			buffer2.append(formatDeltaToXML2(rs));
			buffer2.append("</xml>\n");
			CCache.cacheMData(lexiconID, sqlId, buffer2.toString());
			buffer.append(buffer2.toString());
		}

		ccTree = CCache.getMMeta(lexiconID, sqlId);
		if (ccTree != null)
			buffer.append(ccTree.getValue());
		else {
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("<xml id=\"" + datasetName + "Meta\">\n");
			buffer2.append(formatMetaDataToXML(rs, lexiconID));
			buffer2.append("</xml>\n");
			CCache.cacheMMeta(lexiconID, sqlId, buffer2.toString());
			buffer.append(buffer2.toString());
		}

		ccTree = CCache.getMDelta(lexiconID, sqlId);
		if (useCache && ccTree != null)
			buffer.append(ccTree.getValue());
		else {
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("<xml id=\"" + datasetName + "Data\">");
			buffer2.append(formatResultToXML2(rs, pageLines));
			buffer2.append("</xml>\n");
			if (useCache)
				CCache.cacheMDelta(lexiconID, sqlId, buffer2.toString());
			buffer.append(buffer2.toString());
		}
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatToXmlEx(String sqlId, String lexiconID, boolean useCache) throws SQLException {
		return formatToXmlEx(0, sqlId, lexiconID, useCache, "m");
	}

	public String formatToXmlEx(String sqlId, String lexiconID, boolean useCache, String datasetName)
			throws SQLException {
		return formatToXmlEx(0, sqlId, lexiconID, useCache, datasetName);
	}

	// 使用SqlID执行SQL
	public int updateBySqlID(String SqlID, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return -1;
		String pSql = getContraint(SqlID, "", session);
		return executeUpdate(pSql);
	}

	// 直接使用Sql语句执行SQL
	public int updateBySql(String Sql, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return -1;
		return executeUpdate(replaceSessionParam(Sql, session));
	}

	public int updateBySqlWithParam(String Sql, String[] Params, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return -1;
		for (int i = 0; i < Params.length - 1; i++)
			Params[i] = Params[i].replace("'", "''");
		Sql = CUtil.formatStr(Sql, Params, "%s");
		Sql = replaceSessionParam(Sql, session);
		return executeUpdate(Sql);
	}

	public int updateBySqlWithParamInner(String Sql, String[] Params) throws SQLException {
		for (int i = 0; i < Params.length - 1; i++)
			Params[i] = Params[i].replace("'", "''");
		Sql = CUtil.formatStr(Sql, Params, "%s");
		return executeUpdate(Sql);
	}

	public int updateBySqlWithParamInner(String Sql, String Param) throws SQLException {
		String[] Params = new String[1];
		Params[0] = Param.replace("'", "''");
		Sql = CUtil.formatStr(Sql, Params, "%s");
		return executeUpdate(Sql);
	}

	// 带参数的SqlID执行
	public int updateBySqlIDWithParam(String SqlID, String[] Params, HttpSession session) throws SQLException {
		if (!sessionValidated(session))
			return -1;
		for (int i = 0; i < Params.length - 1; i++)
			Params[i] = Params[i].replace("'", "''");

		String pSql = getContraint(SqlID, Params, session);
		pSql = replaceSessionParam(pSql, session);
		return executeUpdate(pSql);
	}

	public int updateBySqlIDWithParamInner(String SqlID, String[] Params) throws SQLException {
		for (int i = 0; i < Params.length - 1; i++)
			Params[i] = Params[i].replace("'", "''");

		String pSql = getContraint(SqlID, Params, null);
		return executeUpdate(pSql);
	}

	public int updateBySqlIDWithParam(String SqlID, String Param, HttpSession session) throws SQLException {
		String[] Params = new String[1];
		Params[0] = Param.replace("'", "''");
		return updateBySqlIDWithParam(SqlID, Params, session);
	}

	public int updateBySqlIDWithParamInner(String SqlID, String Param) throws SQLException {
		String[] Params = new String[1];
		Params[0] = Param.replace("'", "''");
		return updateBySqlIDWithParamInner(SqlID, Params);
	}

	public String formatToXml9OnlyData(int fixedCol, int keyIndex) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		_DeltaBuf = new StringBuffer();
		_MetaBuf = new StringBuffer();

		// 构造主表Delta
		buffer.append("<xml id=\"BlueC\">");
		buffer.append(formatResultToXML9(rs, fixedCol, keyIndex, ""));
		buffer.append("</xml>\n");
		return buffer.toString();
	}

	public String formatToXml9(int fixedCol, int keyIndex, String lexiconID, String datasetName) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		_DeltaBuf = new StringBuffer();
		_MetaBuf = new StringBuffer();

		// 构造主表Delta
		buffer.append("<xml id=\"" + datasetName + "Data\">");
		buffer.append(formatResultToXML9(rs, fixedCol, keyIndex, ""));
		buffer.append("</xml>\n");
		buffer.append("<xml id=\"" + datasetName + "Delta\">\n");
		buffer.append(formatDeltaToXML9(rs, fixedCol));
		buffer.append("</xml>\n");
		buffer.append("<xml id=\"" + datasetName + "Meta\">\n");
		buffer.append(formatMetaDataToXML9(rs, fixedCol));
		buffer.append("</xml>\n");
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatToXml9(int fixedCol, int keyIndex, String lexiconID) throws SQLException {
		return formatToXml9(fixedCol, keyIndex, lexiconID, "m");
	}

	public String formatToXml9(int fixedCol, int keyIndex) throws SQLException {
		return formatToXml9(fixedCol, keyIndex, "", "m");
	}

	public String formatToXml10(int fixedCol, String[] keyField, String lexiconID, String datasetName)
			throws SQLException {
		StringBuffer buffer = new StringBuffer();

		// 构造主表Delta
		try {
			buffer.append("<xml id=\"" + datasetName + "Data\">");
			buffer.append(formatResultToXML10(rs, fixedCol, keyField, ""));
			buffer.append("</xml>\n");
			buffer.append("<xml id=\"" + datasetName + "Delta\">\n");
			buffer.append(formatDeltaToXML10(rs, fixedCol));
			buffer.append("</xml>\n");
			buffer.append("<xml id=\"" + datasetName + "Meta\">\n");
			buffer.append(formatMetaDataToXML10(rs, fixedCol));
			buffer.append("</xml>\n");
		} finally {
			closeConn();
		}
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatToXml10OnlyData(int fixedCol, String[] keyField) throws SQLException {
		StringBuffer buffer = new StringBuffer();

		// 构造主表Delta
		try {
			buffer.append("<xml id=\"BlueC\">");
			buffer.append(formatResultToXML10(rs, fixedCol, keyField, ""));
			buffer.append("</xml>\n");
		} finally {
			closeConn();
		}
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatToXml10(int fixedCol, String[] keyField, String lexiconID) throws SQLException {
		return formatToXml10(fixedCol, keyField, lexiconID, "m");
	}

	public String formatToXml10(int fixedCol, String[] keyField) throws SQLException {
		return formatToXml10(fixedCol, keyField, "", "m");
	}

	public String formatToXml10(int fixedCol, String keyField, String lexiconID, String datasetName)
			throws SQLException {
		String[] keyFields = { keyField };
		return formatToXml10(fixedCol, keyFields, lexiconID, datasetName);
	}

	public String formatToXml10(int fixedCol, String keyField, String lexiconID) throws SQLException {
		return formatToXml10(fixedCol, keyField, lexiconID, "m");
	}

	public String formatToXml10(int fixedCol, String keyField) throws SQLException {
		return formatToXml10(fixedCol, keyField, "", "m");
	}

	public String formatToXml10(int fixedCol, int keyField, String lexiconID, String datasetName) throws SQLException {
		// 构造主表Delta
		ResultSetMetaData mData = rs.getMetaData();
		String[] keyFields = { mData.getColumnName(keyField) };
		return formatToXml10(fixedCol, keyFields, lexiconID, datasetName);
	}

	public String formatToXml10(int fixedCol, int keyField, String lexiconID) throws SQLException {
		return formatToXml10(fixedCol, keyField, lexiconID, "m");
	}

	public String formatToXml10(int fixedCol, int keyField) throws SQLException {
		return formatToXml10(fixedCol, keyField, "", "m");
	}

	public String resultSetToXml(String lexiconID) {
		StringBuilder buffer = new StringBuilder();
		try {
			// 构造主表Delta
			buffer.append("<xml id=\"innerData\">" + _XmlCr);
			buffer.append("<delta>" + _XmlCr);
			buffer.append(formatDeltaToXML2(rs));
			buffer.append("</delta>\n");
			buffer.append("<mmeta>" + _XmlCr);
			buffer.append(formatMetaDataToXML(rs, lexiconID));
			buffer.append("</mmeta>\n");
			buffer.append("<mdata>" + _XmlCr);
			buffer.append(formatResultToXML2(rs, 0));
			buffer.append("</mdata>\n");
			buffer.append("</xml>");
		} catch (Exception ex) {
		}
		// 构造主表MetaData
		return buffer.toString();
	}

	public String formatMetaToDataSet(String tablename) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<xml id=\"" + tablename + "\">" + _XmlCr);
		try {
			queryBySqlInner("select * from " + tablename + " where 1>2");
			buffer.append(formatMetaToDataSet());
		} catch (Exception ex) {

		} finally {
			closeConn();
		}
		buffer.append("</xml>");
		return buffer.toString();
	}

	public String keyCodeToJSArray() {
		StringBuffer buffer = new StringBuffer();
		if (__keyCodeValues != null) {
			int keyLen = __keyCodeValues.length;
			String spChar = "";
			buffer.append("[");
			for (int i = 0; i < keyLen; i++) {
				buffer.append(spChar);
				buffer.append("\"" + CUtil.NVL(__keyCodeValues[i]) + "\"");
				spChar = ",";
			}
			buffer.append("]");
		}
		return buffer.toString();
	}

	public void setRequest(HttpServletRequest request) {
		prequest = request;
	}

	public void initRequest(HttpServletRequest request, HttpServletResponse response) {
		prequest = request;
		__Request = request;
		__Response = response;
		__Session = request.getSession();

	}

	public void initRequest(HttpServletRequest request) {
		prequest = request;
		__Request = request;
		__Session = request.getSession();

	}

	public boolean varExists(String varName) {
		return prequest.getParameter(varName) != null;
	}

	protected void outPrint(String str) {
		try {
			PrintWriter out = __Response.getWriter();
			out.print(str);
			out.close();
		} catch (Exception ex) {
		}
	}

	protected void outPrintln(String str) {
		try {
			PrintWriter out = __Response.getWriter();
			out.println(str);
			out.close();
		} catch (Exception ex) {
		}
	}

	protected void outln(String str) {
		try {
			__Response.reset();
			ServletOutputStream out = __Response.getOutputStream();
			out.println(str);
			out.close();
		} catch (Exception ex) {
		}
	}

	/**
	 * 获取请求变量的值
	 * 
	 * @param 变量名称
	 * @param 限定的最大值
	 * @return 返回结果
	 * @throws Exception
	 */
	public String request(String varName, int maxLength) throws Exception {
		String Result = null;
		try {
			if (__Request.getParameter(varName) != null) {
				Result = __Request.getParameter(varName).toString();
				if (maxLength > 0 && Result.length() > maxLength)
					Result = Result.substring(0, maxLength);
			}
		} catch (Exception ex) {
			throw new SQLException("获取请求变量失败:\n " + ex.getMessage());
		}
		return Result;
	}

	public String request(String varName) throws Exception {
		return request(varName, 0);
	}

	public String request(String varName, String defaultValue) throws Exception {
		String result = request(varName, 0);
		return (result != null) ? result : defaultValue;
	}

	public int requestInt(String varName) throws Exception {
		return Integer.parseInt(request(varName, 0));
	}

	public int requestInt(String varName, int defaultValue) throws Exception {
		String result = request(varName, 0);
		return (result != null) ? Integer.parseInt(request(varName, 0)) : defaultValue;
	}

	public float requestFloat(String varName) throws Exception {
		return Float.parseFloat(request(varName, 0));
	}

	public float requestFloat(String varName, float defaultValue) throws Exception {
		String result = request(varName, 0);
		return (result != null) ? Float.parseFloat(request(varName, 0)) : defaultValue;
	}

	public boolean sessionAttrIsExists(String Attr) {
		return (__Session != null) && (__Session.getAttribute(Attr) != null);
	}

	public String sessionAttr(String Attr) {
		if (__Session != null && __Session.getAttribute(Attr) != null) {
			return __Session.getAttribute(Attr).toString();
		}
		return null;
	}

	public int sessionAttrInt(String Attr) {
		String result = sessionAttr(Attr);
		return (result != null) ? Integer.parseInt(result) : 0;
	}

	public String sessionAttr(String Attr, String defaultValue) {
		String result = sessionAttr(Attr);
		return (result == null) ? defaultValue : result;
	}

	public int sessionAttrInt(String Attr, int defaultValue) {
		String result = sessionAttr(Attr);
		return (result == null) ? defaultValue : Integer.parseInt(result);
	}

	public boolean requestParamIsExists(String Param) {
		return (__Request != null) && (__Request.getParameter(Param) != null);
	}

	public String requestParam(String Param) {
		if (__Request != null && __Request.getParameter(Param) != null) {
			return __Request.getParameter(Param).toString();
		}
		return null;
	}

	public String requestParam(String Param, String defaultValue) {
		String result = requestParam(Param);
		return (result == null) ? defaultValue : result;
	}

	public String _R$(String Param) {
		return requestParam(Param);
	}

	public String _R$(String Param, String defaultValue) {
		return requestParam(Param, defaultValue);
	}

	public String __R$(String Param) {
		return requestParam(Param);
	}

	public String __R$(String Param, String defaultValue) {
		return requestParam(Param, defaultValue);
	}
	
	public boolean sessionValidated(HttpServletRequest request) {

		__Session = request.getSession();
		__Request = request;

		return sessionValidated(__Session);
	}

	protected String __bufSql;

	protected String[] __bufField;

	protected String[] __bufDType;

	protected String[] __bufFroms;

	protected String[] vPrcoReturn;

	// private String[] __returnValue;

	private int[] __bufDirection;

	private int __gtag;

	public String sProcResult = "";

	public long iProcResult = 0;

	public double fProcResult = 0;

	/**
	 * 获取要执行的sql语句或者存储过程
	 */
	private boolean getProcSql(String procID) {
		boolean Result = false;
		try {
			executeQuery2("select sql_server,nvl(sql_session,' ') sql_session,"
					+ "       nvl(sql_pdtype,' ') sql_pdtype,nvl(sql_pfrom,' ') sql_pfrom,gtag,nvl(aexec,0)*10+att att "
					+ "from sys_procs where sql_id=?", procID);
			if (rs.next()) {
				Result = true;
				__bufSql = rs.getString(1);
				__bufField = rs.getString(2).split("/");
				__bufDType = rs.getString(3).split("/");
				__bufFroms = rs.getString(4).split("/");
				__gtag = rs.getInt(5);
			} else
				_errorInf = "没有找到" + procID + ".";
		} catch (SQLException ex) {
		}
		return Result;
	}

	/**
	 * 原执行存储过程和UPDATE SQL的方法
	 * 
	 */

	public int executeOracleProc(String procID, String[] params, HttpSession session) {
		int j, result = -1;

		if (getProcSql(procID))
			try {

				if (conn == null || conn.isClosed())
					conn = getConnection();

				if (stmt == null)
					stmt = conn.createStatement();
				CallableStatement cstmt = null;

				// if (_bprintTrack) P(__bufSql);

				switch (__gtag) {
				case 4:
					cstmt = conn.prepareCall("{" + __bufSql + "}");
					cstmt.registerOutParameter(1, Types.INTEGER);
					cstmt.registerOutParameter(2, Types.VARCHAR);
					for (j = 0; j < params.length; j++) {
						switch (Integer.parseInt(__bufDType[j + 2])) {
						case 1:
							cstmt.setString(j + 3, params[j]);
							break;
						case 2:
							cstmt.setLong(j + 3, Long.parseLong(params[j]));
							break;
						case 3:
							cstmt.setDouble(j + 3, Double.parseDouble(params[j]));
							break;
						case 4:
							cstmt.setTimestamp(j + 3, Timestamp.valueOf(params[j]));
							break;
						}
					}
					cstmt.executeQuery();
					RecordCount = cstmt.getInt(1);

					if (RecordCount != 0)
						RecordCount = 0;
					else
						RecordCount = 1;

					_errorInf = cstmt.getString(2);
					break;
				case 5:
					pstmt = conn.prepareStatement(__bufSql);
					for (j = 0; j < params.length; j++) {
						switch (Integer.parseInt(__bufDType[j])) {
						case 1:
							pstmt.setString(j + 1, params[j]);
							break;
						case 2:
							pstmt.setLong(j + 1, Long.parseLong(params[j]));
							break;
						case 3:
							pstmt.setDouble(j + 1, Double.parseDouble(params[j]));
							break;
						case 4:
							pstmt.setTimestamp(j + 1, Timestamp.valueOf(params[j]));
							break;
						}
					}
					RecordCount = pstmt.executeUpdate();
					break;
				}
				if (RecordCount > 0)
					result = 0;
				else
					result = -1;
				if (cstmt != null)
					cstmt.close();
			} catch (SQLException ex) {
				_errorInf = ex.getMessage();
				P(_errorInf);
				result = -1;
				// throw new SQLException("更新数据产生异常：\n " + _errorInf);
			} finally {
				if (_errorInf == null)
					_errorInf = "未知的错误.";
				closeConn();
			}
		return result;
	}

	/**
	 * 
	 * @param procName
	 * @param vparams
	 * @param paramType
	 *            输入参数类型
	 * @param paramOut
	 *            输出参数类型
	 * @param execAtt
	 *            4-执行存储过程,5-执行SQL
	 * @return
	 */
	public int executeOracleProc(String procName, String[] vparams, int[] paramType, int[] paramOut, int execAtt) {
		int j, oj = 3, rj = 2, result = -1;
		int paramCount = paramType.length;

		if (paramOut != null) {
			oj += paramOut.length;
			rj += paramOut.length; // 返回参数个数
			__procReturn = new String[paramOut.length];
		}

		vPrcoReturn = new String[rj];

		__gtag = execAtt;

		try {

			if (conn == null || conn.isClosed())
				conn = getConnection();

			if (stmt == null)
				stmt = conn.createStatement();
			CallableStatement cstmt = null;

			switch (__gtag) {
			case 4:
				__bufSql = "call " + procName + "(" + CUtil.combinate("?", ",", paramCount + rj) + ")";

				cstmt = conn.prepareCall("{" + __bufSql + "}");
				cstmt.registerOutParameter(1, Types.INTEGER);
				cstmt.registerOutParameter(2, Types.VARCHAR);
				for (j = 2; j < rj; j++) {
					switch (paramOut[j - 2]) {
					case 1:
						cstmt.registerOutParameter(j + 1, Types.VARCHAR);
						break;
					case 2:
						cstmt.registerOutParameter(j + 1, Types.INTEGER);
						break;
					case 3:
						cstmt.registerOutParameter(j + 1, Types.DOUBLE);
						break;
					}
				}

				for (j = 0; j < paramCount; j++) {
					switch (paramType[j]) {
					case 1:
						cstmt.setString(j + oj, vparams[j]);
						break;
					case 2:
						cstmt.setLong(j + oj, Long.parseLong(vparams[j]));
						break;
					case 3:
						cstmt.setDouble(j + oj, Double.parseDouble(vparams[j]));
						break;
					case 4:
						cstmt.setTimestamp(j + oj, Timestamp.valueOf(vparams[j]));
						break;
					}
				}
				cstmt.executeQuery();
				RecordCount = cstmt.getInt(1);
				_errorInf = cstmt.getString(2);

				vPrcoReturn[0] = RecordCount + "";
				vPrcoReturn[1] = _errorInf;
				for (j = 2; j < rj; j++) {
					switch (paramOut[j - 2]) {
					case 1:
						vPrcoReturn[j] = cstmt.getString(j + 1);
						break;
					case 2:
						vPrcoReturn[j] = cstmt.getInt(j + 1) + "";
						break;
					case 3:
						vPrcoReturn[j] = CUtil.big(cstmt.getDouble(j + 1));
						break;
					}
					__procReturn[j - 2] = vPrcoReturn[j];
				}

				RecordCount = (RecordCount != 0) ? 0 : 1;
				break;
			case 5:
				__bufSql = procName;
				pstmt = conn.prepareStatement(__bufSql);
				for (j = 0; j < paramCount; j++) {
					switch (paramType[j]) {
					case 1:
						pstmt.setString(j + 1, vparams[j]);
						break;
					case 2:
						pstmt.setLong(j + 1, Long.parseLong(vparams[j]));
						break;
					case 3:
						pstmt.setDouble(j + 1, Double.parseDouble(vparams[j]));
						break;
					case 4:
						pstmt.setTimestamp(j + 1, Timestamp.valueOf(vparams[j]));
						break;
					}
				}
				RecordCount = pstmt.executeUpdate();
				break;
			}

			result = (RecordCount > 0) ? 0 : -1;
			if (cstmt != null)
				cstmt.close();
		} catch (SQLException ex) {
			_errorInf = "E:" + ex.getMessage();
			result = -1;
			P(_errorInf);
			// throw new SQLException("更新数据产生异常：\n " + _errorInf);
		} finally {
			if (_errorInf == null)
				_errorInf = "未知的错误.";
			closeConn();
		}
		return result;
	}

	public int executeOracleProc(String procName, String vparam, int vType, int[] paramOut, int execAtt) {
		String[] vparams = { vparam };
		int[] paramType = { vType };
		return executeOracleProc(procName, vparams, paramType, paramOut, execAtt);
	}

	/**
	 * 执行SQL SERVER存储过程
	 * 
	 * @param procName
	 *            String 过程名称
	 * @param params
	 *            String[] 过程参数
	 * @param session
	 *            HttpSession
	 * @return int
	 */
	public int executeMsSqlProc(String procName, String[] params) {
		int result = 0, paramCount = 0, i = 1, nout = 1;
		try {
			// 求参数个数
			executeQuery2("select c=count(*) " + "from sysobjects a,syscolumns b " + "where a.name=? and a.id=b.id",
					procName);
			if (rs.next())
				paramCount = rs.getInt(1);
			if (paramCount >= 0) {
				__bufDType = new String[paramCount + 1];
				__bufField = new String[paramCount + 1];
				// __returnValue = new String[paramCount + 1];
				__bufDirection = new int[paramCount + 1];

				__bufSql = "? = call " + procName + "(" + CUtil.combinate("?", ",", paramCount) + ")";

				__bufDType[0] = "2";
				__bufField[0] = "@Result";
				__bufDirection[0] = 1;

				executeQuery2("select b.name,b.xtype,b.isoutparam,b.length " + "from sysobjects a,syscolumns b "
						+ "where a.name=? and a.id=b.id", procName);
				while (rs.next()) {
					__bufField[i] = rs.getString("NAME");
					switch (rs.getInt("XTYPE")) {
					case 35:
					case 175:
					case 167:
					case 231:
						__bufDType[i] = "1";
						break;
					case 48:
					case 52:
					case 56:
						__bufDType[i] = "2";
						break;
					case 60:
					case 62:
						__bufDType[i] = "3";
						break;
					case 58:
					case 61:
						__bufDType[i] = "4";
					}
					__bufDirection[i] = rs.getInt("ISOUTPARAM");
					i++;
				}
			}
		} catch (SQLException ex) {
			E("异常：" + ex.getMessage());
		} finally {
			closeConn();
		}

		try {
			if (conn == null || conn.isClosed())
				conn = getConnection();

			CallableStatement cstmt = null;

			// if (_bprintTrack) P(__bufSql);

			cstmt = conn.prepareCall("{" + __bufSql + "}");

			cstmt.registerOutParameter(1, Types.INTEGER);
			int j = 2;

			for (i = 0; i < paramCount; i++, j++) {
				if (__bufDirection[i + 1] == 1) {
					switch (Integer.parseInt(__bufDType[i + 1])) {
					case 1:
						cstmt.registerOutParameter(j, Types.VARCHAR);
						if (i < params.length)
							cstmt.setString(j, params[i]);
						break;
					case 2:
						cstmt.registerOutParameter(j, Types.INTEGER);
						break;
					case 3:
						cstmt.registerOutParameter(j, Types.DOUBLE);
						break;
					case 4:
						cstmt.registerOutParameter(j, Types.TIMESTAMP);
						break;
					}
					nout++;
				} else
					switch (Integer.parseInt(__bufDType[i + 1])) {
					case 1:
						cstmt.setString(j, params[i]);
						break;
					case 2:
						cstmt.setLong(j, Long.parseLong(params[i]));
						break;
					case 3:
						cstmt.setDouble(j, Double.parseDouble(params[i]));
						break;
					case 4:
						cstmt.setTimestamp(j, Timestamp.valueOf(params[i]));
						break;
					}
			}

			cstmt.executeUpdate();
			result = cstmt.getInt(1);
			RecordCount = result;
			__procReturn = new String[nout + 1];
			_errorInf = "";
			j = 0;

			for (i = 1; i <= paramCount; i++)
				if (__bufDirection[i] == 1) {
					_errorInf += " " + __bufField[i].substring(1) + "=";
					switch (Integer.parseInt(__bufDType[i])) {
					case 1:
						sProcResult = cstmt.getString(i + 1);
						_errorInf = _errorInf + "\"" + sProcResult + "\"";
						break;
					case 2:
						iProcResult = cstmt.getLong(i + 1);
						_errorInf = _errorInf + "\"" + iProcResult + "\"";
						break;
					case 3:
						fProcResult = cstmt.getDouble(i + 1);
						_errorInf = _errorInf + "\"" + fProcResult + "\"";
						break;
					case 4:
						_errorInf = _errorInf + "\"" + cstmt.getTimestamp(i + 1) + "\"";
						break;
					}
					__procReturn[j++] = cstmt.getString(i + 1);
				}

			if (cstmt != null)
				cstmt.close();

		} catch (SQLException ex) {
			_errorInf = ex.getMessage();
			result = -1;
		} finally {
			if (_errorInf == null || _errorInf.equals(""))
				_errorInf = "未知的错误.";
			closeConn();
		}

		return result;
	}

	public int executeMsSqlProc(String procName, String[] params, HttpSession session) {
		return executeMsSqlProc(procName, params);
	}

	/**
	 * 执行存储过程
	 * 
	 * @param request
	 * @return
	 */
	public String executeProc(HttpServletRequest request) {
		String infMsg = "0";
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ServletInputStream in = request.getInputStream();
			Document doc = builder.parse(in); // 建立文档
			Node node = doc.getFirstChild().getFirstChild().getNextSibling();

			node = node.getFirstChild().getNextSibling();

			node = node.getNextSibling().getNextSibling();
			String procid = node.getFirstChild().getNodeValue();

			node = node.getNextSibling().getNextSibling();

			NamedNodeMap map = node.getAttributes();

			int pcount = map.getLength(); // 参数个数

			String[] params = new String[pcount];

			for (int i = 0; i < pcount; i++) {
				params[i] = map.item(i).getNodeValue();
				if (params[i] == null)
					params[i] = "";
			}
			if (_databaseType == 0) {
				if (executeOracleProc(procid, params, request.getSession()) == 0)
					infMsg = "<xml><ds><d>ok</d></ds></xml>";
				else
					infMsg = "<xml><errors><error>" + _errorInf + "</error></errors></xml>";
			} else {
				if (executeMsSqlProc(procid, params, request.getSession()) == 0)
					infMsg = "<xml><ds><d>ok</d><r" + _errorInf + "></r></ds></xml>";
				else
					infMsg = "<xml><errors><error>" + _errorInf + "</error></errors></xml>";
			}
		} catch (Exception pcException) {
			// System.out.println(pcException.getMessage());
			infMsg = "<xml><errors><error>执行存储过程产生异常</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public int executeProc(String procName, String[] params, HttpSession session) {
		int result = 0;
		if (_databaseType == 0) {
			result = executeOracleProc(procName, params, session);
		} else {
			result = executeMsSqlProc(procName, params, session);
		}
		return result;
	}

	public boolean createArray(String sql, HttpSession session) {
		if (!sessionValidated(session))
			return false;
		String spChar = "";
		try {
			executeQuery(sql);
			StringBuffer buffer1 = new StringBuffer();
			StringBuffer buffer2 = new StringBuffer();

			while (rs.next()) {
				buffer1.append(spChar);
				buffer1.append("\"" + CUtil.NVL(rs.getString(1)) + "\"");
				buffer2.append(spChar);
				buffer2.append("\"" + CUtil.NVL(rs.getString(2)) + "\"");
				spChar = ",";
			}
			__arrayNo = buffer1.toString();
			__arrayName = buffer2.toString();
		} catch (Exception ex) {
			P(ex.getMessage());
		} finally {
			closeConn();
		}
		return true;
	}

	public boolean createArray(String sql, String param, HttpSession session) {
		return createArray(CUtil.formatStr(sql, param, "%s"), session);
	}

	public boolean createArrayBySqlId(String sqlid, String param, HttpSession session) {
		String sql = "";
		try {
			sql = getContraint(sqlid, param, null);
		} catch (Exception ex) {

		}
		return createArray(sql, session);
	}

	public void setSelectOption(String vOption) {
		__vOption = vOption;
	}

	private String getOptionsValue() throws SQLException {
		String blanks = "";
		boolean hasLevels = false;
		hasLevels = rs.getMetaData().getColumnCount() > 2;
		StringBuffer buffer = new StringBuffer();
		while (next()) {
			String checked = __vOption.equals(getString(1)) ? " selected" : "";
			buffer.append("<OPTION value=\"" + getString(1) + "\"" + checked + ">");
			if (hasLevels) {
				for (int i = 1; i < getInt(3); i++)
					blanks += "&nbsp;&nbsp;";
				buffer.append(blanks);
				blanks = "";
			}
			buffer.append(getString(2) + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getOptionsValue(String[] Ids, String[] Values, String defaultId) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < Ids.length; i++) {
			String checked = Ids[i].equals(defaultId) ? " selected" : "";
			buffer.append("<OPTION value=\"" + Ids[i] + "\"" + checked + ">");
			buffer.append(Values[i] + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getOptionsValue(String[] Ids, String defaultId) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < Ids.length; i++) {
			String checked = Ids[i].equals(defaultId) ? " selected" : "";
			buffer.append("<OPTION value=\"" + Ids[i] + "\"" + checked + ">");
			buffer.append(Ids[i] + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getOptionsValue(int[] Ids, String[] Values, int defaultId) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < Ids.length; i++) {
			String checked = (Ids[i] == defaultId) ? " selected" : "";
			buffer.append("<OPTION value=\"" + Ids[i] + "\"" + checked + ">");
			buffer.append(Values[i] + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getOptionsValue(int[] Ids, int defaultId, String unit) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < Ids.length; i++) {
			String checked = (Ids[i] == defaultId) ? " selected" : "";
			buffer.append("<OPTION value=\"" + Ids[i] + "\"" + checked + ">");
			buffer.append(Ids[i] + unit + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getOptionsValue(int[] Ids, int defaultId) {
		return getOptionsValue(Ids, defaultId, "");
	}

	public String getOptionsValue(int start, int end, int defaultId, String unit) {
		StringBuffer buffer = new StringBuffer();
		for (int i = start; i <= end; i++) {
			String checked = (i == defaultId) ? " selected" : "";
			buffer.append("<OPTION value=\"" + i + "\"" + checked + ">");
			buffer.append(i + unit + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getOptionsValue(int start, int end, int defaultId) {
		return getOptionsValue(start, end, defaultId, "");
	}

	public String getTimeOptionsValue(int start, int end, int defaultId) {
		StringBuffer buffer = new StringBuffer();
		for (int i = start; i <= end; i++) {
			String checked = (i == defaultId) ? " selected" : "";
			String value = (100 + i) + "";
			buffer.append("<OPTION value=\"" + value.substring(1) + "\"" + checked + ">");
			buffer.append(i + "</OPTION>\n");
		}
		return buffer.toString();
	}

	public String getMonthOptionsValue(int off, int count, int defaultOff) {

		String defaultyf = "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		if (defaultOff != 0) {
			calendar.add(Calendar.MONTH, defaultOff);
		}
		int Y = calendar.get(Calendar.YEAR);
		int M = 1 + calendar.get(Calendar.MONTH);

		defaultyf = Y + Integer.toString(100 + M).substring(1);

		if (off - defaultOff != 0)
			calendar.add(Calendar.MONTH, off - defaultOff);
		Y = calendar.get(Calendar.YEAR);
		M = 1 + calendar.get(Calendar.MONTH);

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < count; i++) {
			String yf = Y + Integer.toString(100 + M).substring(1);
			String checked = (yf.equals(defaultyf)) ? " selected" : "";
			buffer.append("<OPTION value=\"" + yf + "\"" + checked + ">");
			buffer.append(Y + "年" + M + "月</OPTION>\n");
			M++;
			if (M > 12) {
				M = 1;
				Y++;
			}
		}
		return buffer.toString();
	}

	public String getOptions(String sql, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			executeQuery(sql);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptions(String sql, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptions(sql, session);
	}

	public String getOptionsBySql(String sql, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			executeQuery(sql);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptionsBySql(String sql, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptionsBySql(sql, session);
	}

	public String getOptionsBySql(String sql, String param, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			queryBySqlInner(sql, param);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptionsBySql(String sql, String param, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptionsBySql(sql, param, session);
	}

	public String getOptionsBySql(String sql, String[] params, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			queryBySqlInner(sql, params);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptionsBySql(String sql, String[] params, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptionsBySql(sql, params, session);
	}

	public String getOptionsBySqlId(String sqlId, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			queryBySqlIDInner(sqlId);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptionsBySqlId(String sqlId, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptionsBySqlId(sqlId, session);
	}

	public String getOptionsBySqlId(String sqlId, String param, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			queryBySqlIDWithParamInner(sqlId, param);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptionsBySqlId(String sqlId, String param, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptionsBySqlId(sqlId, param, session);
	}

	public String getOptionsBySqlId(String sqlId, String[] params, HttpSession session) {
		if (session != null && !sessionValidated(session))
			return "";
		String sResult = "";
		try {
			queryBySqlIDWithParamInner(sqlId, params);
			sResult = getOptionsValue();
		} catch (Exception ex) {
		} finally {
			closeConn();
		}
		return sResult;
	}

	public String getOptionsBySqlId(String sqlId, String[] params, HttpSession session, String defaultOp) {
		setSelectOption(defaultOp);
		return getOptionsBySqlId(sqlId, params, session);
	}

}
