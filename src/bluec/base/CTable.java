package bluec.base;

/**
 * <p>Title: 数据处理包</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.sql.*;

import javax.servlet.http.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.servlet.ServletInputStream;
import java.io.*;

/**
 * CTable 管理一个表
 */
public class CTable extends CDataSet {

	protected String[] allFields; // 字段名列表

	protected int[] allFieldType; // 字段名列表

	/* 数据更新系列SQL语句 */
	protected int tableID = 0; 

	protected String tableName = null; // 关联表的名称

	protected String insertSql = null; // 插入SQL语句

	protected String valuesSql = null; // values语句

	protected String updateSql = null; // 更新SQL语句

	protected String deleteSql = null; // 删除SQL语句

	protected String whereSql = null; // 查找语句

	protected int[] wDataTypes = null; // 数据类型

	protected int[] wIsNull = null; // 是否可空

	/*
	 * 数据更新字段记录
	 */
	protected String[] updateFields = null; // 值字段列表

	protected String[] dataTypes = null; // 数据类型

	protected String[] whereFields = null; // 查找字段列表

	protected String[] safeFields = null; // 安全控制检查字段列表

	protected String[] safeSFields = null; // 安全控制检查字段对应session属性表

	protected int[] setSafes = null; // 安全控制强制字段属性

	protected int[] seachFields = null; // 查找字段属性

	protected int safeCount = 0;

	/* 事务控制标记 */
	protected int useTransaction = 0;

	/* 相关SQL语句变量 */
	/* 每条记录插入之前要执行的sql语句 */
	protected boolean hasbInsert = false; // 表示是否有sql语句要执行

	protected String bInsertSql = ""; // 要执行的sql语句

	protected int bInsertTag = 0;

	protected int bInsertUAtt = 0;

	protected String[] bInsertField = null; // 字段参数

	protected int[] bInsertFType = null; // 字段类型

	protected int[] bInsertFFrom = null; // 字段来源

	/* 每条记录插入之后要执行的sql语句* */
	protected boolean hasaInsert = false;

	protected String aInsertSql = "";

	protected int aInsertTag = 0;

	protected int aInsertUAtt = 0;

	protected String[] aInsertField = null;

	protected int[] aInsertFType = null; // 字段类型

	protected int[] aInsertFFrom = null; // 字段来源

	/* 每条记录删除之前要执行的sql语句* */
	protected boolean hasbDelete = false;

	protected String bDeleteSql = "";

	protected int bDeleteTag = 0;

	protected int bDeleteUAtt = 0;

	protected String[] bDeleteField = null;

	protected int[] bDeleteFType = null; // 字段类型

	protected int[] bDeleteFFrom = null; // 字段来源

	/* 每条记录删除之后要执行的sql语句* */
	protected boolean hasaDelete = false;

	protected String aDeleteSql = "";

	protected int aDeleteTag = 0;

	protected int aDeleteUAtt = 0;

	protected String[] aDeleteField = null;

	protected int[] aDeleteFType = null; // 字段类型

	protected int[] aDeleteFFrom = null; // 字段来源

	/* 每条记录更新之前要执行的sql语句* */
	protected boolean hasbUpdate = false;

	protected String bUpdateSql = "";

	protected int bUpdateTag = 0;

	protected int bUpdateUAtt = 0;

	protected String[] bUpdateField = null;

	protected int[] bUpdateFType = null; // 字段类型

	protected int[] bUpdateFFrom = null; // 字段来源

	/* 每条记录更新之后要执行的sql语句* */
	protected boolean hasaUpdate = false;

	protected String aUpdateSql = "";

	protected int aUpdateTag = 0;

	protected int aUpdateUAtt = 0;

	protected String[] aUpdateField = null;

	protected int[] aUpdateFType = null; // 字段类型

	protected int[] aUpdateFFrom = null; // 字段来源

	/* 数据集执行数据库操作之前要执行的sql语句* */
	protected boolean hallbUpdate = false;

	protected String allbUpdateSql = "";

	protected int allbUpdateTag = 0;

	protected int allbUpdateUAtt = 0;

	protected String[] allbUpdateField = null;

	protected int[] allbUpdateFType = null; // 字段类型

	protected int[] allbUpdateFFrom = null; // 字段来源

	protected boolean[] bUpdateOldValue = null;

	/* 数据集执行数据库操作之前要执行的sql语句* */
	protected boolean hallaUpdate = false;

	protected String allaUpdateSql = "";

	protected int allaUpdateTag = 0;

	protected int allaUpdateUAtt = 0;

	protected String[] allaUpdateField = null;

	protected int[] allaUpdateFType = null; // 字段类型

	protected int[] allaUpdateFFrom = null; // 字段来源

	protected boolean[] aUpdateOldValue = null;

	// 以下是需要执行的语句
	protected boolean hallwaysUpdate = false;

	protected String allwaysUpdateSql = "";

	protected int allwaysUpdateTag = 0;

	protected int allwaysUpdateUAtt = 0;

	protected String[] allwaysUpdateField = null;

	protected int[] allwaysUpdateFType = null; // 字段类型

	protected int[] allwaysUpdateFFrom = null; // 字段来源

	private Vector<TSqlObject> allSql;

	private String savePageId = "0";

	public String BeforeInsertSql = "";

	public String AfterInsertSql = "";

	public String BeforeDeleteSql = "";

	public String AfterDeleteSql = "";

	public String BeforeUpdateSql = "";

	public String AfterUpdateSql = "";

	public String aBeforeUpdateSql = "";

	public String aAfterUpdateSql = "";

	public String allLastUpdateSql = "";

	private boolean _innerCall = false;
	/**
	 * SQL语句对象,用来存储一个将要执行的SQL语句
	 * 
	 * @author Administrator
	 * 
	 */
	private class TSqlObject {
		public int tableID;   		// 表id,用来区分该sql属于的表

		public int recNo;     		// 记录号

		public String sql;    		// 要执行的sql语句

		public int sqlType;   		// sql类型,1-insert,2-update,3-delete,4-procedure,5-sql

		public int updateAtt;   	// 更新属性,0-不运行空更新，1-运行空更新

		public String[] params;   // 参数值

		public int[] paramtype; 	// 参数数据类型
	}

	public CTable() {
	}

	public void destroy() {
	}

	protected Node findNode(Node pnode,String nodeName){
		Node node = pnode;
		int i=0;
		if(node.hasChildNodes())
			for(i=0;i<node.getChildNodes().getLength();i++)
				if(node.getChildNodes().item(i).getNodeName().equals(nodeName))
					return node.getChildNodes().item(i);
		return node;
	}
	
	protected Node findNodeEx(Node pnode,String nodeName){
		Node node = pnode;
		int i=0;
		if(node.hasChildNodes()){
			for(i=0;i<node.getChildNodes().getLength();i++)
				if(node.getChildNodes().item(i).getNodeName().equals(nodeName))
					return node.getChildNodes().item(i);
				else
					node = findNodeEx(node.getChildNodes().item(i),nodeName);
		}
		return node;
	}
	/**
	 * 保存数据到数据库,xml数据格式如下
	 * 
	 * @param 要保存的数据
	 * @return =0 成功 非0 - 错误代码 数据格式 <datasets> <dataset> <table tablename="表名称"/>
	 *         <fields> <field attribname="字段名称" ... /> ... </fields> <rowdata>
	 *         <row field1="数据" ... /> ... </rowdata> </dataset> <dataset> ...
	 *         </dataset> </datasets>
	 */
	/**
	 * 获取表名称
	 * 
	 * @param 表名称节点
	 * @return =0 成功 非0 - 错误代码
	 */
	protected int getTableName(Node node) {
		// 获取属性节点表
		/* 跟踪语句 */
		// System.out.print("NodeName:"+node.getNodeName()+"
		// NodeType:"+node.getNodeType()+"\n");
		// 获取表的名称
		tableName = node.getAttributes().getNamedItem("tn").getNodeValue();

		return 0;
	}

