package com.gdxx.base;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
/*
 * 微信用户实体
 */
@Setter
@Getter
public class WechatUser {

	@JsonProperty("openid")
	private String openId;

	@JsonProperty("nickname")
	private String nickName;

	@JsonProperty("sex")
	private int sex;

	@JsonProperty("province")
	private String province;

	@JsonProperty("city")
	private String city;

	@JsonProperty("country")
	private String country;

	@JsonProperty("headimgurl")
	private String headimgurl;

	@JsonProperty("language")
	private String language;

	@JsonProperty("privilege")
	private String[] privilege;
}
