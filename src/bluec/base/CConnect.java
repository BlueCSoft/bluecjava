package bluec.base;

import java.sql.Connection;
import java.sql.SQLException;
//import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import bluec.base.CInitParam;

public class CConnect {
	static Context ic = null;

	static DataSource ds = null;

	static Logger logger = Logger.getLogger(CConnect.class.getName());

	static String _errorinf = "";

	static {
		try {
			ic = (Context) (new InitialContext());
			if (ic == null)
				throw new Exception("û��ƥ��Ļ���");

			ds = (DataSource) ic.lookup(CInitParam.connStr);
		} catch (Exception e) {
			System.err.println("��ʼ�����ӳ��쳣:" + e.getMessage());
		}
	}

	public static Connection getConnection() throws SQLException {
		try {
			return ds.getConnection();
		} catch (Exception e) {
			_errorinf = e.getMessage();
			throw new SQLException(_errorinf);
		}
	}

}
