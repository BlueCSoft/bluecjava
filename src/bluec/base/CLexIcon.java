/*
 * Copyright BlueC Soft
 * project 蓝洲软件
 * Comment
 * JDK Version 1.4.2
 * Created on ${date}
 * version 1.0
 * Modify history
 */

package bluec.base;

/**
 * <p>
 * Title: 基本数据类
 * </p>
 * <p>
 * 获取基本数据字典的类
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
import java.sql.*;

import javax.servlet.http.*;

import java.util.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.servlet.ServletInputStream;

/**
 * 目前不清楚该类的具体作用
 * 
 * @author LSH
 * 
 */
public class CLexIcon extends CTreeView {
	
	private String[] __keyFields = null;

	private int __fixedCol = 0;

	private int __xmlFormat = 0;

	private int __pagesize = 0;

	private String __lexIconName = "";

	public CLexIcon() {
	}

	/**
	 * 
	 * @param lexiconID
	 * @return
	 */
	public String[] getLexiconName(String lexiconID) throws SQLException {
		Vector<String> v = new Vector<String>();
		rs = executeQuery("SELECT DIR_NAME FROM DIR_PUB " + "WHERE DIR_ID='" + lexiconID + "'");
		try {
			while (rs.next()) {
				v.addElement(rs.getString(1));
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		} finally {
			closeConn();
		}
		String[] s = new String[v.size()];
		v.copyInto(s);
		return s;
	}


	public void showNode(Node node, String space) {
		// if(node.getNodeType()!=3)
		System.out.print(space + "NodeName:" + node.getNodeName() + "  NodeType:" + node.getNodeType()
				+ "\n");
		if (node.hasChildNodes()) {
			NodeList nList = node.getChildNodes();
			for (int i = 0; i < nList.getLength(); i++)
				showNode(nList.item(i), "  " + space);
		}
	}

	public String getTreeViewInfo(HttpServletRequest request) {
		String infMsg = "";
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ServletInputStream in = request.getInputStream();
			Document doc = builder.parse(in); // 建立文档
			Node node = doc.getFirstChild().getFirstChild().getNextSibling();

			node = node.getFirstChild().getNextSibling();

			node = node.getNextSibling().getNextSibling();
			String sqlid = node.getFirstChild().getNodeValue();

			node = node.getNextSibling().getNextSibling();

			NamedNodeMap map = node.getAttributes();
			int pcount = map.getLength(); // 参数个数
			String[] params = new String[pcount + 1];

			for (int i = 0; i < pcount; i++) {
				params[i] = map.item(i).getNodeValue();
				if (params[i] == null)
					params[i] = "";
			}

			params[pcount] = "";

			node = node.getNextSibling().getNextSibling();

			map = node.getAttributes();
			// System.out.println(map.getLength());

			int rLevel = Integer.parseInt(map.item(0).getNodeValue());
			int checkbox = Integer.parseInt(map.item(1).getNodeValue());
			int showcode = Integer.parseInt(map.item(2).getNodeValue());
			int isexpand = Integer.parseInt(map.item(3).getNodeValue());

			infMsg = "<xml><datas><data>"
					+ CUtil.replaceXmlSpChar(super.getTreeViewInfoEx(sqlid, params, rLevel, checkbox,
							showcode, isexpand, "", request.getSession())) + "</data></datas></xml>";

		} catch (Exception pcException) {
			infMsg = "<xml><errors><error>获取数据失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public int execSql(HttpServletRequest request) {
		int infMsg = 0;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// get a DocumentBuilder for building the DOM tree
			DocumentBuilder builder = factory.newDocumentBuilder();
			// create a new Document
			ServletInputStream in = request.getInputStream();
			Document doc = builder.parse(in); // 建立文档
			// showNode(doc," ");
			Node node = doc.getFirstChild().getFirstChild().getNextSibling();
			// showNode(node," ");
			node = node.getFirstChild().getNextSibling();

			String sqlid = node.getFirstChild().getNodeValue();
			node = node.getNextSibling().getNextSibling();

			node = node.getNextSibling().getNextSibling();

			infMsg = updateBySql(sqlid, request.getSession());
		} catch (Exception pcException) {
			System.out.println(pcException.getMessage());
			infMsg = 1;
		} finally {
		}
		return infMsg;
	}

