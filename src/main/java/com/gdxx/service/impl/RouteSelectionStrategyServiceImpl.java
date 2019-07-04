package com.gdxx.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxx.base.BaiduLine;
import com.gdxx.dao.FlightDao;
import com.gdxx.dao.RailDao;
import com.gdxx.dao.SitesDao;
import com.gdxx.dto.ResultDTO;
import com.gdxx.dto.Step;
import com.gdxx.entity.Flight;
import com.gdxx.entity.Rail;
import com.gdxx.entity.Sites;
import com.gdxx.entity.SubwayStations;
import com.gdxx.param.SearchParam;
import com.gdxx.service.JedisOperationService;
import com.gdxx.service.LineService;
import com.gdxx.service.RouteSelectionStrategyService;
import com.gdxx.service.result.ServiceResult;
import com.gdxx.utils.BaiDuUtil;
import com.gdxx.utils.CalculateUtil;

@Service
public class RouteSelectionStrategyServiceImpl implements RouteSelectionStrategyService {

    @Autowired
    private LineService lineService;
    @Autowired
    private RailDao railDao;
    @Autowired
    private FlightDao flightDao;
    @Autowired
    private JedisOperationService jedisOperate;
    @Autowired
    private SitesDao sitesDao;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    // 路线策划
    public ServiceResult<ResultDTO> makeRouteSelection(SearchParam searchParam) {
        String departWay = searchParam.getDepartWay();
        int ecScore = CalculateUtil.convertScore(searchParam.getEconomicScore());
        int timeScore = CalculateUtil.convertScore(searchParam.getTimeScore());
        int loadBearingScore = CalculateUtil.convertScore(searchParam.getLoadBearingScore());
        switch (departWay) {
            case "商务旅客":
                ecScore += 10;
                break;
            case "个人出行":
                timeScore += 10;
                break;
            case "家庭出游":
                loadBearingScore += 10;
                break;
        }
        searchParam.setEconomicScore(String.valueOf(ecScore));
        searchParam.setTimeScore(String.valueOf(timeScore));
        searchParam.setLoadBearingScore(String.valueOf(loadBearingScore));

        ResultDTO resultDTO = null;
        if (ecScore >= 50 && timeScore >= 50 && loadBearingScore >= 50) {
            resultDTO = RouteStrategy1(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }

        if (ecScore >= 50 && timeScore >= 50 && loadBearingScore < 50) {
            resultDTO = RouteStrategy2(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }

        if (ecScore >= 50 && timeScore < 50 && loadBearingScore >= 50) {
            resultDTO = RouteStrategy3(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }

        if (ecScore < 50 && timeScore >= 50 && loadBearingScore >= 50) {
            resultDTO = RouteStrategy4(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }
        if (ecScore >= 50 && timeScore < 50 && loadBearingScore < 50) {
            resultDTO = RouteStrategy5(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }

        if (ecScore < 50 && timeScore >= 50 && loadBearingScore < 50) {
            resultDTO = RouteStrategy6(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }

        if (ecScore < 50 && timeScore < 50 && loadBearingScore >= 50) {
            resultDTO = RouteStrategy7(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }
        if (ecScore < 50 && timeScore < 50 && loadBearingScore < 50) {
            resultDTO = RouteStrategy8(searchParam);
            if (resultDTO != null) {
                return ServiceResult.of(resultDTO);
            }
            return ServiceResult.notFound();
        }
        return ServiceResult.notFound();
    }

    // 地铁(最小换乘时间)+飞机+地铁(最少换乘次数)
    private ResultDTO RouteStrategy8(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<Step>();
        String subwayOri = null;
        int spendTime = 0;
        Double lat1 = null;
        Double lng1 = null;
        Double lat2 = null;
        Double lng2 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            searchParam.setCurrentLatitude("40.0757");
            searchParam.setCurrentLongitude("116.287");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
            ServiceResult<Step> baiduStep = lineService.getBaiduLine(lat1, lng1, searchParam.getDepartTime(), "当前位置",
                    null);
            if (baiduStep.getResult() == null) {
                return null;
            }
            stepList.add(baiduStep.getResult());
            subwayOri = baiduStep.getResult().getTempTer();
            spendTime = CalculateUtil.hourAndMinute2seconds(baiduStep.getResult().getTempTime());
        } else {
            subwayOri = searchParam.getOrigin();
        }
        String departTime = sdf.format(searchParam.getDepartTime());
        int departTimeInt = CalculateUtil.HHmm2seconds(departTime);
        List<Flight> sortedFlightList = afterSortedFlightList("bj", searchParam, departTime);
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }
        // 选择航班
        Flight flightArrive = null;
        for (Flight flight : sortedFlightList) {
            // 去机场的路线
            lat2 = Double.valueOf(flight.getDeparturePlace().getSiteLat());
            lng2 = Double.valueOf(flight.getDeparturePlace().getSiteLng());
            SubwayStations subwayStation = CalculateUtil.findNearestSubwayStation(lat2, lng2);
            ServiceResult<Step> subwayStep = lineService.getLinesPlanning(subwayOri, subwayStation.getStationName(),
                    true);
            if (subwayStep.getResult() == null) {
                return null;
            }
            int subwayTime = CalculateUtil.hourAndMinute2seconds(subwayStep.getResult().getTempTime());
            if (departTimeInt + subwayTime + spendTime > CalculateUtil.HHmm2seconds(flight.getDepartureTime())) {
                continue;
            } else {
                flightArrive = flight;
                stepList.add(subwayStep.getResult());
                stepList.add(getFlightStep(flightArrive));
                break;
            }
        }
        if (flightArrive == null) {
            return null;
        }
        ServiceResult<Step> subwayToTerminal = lineService.getLinesPlanning("机场中心", searchParam.getTerminal(), false);
        if (subwayToTerminal.getResult() == null) {
            return null;
        }
        stepList.add(subwayToTerminal.getResult());
        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // 地铁(最少换乘次数)+飞机+地铁(最少换乘次数)
    private ResultDTO RouteStrategy7(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<Step>();
        String subwayOri = null;
        int spendTime = 0;
        Double lat1 = null;
        Double lng1 = null;
        Double lat2 = null;
        Double lng2 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            searchParam.setCurrentLatitude("40.0723442");
            searchParam.setCurrentLongitude("116.3023442");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
            ServiceResult<Step> baiduStep = lineService.getBaiduLine(lat1, lng1, searchParam.getDepartTime(), "当前位置",
                    null);
            if (baiduStep.getResult() == null) {
                return null;
            }
            stepList.add(baiduStep.getResult());
            subwayOri = baiduStep.getResult().getTempTer();
            spendTime = CalculateUtil.hourAndMinute2seconds(baiduStep.getResult().getTempTime());
        } else {
            subwayOri = searchParam.getOrigin();
        }
        String departTime = sdf.format(searchParam.getDepartTime());
        int departTimeInt = CalculateUtil.HHmm2seconds(departTime);
        List<Flight> sortedFlightList = afterSortedFlightList("bj", searchParam, departTime);
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }
        // 选择航班
        Flight flightArrive = null;
        for (Flight flight : sortedFlightList) {
            // 去机场的路线
            lat2 = Double.valueOf(flight.getDeparturePlace().getSiteLat());
            lng2 = Double.valueOf(flight.getDeparturePlace().getSiteLng());
            SubwayStations subwayStation = CalculateUtil.findNearestSubwayStation(lat2, lng2);
            ServiceResult<Step> subwayStep = lineService.getLinesPlanning(subwayOri, subwayStation.getStationName(),
                    false);
            if (subwayStep.getResult() == null) {
                return null;
            }
            int subwayTime = CalculateUtil.hourAndMinute2seconds(subwayStep.getResult().getTempTime());
            if (departTimeInt + subwayTime + spendTime > CalculateUtil.HHmm2seconds(flight.getDepartureTime())) {
                continue;
            } else {
                flightArrive = flight;
                stepList.add(subwayStep.getResult());
                stepList.add(getFlightStep(flightArrive));
                break;
            }
        }
        if (flightArrive == null) {
            return null;
        }
        ServiceResult<Step> subwayToTerminal = lineService.getLinesPlanning("机场中心", searchParam.getTerminal(), false);
        if (subwayToTerminal.getResult() == null) {
            return null;
        }
        stepList.add(subwayToTerminal.getResult());
        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // 地铁(最少换乘次数)+动车+飞机+地铁(最少换乘次数)
    private ResultDTO RouteStrategy6(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<Step>();
        String subwayOri = null;
        int spendTime = 0;
        Double lat1 = null;
        Double lng1 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            // Test 寻找最近地铁站
            searchParam.setCurrentLatitude("39.9916376781");
            searchParam.setCurrentLongitude("116.2700219427");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
            ServiceResult<Step> baiduStep = lineService.getBaiduLine(lat1, lng1, searchParam.getDepartTime(), "当前位置",
                    null);
            if (baiduStep.getResult() == null) {
                return null;
            }
            stepList.add(baiduStep.getResult());
            subwayOri = baiduStep.getResult().getTempTer();
            spendTime = CalculateUtil.hourAndMinute2seconds(baiduStep.getResult().getTempTime());
        } else {
            subwayOri = searchParam.getOrigin();
        }
        String departTime = sdf.format(searchParam.getDepartTime());
        List<Rail> sortedRailList = afterSortedRailList("bj", searchParam, departTime);
        if (sortedRailList == null || sortedRailList.size() <= 0) {
            return null;
        }
        // 选择列车车次
        Rail railArrive = null;
        ServiceResult<Step> subwayStep = null;
        for (Rail rail : sortedRailList) {
            double railLat = Double.parseDouble(rail.getDeparturePlace().getSiteLat());
            double railLng = Double.parseDouble(rail.getDeparturePlace().getSiteLng());
            SubwayStations subwayStation = CalculateUtil.findNearestSubwayStation(railLat, railLng);
            subwayStep = lineService.getLinesPlanning(subwayOri, subwayStation.getStationName(), false);
            if (subwayStep.getResult() == null) {
                return null;
            }
            spendTime += CalculateUtil.hourAndMinute2seconds(subwayStep.getResult().getTempTime());
            if (CalculateUtil.HHmm2seconds(departTime) + spendTime > CalculateUtil
                    .HHmm2seconds(rail.getDepartureTime())) {
                continue;
            } else {
                railArrive = rail;
                break;
            }
        }
        if (railArrive == null) {
            return null;
        }
        stepList.add(subwayStep.getResult());
        Step step3 = getRailStep(railArrive);
        stepList.add(step3);
        // 选择航班
        Flight flightArrive = null;
        List<Flight> sortedFlightList = afterSortedFlightList("sjz", searchParam, railArrive.getArriveTime());
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }
        for (Flight flight : sortedFlightList) {
            BaiduLine line = BaiDuUtil.WalkRoutePlanning(Double.valueOf(railArrive.getStopoverStation().getSiteLat()),
                    Double.valueOf(railArrive.getStopoverStation().getSiteLng()),
                    Double.valueOf(flight.getDeparturePlace().getSiteLat()),
                    Double.valueOf(flight.getDeparturePlace().getSiteLng()));
            String sptime = line.getResult().getRoutes().get(0).getDuration();
            int tt = CalculateUtil.getSpendTime(railArrive.getArriveTime(), flight.getDepartureTime());
            if (tt < Integer.valueOf(sptime)) {
                continue;
            } else {
                flightArrive = flight;
                Step step1 = Step.builder().tempOri(railArrive.getStopoverStation().getSiteName())
                        .tempTer(flight.getDeparturePlace().getSiteName()).tempCost(0.0).isWalk(true)
                        .way(line.getResult().getRoutes().get(0))
                        .tempTime(CalculateUtil.seconds2HourAndMinute(
                                Integer.valueOf(line.getResult().getRoutes().get(0).getDuration())))
                        .tempDistance(line.getResult().getRoutes().get(0).getDistance()).build();
                stepList.add(step1);
                stepList.add(getFlightStep(flightArrive));
                break;
            }
        }
        if (flightArrive == null) {
            return null;
        }
        ServiceResult<Step> subwayToTerminal = lineService.getLinesPlanning("机场中心", searchParam.getTerminal(), false);
        if (subwayToTerminal.getResult() == null) {
            return null;
        }
        stepList.add(subwayToTerminal.getResult());
        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // 打车+飞机+打车
    private ResultDTO RouteStrategy5(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<Step>();
        Double lat1 = null;
        Double lng1 = null;
        Double lat2 = null;
        Double lng2 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            // Test 寻找最近地铁站
            searchParam.setCurrentLatitude("39.9916376781");
            searchParam.setCurrentLongitude("116.2700219427");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
        } else {
            List<SubwayStations> subways = jedisOperate.getStationsByName(searchParam.getOrigin());
            lat1 = Double.parseDouble(subways.get(0).getStationLat());
            lng1 = Double.parseDouble(subways.get(0).getStationLng());
        }

        String departTime = sdf.format(searchParam.getDepartTime());
        int departTimeInt = CalculateUtil.HHmm2seconds(departTime);
        List<Flight> sortedFlightList = afterSortedFlightList("bj", searchParam, departTime);
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }
        // 选择航班
        Flight flightArrive = null;
        BaiduLine line = null;
        for (Flight flight : sortedFlightList) {
            // 去机场的路线
            lat2 = Double.valueOf(flight.getDeparturePlace().getSiteLat());
            lng2 = Double.valueOf(flight.getDeparturePlace().getSiteLng());
            line = BaiDuUtil.DriveRoutePlanning(lat1, lng1, lat2, lng2);
            if (line == null) {
                return null;
            }
            int seconds = Integer.parseInt(line.getResult().getRoutes().get(0).getDuration());
            if (departTimeInt + seconds > CalculateUtil.HHmm2seconds(flight.getDepartureTime())) {
                continue;
            } else {
                flightArrive = flight;
                break;
            }
        }
        if (flightArrive == null) {
            return null;
        }
        stepList.add(getDriveStepWithFlight(line, searchParam, flightArrive));
        stepList.add(getFlightStep(flightArrive));
        stepList.add(getDriveStepToTerminal(flightArrive, searchParam));
        result.setSteps(stepList);
        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // [地铁(最少换乘次数)+[飞机]/[动车+飞机]=>(费用权衡) +地铁(最少换乘次数)
    private ResultDTO RouteStrategy4(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<Step>();
        String subwayOri = null;
        int spendTime = 0;
        Double lat1 = null;
        Double lng1 = null;
        Double lat2 = null;
        Double lng2 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            searchParam.setCurrentLatitude("40.0723442");
            searchParam.setCurrentLongitude("116.3023442");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
            ServiceResult<Step> steoToStation = lineService.getBaiduLine(lat1, lng1, searchParam.getDepartTime(),
                    "当前位置", null);
            if (steoToStation.getResult() == null) {
                return null;
            }
            stepList.add(steoToStation.getResult());
            subwayOri = steoToStation.getResult().getTempTer();
            spendTime = CalculateUtil.hourAndMinute2seconds(steoToStation.getResult().getTempTime());
        } else {
            subwayOri = searchParam.getOrigin();
        }

        String departTime = sdf.format(searchParam.getDepartTime());
        int departTimeInt = CalculateUtil.HHmm2seconds(departTime);

        List<Flight> sortedFlightList = afterSortedFlightList("bj", searchParam, departTime);
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }

        // 选择航班
        Flight flightArrive1 = null;
        ServiceResult<Step> subwayStep1 = null;
        for (Flight flight : sortedFlightList) {
            // 去机场的路线
            lat2 = Double.valueOf(flight.getDeparturePlace().getSiteLat());
            lng2 = Double.valueOf(flight.getDeparturePlace().getSiteLng());
            SubwayStations subwayStation = CalculateUtil.findNearestSubwayStation(lat2, lng2);
            subwayStep1 = lineService.getLinesPlanning(subwayOri, subwayStation.getStationName(), false);
            if (subwayStep1 == null) {
                return null;
            }
            int subwayTime = CalculateUtil.hourAndMinute2seconds(subwayStep1.getResult().getTempTime());
            if (departTimeInt + subwayTime + spendTime > CalculateUtil.HHmm2seconds(flight.getDepartureTime())) {
                continue;
            } else {
                flightArrive1 = flight;
                break;
            }
        }
        if (flightArrive1 == null) {
            return null;
        }

        // 动车+飞机的组合
        List<Rail> sortedRailList = afterSortedRailList("bj", searchParam, departTime);
        if (sortedRailList == null || sortedRailList.size() <= 0) {
            return null;
        }

        ServiceResult<Step> subwayStep2 = null;
        Rail railArrive = null;
        Step railToFlight = null;
        Flight flightArrive2 = null;
        spendTime = 0;
        for (Rail rail : sortedRailList) {
            double railLat = Double.parseDouble(rail.getDeparturePlace().getSiteLat());
            double railLng = Double.parseDouble(rail.getDeparturePlace().getSiteLng());
            SubwayStations subwayStation = CalculateUtil.findNearestSubwayStation(railLat, railLng);
            subwayStep2 = lineService.getLinesPlanning(subwayOri, subwayStation.getStationName(), false);
            if (subwayStep2.getResult() == null) {
                return null;
            }
            spendTime += CalculateUtil.hourAndMinute2seconds(subwayStep2.getResult().getTempTime());
            if (CalculateUtil.HHmm2seconds(departTime) + spendTime > CalculateUtil
                    .HHmm2seconds(rail.getDepartureTime())) {
                continue;
            } else {
                railArrive = rail;
                break;
            }
        }
        // 选择航班
        List<Flight> sortedFlightListFromsjz = afterSortedFlightList("sjz", searchParam, railArrive.getArriveTime());
        if (sortedFlightListFromsjz == null || sortedFlightListFromsjz.size() <= 0) {
            return null;
        }

        for (Flight flight1 : sortedFlightListFromsjz) {
            BaiduLine line = BaiDuUtil.WalkRoutePlanning(Double.valueOf(railArrive.getStopoverStation().getSiteLat()),
                    Double.valueOf(railArrive.getStopoverStation().getSiteLng()),
                    Double.valueOf(flight1.getDeparturePlace().getSiteLat()),
                    Double.valueOf(flight1.getDeparturePlace().getSiteLng()));
            String sptime = line.getResult().getRoutes().get(0).getDuration();
            int tt = CalculateUtil.getSpendTime(railArrive.getArriveTime(), flight1.getDepartureTime());
            if (tt < Integer.valueOf(sptime)) {
                continue;
            } else {
                flightArrive2 = flight1;
                railToFlight = Step.builder().tempOri(railArrive.getStopoverStation().getSiteName())
                        .tempTer(flight1.getDeparturePlace().getSiteName()).tempCost(0.0).isWalk(true)
                        .way(line.getResult().getRoutes().get(0))
                        .tempTime(CalculateUtil.seconds2HourAndMinute(
                                Integer.valueOf(line.getResult().getRoutes().get(0).getDuration())))
                        .tempDistance(line.getResult().getRoutes().get(0).getDistance()).build();
                break;
            }
        }
        if (railToFlight == null || flightArrive2 == null || railArrive == null || subwayStep2 == null) {
            return null;
        }
        // 两个计划做 所需金钱的对比
        double planAMoney = subwayStep1.getResult().getTempCost() + flightArrive1.getFlightPrice();
        double planBMoney = subwayStep2.getResult().getTempCost() + railArrive.getSecondSeatPrice()
                + railToFlight.getTempCost() + flightArrive2.getFlightPrice();
        if (planAMoney > planBMoney) {
            stepList.add(subwayStep1.getResult());
            stepList.add(getFlightStep(flightArrive1));
        } else {
            stepList.add(subwayStep2.getResult());
            Step step = getRailStep(railArrive);
            step.setTempCost(railArrive.getFirstSeatPrice());
            stepList.add(step);
            stepList.add(railToFlight);
            stepList.add(getFlightStep(flightArrive2));
        }
        ServiceResult<Step> subwayToTerminal = lineService.getLinesPlanning("机场中心", searchParam.getTerminal(), false);
        if (subwayToTerminal.getResult() == null) {
            return null;
        }
        stepList.add(subwayToTerminal.getResult());
        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // [驾车]/[地铁(最小换乘时间)]=>(时间权衡) +飞机 +[驾车]/[地铁((最小换乘时间))]=>(时间权衡)
    private ResultDTO RouteStrategy3(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<>();
        String subwayOri = null;
        int spendTime = 0;
        Double lat1 = null;
        Double lng1 = null;
        Double lat2 = null;
        Double lng2 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            searchParam.setCurrentLatitude("40.0723442");
            searchParam.setCurrentLongitude("116.3023442");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
            ServiceResult<Step> stepToStation = lineService.getBaiduLine(lat1, lng1, searchParam.getDepartTime(),
                    "当前位置", null);
            if (stepToStation.getResult() == null) {
                return null;
            }
            stepList.add(stepToStation.getResult());
            subwayOri = stepToStation.getResult().getTempTer();
            spendTime = CalculateUtil.hourAndMinute2seconds(stepToStation.getResult().getTempTime());
        } else {
            subwayOri = searchParam.getOrigin();
            List<SubwayStations> subways = jedisOperate.getStationsByName(searchParam.getOrigin());
            lat1 = Double.parseDouble(subways.get(0).getStationLat());
            lng1 = Double.parseDouble(subways.get(0).getStationLng());
        }

        String departTime = sdf.format(searchParam.getDepartTime());
        int departTimeInt = CalculateUtil.HHmm2seconds(departTime);

        List<Flight> sortedFlightList = afterSortedFlightList("bj", searchParam, departTime);
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }

        // 选择航班
        Flight flightArrive = null;
        BaiduLine line1 = null;
        ServiceResult<Step> subwayStep = null;
        boolean selectDrive = false;
        for (Flight flight : sortedFlightList) {
            // 驾车去机场的路线
            lat2 = Double.valueOf(flight.getDeparturePlace().getSiteLat());
            lng2 = Double.valueOf(flight.getDeparturePlace().getSiteLng());
            line1 = BaiDuUtil.DriveRoutePlanning(lat1, lng1, lat2, lng2);
            int driveTime = Integer.parseInt(line1.getResult().getRoutes().get(0).getDuration());

            SubwayStations subwayStation = CalculateUtil.findNearestSubwayStation(lat2, lng2);
            subwayStep = lineService.getLinesPlanning(subwayOri, subwayStation.getStationName(), true);
            if (subwayStep.getResult() == null) {
                return null;
            }
            // 地铁无法直接到达
            int subwayTime = 0;
            if (!subwayStep.isSuccess()) {
                selectDrive = true;
            } else {
                subwayTime = CalculateUtil.hourAndMinute2seconds(subwayStep.getResult().getTempTime());
            }
            // 驾车和地铁花费时间做对比
            if (subwayTime != 0 && subwayTime >= driveTime) {
                selectDrive = true;
            }
            int flightTime = CalculateUtil.HHmm2seconds(flight.getDepartureTime());
            if (selectDrive && (departTimeInt + driveTime > flightTime)
                    || !selectDrive && (departTimeInt + subwayTime + spendTime > flightTime)) {
                continue;
            } else {
                flightArrive = flight;
                break;
            }
        }

        if (selectDrive && (flightArrive == null || line1 == null)) {
            return null;
        }
        if (!selectDrive && (flightArrive == null || subwayStep.getResult() == null)) {
            return null;
        }

        if (!selectDrive) {
            stepList.add(subwayStep.getResult());
        } else {
            stepList.remove(stepList.size() - 1);
            stepList.add(getDriveStepWithFlight(line1, searchParam, flightArrive));
        }
        stepList.add(getFlightStep(flightArrive));

        ServiceResult<Step> subwayToTerminal = lineService.getLinesPlanning("机场中心", searchParam.getTerminal(), true);
        Step step = getDriveStepToTerminal(flightArrive, searchParam);
        if (subwayToTerminal.getResult() == null || step == null) {
            return null;
        }
        if (CalculateUtil.hourAndMinute2seconds(step.getTempTime()) <= CalculateUtil
                .hourAndMinute2seconds(subwayToTerminal.getResult().getTempTime())) {
            stepList.add(step);
        } else {
            stepList.add(subwayToTerminal.getResult());
        }

        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // 驾车+动车+飞机+驾车
    private ResultDTO RouteStrategy2(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<>();
        Double lat1 = null;
        Double lng1 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            // Test 寻找最近地铁站
            searchParam.setCurrentLatitude("39.9916376781");
            searchParam.setCurrentLongitude("116.2700219427");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
        } else {
            List<SubwayStations> subways = jedisOperate.getStationsByName(searchParam.getOrigin());
            lat1 = Double.parseDouble(subways.get(0).getStationLat());
            lng1 = Double.parseDouble(subways.get(0).getStationLng());
        }
        String departTime = sdf.format(searchParam.getDepartTime());
        List<Rail> sortedRailList = afterSortedRailList("bj", searchParam, departTime);
        if (sortedRailList == null || sortedRailList.size() <= 0) {
            return null;
        }
        // 选择列车车次
        Rail railArrive = null;
        BaiduLine line1 = null;
        for (Rail rail : sortedRailList) {
            double railLat = Double.parseDouble(rail.getDeparturePlace().getSiteLat());
            double railLng = Double.parseDouble(rail.getDeparturePlace().getSiteLng());
            line1 = BaiDuUtil.DriveRoutePlanning(lat1, lng1, railLat, railLng);
            if (line1 == null) {
                return null;
            }
            if (CalculateUtil.HHmm2seconds(departTime) + Integer.valueOf(line1.getResult().getRoutes().get(0).getDuration())
                    > CalculateUtil.HHmm2seconds(rail.getDepartureTime())) {
                continue;
            } else {
                railArrive = rail;
                break;
            }
        }
        // 选择航班
        Flight flightArrive = null;
        BaiduLine line2 = null;
        List<Flight> sortedFlightListFromsjz = afterSortedFlightList("sjz", searchParam, railArrive.getArriveTime());
        if (sortedFlightListFromsjz == null || sortedFlightListFromsjz.size() <= 0) {
            return null;
        }
        for (Flight flight : sortedFlightListFromsjz) {
            line2 = BaiDuUtil.WalkRoutePlanning(Double.valueOf(railArrive.getStopoverStation().getSiteLat()),
                    Double.valueOf(railArrive.getStopoverStation().getSiteLng()),
                    Double.valueOf(flight.getDeparturePlace().getSiteLat()),
                    Double.valueOf(flight.getDeparturePlace().getSiteLng()));

            String sptime = line2.getResult().getRoutes().get(0).getDuration();
            int tt = CalculateUtil.getSpendTime(railArrive.getArriveTime(), flight.getDepartureTime());
            if (tt < Integer.valueOf(sptime)) {
                continue;
            } else {
                flightArrive = flight;
                break;
            }
        }

        if (railArrive == null || line1 == null || flightArrive == null || line2 == null) {
            return null;
        }

        stepList.add(getDriveStepWithRail(line1, searchParam, railArrive));
        Step step = getRailStep(railArrive);
        step.setTempCost(railArrive.getBusinessSeatPrice());
        stepList.add(step);

        Step step1 = Step.builder().tempOri(railArrive.getStopoverStation().getSiteName())
                .tempTer(flightArrive.getDeparturePlace().getSiteName()).tempCost(0.0).isWalk(true)
                .way(line2.getResult().getRoutes().get(0))
                .tempTime(CalculateUtil
                        .seconds2HourAndMinute(Integer.valueOf(line2.getResult().getRoutes().get(0).getDuration())))
                .tempDistance(line2.getResult().getRoutes().get(0).getDistance()).build();
        stepList.add(step1);
        stepList.add(getFlightStep(flightArrive));

        stepList.add(getDriveStepToTerminal(flightArrive, searchParam));
        result.setSteps(stepList);
        result.setSteps(stepList);
        getToatlTimeAndCost(result);
        return result;
    }

