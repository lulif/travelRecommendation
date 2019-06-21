package com.gdxx.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubwaySchedule {
	private Long scheduleId;
	private String stationName;
	private String firstCarTime;
	private String lastCarTime;
	private Long LineId;
}
