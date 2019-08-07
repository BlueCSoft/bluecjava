package bluec.base;

import java.util.TimerTask;
import bluec.base.CAppTask;

public class CTask extends TimerTask{

	public void run(){
		CAppTask.CallTask();
	}
}

