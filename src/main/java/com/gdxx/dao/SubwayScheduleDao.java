package com.gdxx.dao;

import org.apache.ibatis.annotations.Param;

import com.gdxx.entity.SubwaySchedule;
/*
 * 地铁时刻Dao
 */
public interface SubwayScheduleDao {

	SubwaySchedule querySubwayScheduleByNameAndId(@Param("stationName") String stationName,
			@Param("lineId") Integer lineId);

}
