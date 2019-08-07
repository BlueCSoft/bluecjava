package bluec.keg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import bluec.base.CJson;
import bluec.base.CUtil;
import bluec.base.CHttpService;
import bluec.base.CWeiXin;

public class CPhauto extends CJson {
	public Boolean isSuccess = false;
	/**
	 * 数据初始化，返回0-成功初始化,1-未绑定帐号
	 */
	public int WxInit(HttpServletRequest request, HttpServletResponse response) {
		int result = -9;
		initRequest(request, response);

		if (__Request.getParameter("iscancel") != null) // 解除绑定
		{
			if (__Session.getAttribute("gUserStatus") != null) {
				executeMsSqlProc("WxBoundCancel", new String[] { __Session.getAttribute("gOpenId").toString(), "" });
				__Session.setAttribute("gUserId", null);
				__Session.setAttribute("gUserStatus", -2); // 未注册或绑定信息
				__Session.setAttribute("gUserCode", null);
				__Session.setAttribute("gUserName", null);
				__Session.setAttribute("gKhId", null);
				__Session.setAttribute("gKhCode", null);
			}
			result = -2;
		} else {
			if (__Session.getAttribute("gUserStatus") == null
					|| Integer.parseInt(__Session.getAttribute("gUserStatus").toString()) < 2) {
				String openid = __Session.getAttribute("gOpenId").toString();
				try {
					executeQuery3("select * from v_user where OpenId<>'' and OpenId='%s'", new String[] { openid });
					if (next()) {
						__Session.setAttribute("gUserId", getString("user_id"));
						__Session.setAttribute("gUserCode", getString("user_code"));
						__Session.setAttribute("gUserName", getString("user_name"));
						__Session.setAttribute("gUserStatus", getString("userstatus"));
						result = getInt("userstatus");
						__Session.setAttribute("gKhId", getInt("KH_ID"));
						executeQuery3("select kh_code from BlueCQp3..yx_kh where kh_id=%s",
								new String[] { getString("KH_ID") });
						if (next())
							__Session.setAttribute("gKhCode", getString("KH_CODE"));
						else
							__Session.setAttribute("gKhCode", "");

					} else {
						__Session.setAttribute("gUserId", null);
						__Session.setAttribute("gUserStatus", -2); // 未注册或绑定信息
						__Session.setAttribute("gUserCode", null);
						__Session.setAttribute("gUserName", null);
						__Session.setAttribute("gKhId", null);
						__Session.setAttribute("gKhCode", null);
						result = -2;
					}

				} catch (Exception ex) {

				} finally {
					closeConn();
				}
			} else {
				result = Integer.parseInt(__Session.getAttribute("gUserStatus").toString());
			}
		}

		return result;
	}

	/**
	 * 推送微信信息
	 */
	public void SendWxMsg() {
		new CWeiXin().BatchSendText("3000003", "select IDKEY,OPENID,MSG  from WX_MSGSAVE where STATUS between 0 and 4",
				"update WX_MSGSAVE set Status=9 where idkey=%s",
				"update WX_MSGSAVE set Status=Status+1 where idkey=%s");
	}

	/**
	 * VIN查询
	 */
	public String getInfoByVin(String vin) {
		try {
			JSONObject o = new JSONObject();
			o.put("username", "danvy118");
			o.put("password", "fca4e6d3aefbd3e416873f89cf1d84ea45efb679");
			JSONObject d = new JSONObject();
			d.put("vinCode", vin);
			o.put("data", d);
			_errorInf = CHttpService.sendPostJson("http://www.easyepc123.com/api/111002", o.toString());
		} catch (Exception ex) {

		}
		return _errorInf;
	}

	/**
     * 车架号查询
     */
    public String queryInfoByCjh(String cjh){
    	try{
    		int cxnum = 0;
    		String cjh0 = cjh.substring(0,8)+cjh.substring(9,12);
    		isSuccess = false;
    		if(executeMsSqlProc("QueryInfoByCjh", new String[]{cjh,"","0",""})==0){
    			cxnum = Integer.parseInt(__procReturn[0]);
    			if(cxnum <= 0){
    				JSONObject o = new JSONObject();
    	    		o.put("username", "danvy118");
    	    		o.put("password", "fca4e6d3aefbd3e416873f89cf1d84ea45efb679");
    	    		JSONObject d = new JSONObject();
    	    		d.put("vinCode", cjh);
    	    		o.put("data", d);
    	    		P(o.toString());
    	        	_errorInf = CHttpService.sendPostJson("http://www.easyepc123.com/api/111002",
    	    				o.toString());	
    	        	//P(_errorInf);
    	        	o = new JSONObject(_errorInf);
    	        	if(o.getString("code").equals("000000")){
    	        		String rid = CUtil.GenerateNonceStr();
    	        		
    	        		if(saveJsonArrayToDb("BlueCQp3Bak..BASE_CX_TEMP",
    	        				o.getJSONObject("result").getJSONArray("vehicleList"),
    	        				"XH",new String[]{"RID"},new String[]{rid})){
    	        			if(executeMsSqlProc("QueryInfoByCjh", new String[]{cjh,rid,"0",""})==0){
    	        			    _errorInf = sucessMsgToXml(cjh0);
    	        			    isSuccess = true;
    	        			}else{
    	        				_errorInf = errorMsgToXml(_errorInf);	
    	        			}
    	        		}else{
    	        			_errorInf = errorMsgToXml(_errorInf);	
    	        		}
    	        	}else{
    	        		if(o.getString("code").equals("0042") && cxnum == 0){  //保存条码
    	        			String sql = String.format("insert into BlueCQp3Bak..BASE_CX_MXBINP(SNAME,"
								+ "CJH,CJH0,CJH1,CJH2,CJH3,CJH4,CJH5,CJH6,CJH7,CJH8,CJH9,CJH10,CJH11,CJH12,CJH13,CJH14,CJH15,CJH16,CJH17) "
    	        				+ "values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
								"未知车型",cjh,cjh0,cjh.substring(0,1),cjh.substring(1,2),cjh.substring(2,3),cjh.substring(3,4),
								cjh.substring(4,5),cjh.substring(5,6),cjh.substring(6,7),cjh.substring(7,8),cjh.substring(8,9),
								cjh.substring(9,10),cjh.substring(10,11),cjh.substring(11,12),cjh.substring(12,13),
								cjh.substring(13,14),cjh.substring(14,15),cjh.substring(15,16),cjh.substring(16,17));
    	        			executeUpdate(sql);
    	        			_errorInf = errorMsgToXml(o.getString("message"));
    	        		}else{
    	        		    _errorInf = errorMsgToXml(o.getString("message"));
    	        		}
    	        	}
    			}else{
    				_errorInf = sucessMsgToXml(cjh0);
    				isSuccess = true;
    			}
    		}else{
    			_errorInf = errorMsgToXml(_errorInf);
    		}
    		
    	}catch(Exception ex){
    		_errorInf = errorMsgToXml(ex.getMessage());
    	}
    	return _errorInf;
    }
    
    /**
     * 车型结构查询
     */
    public String queryInfoByvehicleId(String vehicleId){
    	try{
 
    		
    	}catch(Exception ex){
    		_errorInf = errorMsgToXml(ex.getMessage());
    	}
    	return _errorInf;
    }
}
