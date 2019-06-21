package com.gdxx.controller.wechat;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gdxx.beans.WechatUser;
import com.gdxx.dto.UserAccessToken;
import com.gdxx.entity.User;
import com.gdxx.service.UserService;
import com.gdxx.utils.WechatUserUtil;
/*
 * 微信授权登陆
 */
@Controller
@RequestMapping("/wechatlogin")
public class WechatLoginController {
	@Autowired
	UserService userService;

	@RequestMapping(value = "/logincheck", method = { RequestMethod.GET })
	public String doGet(HttpServletRequest request, HttpServletResponse response) {
		String code = request.getParameter("code");
		String openId = null;
		WechatUser wechatUser = null;
		User user = null;
		if (code != null) {
			UserAccessToken token;
			try {
				token = WechatUserUtil.getUserAccessToken(code);
				String accessToken = token.getAccessToken();
				openId = token.getOpenId();
				wechatUser = WechatUserUtil.getUserInfo(accessToken, openId);
				request.getSession().setAttribute("openId", openId);
				user = userService.getUserByOpenId(openId).getResult();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (user == null) {
			user = WechatUserUtil.getPersonInfoFromRequest(wechatUser);
			user.setOpenId(openId);
			user.setCreateTime(new Date());
			userService.registerUser(user);
		}
		request.getSession().setAttribute("user", user);
		return "index";
	}

}
