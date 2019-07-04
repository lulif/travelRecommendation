package com.gdxx.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gdxx.dao.SubwayStationsDao;
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
    @Autowired
    private SubwayStationsDao subwayStationsDao;

    @Override
    public ServiceMultiResult<SubwayStations> getSubwayStationsByCityName(String cityName) {
        List<SubwayStations> subwayStationsList = jedisOperationService.getStationsByCityName(cityName);
        int count = subwayStationsList.size();
        return new ServiceMultiResult<>(true, count, subwayStationsList);
    }

    @Override
    public ServiceMultiResult<String> getLocationForSupport(String cityName) {
        Set<String> stationNameSet = jedisOperationService.getStationNamesForSupport(cityName);
        int count = stationNameSet.size();
        if (count <= 0) {
            return new ServiceMultiResult<>(false);
        }
        return new ServiceMultiResult<>(true, count, new ArrayList<>(stationNameSet));
    }
}
