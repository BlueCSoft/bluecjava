package bluec.base.WxApp;

import java.util.logging.Logger;

import bluec.base.CHttpService;
import bluec.base.CQuery;

public class CWxMenu extends CQuery {
	static Logger logger = Logger.getLogger(CWxMenu.class.getName());

	private WxReqObj wxreqobj = null;

	public CWxMenu(WxReqObj pwxreqobj) {
		wxreqobj = pwxreqobj;
	}

	public String CreateUserMenu() {
		String merchantid = wxreqobj.gMerchantId;
		String gHomeUrl = wxreqobj.gHomeUrl;

		String sResult = "";
		try {
			executeQuery3("select Url from wxMerchant where MerchantId='%s'", merchantid);

			next();

			String url = getString("Url");

			if (url.equals(""))
				url = gHomeUrl + "/wxmain/wxabout.jsp?merid=" + merchantid;

			int nCount = 0;
			boolean bNext = false;

			executeQuery3("select id,fun_id,fun_name,target,pagename,haschild,csfun_id,cid,b.url "
					+ "from wx_menu a,wx_menutype b " + "where a.merchantid='%s' and b.sno=a.csfun_id "
					+ "order by a.fun_id", merchantid);
			bNext = next();

			if (!bNext) {
				executeQuery("select id,fun_id,fun_name,target,pagename,haschild,csfun_id,cid from sys_funs "
						+ "where fun_id like '99%' and level>1 order by fun_id");
				bNext = next();
			}
			
			StringBuilder buf = new StringBuilder();
			
			buf.append("{");
			buf.append("\"button\":[");

			while (bNext) {
				String fid = getString("id");
				String fun_id = getString("fun_id");
				String fun_name = getString("fun_name");
				String target = getString("target");
				String pagename = getNote("pagename");
				String menulx = getString("csfun_id");
				String burl = getNote("url");

				switch (menulx) {
				case "VW_PAGE":
					pagename = gHomeUrl + "/mdir/" + merchantid + "/" + pagename;
					int n = pagename.lastIndexOf("?");

					if (n < 0)
						pagename = pagename + "?merchantid=" + merchantid;
					else if (n == pagename.length() - 1)
						pagename = pagename + "merchantid=" + merchantid;
					else
						pagename = pagename + "&merchantid=" + merchantid;
					break;
				case "VW_URL":
					if (pagename.equals(""))
						pagename = url;
					break;
				case "VR_START":
					pagename = gHomeUrl + burl;
					break;
				case "VR_IMQUERY":
					pagename = gHomeUrl + burl;
					break;
				case "VR_VIP":
					pagename = gHomeUrl + burl;
					break;
				default:
					pagename = menulx;
					if (pagename.equals("CW_RETURN"))
						pagename = pagename + "#" + getString("ID");
					else if (pagename.substring(0, 6).equals("VR_DEF"))
						pagename = gHomeUrl + burl;
					break;
				}

				pagename = pagename.replaceAll("\\{merchantid\\}", merchantid);

				int haschild = getInt("haschild");

				switch (target) {
				case "click":
					if (nCount > 0)
						buf.append(",");
					buf.append("{");
					buf.append("\"type\":\"click\",");
					buf.append("\"name\":\"" + fun_name + "\",");
					buf.append("\"key\":\"" + pagename + "\"");
					buf.append("}");
					break;
				case "view":
					if (nCount > 0)
						buf.append(",");
					buf.append("{");
					buf.append("\"type\":\"view\",");
					buf.append("\"name\":\"" + fun_name + "\",");
					buf.append("\"url\":\"" + pagename + "\"");
					buf.append("}");
					break;
				case "menu":
					if (nCount > 0)
						buf.append(",");
					buf.append("{");
					buf.append("\"name\":\"" + fun_name + "\",");
					buf.append("\"sub_button\":[");

					for (int i = 0; i < haschild; i++) {
						bNext = next();

						if (bNext) {
							fun_id = getString("fun_id");
							fun_name = getString("fun_name");
							target = getString("target");
							pagename = getNote("pagename");
							menulx = getString("csfun_id");
							burl = getNote("url");

							switch (menulx) {
							case "VW_PAGE":
								pagename = gHomeUrl + "/mdir/" + merchantid + "/" + pagename;
								int n = pagename.lastIndexOf("?");

								if (n < 0)
									pagename = pagename + "?merchantid=" + merchantid;
								else if (n == pagename.length() - 1)
									pagename = pagename + "merchantid=" + merchantid;
								else
									pagename = pagename + "&merchantid=" + merchantid;
								break;
							case "VW_URL":
								if (pagename.equals(""))
									pagename = url;
								break;
							case "VR_START":
								pagename = gHomeUrl + burl;
								break;
							case "VR_IMQUERY":
								pagename = gHomeUrl + burl;
								break;
							case "VR_VIP":
								pagename = gHomeUrl + burl;
								break;
							default:
								pagename = menulx;
								if (pagename.equals("CW_RETURN"))
									pagename = pagename + "#" + getString("ID");
								else if (pagename.substring(0, 6).equals("VR_DEF"))
									pagename = gHomeUrl + burl;
								break;
							}

							pagename = pagename.replaceAll("\\{merchantid\\}", merchantid);

							switch (target) {
							case "click":
								if (i > 0)
									buf.append(",");
								buf.append("{");
								buf.append("\"type\":\"click\",");
								buf.append("\"name\":\"" + fun_name + "\",");
								buf.append("\"key\":\"" + pagename + "\"");
								buf.append("}");
								break;
							case "view":
								if (i > 0)
									buf.append(",");
								buf.append("{");
								buf.append("\"type\":\"view\",");
								buf.append("\"name\":\"" + fun_name + "\",");
								buf.append("\"url\":\"" + pagename + "\"");
								buf.append("}");
								break;
							}
						}
					}

					buf.append("]");
					buf.append("}");
					break;

				}
				nCount++;
				bNext = bNext && next();
			}

			buf.append("]");
			buf.append("}");

			wxreqobj.gMsg = buf.toString();
			P("MenuJson:\n" + buf.toString());
			sResult = CHttpService.webRequestJsonPost("https://api.weixin.qq.com/cgi-bin/menu/create",
					  "access_token=" + wxreqobj.gAccessToken, buf.toString());
		} catch (Exception ex) {
			sResult = ex.getMessage();
		}
		logger.info("CreateResult:\n" + sResult);
		return sResult;
	}
	
	public String QueryUserMenu()
    {
        String sResult = CHttpService.webRequestPost("https://api.weixin.qq.com/cgi-bin/menu/get",
                                                     "access_token=" + wxreqobj.gAccessToken, "");
        return sResult;
    }

    public String DeleteUserMenu()
    {
        String sResult = CHttpService.webRequestPost("https://api.weixin.qq.com/cgi-bin/menu/delete",
                                              "access_token=" + wxreqobj.gAccessToken, "");
        return sResult;
    }
}
