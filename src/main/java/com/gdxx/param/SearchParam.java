package com.gdxx.param;

import java.util.Date;

import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/*
 * 接收前台传送过来的数据
 */
@Getter
@Setter
@ToString
public class SearchParam {
	private String currentLatitude;
	private String currentLongitude;
	@NotBlank(message = "出发地不可以为空")
	private String origin;
	@NotBlank(message = "目的地不可以为空")
	private String terminal;
	private String departWay;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date departTime;
	private String economicScore;
	private String timeScore;
	private String loadBearingScore;
}
