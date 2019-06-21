package com.gdxx.utils;

import java.util.HashMap;
import java.util.Map;

import com.gdxx.service.impl.LineServiceImpl.Node;

import lombok.AllArgsConstructor;
/*
 * 路线算法：Dijkstra
 */
public class LineAlgorithmUtil {

	@AllArgsConstructor
	public static class Result {
		public int tt;
		public String[] stationAndLine;
	}

	public static Result dijkstra(Node[][] map, int start, int end) {

		Map<Integer, String> path = new HashMap<Integer, String>();
		int[] tempDis = new int[map.length];
		boolean[] isVis = new boolean[map.length];
		int next = start;
		for (int i = 1; i < map.length; i++) {
			tempDis[i] = map[start][i].weight;
			path.put(i, map[start][i].stationName + "_" + String.valueOf(i));
		}
		isVis[start] = true;
		for (int i = 1; i < map.length; i++) {
			int min = 0x3F3F3F3F;
			for (int j = 1; j < map.length; j++) {
				if (!isVis[j] && tempDis[j] < min) {
					next = j;
					min = tempDis[j];
				}
			}
			isVis[next] = true;
			for (int j = 1; j < map.length; j++) {
				if (!isVis[j] && tempDis[next] + map[next][j].weight < tempDis[j]) {
					tempDis[j] = tempDis[next] + map[next][j].weight;
					String p = path.get(next);
					path.put(j, p + "-" + map[next][j].stationName + "_" + String.valueOf(j));
				}
			}
		}
		String res = path.get(end);
		String[] stationAndLine = res.split("-");
		return new Result(tempDis[end], stationAndLine);
	}

}
