package com.gdxx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserAccessToken {
	@JsonProperty("access_token")
	private String accessToken;
	// 凭证有效时间
	@JsonProperty("expires_in")
	private String expiresIn;
	// 更新令牌
	@JsonProperty("refresh_token")
	private String refreshToken;
	// openid
	@JsonProperty("openid")
	private String openId;
	// 权限范围
	@JsonProperty("scope")
	private String scope;
	// 错误码
	@JsonProperty("errcode")
	private String errCode;
	// 错误信息
	@JsonProperty("errmsg")
	private String errMsg;
}
