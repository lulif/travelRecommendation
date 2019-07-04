package com.gdxx.service;

import java.util.List;
import java.util.Set;

import com.gdxx.entity.SubwayStations;
import com.gdxx.entity.SubwayTransfer;

public interface JedisOperationService {
	List<SubwayStations> getStationsByName(String stationName);

	List<SubwayTransfer> getTransferList();

	List<SubwayStations> getStationsByLineId(Integer lineId);

	List<SubwayStations> getStationsByCityName(String cityName);

	List<SubwayStations> getAllStation();

    Set<String> getStationNamesForSupport(String cityName);
}
