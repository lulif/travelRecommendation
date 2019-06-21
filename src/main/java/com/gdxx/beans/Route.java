package com.gdxx.beans;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Route {
	private String distance;
	private String duration;
	private List<Step> steps;
}
