package com.gdxx.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
/*
 * 路线分段 数据封装
 */
@Getter
@Setter
@Builder(toBuilder = true)
public class Step {

	private String tempOri;
	private String tempTer;
	private String tempTime;
	private Double tempCost;
	private String tempDistance;

	private Object way;

	private SubwayLinePlanning subwayLinePlanning;
	@Builder.Default
	private Boolean isSubway = false;
	@Builder.Default
	private Boolean isDrive = false;
	@Builder.Default
	private Boolean isWalk = false;
	@Builder.Default
	private Boolean isFlight = false;
	@Builder.Default
	private Boolean isRail = false;

}
