package com.gdxx.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vehicle {
	private String name;
	private String direction_text;
	private String start_name;
	private String end_name;
	private String start_time;
	private String end_time;
	private Integer stop_num;
	private Integer total_price;
}
