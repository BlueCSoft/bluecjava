package bluec.eleme;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.Base64;

public class SignatureUtil {
	@SuppressWarnings("rawtypes")
	public static String generateSignature(String appKey, String secret, long timestamp, String action, 
			String token, Map<String, Object> parameters) {
        @SuppressWarnings("unchecked")
		final Map<String, Object> sorted = new TreeMap();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        StringBuffer string = new StringBuffer();
        
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            string.append(entry.getKey()).append("=").append("\"").append(entry.getValue().toString()).append("\"");
        }
        String splice = String.format("%s%s%s%s", action, token, string, secret);
        System.out.println(splice + "\n\n");
        String calculatedSignature = md5(splice);
        return calculatedSignature.toUpperCase();
    }

    public static String md5(String str) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));
        } catch (Exception e) {
        }

        byte byteData[] = md.digest();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < byteData.length; i++)
            buffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

        return buffer.toString();
    }
    
    public static String Base64Encode(String str){
    	try
    	{
    	    return Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
    	}
    	catch (Exception e) {
    		return "";
        }
    }
}
