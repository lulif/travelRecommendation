package com.gdxx.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxx.base.BaiduLine;
import com.gdxx.dao.SubwayLinesDao;
import com.gdxx.dao.SubwayScheduleDao;
import com.gdxx.dao.SubwayTransferDao;
import com.gdxx.dto.Step;
import com.gdxx.dto.SubwayLinePlanning;
import com.gdxx.entity.SubwayLines;
import com.gdxx.entity.SubwayStations;
import com.gdxx.entity.SubwayTransfer;
import com.gdxx.service.JedisOperationService;
import com.gdxx.service.LineService;
import com.gdxx.service.result.ServiceResult;
import com.gdxx.utils.BaiDuUtil;
import com.gdxx.utils.CalculateUtil;
import com.gdxx.utils.LineAlgorithmUtil;
import com.gdxx.utils.LineAlgorithmUtil.Result;

import lombok.AllArgsConstructor;

@Service
public class LineServiceImpl implements LineService {
	// 无/有向图 节点
	@AllArgsConstructor
	public static class Node {
		public String stationName;
		public int weight;
	}

	@Autowired
	private SubwayTransferDao subwayTransferDao;
	@Autowired
	private SubwayScheduleDao subwayScheduleDao;
	@Autowired
	private JedisOperationService jedisOperation;
	@Autowired
	private SubwayLinesDao lineDao;

	@Override
	// 返回时间为 分钟
	public ServiceResult<Step> getLinesPlanning(String origin, String terminal, boolean isMinTime) {
		List<SubwayStations> stationOriList = jedisOperation.getStationsByName(origin);
		List<SubwayStations> stationTerList = jedisOperation.getStationsByName(terminal);

		double lat1 = Double.parseDouble(stationOriList.get(0).getStationLat());
		double lng1 = Double.parseDouble(stationOriList.get(0).getStationLng());
		double lat2 = Double.parseDouble(stationTerList.get(0).getStationLat());
		double lng2 = Double.parseDouble(stationTerList.get(0).getStationLng());

		double distance = CalculateUtil.getDistance(lat1, lng1, lat2, lng2);
		double cost = CalculateUtil.getSubwayFare(distance);

		SubwayLinePlanning lp = null;
		lp = isOnSameLine(origin, terminal, stationOriList, stationTerList);
		if (lp == null) {
			lp = getLine4Min(stationOriList, stationTerList, isMinTime);
		}

		if (lp.getPassSitesList() != null && (lp.getTotalSpendTime() > 0 || lp.getTransitTimes() != 0)) {
			Step step = Step.builder().isSubway(true)
					.tempTime(CalculateUtil.seconds2HourAndMinute(lp.getTotalSpendTime())).tempOri(origin)
					.tempTer(terminal).tempDistance(String.valueOf(distance)).tempCost(cost).subwayLinePlanning(lp)
					.build();
			return ServiceResult.of(step);
		}
		return ServiceResult.notFound();
	}

	// 狄忒线路方向是否正确
	private boolean isRightDirection(SubwayStations so, SubwayStations st, Integer lineId) {
		List<SubwayStations> line = jedisOperation.getStationsByLineId(lineId);
		boolean flagOri = false;
		boolean flagTer = false;
		boolean fail = false;
		for (SubwayStations s : line) {
			if (s.getStationName().equals(so.getStationName())) {
				flagOri = true;
			}
			if (s.getStationName().equals(st.getStationName())) {
				flagTer = true;
			}
			if (flagTer && !flagOri) {
				fail = true;
				break;
			}
		}
		if (fail)
			return false;
		return true;
	}

	// 判断两站是否在同一线路上
	private SubwayLinePlanning isOnSameLine(String origin, String terminal, List<SubwayStations> stationOriList,
			List<SubwayStations> stationTerList) {
		for (SubwayStations so : stationOriList) {
			for (SubwayStations st : stationTerList) {
				if (so.getLine().getLineId() == st.getLine().getLineId()) {
					if (isRightDirection(so, st, so.getLine().getLineId())) {
						SubwayLinePlanning lp = getLineWithSameLine(so, st, so.getLine().getLineId());
						return lp;
					}
				}
			}
		}
		return null;
	}

