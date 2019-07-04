package com.gdxx.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.gdxx.entity.Flight;

/*
 * 航班Dao接口
 */
public interface FlightDao {
    /*
     * departTime 格式:00:00
     */
    int queryFlightsCountByCityName(String cityName);

    int updateFlightScore(Flight flight);

    List<Flight> queryFlightListByCityName(String cityName);

    List<Flight> queryFlightListOrderByDepartTime(@Param("cityName") String cityName,
                                                  @Param("departTime") String departTime);


}
