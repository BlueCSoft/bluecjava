package bluec.base;

public class CObject {
	protected static void P(String msg) {
		if (CInitParam.isBprintTrack()) {
			System.out.println(CUtil.getOrigTime() + ":\n" + msg);
		}
	}

	protected static void D(String msg) {
		if (CInitParam.isDebug()) {
			System.out.println(CUtil.getOrigTime() + ":\n" + msg);
		}
	}
	
	protected static void E(String msg) {
		System.out.println(CUtil.getOrigTime() + ":" + msg);
	}
}
