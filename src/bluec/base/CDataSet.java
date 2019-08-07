package bluec.base;

/**
 * CDataSet
 * 数据管理基本类，提供数据连接操作，基本的查询
 * 如果用户指定的值大于默认值，就采用指定的值
 *
 * @author Handi Lan
 * @version 1.0 April 25, 2003
 */
import java.sql.*;

import javax.servlet.http.*;

import oracle.jdbc.OracleTypes;

import java.util.*;

import java.util.logging.Logger;
import java.lang.System;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import bluec.base.CConnect;
import bluec.base.CUtil;
import bluec.base.CInitParam;
import bluec.base.cache.*;

public class CDataSet {

	protected Connection conn = null;

	protected ResultSet rs = null;

	protected Statement stmt = null;

	protected PreparedStatement pstmt = null;

	protected CallableStatement cstmt = null;

	protected int Result = -1;

	protected int RecordCount = 0; // 最后一次SQL语句影响的记录数

	protected boolean _rsEof = true;

	protected int _databaseType = CInitParam.databaseType;

	protected String strResult = "error"; // 最后的结果字符串

	protected String _errorInf = ""; // 最后的错误信息

	protected String[] _paramType = null; // 参数数据类型

	protected String[] _paramSVar = null; // 来自session参数名称

	protected String[] _paramSMark = null; // 参数是否来自session

	protected int _sqlType = 5; // sql类型，0-参数为%s,1-参数为?,2存储过程

	protected String _procName = ""; // 需要执行的存储过程名称

	protected static boolean _bprintTrack = CInitParam.bprintTrack;

	protected static String _XmlCr = CInitParam.XmlCr; // XML节点间是否添加回车换行符

	protected boolean _HasFieldInfo = false;// //查询数据是否包含字段描述信息

	protected String calTotalCtlField = "";

	protected boolean hasCalTotalCtlField = false;

	static String[] __upFormat = { "TO_DATE(%s,'yyyy.mm.dd hh24:mi:ss')", "%s" };

	static String[] __upNvl = { "nvl(%s,' ')", "isnull(%s,'')" };

	protected static Logger logger = Logger.getLogger(CDataSet.class.getName());

	//
	protected StringBuffer _DeltaBuf = null;

	protected StringBuffer _MetaBuf = null;

	protected String[] __keyCodeValues = null;

	protected String[] __keyNameValues = null;

	protected String[] __keyLevelValues = null;

	protected boolean __zeroToSpace = false;

	private boolean[] __isNumber = null;

	private double[] __fieldTotal = null;

	protected String gm_id = "";

	protected String gu_id = "0";

	public CDataSet() {
		Result = -1;
	}

	public void destroy() {
	}

	protected void P(String msg) {
		if (CInitParam.bprintTrack) {
			System.out.println(CUtil.getOrigTime() + ":\n" + msg);
		}
	}

	protected static void D(String msg) {
		if (CInitParam.isDebug()) {
			System.out.println(CUtil.getOrigTime() + ":\n" + msg);
		}
	}

	protected void E(String msg) {
		System.out.println(CUtil.getOrigTime() + ":" + msg);
	}

	protected void P(String[] msgs) {
		if (CInitParam.bprintTrack) {
			for (String msg : msgs) {
				System.out.println(msg);
			}
		}
	}

	protected void P(String[] msgs,String dh) {
		if (CInitParam.bprintTrack) {
			for (String msg : msgs) {
				System.out.println(msg+dh);
			}
		}
	}
	
	protected static void D(String[] msgs) {
		if (CInitParam.isDebug()) {
			for (String msg : msgs) {
				System.out.println(CUtil.getOrigTime() + ":\n" + msg);
			}
		}
	}

	protected void E(String[] msgs) {
		for (String msg : msgs) {
			System.out.println(CUtil.getOrigTime() + ":\n" + msg);
		}
	}

	protected Connection getConnection() throws SQLException {
		return CConnect.getConnection();
	}
	/**
	 * 关闭所有连接
	 */
	public void closeConn() {
		try {
			if (rs != null)
				rs.close(); // 关闭记录集
			if (stmt != null)
				stmt.close();
			if (pstmt != null)
				pstmt.close();
			if (cstmt != null)
				cstmt.close();

			if (conn != null && (!conn.isClosed()))
				conn.close();
			rs = null;
			pstmt = null;
			stmt = null;
			cstmt = null;
			conn = null;
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		} finally {
		}
	}

	protected int JsonTypeTo(String JsonTypeName) {
		int dataType = 12;
		switch (JsonTypeName) {
		case "Int":
			dataType = 4;
			break;
		}
		return dataType;
	}

	protected String getContraint0(String SqlID, String[] Params) {
		String pSql = "";
		boolean bMark = false;
		if (CInitParam.sqlCache) {
			CSqlTree sqlcache = CCache.getSql(SqlID);
			if (sqlcache != null) {
				pSql = sqlcache.getSqls();
				_sqlType = sqlcache.getSqlType();
				_procName = sqlcache.getProcName();
				_paramType = sqlcache.getParamType();
				_paramSVar = sqlcache.getParamSVar();
				_paramSMark = sqlcache.getParamSVar();
				bMark = true;
			}
		}

		if (!bMark) { // 没有缓存的sql数据,从数据库中读取
			String ppType = "";
			String ppSVar = "";
			String ppFrom = "";
			// 获取SQL语句
			// P("getContraint0:SqlID="+SqlID);
			try {
				executeQuery2("SELECT SQL_SERVER,SQL_PDTYPE,SQL_PFROM,SQL_SESSION,SQLATT,PROCNAME "
						+ "FROM SYS_SQLDS WHERE SQL_ID=?", SqlID);
				if (rs != null && rs.next()) {
					pSql = rs.getString(1);
					ppType = CUtil.NVL(rs.getString(2));
					ppFrom = CUtil.NVL(rs.getString(3));
					ppSVar = CUtil.NVL(rs.getString(4));
					_sqlType = rs.getInt(5);
					_procName = rs.getString(6);

					if (_sqlType != 4) // 不是存储过程语句并且参数个数不为0，设置为?执行方式
						_sqlType = (!ppType.trim().equals("")) ? 3 : 5;

					if (_sqlType == 3) {
						_paramType = ppType.split("/");
						_paramSVar = ppSVar.split("/");
						_paramSMark = ppFrom.split("/");
					}
					if (CInitParam.sqlCache)
						CCache.cacheSql(SqlID, pSql, _sqlType, _procName, _paramType, _paramSVar, _paramSMark);
					bMark = true;
				} else {
					if (SqlID.indexOf("@") == 0) {
						pSql = "select * from " + SqlID.substring(1);
						bMark = true;
					} else if (SqlID.indexOf("#") == 0) {
						pSql = "select * from " + SqlID.substring(1) + " where 1>2";
						bMark = true;
					}
				}
				rs.close();

			} catch (SQLException ex) {
				System.err.println(ex.getMessage());

			} finally {
			}
		}
		return pSql;
	}

	// 检查附加约束条件
	protected String getContraint(String SqlID, String UserID, HttpSession session) throws SQLException {
		String[] params = { UserID };
		return getContraint0(SqlID, params);
	}

	/**
	 * 获取某个字段的值，当字段为null时，返回defaultValue
	 * 
	 * @param _rs
	 *            数据集
	 * @param fieldName
	 *            字段名称
	 * @param defaultValue
	 *            默认的变换值
	 * @return
	 */
	protected String NVL(ResultSet _rs, String fieldName, String defaultValue) throws SQLException {
		try {
			String tStr = _rs.getString(fieldName);
			if (_rs.wasNull())
				tStr = defaultValue;
			else
				tStr = CUtil.replaceXmlSpChar(tStr);
			return tStr;
		} catch (SQLException ex) {
			throw new SQLException("获取数据产生异常：\n " + ex.getMessage());
		}
	}

	private String getBlobField(int fieldIndex) throws SQLException {
		String result = "";

		try {
			byte b[] = rs.getBytes(fieldIndex);
			if (!rs.wasNull() && b.length > 0) {
				result = new String(b, "gb2312");
			}
		} catch (Exception ex) {
			// throw new SQLException(ex.getMessage());
		}

		return result;
	}

