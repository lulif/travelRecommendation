package com.gdxx.controller.wechat;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gdxx.utils.HttpServletRequestUtil;

import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
/*
 * 微信地理位置获取
 */
@Controller
@RequestMapping("/wechatlocation")
public class WechatLocationController {
	@Autowired
	private WxMpService wxMpService;

	@Autowired
	private WxMpConfigStorage configStorage;

	@RequestMapping(value = "/getticket",method=RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> getTicket(HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		String url = HttpServletRequestUtil.getString(request, "url");
		try {
			// 获取微信signature
			WxJsapiSignature sin = wxMpService.createJsapiSignature(url);
			modelMap.put("appId", configStorage.getAppId());
			modelMap.put("timestamp", sin.getTimestamp());
			modelMap.put("nonceStr", sin.getNoncestr());
			modelMap.put("signature", sin.getSignature());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return modelMap;
	}

}