package com.gdxx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
/*
 * wechat配置
 */
@Configuration
public class WechatConfig {
		 
	    @Value("${wechat.appid}")
	    public String appid;
	 
	    @Value("${wechat.appsecret}")
	    public String appsecret;
	 
	    @Value("${wechat.token}")
	    public String token;
	   
	    protected WechatConfig(){}
	    
	    protected WechatConfig(String appid, String appsecret, String token) {
	        this.appid = appid;
	        this.appsecret = appsecret;
	        this.token = token;
	    }
	 
	    @Bean
	    public WxMpConfigStorage wxMpConfigStorage() {
	        WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
	        configStorage.setAppId(this.appid);
	        configStorage.setSecret(this.appsecret);
	        configStorage.setToken(this.token);
	        return configStorage;
	    }
	 
	    @Bean
	    public WxMpService wxMpService() {
	        WxMpService wxMpService = new WxMpServiceImpl();
	        wxMpService.setWxMpConfigStorage(wxMpConfigStorage());
	        return wxMpService;
	    }
	 
}
