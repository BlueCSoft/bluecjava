package bluec.base; 

import java.sql.SQLException;
import java.sql.*;

import javax.servlet.http.*;


public class CReport extends CQuery {

	public String GetReportList(String cid, String rpcode, String uid) throws SQLException {
		StringBuilder sResult = new StringBuilder();
		String result = "";
		String userDefRep = "";
		int rpindex = 0;

		try {
			executeQuery("select rpid,rpname,rpindex from Sys_Report " + "where cid in('" + cid
					+ "','ZZZZ') and rpcode='" + rpcode + "' order by rpindex");
			sResult.append("<xml><rps>");
			while (next()) {
				sResult.append("<rp>");
				sResult.append("<rpid>" + getString("rpid") + "</rpid>");
				sResult.append("<rpname>" + CUtil.replaceXmlSpChar(getString("rpname")) + "</rpname>");
				rpindex = getInt("rpindex");
				sResult.append("<rpindex>" + rpindex + "</rpindex>");
				sResult.append("</rp>");
			}
			if (rpindex == 0) {
				sResult.append("<rp>");
				sResult.append("<rpid>" + cid + "-" + rpcode + "-1</rpid>");
				sResult.append("<rpname>Ĭ�ϸ�ʽ</rpname>");
				rpindex = 1;
				sResult.append("<rpindex>1</rpindex>");
				sResult.append("</rp>");
			}
			sResult.append("</rps>");
			sResult.append("<rpi>" + rpindex + "</rpi>");

			executeQuery("select rpid from Sys_ReportUserDef where userid='" + uid + "'");
			if (next())
				userDefRep = getString("rpid");
			sResult.append("<rpu>" + userDefRep + "</rpu>");
			sResult.append("</xml>");
			result = sResult.toString();
		} catch (SQLException ex) {
			System.out.println("��ȡ��������ʧ�ܣ�\n " + ex.getMessage());
			result = "<xml><err>" + "��ȡ��������ʧ�ܣ�\n " + CUtil.replaceXmlSpChar(ex.getMessage())
					+ "</err></xml>";
		} finally {
			closeConn();
		}
		return result;
	}

	public String GetReportFmt(String rpid) {
		String result = "";
		StringBuilder sResult = new StringBuilder();
		try {
			executeQuery("select rpcontent from Sys_Report where rpid='" + rpid + "'");
			sResult.append("<xml><fmt>");
			if (next()) {
				sResult.append(getNote("rpcontent"));
			}
			sResult.append("</fmt></xml>");
			result = sResult.toString();
		} catch (Exception ex) {
			System.out.println("��ȡ�����ʽ����ʧ�ܣ�\n " + ex.getMessage());
			result = "<xml><err>" + "��ȡ�����ʽ����ʧ�ܣ�\n " + CUtil.replaceXmlSpChar(ex.getMessage())
					+ "</err></xml>";
		} finally {
			closeConn();
		}
		return result;
	}
 
	public String DeleteReport(String rpid, String uid) {
		String result = "";
		StringBuilder sResult = new StringBuilder();
		try {
			executeUpdate("delete from Sys_ReportUserDef where rpid='" + rpid + "'");
			executeUpdate("delete from Sys_Report where rpid='" + rpid + "'");
			sResult.append("<xml><ok>");
			sResult.append("����ɾ���ɹ�.");
			sResult.append("</ok></xml>");
			result = sResult.toString();
		} catch (Exception ex) {
			System.out.println("����ɾ��ʧ�ܣ�\n " + ex.getMessage());
			result = "<xml><err>" + "����ɾ��ʧ�ܣ�\n " + CUtil.replaceXmlSpChar(ex.getMessage())
					+ "</err></xml>";
		} finally {
			closeConn();
		}
		return result;
	}