	/**
	 * 根据xml中包含的字段描述信息，构造sql语句
	 * 
	 * @param node
	 *          数据元的根节点<medata>
	 * @return >0成功,0-无字段信息,<0在查找语句中缺少安全控制字段
	 */
	protected int createSqls(Node node) throws SQLException {
		NodeList nList; // 子节点列表
		NamedNodeMap map; // 属性列表
		/* SQL缓冲 */
		StringBuffer insertBuf = new StringBuffer();
		StringBuffer valueBuf = new StringBuffer();
		StringBuffer updateBuf = new StringBuffer();
		StringBuffer whereBuf = new StringBuffer();

		/* 字段记录缓冲 */
		Vector<String> vFields = new Vector<String>(); // 可更新字段
		Vector<String> vDataType = new Vector<String>(); // 可更新字段类型
		Vector<String> vWhere = new Vector<String>(); // 查找字段

		int uIndex = 0; // 更新字段索引,当找到一个允许更新的字段时加1
		int wIndex = 0; // 查找字段索引，当找到一个可以作为查找字段时加1

		/* SQL语句开始部分 */
		insertBuf.append("INSERT INTO " + tableName + "(");
		valueBuf.append("VALUES(");

		updateBuf.append("UPDATE " + tableName);
		whereBuf.append("WHERE ");

		// 获取字段描述节点
		nList = node.getChildNodes();

		allFields = new String[nList.getLength()];
		allFieldType = new int[nList.getLength()];

		wDataTypes = new int[nList.getLength()];
		wIsNull = new int[nList.getLength()];

		// 建立字段数组
		for (int i = 0; i < nList.getLength(); i++)
			if (nList.item(i).getNodeType() != 3) { // nList.item(i)表示每一个<FIELD>描述
				map = nList.item(i).getAttributes(); // 获取属性列表
				// 字段名称
				String fieldName = map.getNamedItem("A").getNodeValue();
				String tempStr = null; // 临时串
				// 数据类型
				int dataType = Integer.parseInt(map.getNamedItem("B").getNodeValue());
				// 是否可更新
				int fieldUpdate = Integer.parseInt(map.getNamedItem("U").getNodeValue());
				// 是否可空
				int isNull = Integer.parseInt(map.getNamedItem("D").getNodeValue());

				allFields[i] = fieldName;
				allFieldType[i] = dataType;

				// 根据不同的数据类型产生响应的格式
				switch (dataType) {
				case -1:// SqlServer text类型
				case 1:// 1-CHAR类型,12-VARCHAR2类型
				case 12:
					tempStr = "'%s'";
					break;
				case 91:
				case 93: // DATE类型
					tempStr = __upFormat[_databaseType];
					break;
				default: // 其他类型
					tempStr = "%s";
				}

				if (fieldUpdate == 1) { // 1表示该字段允许更新
					uIndex++;
					// 如果前面已经存在一个字段，先加一个逗号分割
					if (uIndex > 1) {
						insertBuf.append(",");
						valueBuf.append(",");
					}

					vFields.add(fieldName);
					vDataType.add(Integer.toString(dataType));
					// 构造Insert语句的格式
					insertBuf.append(fieldName);
					valueBuf.append(tempStr);
				}
				// 处理查找字段
				if (dataType > 0 && map.getNamedItem("S").getNodeValue().equals("1")) { // 允许作为查找字段
					wDataTypes[wIndex] = dataType;
					wIsNull[wIndex] = isNull;
					wIndex++;
					if (wIndex > 1)
						whereBuf.append(" AND ");
					if (isNull == 1) {
						switch (dataType) {
						case 1:
						case 12:
							whereBuf.append(CUtil.formatStr(__upNvl[_databaseType], fieldName, "%s") + "="
									+ tempStr);
							break;
						case -7:
						case -6:
						case -5:
						case 2:
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
							whereBuf.append(fieldName + tempStr);
							break;
						case 91:
						case 93:
							whereBuf.append(fieldName + "=" + tempStr);
				
							break;
						default:
							whereBuf.append(fieldName + tempStr);
							break;
						}
					} else
						whereBuf.append(fieldName + "="+ tempStr);
					vWhere.add(fieldName);
				}
			}
		insertBuf.append(")");
		valueBuf.append(")");

		// 安全更新检查
		safeCount = 0;

		if (uIndex > 0) {
			insertSql = insertBuf.toString();
			valuesSql = valueBuf.toString();
			updateSql = updateBuf.toString();
			whereSql = whereBuf.toString();

			updateFields = new String[vFields.size()];
			vFields.copyInto(updateFields);

			dataTypes = new String[vDataType.size()];
			vDataType.copyInto(dataTypes);

			whereFields = new String[vWhere.size()];
			vWhere.copyInto(whereFields);
		} else {
			insertSql = null;
			valuesSql = null;
			updateSql = null;
			whereSql = null;
			updateFields = null;
			whereFields = null;
			dataTypes = null;
		}
		return uIndex;
	}

	private String __bufSql;

	private String __bufField;

	private String __bufDType;

	private String __bufFroms;

	private int __gtag;

	private int __uAtt;

	private boolean getProcSql(String sqlID) {
		boolean Result = false;
		try {
			if (_databaseType == 0) // oracle
				executeQuery2(
						"select sql_server,nvl(sql_session,' ') sql_session,"
								+ "       nvl(sql_pdtype,' ') sql_pdtype,nvl(sql_pfrom,' ') sql_pfrom,gtag,nvl(aexec,0)*10+att att "
								+ "from sys_procs where sql_id=?", sqlID);
			else
				executeQuery2(
						"select sql_server,sql_session=isnull(sql_session,' '),"
								+ "       sql_pdtype=isnull(sql_pdtype,' '),sql_pfrom=isnull(sql_pfrom,' '),gtag,att=isnull(aexec,0)*10+sqlatt  "
								+ "from sys_procs where sql_id=?", sqlID);

			if (rs.next()) {
				Result = true;
				__bufSql = rs.getString(1);
				__bufField = rs.getString(2);
				__bufDType = rs.getString(3);
				__bufFroms = rs.getString(4);
				__gtag = rs.getInt(5);
				__uAtt = rs.getInt(6);
			}
		} catch (SQLException ex) {
		}
		return Result;
	}

	private boolean getProcSqlWithSql(String sql) {
		boolean Result = false;
		String afx = sql.substring(0,5);
		String fgf = "";
		StringBuffer buffer = new StringBuffer();
		__bufField = "";
		__bufDType = "";
		__bufFroms = "";
		
		__gtag = 5;
		__uAtt = 11;
		try {
			if(afx.equals("proc.")){  //是存储过程
			 __gtag = 4;	
			 Result = false;
			}else{
			 int si=sql.indexOf("{");
			 int ei=0;
			 while(si>0){
				 ei = sql.indexOf("}");
				 if(ei<si)break;
				 String fName = sql.substring(si+1,ei);
				 if(fName.indexOf("old.")==0){
					 __bufField = __bufField+fgf+fName.substring(4);
				   __bufFroms = __bufFroms+fgf+"2";		//使用原字段值		 
			   }else{
					 __bufField = __bufField+fgf+fName;
					 __bufFroms = __bufFroms+fgf+"1"; //使用新字段值
				 }	 
				 __bufDType = __bufDType+fgf+"1";   //全部做字符型处理
				 
				 buffer.append(sql.substring(0,si));
				 buffer.append("%s");
				 sql = sql.substring(ei+1);
				 si = sql.indexOf("{"); 
			 }
			 buffer.append(sql);
			}
			__bufSql = buffer.toString();
			Result = true;
		} finally {
		}
		return Result;
	}
	
