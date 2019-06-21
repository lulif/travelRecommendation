package com.gdxx.config;

import java.beans.PropertyVetoException;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gdxx.utils.DESUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
/*
 * 数据库配置
 */
@Configuration
// 配置mybatis mapper的扫描路径
@MapperScan("com.gdxx.dao")
public class DataSourceConfiguration {
	@Value("${jdbc.driver}")
	private String jdbcDriver;
	@Value("${jdbc.username}")
	private String jdbcUserName;
	@Value("${jdbc.password}")
	private String jdbcPassword;
	@Value("${jdbc.url}")
	private String jdbcUrl;

	@Bean(name = "dataSource")
	public ComboPooledDataSource createDataSource() throws PropertyVetoException {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass(jdbcDriver);
		dataSource.setJdbcUrl(jdbcUrl);
		dataSource.setUser(DESUtil.getDecryptString(jdbcUserName));
		dataSource.setPassword(DESUtil.getDecryptString(jdbcPassword));
		dataSource.setIdleConnectionTestPeriod(60);
		dataSource.setInitialPoolSize(5);
		dataSource.setMaxIdleTime(60);
		dataSource.setMaxPoolSize(10);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxStatements(100);
		dataSource.setMaxStatementsPerConnection(3);
		dataSource.setPreferredTestQuery("select 1");
		dataSource.setAcquireRetryAttempts(3);
		dataSource.setAcquireRetryDelay(1000);
		dataSource.setCheckoutTimeout(3000);
		return dataSource;
	}

}
