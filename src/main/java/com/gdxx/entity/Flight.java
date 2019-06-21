package com.gdxx.entity;

import lombok.Getter;
import lombok.Setter;

/*
 * 航班信息
 */
@Getter
@Setter
public class Flight {
	private Long flightId;
	private String flightName;
	private String aircraftType;
	private String aircraftTypeCode;
	private String departureTime;
	private Sites departurePlace;
	private Sites destinationPlace;
	private String arriveTime;
	private Integer flightPrice;
	private String discountMethod;
	private Integer punctualityRate;

}