	public String SaveUserReportDef(String rpid, String uid) {
		String result = "";
		StringBuilder sResult = new StringBuilder();
		try {

			executeUpdate("if exists(select * from Sys_ReportUserdef where userid=" + uid + ")"
					+ " update Sys_ReportUserdef set rpid='" + rpid + "' where userid='" + uid + "'"
					+ " else " + " insert into Sys_ReportUserdef(rpid,userid) values('" + rpid + "','" + uid
					+ "')");

			sResult.append("<xml><ok>");
			sResult.append("������³ɹ�.");
			sResult.append("</ok></xml>");
			result = sResult.toString();
		} catch (Exception ex) {
			System.out.println("�������ʧ�ܣ�\n " + ex.getMessage());
			result = "<xml><err>" + "�������ʧ�ܣ�\n " + CUtil.replaceXmlSpChar(ex.getMessage())
					+ "</err></xml>";
		} finally {
			closeConn();
		}
		return result;
	}

	public String UpdateReport(String rpid, String cid, String rpcode, int rpindex, String rpname,
			String rpcontent, String userid) {
		String result = "";
		
		result = "<xml><ok>���б������ݳɹ�</ok></xml>";

		int nResult = 0;
		
		try {
			if (conn == null || conn.isClosed())
				conn = CConnect.getConnection();

			CallableStatement cstmt = null;

			cstmt = conn.prepareCall("{? = call UpdateReport(?,?,?,?,?,?,?,?)}");

			cstmt.registerOutParameter(1, Types.INTEGER);
			cstmt.setString(2, rpid);
			cstmt.setString(3, cid);
			cstmt.setString(4, rpcode);
			cstmt.setLong(5, rpindex);
			cstmt.setString(6, rpname); 
			cstmt.setString(7, rpcontent);
			cstmt.setString(8, userid);

			cstmt.registerOutParameter(9, Types.VARCHAR);
			cstmt.executeUpdate();

			nResult = cstmt.getInt(1);
			if (nResult == 0)
				result = "<xml><ok>" + "���б������ݳɹ���\n " + CUtil.replaceXmlSpChar(_errorInf) + "</ok></xml>";
			else
				result = "<xml><err>" + "���б�������ʧ�ܣ�\n "
						+ CUtil.replaceXmlSpChar(cstmt.getString(9)) + "</err></xml>";
		} catch (SQLException ex) {
			_errorInf = ex.getMessage();
			result = "<xml><err>" + "���б�������ʧ�ܣ�\n " + CUtil.replaceXmlSpChar(_errorInf) + "</err></xml>";
		} finally {
			closeConn();
		} 
		return result;
	}

public String GetReportData(HttpServletRequest request)
  {
      String result = "";
      try
      {
          int op = Integer.parseInt(request.getParameter("op").toString());
          
          switch(op)
          {
              case 0: // ��ȡ�����б�
                  result = GetReportList(request.getParameter("cid").toString(),
                      request.getParameter("rpcode").toString(), 
                      request.getParameter("userid").toString());
                  break;
              case 1:
                  result = GetReportFmt(request.getParameter("rpid").toString());
                  break;
              case 2: // ���±�������
            	  
            	  result = UpdateReport(request.getParameter("rpid").toString(), 
                  		request.getParameter("cid").toString(),
                      request.getParameter("rpcode").toString(), 
                      Integer.parseInt(request.getParameter("rpindex").toString()),
                      request.getParameter("rpname").toString(), 
                      request.getParameter("rpcontent").toString(),
                      request.getParameter("userid").toString()); 
                  break;
              case 3: // ����ɾ��
                  result = DeleteReport(request.getParameter("rpid").toString(), 
                  		request.getParameter("userid").toString());
                  break;
              case 4:
                  result = SaveUserReportDef(request.getParameter("rpid").toString(), 
                  		request.getParameter("userid").toString());
                  break;
              default:
                  result = "<xml><err>" + "��Ч�Ĳ���������룺\n " + CUtil.replaceXmlSpChar(_errorInf) + "</err></xml>";
                  break;
          }
      }
      catch (Exception ex)
      {
          _errorInf = ex.getMessage();
          result = "<xml><err>" + "����������ʧ�ܣ�\n " + CUtil.replaceXmlSpChar(_errorInf) + "</err></xml>";
      }
      return result;
  }}
