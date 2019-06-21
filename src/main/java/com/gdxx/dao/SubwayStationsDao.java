package com.gdxx.dao;

import java.util.List;

import com.gdxx.entity.SubwayStations;

/*
 * 地铁站点Dao
 */
public interface SubwayStationsDao {
	List<SubwayStations> querySubwayStationsList();

	int querySubwayStationsCount();

	List<SubwayStations> querySubwayStationByName(String subwayStationName);

	List<SubwayStations> querySubwayStationByLineId(Integer lineId);

	List<SubwayStations> querySubwayStationByCityName(String cityName);
}
