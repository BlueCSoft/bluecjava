package bluec.gyy;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;

import javax.servlet.http.HttpServletRequest;

import bluec.base.CJson;
import bluec.base.CUtil;
import oracle.jdbc.OracleTypes;

public class CYmsInterFace extends CJson {
	public CYmsInterFace(HttpServletRequest request) {
		super(request);
	}
	
	public CYmsInterFace(){
		
	}
	// 执行存储过程
	protected int callProc(String procName, String[] vparams, int[] inParamType, int[] outParamType, boolean hasRet)
			throws Exception {
		int j, result = -1;
		int inParamCount = inParamType.length;
		int outParamCount = (outParamType != null) ? outParamType.length : 0;

		if (outParamCount > 0) {
			__procReturn = new String[outParamCount];
		}

		try {

			P(vparams);
			if (conn == null || conn.isClosed())
				conn = getConnection();

			__bufSql = "call " + procName + "(" + CUtil.combinate("?", ",", inParamCount + outParamCount) + ")";
			cstmt = conn.prepareCall("{" + __bufSql + "}");

			for (j = 1; j <= inParamCount; j++) {
				switch (inParamType[j - 1]) {
				case 1:
					cstmt.setString(j, vparams[j - 1]);
					break;
				case 2:
					cstmt.setLong(j, Long.parseLong(vparams[j - 1]));
					break;
				case 3:
					cstmt.setDouble(j, Double.parseDouble(vparams[j - 1]));
					break;
				case 4:
					cstmt.setTimestamp(j, Timestamp.valueOf(vparams[j - 1]));
					break;
				}
			}

			for (j = 1; j <= outParamCount; j++) {
				switch (outParamType[j - 1]) {
				case 1:
					cstmt.registerOutParameter(j + inParamCount, Types.VARCHAR);
					break;
				case 2:
					cstmt.registerOutParameter(j + inParamCount, Types.INTEGER);
					break;
				case 3:
					cstmt.registerOutParameter(j + inParamCount, Types.DOUBLE);
					break;
				case 9:
					cstmt.registerOutParameter(j + inParamCount, OracleTypes.CURSOR);
					break;
				}
			}

			cstmt.executeQuery();

			if (outParamCount > 0) {

				if (hasRet) {
					result = cstmt.getInt(inParamCount + 1);
					__procReturn[0] = result + "";
					j = 2;
				} else {
					result = 1;
					j = 1;
				}

				for (; j <= outParamCount; j++) {
					switch (outParamType[j - 1]) {
					case 1:
						__procReturn[j - 1] = toGbk(cstmt.getString(j + inParamCount));
						break;
					case 2:
						__procReturn[j - 1] = cstmt.getInt(j + inParamCount) + "";
						break;
					case 3:
						__procReturn[j - 1] = CUtil.big(cstmt.getDouble(j + inParamCount));
						break;
					case 9:
						rs = (ResultSet) (cstmt.getObject(j + inParamCount));
						break;
					}
				}
				P("result=" + result);
				result = (result == 1) ? 0 : 1;
			} else {
				result = 0;
			}
		} catch (Exception ex) {
			_errorInf = "E:" + ex.getMessage();
			result = -1;
			P(_errorInf);
			throw new Exception(ex.getMessage());
		} finally {
			if (_errorInf == null)
				_errorInf = "未知的错误.";
		}
		return result;
	}

	protected void listField() {
		try {
			ResultSetMetaData mData = rs.getMetaData();
			P("fieldcount=" + mData.getColumnCount());
			for (int i = 1; i <= mData.getColumnCount(); i++) {
				P(mData.getColumnName(i) + "=" + mData.getColumnTypeName(i));
			}
		} catch (Exception ex) {
			P(ex.getMessage());
		}
	}

	protected String toGbk(String s) throws Exception {
		if(s==null){
			return "";
		}
		return new String(s.getBytes("ISO-8859-1"), "gbk");
	}
}