    // 驾车+飞机+驾车
    private ResultDTO RouteStrategy1(SearchParam searchParam) {
        ResultDTO result = new ResultDTO();
        List<Step> stepList = new ArrayList<Step>();
        Double lat1 = null;
        Double lng1 = null;
        Double lat2 = null;
        Double lng2 = null;
        if (searchParam.getOrigin().equals("当前位置")) {
            // Test 寻找最近地铁站
            searchParam.setCurrentLatitude("39.9916376781");
            searchParam.setCurrentLongitude("116.2700219427");
            lat1 = Double.valueOf(searchParam.getCurrentLatitude());
            lng1 = Double.valueOf(searchParam.getCurrentLongitude());
        } else {
            List<SubwayStations> stations = jedisOperate.getStationsByName(searchParam.getOrigin());
            lat1 = Double.parseDouble(stations.get(0).getStationLat());
            lng1 = Double.parseDouble(stations.get(0).getStationLng());
        }

        String departTime = sdf.format(searchParam.getDepartTime());
        int departTimeInt = CalculateUtil.HHmm2seconds(departTime);

        List<Flight> sortedFlightList = afterSortedFlightList("bj", searchParam, departTime);
        if (sortedFlightList == null || sortedFlightList.size() <= 0) {
            return null;
        }

        // 选择航班
        Flight flightArrive = null;
        BaiduLine line = null;
        for (Flight flight : sortedFlightList) {
            // 去机场的路线
            lat2 = Double.valueOf(flight.getDeparturePlace().getSiteLat());
            lng2 = Double.valueOf(flight.getDeparturePlace().getSiteLng());
            line = BaiDuUtil.DriveRoutePlanning(lat1, lng1, lat2, lng2);
            if (line.getResult() == null) {
                return null;
            }
            int seconds = Integer.parseInt(line.getResult().getRoutes().get(0).getDuration());
            if (departTimeInt + seconds > CalculateUtil.HHmm2seconds(flight.getDepartureTime())) {
                continue;
            } else {
                flightArrive = flight;
                break;
            }
        }

        if (flightArrive == null || line == null) {
            return null;
        } else {
            stepList.add(getDriveStepWithFlight(line, searchParam, flightArrive));
            stepList.add(getFlightStep(flightArrive));
            stepList.add(getDriveStepToTerminal(flightArrive, searchParam));
            result.setSteps(stepList);
            getToatlTimeAndCost(result);
            return result;
        }
    }

