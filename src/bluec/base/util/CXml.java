package bluec.base.util;
 
/**
 * <p>Title: BlueCJavaBean库</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: BlueE Soft</p>
 *
 * @author not attributable
 * @version 1.0
 */
//import java.sql.*;

import javax.servlet.http.*;

//import java.util.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.servlet.ServletInputStream;

public class CXml {
  private String[] params=null;
  public CXml() {
  }
  public String[] analyseXml(HttpServletRequest request){
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      ServletInputStream in = request.getInputStream();
      Document doc = builder.parse(in); //建立文档
      Node node = doc.getFirstChild().getFirstChild().getNextSibling();

      node = node.getFirstChild().getNextSibling();
      int sqltype = 1; //Integer.parseInt(node.getFirstChild().getNodeValue());

      node = node.getNextSibling().getNextSibling();
      String sqls = node.getFirstChild().getNodeValue();

      node = node.getNextSibling().getNextSibling();

      NamedNodeMap map = node.getAttributes();

      int pcount = map.getLength(); //参数个数
      params = null;
      if(pcount>0){
        params = new String[pcount];
        for (int i = 0; i < pcount; i++) {
          params[i] = map.item(i).getNodeValue();
          if (params[i] == null) params[i] = "";
        }
      }
     }catch (Exception pcException) {
      System.out.println(pcException.getMessage());
     }
     finally {
     }
     return params;
  }
}
