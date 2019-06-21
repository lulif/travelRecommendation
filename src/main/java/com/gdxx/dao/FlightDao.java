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
	List<Flight> queryFlightList(@Param("cityName") String cityName, @Param("departTime") String departTime);

	List<Flight> queryFlightOrderByPrice(@Param("cityName") String cityName, @Param("departTime") String departTime);

	List<Flight> queryFligtOrderByRate(@Param("cityName") String cityName, @Param("departTime") String departTime);

	List<Flight> queryFlightOrderByPriceAndRate(@Param("cityName") String cityName,
			@Param("departTime") String departTime);

	List<Flight> queryFlightOrderByRateAndPrice(@Param("cityName") String cityName,
			@Param("departTime") String departTime);
}
