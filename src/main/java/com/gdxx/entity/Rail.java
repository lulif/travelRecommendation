package com.gdxx.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Rail implements Comparable{
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
	private Double economicScore;
	private Double timeScore;
	private Double loadBearingScore;
	private Integer esGradeNumber;
	private Integer tsGradeNumber;
	private Integer lbsGradeNumber;
	private Double differenceDegree;

	@Override
	public int compareTo(Object o) {
		return 1;
	}
}
