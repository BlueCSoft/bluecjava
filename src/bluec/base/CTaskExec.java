package bluec.base;

public class CTaskExec implements CTaskFace {
	private boolean isBusy = false;
    
	public int doTask() {
		try{
			System.out.println("��ʱ����ִ��...");
			if(!isBusy && !CInitParam.TaskClass.equals("")){
				isBusy = true;
		        Class.forName(CInitParam.TaskClass).newInstance();
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
