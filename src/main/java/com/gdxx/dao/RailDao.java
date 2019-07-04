package com.gdxx.dao;

import java.util.List;
import java.util.Map;

import com.gdxx.entity.Flight;
import org.apache.ibatis.annotations.Param;

import com.gdxx.entity.Rail;

/*
 * 列车Dao接口
 */
public interface RailDao {

    List<Rail> queryRailsByCityName(@Param("cityName") String cityName);

    int queryRailsCountByCityName(String cityName);

    int updateRailScore(Rail rail);

    List<Rail> queryRailsOrderByDepartTime(@Param("cityName") String cityName,
                                           @Param("departTime") String departTime);
}
