package com.gdxx.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
	private Long userId;
	private String openId;
	private String profileImg;
	private String nickName;
	private Date createTime;
}
