package com.gdxx.service;

import com.gdxx.entity.SubwayStations;
import com.gdxx.service.result.ServiceMultiResult;

public interface LocationSearchService {

	ServiceMultiResult<SubwayStations> getSubwayStationsByCityName(String cityName);

}
