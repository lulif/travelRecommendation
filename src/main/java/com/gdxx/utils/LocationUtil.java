package com.gdxx.utils;

import java.util.List;

import com.gdxx.entity.SubwayStations;
import com.gdxx.param.SearchParam;
import com.gdxx.service.result.ServiceMultiResult;

/*
 * 地点检索匹配
 */
public class LocationUtil {
	public static boolean locationMatched(ServiceMultiResult<SubwayStations> bjStations,
			ServiceMultiResult<SubwayStations> kmStations, SearchParam searchParam) {
		if (bjStations.getTotal() <= 0 || kmStations.getTotal() <= 0) {
			return false;
		}
		if ("当前位置".equals(searchParam.getOrigin())) {
			if (hasStation(kmStations.getResult(), searchParam.getTerminal())) {
				return true;
			}
		} else {
			if (hasStation(kmStations.getResult(), searchParam.getTerminal())
					&& hasStation(bjStations.getResult(), searchParam.getOrigin())) {
				return true;
			}
		}
		return false;
	}

	public static boolean fixedCurrentPosition(SearchParam searchParam) {
		if (searchParam.getOrigin().equals("当前位置")) {
			if (searchParam.getCurrentLatitude() == null || searchParam.getCurrentLatitude().equals("")
					|| searchParam.getCurrentLongitude() == null || searchParam.getCurrentLongitude().equals("")) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasStation(List<SubwayStations> list, String place) {
		for (SubwayStations stations : list) {
			if (stations.getStationName().equals(place)) {
				return true;
			}
		}
		return false;
	}
}