	private void copyArray(String[] s, int[] d) {
		for (int i = 0; i < s.length; i++)
			if (!s[i].trim().equals(""))
				d[i] = Integer.parseInt(s[i]);
			else
				d[i] = 0;
	}

	/**
	 * 分析参数
	 * 
	 * @param node
	 *          Node 此时表示<usetran>
	 * @return int
	 */
	private int AnalyseParam(Node dataset) {
		int i = 0, j;
		NamedNodeMap map;
		String[] ta = null;
		P("tableName="+tableName);
		useTransaction = Integer.parseInt(findNode(dataset,"ut").getFirstChild().getNodeValue());
		
		Node node = findNode(dataset,"bi");
		hasbInsert = node.hasChildNodes();
		if (hasbInsert) {
			bInsertSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();

			hasbInsert = (_innerCall)? getProcSqlWithSql(bInsertSql):getProcSql(bInsertSql);

			if (hasbInsert) {
				bInsertSql = __bufSql;
				bInsertField = __bufField.split("/");

				ta = __bufDType.split("/");
				bInsertFType = new int[ta.length];
				copyArray(ta, bInsertFType);

				ta = __bufFroms.split("/");
				bInsertFFrom = new int[ta.length];
				copyArray(ta, bInsertFFrom);

				bInsertTag = __gtag;
				bInsertUAtt = __uAtt;
				// 获取参数
				if(!_innerCall){
				  map = node.getAttributes();
				  for (i = 0, j = 0; i < bInsertFFrom.length; i++)
					  if (bInsertFFrom[i] == 5) {
					  	bInsertField[i] = map.item(j).getNodeValue();
						  j++;
				  	}
				}
			}
		}

		node = findNode(dataset,"ai");
		hasaInsert = node.hasChildNodes();
		if (hasaInsert) {
			aInsertSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();
			hasaInsert = (_innerCall)? getProcSqlWithSql(aInsertSql):getProcSql(aInsertSql);

			if (hasaInsert) {
				aInsertSql = __bufSql;
				aInsertField = __bufField.split("/");

				ta = __bufDType.split("/");
				aInsertFType = new int[ta.length];
				copyArray(ta, aInsertFType);

				ta = __bufFroms.split("/");
				aInsertFFrom = new int[ta.length];
				copyArray(ta, aInsertFFrom);

				aInsertTag = __gtag;
				aInsertUAtt = __uAtt;
				// 获取参数
				if(!_innerCall){
				  map = node.getAttributes();
			  	for (i = 0, j = 0; i < aInsertFFrom.length; i++)
				  	if (aInsertFFrom[i] == 5) {
					  	aInsertField[i] = map.item(j).getNodeValue();
						  j++;
				  	}
				}
			}
		}

		node = findNode(dataset,"bd");
		// bDeleteSql
		hasbDelete = node.hasChildNodes();
		if (hasbDelete) {
			bDeleteSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();
			hasbDelete = (_innerCall)? getProcSqlWithSql(bDeleteSql):getProcSql(bDeleteSql);

			if (hasbDelete) {
				bDeleteSql = __bufSql;
				bDeleteField = __bufField.split("/");

				ta = __bufDType.split("/");
				bDeleteFType = new int[ta.length];
				copyArray(ta, bDeleteFType);

				ta = __bufFroms.split("/");
				bDeleteFFrom = new int[ta.length];
				copyArray(ta, bDeleteFFrom);
				bDeleteTag = __gtag;
				bDeleteUAtt = __uAtt;
				if(!_innerCall){
				  map = node.getAttributes();
				  for (i = 0, j = 0; i < bDeleteFFrom.length; i++)
				  	if (bDeleteFFrom[i] == 5) {
					  	bDeleteField[i] = map.item(j).getNodeValue();
					  	j++;
				  	}
				}
			}
		}

		node = findNode(dataset,"ad");
		// aDeleteSql
		hasaDelete = node.hasChildNodes();
		if (hasaDelete) {
			aDeleteSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();
			hasaDelete = (_innerCall)? getProcSqlWithSql(aDeleteSql):getProcSql(aDeleteSql);

			if (hasaDelete) {
				aDeleteSql = __bufSql;
				aDeleteField = __bufField.split("/");

				ta = __bufDType.split("/");
				aDeleteFType = new int[ta.length];
				copyArray(ta, aDeleteFType);

				ta = __bufFroms.split("/");
				aDeleteFFrom = new int[ta.length];
				copyArray(ta, aDeleteFFrom);

				aDeleteTag = __gtag;
				aDeleteUAtt = __uAtt;
				
				if(!_innerCall){
				  map = node.getAttributes();
				  for (i = 0, j = 0; i < aDeleteFFrom.length; i++)
				  	if (aDeleteFFrom[i] == 5) {
					  	aDeleteField[i] = map.item(j).getNodeValue();
					  	j++;
				  	}
				}
			}
		}

		node = findNode(dataset,"bu");
		hasbUpdate = node.hasChildNodes();
		if (hasbUpdate) {

			bUpdateSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();

			hasbUpdate = (_innerCall)? getProcSqlWithSql(bUpdateSql):getProcSql(bUpdateSql);

			if (hasbUpdate) {
				bUpdateSql = __bufSql;
				bUpdateField = __bufField.split("/");

				ta = __bufDType.split("/");
				bUpdateFType = new int[ta.length];
				copyArray(ta, bUpdateFType);

				ta = __bufFroms.split("/");
				bUpdateFFrom = new int[ta.length];
				copyArray(ta, bUpdateFFrom);
				bUpdateTag = __gtag;
				bUpdateUAtt = __uAtt;
				
				if(!_innerCall){
				  map = node.getAttributes();
				  for (i = 0, j = 0; i < bUpdateFFrom.length; i++)
				  	if (bUpdateFFrom[i] == 5) {
					  	bUpdateField[i] = map.item(j).getNodeValue();
					  	j++;
					  }
				}
			}
		}

		node = findNode(dataset,"au");
		hasaUpdate = node.hasChildNodes();
		if (hasaUpdate) {
			aUpdateSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();
			hasaUpdate = (_innerCall)? getProcSqlWithSql(aUpdateSql):getProcSql(aUpdateSql);

			if (hasaUpdate) {
				aUpdateSql = __bufSql;
				aUpdateField = __bufField.split("/");

				ta = __bufDType.split("/");
				aUpdateFType = new int[ta.length];
				copyArray(ta, aUpdateFType);

				ta = __bufFroms.split("/");
				aUpdateFFrom = new int[ta.length];
				copyArray(ta, aUpdateFFrom);
				aUpdateTag = __gtag;
				aUpdateUAtt = __uAtt;
				
				if(!_innerCall){
				  map = node.getAttributes();
				  for (i = 0, j = 0; i < aUpdateFFrom.length; i++)
				  	if (aUpdateFFrom[i] == 5) {
					  	aUpdateField[i] = map.item(j).getNodeValue();
					  	j++;
					  }
				}
			}
		}

		node = findNode(dataset,"albu");
		hallbUpdate = node.hasChildNodes();
		if (hallbUpdate) {
			allbUpdateSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();
			hallbUpdate = (_innerCall)? getProcSqlWithSql(allbUpdateSql):getProcSql(allbUpdateSql);
			if (hallbUpdate) {
				allbUpdateSql = __bufSql;
				allbUpdateTag = __gtag;
				allbUpdateUAtt = __uAtt;

				allbUpdateField = __bufField.split("/");

				ta = __bufDType.split("/");
				allbUpdateFType = new int[ta.length];
				copyArray(ta, allbUpdateFType);

				ta = __bufFroms.split("/");
				allbUpdateFFrom = new int[ta.length];
				copyArray(ta, allbUpdateFFrom);
				
				if(!_innerCall){
				  map = node.getAttributes();
				  for (i = 0, j = 0; i < allbUpdateFFrom.length; i++)
					  if (allbUpdateFFrom[i] == 5) {
						  allbUpdateField[i] = map.item(j).getNodeValue();
						  j++;
				  	}
				}
			}
		}

		node = findNode(dataset,"alau");
		hallaUpdate = node.hasChildNodes();
		if (hallaUpdate) {

			allaUpdateSql = CUtil.replaceXmlXA(node.getFirstChild().getNodeValue()).trim();
			P("allaUpdateSql="+allaUpdateSql);
			hallaUpdate = (_innerCall)? getProcSqlWithSql(allaUpdateSql):getProcSql(allaUpdateSql);

			if (hallaUpdate) {

				allaUpdateSql = __bufSql;
				allaUpdateTag = __gtag;
				allaUpdateUAtt = __uAtt;
				allaUpdateField = __bufField.split("/");

				ta = __bufDType.split("/");
				allaUpdateFType = new int[ta.length];
				copyArray(ta, allaUpdateFType);

				ta = __bufFroms.split("/");
				allaUpdateFFrom = new int[ta.length];
				copyArray(ta, allaUpdateFFrom);
				
				if(!_innerCall){
				  map = node.getAttributes();

				  for (i = 0, j = 0; i < allaUpdateFFrom.length; i++)
					  if (allaUpdateFFrom[i] == 5) {
						  allaUpdateField[i] = map.item(j).getNodeValue();
						  j++;
				  	}
				}
			}
		}

		node = findNode(dataset,"allways");
		hallwaysUpdate = node.hasChildNodes();
		if (hallwaysUpdate) {
			allwaysUpdateSql = CUtil.NVL(node.getFirstChild().getNodeValue()).trim();
			hallwaysUpdate = (_innerCall)? getProcSqlWithSql(allwaysUpdateSql):getProcSql(allwaysUpdateSql);

			if (hallwaysUpdate) {
				
				//P("hallwaysUpdate:"+__bufSql);
				allwaysUpdateSql = __bufSql;
				allwaysUpdateTag = __gtag;
				allwaysUpdateUAtt = __uAtt;
				allwaysUpdateField = __bufField.split("/");

				ta = __bufDType.split("/");
				allwaysUpdateFType = new int[ta.length];
				copyArray(ta, allwaysUpdateFType);

				ta = __bufFroms.split("/");
				allwaysUpdateFFrom = new int[ta.length];
				copyArray(ta, allwaysUpdateFFrom);
				
				if(!_innerCall){
				  map = node.getAttributes();

				  for (i = 0, j = 0; i < allwaysUpdateFFrom.length; i++)
					  if (allwaysUpdateFFrom[i] == 5) {
						  allwaysUpdateField[i] = map.item(j).getNodeValue();
					  	j++;
					  }
				}
			}
		}

		return 0;
	}

