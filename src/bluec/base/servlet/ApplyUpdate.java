package bluec.base.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import bluec.base.CTable;
import java.util.logging.Logger;

public class ApplyUpdate
    extends HttpServlet {
  //private static final String CONTENT_TYPE = "text/html; charset=GB2312";
  static Logger  logger = Logger.getLogger(ApplyUpdate.class.getName());
  private static final long serialVersionUID = 1L; //这个是缺省的
  
  public void ProcessServlet(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {
    String errorInf = "";
    response.setContentType("CONTENT_TYPE");
    int nResult = -1;
    try {
     CTable table = new CTable();
     nResult = table.applyUpdates(request);
     errorInf = table.getLastError();
    }catch(Exception ex){
     System.err.println(ex.getMessage());
    } finally {
    }
    PrintWriter out = response.getWriter();
    if(nResult==0)
     out.print("<xml><ds><d>ok</d></ds></xml>");
    else
     out.print("<xml><ds><d>"+errorInf+"</d></ds></xml>");
    out.close();
  }
  //Initialize global variables
  public void init() throws ServletException {
  }

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    ProcessServlet(request,response);
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response)
                  throws ServletException, IOException {
    ProcessServlet(request, response);
  }

  //Clean up resources
  public void destroy() {
  }
}
