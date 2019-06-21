package com.gdxx.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Rail {
	private Long railId;
	private String railCode;
	private Sites departurePlace;
	private String departureTime;
	private Sites stopoverStation;
	private String arriveTime;
	private String consumeTime;
	private Double firstSeatPrice;
	private Double secondSeatPrice;
	private Double businessSeatPrice;
}
