package com.gdxx.dto;

import java.util.List;

import com.gdxx.entity.SubwayLines;
import com.gdxx.entity.SubwayStations;

import lombok.Getter;
import lombok.Setter;

/*
 * 地铁路线结果集封装
 */
@Setter
@Getter
public class SubwayLinePlanning {
	private List<SubwayStations> passSitesList;
	private List<SubwayLines> passSubwayLineList;
	private int TransitTimes;
	private int totalSpendTime;
}
