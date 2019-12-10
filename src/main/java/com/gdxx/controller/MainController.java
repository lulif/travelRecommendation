package com.gdxx.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.gdxx.utils.CalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.gdxx.base.ApiResponse;
import com.gdxx.base.ApiResponse.Status;
import com.gdxx.dto.ResultDTO;
import com.gdxx.entity.SubwayStations;
import com.gdxx.entity.User;
import com.gdxx.param.SearchParam;
import com.gdxx.service.LocationSearchService;
import com.gdxx.service.RouteSelectionStrategyService;
import com.gdxx.service.UserService;
import com.gdxx.service.result.ServiceMultiResult;
import com.gdxx.service.result.ServiceResult;
import com.gdxx.utils.LocationUtil;

import java.io.UnsupportedEncodingException;

@RestController
public class MainController {
    @Autowired
    private LocationSearchService locationSearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private RouteSelectionStrategyService routeSelectionService;

    // 查询参数校验
    @PostMapping("/checkSearchMsg")
    private ApiResponse getSearchMsg(@Valid SearchParam searchParam, BindingResult bindingResult) throws UnsupportedEncodingException {
        // Test 防止定位失败
        searchParam.setCurrentLatitude("40.0755");
        searchParam.setCurrentLongitude("116.286");

        if (bindingResult.hasErrors()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        int ecScore = CalculateUtil.convertScore(searchParam.getEconomicScore());
        int timeScore = CalculateUtil.convertScore(searchParam.getTimeScore());
        int loadBearingScore = CalculateUtil.convertScore(searchParam.getLoadBearingScore());

        if (ecScore == 0 && timeScore == 0 && loadBearingScore == 0) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_TRAVEL_STATUS);
        }
        ServiceMultiResult<SubwayStations> bjStations = null;
        if (!"当前位置".equals(searchParam.getOrigin())) {
            bjStations = locationSearchService.getSubwayStationsByCityName("bj");
        }
        ServiceMultiResult<SubwayStations> kmStations = locationSearchService.getSubwayStationsByCityName("km");
        if (!LocationUtil.locationMatched(bjStations, kmStations, searchParam)) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_MATCHED_LOCATION);
        }

        if (!LocationUtil.fixedCurrentPosition(searchParam)) {
            return ApiResponse.ofStatus(ApiResponse.Status.FIXED_POSITION_FAIL);
        }
        return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
    }

    // 路线规划
    @PostMapping("/getTripLine")
    private ApiResponse getTripLine(@Valid SearchParam searchParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        ServiceResult<ResultDTO> result = routeSelectionService.makeRouteSelection(searchParam);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getResult());
        }
        return ApiResponse.ofStatus(ApiResponse.Status.FAIL);
    }

    // 用户信息获取
    @GetMapping("/getUserMsg")
    private ApiResponse getUserMsg(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            return ApiResponse.ofSuccess(user);
        } else {
            String openId = (String) request.getSession().getAttribute("opneId");
            if (openId != null) {
                ServiceResult<User> res = userService.getUserByOpenId(openId);
                user = res.getResult();
                return ApiResponse.ofSuccess(user);
            }
        }
        return ApiResponse.ofStatus(Status.NOT_FOUND);
    }

    //地点提示
    @GetMapping("/locationSupport/{cityName}")
    private ApiResponse getLocationSupport(@PathVariable("cityName") String cityName) {
        ServiceMultiResult<String> supportResult =
                locationSearchService.getLocationForSupport(cityName);
        if (supportResult.isSuccess()) {
            return ApiResponse.ofSuccess(supportResult.getResult());
        }
        return ApiResponse.ofStatus(Status.NOT_FOUND);
    }
}