    private Step getFlightStep(Flight flightArrive) {
        int tempTime = CalculateUtil.getSpendTime(flightArrive.getDepartureTime(), flightArrive.getArriveTime());
        Step step = Step.builder().tempTer(flightArrive.getDestinationPlace().getSiteName())
                .tempTime(CalculateUtil.seconds2HourAndMinute(tempTime))
                .tempCost(Double.valueOf(flightArrive.getFlightPrice()))
                .tempOri(flightArrive.getDeparturePlace().getSiteName()).way(flightArrive).isFlight(true).build();
        return step;
    }

    private Step getRailStep(Rail railArrive) {
        Step step = Step.builder().way(railArrive).isRail(true).tempOri(railArrive.getDeparturePlace().getSiteName())
                .tempTer(railArrive.getStopoverStation().getSiteName())
                .tempTime(CalculateUtil.seconds2HourAndMinute(CalculateUtil.HHmm2seconds(railArrive.getConsumeTime())))
                .tempCost(railArrive.getSecondSeatPrice()).build();
        return step;
    }

    private Step getDriveStepWithFlight(BaiduLine line, SearchParam searchParam, Flight flight) {
        return Step.builder().tempOri(searchParam.getOrigin()).tempTer(flight.getDeparturePlace().getSiteName())
                .tempCost(CalculateUtil
                        .getTaixFare(line.getResult().getRoutes().get(0).getDistance(), searchParam.getDepartTime()))
                .isDrive(true).way(line.getResult().getRoutes().get(0))
                .tempTime(CalculateUtil
                        .seconds2HourAndMinute(Integer.valueOf(line.getResult().getRoutes().get(0).getDuration())))
                .tempDistance(line.getResult().getRoutes().get(0).getDistance()).build();
    }

