package com.gdxx.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
/*
 * mvc配置
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableWebMvc 
public class MvcConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/*
	 * 静态资源
	 */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/*
	 * viewResolver
	 */
	@Bean(name = "viewResolver")
	public InternalResourceViewResolver createInternalResourceViewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setApplicationContext(applicationContext);
		viewResolver.setCache(false);
		viewResolver.setPrefix("/WEB-INF/html/");
		viewResolver.setSuffix(".html");
		return viewResolver;
	}
}
