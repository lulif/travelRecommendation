package com.gdxx.utils;

import com.gdxx.base.BaiduLine;
import com.gdxx.base.BusBaiDuLine;
import com.google.gson.Gson;

/*
 * 百度接口调用
 */
public class BaiDuUtil {
	public static final String AK = "KGPOhDf6okL4OYQZoQjnGFyzCnASS5E6";

	// 步行路线规划
	public static BaiduLine WalkRoutePlanning(Double originLat, Double originLng, Double terminalLat,
			Double terminalLng) {
		String url = "http://api.map.baidu.com/directionlite/v1/walking?origin=" + originLat + "," + originLng
				+ "&destination=" + terminalLat + "," + terminalLng + "&ak=" + AK;
		String message = HttpsRequestUtil.httpsRequest(url, "GET", null);
		Gson gson = new Gson();
		BaiduLine walkLine = gson.fromJson(message, BaiduLine.class);
		return walkLine;
	}

	// 驾驶路线规划
	public static BaiduLine DriveRoutePlanning(Double originLat, Double originLng, Double terminalLat,
			Double terminalLng) {
		String url = "http://api.map.baidu.com/directionlite/v1/driving?origin=" + originLat + "," + originLng
				+ "&destination=" + terminalLat + "," + terminalLng + "&ak=" + AK;
		String message = HttpsRequestUtil.httpsRequest(url, "GET", null);
		Gson gson = new Gson();
		BaiduLine walkLine = gson.fromJson(message, BaiduLine.class);
		return walkLine;
	}

	// 公交/地铁路线(未使用)
	public static BusBaiDuLine BusRotePlanning(Double originLat, Double originLng, Double terminalLat,
			Double terminalLng) {
		String url = "http://api.map.baidu.com/directionlite/v1/transit?origin=" + originLat + "," + originLng
				+ "&destination=" + terminalLat + "," + terminalLng + "&ak=" + AK;
		String message = HttpsRequestUtil.httpsRequest(url, "GET", null);
		Gson gson = new Gson();
		BusBaiDuLine busLine = gson.fromJson(message, BusBaiDuLine.class);
		return busLine;
	}

	// public static void main(String[] args) throws Exception {
	//// BusBaiDuLine line = BusRotePlanning(39.9916376781, 116.2700219427,
	// 39.97954292391489, 116.39868863487347);
	//// System.out.println("OK");
	// BaiduLine line=WalkRoutePlanning(39.9916376781, 116.2700219427,
	// 39.97954292391489, 116.39868863487347);
	// }

}
