package bluec.base;
  
import javax.imageio.ImageIO;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.image.*;
import java.io.OutputStream;
import javax.servlet.http.*;
/**
 * <p>Title: BlueEJavaBean库</p>
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
public class CImage {
  static Logger  logger = Logger.getLogger(CImage.class.getName());

  public CImage() {
  }
 
  public void CreateImage(String checkCode,
                           HttpSession session,
                           HttpServletResponse response){
   //在内存中创建图象
   int width=56, height=18;
   checkCode = Double.toString(13507*Math.random());
   BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
   //获取图形上下文
   Graphics g = image.getGraphics();
   //设定背景色
   g.setColor(Color.white);
   g.fillRect(0, 0, width, height);
   //画边框
   g.setColor(Color.gray);
   g.drawRect(0,0,width-1,height-1);
   //取随机产生的认证码(4位数字)
   checkCode = checkCode.substring(0,checkCode.indexOf("."));
   switch(checkCode.length()){
    case 1: checkCode = "000"+checkCode; break;
    case 2: checkCode = "00"+checkCode; break;
    case 3: checkCode = "0"+checkCode; break;
    default: checkCode = checkCode.substring(0,4);
   }
   session.setAttribute("checkCode",checkCode);
   /*
   //随机产生200个干扰点，使图象中的认证码不易被其它程序探测到
   Random random = new Random();
   for(int i=0;i<200;i++){
    int x = random.nextInt(width-2)+1;
    int y = random.nextInt(height-2)+1;

    int z = random.nextInt(7);
    switch(z){
      case 0:g.setColor(Color.blue);break;
      case 1:g.setColor(Color.cyan);break;
      case 2:g.setColor(Color.green);break;
      case 3:g.setColor(Color.magenta);break;
      case 4:g.setColor(Color.orange);break;
      case 5:g.setColor(Color.red);break;
      case 6:g.setColor(Color.yellow);
    }
    g.drawLine(x,y,x,y);
   } */
   //将认证码显示到图象中
   //g.setFont(new Font("",Font.BOLD,16));]

   g.setColor(Color.lightGray);
   g.drawString(checkCode,6,14);
   g.setColor(Color.black);
   g.drawString(checkCode,5,13);
   // 图象生效
   g.dispose();
   // 输出图象到页面
   
   try{
	   OutputStream os = response.getOutputStream();
	   ImageIO.write(image, "JPEG", os);
	   os.flush();
	   os.close();
	   os = null;
	   response.flushBuffer();
   }
   catch(Exception e){

   }
  }

}