	public boolean getData(HttpServletRequest request) {
		boolean bMark = false;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ServletInputStream in = request.getInputStream();
			Document doc = builder.parse(in); // 建立文档
			
			/*
			 * byte bs[] = new byte[10000]; in.read(bs,0,100);
			 */
			Node node = doc.getElementsByTagName("sqltype").item(0);
			//if(node!=null)
			//	System.out.println(node.getFirstChild().getNodeValue());
			
			int sqltype = Integer.parseInt(node.getFirstChild().getNodeValue());
			
			node = doc.getElementsByTagName("sqls").item(0);
			String sqls = node.getFirstChild().getNodeValue();
			
			node = doc.getElementsByTagName("params").item(0);
			NamedNodeMap map = node.getAttributes();
			//System.out.println("hear1");
			int pcount = map.getLength(); // 参数个数
           
			if (pcount == 0) {
				if (sqltype/10 == 1){ // 直接sql语句
					queryBySqlInner(sqls);
				}else{
					queryBySqlIDInner(sqls);
				}
			} else {
				String[] params = new String[pcount];
				for (int i = 0; i < pcount; i++) {
					params[i] = map.item(i).getNodeValue();
					if (params[i] == null)
						params[i] = "";
				}
				if (sqltype/10 == 1){ // 直接sql语句
					queryBySqlInner(sqls, params);
				}else{
					queryBySqlIDWithParamInner(sqls, params);
				}
			}
			//System.out.println("hear2");
			node = doc.getElementsByTagName("pagesize").item(0);
			__pagesize = Integer.parseInt(node.getFirstChild().getNodeValue());
			//System.out.println("hear3");
			node = doc.getElementsByTagName("dataatt").item(0);
			__xmlFormat = Integer.parseInt(node.getFirstChild().getNodeValue());
			//System.out.println("hear4");
			
			node = doc.getElementsByTagName("lexicon").item(0);
			if(node.getFirstChild()!=null)
   		      __lexIconName = node.getFirstChild().getNodeValue();

   		    node = doc.getElementsByTagName("keyvalues").item(0);
			map = node.getAttributes();
			pcount = map.getLength(); // 参数个数
			if (pcount > 0) {
				__keyCodeValues = new String[pcount];
				for (int i = 0; i < pcount; i++) {
					__keyCodeValues[i] = map.item(i).getNodeValue();
					if (__keyCodeValues[i] == null)
						__keyCodeValues[i] = "";
				}
			}

			node = doc.getElementsByTagName("fixedcol").item(0);
			__fixedCol = Integer.parseInt(node.getFirstChild().getNodeValue());

			node = doc.getElementsByTagName("keyfield").item(0);
			map = node.getAttributes();
			pcount = map.getLength(); // 参数个数
			if (pcount > 0) {
				__keyFields = new String[pcount];
				for (int i = 0; i < pcount; i++) {
					__keyFields[i] = map.item(i).getNodeValue();
					if (__keyFields[i] == null)
						__keyFields[i] = "";
				}
			}
			bMark = true;
		} catch (Exception pcException) {
			System.out.println("getData:" + pcException.getMessage());
			closeConn();
		}
		return bMark;
	}

	public String getRecords(HttpServletRequest request) {
		String infMsg = "0";
		try {
			if (getData(request)) {
				
				switch (__xmlFormat) {
				case 1:// 普通格式纯数据，不包含字段描述信息
					infMsg = formatToXmlOnlyData("bluec");
					break;
				case 2:
				case 3:// 列转行纯数据
					infMsg = formatToXml10OnlyData(__fixedCol, __keyFields);
					break;
				case 4:// Excel透视表格式数据
					infMsg = formatToAdoRecordset(__keyFields, __keyCodeValues, "-@-");
					break;
				case 5:// 列转行数据
					infMsg = formatToXml9OnlyData(__fixedCol, Integer.parseInt(__keyCodeValues[0]));
					break;
				case 6:
					infMsg = formatToClientXml(__lexIconName);
					//System.out.println(infMsg);
					break;
				case 7:// 带字段描述信息的数据
				case 8:
					infMsg = formatToXmlData("bluec");
					break;
				case 9:
                    infMsg = resultSetToXml(__lexIconName);
                    break;
                case 10:
                    
                    break;
				default:

					if (__xmlFormat > 100)
						infMsg = formatToXml9OnlyData(__fixedCol, __xmlFormat - 100);
					else
						infMsg = formatResultToXML4("bluec");
				}

			} else
				infMsg = _errorInf;
		} catch (Exception pcException) {
			System.out.println("getRecords:"+pcException.getMessage());
			infMsg = "<xml><errors><error>获取数据失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public String getRecordsEx(HttpServletRequest request) {
		String infMsg = "0";
		try {
			if (getData(request)) {
				if (__pagesize > 0)
					infMsg = formatToXmlOnlyData("bluec", __pagesize);
				else
					infMsg = formatToXmlOnlyData("bluec");
			} else
				infMsg = _errorInf;
		} catch (Exception pcException) {
			System.out.println(pcException.getMessage());
			infMsg = "<xml><errors><error>获取数据失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public boolean getDataIPad(HttpServletRequest request) {
		boolean bMark = false;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ServletInputStream in = request.getInputStream();
			Document doc = builder.parse(in); // 建立文档

			/*
			 * byte bs[] = new byte[10000]; in.read(bs,0,100);
			 */

			Node node = doc.getFirstChild().getFirstChild().getNextSibling();

			node = node.getFirstChild().getNextSibling();
			int sqltype = Integer.parseInt(node.getFirstChild().getNodeValue());

			node = node.getNextSibling().getNextSibling();
			String sqls = node.getFirstChild().getNodeValue();

			node = node.getNextSibling().getNextSibling();

			NamedNodeMap map = node.getAttributes();

			int pcount = map.getLength(); // 参数个数

			if (pcount == 0) {
				if (sqltype == 0) // 直接sql语句
					queryBySqlInner(sqls);
				else
					queryBySqlIDInner(sqls);
			} else {
				String[] params = new String[pcount];
				for (int i = 0; i < pcount; i++) {
					params[i] = map.item(i).getNodeValue();
					if (params[i] == null)
						params[i] = "";
				}
				if (sqltype == 0) // 直接sql语句
					queryBySqlInner(sqls, params);
				else
					queryBySqlIDWithParamInner(sqls, params);
			}

			node = node.getNextSibling().getNextSibling();
			__pagesize = Integer.parseInt(node.getFirstChild().getNodeValue());
			node = node.getNextSibling().getNextSibling();
			__xmlFormat = Integer.parseInt(node.getFirstChild().getNodeValue());

			node = node.getNextSibling().getNextSibling();
			if (node.getFirstChild() != null)
				__lexIconName = node.getFirstChild().getNodeValue();

			node = node.getNextSibling().getNextSibling();
			map = node.getAttributes();
			pcount = map.getLength(); // 参数个数
			if (pcount > 0) {
				__keyCodeValues = new String[pcount];
				for (int i = 0; i < pcount; i++) {
					__keyCodeValues[i] = map.item(i).getNodeValue();
					if (__keyCodeValues[i] == null)
						__keyCodeValues[i] = "";
				}
			}

			node = node.getNextSibling().getNextSibling();
			__fixedCol = Integer.parseInt(node.getFirstChild().getNodeValue());

			node = node.getNextSibling().getNextSibling();
			map = node.getAttributes();
			pcount = map.getLength(); // 参数个数
			if (pcount > 0) {
				__keyFields = new String[pcount];
				for (int i = 0; i < pcount; i++) {
					__keyFields[i] = map.item(i).getNodeValue();
					if (__keyFields[i] == null)
						__keyFields[i] = "";
				}
			}
			bMark = true;
		} catch (Exception pcException) {
			System.out.println("getData:" + pcException.getMessage());
			closeConn();
		}
		return bMark;
	}

	public String getRecordsIPad(HttpServletRequest request) {
		String infMsg = "0";
		try {
			if (getDataIPad(request)) {
				switch (__xmlFormat) {
				case 1:// 普通格式纯数据，不包含字段描述信息
					infMsg = formatToXmlOnlyData("bluec");
					break;
				case 2:
				case 3:// 列转行纯数据
					infMsg = formatToXml10OnlyData(__fixedCol, __keyFields);
					break;
				case 4:// Excel透视表格式数据
					infMsg = formatToAdoRecordset(__keyFields, __keyCodeValues, "-@-");
					break;
				case 5:// 列转行数据
					infMsg = formatToXml9OnlyData(__fixedCol, Integer.parseInt(__keyCodeValues[0]));
					break;
				case 6:
					infMsg = formatToClientXml(__lexIconName);
					break;
				default:

					if (__xmlFormat > 100)
						infMsg = formatToXml9OnlyData(__fixedCol, __xmlFormat - 100);
					else
						infMsg = formatResultToXML4("bluec");
				}

			} else
				infMsg = _errorInf;
		} catch (Exception pcException) {
			System.out.println(pcException.getMessage());
			infMsg = "<xml><errors><error>获取数据失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public String getRecordsExIPad(HttpServletRequest request) {
		String infMsg = "0";
		try {
			if (getDataIPad(request)) {
				if (__pagesize > 0)
					infMsg = formatToXmlOnlyData("bluec", __pagesize);
				else
					infMsg = formatToXmlOnlyData("bluec");
			} else
				infMsg = _errorInf;
		} catch (Exception pcException) {
			System.out.println(pcException.getMessage());
			infMsg = "<xml><errors><error>获取数据失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public String getAdoRecordset(HttpServletRequest request) {
		String infMsg = "0";
		try {
			if (getData(request)) {
				infMsg = formatToAdoRecordset();
			} else
				infMsg = _errorInf;
		} catch (Exception pcException) {
			System.out.println(pcException.getMessage());
			infMsg = "<xml><errors><error>获取数据失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}


	/**
	 * 获取序列号值
	 * 
	 * @param request
	 *          页面请求，包含序列名称
	 * @return 序列值
	 */
	public String getMaxId(HttpServletRequest request) {
		String infMsg = "";
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();

			ServletInputStream in = request.getInputStream();

			Document doc = builder.parse(in); // 建立文档

			Node node = doc.getFirstChild().getFirstChild().getNextSibling();

			node = node.getFirstChild().getNextSibling();

			node = node.getNextSibling().getNextSibling();
			String sqName = node.getFirstChild().getNodeValue();

			if (_databaseType == 0) {
				// 序列名称
				if (sqName.equals(""))
					sqName = "SQ_MAXID"; // 默认序列

				executeQuery("SELECT " + sqName + ".Nextval FROM dual");

				if (rs.next())
					infMsg = "<xml><sq><value>" + rs.getInt(1) + "</value></sq></xml>";
			} else {
				String[] params = new String[1];
				params[0] = sqName;
				if (executeMsSqlProc("GetSequence", params, request.getSession()) > 0)
					infMsg = "<xml><sq><value>" + RecordCount + "</value></sq></xml>";
				else
					infMsg = "<xml><errors><error>获取序列值失败</error></errors></xml>";
			}
		} catch (Exception pcException) {
			infMsg = "<xml><errors><error>获取序列值失败</error></errors></xml>";
		} finally {
			closeConn();
		}
		return infMsg;
	}

	public int getMaxId(String seqid, HttpSession session) {
		int result = 0;
		try {
			if (_databaseType == 0) {
				// 序列名称
				if (seqid.equals(""))
					seqid = "SQ_MAXID"; // 默认序列

				executeQuery("SELECT " + seqid + ".Nextval FROM dual");

				if (rs.next())
					result = rs.getInt(1);
			} else {
				String[] params = new String[1];
				params[0] = seqid;
				if (executeMsSqlProc("GetSequence", params, session) > 0)
					result = RecordCount;
				else
					result = -1;
			}
		} catch (Exception pcException) {
			result = -1;
		} finally {
			closeConn();
		}
		return result;
	}

	public String createDCube(String sqlId, String pvtAName, HttpSession session) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		try {
			queryBySql("select * from sys_sqlds_d where sql_id='%s'", sqlId, session);
			while (next()) {
				int nType = getInt("TOTALID");
				String fName = getString("FIELDNAME");
				String dFormat = getString("DISPLAYFROMAT");

				buffer.append(pvtAName + ".FieldSets(\"" + fName + "\").Caption =\""
						+ getString("DISPLAYLABEL") + "\";\n");
				buffer.append(pvtAName + ".FieldSets(\"" + fName + "\").Fields(0).Caption =\""
						+ rs.getString("DISPLAYLABEL") + "\";\n");

				if (!dFormat.equals(""))
					buffer.append(pvtAName + ".FieldSets(\"" + fName + "\").Fields(0).NumberFormat = \""
							+ dFormat + "\";");

				switch (nType) {
				case 1:
					buffer.append(pvtAName + ".FieldSets(\"" + fName + "\").Fields(0).IsIncluded = true;\n");
					buffer.append(pvtAName + ".RowAxis.InsertFieldSet(" + pvtAName + ".FieldSets(\"" + fName
							+ "\"));\n");
					break;
				case 2:
					buffer.append(pvtAName + ".FieldSets(\"" + fName + "\").Fields(0).IsIncluded = true;\n");
					buffer.append(pvtAName + ".ColumnAxis.InsertFieldSet(" + pvtAName + ".FieldSets(\""
							+ fName + "\"));\n");
					break;
				case 3:
					buffer.append("var ctotal=" + pvtAName + ".AddTotal(\"" + rs.getString("DISPLAYLABEL")
							+ "\"," + pvtAName + ".FieldSets(\"" + fName
							+ "\").Fields(0), pvtconstants.plFunctionSum);\n");
					buffer.append(pvtAName + ".DataAxis.InsertTotal(ctotal, 0);\n");
					break;
				case 4:
					buffer.append(pvtAName + ".FieldSets(\"" + fName + "\").Fields(0).IsIncluded = true;\n");
					buffer.append(pvtAName + ".FilterAxis.InsertFieldSet(" + pvtAName + ".FieldSets(\""
							+ fName + "\"));\n");
					break;
				}
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		} finally {
			closeConn();
		}
		return buffer.toString();
	}

	public String getQrGridCreateString(boolean hasCheckBox) throws SQLException {
		String SELFIELD = getString("SELFIELD");
		String Result = (hasCheckBox) ? "true" : "false";
		int rowheight = getInt("ROWHEIGHT");
		String fixedcol = (getInt("FIXEDCOL") == 1) ? "true" : "false";
		String wordbreak = (getInt("WORDBREAK") == 1) ? "true" : "false";

		return Result + ",\"" + SELFIELD + "\"," + rowheight + "," + fixedcol + "," + wordbreak;
	}

	public String getQrGridColumnString() throws SQLException {
		String Result = "\"" + getString("FIELDTITLE") + "\"," + "\"" + getString("FIELDNAME") + "\","
				+ "\"" + getString("ALIGNMENT") + "\",";

		String Style = (!getString("FONTNAME").equals("")) ? "font-family:" + getString("FONTNAME")
				+ ";" : "";

		if (getInt("FONTSIZE") != 0)
			Style += "font-size:" + getInt("FONTSIZE") + "px;";

		if (getInt("FONTSTYLE") != 0)
			Style += "font-style:italic;";

		if (getInt("FONTWEIGHT") != 0)
			Style += "font-weight:bold;";

		if (getInt("FONTUNDERLING") != 0)
			Style += "text-decoration:underline;";

		if (!getString("FONTCOLOR").equals(""))
			Style += "color:" + getString("FONTCOLOR") + ";";

		if (!getString("BGCOLOR").equals(""))
			Style += "background:" + getString("BGCOLOR") + ";";

		String IsLink = (getInt("ISLINK") == 1) ? "true" : "false";

		return Result + "\"" + Style + "\",\"" + getString("DATAFORMAT") + "\"," + getInt("WIDTH")
				+ "," + IsLink;
	}

	public String CreateQrGrid(String GridName, String LexIconId, boolean hasCheckBox,
			HttpSession session) throws SQLException {
		StringBuilder buffer = new StringBuilder();
		queryBySql(
				"select SELFIELD,ROWHEIGHT,FIXEDCOL,WORDBREAK from sysi_lexicons where lexiconid='%s'",
				LexIconId, session);
		if (next()) {
			buffer.append(GridName + " = new TQRGrid(" + getQrGridCreateString(hasCheckBox) + ");\n");
			queryBySql("select * from sysi_lexiconsd where lexiconid='%s'", LexIconId, session);
			while (next()) {
				buffer.append(GridName + ".addColumn(" + getQrGridColumnString() + ");\n");
			}
			buffer.append(GridName + ".CreateGrid();");
		}
		return buffer.toString();
	}
}
