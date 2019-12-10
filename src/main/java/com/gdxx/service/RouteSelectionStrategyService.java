package com.gdxx.service;

import com.gdxx.dto.ResultDTO;
import com.gdxx.param.SearchParam;
import com.gdxx.service.result.ServiceResult;

public interface RouteSelectionStrategyService {
    //根据评分制定路线策略
    ServiceResult<ResultDTO> makeRouteSelection(SearchParam searchParam);

    //新增/更新 航班数据时可调用
    ServiceResult updateFlightScore(String cityName);

    //新增/更新 列车数据时可调用
    ServiceResult updateRailScore(String cityName);

}