	/**
	 * 增加记录到数据库中去
	 * 
	 * @param map
	 *          代表一行的各字段数组
	 * @return
	 */
	protected int applyInsert(NamedNodeMap map) throws SQLException {
		String values[] = new String[updateFields.length]; // 分配值存储数组
		for (int i = 0; i < updateFields.length; i++) {
			String tStr = map.getNamedItem(updateFields[i]).getNodeValue();
			if (tStr.equals(" "))
				tStr = "";
			boolean isnull = (tStr == null || tStr.equals(""));
			switch (Integer.parseInt(dataTypes[i])) {
			case -7:
			case -6:
			case -5:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				if (isnull)
					values[i] = "null";
				else
					values[i] = tStr.trim();
				break;
			case 91:
			case 93:
				if (isnull) // 日期类型
					values[i] = "null";
				else{
					if(tStr.compareToIgnoreCase("sysdate")==0){
						if(_databaseType==0){
							values[i] = "sysdate";
						}else{
							values[i] = "getdate()";
						}
					}else{
						values[i] = "'" + tStr.trim() + "'";						
					}
				}
				break;
			default:
				if (isnull)
					values[i] = "";
				else
					values[i] = CUtil.replaceSpChar(tStr);
				if (updateFields[i].equals("PASSWORD"))
					values[i] = CUtil.SetPassWord(values[i]);

			}
			// System.out.println(updateFields[i]+","+dataTypes[i]+","+values[i]);
		}

		String pSql = "";

		TSqlObject sqlObject = null;

		if (hasbInsert) {
			sqlObject = new TSqlObject();
			sqlObject.tableID = tableID;
			sqlObject.sql = bInsertSql;
			sqlObject.paramtype = bInsertFType;
			sqlObject.sqlType = bInsertTag;
			sqlObject.updateAtt = bInsertUAtt;

			if (bInsertField != null) {
				sqlObject.params = new String[bInsertField.length];
				for (int i = 0; i < bInsertField.length; i++)
					switch (bInsertFFrom[i]) {  //数据来源
					case 1:                     //字段新值
						sqlObject.params[i] = map.getNamedItem(bInsertField[i]).getNodeValue();
						if ((bInsertFType[i] == 2 || bInsertFType[i] == 3)
								&& (sqlObject.params[i] == null || sqlObject.params[i].trim().equals("")))
							sqlObject.params[i] = "0";
						break;
					case 2:                     //字段原值
						if (bInsertFType[i] == 2 || bInsertFType[i] == 3) // 字段原值
							sqlObject.params[i] = "0";
						else
							sqlObject.params[i] = "";
						break;
					case 0:                     //无
					case 5:                     //常数
						sqlObject.params[i] = bInsertField[i];
						break;
					}
			}
			allSql.add(sqlObject);
		}

		sqlObject = new TSqlObject();
		pSql = CUtil.formatStr(valuesSql, values, "%s");

		if(_databaseType==0){
			pSql = CUtil.formatStr(pSql, "sysdate", "TO_DATE(sysdate,'yyyy.mm.dd hh24:mi:ss')");
		}
		
		sqlObject = new TSqlObject();
		sqlObject.tableID = tableID;
		sqlObject.sql = insertSql + "\n" + pSql;
		sqlObject.sqlType = 1;
		sqlObject.updateAtt = 0;
		allSql.add(sqlObject);

		if (hasaInsert) {
			sqlObject = new TSqlObject();
			sqlObject.tableID = tableID;
			sqlObject.sql = aInsertSql;
			sqlObject.paramtype = aInsertFType;
			sqlObject.sqlType = aInsertTag;
			sqlObject.updateAtt = aInsertUAtt;

			if (aInsertField != null) {
				sqlObject.params = new String[aInsertField.length];
				for (int i = 0; i < aInsertField.length; i++)
					switch (aInsertFFrom[i]) {
					case 1:
						sqlObject.params[i] = map.getNamedItem(aInsertField[i]).getNodeValue(); // 字段值
						if ((aInsertFType[i] == 2 || aInsertFType[i] == 3)
								&& (sqlObject.params[i] == null || sqlObject.params[i].trim().equals("")))
							sqlObject.params[i] = "0";

						break;
					case 2:
						if (aInsertFType[i] == 2 || aInsertFType[i] == 3) // 字段原值
							sqlObject.params[i] = "0";
						else
							sqlObject.params[i] = "";
						break;
					case 0:
					case 5:
						sqlObject.params[i] = aInsertField[i]; // 常数
						break;
					}
			}
			allSql.add(sqlObject);
		}

		return 0;
	}

