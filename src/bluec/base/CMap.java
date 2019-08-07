package bluec.base;

import java.awt.geom.Point2D;

import org.json.JSONArray;
import org.json.JSONObject;

public class CMap extends CObject {
	private static final String AK = "bBHLPE6p5W3RymVCr4zatMAPWFbWQ96h";
	private static final double EARTH_RADIUS = 6371393; // 平均半径,单位：m

	/**
	 * 根据坐标获取地理位置
	 * 
	 * @param 经度
	 * @param 纬度
	 * @return
	 * @throws Exception
	 */
	public static String getLocationByBaiduMap(String longitude, String latitude) throws Exception {
		String locJson = CHttpService.sendGet(
				"http://api.map.baidu.com/geoconv/v1/?coords=" + longitude + "," + latitude + "&from=1&to=5&ak=" + AK);
		// System.out.println(locJson);
		JSONObject jobject = new JSONObject(locJson);
		JSONArray jsonArray = jobject.getJSONArray("result");
		String lat = jsonArray.getJSONObject(0).getString("y");
		String lng = jsonArray.getJSONObject(0).getString("x");

		String addrJson = CHttpService.sendGet("http://api.map.baidu.com/geocoder/v2/?ak=" + AK + "&location=" + lat
				+ "," + lng + "&output=json&pois=1");

		JSONObject jobjectaddr = new JSONObject(addrJson);
		JSONObject robj = jobjectaddr.getJSONObject("result");
		JSONObject aobj = robj.getJSONObject("addressComponent");

		D(robj.toString());
		String addr = robj.getString("formatted_address") + "," + aobj.getString("province")
				+ aobj.getString("district") + aobj.getString("street");

		return addr;
	}

	public static double getDistance(Point2D pointA, Point2D pointB) {
		double radiansAX = Math.toRadians(pointA.getX());
		double radiansAY = Math.toRadians(pointA.getY());
		double radiansBX = Math.toRadians(pointB.getX());
		double radiansBY = Math.toRadians(pointB.getY());

		double cos = Math.cos(radiansAY) * Math.cos(radiansBY) * Math.cos(radiansAX - radiansBX)
				+ Math.sin(radiansAY) * Math.sin(radiansBY);
		double acos = Math.acos(cos);
		return EARTH_RADIUS * acos;
	}

	public static double getDistance(double x0, double y0, double x1, double y1) {
		Point2D point0 = new Point2D.Double(x0, y0);
		Point2D point1 = new Point2D.Double(x1, y1);
		return getDistance(point0, point1);
	}

	public static CMapPoint qqMapTransBMap(String longitude, String latitude) {
		CMapPoint mp = new CMapPoint();
		double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
		/*
		 * double PI = 3.1415926535897932384626; double a = 6378245.0; double ee
		 * = 0.00669342162296594323;
		 */
		double lon = Double.parseDouble(longitude);
		double lat = Double.parseDouble(latitude);
		double z = Math.sqrt(lon * lon + lat * lat) + 0.00002 * Math.sin(lat * Math.PI);
		double theta = Math.atan2(lat, lon) + 0.000003 * Math.cos(lon * x_PI);
		mp.lon = String.format("%.6f",z * Math.cos(theta) + 0.0065);
		mp.lat = String.format("%.6f",z * Math.sin(theta) + 0.006);

		return mp;
	}
	
	public static CMapPoint bMapTransQQMap(String longitude, String latitude) {
		CMapPoint mp = new CMapPoint();
		double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
		double lon = Double.parseDouble(longitude) - 0.0065;
		double lat = Double.parseDouble(latitude) - 0.006;
		
		double z = Math.sqrt(lon * lon + lat * lat) - 0.00002 * Math.sin(lat * Math.PI);
		double theta = Math.atan2(lat, lon) - 0.000003 * Math.cos(lon * x_PI);
		mp.lon = String.format("%.6f",z * Math.cos(theta));
		mp.lat = String.format("%.6f",z * Math.sin(theta));

		//P("l="+String.format("%.6f",mp.lon)+","+String.format("%.6f",mp.lat));
		return mp;
	}
}
