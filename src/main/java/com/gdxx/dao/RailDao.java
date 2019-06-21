package com.gdxx.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.gdxx.entity.Rail;
/*
 * 列车Dao接口
 */
public interface RailDao {
	List<Rail> queryRailList(@Param("cityName") String cityName, @Param("departTime") String departTime);
}
