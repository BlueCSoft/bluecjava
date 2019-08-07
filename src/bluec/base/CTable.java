package bluec.base;

/**
 * <p>Title: ���ݴ����</p>
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
 * CTable ����һ����
 */
public class CTable extends CDataSet {

	protected String[] allFields; // �ֶ����б�

	protected int[] allFieldType; // �ֶ����б�

	/* ���ݸ���ϵ��SQL��� */
	protected int tableID = 0; 

	protected String tableName = null; // �����������

	protected String insertSql = null; // ����SQL���

	protected String valuesSql = null; // values���

	protected String updateSql = null; // ����SQL���

	protected String deleteSql = null; // ɾ��SQL���

	protected String whereSql = null; // �������

	protected int[] wDataTypes = null; // ��������

	protected int[] wIsNull = null; // �Ƿ�ɿ�

	/*
	 * ���ݸ����ֶμ�¼
	 */
	protected String[] updateFields = null; // ֵ�ֶ��б�

	protected String[] dataTypes = null; // ��������

	protected String[] whereFields = null; // �����ֶ��б�

	protected String[] safeFields = null; // ��ȫ���Ƽ���ֶ��б�

	protected String[] safeSFields = null; // ��ȫ���Ƽ���ֶζ�Ӧsession���Ա�

	protected int[] setSafes = null; // ��ȫ����ǿ���ֶ�����

	protected int[] seachFields = null; // �����ֶ�����

	protected int safeCount = 0;

	/* ������Ʊ�� */
	protected int useTransaction = 0;

	/* ���SQL������ */
	/* ÿ����¼����֮ǰҪִ�е�sql��� */
	protected boolean hasbInsert = false; // ��ʾ�Ƿ���sql���Ҫִ��

	protected String bInsertSql = ""; // Ҫִ�е�sql���

	protected int bInsertTag = 0;

	protected int bInsertUAtt = 0;

	protected String[] bInsertField = null; // �ֶβ���

	protected int[] bInsertFType = null; // �ֶ�����

	protected int[] bInsertFFrom = null; // �ֶ���Դ

	/* ÿ����¼����֮��Ҫִ�е�sql���* */
	protected boolean hasaInsert = false;

	protected String aInsertSql = "";

	protected int aInsertTag = 0;

	protected int aInsertUAtt = 0;

	protected String[] aInsertField = null;

	protected int[] aInsertFType = null; // �ֶ�����

	protected int[] aInsertFFrom = null; // �ֶ���Դ

	/* ÿ����¼ɾ��֮ǰҪִ�е�sql���* */
	protected boolean hasbDelete = false;

	protected String bDeleteSql = "";

	protected int bDeleteTag = 0;

	protected int bDeleteUAtt = 0;

	protected String[] bDeleteField = null;

	protected int[] bDeleteFType = null; // �ֶ�����

	protected int[] bDeleteFFrom = null; // �ֶ���Դ

	/* ÿ����¼ɾ��֮��Ҫִ�е�sql���* */
	protected boolean hasaDelete = false;

	protected String aDeleteSql = "";

	protected int aDeleteTag = 0;

	protected int aDeleteUAtt = 0;

	protected String[] aDeleteField = null;

	protected int[] aDeleteFType = null; // �ֶ�����

	protected int[] aDeleteFFrom = null; // �ֶ���Դ

	/* ÿ����¼����֮ǰҪִ�е�sql���* */
	protected boolean hasbUpdate = false;

	protected String bUpdateSql = "";

	protected int bUpdateTag = 0;

	protected int bUpdateUAtt = 0;

	protected String[] bUpdateField = null;

	protected int[] bUpdateFType = null; // �ֶ�����

	protected int[] bUpdateFFrom = null; // �ֶ���Դ

	/* ÿ����¼����֮��Ҫִ�е�sql���* */
	protected boolean hasaUpdate = false;

	protected String aUpdateSql = "";

	protected int aUpdateTag = 0;

	protected int aUpdateUAtt = 0;

	protected String[] aUpdateField = null;

	protected int[] aUpdateFType = null; // �ֶ�����

	protected int[] aUpdateFFrom = null; // �ֶ���Դ

	/* ���ݼ�ִ�����ݿ����֮ǰҪִ�е�sql���* */
	protected boolean hallbUpdate = false;

	protected String allbUpdateSql = "";

	protected int allbUpdateTag = 0;

	protected int allbUpdateUAtt = 0;

	protected String[] allbUpdateField = null;

	protected int[] allbUpdateFType = null; // �ֶ�����

	protected int[] allbUpdateFFrom = null; // �ֶ���Դ

	protected boolean[] bUpdateOldValue = null;

	/* ���ݼ�ִ�����ݿ����֮ǰҪִ�е�sql���* */
	protected boolean hallaUpdate = false;

	protected String allaUpdateSql = "";

	protected int allaUpdateTag = 0;

	protected int allaUpdateUAtt = 0;

	protected String[] allaUpdateField = null;

	protected int[] allaUpdateFType = null; // �ֶ�����

	protected int[] allaUpdateFFrom = null; // �ֶ���Դ

	protected boolean[] aUpdateOldValue = null;

	// ��������Ҫִ�е����
	protected boolean hallwaysUpdate = false;

	protected String allwaysUpdateSql = "";

	protected int allwaysUpdateTag = 0;

	protected int allwaysUpdateUAtt = 0;

	protected String[] allwaysUpdateField = null;

	protected int[] allwaysUpdateFType = null; // �ֶ�����

	protected int[] allwaysUpdateFFrom = null; // �ֶ���Դ

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
	 * SQL������,�����洢һ����Ҫִ�е�SQL���
	 * 
	 * @author Administrator
	 * 
	 */
	private class TSqlObject {
		public int tableID;   		// ��id,�������ָ�sql���ڵı�

		public int recNo;     		// ��¼��

		public String sql;    		// Ҫִ�е�sql���

		public int sqlType;   		// sql����,1-insert,2-update,3-delete,4-procedure,5-sql

		public int updateAtt;   	// ��������,0-�����пո��£�1-���пո���

		public String[] params;   // ����ֵ

		public int[] paramtype; 	// ������������
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
	 * �������ݵ����ݿ�,xml���ݸ�ʽ����
	 * 
	 * @param Ҫ���������
	 * @return =0 �ɹ� ��0 - ������� ���ݸ�ʽ <datasets> <dataset> <table tablename="������"/>
	 *         <fields> <field attribname="�ֶ�����" ... /> ... </fields> <rowdata>
	 *         <row field1="����" ... /> ... </rowdata> </dataset> <dataset> ...
	 *         </dataset> </datasets>
	 */
	/**
	 * ��ȡ������
	 * 
	 * @param �����ƽڵ�
	 * @return =0 �ɹ� ��0 - �������
	 */
	protected int getTableName(Node node) {
		// ��ȡ���Խڵ��
		/* ������� */
		// System.out.print("NodeName:"+node.getNodeName()+"
		// NodeType:"+node.getNodeType()+"\n");
		// ��ȡ�������
		tableName = node.getAttributes().getNamedItem("tn").getNodeValue();

		return 0;
	}

