package bluec.base;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class CFileUpLoad extends CQuery {

	private HashMap<String, String> vParams = new HashMap<String, String>();
	private String newFileName = "";
	private String oldFileName = "";
	private long newFileSize = 0;
	private String returnXml = "";

	public String getParameter(String paramentName) {
		return vParams.containsKey(paramentName) ? vParams.get(paramentName).toString()
				: (__Request.getParameter(paramentName) != null) ? __Request.getParameter(paramentName).toString() : "";
	}

	public String getNewFileName() {
		return newFileName;
	}

	public String getReturnXml() {
		return returnXml;
	}

	public class CMIMEType {
		HashMap<String, String> MimeType = new HashMap<String, String>();

		public CMIMEType() {
			MimeType.put("ez", "application/andrew-inset");
			MimeType.put("hqx", "application/mac-binhex40");
			MimeType.put("cpt", "application/mac-compactpro");
			MimeType.put("doc", "application/msword");
			MimeType.put("bin", "application/octet-stream");
			MimeType.put("dms", "application/octet-stream");
			MimeType.put("lha", "application/octet-stream");
			MimeType.put("lzh", "application/octet-stream");
			MimeType.put("exe", "application/octet-stream");
			MimeType.put("class", "application/octet-stream");
			MimeType.put("so", "application/octet-stream");
			MimeType.put("dll", "application/octet-stream");
			MimeType.put("oda", "application/oda");
			MimeType.put("pdf", "application/pdf");
			MimeType.put("ai", "application/postscript");
			MimeType.put("eps", "application/postscript");
			MimeType.put("ps", "application/postscript");
			MimeType.put("smi", "application/smil");
			MimeType.put("smil", "application/smil");
			MimeType.put("mif", "application/vnd.mif");
			MimeType.put("xls", "application/vnd.ms-excel");
			MimeType.put("ppt", "application/vnd.ms-powerpoint");
			MimeType.put("wbxml", "application/vnd.wap.wbxml");
			MimeType.put("wmlc", "application/vnd.wap.wmlc");
			MimeType.put("wmlsc", "application/vnd.wap.wmlscriptc");
			MimeType.put("bcpio", "application/x-bcpio");
			MimeType.put("vcd", "application/x-cdlink");
			MimeType.put("pgn", "application/x-chess-pgn");
			MimeType.put("cpio", "application/x-cpio");
			MimeType.put("csh", "application/x-csh");
			MimeType.put("dcr", "application/x-director");
			MimeType.put("dir", "application/x-director");
			MimeType.put("dxr", "application/x-director");
			MimeType.put("dvi", "application/x-dvi");
			MimeType.put("spl", "application/x-futuresplash");
			MimeType.put("gtar", "application/x-gtar");
			MimeType.put("hdf", "application/x-hdf");
			MimeType.put("js", "application/x-javascript");
			MimeType.put("skp", "application/x-koan");
			MimeType.put("skd", "application/x-koan");
			MimeType.put("skt", "application/x-koan");
			MimeType.put("skm", "application/x-koan");
			MimeType.put("latex", "application/x-latex");
			MimeType.put("nc", "application/x-netcdf");
			MimeType.put("cdf", "application/x-netcdf");
			MimeType.put("sh", "application/x-sh");
			MimeType.put("shar", "application/x-shar");
			MimeType.put("swf", "application/x-shockwave-flash");
			MimeType.put("sit", "application/x-stuffit");
			MimeType.put("sv4cpio", "application/x-sv4cpio");
			MimeType.put("sv4crc", "application/x-sv4crc");
			MimeType.put("tar", "application/x-tar");
			MimeType.put("tcl", "application/x-tcl");
			MimeType.put("tex", "application/x-tex");
			MimeType.put("texinfo", "application/x-texinfo");
			MimeType.put("texi", "application/x-texinfo");
			MimeType.put("t", "application/x-troff");
			MimeType.put("tr", "application/x-troff");
			MimeType.put("roff", "application/x-troff");
			MimeType.put("man", "application/x-troff-man");
			MimeType.put("me", "application/x-troff-me");
			MimeType.put("ms", "application/x-troff-ms");
			MimeType.put("ustar", "application/x-ustar");
			MimeType.put("src", "application/x-wais-source");
			MimeType.put("xhtml", "application/xhtml+xml");
			MimeType.put("xht", "application/xhtml+xml");
			MimeType.put("zip", "application/zip");
			MimeType.put("au", "audio/basic");
			MimeType.put("snd", "audio/basic");
			MimeType.put("mid", "audio/midi");
			MimeType.put("midi", "audio/midi");
			MimeType.put("kar", "audio/midi");
			MimeType.put("mpga", "audio/mpeg");
			MimeType.put("mp2", "audio/mpeg");
			MimeType.put("mp3", "audio/mpeg");
			MimeType.put("aif", "audio/x-aiff");
			MimeType.put("aiff", "audio/x-aiff");
			MimeType.put("aifc", "audio/x-aiff");
			MimeType.put("m3u", "audio/x-mpegurl");
			MimeType.put("ram", "audio/x-pn-realaudio");
			MimeType.put("rm", "audio/x-pn-realaudio");
			MimeType.put("rpm", "audio/x-pn-realaudio-plugin");
			MimeType.put("ra", "audio/x-realaudio");
			MimeType.put("wav", "audio/x-wav");
			MimeType.put("pdb", "chemical/x-pdb");
			MimeType.put("xyz", "chemical/x-xyz");
			MimeType.put("bmp", "image/bmp");
			MimeType.put("gif", "image/gif");
			MimeType.put("ief", "image/ief");
			MimeType.put("jpeg", "image/jpeg");
			MimeType.put("jpg", "image/jpeg");
			MimeType.put("jpe", "image/jpeg");
			MimeType.put("png", "image/png");
			MimeType.put("tiff", "image/tiff");
			MimeType.put("tif", "image/tiff");
			MimeType.put("djvu", "image/vnd.djvu");

			MimeType.put("djv", "image/vnd.djvu");
			MimeType.put("wbmp", "image/vnd.wap.wbmp");
			MimeType.put("ras", "image/x-cmu-raster");
			MimeType.put("pnm", "image/x-portable-anymap");
			MimeType.put("pbm", "image/x-portable-bitmap");
			MimeType.put("pgm", "image/x-portable-graymap");
			MimeType.put("ppm", "image/x-portable-pixmap");
			MimeType.put("rgb", "image/x-rgb");
			MimeType.put("xbm", "image/x-xbitmap");
			MimeType.put("xpm", "image/x-xpixmap");
			MimeType.put("xwd", "image/x-xwindowdump");
			MimeType.put("igs", "model/iges");
			MimeType.put("iges", "model/iges");
			MimeType.put("msh", "model/mesh");
			MimeType.put("mesh", "model/mesh");
			MimeType.put("silo", "model/mesh");
			MimeType.put("wrl", "model/vrml");
			MimeType.put("vrml", "model/vrml");
			MimeType.put("css", "text/css");
			MimeType.put("html", "text/html");
			MimeType.put("htm", "text/html");
			MimeType.put("asc", "text/plain");
			MimeType.put("txt", "text/plain");
			MimeType.put("rtx", "text/richtext");
			MimeType.put("rtf", "text/rtf");
			MimeType.put("sgml", "text/sgml");
			MimeType.put("sgm", "text/sgml");
			MimeType.put("tsv", "text/tab-separated-values");
			MimeType.put("wml", "text/vnd.wap.wml");
			MimeType.put("wmls", "text/vnd.wap.wmlscript");
			MimeType.put("etx", "text/x-setext");
			MimeType.put("xsl", "text/xml");
			MimeType.put("xml", "text/xml");
			MimeType.put("mpeg", "video/mpeg");
			MimeType.put("mpg", "video/mpeg");
			MimeType.put("mpe", "video/mpeg");
			MimeType.put("qt", "video/quicktime");
			MimeType.put("mov", "video/quicktime");
			MimeType.put("mxu", "video/vnd.mpegurl");
			MimeType.put("avi", "video/x-msvideo");
			MimeType.put("movie", "video/x-sgi-movie");
			MimeType.put("ice", "x-conference/x-cooltalk");
		}

		public String FileMimeType(String FileExt) {
			if (FileExt.length() > 0 && FileExt.indexOf(".") == 0)
				FileExt = FileExt.substring(1);
			FileExt = FileExt.toLowerCase();
			if (MimeType.containsKey(FileExt))
				return MimeType.get(FileExt).toString();
			else
				return "application/octet-stream";
		}
	}

	public void makeDir(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public int processParams(HttpServletRequest request) {
		int result = -1;
		__Request = request;
		String tmpDir = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(new File(tmpDir));
		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setHeaderEncoding("UTF-8");

		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				List<FileItem> list = upload.parseRequest(request);
				for (FileItem item : list) {
					if (item.isFormField()) {
						vParams.put(item.getFieldName(), item.getString("UTF-8"));
					}
				}
				result = 0;
			} catch (Exception ex) {
				_errorInf = ex.getMessage();
				newFileName = _errorInf;
			}
		}
		return result;
	}

	public int UpLoad(HttpServletRequest request, Boolean useDefPath) {
		int result = -1;
		String tmpDir = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");

		String sdir = "";
		String fileNameAfx = "";
		String merchantid = "";
		String attach = "";

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(new File(tmpDir));
		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setHeaderEncoding("UTF-8");
		upload.setFileSizeMax(3 * 1024 * 1024);

		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				List<FileItem> list = upload.parseRequest(request);
				for (FileItem item : list) {
					if (item.isFormField()) {
						vParams.put(item.getFieldName(), item.getString("UTF-8"));
					}
				}

				if (vParams.containsKey("oldfilename")) {
					File file = new File(request.getSession().getServletContext()
							.getRealPath(vParams.get("oldfilename").toString()));
					if (file.exists())
						file.delete();
				}

				fileNameAfx = vParams.get("fileafx").toString();
				merchantid = vParams.get("merid").toString();
				attach = vParams.get("attach").toString();
				int type = Integer.parseInt(vParams.get("type").toString());
				switch (type) {
				case -1:
					sdir = "/mdir/" + merchantid + "/";
					break;
				case 0:// 商家图片
					sdir = "/mdir/" + merchantid + "/pics/";
					break;
				case 1:// 门店图片
					sdir = "/mdir/" + merchantid + "/pics/";
					break;
				}

				if (useDefPath)
					sdir = CAppListener.getParam("filepath1") + sdir;

				String parentDir = request.getSession().getServletContext().getRealPath(sdir);

				makeDir(parentDir);

				for (FileItem item : list) {
					if (!item.isFormField()) {

						InputStream in = item.getInputStream();
						String fileName = item.getName();
						int k = fileName.lastIndexOf(".");

						if (vParams.containsKey("auto")) {
							int fileid = executeMsSqlProc("getSequence", new String[] { "FILLID" });

							if (k > -1)
								fileName = fileNameAfx + fileid + fileName.substring(k);
							else
								fileName = fileNameAfx + fileid;
						}
						newFileName = fileName;
						FileOutputStream fos = new FileOutputStream(parentDir + "/" + fileName);
						int len = 0;
						byte[] b = new byte[1024];
						while ((len = in.read(b)) != -1) {
							fos.write(b, 0, len);
						}
						fos.close();
					}
				}
				result = 0;
			} catch (Exception ex) {
				_errorInf = ex.getMessage();
				newFileName = _errorInf;
			}
		}

		returnXml = "<xml><ds><data status=\"" + result + "\" fileafx=\"" + fileNameAfx + "\" addpath=\"" + sdir
				+ "\" filename=\"" + newFileName + "\" attach=\"" + attach + "\"></data></ds></xml>";
		// System.out.println(returnXml);
		return result;
	}

	public int UpLoadBlueC(HttpServletRequest request) {
		int result = -1;
		String step = "";
		String tmpDir = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");

		String apath = "", pictype = "0", picpath = "/pics/", albumid = "0", attach = "", merid = "", fileidname = "",
				dwcode = "", addPath = "", filelx = "";
		boolean removeold = false, isjson = false;

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(new File(tmpDir));
		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setHeaderEncoding("UTF-8");
		upload.setFileSizeMax(3 * 1024 * 1024);

		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				String nfilename = "";
				List<FileItem> list = upload.parseRequest(request);

				if (request.getParameter("filelx") != null) {

					if (request.getParameter("apath") != null)
						apath = request.getParameter("apath").toString();

					if (request.getParameter("attach") != null)
						attach = request.getParameter("attach").toString();

					if (request.getParameter("pictype") != null)
						pictype = request.getParameter("pictype").toString();

					if (request.getParameter("albumid") != null)
						albumid = request.getParameter("albumid").toString();

					if (request.getParameter("picpath") != null)
						picpath = request.getParameter("picpath").toString();

					if (request.getParameter("dwcode") != null)
						dwcode = request.getParameter("dwcode").toString();

					if (request.getParameter("merid") != null)
						merid = request.getParameter("merid").toString();

					addPath = "/mdir/" + merid + apath + picpath;

					filelx = request.getParameter("filelx").toString();

					if (filelx.equals("news") || filelx.equals("issue"))
						addPath = "/doc/" + filelx + "/images/";
					else if (filelx.equals("article"))
						addPath = "/mdir/" + merid + "/article/pics/";

					if (request.getParameter("fileidname") != null) {
						fileidname = request.getParameter("fileidname").toString();
						if (fileidname.equals(""))
							fileidname = filelx + dwcode;
					}

					removeold = request.getParameter("removeold") != null;
					isjson = request.getParameter("isjson") != null;

					nfilename = filelx;

					if (request.getParameter("auto") != null) {
						nfilename = nfilename + CUtil.getTimeT();
					} else {
						if (request.getParameter("pickey") != null)
							nfilename = nfilename + request.getParameter("pickey").toString();
						if (fileidname.equals(""))
							fileidname = filelx + dwcode;
					}
				} else {
					for (FileItem item : list) {
						if (item.isFormField()) {

							vParams.put(item.getFieldName(), item.getString("UTF-8"));
						}
					}

					if (vParams.containsKey("apath"))
						apath = vParams.get("apath").toString();

					if (vParams.containsKey("attach"))
						attach = vParams.get("attach").toString();

					if (vParams.containsKey("pictype"))
						pictype = vParams.get("pictype").toString();

					if (vParams.containsKey("albumid"))
						albumid = vParams.get("albumid").toString();

					if (vParams.containsKey("picpath"))
						picpath = vParams.get("picpath").toString();

					if (vParams.containsKey("dwcode"))
						dwcode = vParams.get("dwcode").toString();

					if (vParams.containsKey("merid"))
						merid = vParams.get("merid").toString();

					addPath = "/mdir/" + merid + apath + picpath;

					// step = addPath;

					filelx = vParams.get("filelx").toString();

					if (filelx.equals("news") || filelx.equals("issue"))
						addPath = "/doc/" + filelx + "/images/";
					else if (filelx.equals("article"))
						addPath = "/mdir/" + merid + "/article/pics/";

					if (vParams.containsKey("fileidname")) {
						fileidname = vParams.get("fileidname").toString();
						if (fileidname.equals(""))
							fileidname = filelx + dwcode;
					}

					removeold = vParams.containsKey("removeold");
					isjson = vParams.containsKey("isjson");

					nfilename = filelx;

					if (vParams.containsKey("auto")) {
						nfilename = nfilename + CUtil.getTimeT();
					} else {
						if (vParams.containsKey("pickey"))
							nfilename = nfilename + vParams.get("pickey").toString();
						if (fileidname.equals(""))
							fileidname = filelx + dwcode;
					}
				}
				String realPath = request.getSession().getServletContext().getRealPath(addPath) + "\\";
				// System.out.println("realPath:"+realPath);

				makeDir(realPath);

				for (FileItem item : list) {
					if (!item.isFormField()) {

						InputStream in = item.getInputStream();
						oldFileName = item.getName();
						newFileSize = item.getSize();

						newFileName = (nfilename.equals("")) ? oldFileName
								: nfilename + CUtil.GetExtension(oldFileName);

						FileOutputStream fos = new FileOutputStream(realPath + newFileName);
						int len = 0;
						byte[] b = new byte[1024];
						while ((len = in.read(b)) != -1) {
							fos.write(b, 0, len);
						}
						fos.close();
					}
				}
				result = 0;

				if (!merid.equals("")) {
					executeMsSqlProc("OwPicUpLoadSave",
							new String[] { merid, newFileName, fileidname, addPath, albumid, pictype });
					oldFileName = __procReturn[0];
				}

				if (removeold && !fileidname.equals("")) {
					DeleteFile(realPath + oldFileName, request);
				}

				if (isjson) {
					returnXml = CJson.ToJson(new String[] { "originalName", oldFileName, "name", newFileName, "url",
							addPath + newFileName, "size", newFileSize + "", "state", "SUCCESS", "type",
							CUtil.GetExtension(newFileName, 1), "filelx", filelx });
				} else {
					returnXml = "<xml><ds><data status=\"" + result + "\" filelx=\"" + filelx + "\" addpath=\""
							+ addPath + "\" filename=\"" + newFileName + "\" attach=\"" + attach
							+ "\"></data></ds></xml>";
				}
			} catch (Exception ex) {
				_errorInf = ex.getMessage();
				returnXml = _errorInf + step;
			}
		} else {
			returnXml = "not multipart/form-data";
		}

		// System.out.println("returnXml:" + returnXml);
		return result;
	}

	public void DeleteFile(String filePath, HttpServletRequest request) {
		File file = new File(request.getSession().getServletContext().getRealPath(filePath));
		file.lastModified();
		file.delete();
		if (file.exists())
			file.delete();
	}

	public String ReadFile(String filePath, HttpServletRequest request) {
		File file = new File(request.getSession().getServletContext().getRealPath(filePath));

		BufferedReader reader = null;
		StringBuffer sResult = new StringBuffer();

		try {
			if (file.exists()) {
				reader = new BufferedReader(new FileReader(file));
				String tempString = null;
				// 一次读入一行，直到读入null为文件结束
				while ((tempString = reader.readLine()) != null) {
					// 显示行号
					sResult.append(tempString);
				}
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return sResult.toString();
	}

	public void SaveFile(String filePath, String FileContent, HttpServletRequest request) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(request.getSession().getServletContext().getRealPath(filePath), false);
			writer.write(FileContent);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void WxSaveFile(String MerchantId, String ArtId, String ArtTitle, String ArtNote, String ArtPic,
			String LinkType, String LinkUrl, String FilePath, String FileContent, HttpServletRequest request) {
		SaveFile(FilePath, FileContent, request);

		String artsign = CUtil.SetPassWordEx(MerchantId + ArtId + "|" + CUtil.GenerateTimeStamp());

		String[] vparams = { MerchantId, ArtId, ArtTitle, ArtNote, ArtPic, LinkType, LinkUrl, artsign };
		try {
			/*
			 * OwSaveArticle(@MerchantId varchar(10),@ArtId int,@ArtTitle
			 * varchar(60),@ArtNote varchar(120),
			 * 
			 * @ArtPic varchar(200),
			 * 
			 * @LinkType smallint,@LinkUrl varchar(200),
			 * 
			 * @sResult varchar(300) out)
			 */
			executeMsSqlProc("OwSaveArticle", vparams);
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
	}

	public void SaveNewsFile(String FilePath, String FileContent, String idKey, String title, String userCode,
			HttpServletRequest request) {

		SaveFile(FilePath, FileContent, request);

		try {
			executeMsSqlProc("SaveNews", new String[] { idKey, title, userCode });
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
	}

	public void SaveIssueFile(String Path, String FileName, String FileContent, String idKey, String ddKey,
			String softid, String issueid, String title, String Txt, String userCode, HttpServletRequest request) {
		makeDir(request.getSession().getServletContext().getRealPath(Path));
		SaveFile(Path + FileName, FileContent, request);

		try {
			if (executeMsSqlProc("SaveIssue",
					new String[] { idKey, ddKey, softid, issueid, title, Txt, userCode }) != 0)
				P(_errorInf);
		} catch (Exception ex) {
			_errorInf = ex.getMessage();
		}
	}
}