	/**
	 * 执行数据修改操作
	 * 
	 * @param map
	 * @param node
	 * @return
	 */
	protected int applyUpdate(NamedNodeMap map, Node node) throws SQLException {
		String wheres[] = new String[whereFields.length]; // 分配查找语句的数组
		for (int i = 0; i < whereFields.length; i++) {
			String v = CUtil.replaceSpChar(map.getNamedItem(whereFields[i]).getNodeValue());
			if (wIsNull[i] == 1 || wDataTypes[i] == 91 || wDataTypes[i] == 93) {
				switch (wDataTypes[i]) {
				case -7:
				case -6:
				case -5:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					if (v.trim().equals(""))
						v = " is null";
					else
						v = " =" + v;
					break;
				case 91:
				case 93:  
					if (v.equals(""))
						v = "null";
					else {
						if(v.compareToIgnoreCase("sysdate")==0){
							if(_databaseType==0){
								v = "sysdate";
							}else{
								v = "getdate()";
							}
						}else{
							v = "'"+v+"'";					
						}
					}
					break;
				default:
					if (v.equals(""))
						v = " ";
				}
			}

			wheres[i] = v;
		}

		String pWhere = CUtil.formatStr(whereSql, wheres, "%s"); // 本次where语句

		StringBuffer setFields = new StringBuffer();
		int ncount = 0;
		setFields.append("SET ");
		// map为旧值,newmap为新值
		NamedNodeMap newmap = node.getNextSibling().getAttributes();

		for (int i = 0; i < updateFields.length; i++) {
			String oValue = map.getNamedItem(updateFields[i]).getNodeValue(); // 旧值
			String nValue = newmap.getNamedItem(updateFields[i]).getNodeValue(); // 新值

			if (!oValue.equals(nValue)) { // 只有新旧值不等时才更新
				if (nValue.equals(" "))
					nValue = "";
				boolean isnull = (nValue == null) || (nValue.equals(""));
				ncount++;
				if (ncount > 1)
					setFields.append(",");
				switch (Integer.parseInt(dataTypes[i])) {
				case -7:
				case -6:
				case -5:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					if (isnull)
						nValue = "null";
					else
						nValue = nValue.trim();
					setFields.append(updateFields[i] + "=" + nValue);
					break;
				case 91:
				case 93:
					if (isnull)
						nValue = "null"; // 日期
					else{
						nValue = nValue.trim();
						if(nValue.compareToIgnoreCase("sysdate")==0){
							if(_databaseType==0){
								nValue = "sysdate";
							}else{
								nValue = "getdate()";
							}
						}else{
							nValue = "'" + nValue + "'";
							nValue = CUtil.formatStr(__upFormat[_databaseType], nValue, "%s");
						}
					}
						
					setFields.append(updateFields[i] + "=" +nValue);
					break;
				default:
					if (isnull)
						nValue = "";
					else
						nValue = CUtil.replaceSpChar(nValue);
					if (updateFields[i].equals("PASSWORD"))
						nValue = CUtil.SetPassWord(nValue);
					setFields.append(updateFields[i] + "='" + nValue + "'");
				}
			}
		}

		if (ncount > 0) {
			TSqlObject sqlObject = null;

			int nnum = 1; // 记录新旧值不同的字段数
			if (hasbUpdate) {

				sqlObject = new TSqlObject();
				sqlObject.sql = bUpdateSql;
				sqlObject.paramtype = bUpdateFType;
				sqlObject.tableID = tableID;
				sqlObject.sqlType = bUpdateTag;
				sqlObject.updateAtt = bUpdateUAtt;

				if (bUpdateField != null) {
					nnum = 0;
					sqlObject.params = new String[bUpdateField.length];
					for (int i = 0; i < bUpdateField.length; i++)
						if (bUpdateFFrom[i] == 1 || bUpdateFFrom[i] == 2) {
							String oValue = map.getNamedItem(bUpdateField[i]).getNodeValue(); // 旧值
							String nValue = newmap.getNamedItem(bUpdateField[i]).getNodeValue(); // 新值
							if (!oValue.equals(nValue) || (bUpdateUAtt >= 10))
								nnum++;
							if (bUpdateFFrom[i] == 2) // 使用旧值
								sqlObject.params[i] = oValue;
							else
								sqlObject.params[i] = nValue;
						} else if (aUpdateFFrom[i] == 0 || bUpdateFFrom[i] == 5) {
							sqlObject.params[i] = bUpdateField[i];
							nnum++;
						}
				}
				if (nnum > 0) {
					allSql.add(sqlObject);
				}
			}

			sqlObject = new TSqlObject();
			sqlObject.tableID = tableID;
			sqlObject.sql = updateSql + "\n" + setFields.toString() + "\n" + pWhere;
			sqlObject.sqlType = 2;
			sqlObject.updateAtt = 0;

			allSql.add(sqlObject);

			if (hasaUpdate) {
				sqlObject = new TSqlObject();
				sqlObject.sql = aUpdateSql;
				sqlObject.paramtype = aUpdateFType;
				sqlObject.tableID = tableID;
				sqlObject.sqlType = aUpdateTag;
				sqlObject.updateAtt = aUpdateUAtt;
				nnum = 1;
				if (aUpdateField != null) {
					nnum = 0;
					sqlObject.params = new String[aUpdateField.length];
					for (int i = 0; i < aUpdateField.length; i++)
						if (aUpdateFFrom[i] == 1 || aUpdateFFrom[i] == 2) {
							String oValue = map.getNamedItem(aUpdateField[i]).getNodeValue(); // 旧值
							String nValue = newmap.getNamedItem(aUpdateField[i]).getNodeValue(); // 新值
							if (!oValue.equals(nValue) || (aUpdateUAtt >= 10))
								nnum++;
							if (aUpdateFFrom[i] == 2) // 使用旧值
								sqlObject.params[i] = oValue;
							else
								sqlObject.params[i] = nValue;
						} else if (aUpdateFFrom[i] == 0 || aUpdateFFrom[i] == 5) {
							sqlObject.params[i] = aUpdateField[i];
							nnum++;
						}
				}
				if (nnum > 0)
					allSql.add(sqlObject);
			}
		}
		return 0;
	}