	/**
	 * ����xml�а������ֶ�������Ϣ������sql���
	 * 
	 * @param node
	 *          ����Ԫ�ĸ��ڵ�<medata>
	 * @return >0�ɹ�,0-���ֶ���Ϣ,<0�ڲ��������ȱ�ٰ�ȫ�����ֶ�
	 */
	protected int createSqls(Node node) throws SQLException {
		NodeList nList; // �ӽڵ��б�
		NamedNodeMap map; // �����б�
		/* SQL���� */
		StringBuffer insertBuf = new StringBuffer();
		StringBuffer valueBuf = new StringBuffer();
		StringBuffer updateBuf = new StringBuffer();
		StringBuffer whereBuf = new StringBuffer();

		/* �ֶμ�¼���� */
		Vector<String> vFields = new Vector<String>(); // �ɸ����ֶ�
		Vector<String> vDataType = new Vector<String>(); // �ɸ����ֶ�����
		Vector<String> vWhere = new Vector<String>(); // �����ֶ�

		int uIndex = 0; // �����ֶ�����,���ҵ�һ��������µ��ֶ�ʱ��1
		int wIndex = 0; // �����ֶ����������ҵ�һ��������Ϊ�����ֶ�ʱ��1

		/* SQL��俪ʼ���� */
		insertBuf.append("INSERT INTO " + tableName + "(");
		valueBuf.append("VALUES(");

		updateBuf.append("UPDATE " + tableName);
		whereBuf.append("WHERE ");

		// ��ȡ�ֶ������ڵ�
		nList = node.getChildNodes();

		allFields = new String[nList.getLength()];
		allFieldType = new int[nList.getLength()];

		wDataTypes = new int[nList.getLength()];
		wIsNull = new int[nList.getLength()];

		// �����ֶ�����
		for (int i = 0; i < nList.getLength(); i++)
			if (nList.item(i).getNodeType() != 3) { // nList.item(i)��ʾÿһ��<FIELD>����
				map = nList.item(i).getAttributes(); // ��ȡ�����б�
				// �ֶ�����
				String fieldName = map.getNamedItem("A").getNodeValue();
				String tempStr = null; // ��ʱ��
				// ��������
				int dataType = Integer.parseInt(map.getNamedItem("B").getNodeValue());
				// �Ƿ�ɸ���
				int fieldUpdate = Integer.parseInt(map.getNamedItem("U").getNodeValue());
				// �Ƿ�ɿ�
				int isNull = Integer.parseInt(map.getNamedItem("D").getNodeValue());

				allFields[i] = fieldName;
				allFieldType[i] = dataType;

				// ���ݲ�ͬ���������Ͳ�����Ӧ�ĸ�ʽ
				switch (dataType) {
				case -1:// SqlServer text����
				case 1:// 1-CHAR����,12-VARCHAR2����
				case 12:
					tempStr = "'%s'";
					break;
				case 91:
				case 93: // DATE����
					tempStr = __upFormat[_databaseType];
					break;
				default: // ��������
					tempStr = "%s";
				}

				if (fieldUpdate == 1) { // 1��ʾ���ֶ��������
					uIndex++;
					// ���ǰ���Ѿ�����һ���ֶΣ��ȼ�һ�����ŷָ�
					if (uIndex > 1) {
						insertBuf.append(",");
						valueBuf.append(",");
					}

					vFields.add(fieldName);
					vDataType.add(Integer.toString(dataType));
					// ����Insert���ĸ�ʽ
					insertBuf.append(fieldName);
					valueBuf.append(tempStr);
				}
				// ��������ֶ�
				if (dataType > 0 && map.getNamedItem("S").getNodeValue().equals("1")) { // ������Ϊ�����ֶ�
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

		// ��ȫ���¼��
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
			if(afx.equals("proc.")){  //�Ǵ洢����
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
				   __bufFroms = __bufFroms+fgf+"2";		//ʹ��ԭ�ֶ�ֵ		 
			   }else{
					 __bufField = __bufField+fgf+fName;
					 __bufFroms = __bufFroms+fgf+"1"; //ʹ�����ֶ�ֵ
				 }	 
				 __bufDType = __bufDType+fgf+"1";   //ȫ�����ַ��ʹ���
				 
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
	 * ��������
	 * 
	 * @param node
	 *          Node ��ʱ��ʾ<usetran>
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
				// ��ȡ����
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
				// ��ȡ����
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
	 * ���Ӽ�¼�����ݿ���ȥ
	 * 
	 * @param map
	 *          ����һ�еĸ��ֶ�����
	 * @return
	 */
	protected int applyInsert(NamedNodeMap map) throws SQLException {
		String values[] = new String[updateFields.length]; // ����ֵ�洢����
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
				if (isnull) // ��������
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
					switch (bInsertFFrom[i]) {  //������Դ
					case 1:                     //�ֶ���ֵ
						sqlObject.params[i] = map.getNamedItem(bInsertField[i]).getNodeValue();
						if ((bInsertFType[i] == 2 || bInsertFType[i] == 3)
								&& (sqlObject.params[i] == null || sqlObject.params[i].trim().equals("")))
							sqlObject.params[i] = "0";
						break;
					case 2:                     //�ֶ�ԭֵ
						if (bInsertFType[i] == 2 || bInsertFType[i] == 3) // �ֶ�ԭֵ
							sqlObject.params[i] = "0";
						else
							sqlObject.params[i] = "";
						break;
					case 0:                     //��
					case 5:                     //����
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
						sqlObject.params[i] = map.getNamedItem(aInsertField[i]).getNodeValue(); // �ֶ�ֵ
						if ((aInsertFType[i] == 2 || aInsertFType[i] == 3)
								&& (sqlObject.params[i] == null || sqlObject.params[i].trim().equals("")))
							sqlObject.params[i] = "0";

						break;
					case 2:
						if (aInsertFType[i] == 2 || aInsertFType[i] == 3) // �ֶ�ԭֵ
							sqlObject.params[i] = "0";
						else
							sqlObject.params[i] = "";
						break;
					case 0:
					case 5:
						sqlObject.params[i] = aInsertField[i]; // ����
						break;
					}
			}
			allSql.add(sqlObject);
		}

		return 0;
	}

	/**
	 * ִ�������޸Ĳ���
	 * 
	 * @param map
	 * @param node
	 * @return
	 */
	protected int applyUpdate(NamedNodeMap map, Node node) throws SQLException {
		String wheres[] = new String[whereFields.length]; // ���������������
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

		String pWhere = CUtil.formatStr(whereSql, wheres, "%s"); // ����where���

		StringBuffer setFields = new StringBuffer();
		int ncount = 0;
		setFields.append("SET ");
		// mapΪ��ֵ,newmapΪ��ֵ
		NamedNodeMap newmap = node.getNextSibling().getAttributes();

		for (int i = 0; i < updateFields.length; i++) {
			String oValue = map.getNamedItem(updateFields[i]).getNodeValue(); // ��ֵ
			String nValue = newmap.getNamedItem(updateFields[i]).getNodeValue(); // ��ֵ

			if (!oValue.equals(nValue)) { // ֻ���¾�ֵ����ʱ�Ÿ���
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
						nValue = "null"; // ����
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

			int nnum = 1; // ��¼�¾�ֵ��ͬ���ֶ���
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
							String oValue = map.getNamedItem(bUpdateField[i]).getNodeValue(); // ��ֵ
							String nValue = newmap.getNamedItem(bUpdateField[i]).getNodeValue(); // ��ֵ
							if (!oValue.equals(nValue) || (bUpdateUAtt >= 10))
								nnum++;
							if (bUpdateFFrom[i] == 2) // ʹ�þ�ֵ
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
							String oValue = map.getNamedItem(aUpdateField[i]).getNodeValue(); // ��ֵ
							String nValue = newmap.getNamedItem(aUpdateField[i]).getNodeValue(); // ��ֵ
							if (!oValue.equals(nValue) || (aUpdateUAtt >= 10))
								nnum++;
							if (aUpdateFFrom[i] == 2) // ʹ�þ�ֵ
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
	 * ִ��ɾ������
	 * 
	 * @param map
	 * @return
	 */
	protected int applyDelete(NamedNodeMap map) throws SQLException {
		String wheres[] = new String[whereFields.length]; // ���������������
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
		String pWhere = CUtil.formatStr(whereSql, wheres, "%s"); // ����where���
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
	 * ���ݸ���
	 * 
	 * @param dataset
	 *          ���ݼ��ڵ�
	 * @return
	 */
	protected int applyUpdates(Node dataset) throws SQLException {
		int result = 0;
		tableID++;
		// dataset.getFirstChild()Ϊtext�ڵ�
		// �������ӽӵ�,����text�ڵ�,tnNode��ʾ<tableset>
		Node tnNode = findNode(dataset,"tb");
		getTableName(tnNode); // ��ȡҪ���µı�����

		tnNode = findNode(dataset,"op"); // tnNode��ʾ<Op>
		AnalyseParam(tnNode); // ��������

		// ��һ��Ϊ<tableset>��text,tnNode����<FIELDS>
		tnNode = findNode(dataset,"FS");

		result = createSqls(tnNode); // �����������Ҫ��SQL���

		if (result < 0) {
			_errorInf = "�ڲ����ֶ���ȱ�ٰ�ȫ�����ֶΣ�ϵͳ��ֹ����.";
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
		
		NodeList nList; // �ӽڵ��б�
		NamedNodeMap map; // �����б�
		nList = tnNode.getChildNodes(); /* ��ȡÿһ�е��ӽڵ��б� */
		
		int i = 0,j = 0;
		// ����SQL���
		while (i < nList.getLength()) {
			Node node = nList.item(i);
			//if (node.getNodeType() != 3 && i > 0) { // ���ı��ڵ�
			if (node.getNodeType() != 3){
				if(j>0){
				    map = node.getAttributes(); // ��ȡ���ݣ������а����˾��������
				    int R_Type = Integer.parseInt(map.getNamedItem("RT").getNodeValue());
				    switch (R_Type) {
				    case 1: // ���޸ĵļ�¼; 1-��ԭ��¼,2-���޸ĺ�ļ�¼
					    result = applyUpdate(map, node);
					    break;
				    case 3: // �����ӵļ�¼
				    	result = applyInsert(map);
					    break;
				    case 4: // ��ɾ���ļ�¼
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
	 * ʹ�õݹ鷽���� �ڵ��� ���� ֦�ĸ�ʽ ��ʾxml����ÿ���ڵ��ֵ���˷��������鿴xml����
	 * 
	 * @param node
	 *          Node Ҫ��ʾ�Ľڵ㼰�¼��ӵ�
	 * @param space
	 *          String ����ַ���
	 */
	public void showNode(Node node, String space) {
		// ��ʾ���ڵ�
		System.out.print(space + "NodeName:" + node.getNodeName() + "  NodeType:" + node.getNodeType()
				+ "  NodeValue:" + node.getNodeValue() + "\n");
		// ��ʾ�¼��ڵ�
		if (node.hasChildNodes()) {
			NodeList nList = node.getChildNodes();
			for (int i = 0; i < nList.getLength(); i++)
				showNode(nList.item(i), "  " + space); //
		}
		// ��ʾ�ڵ������
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
	 * ����xml����������SQL��䣬�������ݿ�����
	 * 
	 * @param in
	 *          ������
	 * @return ����ֵ 0-�ɹ�
	 */
	protected int applyUpdateByInputStream(InputStream in) {
		int j, result = -1;
		// Savepoint savepoint = null;
		// String t1,t2,t3;
		// t1 = CUtil.getTime();

		try {
			// ����4������request�е�xml�������ŵ��ĵ�������
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(in); // �����ĵ�
			//showNode(doc," ");
			// ��ȡ���ݼ��б�,ÿ�����ݼ���һ��dataset�ӽӵ�
			 NodeList datasets = doc.getFirstChild().getChildNodes();
			
			allSql = new Vector<TSqlObject>(); // allSqlΪsql����
			// ����sql���
			for (int i = 0; i < datasets.getLength(); i++) {
				Node node = datasets.item(i); // ���¸������ݼ�
				if (node.getNodeType() != 3) // �����ı��ڵ�
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
						    P("ִ��SQL\n"+t.sql);
							RecordCount = stmt.executeUpdate(t.sql) + 1;
							break;
						case 2:
						    P("ִ��SQL\n"+t.sql);
							RecordCount = stmt.executeUpdate(t.sql);
							break;
						case 3:
						    P("ִ��SQL\n"+t.sql);
							RecordCount = stmt.executeUpdate(t.sql);
							break;
						case 4: // �洢����
							P("ִ��SQL\n"+t.sql);
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
								P("ִ��SQL\n"+t.sql);

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
							_errorInf = "ϵͳ�޷������¼.";
							break;
						case 2:
							_errorInf = "ϵͳ�޷����¼�¼,�п��ܼ�¼�Ѿ��������û�����.";
							break;
						case 3:
							_errorInf = "ϵͳ�޷�ɾ����¼,�п��ܼ�¼�Ѿ��������û�����.";
							break;
						case 5:
							_errorInf = "ִ�и���SQL����쳣,Ӱ��ļ�¼��Ϊ0.";
							break;
						}
						P("ִ��SQL�����\n"+_errorInf);
						result = -3;
					}
					if (cstmt != null)
						cstmt.close();
				} catch (SQLException ex) {
					conn.rollback();
					_errorInf = analysisError(ex.getMessage());
					result = -2;
					throw new SQLException("�������ݲ����쳣��\n" + _errorInf);
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
					 * "values(" + savePageId + "," + pageId + ",'�ύ','" + savestime +
					 * "','" + t1 + "','" + t2 + "','" + t3 + "')"; executeUpdate(Sql);
					 * _errorInf = savePageId; }
					 */

				}
		} catch (Exception pcException) {
			pcException.printStackTrace(); 
			closeConn();
			_errorInf = pcException.getMessage()+"\n�����ں�����CTable.applyUpdateByInputStream��";
			result = -1;
		} finally {
		}
		return result;
	}

	/**
	 * ����xml���ݣ�������ص�sql��䣬ִ�����ݿ���²���
	 * 
	 * @param request
	 *          HttpServletRequest ҳ�������xml���ݰ�����������������
	 * @return String ���ؽ��
	 */
	public int applyUpdates(HttpServletRequest request) {
		int result = -1;

		try {
			// ����4������request�е�xml�������ŵ��ĵ�������
			_innerCall = false;  //�ⲿҳ�����
			 
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
     * delphi�������
     * @param request
     * @return
     */
	public int delphiUpdates(HttpServletRequest request) {
		int result = -1;

		try {
			// ����4������request�е�xml�������ŵ��ĵ�������
			_innerCall = true;  //delphi�������
			 
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
			// ����4������request�е�xml�������ŵ��ĵ�������
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
			// ����4������request�е�xml�������ŵ��ĵ�������
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
			// ����4������request�е�xml�������ŵ��ĵ�������
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
			// ����4������request�е�xml�������ŵ��ĵ�������
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
