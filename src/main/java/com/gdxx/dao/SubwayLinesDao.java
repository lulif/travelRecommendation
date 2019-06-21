package com.gdxx.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.gdxx.entity.SubwayLines;
/*
 * 地铁路线Dao
 */
public interface SubwayLinesDao {
	SubwayLines querySubwayLinesByLineId(Integer LineId);

	List<SubwayLines> querySubwayLineByLineName(@Param("lineName") String lineName, @Param("cityName") String cityName);
}
