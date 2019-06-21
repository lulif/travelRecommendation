package com.gdxx.controller.wechat;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import me.chanjar.weixin.mp.api.WxMpService;

@Controller
@RequestMapping("/wechat")
public class WechatController {

	@Autowired
	private WxMpService wxMpService;

	@RequestMapping(method = { RequestMethod.GET })
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		PrintWriter out = null;
		try {
			if (wxMpService.checkSignature(timestamp, nonce, signature)) {
				response.getWriter().print(echostr);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}
}