    private Step getDriveStepWithRail(BaiduLine line, SearchParam searchParam, Rail railArrive) {
        return Step.builder().tempOri(searchParam.getOrigin()).tempTer(railArrive.getDeparturePlace().getSiteName())
                .tempCost(CalculateUtil
                        .getTaixFare(line.getResult().getRoutes().get(0).getDistance(), searchParam.getDepartTime()))
                .isDrive(true).way(line.getResult().getRoutes().get(0))
                .tempTime(CalculateUtil
                        .seconds2HourAndMinute(Integer.valueOf(line.getResult().getRoutes().get(0).getDuration())))
                .tempDistance(line.getResult().getRoutes().get(0).getDistance()).build();
    }

    private Step getDriveStepToTerminal(Flight flightArrive, SearchParam searchParam) {
        Sites site = sitesDao.querySiteById(flightArrive.getDestinationPlace().getSiteId());
        List<SubwayStations> subways = jedisOperate.getStationsByName(searchParam.getTerminal());
        BaiduLine line = BaiDuUtil.DriveRoutePlanning(Double.parseDouble(site.getSiteLat()),
                Double.parseDouble(site.getSiteLng()), Double.parseDouble(subways.get(0).getStationLat()),
                Double.parseDouble(subways.get(0).getStationLng()));
        return Step.builder().tempOri(site.getSiteName()).tempTer(searchParam.getTerminal())
                .tempCost(CalculateUtil
                        .getTaixFare(line.getResult().getRoutes().get(0).getDistance(), searchParam.getDepartTime()))
                .isDrive(true).way(line.getResult().getRoutes().get(0))
                .tempTime(CalculateUtil
                        .seconds2HourAndMinute(Integer.valueOf(line.getResult().getRoutes().get(0).getDuration())))
                .tempDistance(line.getResult().getRoutes().get(0).getDistance()).build();

    }