	/**
	 * formatResultToXML1 把结果按XML的方式输出
	 * 
	 * @param rs
	 *            查询的结果
	 * @pageLines 每页行数
	 * @return XML格式的数据 <ROWDATA> <ROW RL="0" 字段1="值" ... /> ... </ROWDATA>
	 */
	private String formatResultToXML1(ResultSet rs, int pageLines, String nullIs) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		int nrow = 0;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			RecordCount = 0; // 记录数设置为0
			sResult.append("<RS>" + _XmlCr);
			while (rs.next()) {
				Result = 0; // 设置有数据标记
				RecordCount++; // 记录数增加
				sResult.append("<R RL=\"" + RecordCount + "\" RT=\"1\" RS=\"0\" RR=\"" + RecordCount + "\""); // 记录状态
				// 把每个字段按 <字段名>值</字段名> 的方式组合
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					switch (dataType) {
					/*
					 * case -7: case -6: case -5: case 4: case 5: tStr =
					 * String.valueOf(rs.getLong(i)); break; case 2: case 3:
					 * tStr = String.valueOf(rs.getBigDecimal(i)); break; case
					 * 6: case 7: tStr = String.valueOf(rs.getDouble(i)); break;
					 */
					case -1:
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatResultToXML1)：\n " + ex.getMessage());
						} finally {
						}
						break;
					default:
						tStr = rs.getString(i);
					}
					if (rs.wasNull())
						tStr = nullIs;
					else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
					}
					sResult.append(" F" + i + "=\"" + tStr + "\"");
				}
				sResult.append("/>" + _XmlCr);
				nrow++;
			}
			int blankLines = 0; // 需要保留的空行数
			if (pageLines >= 0) {
				if (RecordCount == 0)
					blankLines = (pageLines == 0) ? 1 : pageLines;
				else if (pageLines > 0) {
					blankLines = pageLines - (RecordCount % pageLines);
				}
			}
			// 增加空行
			while (blankLines > 0) {
				sResult.append("<R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"\""); // 新记录
				// 把每个字段按 <字段名>值</字段名> 的方式组合
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					sResult.append(" F" + i + "=\"" + nullIs + "\"");
				}
				sResult.append("/>" + _XmlCr);
				blankLines--;
			}
			sResult.append("</RS>" + _XmlCr);
		} catch (SQLException ex) {
			E("格式化数据产生异常1：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToXMLEx(String nullIs) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			RecordCount = 0; // 记录数设置为0
			sResult.append("<RS>" + _XmlCr);
			while (rs.next()) {
				RecordCount++;
				sResult.append("  <R RL=\"" + RecordCount + "\" RT=\"0\" RS=\"0\" RR=\"\"");
				// 把每个字段按 <字段名>值</字段名> 的方式组合
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					if (dataType > 0 || dataType < -4)
						tStr = rs.getString(i);
					else {
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatResultToXMLEx)：\n " + ex.getMessage());
						} finally {
						}
					}
					if (rs.wasNull())
						tStr = nullIs;
					else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
					}
					sResult.append(" F" + i + "=\"" + tStr + "\"");
				}
				sResult.append("/>" + _XmlCr);
			}
			if (RecordCount == 0) {
				sResult.append("  <R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"\"");
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					sResult.append(" F" + i + "=\"\"");
				}
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);
		} catch (SQLException ex) {
			E("格式化数据产生异常2：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToJson(String nullIs, boolean bBlankLine) throws SQLException {

		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();
			String[] strArray = new String[fieldCount];

			RecordCount = 0; // 记录数设置为0
			sResult.append("[");
			while (rs.next()) {
				if (RecordCount == 0)
					sResult.append("{");
				else
					sResult.append(",{");
				RecordCount++;

				// 把每个字段按 <字段名>值</字段名> 的方式组合
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					int nScale = mData.getScale(i);
					if (dataType > 0 || dataType < -4)
						tStr = rs.getString(i);
					else {
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatResultToJson)：\n " + ex.getMessage());
						} finally {
						}
					}
					if (rs.wasNull()) {
						// tStr = (nullIs.equals(""))? "null":nullIs;

						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
						case 91:
						case 93:
							tStr = "\"\"";
							break;
						default:
							if (nScale == 0)
								tStr = "0";
							else
								tStr = "0.0";
							break;
						}
					} else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = "\"" + CUtil.replaceXmlSpChar(tStr) + "\"";
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							tStr = "\"" + tStr + "\"";
							break;
						default:
							if (nScale > 0) {
								if (tStr.indexOf(".") < 0)
									tStr = tStr + ".0";
								if (tStr.indexOf(".") == 0)
									tStr = "0" + tStr;
							}
							break;
						}
					}
					strArray[i - 1] = "\"" + mData.getColumnName(i).toLowerCase() + "\":" + tStr;
				}
				sResult.append(CUtil.join(",", strArray));
				sResult.append("}");
			}

			if (RecordCount == 0 && bBlankLine) {
				sResult.append("{");
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					strArray[i - 1] = "\"" + mData.getColumnName(i).toLowerCase() + "\":\"\"";
				}
				sResult.append(CUtil.join(",", strArray));
				sResult.append("}");
			}
			sResult.append("]");
		} catch (SQLException ex) {
			E("格式化数据产生异常2：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToJson() throws SQLException {

		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();
			String[] strArray = new String[fieldCount];

			if (next()) {
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					int nScale = mData.getScale(i);
					if (dataType > 0 || dataType < -4)
						tStr = rs.getString(i);
					else {
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatResultToJson)：\n " + ex.getMessage());
						} finally {
						}
					}
					if (rs.wasNull()) {
						// tStr = "null";
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
						case 91:
						case 93:
							tStr = "\"\"";
							break;
						default:
							if (nScale == 0)
								tStr = "0";
							else
								tStr = "0.0";
							break;
						}
					} else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = "\"" + CUtil.replaceXmlSpChar(tStr) + "\"";
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							tStr = "\"" + tStr + "\"";
							break;
						default:
							if (nScale > 0) {
								if (tStr.indexOf(".") < 0)
									tStr = tStr + ".0";
								if (tStr.indexOf(".") == 0)
									tStr = "0" + tStr;
							}
							break;
						}
					}
					strArray[i - 1] = "\"" + mData.getColumnName(i).toLowerCase() + "\":" + tStr;
				}
			} else {
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					int nScale = mData.getScale(i);
					switch (dataType) {
					case -1: // sqlserver text类型
					case 1:
					case 12:
					case 91:
					case 93:
						tStr = "\"\"";
						break;
					default:
						if (nScale > 0)
							tStr = "0.0";
						else
							tStr = "0";
						break;
					}
					strArray[i - 1] = "\"" + mData.getColumnName(i).toLowerCase() + "\":" + tStr;
				}
			}

			sResult.append(CUtil.join(",", strArray));
		} catch (SQLException ex) {
			E("格式化数据产生异常2：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToJson(String nullIs) throws SQLException {
		return formatResultToJson(nullIs, true);
	}

	/* 生成insert into 语句模板 */
	protected String[] __dynFieldNames = null;
	protected String[] __jsonFieldNames = null;
	protected int[] __dynFieldTypes = null;

	protected String createInsertSql(String tableName, String outFieldName) throws SQLException {

		StringBuffer sResult = new StringBuffer();

		try {
			String sSql = "*";
			if (tableName.indexOf("@") > -1) {
				int k = tableName.indexOf("@");
				sSql = tableName.substring(k + 1);
				tableName = tableName.substring(0, k);
			}
			executeQuery("select " + sSql + " from " + tableName + " where 1>2");
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();

			__dynFieldNames = new String[fieldCount];
			__dynFieldTypes = new int[fieldCount];

			String[] valuesArray = new String[fieldCount];

			for (int i = 1, j = 0; i <= fieldCount; i++) {
				if (mData.getColumnName(i).compareToIgnoreCase(outFieldName) != 0) {
					__dynFieldNames[j] = mData.getColumnName(i).toLowerCase();
					__dynFieldTypes[j] = mData.getColumnType(i);
					/*
					 * switch (dataType) { case -1: // sqlserver text类型 case 1:
					 * case 12: tStr = "'%s'"; break; case 91: case 93: tStr =
					 * "to_date('%s','yyyy-MM-dd HH:mi:ss')"; break; default:
					 * tStr = "%s"; break; }
					 */
					valuesArray[j++] = "%s";
				}
			}

			sResult.append("insert into " + tableName + "(" + CUtil.join(",", __dynFieldNames) + ")");
			sResult.append("values(" + CUtil.join(",", valuesArray) + ")");

		} catch (SQLException ex) {
			E("格式化数据产生异常2：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String createInsertSql(String tableName) throws SQLException {
		return createInsertSql(tableName, "");
	}

	protected String formatResultToXMLData(String nullIs,Boolean _withFieldInfo) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			RecordCount = 0; // 记录数设置为0
			sResult.append("<RS>" + _XmlCr);
			while (rs.next()) {
				RecordCount++;
				sResult.append("<R");
				// 把每个字段按 <字段名>值</字段名> 的方式组合
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					if (dataType > 0 || dataType < -4)
						tStr = rs.getString(i);
					else {
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatResultToXMLOnlyData)：\n " + ex.getMessage());
						} finally {
						}
					}
					if (rs.wasNull())
						tStr = nullIs;
					else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
					}
					sResult.append(" F");
					sResult.append(i);
					sResult.append("=\"");
					sResult.append(tStr);
					sResult.append("\"");
				}
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);

			if (_withFieldInfo) {
				sResult.append("<FS>" + _XmlCr);

				for (int i = 1; i <= mData.getColumnCount(); i++) {
					sResult.append("<F");
					sResult.append(" A=\"" + mData.getColumnName(i).toUpperCase() + "\""); // 字段名称
					sResult.append(" O=\"F" + i + "\""); // 字段别名
					sResult.append(" B=\"" + mData.getColumnType(i) + "\"/>" + _XmlCr); // 字段类型
				}
				sResult.append("</FS>" + _XmlCr);
			}
		} catch (SQLException ex) {
			// System.out.println("格式化数据产生异常2：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToXMLOnlyData(String nullIs) throws SQLException {
		return formatResultToXMLData(nullIs,false);
	}
	
	protected String formatResultToXMLOnlyData(String nullIs, int pageSize) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			boolean noEof = rs.next();
			sResult.append("<RS>" + _XmlCr);
			ResultSetMetaData mData = rs.getMetaData();
			if (noEof) {
				int fieldCount = mData.getColumnCount();
				int nn = 1;
				RecordCount = 0; // 记录数设置为0
				while (noEof) {
					RecordCount++;
					StringBuffer rowbuf = new StringBuffer();
					rowbuf.append("  <R");

					for (int i = 1; i <= fieldCount; i++) {
						int dataType = mData.getColumnType(i);
						if (dataType > 0 || dataType < -4)
							tStr = rs.getString(i);
						else {
							tStr = "";
							try {
								tStr = getBlobField(i);
								/*
								 * byte b[] = rs.getBytes(i); if (b.length > 0)
								 * { tStr = new String(b, "gb2312"); }
								 */
							} catch (Exception ex) {
							} finally {
							}
						}
						if (rs.wasNull())
							tStr = nullIs;
						else {
							// tStr = tStr.trim();
							tStr = CUtil.rightTrim(tStr);
							switch (dataType) {
							case -1: // sqlserver text类型
							case 1:
							case 12:
								tStr = CUtil.replaceXmlSpChar(tStr);
								break;
							case 91:
							case 93:
								if (tStr.length() > 19) // 日期型
									if (tStr.substring(11, 19).equals("00:00:00"))
										tStr = tStr.substring(0, 10);
									else
										tStr = tStr.substring(0, 19);
								break;
							}
						}
						rowbuf.append(" F");
						rowbuf.append(i);
						rowbuf.append("=\"");
						rowbuf.append(tStr);
						rowbuf.append("\"");
					}
					rowbuf.append("/>" + _XmlCr);
					String rowdata = rowbuf.toString();
					if (nn == 1 || nn == pageSize)
						sResult.append(rowdata);
					noEof = rs.next();

					if (!noEof) {
						if (nn < pageSize)
							sResult.append(rowdata);
						break;
					}

					nn++;
					if (nn > pageSize)
						nn = 1;
				}
			}

			if (_HasFieldInfo) {
				sResult.append("<FS>" + _XmlCr);

				for (int i = 1; i <= mData.getColumnCount(); i++) {
					sResult.append("<F");
					sResult.append(" A=\"" + mData.getColumnName(i).toUpperCase() + "\""); // 字段名称
					sResult.append(" O=\"F" + i + "\""); // 字段别名
					sResult.append(" B=\"" + mData.getColumnType(i) + "\"/>" + _XmlCr); // 字段类型
				}
				sResult.append("</FS>" + _XmlCr);
			}

			sResult.append("</RS>" + _XmlCr);
		} catch (SQLException ex) {
			// System.out.println("格式化数据产生异常2：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	/**
	 * formatResultToXML2 不规定页行数的
	 * 
	 * @param rs
	 * @return
	 */
	public String formatResultToXML2(ResultSet rs, int pageLines) throws SQLException {
		return formatResultToXML1(rs, pageLines, "");
	}

	public String formatResultToXML2(ResultSet rs) throws SQLException {
		return formatResultToXML1(rs, 0, "");
	}

	public String formatResultToXML3(ResultSet rs) throws SQLException {
		return formatResultToXML1(rs, -1, ""); // 不设置空白行
	}

	public String formatResultToXML4(ResultSet rs) throws SQLException {
		return formatResultToXML1(rs, 0, ""); // 不设置空白行
	}

	public String formatResultToXML4() throws SQLException {
		return formatResultToXML1(rs, 0, ""); // 不设置空白行
	}

	public String formatResultToXML4(String DatasetName) throws SQLException {
		String Result;
		if (DatasetName.equals(""))
			Result = formatResultToXML1(rs, 0, "");
		else
			Result = "<xml id=\"" + DatasetName + "\">" + formatResultToXML1(rs, 0, "") + "</xml>";
		return Result;
	}

	/**
	 * formatDeltaToXML2构造Delta，用来在客户端记录数据的变化
	 * 
	 * @param rs
	 *            记录集
	 * @return 结果XML
	 */
	public String formatDeltaToXML2(ResultSet rs) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		try {
			ResultSetMetaData mData = rs.getMetaData();
			sResult.append("<RS>" + _XmlCr);
			sResult.append("<R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"0\""); // 记录状态
			// 把每个字段按 <字段名>值</字段名> 的方式组合
			for (int i = 1; i <= mData.getColumnCount(); i++)
				sResult.append(" " + mData.getColumnName(i).toUpperCase() + "=\"\"");
			sResult.append("/>" + _XmlCr);
			sResult.append("</RS>" + _XmlCr);

		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[2]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	/**
	 * formatMetaDataToXML获取元数据
	 * 
	 * @param rs
	 *            数据集,att=0字段类型用数字表示，否则字段类型用字符串表示
	 * @return 元数据的XML
	 */
	public String formatMetaDataToXML(ResultSet rs, int att) throws SQLException {
		StringBuffer sResult = new StringBuffer();
		try {
			ResultSetMetaData mData = rs.getMetaData();
			sResult.append("<FS>" + _XmlCr);
			for (int i = 1; i <= mData.getColumnCount(); i++) {
				int ctype = mData.getColumnType(i);
				int nscale = mData.getScale(i);
				if ((ctype == 2 || ctype == 3) && nscale == 0)
					ctype = 4;

				sResult.append("<F");
				sResult.append(" A=\"" + mData.getColumnName(i).toUpperCase() + "\""); // 字段名称
				sResult.append(" O=\"F" + i + "\""); // 字段别名
				sResult.append(" L=\"" + mData.getColumnLabel(i) + "\""); // 字段标题
				if (att == 0)
					sResult.append(" B=\"" + ctype + "\""); // 字段类型
				else
					sResult.append(" B=\"" + mData.getColumnTypeName(i) + "\"");
				sResult.append(" W=\"" + mData.getPrecision(i) + "\""); // 宽度
				sResult.append(" D=\"" + mData.isNullable(i) + "\""); // 可空
				sResult.append(" P=\"" + nscale + "\"");
				sResult.append(" R=\"" + mData.isReadOnly(i) + "\"");
				sResult.append(" K=\"0\"");
				sResult.append(" S=\"1\"");
				if (mData.isAutoIncrement(i))
					sResult.append(" U=\"0\"");
				else
					sResult.append(" U=\"1\"");
				sResult.append(" C=\"\"");
				sResult.append(" E=\"\"");
				sResult.append(" F=\"\"");
				sResult.append(" H=\"\"");
				sResult.append(" M=\"\"");
				sResult.append(" BN=\"" + mData.getColumnTypeName(i) + "\""); // 字段名称
				sResult.append(" BC=\"" + mData.getCatalogName(i) + "\""); // 字段名称
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</FS>" + _XmlCr);
		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[3]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	public String formatMetaDataToXML(ResultSet rs) throws SQLException {
		return formatMetaDataToXML(rs, 1);
	}

	/**
	 * formatMetaDataToXML根据数据字典获取元数据
	 * 
	 * @param rs
	 *            数据集,cType - 字典标识
	 * @return 元数据的XML
	 */

	public String formatMetaDataToXML(ResultSet rs, String cType) throws SQLException {
		int m = 1; // 计数
		StringBuffer sResult = new StringBuffer();
		if (!cType.equals("")) {
			ResultSet rsLexicon = null;
			ResultSetMetaData mData = rs.getMetaData();

			boolean[] isSet = new boolean[mData.getColumnCount() + 1]; // 记录那些字段被设置过
			String[] sBuffer = new String[mData.getColumnCount() + 1];

			Vector<String> vfs = new Vector<String>();
			for (int j = 1; j <= mData.getColumnCount(); j++) {
				vfs.add(mData.getColumnName(j).toUpperCase());
				isSet[j] = false;
			}
			// 内部查询
			PreparedStatement istmt = null;
			try {
				if (conn == null)
					conn = getConnection();
				istmt = conn.prepareStatement("SELECT * FROM SYS_LEXICOND WHERE T_ID=? ORDER BY F_XH");
				istmt.setString(1, cType);
				rsLexicon = istmt.executeQuery();

				while (rsLexicon.next()) {
					String fieldName = rsLexicon.getString("F_NAME"); // 获取字段名称
					int k = vfs.indexOf(fieldName);
					if (k >= 0) {
						k = k + 1;

						int ctype = mData.getColumnType(k);
						int nscale = mData.getScale(k);
						if ((ctype == 2 || ctype == 3) && nscale == 0)
							ctype = 4;

						StringBuffer sR = new StringBuffer();
						sR.append("<F");
						sR.append(" A=\"" + fieldName.toUpperCase() + "\""); // 字段名称
						sR.append(" O=\"F" + k + "\""); // 字段别名
						sR.append(" L=\"" + CUtil.NVL(rsLexicon.getString("F_LABEL")) + "\""); // 字段标题
						sR.append(" B=\"" + ctype + "\""); // 字段类型
						sR.append(" W=\"" + mData.getPrecision(k) + "\""); // 宽度
						sR.append(" D=\"" + rsLexicon.getInt("F_NULL") + "\""); // 可空
						sR.append(" P=\"" + nscale + "\"");
						sR.append(" R=\"" + mData.isReadOnly(k) + "\"");
						sR.append(" K=\"0\"");
						sR.append(" S=\"" + rsLexicon.getInt("F_KEY") + "\"");
						if (mData.isAutoIncrement(k))
							sR.append(" U=\"0\"");
						else
							sR.append(" U=\"" + (1 - rsLexicon.getInt("NOUPDATE")) + "\"");
						sR.append(" C=\"" + CUtil.NVL(rsLexicon.getString("F_CONSTRANT")) + "\"");
						sR.append(" E=\"" + CUtil.NVL(rsLexicon.getString("F_ERRORMSG")) + "\"");
						sR.append(" F=\"" + CUtil.NVL(rsLexicon.getString("SHOWEDIT")) + "\"");
						sR.append(" H=\"" + CUtil.NVL(rsLexicon.getString("F_FORMATERR")) + "\"");
						sR.append(" M=\"" + CUtil.NVL(rsLexicon.getString("SHOWFORMAT")) + "\"");
						sR.append(" BN=\"" + mData.getColumnTypeName(k) + "\""); // 字段名称
						sR.append(" BC=\"" + mData.getCatalogName(k) + "\""); // 字段名称
						sR.append("/>" + _XmlCr);
						sBuffer[k] = sR.toString();

						isSet[k] = true;
						m++;
					}
				}

				vfs = null;
				for (int j = 1; j <= mData.getColumnCount(); j++)
					if (!isSet[j]) {
						StringBuffer sR = new StringBuffer();
						int ctype = mData.getColumnType(j);
						int nscale = mData.getScale(j);
						if ((ctype == 2 || ctype == 3) && nscale == 0)
							ctype = 4;
						sR.append("<F");
						sR.append(" A=\"" + mData.getColumnName(j).toUpperCase() + "\""); // 字段名称
						sR.append(" O=\"F" + j + "\""); // 字段别名
						sR.append(" L=\"" + mData.getColumnName(j) + "\""); // 字段标题
						sR.append(" B=\"" + ctype + "\""); // 字段类型
						sR.append(" W=\"" + mData.getPrecision(j) + "\""); // 宽度
						sR.append(" D=\"" + mData.isNullable(j) + "\""); // 可空
						sR.append(" P=\"" + nscale + "\"");
						sR.append(" R=\"" + mData.isReadOnly(j) + "\"");
						sR.append(" K=\"0\"");
						sR.append(" S=\"1\"");
						sR.append(" U=\"1\"");
						sR.append(" C=\"\"");
						sR.append(" E=\"\"");
						sR.append(" F=\"\"");
						sR.append(" H=\"\"");
						sR.append(" M=\"\"");
						sR.append(" BN=\"" + mData.getColumnTypeName(j) + "\""); // 字段名称
						sR.append(" BC=\"" + mData.getCatalogName(j) + "\""); // 字段名称

						sR.append("/>" + _XmlCr);
						sBuffer[j] = sR.toString();
						m++;
					}

				sResult.append("<FS>" + _XmlCr);
				for (int i = 1; i <= mData.getColumnCount(); i++)
					sResult.append(sBuffer[i]);
				sResult.append("</FS>" + _XmlCr);
			} catch (SQLException ex) {
				System.err.println(ex.getMessage());
			} finally {
				rsLexicon.close();
				istmt.close();
			}
		}
		String result = (m <= 1) ? formatMetaDataToXML(rs, 0) : sResult.toString();
		// P(result);

		return result;
	}

	protected String formatMetaToDataSet() {
		if (rs == null)
			return "";
		StringBuilder sResult = new StringBuilder();
		try {
			sResult.append("<RS>" + _XmlCr);
			int nw = 0, dnw = 0;
			ResultSetMetaData mData = rs.getMetaData();

			for (int i = 1; i <= mData.getColumnCount(); i++) {
				sResult.append("<R RL=\"" + (i + 1) + "\" RT=\"0\" RS=\"0\" RR=\"0\"");
				sResult.append(" F1=\"" + mData.getColumnName(i).toUpperCase() + "\""); // 字段名称
				sResult.append(" F2=\"F" + i + "\""); // 字段别名
				sResult.append(" F3=\"" + mData.getColumnName(i) + "\""); // 字段标题
				sResult.append(" F4=\"" + mData.getColumnTypeName(i) + "\"");

				switch (mData.getColumnType(i)) {
				case 2:
					nw = mData.getPrecision(i);
					dnw = mData.getScale(i);
					break;
				case 3:
					nw = mData.getPrecision(i);
					dnw = 4;
					break;
				default:
					nw = mData.getPrecision(i);
					dnw = mData.getScale(i);
					break;
				}

				sResult.append(" F5=\"" + nw + "\""); // 宽度 12
				sResult.append(" F6=\"" + mData.isNullable(i) + "\""); // 可空 13
				sResult.append(" F7=\"" + dnw + "\""); // 4小数位数

				sResult.append(" F8=\"" + mData.isReadOnly(i) + "\""); // 只读
				sResult.append(" F9=\"0\"");

				sResult.append(" F10=\"" + 0 + "\"");

				sResult.append(" F11=\"0\"");
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);

			sResult.append("<FS>" + _XmlCr);
			for (int i = 0; i < 12; i++) {
				sResult.append("<F");
				sResult.append(" A=\"F" + (i + 1) + "\""); // 字段名称
				sResult.append(" O=\"F" + (i + 1) + "\""); // 字段别名
				sResult.append(" B=\"" + 1 + "\"/>" + _XmlCr); // 字段类型
			}
			sResult.append("</FS>" + _XmlCr);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return sResult.toString();
	}

	public boolean sessionValidated(HttpSession session) {
		// 设置安全控制字段的值
		boolean isLogin = CInitParam.safeField.equals("") || session.getAttribute(CInitParam.safeField) != null;

		if (!CInitParam.safeField.equals("") && session.getAttribute(CInitParam.safeField) != null)
			gu_id = session.getAttribute(CInitParam.safeField).toString();
		return isLogin;
	}

	public boolean sessionValidated(HttpServletRequest request) {
		return sessionValidated(request.getSession());
	}

	protected String getUserID(HttpSession session) {
		return session.getAttribute(CInitParam.safeField).toString();
	}

	/**
	 * executeQuery 根据sql语句查询数据
	 * 
	 * @param sql
	 *            指定的sql语句
	 * @return 返回结果集，无时返回null
	 */
	protected ResultSet executeQuery(String sql) throws SQLException {
		int sMark = 0; // 成功标记,查询成功时等于1

		sql = CUtil.formatStr(sql, gu_id, "{U_ID}");

		if (_bprintTrack)
			P("executeQuery-SQL:\n" + sql);

		if (sql == null || sql == "")
			return null;
		try {
			if (conn == null)
				conn = getConnection();
			if (stmt == null)
				stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			sMark = 1;
		} catch (SQLException ex) {
			// System.err.println(ex.getMessage());
			closeConn();
			_errorInf = ex.getMessage();
			throw new SQLException("(E):\n " + ex.getMessage());
		} finally {
			if (sMark == 0) // 如果查询失败，关闭所有
				closeConn();
		}
		return rs;
	}

	protected ResultSet executeQuery2(String sql, String[] params) throws SQLException {
		int sMark = 0; // 成功标记,查询成功时等于1

		sql = CUtil.formatStr(sql, gu_id, "{U_ID}");

		if (_bprintTrack)
			P("executeQuery2-SQL:\n" + sql);

		try {
			if (conn == null)
				conn = getConnection();

			if (pstmt != null)
				pstmt.close();
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++)
				pstmt.setString(i + 1, params[i]);

			rs = pstmt.executeQuery();
			sMark = 1;
			return rs;

		} catch (SQLException ex) {
			_errorInf = ex.getMessage();
			throw new SQLException("(E2):\n " + ex.getMessage());
		} finally {
			if (sMark == 0) // 如果查询失败，关闭所有
				closeConn();
		}

	}

	protected ResultSet executeQuery2(String sql, String param) throws SQLException {
		int sMark = 0; // 成功标记,查询成功时等于1

		sql = CUtil.formatStr(sql, gu_id, "{U_ID}");

		if (_bprintTrack)
			P("executeQuery2-SQL:\n" + sql + "," + param);

		try {
			if (conn == null)
				conn = getConnection();

			if (pstmt != null)
				pstmt.close();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, param);

			rs = pstmt.executeQuery();
			sMark = 1;
		} catch (SQLException ex) {
			throw new SQLException("(E22):\n " + ex.getMessage());
		} finally {
			if (sMark == 0) // 如果查询失败，关闭所有
				closeConn();
		}
		return rs;
	}

	protected ResultSet executeQuery3(String sql, String[] vparams) throws SQLException {
		sql = CUtil.formatStr(sql, vparams, "%s");
		return executeQuery(sql);
	}

	protected ResultSet executeQuery3(String sql, String vparam) throws SQLException {
		sql = CUtil.formatStr(sql, vparam, "%s");
		return executeQuery(sql);
	}

	protected ResultSet executeProcQuery(String procName, String sql) throws SQLException {
		int sMark = 0; // 成功标记,查询成功时等于1

		sql = CUtil.formatStr(sql, gu_id, "{U_ID}");

		if (_bprintTrack)
			P("executeProcQuery-SQL3:\n" + sql);

		try {
			if (conn == null)
				conn = getConnection();

			cstmt = conn.prepareCall("{call " + procName + "(?,?)}");
			cstmt.setString(1, sql);
			cstmt.registerOutParameter(2, OracleTypes.CURSOR);

			cstmt.executeQuery();
			rs = (ResultSet) cstmt.getObject(2);
			sMark = 1;

		} catch (SQLException ex) {
			throw new SQLException("执行过程异常:\n " + ex.getMessage());
		} finally {
			if (sMark == 0) // 如果查询失败，关闭所有
				closeConn();
		}
		return rs;
	}

	/**
	 * 执行指定的SQL语句
	 * 
	 * @param sql
	 * @return 0-失败,非0-成功
	 * @throws SQLException
	 */
	protected int executeUpdate(String sql) throws SQLException {
		int result = 0;

		sql = CUtil.formatStr(sql, gu_id, "{U_ID}");

		if (_bprintTrack)
			P("executeUpdate:\n" + sql);

		if (sql != null && !sql.equals(""))
			try {
				if (conn == null)
					conn = getConnection();
				if (stmt == null)
					stmt = conn.createStatement();
				result = stmt.executeUpdate(sql);

				if (result == 0)
					conn.rollback();
				else {
					conn.commit();
					_errorInf = "执行成功!本次执行影响了" + result + "条记录.";
				}
			} catch (SQLException ex) {
				_errorInf = ex.getMessage();
				// System.out.println(_errorInf);
				throw new SQLException("执行SQL语句产生异常：\n " + _errorInf);
			} finally {
				closeConn();
			}
		return result;
	}

	protected int executeUpdate(String sql, String[] vparams) {
		sql = CUtil.formatStr(sql, vparams, "%s");
		int nResult = 0;
		try {
			nResult = executeUpdate(sql);
		} catch (SQLException ex) {
			nResult = 0;
		} finally {

		}
		return nResult;
	}

	protected int executeUpdateEx(String sql) throws SQLException {
		int result = 0;


		sql = CUtil.formatStr(sql, gu_id, "{U_ID}");

		if (_bprintTrack)
			P("executeUpdateEx:\n" + sql);

		sql = CUtil.removeCR(sql);
		// System.out.println("2:"+sql);
		if (sql != null && sql != "")
			try {
				if (conn == null)
					conn = getConnection();
				if (stmt == null)
					stmt = conn.createStatement();

				result = stmt.executeUpdate(sql);
				conn.commit();
				if (result > 0)
					_errorInf = "执行成功!共影响" + result + "条记录.";
				else
					_errorInf = "执行成功，但未修改任何记录.";
			} catch (SQLException ex) {
				_errorInf = ex.getMessage();
				throw new SQLException("执行SQL语句产生异常：\n " + _errorInf);
			} finally {
				closeConn();
			}
		return result;
	}

	protected int executeSqls(String[] allSqlds) {
		int result = -1;
		try {
			try {
				closeConn();
				if (conn == null || conn.isClosed())
					conn = getConnection();

				if (stmt == null)
					stmt = conn.createStatement();
				// CallableStatement cstmt = null;

				conn.setAutoCommit(false);

				// savepoint = conn.setSavepoint("POINT1");

				int i = 0, sqlcount = allSqlds.length;
				while (i < sqlcount) {
					// System.out.println("sql="+allSqlds[i]);
					RecordCount = stmt.executeUpdate(allSqlds[i]) + 1;
					if (RecordCount == 0)
						break;
					i++;
				}
				if (RecordCount > 0) {
					conn.commit();
					result = 0;
				} else {
					conn.rollback();
					_errorInf = "系统无法插入记录.";
					result = -3;
				}
				// if (cstmt != null) cstmt.close();
			} catch (SQLException ex) {
				conn.rollback();
				_errorInf = ex.getMessage();
				result = -2;
			} finally {
				// conn.releaseSavepoint(savepoint);
				conn.setAutoCommit(true);
				closeConn();
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
			result = -2;
		}
		return result;
	}

	protected int executeBatchSql(String[] allSqlds) {
		int result = -1;
		try {
			try {
				closeConn();
				if (conn == null || conn.isClosed())
					conn = getConnection();

				if (stmt == null)
					stmt = conn.createStatement();

				conn.setAutoCommit(false);

				// savepoint = conn.setSavepoint("POINT1");

				int i = 0, sqlcount = allSqlds.length;
				while (i < sqlcount) {
					stmt.addBatch(allSqlds[i]);
					i++;
				}
				stmt.executeBatch();
				conn.commit();
				result = 0;
			} catch (BatchUpdateException ex) {
				conn.rollback();
				_errorInf = ex.getMessage();
				result = -2;
			} finally {
				// conn.releaseSavepoint(savepoint);
				conn.setAutoCommit(true);
				closeConn();
			}
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
			result = -2;
		}
		return result;
	}

	public int getLastErrorCode() {
		return Result;
	}
	
	public String getLastError() {
		return _errorInf;
	}

	public int getRecordCount() {
		return RecordCount;
	}

	public void checkIsNumber(String vcalTotalCtlField) throws SQLException {

		try {
			ResultSetMetaData mData = rs.getMetaData();
			int nf = mData.getColumnCount();
			__isNumber = null;
			__fieldTotal = null;
			__isNumber = new boolean[nf];
			__fieldTotal = new double[nf];

			hasCalTotalCtlField = !vcalTotalCtlField.equals("");

			calTotalCtlField = vcalTotalCtlField;

			for (int i = 0; i < nf; i++) {
				switch (mData.getColumnType(i + 1)) {
				case -5:
				case -6:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					__isNumber[i] = true;
					break;
				default:
					__isNumber[i] = false;
				}
				__fieldTotal[i] = 0;
			}
		} catch (SQLException ex) {
			throw new SQLException("检查数据类型产生异常[1]：\n " + ex.getMessage());
		} finally {
		}
	}

	public void checkIsNumber() throws SQLException {
		checkIsNumber("");
	}

	public boolean nextTotal() throws SQLException {
		_rsEof = !rs.next();
		if (!_rsEof) {
			int nf = __isNumber.length;
			for (int i = 0; i < nf; i++)
				if (__isNumber[i] && (!hasCalTotalCtlField || rs.getInt(calTotalCtlField) == 1)) {
					String moneys = CUtil.NVL(rs.getString(i + 1));
					if (moneys.equals(""))
						moneys = "0";
					__fieldTotal[i] += Double.parseDouble(moneys);
				}
		}
		return !_rsEof;
	}

	public String getTotal(String fieldName, String sFormat) throws SQLException {

		ResultSetMetaData mData = rs.getMetaData();
		int i, nf = mData.getColumnCount();
		double n = 0;

		for (i = 1; i <= nf; i++) {
			if (fieldName.toUpperCase().equals(mData.getColumnName(i).toUpperCase()))
				break;
		}

		if (i <= nf && __isNumber[i - 1])
			n = __fieldTotal[i - 1];

		if (__zeroToSpace && n == 0)
			return "";

		java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.DecimalFormat.getInstance();
		df.applyPattern(sFormat);

		return df.format(n);
	}

	public boolean next() throws SQLException {
		_rsEof = !rs.next();
		return !_rsEof;
	}

	public String getString(String fieldName) throws SQLException {
		return CUtil.NVL(rs.getString(fieldName));
	}

	public String getString(int fieldIndex) throws SQLException {
		return CUtil.NVL(rs.getString(fieldIndex));
	}

	public String getString(String fieldName, String defaultValue) throws SQLException {
		String v = rs.getString(fieldName);
		if (v == null || v.equals(""))
			return defaultValue;
		return CUtil.NVL(v);
	}

	public String getString(int fieldIndex, String defaultValue) throws SQLException {
		String v = rs.getString(fieldIndex);
		if (v == null || v.equals(""))
			return defaultValue;
		return CUtil.NVL(v);
	}

	public String getString2(String fieldName, String defaultValue) throws SQLException {
		if (!_rsEof)
			return getString(fieldName, defaultValue);
		else
			return defaultValue;
	}

	public String getString2(String fieldName) throws SQLException {
		if (!_rsEof)
			return getString(fieldName, "");
		else
			return "";
	}

	public String getString2(int fieldIndex, String defaultValue) throws SQLException {
		if (!_rsEof)
			return getString(fieldIndex, defaultValue);
		else
			return defaultValue;
	}

	public String getStringX(String fieldName) throws SQLException {
		return CUtil.NVLX(rs.getString(fieldName));
	}

	public String getStringX(int fieldIndex) throws SQLException {
		return CUtil.NVLX(rs.getString(fieldIndex));
	}

	public String getCurrency(String fieldName, String sFormat) throws SQLException {
		double n = rs.getDouble(fieldName);
		n = rs.wasNull() ? 0 : n;
		if (__zeroToSpace && n == 0)
			return "";

		java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.DecimalFormat.getInstance();
		df.applyPattern(sFormat);

		return df.format(n);
	}

	public double getFloat(String fieldName) throws SQLException {
		double n = rs.getDouble(fieldName);
		return rs.wasNull() ? 0 : n;
	}

	public String getFloat(String fieldName, String sFormat) throws SQLException {
		double n = rs.getFloat(fieldName);
		n = rs.wasNull() ? 0 : n;
		if (__zeroToSpace && n == 0)
			return "";

		java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.DecimalFormat.getInstance();
		df.applyPattern(sFormat);

		return df.format(n);
	}

	public double getFloat(int fieldIndex) throws SQLException {
		double n = rs.getFloat(fieldIndex);
		return rs.wasNull() ? 0 : n;
	}

	public String getFloat(int fieldIndex, String sFormat) throws SQLException {
		double n = rs.getFloat(fieldIndex);
		n = rs.wasNull() ? 0 : n;
		if (__zeroToSpace && n == 0)
			return "";

		java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.DecimalFormat.getInstance();
		df.applyPattern(sFormat);
		return df.format(n);
	}

	public double getFloat(String fieldName, double defaultValue) throws SQLException {
		double n = rs.getFloat(fieldName);
		return rs.wasNull() ? defaultValue : n;
	}

	public String getFloat(String fieldName, double defaultValue, String sFormat) throws SQLException {
		double n = rs.getFloat(fieldName);
		n = rs.wasNull() ? defaultValue : n;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public double getFloat(int fieldIndex, double defaultValue) throws SQLException {
		double n = rs.getFloat(fieldIndex);
		return rs.wasNull() ? defaultValue : n;
	}

	public String getFloat(int fieldIndex, double defaultValue, String sFormat) throws SQLException {
		double n = rs.getFloat(fieldIndex);
		n = rs.wasNull() ? defaultValue : n;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public double getFloat2(String fieldName, double defaultValue) throws SQLException {
		return (!_rsEof) ? getFloat(fieldName, defaultValue) : defaultValue;
	}

	public String getFloat2(String fieldName, double defaultValue, String sFormat) throws SQLException {
		double n = (!_rsEof) ? getFloat(fieldName, defaultValue) : defaultValue;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public double getFloat2(int fieldIndex, double defaultValue) throws SQLException {
		return (!_rsEof) ? getFloat(fieldIndex, defaultValue) : defaultValue;
	}

	public String getFloat2(int fieldIndex, double defaultValue, String sFormat) throws SQLException {
		double n = (!_rsEof) ? getFloat(fieldIndex, defaultValue) : defaultValue;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public int getInt(String fieldName) throws SQLException {
		int n = rs.getInt(fieldName);
		return rs.wasNull() ? 0 : n;
	}

	public String getInt(String fieldName, String sFormat) throws SQLException {
		int n = rs.getInt(fieldName);
		n = rs.wasNull() ? 0 : n;
		if (__zeroToSpace && n == 0)
			return "";
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public int getInt(int fieldIndex) throws SQLException {
		int n = rs.getInt(fieldIndex);
		return rs.wasNull() ? 0 : n;
	}

	public String getInt(int fieldIndex, String sFormat) throws SQLException {
		int n = rs.getInt(fieldIndex);
		n = rs.wasNull() ? 0 : n;
		if (__zeroToSpace && n == 0)
			return "";
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public int getInt(String fieldName, int defaultValue) throws SQLException {
		int n = rs.getInt(fieldName);
		return rs.wasNull() ? defaultValue : n;
	}

	public String getInt(String fieldName, int defaultValue, String sFormat) throws SQLException {
		int n = rs.getInt(fieldName);
		n = rs.wasNull() ? defaultValue : n;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public int getInt(int fieldIndex, int defaultValue) throws SQLException {
		int n = rs.getInt(fieldIndex);
		return rs.wasNull() ? defaultValue : n;
	}

	public String getInt(int fieldIndex, int defaultValue, String sFormat) throws SQLException {
		int n = rs.getInt(fieldIndex);
		n = rs.wasNull() ? defaultValue : n;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public int getInt2(String fieldName, int defaultValue) throws SQLException {
		return (!_rsEof) ? getInt(fieldName, defaultValue) : defaultValue;
	}

	public String getInt2(String fieldName, int defaultValue, String sFormat) throws SQLException {
		int n = (!_rsEof) ? getInt(fieldName, defaultValue) : defaultValue;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	public int getInt2(int fieldIndex, int defaultValue) throws SQLException {
		return (!_rsEof) ? getInt(fieldIndex, defaultValue) : defaultValue;
	}

	public String getInt2(int fieldIndex, int defaultValue, String sFormat) throws SQLException {
		int n = (!_rsEof) ? getInt(fieldIndex, defaultValue) : defaultValue;
		DecimalFormat df = new DecimalFormat(sFormat);
		return df.format(n);
	}

	protected int getIndexByFieldName(String fieldName) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		int j = 0;
		for (int i = 1; i <= mData.getColumnCount(); i++)
			if (mData.getColumnName(i).toUpperCase().equals(fieldName.toUpperCase())) {
				j = i;
				break;
			}
		return j;
	}

	/**
	 * 2013-01-17
	 * 
	 * @param fieldName
	 * @return
	 * @throws SQLException
	 */
	protected int getFieldDataType(String fieldName) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		int dataType = 0;
		for (int i = 1; i <= mData.getColumnCount(); i++)
			if (mData.getColumnName(i).toUpperCase().equals(fieldName.toUpperCase())) {
				dataType = mData.getColumnType(i);
				break;
			}
		return dataType;
	}

	/**
	 * 2013-01-17
	 * 
	 * @param fieldIndex
	 * @return
	 * @throws SQLException
	 */

	protected int getFieldDataType(int fieldIndex) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		return mData.getColumnType(fieldIndex);
	}

	public String getNote(int fieldIndex) throws SQLException {
		String tStr = "";
		try {
			tStr = getBlobField(fieldIndex);
			/*
			 * byte b[] = rs.getBytes(fieldIndex); if (b.length > 0) { tStr =
			 * new String(b, "gb2312"); }
			 */
		} catch (Exception ex) {
			// System.out.println("获取备注型数据产生异常：\n " +
			// ex.getMessage());
		} finally {
		}
		return tStr;
	}

	/**
	 * 获取备注字段内容
	 * 
	 * @param fieldName
	 *            字段名称
	 * @return 字段内容,字段为空时返回""
	 * @throws SQLException
	 */

	public String getNote(String fieldName) throws SQLException {
		String tStr = "";

		int i = getIndexByFieldName(fieldName);
		if (i > 0)
			tStr = getNote(i);
		return tStr;
	}

	public String getHtml(int fieldIndex) throws SQLException {
		String tStr = "";
		if (getFieldDataType(fieldIndex) == -1)
			tStr = getNote(fieldIndex);
		else
			tStr = getString(fieldIndex);
		tStr = CUtil.formatStr(tStr, "<br>", "\n");
		tStr = CUtil.formatStr(tStr, "&nbsp;", " ");
		return tStr;
	}

	public String getHtml(String fieldName) throws SQLException {
		String tStr = "";

		int i = getIndexByFieldName(fieldName);
		if (i > 0) {
			if (getFieldDataType(i) == -1)
				tStr = getNote(i);
			else
				tStr = getString(i);
			tStr = CUtil.formatStr(tStr, "<br>", "\n");
			tStr = CUtil.formatStr(tStr, "&nbsp;", " ");
		}
		return tStr;
	}

	public java.sql.Date getDate(int fieldIndex) throws SQLException {
		return rs.getDate(fieldIndex);
	}

	public java.sql.Date getDate(String fieldName) throws SQLException {
		return rs.getDate(fieldName);
	}

	public String getDate(int fieldIndex, String dFormat) throws SQLException {
		Timestamp dDate;
		String tStr = "";
		dDate = rs.getTimestamp(fieldIndex);
		if (!rs.wasNull()) {
			DateFormat df = new SimpleDateFormat(dFormat);
			tStr = df.format(dDate);
		}
		return tStr;
	}

	public String getDate(String fieldName, String dFormat) throws SQLException {
		String tStr = "";

		int i = getIndexByFieldName(fieldName);
		if (i > 0)
			tStr = getDate(i, dFormat);
		return tStr;
	}

	public String getDate2(String fieldName, String dFormat, String defaultValue) throws SQLException {
		return (!_rsEof) ? getDate(fieldName, dFormat) : defaultValue;
	}

	public int getFieldCount() throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		return mData.getColumnCount();
	}

	public String getFieldName(int fieldIndex) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		mData.getColumnCount();
		return mData.getColumnName(fieldIndex);
	}

	public int getFieldType(int fieldIndex) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		return mData.getColumnType(fieldIndex);
	}

	public String getFieldTypeName(int fieldIndex) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		return mData.getColumnTypeName(fieldIndex);
	}

	public void setZeroToSpace(boolean zTos) {
		__zeroToSpace = zTos;
	}

	public String[] getKeyValues(int n) {
		switch (n) {
		case 1:
			return __keyNameValues;
		case 2:
			return __keyLevelValues;
		default:
			return __keyCodeValues;
		}
	}

	public String[] getKeyCodeValues() {
		return __keyCodeValues;
	}

	public String[] getKeyNameValues() {
		return __keyNameValues;
	}

	public String[] getKeyLevelValues() {
		return __keyLevelValues;
	}

	public int getKeyCount() {
		return (__keyCodeValues != null) ? 0 : __keyCodeValues.length;
	}

	/**
	 * 交叉报表数据产生器
	 * 
	 * @param rs
	 * @param pageLines
	 * @param nullIs
	 * @return
	 * @throws SQLException
	 */
	protected String formatResultToXML90(ResultSet rs, int fixedCol, int keyIndex, String nullIs) throws SQLException {
		if (rs == null)
			return "";

		StringBuffer sResult = new StringBuffer();
		String[] fixedValues = new String[fixedCol];
		String tStr = null;
		String keyValue;
		int nrow = 0, j;
		boolean noeof, bDetal = true;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();
			RecordCount = 0; // 记录数设置为0
			sResult.append("<RS>" + _XmlCr);
			noeof = rs.next();
			while (noeof) {
				Result = 0; // 设置有数据标记
				RecordCount++; // 记录数增加
				j = 1;
				keyValue = rs.getString(keyIndex);

				StringBuffer aResult = new StringBuffer();

				for (int i = 1; i <= fixedCol; i++, j++) {
					int dataType = mData.getColumnType(i);
					tStr = rs.getString(i);
					tStr = rs.wasNull() ? nullIs : CUtil.rightTrim(tStr);
					switch (dataType) {
					case -1:
					case 1:
					case 12:
						tStr = CUtil.replaceXmlSpChar(tStr);
						break;
					case 91:
					case 93:
						if (tStr.length() > 19) // 日期型
							if (tStr.substring(11, 19).equals("00:00:00"))
								tStr = tStr.substring(0, 10);
							else
								tStr = tStr.substring(0, 19);
						break;
					}
					if (tStr.equals(fixedValues[i - 1]))
						tStr = "";
					else
						fixedValues[i - 1] = tStr;

					aResult.append(" F" + j + "=\"" + tStr + "\"");
				}

				sResult.append("<R RL=\"" + RecordCount + "\" RT=\"1\" RS=\"0\" RR=\"" + RecordCount + "\""); // 记录状态
				sResult.append(aResult);

				boolean bmark = true;
				int m = 1;
				while (noeof && bmark) {
					String fKeyValue = rs.getString(fixedCol + 1);
					for (int i = fixedCol + 2; i <= fieldCount; i++, j++, m++) {
						int dataType = mData.getColumnType(i);
						tStr = rs.getString(i);
						tStr = rs.wasNull() ? nullIs : CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1:
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
						if (bDetal) {
							_DeltaBuf.append(" _" + fKeyValue + "_" + m + "=\"\"");
							_MetaBuf.append("<F");
							_MetaBuf.append(" A=\"_F" + fKeyValue + "\""); // 字段名称
							_MetaBuf.append(" O=\"F" + j + "\""); // 字段别名
							_MetaBuf.append(" L=\"" + fKeyValue + "\""); // 字段标题
							_MetaBuf.append(" B=\"" + mData.getColumnType(i) + "\""); // 字段类型
							_MetaBuf.append(" W=\"" + mData.getColumnDisplaySize(i) + "\""); // 宽度
							_MetaBuf.append(" D=\"" + mData.isNullable(i) + "\""); // 可空
							_MetaBuf.append(" P=\"" + mData.getScale(i) + "\"");
							_MetaBuf.append(" R=\"" + mData.isReadOnly(i) + "\"");
							_MetaBuf.append(" K=\"0\"");
							_MetaBuf.append(" S=\"1\"");
							_MetaBuf.append(" U=\"1\"");
							_MetaBuf.append(" C=\"\"");
							_MetaBuf.append(" E=\"\"");
							_MetaBuf.append(" F=\"\"");
							_MetaBuf.append(" H=\"\"");
							_MetaBuf.append(" M=\"\"");
							_MetaBuf.append(" BN=\"" + mData.getColumnTypeName(j) + "\""); // 字段名称
							_MetaBuf.append(" BC=\"" + mData.getCatalogName(j) + "\""); // 字段名称
							_MetaBuf.append("/>" + _XmlCr);
						}
						sResult.append(" F" + j + "=\"" + tStr + "\"");
					}
					noeof = rs.next();
					String v = "";
					if (noeof) {
						v = rs.getString(keyIndex);
						bmark = v.equals(keyValue);
					} else
						bmark = false;

					if (!bmark) {
						sResult.append("/>" + _XmlCr);
						keyValue = v;
						bDetal = false;
					}
				}
				nrow++;
			}

			if (RecordCount == 0) {
				sResult.append("<R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"0\"");
				for (int i = 1; i <= fixedCol; i++)
					sResult.append(" F" + i + "=\"\"");
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);
		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToXML91(ResultSet rs, int keyIndex, String nullIs) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		String keyValue = "";

		int j;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();
			RecordCount = 0; // 记录数设置为0
			sResult.append("<RS>" + _XmlCr);
			while (rs.next()) {
				Result = 0; // 设置有数据标记
				RecordCount++; // 记录数增加
				j = 1;

				StringBuffer aResult = new StringBuffer();

				for (int i = 1; i <= fieldCount; i++, j++) {
					int dataType = mData.getColumnType(i);
					tStr = rs.getString(i);
					tStr = rs.wasNull() ? nullIs : CUtil.rightTrim(tStr);
					switch (dataType) {
					case -1:
					case 1:
					case 12:
						tStr = CUtil.replaceXmlSpChar(tStr);
						break;
					case 91:
					case 93:
						if (tStr.length() > 19) // 日期型
							if (tStr.substring(11, 19).equals("00:00:00"))
								tStr = tStr.substring(0, 10);
							else
								tStr = tStr.substring(0, 19);
						break;
					}

					if (keyIndex == i)
						if (tStr.equals(keyValue))
							tStr = "";
						else
							keyValue = tStr;

					aResult.append(" F" + j + "=\"" + tStr + "\"");
				}

				sResult.append("<R RL=\"" + RecordCount + "\" RT=\"1\" RS=\"0\" RR=\"" + RecordCount + "\""); // 记录状态
				sResult.append(aResult);
				sResult.append("/>" + _XmlCr);
			}

			if (RecordCount == 0) {
				sResult.append("<R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"0\"");
				for (int i = 1; i <= fieldCount; i++)
					sResult.append(" F" + i + "=\"\"");
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);
		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToXML9(ResultSet rs, int fixedCol, int keyIndex, String nullIs) throws SQLException {

		if (fixedCol > 0)
			return formatResultToXML90(rs, fixedCol, keyIndex, nullIs);
		else
			return formatResultToXML91(rs, keyIndex, nullIs);
	}

	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String formatDeltaToXML9(ResultSet rs, int fixedCol) throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = (fixedCol > 0) ? fixedCol : mData.getColumnCount();
			sResult.append("<RS>" + _XmlCr);
			sResult.append("<R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"0\""); // 记录状态
			// 把每个字段按 <字段名>值</字段名> 的方式组合
			for (int i = 1; i <= fieldCount; i++)
				sResult.append(" " + mData.getColumnName(i).toUpperCase() + "=\"\"");
			if (fixedCol > 0)
				sResult.append(_DeltaBuf);
			sResult.append("/>" + _XmlCr);
			sResult.append("</RS>" + _XmlCr);

		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	public String formatMetaDataToXML9(ResultSet rs, int fixedCol) throws SQLException {
		StringBuffer sResult = new StringBuffer();
		try {
			ResultSetMetaData mData = rs.getMetaData();

			int fieldCount = (fixedCol > 0) ? fixedCol : mData.getColumnCount();
			sResult.append("<FS>" + _XmlCr);

			for (int i = 1; i <= fieldCount; i++) {
				sResult.append("<F");
				sResult.append(" A=\"" + mData.getColumnName(i).toUpperCase() + "\""); // 字段名称
				sResult.append(" O=\"F" + i + "\""); // 字段别名
				sResult.append(" L=\"" + mData.getColumnLabel(i) + "\""); // 字段标题
				sResult.append(" B=\"" + mData.getColumnType(i) + "\""); // 字段类型
				sResult.append(" W=\"" + mData.getColumnDisplaySize(i) + "\""); // 宽度
				sResult.append(" D=\"" + mData.isNullable(i) + "\""); // 可空
				sResult.append(" P=\"" + mData.getPrecision(i) + "\"");
				sResult.append(" R=\"" + mData.isReadOnly(i) + "\"");
				sResult.append(" K=\"0\"");
				sResult.append(" S=\"1\"");
				sResult.append(" U=\"1\"");
				sResult.append(" C=\"\"");
				sResult.append(" E=\"\"");
				sResult.append(" F=\"\"");
				sResult.append(" H=\"\"");
				sResult.append(" M=\"\"");
				sResult.append(" BN=\"" + mData.getColumnTypeName(i) + "\""); // 字段名称
				sResult.append(" BC=\"" + mData.getCatalogName(i) + "\""); // 字段名称
				sResult.append("/>" + _XmlCr);
			}
			if (fixedCol > 0)
				sResult.append(_MetaBuf);
			sResult.append("</FS>" + _XmlCr);
		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[3]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatResultToXML10(ResultSet rs, int fixedCol, String[] keyField, String nullIs)
			throws SQLException {
		if (rs == null)
			return "";
		StringBuffer sResult = new StringBuffer();

		String tStr = null;
		String[] keyValue = new String[keyField.length]; // 主键值数组
		int nrow = 0, j, kCount = keyField.length; // 主键字段数
		int keyCount = __keyCodeValues.length; // 主键值数

		boolean noeof;
		try {
			ResultSetMetaData mData = rs.getMetaData();
			int fieldCount = mData.getColumnCount();
			RecordCount = 0; // 记录数设置为0
			sResult.append("<RS>" + _XmlCr);
			noeof = rs.next();

			while (noeof) {
				Result = 0; // 设置有数据标记
				RecordCount++; // 记录数增加
				j = 1;
				for (int x = 0; x < kCount; x++)
					keyValue[x] = rs.getString(keyField[x]);

				StringBuffer aResult = new StringBuffer();

				// 先设置固定列

				for (int i = 1; i <= fixedCol; i++, j++) {
					int dataType = mData.getColumnType(i);
					tStr = rs.getString(i);
					tStr = rs.wasNull() ? nullIs : CUtil.rightTrim(tStr);
					switch (dataType) {
					case -1:
					case 1:
					case 12:
						tStr = CUtil.replaceXmlSpChar(tStr);
						break;
					case 91:
					case 93:
						if (tStr.length() > 19) // 日期型
							if (tStr.substring(11, 19).equals("00:00:00"))
								tStr = tStr.substring(0, 10);
							else
								tStr = tStr.substring(0, 19);
						break;
					}
					aResult.append(" F" + j + "=\"" + tStr + "\"");
				}

				sResult.append("  <R RL=\"" + RecordCount + "\" RT=\"1\" RS=\"0\" RR=\"" + RecordCount + "\""); // 记录状态
				sResult.append(aResult);

				boolean bmark = true;
				int m = 1;
				int ki = 0, oki;
				while (noeof && bmark) {
					String fKeyValue = rs.getString(fixedCol + 1);
					oki = ki;
					while (fKeyValue.compareTo(__keyCodeValues[ki]) > 0 && ki < keyCount)
						ki++;
					while (oki < ki) {
						for (int i = fixedCol + 2; i <= fieldCount; i++, j++)
							sResult.append(" F" + j + "=\"\"");
						oki++;
					}
					ki++;
					for (int i = fixedCol + 2; i <= fieldCount; i++, j++, m++) {
						int dataType = mData.getColumnType(i);
						tStr = rs.getString(i);
						tStr = rs.wasNull() ? nullIs : CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1:
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
						sResult.append(" F" + j + "=\"" + tStr + "\"");
					}
					noeof = rs.next();
					if (noeof) {
						for (int x = 0; x < kCount && bmark; x++)
							bmark = keyValue[x].equals(rs.getString(keyField[x]));
					} else
						bmark = false;

					if (!bmark) {
						while (ki < keyCount) {
							for (int i = fixedCol + 2; i <= fieldCount; i++, j++)
								sResult.append(" F" + j + "=\"\"");
							ki++;
						}
						sResult.append("/>" + _XmlCr);
					}
				}
				nrow++;
			}

			if (RecordCount == 0) {
				sResult.append("  <R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"0\"");
				for (int i = 1; i <= fixedCol; i++)
					sResult.append(" F" + i + "=\"\"");

				int m = fixedCol + 1;

				for (j = 0; j < keyCount; j++)
					for (int i = fixedCol + 2; i <= fieldCount; i++, m++)
						sResult.append(" F" + m + "=\"\"");
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</RS>" + _XmlCr);
		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	protected String formatDeltaToXML10(ResultSet rs, int fixedCol) throws SQLException {
		if (rs == null)
			return "";

		StringBuffer sResult = new StringBuffer();
		try {
			ResultSetMetaData mData = rs.getMetaData();
			sResult.append("<RS>" + _XmlCr);
			sResult.append("<R RL=\"0\" RT=\"0\" RS=\"0\" RR=\"0\""); // 记录状态

			// 把每个字段按 <字段名>值</字段名> 的方式组合
			for (int i = 1; i <= fixedCol; i++)
				sResult.append(" " + mData.getColumnName(i).toUpperCase() + "=\"\"");

			if (RecordCount >= 0) {
				int keyCount = __keyCodeValues.length;
				int fieldCount = mData.getColumnCount();
				int m = fixedCol + 1;

				for (int j = 0; j < keyCount; j++)
					for (int i = fixedCol + 2; i <= fieldCount; i++, m++)
						sResult.append(" F" + m + "=\"\"");
			}
			sResult.append("/>" + _XmlCr);
			sResult.append("</RS>" + _XmlCr);

		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	public String formatMetaDataToXML10(ResultSet rs, int fixedCol) throws SQLException {
		StringBuffer sResult = new StringBuffer();
		try {
			ResultSetMetaData mData = rs.getMetaData();
			sResult.append("<FS>" + _XmlCr);
			for (int i = 1; i <= fixedCol; i++) {
				sResult.append("<F");
				sResult.append(" A=\"" + mData.getColumnName(i).toUpperCase() + "\""); // 字段名称
				sResult.append(" O=\"F" + i + "\""); // 字段别名
				sResult.append(" L=\"" + mData.getColumnLabel(i) + "\""); // 字段标题
				sResult.append(" B=\"" + mData.getColumnType(i) + "\""); // 字段类型
				sResult.append(" W=\"" + mData.getColumnDisplaySize(i) + "\""); // 宽度
				sResult.append(" D=\"" + mData.isNullable(i) + "\""); // 可空
				sResult.append(" P=\"" + mData.getPrecision(i) + "\"");
				sResult.append(" R=\"" + mData.isReadOnly(i) + "\"");
				sResult.append(" K=\"0\"");
				sResult.append(" S=\"1\"");
				sResult.append(" U=\"1\"");
				sResult.append(" C=\"\"");
				sResult.append(" E=\"\"");
				sResult.append(" F=\"\"");
				sResult.append(" H=\"\"");
				sResult.append(" M=\"\"");
				sResult.append("/>" + _XmlCr);
			}

			if (RecordCount >= 0) {
				int keyCount = __keyCodeValues.length;
				int fieldCount = mData.getColumnCount();
				int m = fixedCol + 1;

				for (int j = 0; j < keyCount; j++)
					for (int i = fixedCol + 2; i <= fieldCount; i++, m++) {
						sResult.append("   <F");
						sResult.append(" A=\"F" + m + "\""); // 字段名称
						sResult.append(" O=\"F" + m + "\""); // 字段别名
						sResult.append(" L=\"" + mData.getColumnLabel(i) + "\""); // 字段标题
						sResult.append(" B=\"" + mData.getColumnType(i) + "\""); // 字段类型
						sResult.append(" W=\"" + mData.getColumnDisplaySize(i) + "\""); // 宽度
						sResult.append(" D=\"" + mData.isNullable(i) + "\""); // 可空
						sResult.append(" P=\"" + mData.getPrecision(i) + "\"");
						sResult.append(" R=\"" + mData.isReadOnly(i) + "\"");
						sResult.append(" K=\"0\"");
						sResult.append(" S=\"1\"");
						sResult.append(" U=\"1\"");
						sResult.append(" C=\"\"");
						sResult.append(" E=\"\"");
						sResult.append(" F=\"\"");
						sResult.append(" H=\"\"");
						sResult.append(" M=\"\"");
						sResult.append("/>" + _XmlCr);
					}
			}
			sResult.append("</FS>" + _XmlCr);
		} catch (SQLException ex) {
			throw new SQLException("格式化数据产生异常[3]：\n " + ex.getMessage());
		}
		return sResult.toString();
	}

	public int getUserCount(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, HttpSession> colusers = (Map<String, HttpSession>) (request.getSession().getServletContext()
				.getAttribute("olusers"));
		String safefield = CInitParam.safeField;
		int iRecNo = 0;
		HttpSession usession = null;
		Iterator<String> it = colusers.keySet().iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			usession = (HttpSession) colusers.get(key);
			if (usession.getAttribute(safefield) != null)
				iRecNo++;
		}
		return iRecNo;
	}

	protected String formatToAdoRecordset() throws SQLException {
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			sResult.append("<xml xmlns:s='uuid:BDC6E3F0-6DA3-11d1-A2A3-00AA00C14882' ");
			sResult.append("xmlns:dt='uuid:C2F41010-65B3-11d1-A29F-00AA00C14882' ");
			sResult.append("xmlns:rs='urn:schemas-microsoft-com:rowset' ");
			sResult.append("xmlns:z='#RowsetSchema'>");
			sResult.append("<s:Schema id='RowsetSchema'>");
			sResult.append(" <s:ElementType name='row' content='eltOnly' rs:CommandTimeout='30'>");

			ResultSetMetaData mData = rs.getMetaData();
			for (int i = 1; i <= mData.getColumnCount(); i++) {
				String datatype = "string";
				switch (mData.getColumnType(i)) {
				case -5:
				case -6:
				case 4:
				case 5:
					datatype = "int";
					break;
				case 3:
				case 6:
				case 7:
					datatype = "float";
					break;
				case 93:
					datatype = "datetime";
				}
				sResult.append("<s:AttributeType name='" + mData.getColumnName(i) + "' rs:number='" + i
						+ "' rs:writeunknown='true'>");
				sResult.append("	<s:datatype dt:type='" + datatype + "' dt:maxLength='100' rs:maybenull='true'/>");
				sResult.append("</s:AttributeType>");
			}

			sResult.append("<s:extends type='rs:rowbase'/>");
			sResult.append(" </s:ElementType>");
			sResult.append("</s:Schema>");

			sResult.append("<rs:data>");
			while (rs.next()) {
				sResult.append("<z:row");
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					switch (dataType) {
					case -1:
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatToAdoRecordset)：\n " + ex.getMessage());
						} finally {
						}
						break;
					default:
						tStr = rs.getString(i);
					}
					if (rs.wasNull())
						tStr = "";
					else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
					}
					sResult.append(" " + mData.getColumnName(i) + "=\"" + tStr + "\"");
				}
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</rs:data>");
			sResult.append("</xml>");
		} catch (SQLException ex) {
			P("格式化数据产生异常1：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		} finally {
			closeConn();
		}
		return sResult.toString();
	}

	protected String formatToAdoRecordset(String[] fieldNames, String[] HECount, String sChar) throws SQLException {

		StringBuffer sResult = new StringBuffer();

		String tStr = null;
		try {
			sResult.append("<xml xmlns:s='uuid:BDC6E3F0-6DA3-11d1-A2A3-00AA00C14882' ");
			sResult.append("xmlns:dt='uuid:C2F41010-65B3-11d1-A29F-00AA00C14882' ");
			sResult.append("xmlns:rs='urn:schemas-microsoft-com:rowset' ");
			sResult.append("xmlns:z='#RowsetSchema'>");
			sResult.append("<s:Schema id='RowsetSchema'>");
			sResult.append(" <s:ElementType name='row' content='eltOnly' rs:CommandTimeout='30'>");

			ResultSetMetaData mData = rs.getMetaData();
			int[] nFields = new int[mData.getColumnCount() + 1];

			for (int i = 1, j = 1; i <= mData.getColumnCount(); i++) {
				String datatype = "string";
				switch (mData.getColumnType(i)) {
				case -5:
				case -6:
				case 4:
				case 5:
					datatype = "int";
					break;
				case 3:
				case 6:
				case 7:
					datatype = "float";
					break;
				case 93:
					datatype = "datetime";
				}
				String fName = mData.getColumnName(i);
				int n = CUtil.findElementInArray(fieldNames, fName);
				if (n > -1) {
					nFields[i] = Integer.parseInt(HECount[n]);
					for (int k = 1; k <= nFields[i]; k++) {
						sResult.append("<s:AttributeType name='" + fName + k + "' rs:number='" + j
								+ "' rs:writeunknown='true'>");
						sResult.append(
								"	<s:datatype dt:type='" + datatype + "' dt:maxLength='100' rs:maybenull='true'/>");
						sResult.append("</s:AttributeType>");
						j++;
					}
				} else {
					nFields[i] = -1;
					sResult.append(
							"<s:AttributeType name='" + fName + "' rs:number='" + j + "' rs:writeunknown='true'>");
					sResult.append(
							"	<s:datatype dt:type='" + datatype + "' dt:maxLength='100' rs:maybenull='true'/>");
					sResult.append("</s:AttributeType>");
					j++;
				}
			}

			sResult.append("<s:extends type='rs:rowbase'/>");
			sResult.append(" </s:ElementType>");
			sResult.append("</s:Schema>");

			sResult.append("<rs:data>");
			while (rs.next()) {
				sResult.append("<z:row");
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					switch (dataType) {
					case -1:
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatToAdoRecordset)：\n " + ex.getMessage());
						} finally {
						}
						break;
					default:
						tStr = rs.getString(i);
					}
					if (rs.wasNull())
						tStr = "";
					else {
						// tStr = tStr.trim();
						tStr = CUtil.rightTrim(tStr);
						switch (dataType) {
						case -1: // sqlserver text类型
						case 1:
						case 12:
							tStr = CUtil.replaceXmlSpChar(tStr);
							break;
						case 91:
						case 93:
							if (tStr.length() > 19) // 日期型
								if (tStr.substring(11, 19).equals("00:00:00"))
									tStr = tStr.substring(0, 10);
								else
									tStr = tStr.substring(0, 19);
							break;
						}
					}

					String fName = mData.getColumnName(i);

					int n = nFields[i];
					if (n > -1) {
						String[] values = tStr.split(sChar);
						int j = 0, vlen = values.length;
						for (int k = 1; k <= n; k++) {
							sResult.append(" " + fName + k + "=\"" + values[j] + "\"");
							if (j < vlen - 1)
								j++;
						}
					} else
						sResult.append(" " + fName + "=\"" + tStr + "\"");
				}
				sResult.append("/>" + _XmlCr);
			}
			sResult.append("</rs:data>");
			sResult.append("</xml>");
		} catch (SQLException ex) {
			P("格式化数据产生异常1：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		} finally {
			closeConn();
		}
		return sResult.toString();
	}

	private String getDateStrDelphi(Timestamp dDate) {
		String tStr = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS");
		tStr = df.format(dDate);
		return tStr;
	}

	protected String formatToClientXml(String lexIconName) throws SQLException {
		StringBuffer sResult = new StringBuffer();
		String tStr = null;
		try {
			// sResult.append("<?xml version=\"1.0\" standalone=\"yes\"?>\n");

			ResultSetMetaData mData = rs.getMetaData();

			ResultSet rsLexicon = null;
			PreparedStatement istmt = null;
			Vector<String> sFieldName = new Vector<String>();
			Vector<String> sFieldLabel = new Vector<String>();

			boolean bLexicon = false;
			if (!lexIconName.equals(""))
				try {
					if (conn == null)
						conn = getConnection();
					istmt = conn.prepareStatement(
							"SELECT F_NAME,F_LABEL " + "FROM SYS_LEXICOND WHERE T_ID=? ORDER BY F_XH");
					istmt.setString(1, lexIconName);
					rsLexicon = istmt.executeQuery();

					while (rsLexicon.next()) {
						sFieldName.add(rsLexicon.getString("F_NAME").toUpperCase());
						sFieldLabel.add(rsLexicon.getString("F_LABEL"));
					}
					bLexicon = true;
				} finally {
					rsLexicon.close();
					istmt.close();
				}
			sResult.append("<DATAPACKET Version=\"2.0\">\n");
			sResult.append("<METADATA>\n");
			sResult.append("<FIELDS>\n");

			for (int i = 1; i <= mData.getColumnCount(); i++) {
				int nw = 0, dnw = 0;
				String lx = mData.getColumnTypeName(i);
				String sStr = "";
				String fieldName = mData.getColumnName(i).toUpperCase();
				String fieldLabel = fieldName;
				if (bLexicon) {
					int k = sFieldName.indexOf(fieldLabel);
					if (k > -1)
						fieldLabel = CUtil.replaceXmlSpChar(sFieldLabel.get(k).toString());
				}
				String v = "<FIELD attrname=\"" + fieldName + "\" displaylabel=\"" + fieldLabel + "\" fieldtype";
				switch (mData.getColumnType(i)) {
				case -5:
					sResult.append(v + "=\"i8\"/>");
					break;
				case -2:
					nw = mData.getColumnDisplaySize(i);
					if (lx.equals("binary"))
						sResult.append(v + "=\"bin.hex\" WIDTH=\"" + (nw / 2) + "\"/>");
					else
						sResult.append(v + "=\"bin.hex\" WIDTH=\"8\"/>");
					break;
				case -7:
					sResult.append(v + "=\"boolean\"/>");
					break;
				case 1:
					nw = mData.getColumnDisplaySize(i);
					if (lx.equals("char"))
						sResult.append(v + "=\"string\" SUBTYPE=\"FixedChar\" WIDTH=\"" + nw + "\"/>");
					else if (lx.equals("nchar"))
						sResult.append(v + "=\"string.uni\" WIDTH=\"" + (nw * 2) + "\"/>");
					else
						sResult.append(v + "=\"string\" SUBTYPE=\"Guid\" WIDTH=\"38\"/>");
					break;
				case 93:
					sResult.append(v + "=\"dateTime\"/>");
					break;
				case 2:
				case 3:
					nw = mData.getPrecision(i);
					dnw = mData.getScale(i);
					if (dnw > 0)
						sStr = "DECIMALS=\"" + dnw + "\" ";
					sResult.append(v + "=\"fixed\" " + sStr + "WIDTH=\"" + nw + "\"/>");
					break;
				case 6:
					sResult.append(v + "=\"r8\"/>");
					break;
				case -4:
					sResult.append(v + "=\"bin.hex\" SUBTYPE=\"Binary\"/>");
					break;
				case 4:
					sResult.append(v + "=\"i4\"/>");
					break;
				case -1:
					sResult.append(v + "=\"bin.hex\" SUBTYPE=\"Text\"/>");
					break;
				case 12:
					nw = mData.getColumnDisplaySize(i);
					if (lx.equals("varchar"))
						sResult.append(v + "=\"string\" WIDTH=\"" + nw + "\"/>");
					else
						sResult.append(v + "=\"string.uni\" WIDTH=\"" + (nw * 2) + "\"/>");
					break;
				case 7:
					sResult.append(v + "=\"r8\"/>");
					break;
				case 5:
					sResult.append(v + "=\"i2\"/>");
					break;
				case -6:
					sResult.append(v + "=\"i2\"/>");
					break;
				case -3:
					nw = mData.getColumnDisplaySize(i);
					sResult.append(v + "=\"bin.hex\" SUBTYPE=\"Binary\" WIDTH=\"" + (nw / 2) + "\"/>");
				}
				sResult.append("\n");
			}
			sResult.append("</FIELDS>\n");
			sResult.append("<PARAMS/>\n");
			sResult.append("</METADATA>\n");

			sResult.append("<ROWDATA>");
			while (rs.next()) {
				sResult.append("<ROW");
				for (int i = 1; i <= mData.getColumnCount(); i++) {
					int dataType = mData.getColumnType(i);
					String fname = mData.getColumnName(i).toUpperCase();

					switch (dataType) {
					case -1:
						tStr = "";
						try {
							tStr = getBlobField(i);
							/*
							 * byte b[] = rs.getBytes(i); if (b.length > 0) {
							 * tStr = new String(b, "gb2312"); }
							 */
							if (rs.wasNull())
								tStr = "";
						} catch (Exception ex) {
							System.out.println("获取备注型数据产生异常(formatToClientXml)：\n " + ex.getMessage());
						} finally {
						}
						break;
					default:
						if (dataType == 91 || dataType == 93) {
							Timestamp t = rs.getTimestamp(i);
							if (rs.wasNull())
								tStr = "";
							else
								tStr = getDateStrDelphi(t);
						} else {
							tStr = rs.getString(i);
							if (rs.wasNull())
								tStr = "";
						}
					}

					// tStr = tStr.trim();
					tStr = CUtil.rightTrim(tStr);
					switch (dataType) {
					case -1: // sqlserver text类型
					case 1:
					case 12:
						tStr = CUtil.replaceXmlSpChar(tStr);
						break;
					case 91:
					case 93:
						tStr = CUtil.replaceStr(tStr, "T", " ");
						break;
					}
					sResult.append(" " + fname + "=\"" + tStr + "\"");
				}
				sResult.append("/>\n");
			}
			sResult.append("</ROWDATA>");
			sResult.append("</DATAPACKET>");
		} catch (SQLException ex) {
			P("格式化数据产生异常1：\n " + ex.getMessage());
			throw new SQLException("格式化数据产生异常[1]：\n " + ex.getMessage());
		} finally {
			closeConn();
		}
		return sResult.toString();
	}

	protected int CheckSn(String sn, String osn, String afx1, String afx2, int v) {
		int f = 0;
		int nLen = sn.length();
		int j = 1;
		String sn1 = "", sn2 = "";
		for (int i = 0; i < nLen; i++) {
			f = f + j * (int) sn.charAt(i);
			j += i * 100;
		}
		sn2 = f + afx1;

		j = 1;
		f = v;
		for (int i = 0; i < nLen; i++) {
			f = f + j * (int) sn.charAt(i);
			j += i * 11;
		}
		sn1 = f + afx2;

		sn1 = sn1.substring(0, 4) + "-" + sn1.substring(5, 9) + "-" + sn2.substring(0, 9);

		P("序列号验证完成.");
		if (sn1 == osn)
			return 1;
		else
			return 0;
	}

	protected boolean isNullOrEmpty(String str) {
		return str == null || str.equals("");
	}
}
