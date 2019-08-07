package bluec.base;

/**
 * <p>Title: 应用程序及对话管理</p>
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
	// 在线操作员
	Map<String,HttpSession> colusers = new HashMap<String,HttpSession>();

	// 在线商家
	Map<String,HttpSession> colmarchants = new HashMap<String,HttpSession>();

	Timer timer = new Timer(true);
	
	static ServletContext gcontext = null;
	
	public CAppListener() { 
	}   
 
	public void contextInitialized(ServletContextEvent sce) {
		gcontext = sce.getServletContext(); 
		
		// 设置用来记录在线用户表的队列
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
		// 设置session启动的时间
		session.setAttribute("starttime", (new Date()).toString());
		colusers.put(session.getId(), session);
	}

	public void sessionDestroyed(HttpSessionEvent hse) {
		HttpSession session = hse.getSession();
		// 已经登录
		if (session.getAttribute(CInitParam.safeField) != null) {
			String nDealerId = session.getAttribute(
					CInitParam.safeFieldValue).toString();
			// 更新商家在线的用户数,用户数为0时,移去在线商家
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