	/**
	 * 执行删除操作
	 * 
	 * @param map
	 * @return
	 */
	protected int applyDelete(NamedNodeMap map) throws SQLException {
		String wheres[] = new String[whereFields.length]; // 分配查找语句的数组
		for (int i = 0; i < whereFields.length; i++) {
			String v = CUtil.replaceSpChar(map.getNamedItem(whereFields[i]).getNodeValue());
			if (wIsNull[i] == 1 || wDataTypes[i] == 91 || wDataTypes[i] == 93)
				switch (wDataTypes[i]) {
				case -7:
				case -6:
				case -5:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					if (v.trim().equals(""))
						v = " is null";
					else
						v = " =" + v;
					break;
				case 91:  
				case 93:
					if (v.equals(""))
						v = " is null";
					else
						v = "=" + CUtil.formatStr(__upFormat[_databaseType], "'" + v + "'", "%s");
					break;
				default:
					if (v.equals(""))
						v = " ";
				}
			wheres[i] = v;
		}
		String pWhere = CUtil.formatStr(whereSql, wheres, "%s"); // 本次where语句
		deleteSql = "DELETE FROM " + tableName;

		TSqlObject sqlObject = null;

		if (hasbDelete) {
			sqlObject = new TSqlObject();
			sqlObject.sql = bDeleteSql;
			sqlObject.paramtype = bDeleteFType;
			sqlObject.tableID = tableID;
			sqlObject.sqlType = bDeleteTag;
			sqlObject.updateAtt = bDeleteUAtt;

			if (bDeleteField != null) {
				sqlObject.params = new String[bDeleteField.length];
				for (int i = 0; i < bDeleteField.length; i++)
					switch (bDeleteFFrom[i]) {
					case 1:
					case 2:
						sqlObject.params[i] = map.getNamedItem(bDeleteField[i]).getNodeValue();
						break;
					case 0:
					case 5:
						sqlObject.params[i] = bDeleteField[i];
						break;
					}
			}
			allSql.add(sqlObject);
		}

		sqlObject = new TSqlObject();
		sqlObject.sql = deleteSql + "\n" + pWhere;
		sqlObject.tableID = tableID;
		sqlObject.sqlType = 3;
		sqlObject.updateAtt = 0;
		allSql.add(sqlObject);

		if (hasaDelete) {
			sqlObject = new TSqlObject();
			sqlObject.sql = aDeleteSql;
			sqlObject.paramtype = aDeleteFType;
			sqlObject.tableID = tableID;
			sqlObject.sqlType = aDeleteTag;
			sqlObject.updateAtt = aDeleteUAtt;

			if (aDeleteField != null) {
				sqlObject.params = new String[aDeleteField.length];
				for (int i = 0; i < aDeleteField.length; i++)
					switch (aDeleteFFrom[i]) {
					case 1:
					case 2:
						sqlObject.params[i] = map.getNamedItem(aDeleteField[i]).getNodeValue();
						break;
					case 0:
					case 5:
						sqlObject.params[i] = aDeleteField[i];
						break;
					}
			}
			allSql.add(sqlObject);
		}
		return 0;
	}

	/**
	 * 数据更新
	 * 
	 * @param dataset
	 *          数据集节点
	 * @return
	 */
	protected int applyUpdates(Node dataset) throws SQLException {
		int result = 0;
		tableID++;
		// dataset.getFirstChild()为text节点
		// 表名称子接点,跳过text节点,tnNode表示<tableset>
		Node tnNode = findNode(dataset,"tb");
		getTableName(tnNode); // 获取要更新的表名称

		tnNode = findNode(dataset,"op"); // tnNode表示<Op>
		AnalyseParam(tnNode); // 分析参数

		// 第一个为<tableset>的text,tnNode代表<FIELDS>
		tnNode = findNode(dataset,"FS");

		result = createSqls(tnNode); // 构造更新所需要的SQL语句

		if (result < 0) {
			_errorInf = "在查找字段中缺少安全控制字段，系统终止操作.";
			return result;
		}

		TSqlObject sqlObject = null;
		if (hallbUpdate) {
			sqlObject = new TSqlObject();
			sqlObject.sql = allbUpdateSql; 
			sqlObject.tableID = tableID;
			sqlObject.sqlType = allbUpdateTag;
			sqlObject.updateAtt = allbUpdateUAtt;

			sqlObject.params = allbUpdateField;
			sqlObject.paramtype = allbUpdateFType;
			allSql.add(sqlObject);
		}

		//if(_databaseType==0){  //oracle
		//  tnNode = dataset.getLastChild(); 
		//}else{
		tnNode = findNode(dataset,"RS");     
		
		NodeList nList; // 子节点列表
		NamedNodeMap map; // 属性列表
		nList = tnNode.getChildNodes(); /* 获取每一行的子节点列表 */
		
		int i = 0,j = 0;
		// 产生SQL语句
		while (i < nList.getLength()) {
			Node node = nList.item(i);
			//if (node.getNodeType() != 3 && i > 0) { // 非文本节点
			if (node.getNodeType() != 3){
				if(j>0){
				    map = node.getAttributes(); // 获取数据，属性中包含了具体的数据
				    int R_Type = Integer.parseInt(map.getNamedItem("RT").getNodeValue());
				    switch (R_Type) {
				    case 1: // 被修改的记录; 1-是原记录,2-是修改后的记录
					    result = applyUpdate(map, node);
					    break;
				    case 3: // 新增加的记录
				    	result = applyInsert(map);
					    break;
				    case 4: // 被删除的记录
				    	result = applyDelete(map);
					    break;
				    } 
				}
				j++;
			}
			i++;
		}

		if (hallaUpdate) {
			sqlObject = new TSqlObject();
			sqlObject.sql = allaUpdateSql;
			sqlObject.tableID = tableID;
			sqlObject.sqlType = allaUpdateTag;
			sqlObject.updateAtt = allaUpdateUAtt;

			sqlObject.params = allaUpdateField;
			sqlObject.paramtype = allaUpdateFType;
			allSql.add(sqlObject);
		}

		if (hallwaysUpdate) {
			sqlObject = new TSqlObject();
			sqlObject.sql = allwaysUpdateSql;
			sqlObject.tableID = tableID;
			sqlObject.sqlType = allwaysUpdateTag;
			sqlObject.updateAtt = allwaysUpdateUAtt;

			sqlObject.params = allwaysUpdateField;
			sqlObject.paramtype = allwaysUpdateFType;
			
			//P(allwaysUpdateSql);
			allSql.add(sqlObject);
		}
		return result;
	}

	/**
	 * 使用递归方法按 节点名 类型 枝的格式 显示xml数据每个节点的值，此方法用来查看xml数据
	 * 
	 * @param node
	 *          Node 要显示的节点及下级接点
	 * @param space
	 *          String 间隔字符串
	 */
	public void showNode(Node node, String space) {
		// 显示本节点
		System.out.print(space + "NodeName:" + node.getNodeName() + "  NodeType:" + node.getNodeType()
				+ "  NodeValue:" + node.getNodeValue() + "\n");
		// 显示下级节点
		if (node.hasChildNodes()) {
			NodeList nList = node.getChildNodes();
			for (int i = 0; i < nList.getLength(); i++)
				showNode(nList.item(i), "  " + space); //
		}
		// 显示节点的属性
		if (node.hasAttributes()) {
			NamedNodeMap map = node.getAttributes();
			for (int i = 0; i < map.getLength(); i++)
				showNode(map.item(i), "  " + space);
		}
	}

