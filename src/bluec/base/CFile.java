package bluec.base; 

import java.io.*;

public class CFile {
	
	private static String errorMsgToJson(String errmsg){
		return "{\"sub_code\":\"0\",\"sub_msg\":\""+
		CUtil.replaceStr((errmsg==null)?"���쳣":errmsg,"'","\"")+"\",\"code\":\"1\",\"msg\":\"�ɹ�\"}";
	}
	
	public static boolean saveToFile(String fileName, String fileContent) {
		boolean result = false;
		try {
			//CInitParam.f
			FileWriter fw = new FileWriter(fileName);
			fw.write(fileContent, 0, fileContent.length());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		return result;
	}
	
	public static boolean appendToFile(String fileName, String fileContent) {
		boolean result = false;
		try {
			//CInitParam.f
			FileWriter fw = new FileWriter(fileName,true);
			fw.write(fileContent, 0, fileContent.length());
			fw.write("\n",0,1);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return result;
	}
	
	public static String loadFromFile(String fileName) {
		String result = "";
		try {
			StringBuffer buf = new StringBuffer();
			BufferedReader br = new BufferedReader(new FileReader(fileName));  
			String data = br.readLine();//һ�ζ���һ�У�ֱ������nullΪ�ļ�����  
			while( data!=null){  
				buf.append(data);
			    data = br.readLine(); //���Ŷ���һ��  
			} 
			result = buf.toString();
			br.close();
		} catch (Exception e) {
			result = errorMsgToJson("loadFromFile:"+e.getMessage());
		} finally {
		}

		return result;
	}
}