    private ResultDTO getToatlTimeAndCost(ResultDTO result) {
        List<Step> stepList = result.getSteps();
        Integer totalTime = 0;
        Double totalCost = 0.0;
        for (Step step : stepList) {
            totalCost += step.getTempCost();
            totalTime += CalculateUtil.hourAndMinute2seconds(step.getTempTime());
        }
        String str = String.format("%.2f", totalCost);
        result.setTotalCost(Double.parseDouble(str));
        result.setTotalTime(CalculateUtil.seconds2HourAndMinute(totalTime));
        return result;

    }

    private List<Flight> afterSortedFlightList(String cityName, SearchParam searchParam, String departTime) {
        List<Flight> flightList = flightDao.queryFlightListOrderByDepartTime(cityName, departTime);
        if (flightList == null || flightList.size() <= 0) {
            return null;
        }
        return getFlightSortList(searchParam, flightList);
    }

    private List<Rail> afterSortedRailList(String cityName, SearchParam searchParam, String departTime) {
        List<Rail> railList = railDao.queryRailsOrderByDepartTime(cityName, departTime);
        if (railList == null || railList.size() <= 0) {
            return null;
        }
        return getRailSortList(searchParam, railList);
    }

    //航班分数与旅客分数作做相似度匹配 并排序
    private List<Flight> getFlightSortList(SearchParam searchParam, List<Flight> flightList) {
        List<Flight> resList = new ArrayList<>();
        for (Flight flight : flightList) {
            //曼哈顿距离作为相似度依据
            double tempDegree =
                    Math.abs(Double.valueOf(searchParam.getEconomicScore()) - flight.getEconomicScore()) / (100 / flight.getEsGradeNumber()) +
                            Math.abs(Double.valueOf(searchParam.getTimeScore()) - flight.getTimeScore()) / (100 / flight.getTsGradeNumber()) +
                            Math.abs(Double.valueOf(searchParam.getLoadBearingScore()) - flight.getLoadBearingScore()) / (100 / flight.getLbsGradeNumber());
            flight.setDifferenceDegree(tempDegree);
            resList.add(flight);
        }
        Collections.sort(resList, new Comparator<Flight>() {
            @Override
            public int compare(Flight o1, Flight o2) {
                if (o1.getDifferenceDegree() > o2.getDifferenceDegree()) {
                    return 1;
                }
                return -1;
            }
        });
        return resList;
    }

