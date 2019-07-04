package com.gdxx.dao;

import java.util.List;
import java.util.Set;

import com.gdxx.entity.SubwayStations;
import org.apache.ibatis.annotations.Param;

/*
 * 地铁站点Dao
 */
public interface SubwayStationsDao {
    List<SubwayStations> querySubwayStationsList();

    List<SubwayStations> querySubwayStationByName(String subwayStationName);

    List<SubwayStations> querySubwayStationByLineId(Integer lineId);

    List<SubwayStations> querySubwayStationByCityName(String cityName);

    Set<String> queryStationsNameForSupport(String cityName);
}
