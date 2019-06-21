package com.gdxx.dao;

import com.gdxx.entity.User;
/*
 * 用户Dao
 */
public interface UserDao {
	User queryUserByOpenId(String openId);

	int insertUser(User user);
}
