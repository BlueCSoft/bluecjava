package bluec.base.WxApp;

import java.util.Iterator;

import bluec.base.CHttpService;
import bluec.base.CQuery;
import bluec.base.CUtil;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class CWxMedia extends CQuery {
	private WxReqObj wxreqobj = null;

	public CWxMedia(WxReqObj pwxreqobj) {
		wxreqobj = pwxreqobj;
	}

	public String GetMaterialUrl(String MEDIA_ID, HttpServletResponse __Response) {
		StringBuilder postdata = new StringBuilder();
		postdata.append("{");
		postdata.append("\"media_id\":\"" + MEDIA_ID + "\"");
		postdata.append("}");

		return CHttpService.webRequestPost("https://api.weixin.qq.com/cgi-bin/material/get_material",
				"access_token=" + wxreqobj.gAccessToken, postdata.toString());

	}

	public String GetMaterialCount(String type) {
		String rResult = CHttpService.webRequestGet("https://api.weixin.qq.com/cgi-bin/material/get_materialcount",
				"access_token=" + wxreqobj.gAccessToken);
		StringBuilder sResult = new StringBuilder();

		try {
			JSONObject reader = new JSONObject(rResult);

			if (reader.has("errcode")) {
				sResult.append("<xml><errors><error>");
				sResult.append(reader.getString("errmsg"));
				sResult.append("</error></errors></xml>");
			} else {
				String typefield = "";
				switch (type) {
				case "\"image\"":
					typefield = "image_count";
					break;
				case "\"video\"":
					typefield = "video_count";
					break;
				case "\"voice\"":
					typefield = "voice_count";
					break;
				case "\"news\"":
					typefield = "news_count";
					break;
				}

				sResult.append("<xml id=\"bluec\">" + _XmlCr);
				sResult.append("<RS>" + _XmlCr);
				sResult.append("<R RL=\"" + 1 + "\" RT=\"0\" RS=\"0\" RR=\"0\"");

				int fieldCount = reader.length();
				int i = 0;

				sResult.append(String.format(" F%d=\"%s\"", 1, reader.getString(typefield)));
				Iterator<String> iterator = reader.keys();

				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					sResult.append(String.format(" F%d=\"%s\"", i + 2, reader.getString(key)));
					i++;
				}
				sResult.append("/>" + _XmlCr);
				sResult.append("</RS>" + _XmlCr);

				sResult.append("<FS>" + _XmlCr);

				sResult.append("<F");
				sResult.append(" A=\"F" + 1 + "\""); // 字段名称
				sResult.append(" O=\"F" + 1 + "\""); // 字段别名

				sResult.append(" B=\"" + 12 + "\"/>" + _XmlCr); // 字段类型

				for (i = 0; i < fieldCount; i++) {

					sResult.append("<F");
					sResult.append(" A=\"F" + (i + 2) + "\""); // 字段名称
					sResult.append(" O=\"F" + (i + 2) + "\""); // 字段别名
					sResult.append(" B=\"" + 12 + "\"/>" + _XmlCr); // 字段类型
				}
				sResult.append("</FS>" + _XmlCr);
				sResult.append("</xml>");
			}

		} catch (Exception ex) {
			logger.info(ex.getMessage());
		}
		return sResult.toString();
	}

	public String GetNewsList(String type, int offset, int count) {
		StringBuilder postdata = new StringBuilder();
		postdata.append("{");
		postdata.append("\"type\":\"" + type + "\",");
		postdata.append("\"offset\":" + offset + ",");
		postdata.append("\"count\":" + count);
		postdata.append("}");

		String s = CHttpService.webRequestPost("https://api.weixin.qq.com/cgi-bin/material/batchget_material",
				"access_token=" + wxreqobj.gAccessToken, postdata.toString());

		StringBuilder sResult = new StringBuilder();

		try {
			JSONObject reader = new JSONObject(s);

			if (reader.has("errcode")) {
				strResult = CUtil.ErrorMsgToXml(reader.getString("errmsg") + postdata.toString());
			} else {

				int rowcount = reader.getInt("item_count");

				sResult.append("<xml id=\"bluec\">" + _XmlCr);

				sResult.append("<RS>" + _XmlCr);

				if (rowcount > 0) {

					JSONArray items = reader.getJSONArray("item");

					if (type == "news") {
						for (int i = 0; i < rowcount; i++) {

							JSONObject item = items.getJSONObject(i);
							JSONObject subitem = item.getJSONObject("content").getJSONArray("news_item")
									.getJSONObject(0);

							sResult.append("<R RL=\"" + (i + 1) + "\" RT=\"0\" RS=\"0\" RR=\"0\"");
							sResult.append(String.format(" MEDIA_ID=\"%s\"",
									CUtil.replaceXmlSpChar(item.getString("media_id"))));
							sResult.append(
									String.format(" TITLE=\"%s\"", CUtil.replaceXmlSpChar(subitem.getString("title"))));
							sResult.append(String.format(" THUMB_MEDIA_ID=\"%s\"",
									CUtil.replaceXmlSpChar(subitem.getString("thumb_media_id"))));
							sResult.append(String.format(" SHOW_COVER_PIC=\"%s\"",
									CUtil.replaceXmlSpChar(subitem.getString("show_cover_pic"))));
							sResult.append(String.format(" AUTHOR=\"%s\"",
									CUtil.replaceXmlSpChar(subitem.getString("author"))));
							sResult.append(String.format(" DIGEST=\"%s\"",
									CUtil.replaceXmlSpChar(subitem.getString("digest"))));
							sResult.append(
									String.format(" URL=\"%s\"", CUtil.replaceXmlSpChar(subitem.getString("url"))));
							sResult.append(String.format(" CONTENT_SOURCE_URL=\"{0}\"",
									CUtil.replaceXmlSpChar(subitem.getString("content_source_url"))));

							sResult.append("/>" + _XmlCr);
						}
					} else {
						for (int i = 0; i < rowcount; i++) {

							JSONObject item = items.getJSONObject(i);

							sResult.append("<R RL=\"" + (i + 1) + "\" RT=\"0\" RS=\"0\" RR=\"0\"");
							sResult.append(String.format(" MEDIA_ID=\"%s\"",
									CUtil.replaceXmlSpChar(item.getString("media_id"))));
							sResult.append(
									String.format(" NAME=\"%s\"", CUtil.replaceXmlSpChar(item.getString("name"))));
							sResult.append(String.format(" URL=\"%s\"", CUtil.replaceXmlSpChar(item.getString("url"))));
							sResult.append("/>" + _XmlCr);
						}
					}
				} else {
					sResult.append("<R RL=\"1\" RT=\"0\" RS=\"0\" RR=\"0\"");
					for (int i = 0; i < ((type == "news") ? 8 : 3); i++) {
						sResult.append(String.format(" F%s=\"\"", i + 1));
					}
					sResult.append("/>" + _XmlCr);
				}

				sResult.append("</RS>" + _XmlCr);

				sResult.append("<FS>" + _XmlCr);

				for (int i = 0; i < ((type == "news") ? 8 : 3); i++) {
					sResult.append("<F");
					sResult.append(" A=\"F" + (i + 1) + "\""); // 字段名称
					sResult.append(" O=\"F" + (i + 1) + "\""); // 字段别名
					sResult.append(" B=\"12\"/>" + _XmlCr); // 字段类型
				}
				sResult.append("</FS>" + _XmlCr);

				sResult.append("</xml>");
				strResult = sResult.toString();
			}
		} catch (Exception ex) {
			logger.info(ex.getMessage());
		}
		return strResult;
	}

}
