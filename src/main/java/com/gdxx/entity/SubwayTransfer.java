package com.gdxx.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubwayTransfer {
	private Integer transferId;
	private String stationName;
	private Integer lineFrom;
	private Integer lineTo;
	private String spendTime;
}
