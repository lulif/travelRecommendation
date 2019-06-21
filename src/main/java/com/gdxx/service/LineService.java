package com.gdxx.service;

import java.util.Date;

import com.gdxx.dto.Step;
import com.gdxx.service.result.ServiceResult;

public interface LineService {
	// 规划地铁路线
	ServiceResult<Step> getLinesPlanning(String origin, String terminal, boolean isMinTime);

	// 步行/驾车路线
	ServiceResult<Step> getBaiduLine(Double locationLat, Double locationLng, Date departTime, String ori, String ter);
}
