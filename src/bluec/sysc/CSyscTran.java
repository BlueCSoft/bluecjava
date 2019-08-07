package bluec.sysc;

import bluec.base.CTaskFace;

public class CSyscTran implements CTaskFace {
    private boolean isBusy = false;
    public static boolean IsInit = false;  //是否已初始化
    public static int mtCount = 0;         //分钟计数
    public static int LoginTimeLen = 0;
    public static int CounterTimeLen = 0;
    public static int CashierTimeLen = 0;
    public static int SalesTimeLen = 0;
    public static int BillTimeLen = 0;
    public static int GoodsTimeLen = 0;
    
	public int doTask() {
		try{
			System.out.println("定时任务执行...");
			if(!isBusy){
				isBusy = true;
		        new CSyscObject(null).DataAutoToSysc(false);
			}
	        isBusy = false;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			isBusy = false;
		}
		return 0;
	}

}