	private String analysisError(String sMsg){
	    int n = sMsg.indexOf("[SQLServer]");
	    return sMsg.substring(n+11);
	}
	/**
	 * 根据xml流数据生成SQL语句，更新数据库数据
	 * 
	 * @param in
	 *          输入流
	 * @return 返回值 0-成功
	 */
	protected int applyUpdateByInputStream(InputStream in) {
		int j, result = -1;
		// Savepoint savepoint = null;
		// String t1,t2,t3;
		// t1 = CUtil.getTime();

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(in); // 建立文档
			//showNode(doc," ");
			// 获取数据集列表,每个数据集是一个dataset子接点
			 NodeList datasets = doc.getFirstChild().getChildNodes();
			
			allSql = new Vector<TSqlObject>(); // allSql为sql缓存
			// 生成sql语句
			for (int i = 0; i < datasets.getLength(); i++) {
				Node node = datasets.item(i); // 更新各个数据集
				if (node.getNodeType() != 3) // 跳过文本节点
					result = applyUpdates(datasets.item(i));
			}
			// t2 = CUtil.getTime();
			//P("allSql:"+allSql.size());

			if (allSql.size() > 0)
				try {
					// System.out.println(allSql.size());
					closeConn();
					if (conn == null || conn.isClosed())
						conn = getConnection();

					if (stmt == null)
						stmt = conn.createStatement();
					CallableStatement cstmt = null;

					conn.setAutoCommit(false);

					// savepoint = conn.setSavepoint("POINT1");

					Iterator<TSqlObject> it = allSql.iterator();
					int sqlType = 0, updateAtt = 0;
					while (it.hasNext()) {
						TSqlObject t = (TSqlObject) it.next();
						sqlType = t.sqlType;
						updateAtt = t.updateAtt;

						switch (sqlType) {
						case 1:
						    P("执行SQL\n"+t.sql);
							RecordCount = stmt.executeUpdate(t.sql) + 1;
							break;
						case 2:
						    P("执行SQL\n"+t.sql);
							RecordCount = stmt.executeUpdate(t.sql);
							break;
						case 3:
						    P("执行SQL\n"+t.sql);
							RecordCount = stmt.executeUpdate(t.sql);
							break;
						case 4: // 存储过程
							P("执行SQL\n"+t.sql);
							cstmt = conn.prepareCall("{" + t.sql + "}");

							cstmt.registerOutParameter(1, Types.INTEGER);
							cstmt.registerOutParameter(2, Types.VARCHAR);
							for (j = 2; j < t.params.length; j++) {

								P("param=" + t.params[j]);

								switch (t.paramtype[j]) {
								case 1:
									cstmt.setString(j + 1, t.params[j]);
									break;
								case 2:
									cstmt.setLong(j + 1, Long.parseLong(t.params[j]));
									break;
								case 3:
									cstmt.setDouble(j + 1, Double.parseDouble(t.params[j]));
									break;
								case 4:
									cstmt.setTimestamp(j + 1, Timestamp.valueOf(t.params[j]));
									break;
								}
							}
							cstmt.executeQuery();
							RecordCount = cstmt.getInt(1);
							if (RecordCount != 0) {
								RecordCount = 0;
								_errorInf = cstmt.getString(2);
							} else
								RecordCount = 1;
							break;
						case 5:
						case 6:
							if (_databaseType == 0) {  //oracle
								
								if(sqlType==6)
									t.sql = CUtil.formatStr(t.sql, t.params, "%s");
								
								pstmt = conn.prepareStatement(t.sql);

								if(sqlType==5)  
								for (j = 0; j < t.params.length; j++) {
									switch (t.paramtype[j]) {
									case 1:
										pstmt.setString(j + 1, t.params[j]);
										break;
									case 2:
										pstmt.setLong(j + 1, Long.parseLong(t.params[j]));
										break;
									case 3:
										pstmt.setDouble(j + 1, Double.parseDouble(t.params[j]));
										break;
									case 4:
										pstmt.setTimestamp(j + 1, Timestamp.valueOf(t.params[j]));
										break;
									default:
										break;
									}
								}
								RecordCount = pstmt.executeUpdate() + (updateAtt & 1);
								
							} else {										//sql server
								t.sql = CUtil.formatStr(t.sql, t.params, "%s");
								P("执行SQL\n"+t.sql);

								RecordCount = stmt.executeUpdate(t.sql) + (updateAtt & 1);
							}
							break;
						}
						if (RecordCount == 0)
							break;
					}
					if (RecordCount > 0) {
						conn.commit();
						result = 0;
					} else {
						conn.rollback();
						switch (sqlType) {
						case 1:
							_errorInf = "系统无法插入记录.";
							break;
						case 2:
							_errorInf = "系统无法更新记录,有可能记录已经被其他用户更改.";
							break;
						case 3:
							_errorInf = "系统无法删除记录,有可能记录已经被其他用户更改.";
							break;
						case 5:
							_errorInf = "执行附加SQL语句异常,影响的记录数为0.";
							break;
						}
						P("执行SQL结果：\n"+_errorInf);
						result = -3;
					}
					if (cstmt != null)
						cstmt.close();
				} catch (SQLException ex) {
					conn.rollback();
					_errorInf = analysisError(ex.getMessage());
					result = -2;
					throw new SQLException("更新数据产生异常：\n" + _errorInf);
				} finally {

					// conn.releaseSavepoint(savepoint);
					conn.setAutoCommit(true);
					closeConn();
					allSql.removeAllElements();
					allSql.clear();
					/*
					 * t3 = CUtil.getTime();
					 * 
					 * if (CInitParam.isRecordStep()) { executeQuery("select
					 * seq_sys.nextval a from dual"); rs.next(); savePageId =
					 * rs.getString(1); String pageId = "0"; String savestime = ""; if
					 * (request.getParameter("savestime") != null) { pageId =
					 * request.getParameter("pageid").toString(); savestime =
					 * request.getParameter("savestime").toString(); } String Sql =
					 * "insert into speedlog(xh,pageid,action,stime,ctime,dtime,etime) " +
					 * "values(" + savePageId + "," + pageId + ",'提交','" + savestime +
					 * "','" + t1 + "','" + t2 + "','" + t3 + "')"; executeUpdate(Sql);
					 * _errorInf = savePageId; }
					 */

				}
		} catch (Exception pcException) {
			pcException.printStackTrace(); 
			closeConn();
			_errorInf = pcException.getMessage()+"\n发生于函数：CTable.applyUpdateByInputStream。";
			result = -1;
		} finally {
		}
		return result;
	}

	/**
	 * 根据xml数据，产生相关的sql语句，执行数据库更新操作
	 * 
	 * @param request
	 *          HttpServletRequest 页面的请求，xml数据包含在请求数据流中
	 * @return String 返回结果
	 */
	public int applyUpdates(HttpServletRequest request) {
		int result = -1;

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			_innerCall = false;  //外部页面调用
			 
			ServletInputStream in = request.getInputStream();
			
			//byte[] b = new byte[request.getContentLength()];
			//in.read(b);
			//String tStr = new String(b, "utf-8");
			
			//System.out.println("tStr="+tStr);
			result = applyUpdateByInputStream(in);
			
		} catch (Exception pcException) {
			pcException.printStackTrace();
			_errorInf = "[applyUpdates]" + pcException.getMessage();
			result = -1;
		} finally {
		}
		return result;
	}
    /**
     * delphi程序调用
     * @param request
     * @return
     */
	public int delphiUpdates(HttpServletRequest request) {
		int result = -1;

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			_innerCall = true;  //delphi程序调用
			 
			ServletInputStream in = request.getInputStream();
			
			//byte[] b = new byte[request.getContentLength()];
			//in.read(b);
			//String tStr = new String(b, "utf-8");
			
			//System.out.println("tStr="+tStr);
			result = applyUpdateByInputStream(in);
			
		} catch (Exception pcException) {
			pcException.printStackTrace();
			_errorInf = "[applyUpdates]" + pcException.getMessage();
			result = -1;
		} finally {
		}
		return result;
	}

	public int applyUpdates(String xmlString) {
		int result = -1;

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			_innerCall = true; 
			InputStream in = new ByteArrayInputStream(xmlString.getBytes());
			result = applyUpdateByInputStream(in);
		} catch (Exception pcException) {
			pcException.printStackTrace();
			_errorInf = "[applyUpdates]" + pcException.getMessage();
			result = -1;
		} finally {
		}
		return result;
	}

	private String PrecessParam(String paramStr, String afxstr) {
		String sql = "";
		String param = "";
		int s = 0;
		int e = 0;
		if (!paramStr.equals("")) {
			s = paramStr.indexOf("(");
			e = paramStr.indexOf(")");
			if (s > 1) {
				sql = paramStr.substring(0, s);
				String[] params = paramStr.substring(s + 1, e).split(",");
				for (int i = 0, j = 10; i < params.length; i++, j++)
					param = param + " p" + j + "=\"" + params[i] + "\"";
			} else
				sql = paramStr;
		}
		return "<" + afxstr + param + ">" + sql + "</" + afxstr + ">";
	}
	
	public int applyUpdates(String metaXml, String deltaXml, String tableName) {
		int result = -1;

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			String xmlString = "<datasets><db>" + "<tb tn=\"" + tableName + "\"/>" + "<op>"
					+ "<ut>1</ut>" + "<bi></bi>" + "<ai></ai>" + "<bd></bd>" + "<ad></ad>" + "<bu></bu>"
					+ "<au></au>" + "<albu></albu>" + "<alau></alau>" + "<allways></allways>" + "</op>"
					+ metaXml + deltaXml + "</db></datasets>";
			
			_innerCall = true;
			
			InputStream in = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			result = applyUpdateByInputStream(in);
		} catch (Exception pcException) {
			pcException.printStackTrace();
			_errorInf = "[applyUpdates]" + pcException.getMessage();
			result = -1;
		} finally {
		}
		return result;
	}

	public int applyUpdates(String metaXml, String deltaXml, String tableName,
			String vBeforeInsertSql, String vAfterInsertSql, String vBeforeDeleteSql,
			String vAfterDeleteSql, String vBeforeUpdateSql, String vAfterUpdateSql,
			String vaBeforeUpdateSql, String vaAfterUpdateSql, String vallLastUpdateSql) {
		int result = -1;

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			String xmlString = 
			  "<datasets><db>" +
			  "<tb tn=\"" + tableName + "\"/>" +
			  "<op><ut>1</ut>" + 
			  PrecessParam(vBeforeInsertSql,"bi")+
			  PrecessParam(vAfterInsertSql,"ai")+
			  PrecessParam(vBeforeDeleteSql,"bd")+
			  PrecessParam(vAfterDeleteSql,"ad")+
			  PrecessParam(vBeforeUpdateSql,"bu")+
			  PrecessParam(vAfterUpdateSql,"au")+
			  PrecessParam(vaBeforeUpdateSql,"albu")+
			  PrecessParam(vaAfterUpdateSql,"alau")+
			  PrecessParam(vallLastUpdateSql,"allways")+
			  "</op>" + metaXml + deltaXml + "</db></datasets>";
			
			_innerCall = true;
			
			InputStream in = new ByteArrayInputStream(xmlString.getBytes());
			result = applyUpdateByInputStream(in);
		} catch (Exception pcException) {
			pcException.printStackTrace();
			_errorInf = "[applyUpdates]" + pcException.getMessage();
			result = -1;
		} finally {
		}
		return result;
	}
	
	public int applyUpdatesWithSql(String metaXml, String deltaXml, String tableName) {
		int result = -1;

		try {
			// 以下4句代码把request中的xml数据流放到文档对象中
			String xmlString = 			  
				"<datasets><db>" +
		  	"<tb tn=\"" + tableName + "\"/>" +
		  	"<op><ut>1</ut>" + 
		  	PrecessParam(this.BeforeInsertSql,"bi")+
		  	PrecessParam(this.AfterInsertSql,"ai")+
		  	PrecessParam(this.BeforeDeleteSql,"bd")+
		  	PrecessParam(this.AfterDeleteSql,"ad")+
		  	PrecessParam(this.BeforeUpdateSql,"bu")+
		  	PrecessParam(this.AfterUpdateSql,"au")+
		  	PrecessParam(this.aBeforeUpdateSql,"albu")+
		  	PrecessParam(this.aAfterUpdateSql,"alau")+
		  	PrecessParam(this.allLastUpdateSql,"allways")+
		    "</op>" + metaXml + deltaXml + "</db></datasets>";

			_innerCall = true;
			
			InputStream in = new ByteArrayInputStream(xmlString.getBytes());
			result = applyUpdateByInputStream(in);
		} catch (Exception pcException) {
			pcException.printStackTrace();
			_errorInf = "[applyUpdatesWithSql]" + pcException.getMessage();
			result = -1;
		} finally {
		}
		return result;
	}
	
	public String getApplyUpdateXml(String metaXml, String deltaXml, String tableName){
		
		return "<db>" + "<tb tn=\"" + tableName + "\"/>" + "<op>"
		       + "<ut>1</ut>" + "<bi></bi>" + "<ai></ai>" + "<bd></bd>" 
		       + "<ad></ad>" + "<bu></bu>"
		       + "<au></au>" + "<albu></albu>" + "<alau></alau>" 
		       + "<allways></allways>" + "</op>"
		       + metaXml + deltaXml + "</db>";
	}

	public String getApplyUpdateXml(String metaXml, String deltaXml, String tableName,
			String vBeforeInsertSql, String vAfterInsertSql, String vBeforeDeleteSql,
			String vAfterDeleteSql, String vBeforeUpdateSql, String vAfterUpdateSql,
			String vaBeforeUpdateSql, String vaAfterUpdateSql, String vallLastUpdateSql){
		return "<db>" +
  	  "<tb tn=\"" + tableName + "\"/>" +
  	  "<op><ut>1</ut>" + 
  	  PrecessParam(vBeforeInsertSql,"bi")+
  	  PrecessParam(vAfterInsertSql,"ai")+
  	  PrecessParam(vBeforeDeleteSql,"bd")+
  	  PrecessParam(vAfterDeleteSql,"ad")+
  	  PrecessParam(vBeforeUpdateSql,"bu")+
  	  PrecessParam(vAfterUpdateSql,"au")+
  	  PrecessParam(vaBeforeUpdateSql,"albu")+
  	  PrecessParam(vaAfterUpdateSql,"alau")+
  	  PrecessParam(vallLastUpdateSql,"allways")+
  	  "</op>" + metaXml + deltaXml + "</db>";
	}	
	
	public String getApplyUpdateXmlWithSql(String metaXml, String deltaXml, String tableName){
		return
		  "<db>" +
  	  "<tb tn=\"" + tableName + "\"/>" +
  	  "<op><ut>1</ut>" + 
  	  PrecessParam(this.BeforeInsertSql,"bi")+
  	  PrecessParam(this.AfterInsertSql,"ai")+
  	  PrecessParam(this.BeforeDeleteSql,"bd")+
  	  PrecessParam(this.AfterDeleteSql,"ad")+
  	  PrecessParam(this.BeforeUpdateSql,"bu")+
  	  PrecessParam(this.AfterUpdateSql,"au")+
  	  PrecessParam(this.aBeforeUpdateSql,"albu")+
  	  PrecessParam(this.aAfterUpdateSql,"alau")+
  	  PrecessParam(this.allLastUpdateSql,"allways")+
  	  "</op>" + metaXml + deltaXml + "</db>";
	}
}
