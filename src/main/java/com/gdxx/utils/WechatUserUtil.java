package com.gdxx.utils;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdxx.beans.WechatUser;
import com.gdxx.dto.UserAccessToken;
import com.gdxx.entity.User;

import me.chanjar.weixin.mp.api.WxMpConfigStorage;
/*
 * wechat第三方接口获取用户信息和地理位置
 */
@Component
public class WechatUserUtil {

	private static WxMpConfigStorage wxMpConfigStorage;

	@Autowired
	public void setWxMpConfigStorage(WxMpConfigStorage wxMpConfigStorage) {
		WechatUserUtil.wxMpConfigStorage = wxMpConfigStorage;
	}

	public static UserAccessToken getUserAccessToken(String code) throws IOException {
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + wxMpConfigStorage.getAppId()
				+ "&secret=" + wxMpConfigStorage.getSecret() + "&code=" + code + "&grant_type=authorization_code";
		// 向相应URL发送请求获取token json字符串
		String tokenStr = HttpsRequestUtil.httpsRequest(url, "GET", null);
		System.out.println("tokenStr:"+tokenStr);
		UserAccessToken token = new UserAccessToken();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			token = objectMapper.readValue(tokenStr, UserAccessToken.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (token == null) {
			return null;
		}
		return token;
	}

	public static WechatUser getUserInfo(String accessToken, String openId) {
		// 根据传入的accessToken以及openId拼接出访问微信定义的端口并获取用户信息的URL
		String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId
				+ "&lang=zh_CN";
		// 访问该URL获取用户信息json 字符串
		String userStr = HttpsRequestUtil.httpsRequest(url, "GET", null);
		WechatUser user = new WechatUser();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// 将json字符串转换成相应对象
			user = objectMapper.readValue(userStr, WechatUser.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (user == null) {
			return null;
		}
		return user;
	}

	public static User getPersonInfoFromRequest(WechatUser wechatUser) {
		User user = new User();
		user.setProfileImg(wechatUser.getHeadimgurl());
		user.setNickName(wechatUser.getNickName());
		return user;
	}

}
