package bluec.task;

import bluec.base.CQuery;
import bluec.keg.CKegss;

public class CTaskKeg extends CQuery {
	public CTaskKeg() {
		try {
			new CKegss().billDespatchCancel();
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
			P(_errorInf);
		} finally {
			closeConn();
		}
	}
}
