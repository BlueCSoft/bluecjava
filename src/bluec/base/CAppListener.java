package bluec.base;

/**
 * <p>Title: Ӧ�ó��򼰶Ի�����</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import java.util.Timer;

public class CAppListener implements ServletContextListener,
		HttpSessionListener {
	// ���߲���Ա
	Map<String,HttpSession> colusers = new HashMap<String,HttpSession>();

	// �����̼�
	Map<String,HttpSession> colmarchants = new HashMap<String,HttpSession>();

	Timer timer = new Timer(true);
	
	static ServletContext gcontext = null;
	
	public CAppListener() { 
	}   
 
	public void contextInitialized(ServletContextEvent sce) {
		gcontext = sce.getServletContext(); 
		
		// ����������¼�����û���Ķ���
		gcontext.setAttribute("olusers", colusers);
		gcontext.setAttribute("olmarchants", colmarchants);

		CInitParam.InitParam(gcontext);
		String autotask = getParam("autotask");
		if(autotask != null && autotask.equals("yes")){
		    CAppTask.SetTask(new CTaskExec());
		    CInitParam.RunTimer(timer);  
		}
	}   

	public void contextDestroyed(ServletContextEvent sce) {
		timer.cancel();
		ServletContext context = sce.getServletContext();
		context.removeAttribute("olusers");
		context.removeAttribute("olmarchants");
	}

	public void sessionCreated(HttpSessionEvent hse) {
		HttpSession session = hse.getSession();
		// ����session������ʱ��
		session.setAttribute("starttime", (new Date()).toString());
		colusers.put(session.getId(), session);
	}

	public void sessionDestroyed(HttpSessionEvent hse) {
		HttpSession session = hse.getSession();
		// �Ѿ���¼
		if (session.getAttribute(CInitParam.safeField) != null) {
			String nDealerId = session.getAttribute(
					CInitParam.safeFieldValue).toString();
			// �����̼����ߵ��û���,�û���Ϊ0ʱ,��ȥ�����̼�
			if (colmarchants.get(nDealerId) != null) {
				CMarchant Marchant = (CMarchant) (colmarchants.get(nDealerId));
				Marchant.nusers--;
				if (Marchant.nusers <= 0)
					colmarchants.remove(nDealerId);
			}
		}
		colusers.remove(session.getId());
	}

	public int getUserCount() {
		String safefield = CInitParam.safeField;
		int iRecNo = 0;
		HttpSession usession = null;
		Iterator<String> it = colusers.keySet().iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			usession = (HttpSession) colusers.get(key);
			if (usession.getAttribute(safefield) != null)
				iRecNo++;
		}
		return iRecNo;
	}
	
	public static String getParam(String paramName){
		return (gcontext.getInitParameter(paramName)!=null)?
				gcontext.getInitParameter(paramName).toString():null;
	}
	
	public static String getParam(String paramName,String defaultValue){
		return (gcontext.getInitParameter(paramName)!=null)?
				gcontext.getInitParameter(paramName).toString():defaultValue;
	}
}
