package bluec.base.pay;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.github.wxpay.sdk.WXPayUtil;

import bluec.base.CHttpService;
import bluec.base.CUtil;
import bluec.base.WxApp.WxPayData;

public class CGzhPay extends CBasePay {
	public String JsApiParameters;

    public CGzhPay(String shopid)
    {
    	super(shopid);
        gPayatt = "GZHPAY";
    }

    public CGzhPay(HttpServletRequest request, HttpServletResponse response)
    {
    	super(request,response);
    }
    
    private Boolean WxParamCheck()
    {
        Boolean Result = true;
        if (gAppId.equals(""))
        {
            _errorInf = "ȱ��AppId"; 
            Result = false;
        }
        else
            if (gMchid.equals(""))
            {
                _errorInf = "ȱ���̻���";
                Result = false;
            }
            else
                if (gKey.equals(""))
                {
                    _errorInf = "ȱ���̻�Key";
                    Result = false;
                }
                else
                    if (gCMchid.equals("") && gPlatform == 2)
                    {
                        _errorInf = "ȱ�����̻���";
                        Result = false;
                    }
        return Result;
    }
    
    /**
     * ΢��ͳһ�µ�����������0�ɹ�
     */
    protected int WxGetUnifiedOrderResult(String openid, int fPayFee,
    		String sBody, String sAttach, String sTradeNo, String sGoodsTag, String trade_type)
    {
        //ͳһ�µ�
        int PayFee = (int)(fPayFee);
        if (!WxParamCheck()) return -1;

        Map<String, String> data = new HashMap<String, String>();

        data.put("device_info", "WEB");  //�豸��	  
   
        data.put("body", sBody);
        data.put("attach", sAttach);
        data.put("out_trade_no", sTradeNo);
        data.put("total_fee", PayFee+"");

        data.put("spbill_create_ip", CUtil.getLocalIp());//�ն�ip	  	    
        data.put("time_start", CUtil.getTimeT());
        data.put("time_expire", CUtil.getTimeT(10));

        data.put("goods_tag", sGoodsTag);
        data.put("notify_url", gHomeUrl + "/wxmain/wxpaynotify.jsp");   //�첽֪ͨurl

        data.put("trade_type", "JSAPI");

        if (gPlatform == 1)  //��֧��
        {  
            data.put("openid", openid);
        }
        else                 //����֧�� 
        {
            data.put("sub_appid", gCAppId);    //�ӹ����˺�ID
            data.put("sub_mch_id", gCMchid);   //���̻���
            data.put("sub_openid", openid);
        }

        int result = -1;
        String prepay_id = "";
        try
        {
        	Map<String, String> out = wxpay.unifiedOrder(data);

            if (!out.containsKey("appid") || !out.containsKey("prepay_id") || out.get("prepay_id").equals(""))
            {
                _errorInf = "΢��֧�����󷵻ش���";
                WXPayUtil.getLogger().info("UnifiedOrder response error!");

                if (out.get("return_code").equals("FAIL"))
                    _errorInf = out.get("return_msg");
                else
                {
                    _errorInf = out.get("err_code_des");
                }
            }
            else
            {
                prepay_id = out.get("prepay_id");
                result = 0;
            }

        }
        catch (Exception ex)
        {
            _errorInf = ex.getMessage();
        }

        if (result != 0)
        {
            RecordResult(sTradeNo,
                         "NONE",
                         "NONE",
                         "NONE",
                         "0",
                         "",
                         "",
                         -1,
                         paymothed,
                         _errorInf);
        }
        else
        { 
            WxPayData jsApiParam = new WxPayData();
            String st = CUtil.GenerateTimeStamp();
            String sn = CUtil.GenerateNonceStr();
            jsApiParam.SetValue("appId", gAppId);
            jsApiParam.SetValue("timeStamp", st);
            jsApiParam.SetValue("nonceStr", sn);
            jsApiParam.SetValue("signType","HMAC-SHA256");
            jsApiParam.SetValue("package", "prepay_id=" + prepay_id);
            jsApiParam.SetValue("paySign", jsApiParam.MakeHMACSHA256(gKey));
            JsApiParameters = jsApiParam.ToJson();
        }

        return result;
    }
    
    /*
     * ���ں�ͳһ�µ�
     */

    public int GetUnifiedOrderResult(String body, String subject, String sTradeNo, String sBillId,
    		String BillTypeId, String SysId, String Version,
    		String openid, String sGoodsTag,
    		String shopid, String goodsid, String goodsname,int sPayFee)
    {
        int result = -1;

        if (PayInit(body, subject, sTradeNo, sBillId, BillTypeId, SysId, Version,
                    "WEB", "WEB", shopid, goodsid, goodsname, sPayFee+""))
        {
            switch (gPlatform)
            {
                case 1: result = WxGetUnifiedOrderResult(openid, sPayFee, body, subject, sTradeNo, sGoodsTag, "JSAPI");
                    break;
                case 2: result = WxGetUnifiedOrderResult(openid, sPayFee, body, subject, sTradeNo, sGoodsTag, "JSAPI");
                    break;
                case 3: //��ȡ������֧������ 
                    result = DlbPayUrl(sPayFee+"", "", "", sTradeNo, sGoodsTag);
                    break;
                default:
                    _errorInf = "δ��ͨ";
                    break;
            }
        }
        return result;
    }
    
