package com.gdxx.utils;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gdxx.entity.SubwayStations;
import com.gdxx.service.JedisOperationService;

@Component
public class CalculateUtil {

	private static JedisOperationService jedisOperate;

	@Autowired
	public void setJedisOperate(JedisOperationService jedisOperate) {
		CalculateUtil.jedisOperate = jedisOperate;
	}

	private static final double EARTH_RADIUS = 6378.137;

	// 寻找最近的地铁站
	public static SubwayStations findNearestSubwayStation(Double lat1, Double lng1) {
		List<SubwayStations> list = jedisOperate.getAllStation();
		double distance = 0x3F3F3F3F;
		SubwayStations nearestStation = null;
		for (SubwayStations station : list) {
			double lat2 = Double.parseDouble(station.getStationLat());
			double lng2 = Double.parseDouble(station.getStationLng());
			double dis = CalculateUtil.getDistance(lat1, lng1, lat2, lng2);
			if (distance > dis) {
				distance = dis;
				nearestStation = station;
			}
		}
		return nearestStation;
	}

	// 根据经纬度两点距离(返回千米)
	public static double getDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
		double lat1 = Math.toRadians(latitude1);
		double lat2 = Math.toRadians(latitude2);
		double lng1 = Math.toRadians(longitude1);
		double lng2 = Math.toRadians(longitude2);
		double a = lat1 - lat2;
		double b = lng1 - lng2;
		double s = 2 * Math.asin(Math
				.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		String str = String.format("%.2f", s);
		return Double.parseDouble(str);
	}

	// 计算打车费用
	@SuppressWarnings("deprecation")
	public static double getTaixFare(String distance, Date time) {
		int dis = (int) Math.ceil(Integer.valueOf(distance) / 1000);
		int hours = time.getHours();
		double res = 0;
		if (dis <= 3) {
			res = 13.00;
		} else {
			if (hours < 5 && hours >= 0 || hours >= 23 && hours <= 24) {
				res = 13 + (dis - 3) * 2.3 * 1.2 + 1;
			} else {
				res = 13 + (dis - 3) * 2.3 + 1;
			}
		}
		String str = String.format("%.2f", res);
		return Double.parseDouble(str);
	}

	// 计算地铁费用
	public static double getSubwayFare(double distance) {
		int dis = (int) Math.ceil(distance / 1000);
		if (dis <= 3) {
			return 3.00;
		} else if (dis >= 4 && dis <= 12) {
			return 5.00;
		} else if (dis >= 22 && dis <= 32) {
			return 6.00;
		} else {
			return 6 + Math.ceil((dis - 32) / (double) 20);
		}
	}

	/*
	 * 时间转换
	 */
	public static int HHmm2seconds(String str) {
		if (str.contains("天")) {
			String[] s = str.split("\\+");
			int days = Integer.parseInt(s[1].substring(0, s[1].length() - 1));
			return HHmm2seconds(s[0]) + days * 86400;
		}
		String[] s = str.split(":");
		return Integer.valueOf(s[0]) * 3600 + Integer.valueOf(s[1]) * 60;
	}

	public static int MMss2seconds(String str) {
		String[] s = str.split(":");
		return Integer.valueOf(s[0]) * 60 + Integer.valueOf(s[1]);
	}

	public static String seconds2HHmm(int seconds) {
		int hours = Integer.valueOf(seconds) / 3600;
		int minutes = Integer.valueOf(seconds) % 3600 / 60;
		return String.valueOf(hours) + ":" + String.valueOf(minutes);
	}

	public static String seconds2HourAndMinute(int seconds) {
		int hours = Integer.valueOf(seconds) / 3600;
		int minutes = Integer.valueOf(seconds) % 3600 / 60;
		return String.valueOf(hours) + "时" + String.valueOf(minutes) + "分";
	}

	public static int hourAndMinute2seconds(String str) {
		int hours = Integer.valueOf(str.substring(0, str.indexOf("时")));
		int minute = Integer.valueOf(str.substring(str.indexOf("时") + 1, str.indexOf("分")));
		return hours * 3600 + minute * 60;
	}

	// 两 HH:mm 时间差
	public static int getSpendTime(String timeOri, String timeTer) {
		if (timeTer.contains("天")) {
			String[] str = timeTer.split("\\+");
			int days = Integer.parseInt(str[1].substring(0, str[1].length() - 1));
			timeTer = seconds2HHmm(HHmm2seconds(str[0]) + days * 86400);
		}
		return Math.max(HHmm2seconds(timeOri), HHmm2seconds(timeTer))
				- Math.min(HHmm2seconds(timeOri), HHmm2seconds(timeTer));
	}

	public static int convertScore(String score) {
		return Integer.valueOf(score.trim());
	}

}
