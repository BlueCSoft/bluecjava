package bluec.base;

import bluec.base.CTaskFace;

public class CAppTask {
   private static CTaskFace _task;
   private static boolean IsSetTask = false;
   public static void SetTask(CTaskFace task){
	   _task = task;
	   IsSetTask = true;
   }
   public static void CallTask(){
	   if(IsSetTask)
		   _task.doTask();
   }
}
