package com.gdxx.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxx.beans.BaiduLine;
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
		int ecScore = convertScore(searchParam.getEconomicScore());
		int timeScore = convertScore(searchParam.getTimeScore());
		int loadBearingScore = convertScore(searchParam.getLoadBearingScore());
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
			int flag = ecScore < timeScore ? 1 : 2;
			resultDTO = RouteStrategy8(searchParam, flag);
			if (resultDTO != null) {
				return ServiceResult.of(resultDTO);
			}
			return ServiceResult.notFound();
		}
		return ServiceResult.notFound();
	}

	// 地铁(最小换乘是时间)+飞机(准点率)/飞机(实惠)+地铁(最少换乘次数)
	private ResultDTO RouteStrategy8(SearchParam searchParam, int flag) {
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
		List<Flight> flightList = null;
		try {
			if (flag == 1) {
				flightList = flightDao.queryFlightOrderByPrice("bj", departTime);
			} else {
				flightList = flightDao.queryFlightList("bj", departTime);
			}
		} catch (Exception e) {
			return null;
		}
		// 选择航班
		Flight flightArrive = null;
		for (Flight flight : flightList) {
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

	// 地铁(最少换乘次数)+飞机(准点率+实惠)+地铁(最少换乘次数)
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
		List<Flight> flightList = null;
		try {
			flightList = flightDao.queryFlightOrderByRateAndPrice("bj", departTime);
		} catch (Exception e) {
			return null;
		}
		// 选择航班
		Flight flightArrive = null;
		for (Flight flight : flightList) {
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

	// 地铁(最少换乘次数)+动车+飞机(实惠)+地铁(最少换乘次数)
	private ResultDTO RouteStrategy6(SearchParam searchParam) {
		ResultDTO result = new ResultDTO();
		List<Step> stepList = new ArrayList<Step>();
		String subwayOri = null;
		int spendTime = 0;
		Double lat1 = null;
		Double lng1 = null;
		if (searchParam.getOrigin().equals("当前位置")) {
			// Test 寻找最近地铁站(换其他坐标可能会有bug)
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
		List<Rail> railList = null;
		try {
			railList = railDao.queryRailList("bj", departTime);
		} catch (Exception e) {
			return null;
		}
		// 选择列车车次
		Rail railArrive = null;
		ServiceResult<Step> subwayStep = null;
		for (Rail rail : railList) {
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
		List<Flight> flights = null;
		try {
			flights = flightDao.queryFlightOrderByPrice("sjz", railArrive.getArriveTime());
		} catch (Exception e) {
			return null;
		}
		for (Flight flight : flights) {
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

	// 打车+飞机(耗时最少)+打车
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
		List<Flight> flightList = null;
		try {
			flightList = flightDao.queryFlightList("bj", departTime);
		} catch (Exception e) {
			return null;
		}
		Collections.sort(flightList, new Comparator<Flight>() {
			@Override
			public int compare(Flight o1, Flight o2) {
				int o1Time = CalculateUtil.getSpendTime(o1.getDepartureTime(), o1.getArriveTime());
				int o2Time = CalculateUtil.getSpendTime(o2.getDepartureTime(), o2.getArriveTime());
				if (CalculateUtil.HHmm2seconds(o1.getDepartureTime()) < CalculateUtil
						.HHmm2seconds(o2.getDepartureTime()) && o1Time < o2Time) {
					return -1;
				}
				return 1;
			}
		});
		// 选择航班
		Flight flightArrive = null;
		BaiduLine line = null;
		for (Flight flight : flightList) {
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

	// [地铁(最少换乘次数)+飞机(实惠+准点率)]/[动车+飞机](费用权衡) +地铁(最少换乘次数)
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
		List<Flight> flightList = null;
		try {
			flightList = flightDao.queryFlightOrderByPriceAndRate("bj", departTime);
		} catch (Exception e) {
			return null;
		}
		// 选择航班
		Flight flightArrive1 = null;
		ServiceResult<Step> subwayStep1 = null;
		for (Flight flight : flightList) {
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
		List<Rail> railList = null;
		try {
			railList = railDao.queryRailList("bj", departTime);
		} catch (Exception e) {
			return null;
		}
		ServiceResult<Step> subwayStep2 = null;
		Rail railArrive = null;
		Step railToFlight = null;
		Flight flightArrive2 = null;
		spendTime = 0;
		for (Rail rail : railList) {
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
		List<Flight> flights = null;
		try {
			flights = flightDao.queryFlightList("sjz", railArrive.getArriveTime());
		} catch (Exception e) {
			return null;
		}
		for (Flight flight1 : flights) {
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

	// [驾车]/[地铁(最小换乘时间)](时间权衡) +飞机(耗时最少+准点率) +[驾车]/[地铁((最小换乘时间))](时间权衡)
	private ResultDTO RouteStrategy3(SearchParam searchParam) {
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
		List<Flight> flightList = null;
		try {
			flightList = flightDao.queryFligtOrderByRate("bj", departTime);
		} catch (Exception e) {
			return null;
		}

		Collections.sort(flightList, new Comparator<Flight>() {
			@Override
			public int compare(Flight o1, Flight o2) {
				int o1Time = CalculateUtil.getSpendTime(o1.getDepartureTime(), o1.getArriveTime());
				int o2Time = CalculateUtil.getSpendTime(o2.getDepartureTime(), o2.getArriveTime());
				if (CalculateUtil.HHmm2seconds(o1.getDepartureTime()) < CalculateUtil
						.HHmm2seconds(o2.getDepartureTime()) && o1Time < o2Time) {
					if (o1.getPunctualityRate() > o2.getPunctualityRate()) {
						return -1;
					} else {
						return 0;
					}
				}
				return 1;
			}
		});

		// 选择航班
		Flight flightArrive = null;
		BaiduLine line1 = null;
		ServiceResult<Step> subwayStep = null;
		boolean selectDrive = false;
		for (Flight flight : flightList) {
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
		List<Step> stepList = new ArrayList<Step>();
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
		List<Rail> railList = null;
		try {
			railList = railDao.queryRailList("bj", departTime);
		} catch (Exception e) {
			return null;
		}
		// 选择列车车次
		Rail railArrive = null;
		BaiduLine line1 = null;
		for (Rail rail : railList) {
			double railLat = Double.parseDouble(rail.getDeparturePlace().getSiteLat());
			double railLng = Double.parseDouble(rail.getDeparturePlace().getSiteLng());
			line1 = BaiDuUtil.DriveRoutePlanning(lat1, lng1, railLat, railLng);
			if (line1 == null) {
				return null;
			}
			if (CalculateUtil.HHmm2seconds(departTime)
					+ Integer.valueOf(line1.getResult().getRoutes().get(0).getDuration()) > CalculateUtil
							.HHmm2seconds(rail.getDepartureTime())) {
				continue;
			} else {
				railArrive = rail;
				break;
			}
		}
		// 选择航班
		Flight flightArrive = null;
		BaiduLine line2 = null;
		List<Flight> flights = null;
		try {
			flights = flightDao.queryFlightList("sjz", railArrive.getArriveTime());
		} catch (Exception e) {
			return null;
		}
		for (Flight flight : flights) {
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

	// 驾车+飞机(准点率)+驾车
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
		List<Flight> flightList = null;
		try {
			flightList = flightDao.queryFligtOrderByRate("bj", departTime);
		} catch (Exception e) {
			return null;
		}

		// 选择航班
		Flight flightArrive = null;
		BaiduLine line = null;
		for (Flight flight : flightList) {
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

	public static int convertScore(String score) {
		return Integer.valueOf(score.substring(0, score.length() - 1));
	}
}
