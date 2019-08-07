package bluec.base;

import java.util.logging.Logger;
import javax.servlet.ServletContext;

import java.util.Date;
import java.util.Timer;
import bluec.base.CTask;

public class CInitParam {
	public static String appPath = "";
	public static String connStr = "";
	public static boolean bprintTrack = false;
	public static String vroot = ""; // 附件目录
	public static String dlfpath = ""; // 下载文件目录
	public static boolean rlogins = false; // 是否允许同一用户同时登录
	public static boolean rpageinfo = true; // 是否记录进入页面信息
	public static String webAppType = "tomcat";
	public static String chartset_s = "ISO8859-1";
	public static String chartset_d = "GBK";

	public static boolean sqlCache = false;

	public static boolean recordStep = true;

	public static String safeField = "isLogin"; // 安全控制字段名称

	public static String safeFieldValue = ""; // 安全字段session值

	public static int databaseType = 0; // 数据库类型

	public static String filepath1 = "";

	public static String filepath2 = "";

	static Logger logger = Logger.getLogger(CInitParam.class.getName());

	public static int appid = 0;

	public static int TaskAtt = 0;
	public static Date TaskTime = null;
	public static long TaskDelay = 0;
	public static long TaskPeriod = 0;
	public static String TaskClass = "";
	public static String XmlCr = "\n";

	public static boolean IsCheckCode = false;

	public static String LoginSql = "";

	public static String UserId = "";
	public static String LoginCode = "";
	public static String UserCode = "";
	public static String PassWord = "";
	public static String[] GlobalObject = null;

	public static int online = 1000;

	public static String erpServerUrl = "";
	

	public static void InitParam(ServletContext context) {
		// 设置初始化参数
		appPath = context.getRealPath("");

		connStr = context.getInitParameter("connectstr");

		bprintTrack = context.getInitParameter("printtrack").equals("yes");
		rlogins = context.getInitParameter("rlogins").equals("yes");
		rpageinfo = context.getInitParameter("rpageinfo").equals("yes");
		webAppType = context.getInitParameter("webapptype");
		chartset_s = context.getInitParameter("charset-s");
		chartset_d = context.getInitParameter("charset-d");
		databaseType = Integer.parseInt(context.getInitParameter("databasetype"));
		vroot = context.getInitParameter("vroot");
		dlfpath = context.getInitParameter("dlfpath");
		filepath1 = context.getInitParameter("filepath1");
		filepath2 = context.getInitParameter("filepath2");
		recordStep = context.getInitParameter("recordstep").equals("yes");
		safeField = context.getInitParameter("safefield");
		safeFieldValue = context.getInitParameter("safefieldvalue");
		sqlCache = context.getInitParameter("salcache").equals("yes");

		appid = Integer.parseInt(context.getInitParameter("appid"), 16);

		TaskAtt = Integer.parseInt(context.getInitParameter("taskatt"));
		if (TaskAtt > -1) {
			TaskDelay = Long.parseLong(context.getInitParameter("taskdelay"));
			TaskPeriod = Long.parseLong(context.getInitParameter("taskperiod"));
			if (!context.getInitParameter("tasktime").equals(""))
				TaskTime = java.sql.Date.valueOf(context.getInitParameter("tasktime"));
			TaskClass = context.getInitParameter("taskclass");
		}

		XmlCr = context.getInitParameter("xmlcr");
		XmlCr = (XmlCr.equals("yes")) ? "\n" : "";
		
		IsCheckCode = context.getInitParameter("checkcode").equals("yes");

		LoginSql = context.getInitParameter("loginsql");
		
		GlobalObject = context.getInitParameter("globalobject").split(",");
		
		LoginCode = context.getInitParameter("logincode");
		PassWord = context.getInitParameter("password");
		UserCode = context.getInitParameter("usercode");
		UserId = context.getInitParameter("userid");

		online = Integer.parseInt(context.getInitParameter("online"));

		erpServerUrl = context.getInitParameter("erpserverurl");
	}

	public static void RunTimer(Timer timer) {
		if (TaskAtt > -1 && timer != null) {
			try {
				if ((TaskAtt == 0 || TaskAtt == 1 || TaskAtt == 4) && TaskTime == null) {
					throw new Exception("请设置参数TaskTime");
				}
				switch (TaskAtt) {
				case 0:
					timer.schedule(new CTask(), TaskTime, TaskPeriod);
					break;
				case 1:
					timer.schedule(new CTask(), TaskTime);
					break;
				case 2:
					timer.schedule(new CTask(), TaskDelay);
					break;
				case 3:
					timer.schedule(new CTask(), TaskDelay, TaskPeriod);
					break;
				case 4:
					timer.scheduleAtFixedRate(new CTask(), TaskTime, TaskPeriod);
					break;
				case 5:
					timer.scheduleAtFixedRate(new CTask(), TaskDelay, TaskPeriod);
					break;
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			System.out.println("定时任务启动....");
		}
	}


	public static String realFilePath1() {
		return appPath + filepath1;
	}

	public static Boolean isDebug() {
		String debug = CAppListener.getParam("debug");
		return (debug != null && debug.equals("yes"));
	}
}