    //列车分数与旅客分数作做相似度匹配 并排序
    private List<Rail> getRailSortList(SearchParam searchParam, List<Rail> railList) {
        List<Rail> resList = new ArrayList<>();
        for (Rail rail : railList) {
            //曼哈顿距离作为相似度依据
            double tempDegree =
                    Math.abs(Double.valueOf(searchParam.getEconomicScore()) - rail.getEconomicScore()) / (100 / rail.getEsGradeNumber()) +
                            Math.abs(Double.valueOf(searchParam.getTimeScore()) - rail.getTimeScore()) / (100 / rail.getTsGradeNumber()) +
                            Math.abs(Double.valueOf(searchParam.getLoadBearingScore()) - rail.getLoadBearingScore()) / (100 / rail.getLbsGradeNumber());
            rail.setDifferenceDegree(tempDegree);
            resList.add(rail);
        }
        Collections.sort(resList, new Comparator<Rail>() {
            @Override
            public int compare(Rail o1, Rail o2) {
                if (o1.getDifferenceDegree() > o2.getDifferenceDegree()) {
                    return 1;
                }
                return -1;
            }
        });
        return resList;
    }

    //航班分数数据更新
    @Override
    public ServiceResult updateFlightScore(String cityName) {
        Map<Long, Flight> resMap = new HashMap<>();
        int flightCount = flightDao.queryFlightsCountByCityName(cityName);
        if (flightCount <= 0) {
            return ServiceResult.notFound();
        }
        List<Flight> flightsList = flightDao.queryFlightListByCityName(cityName);
        DecimalFormat df = new DecimalFormat(".000000");
        Multimap<Integer, Flight> multiMap = TreeMultimap.create();
        //economic_score
        int te = 1;
        flightsList.forEach(f -> multiMap.put(f.getFlightPrice(), f));
        for (Map.Entry e : multiMap.asMap().entrySet()) {
            int price = (int) e.getKey();
            List<Flight> flights = new ArrayList(multiMap.get(price));
            for (Flight f : flights) {
                f.setEsGradeNumber(multiMap.asMap().size());
                double economicScore = Double.valueOf(df.format((te / (double) multiMap.asMap().size()) * 100));
                if (resMap.get(f.getFlightId()) != null) {
                    Flight tempFlight = resMap.get(f.getFlightId());
                    tempFlight.setEconomicScore(economicScore);
                    resMap.put(f.getFlightId(), tempFlight);
                } else {
                    f.setEconomicScore(economicScore);
                    resMap.put(f.getFlightId(), f);
                }
            }
            te++;
        }
        multiMap.clear();

        //timeScore
        int ts = 1;
        flightsList.forEach(f -> multiMap.put(CalculateUtil.getSpendTime(f.getDepartureTime(), f.getArriveTime())
                + CalculateUtil.HHmm2seconds(f.getDepartureTime()), f));
        for (Map.Entry e : multiMap.asMap().entrySet()) {
            int time = (int) e.getKey();
            List<Flight> flights = new ArrayList(multiMap.get(time));
            for (Flight f : flights) {
                f.setTsGradeNumber(multiMap.asMap().size());
                double timeScore = Double.valueOf(df.format((ts / (double) multiMap.asMap().size()) * 100));
                if (resMap.get(f.getFlightId()) != null) {
                    Flight tempFlight = resMap.get(f.getFlightId());
                    tempFlight.setTimeScore(timeScore);
                    resMap.put(f.getFlightId(), tempFlight);
                } else {
                    f.setEconomicScore(timeScore);
                    resMap.put(f.getFlightId(), f);
                }
            }
            ts++;
        }
        multiMap.clear();

        //loadBearingScore
        int tlbs = 1;
        flightsList.forEach(f -> multiMap.put(f.getPunctualityRate(), f));
        for (Map.Entry e : multiMap.asMap().entrySet()) {
            int punctualityRate = (int) e.getKey();
            List<Flight> flights = new ArrayList(multiMap.get(punctualityRate));
            for (Flight f : flights) {
                f.setLbsGradeNumber(multiMap.asMap().size());
                double loadBearingScore = Double.valueOf(df.format((tlbs / (double) multiMap.asMap().size()) * 100));
                if (resMap.get(f.getFlightId()) != null) {
                    Flight tempFlight = resMap.get(f.getFlightId());
                    tempFlight.setLoadBearingScore(loadBearingScore);
                    resMap.put(f.getFlightId(), tempFlight);
                } else {
                    f.setLoadBearingScore(loadBearingScore);
                    resMap.put(f.getFlightId(), f);
                }
            }
            tlbs++;
        }
        multiMap.clear();

        //update
        for (Map.Entry<Long, Flight> entry : resMap.entrySet()) {
            try {
                int effNum = flightDao.updateFlightScore(entry.getValue());
                if (effNum < 0) {
                    return new ServiceResult(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new ServiceResult(false);
            }
        }

        return ServiceResult.success();
    }

    //列车分数数据更新
    @Override
    public ServiceResult updateRailScore(String cityName) {
        Map<Long, Rail> resMap = new HashMap<>();
        int railCount = railDao.queryRailsCountByCityName(cityName);
        if (railCount <= 0) {
            return ServiceResult.notFound();
        }

        List<Rail> railsList = railDao.queryRailsByCityName(cityName);
        DecimalFormat df = new DecimalFormat(".000000");
        Multimap<Integer, Rail> multiMap = TreeMultimap.create();

        //timeScore
        int ts = 1;
        railsList.forEach(r -> multiMap.put(CalculateUtil.HHmm2seconds(r.getConsumeTime())
                + CalculateUtil.HHmm2seconds(r.getDepartureTime()), r));
        for (Map.Entry e : multiMap.asMap().entrySet()) {
            int time = (int) e.getKey();
            List<Rail> rails = new ArrayList(multiMap.get(time));
            for (Rail r : rails) {
                r.setTsGradeNumber(multiMap.asMap().size());
                double timeScore = Double.valueOf(df.format((ts / (double) multiMap.asMap().size()) * 100));
                if (resMap.get(r.getRailId()) != null) {
                    Rail tempRail = resMap.get(r.getRailId());
                    tempRail.setTimeScore(timeScore);
                    resMap.put(r.getRailId(), tempRail);
                } else {
                    r.setTimeScore(timeScore);
                    resMap.put(r.getRailId(), r);
                }
            }
            ts++;
        }
        multiMap.clear();


        //economic_score
        Multimap<Double, Rail> multiMap4Double = TreeMultimap.create();
        int te = 1;
        railsList.forEach(r -> multiMap4Double.put((r.getSecondSeatPrice() + r.getFirstSeatPrice() + r.getBusinessSeatPrice()) / 3, r));
        for (Map.Entry e : multiMap4Double.asMap().entrySet()) {
            double price = (double) e.getKey();
            List<Rail> rails = new ArrayList(multiMap4Double.get(price));
            for (Rail rail : rails) {
                rail.setEsGradeNumber(multiMap4Double.asMap().size());
                double economicScore = Double.valueOf(df.format((te / (double) multiMap4Double.asMap().size()) * 100));
                if (resMap.get(rail.getRailId()) != null) {
                    Rail tempRail = resMap.get(rail.getRailId());
                    tempRail.setEconomicScore(economicScore);
                    resMap.put(rail.getRailId(), tempRail);
                } else {
                    rail.setEconomicScore(economicScore);
                    resMap.put(rail.getRailId(), rail);
                }
            }
            te++;
        }
        multiMap4Double.clear();

        //loadBearingScore 按照列车级别分类
        Map<String, List<Rail>> railClassificationMap = new TreeMap<>();
        for (Rail rail : railsList) {
            String railType = rail.getRailCode().substring(0, 1);
            switch (railType) {
                case "G":
                case "C":
                    classifyRailType(rail, "G/C", railClassificationMap);
                    break;
                case "D":
                    classifyRailType(rail, "D", railClassificationMap);
                case "Z":
                case "T":
                case "K":
                    classifyRailType(rail, "Z/T/K", railClassificationMap);
                    break;
                default:
                    classifyRailType(rail, "others", railClassificationMap);
            }
        }

        int tlbs = 0;
        for (Map.Entry<String, List<Rail>> entry : railClassificationMap.entrySet()) {
            switch (entry.getKey()) {
                case "G/C":
                    tlbs = 4;
                    break;
                case "D":
                    tlbs = 3;
                    break;
                case "Z/T/K":
                    tlbs = 2;
                    break;
                case "others":
                    tlbs = 1;
                    break;
            }
            for (Rail rail : entry.getValue()) {
                rail.setLbsGradeNumber(4);
                double loadBearingScore = Double.valueOf(df.format((tlbs / 4.0) * 100));
                if (resMap.get(rail.getRailId()) != null) {
                    Rail tempRail = resMap.get(rail.getRailId());
                    tempRail.setLoadBearingScore(loadBearingScore);
                    resMap.put(rail.getRailId(), tempRail);
                } else {
                    rail.setLoadBearingScore(loadBearingScore);
                    resMap.put(rail.getRailId(), rail);
                }
            }
        }

        for (Map.Entry<Long, Rail> entry : resMap.entrySet()) {
            try {
                int effNum = railDao.updateRailScore(entry.getValue());
                if (effNum < 0) {
                    return new ServiceResult(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new ServiceResult(false);
            }
        }

        return ServiceResult.success();
    }

    private void classifyRailType(Rail rail, String typeCode, Map<String, List<Rail>> railClassificationMap) {
        if (railClassificationMap.get(typeCode) != null) {
            railClassificationMap.get(typeCode).add(rail);
        } else {
            List<Rail> railList = new ArrayList<>();
            railList.add(rail);
            railClassificationMap.put(typeCode, railList);
        }
    }
}
