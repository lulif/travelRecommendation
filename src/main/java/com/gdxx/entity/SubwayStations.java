package com.gdxx.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubwayStations {
	private Long stationId;
	private String stationName;
	private String stationLat;
	private String stationLng;
	private SubwayLines line;
	private String cityName;

}
