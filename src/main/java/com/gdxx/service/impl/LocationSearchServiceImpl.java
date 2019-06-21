package com.gdxx.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxx.entity.SubwayStations;
import com.gdxx.service.JedisOperationService;
import com.gdxx.service.LocationSearchService;
import com.gdxx.service.result.ServiceMultiResult;

@Service
public class LocationSearchServiceImpl implements LocationSearchService {
	@Autowired
	private JedisOperationService jedisOperationService;

	@Override
	public ServiceMultiResult<SubwayStations> getSubwayStationsByCityName(String cityName) {
		List<SubwayStations> subwayStationsList = jedisOperationService.getStationsByCityName(cityName);
		int count = subwayStationsList.size();
		return new ServiceMultiResult<>(count, subwayStationsList);
	}
}
