package com.gdxx.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.gdxx.entity.SubwayTransfer;
/*
 * 地铁换乘Dao
 */
public interface SubwayTransferDao {

	List<SubwayTransfer> querySubwayTransferList();

	SubwayTransfer querySubwayTransferByNameAndFromTo(@Param("stationName") String stationName,
			@Param("lineFrom") Integer lineFrom, @Param("lineTo") Integer lineTo);
}
