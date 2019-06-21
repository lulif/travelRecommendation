package com.gdxx.service;

import com.gdxx.entity.User;
import com.gdxx.service.result.ServiceResult;

public interface UserService {
	// 根据openId获取用户
	ServiceResult<User> getUserByOpenId(String openId);

	// 用户注册
	ServiceResult<User> registerUser(User user);

}