	// 起点,终点在同一条地铁线路上
	private SubwayLinePlanning getLineWithSameLine(SubwayStations stationOri, SubwayStations stationTer,
			Integer lineId) {
		SubwayLinePlanning subwayLinePlanning = new SubwayLinePlanning();
		List<SubwayStations> passSitesList = new ArrayList<SubwayStations>();
		List<SubwayLines> subwayLineList = new ArrayList<SubwayLines>();
		SubwayLines line = lineDao.querySubwayLinesByLineId(lineId);
		subwayLineList.add(line);
		List<SubwayStations> list = jedisOperation.getStationsByLineId(lineId);
		boolean flag = false;
		// 防止环线
		boolean ok = false;
		String timeOri = "";
		String timeTer = "";
		for (SubwayStations station : list) {
			String stationName = station.getStationName().trim();
			if (stationOri.getStationName().equals(stationName)) {
				timeOri = subwayScheduleDao
						.querySubwayScheduleByNameAndId(stationName, stationOri.getLine().getLineId())
						.getFirstCarTime();
				flag = true;
			}
			if (stationTer.getStationName().equals(stationName)) {
				timeTer = subwayScheduleDao
						.querySubwayScheduleByNameAndId(stationName, stationOri.getLine().getLineId())
						.getFirstCarTime();
				passSitesList.add(station);
				flag = false;
				ok = true;
			}
			if (flag && !ok) {
				passSitesList.add(station);
			}
		}
		subwayLinePlanning.setPassSubwayLineList(subwayLineList);
		subwayLinePlanning.setTotalSpendTime(CalculateUtil.getSpendTime(timeOri, timeTer));
		subwayLinePlanning.setPassSitesList(passSitesList);
		return subwayLinePlanning;
	}

	// 起点，终点不在同一条地铁路线上
	private SubwayLinePlanning getLine4Min(List<SubwayStations> stationOriList, List<SubwayStations> stationTerList,
			boolean isTimeMin) {
		List<SubwayTransfer> subwayTransferList = jedisOperation.getTransferList();
		SubwayLinePlanning slp = new SubwayLinePlanning();
		List<SubwayStations> passSitesList = new ArrayList<SubwayStations>();
		List<SubwayLines> subwayLineList = new ArrayList<SubwayLines>();
		
		// 图的初始化
		int maxSize = subwayTransferList.get(subwayTransferList.size() - 1).getTransferId();
		Node[][] subwayTransferMap = new Node[maxSize + 1][maxSize + 1];
		for (int i = 1; i < subwayTransferMap.length; i++)
			for (int j = 1; j < subwayTransferMap.length; j++) {
				subwayTransferMap[i][j] = new Node("", 0x3F3F3F3F);
			}
		if (!isTimeMin) {
			for (SubwayTransfer st : subwayTransferList) {
				subwayTransferMap[st.getLineFrom()][st.getLineTo()] = new Node(st.getStationName(), 1);
			}
		} else {
			for (SubwayTransfer st : subwayTransferList) {
				String time = st.getSpendTime();
				String[] tt = time.split(":");
				int realTime = Integer.valueOf(tt[0]) * 60 + Integer.valueOf(tt[1]);
				subwayTransferMap[st.getLineFrom()][st.getLineTo()] = new Node(st.getStationName(), realTime);
			}
		}

		
		int minTransfer = 0x3F3F3F3F;
		//纵横交错的线路中 两站点 最少换乘/最小换乘时间 结果集
		Result result = null;
		SubwayStations ori = null;
		SubwayStations ter = null;
		
        //遍历起始站点,目的站点所在的各条线路 寻找最优结果集
		for (SubwayStations so : stationOriList) {
			for (SubwayStations st : stationTerList) {
				int from = so.getLine().getLineId();
				int to = st.getLine().getLineId();
				Result r = LineAlgorithmUtil.dijkstra(subwayTransferMap, from, to);
				if (r.tt < minTransfer) {
					ori = so;
					ter = st;
					minTransfer = r.tt;
					result = r;
				}
			}
		}
		// 最少换乘路线 换乘所消耗的时间
		int tt4MinTransfer = 0;
		String cityName = ori.getCityName();
		if (result != null) {
			for (String s : result.stationAndLine) {
				String[] tempStr = s.split("_");
				SubwayStations ss = new SubwayStations();
				SubwayLines line = lineDao.querySubwayLinesByLineId(Integer.valueOf(tempStr[1]));
				ss.setStationName(tempStr[0]);
				ss.setLine(line);
				int lineFrom = ori.getLine().getLineId();

				if (!isTimeMin) {
					SubwayTransfer transfer = subwayTransferDao.querySubwayTransferByNameAndFromTo(tempStr[0], lineFrom,
							Integer.valueOf(tempStr[1]));
					tt4MinTransfer += CalculateUtil.MMss2seconds(transfer.getSpendTime());
				}

				SubwayLinePlanning tempPlanning = null;
				String lineName = ori.getLine().getLineName();
				String[] str = lineName.split("\\(");
				List<SubwayLines> lines = lineDao.querySubwayLineByLineName(str[0], cityName);
				for (SubwayLines sl : lines) {
					if (isRightDirection(ori, ss, sl.getLineId())) {
						tempPlanning = getLineWithSameLine(ori, ss, sl.getLineId());
						break;
					}
				}

				tempPlanning.getPassSitesList().forEach(sites -> passSitesList.add(sites));
				tempPlanning.getPassSubwayLineList().forEach(l -> subwayLineList.add(l));
				slp.setTotalSpendTime(slp.getTotalSpendTime() + tempPlanning.getTotalSpendTime());
				// 两条路线做分割
				passSitesList.add(null);
				subwayLineList.add(null);
				ori = ss;
			}

			SubwayLinePlanning tempPlanningEnd = null;
			String lineName = ori.getLine().getLineName();
			String[] str = lineName.split("\\(");
			List<SubwayLines> lines = lineDao.querySubwayLineByLineName(str[0], cityName);
			for (SubwayLines sl : lines) {
				if (isRightDirection(ori, ter, sl.getLineId())) {
					tempPlanningEnd = getLineWithSameLine(ori, ter, sl.getLineId());
					break;
				}
			}
			tempPlanningEnd.getPassSitesList().forEach(sites -> passSitesList.add(sites));
			tempPlanningEnd.getPassSubwayLineList().forEach(l -> subwayLineList.add(l));
			slp.setTotalSpendTime(slp.getTotalSpendTime() + tempPlanningEnd.getTotalSpendTime());

			if (isTimeMin) {
				slp.setTotalSpendTime(slp.getTotalSpendTime() + result.tt);
			} else {
				slp.setTotalSpendTime(slp.getTotalSpendTime() + tt4MinTransfer);
			}
			slp.setPassSitesList(passSitesList);
			slp.setPassSubwayLineList(subwayLineList);
		} else {
			System.out.println("地铁无法直接到达");
		}
		return slp;
	}

