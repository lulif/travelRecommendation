package com.gdxx.beans;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BusRoute {
	private String distance;
	private String duration;
	private List<List<Step>> steps;
	private Integer price;
}
