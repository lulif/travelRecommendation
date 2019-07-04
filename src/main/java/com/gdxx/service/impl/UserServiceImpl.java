package com.gdxx.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxx.dao.UserDao;
import com.gdxx.entity.User;
import com.gdxx.service.UserService;
import com.gdxx.service.result.ServiceResult;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserDao userDao;

	@Override
	public ServiceResult<User> getUserByOpenId(String openId) {
		return ServiceResult.of(userDao.queryUserByOpenId(openId));
	}

	@Override
	public ServiceResult<User> registerUser(User user) {
		try {
			int effNum = userDao.insertUser(user);
			if (effNum >= 1) {
				return ServiceResult.success();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("用户注册异常");
		}
		return new ServiceResult<>(false);
	}

}
