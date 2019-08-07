package bluec.base;

/**
 * <p>Title: TreeView处理</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.sql.*;
import javax.servlet.http.*;

import bluec.base.CTreeN.CTreeNode;

public class CTreeView extends CQuery {

	protected boolean innerGetData = false;
	StringBuffer treeBuffer = null;
	
	public CTreeView() {
	}

	/**
	 * 产生一棵数
	 * 
	 * @param rLevel
	 *            初试级别
	 * @return 包含树的字符串
	 * @throws SQLExceptionrLevel
	 */
	public void setInnerGetData(boolean value) {
		innerGetData = value;
	}

	/**
	 * 根据SQL语句构造一棵树
	 * 
	 * @param Sql
	 * @param nLevel
	 * @param Session
	 * @return
	 * @throws SQLException
	 */
	/*
	 * protected String createTreeView(int rLevel, int checkbox, int showcode,
	 * int isexpand, String imgPath) throws SQLException { String origid = "";
	 * String origpid = ""; String oCode, oName, checkValue, bchecked; long
	 * hasChild = 0; boolean hasC = rs.getMetaData().getColumnCount() >= 7;
	 * String nPIDS[] = { "", "", "", "", "", "", "", "", "", "" }; int i =
	 * rLevel, nLevel; boolean readnext = false; boolean notEof = true; String
	 * nextPID = "";
	 * 
	 * int fontSize = checkbox/10; fontSize = (fontSize==0)?12:fontSize;
	 * checkbox = checkbox%10;
	 * 
	 * int ii = 0; // int j = 0; int level = 0; StringBuffer buffer = new
	 * StringBuffer(); try { while (notEof && (readnext || rs.next())) { origid
	 * = rs.getString(1); origpid = rs.getString(2); oCode =
	 * CUtil.NVL(rs.getString(3).trim()); oName =
	 * CUtil.NVL(rs.getString(4).trim()); checkValue =
	 * CUtil.NVL(rs.getString(5)); bchecked = CUtil.NVL(rs.getString(6)); if
	 * (hasC) hasChild = rs.getInt(7); while (i > rLevel &&
	 * !origpid.equals(nPIDS[i])) { buffer.append("</div>"); i--; } level = i;
	 * i++; nLevel = i; nPIDS[i] = origid; // 检测下一个节点 ii = 0; if (rs.next()) {
	 * nextPID = rs.getString(2); readnext = true; if (hasChild == 0 &&
	 * !nextPID.equals(origid)) ii = 1; // 本节点不是下一个节点的父 } else { ii = (hasChild
	 * == 0) ? 1 : 0; notEof = false; // 本节点是最后一个节点 }
	 * 
	 * buffer.append("<span><span>"); for (int x = 0; x <= nLevel; x++)
	 * buffer.append("<img align=\"absmiddle\">"); buffer.append(
	 * "<input type=\"hidden\" value=\"" + hasChild + "\">");
	 * buffer.append("</span>");
	 * 
	 * if (checkbox == 1 || (checkbox > 1 && level > 1)) buffer.append(
	 * "<input type='checkbox' value='" + checkValue +
	 * "' style='height:13px;width:13px;margin-top:3px' " + bchecked + ">");
	 * else buffer.append("<span></span>");
	 * 
	 * String sFontSize = (fontSize==0)?"":"font-size:"+fontSize+"px;";
	 * buffer.append("<span style='padding:1px 1px 1px 1px;cursor:hand;"
	 * +sFontSize+"'>"); if (showcode == 1) buffer.append("(" + oCode + ")" +
	 * oName + "</span>"); else buffer.append(oName + "</span>");
	 * 
	 * buffer.append("<input type='hidden' value='" + origid + "'>");
	 * buffer.append("<input type='hidden' value='" + oCode + "' afxvalue1='" +
	 * checkValue + "' afxvalue2='" + bchecked + "'></span>");
	 * buffer.append("<br>");
	 * 
	 * if (isexpand > 0 && level < isexpand) buffer.append(
	 * "<div style='display:;margin-left:0pt'>"); else buffer.append(
	 * "<div style='display:none;margin-left:0pt'>"); } while (i > rLevel) {
	 * buffer.append("</div>"); i--; } } catch (SQLException ex) {
	 * System.out.println("hear:" + ex.getMessage()); throw new SQLException(
	 * "构造数型结构产生异常.：\n " + ex.getMessage()); } finally { closeConn(); } return
	 * buffer.toString(); }
	 */

	private void createNode(CTreeNode node, int level, int checkbox, int showcode, int isexpand,
			String imgPath, Boolean hasC,int fontSize) {

		String origid = String.valueOf(node.id);
//		String origpid = String.valueOf(node.pid);
		String oCode = node.key;
		String oName = node.values[0];
		String checkValue = node.values[1];
		String bchecked = node.values[2];
		long hasChild = (hasC) ? Integer.parseInt(node.values[3]) : 0;

		treeBuffer.append("<span><span>");
		for (int x = 0; x <= level; x++)
			treeBuffer.append("<img align=\"absmiddle\">");
		treeBuffer.append("<input type=\"hidden\" value=\"" + hasChild + "\">");
		treeBuffer.append("</span>");
		
		if (checkbox == 1 || (checkbox > 1 && level > 1))
			treeBuffer.append("<input type='checkbox' value='" + checkValue
					+ "' style='height:13px;width:13px;margin-top:3px' " + bchecked + ">");
		else
			treeBuffer.append("<span></span>");

		String sFontSize = (fontSize == 0) ? "" : "font-size:" + fontSize + "px;";
		
		treeBuffer.append("<span style='padding:1px 1px 1px 1px;cursor:hand;" + sFontSize + "'>");
		if (showcode == 1)
			treeBuffer.append("(" + oCode + ")" + oName + "</span>");
		else
			treeBuffer.append(oName + "</span>");

		treeBuffer.append("<input type='hidden' value='" + origid + "'>");
		treeBuffer.append("<input type='hidden' value='" + oCode + "' afxvalue1='" + checkValue + "' afxvalue2='"
				+ bchecked + "'></span>");
		treeBuffer.append("<br>");
	
		String display = (isexpand > 0 && level < isexpand) ? "" : "none";
		
		treeBuffer.append("<div style='display:"+display+";margin-left:0pt'>");
		for(int i = 0;i < node.cnodes.size(); i++){
			createNode(node.cnodes.elementAt(i),level+1,checkbox,showcode,isexpand,imgPath,hasC,fontSize);
		}

		treeBuffer.append("</div>");
	}

	protected String createTreeView(int rLevel, int checkbox, int showcode, int isexpand, String imgPath)
			throws Exception {

		boolean hasC = rs.getMetaData().getColumnCount() >= 7;
		int fontSize = checkbox / 10;
		fontSize = (fontSize == 0) ? 12 : fontSize;
		checkbox = checkbox % 10;

		treeBuffer = new StringBuffer();
		CTreeN tree = new CTreeN();

		try {
			while (next()) {
				tree.insertNode(getInt(1), getInt(2), CUtil.NVL(rs.getString(3).trim()), "",
						new String[] { CUtil.NVL(rs.getString(4).trim()), CUtil.NVL(rs.getString(5)),
								CUtil.NVL(rs.getString(6)), (hasC) ? rs.getString(7) : "0" });
			}

			for(int i = 0;i < tree.cnodes.size(); i++){
				createNode(tree.cnodes.elementAt(i),rLevel+1,checkbox,showcode,isexpand,imgPath,hasC,fontSize);
			}
		} catch (Exception ex) {
			throw new Exception("构造数型结构产生异常.：\n " + ex.getMessage());
		} finally {
			closeConn();
		}
		return treeBuffer.toString();
	}

	public String getTreeViewInfo(String Sql, int rLevel, int checkbox, int showcode, int isexpand, String imgPath,
			HttpSession session) throws Exception {
		if (!sessionValidated(session))
			return "";
		executeQuery(Sql);
		return createTreeView(rLevel, checkbox, showcode, isexpand, imgPath);
	}

	public String getTreeViewInfo2(String SqlID, int rLevel, int checkbox, int showcode, int isexpand, String imgPath,
			HttpSession session) throws Exception {
		if (!sessionValidated(session))
			return "";
		queryBySqlID(SqlID, session);
		return createTreeView(rLevel, checkbox, showcode, isexpand, imgPath);
	}

	public String getTreeViewInfoEx(String SqlID, String param, int rLevel, int checkbox, int showcode, int isexpand,
			String imgPath, HttpSession session) throws Exception {
		if (innerGetData) {
			queryBySqlIDWithParamInner(SqlID, param);
		} else {
			if (!sessionValidated(session))
				return "";
			queryBySqlIDWithParam(SqlID, param, session);
		}
		return createTreeView(rLevel, checkbox, showcode, isexpand, imgPath);
	}

	public String getTreeViewInfoEx(String SqlID, String[] params, int rLevel, int checkbox, int showcode, int isexpand,
			String imgPath, HttpSession session) throws Exception {
		if (innerGetData) {
			queryBySqlIDWithParamInner(SqlID, params);
		} else {
			if (!sessionValidated(session))
				return "";
			queryBySqlIDWithParam(SqlID, params, session);
		}
		return createTreeView(rLevel, checkbox, showcode, isexpand, imgPath);
	}
}