    public Boolean DlbPayResult()
    {
        Boolean result = false;
        WXPayUtil.getLogger().info("DlbPayResult-Start","");
        try
        { 
            String requestNum = __Request.getParameter("requestNum").toString();
            String orderNum = __Request.getParameter("orderNum").toString();
            String orderAmount = __Request.getParameter("orderAmount").toString();
            String status = __Request.getParameter("status").toString();
            String completeTime = __Request.getParameter("completeTime").toString();
            result = status.equals("SUCCESS");

            WXPayUtil.getLogger().info("DlbPayResult-status:\n" + status);

            if (result)
            {
            	WXPayUtil.getLogger().info("DlbPayResult-requestNum:\n" + requestNum);
                InitParam(requestNum.substring(4, 10));
               
                String[] vparams = { gMchid,
                                 gCMchid,
                                 "NONE",
                                 "NONE",
                                 "N",
                                 requestNum,
                                 "NONE",
                                 orderNum,
                                 completeTime,
                                 "",
                                 Double.parseDouble(orderAmount)*100+"",
                                 "2",
                                 "" };
                switch (requestNum.substring(0, 2))
                {
                    case "DC"://���֧��
                        executeMsSqlProc("WxBookPayOk", vparams);
                        WXPayUtil.getLogger().info("WxBookPayOk\n" + _errorInf);
                        break;
                    case "CZ"://��ֵ֧��
                    	executeMsSqlProc("WxCzPayOk", vparams);
                    	WXPayUtil.getLogger().info("WxCzPayOk\n" + _errorInf);
                        break;
                    case "QT":
                    case "XF"://����֧��
                    	executeMsSqlProc("WxBillPayOk", vparams);
                    	WXPayUtil.getLogger().info("WxBillPayOk\n" + vparams.toString());
                        break;
                }
            }
        }
        catch (Exception ex)
        {
        	WXPayUtil.getLogger().info("DlbPayResult-Error:\n"+ex.getMessage());
        }
        return result;
    }
    
    protected int DlbPayUrl(String payfee, String txcurrcd, String pay_type, String out_trade_no,
    		String goods_name)
         {
             int result = -1;
             //����ʱ��
             String timestamp = CUtil.getTimeFmt("yyMMddHHmmss");

             String data = (IsSelfPay) ? CUtil.ToJson(new String[]{
                                      "customerNum", gCMchid,   //is
                                      "shopNum", gPayShopId,    //is
                                      "machineNum", gPayMchId,  //is
                                      "requestNum" , out_trade_no,   //is
                                      "amount" ,Double.parseDouble(payfee)/100+"", 
                                      "source" ,"API",
                                      "tableNum", "",
                                      "callbackUrl",gHomeUrl+"/wxmain/dlbpaynotify.jsp",
                                      "extraInfo",goods_name}) :
                                  CUtil.ToJson(new String[]{
                                      "agentNum", gMchid, 
                                      "customerNum", gCMchid,   //is
                                      "shopNum", gPayShopId,    //is
                                      "machineNum", gPayMchId,  //is
                                      "requestNum" , out_trade_no,   //is
                                      "amount" ,Double.parseDouble(payfee)/100+"", 
                                      "source" ,"API",
                                      "tableNum", "",
                                      "callbackUrl",gHomeUrl+"/wxmain/dlbpaynotify.jsp",
                                      "extraInfo",goods_name});

             String ac = (IsSelfPay) ? "customer" : "agent";
             String url = "/v1/" + ac + "/order/payurl/create";

             String sign = CUtil.SHA1(new String[] { "secretKey=", gKey,
             "&timestamp=",timestamp,"&path=",url,"&body=",data});

             String[] headvars = { "accessKey", gAppId, "timestamp", timestamp, "token", sign };

             WXPayUtil.getLogger().info("DlbPayUrlCall:\n" + data);
             
             String jsondata = CHttpService.webRequestJsonPost(gPayUrl + url, "", data , headvars);

             WXPayUtil.getLogger().info("DlbPayUrlResult:\n" + jsondata);
             
             try
             {
                 JSONObject reader = new JSONObject(jsondata);
            	 if (reader.has("result"))
                 {
                     String respcd = reader.getString("result");
                     if (respcd.equals("success"))
                     {
                         result = 0;
                         reader = reader.getJSONObject("data");
                         _errorInf = reader.getString("url");
                     }
                     else
                     {
                         String err = reader.getString("errorCode");
                         if (reader.has("errorMsg"))
                             err += reader.getString("errorMsg");
                         else
                             err += "�����ˣ�����֧�����ò���";

                         _errorInf = err;
                     }
                 }
                 else
                 {
                     _errorInf = "֧��ʧ��";
                 }
             }
             catch (Exception ex)
             {
                 _errorInf = ex.getMessage();
             }
             return result;
         }
}