	@Override
	public ServiceResult<Step> getBaiduLine(Double locationLat, Double locationLng, Date departTime, String ori,
			String ter) {
		SubwayStations nearestStation = CalculateUtil.findNearestSubwayStation(locationLat, locationLng);
		Double nearestLat = Double.valueOf(nearestStation.getStationLat());
		Double nearestLng = Double.valueOf(nearestStation.getStationLng());

		Step step = Step.builder().build();

		if (ter == null) {
			step = step.toBuilder().tempOri(ori).tempTer(nearestStation.getStationName()).build();
		} else {
			step = step.toBuilder().tempTer(ter).tempOri(nearestStation.getStationName()).build();
		}

		BaiduLine line = BaiDuUtil.WalkRoutePlanning(locationLat, locationLng, nearestLat, nearestLng);
		if (line.getResult().getRoutes().size() > 0) {
			int distance = Integer.valueOf(line.getResult().getRoutes().get(0).getDistance());
			// 距离大于1500m 选择驾车
			if (distance > 1500) {
				line = BaiDuUtil.DriveRoutePlanning(locationLat, locationLng, nearestLat, nearestLng);
				String disStr = line.getResult().getRoutes().get(0).getDistance();
				step = step.toBuilder().tempCost(CalculateUtil.getTaixFare(disStr, departTime)).isDrive(true).build();
			} else {
				step = step.toBuilder().tempCost(0.00).build();
			}
			step = step.toBuilder().way(line.getResult().getRoutes().get(0))
					.tempTime(CalculateUtil
							.seconds2HourAndMinute(Integer.valueOf(line.getResult().getRoutes().get(0).getDuration())))
					.tempDistance(line.getResult().getRoutes().get(0).getDistance()).build();
			return ServiceResult.of(step);
		} else {
			return ServiceResult.notFound();
		}
	}

}
