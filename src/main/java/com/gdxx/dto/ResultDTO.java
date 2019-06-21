package com.gdxx.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
/*
 * 路线结果集封装
 */
@Setter
@Getter
public class ResultDTO {
	private List<Step> steps;
	private String totalTime;
	private Double totalCost;
}
