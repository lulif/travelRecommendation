package com.gdxx.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
/*
 * mybatis sqlsession
 */
@Configuration
public class SqlSessionFactoryConfiguration {
	@Autowired
	public DataSource dataSource;

	private static String mapperPath;

	@Value("${mapper_path}")
	public  void setMapperPath(String mapperPath) {
		SqlSessionFactoryConfiguration.mapperPath = mapperPath;
	}

	private static String mybatisConfigFile;

	@Value("${mybatis_config_file}")
	public  void setMapperConfigFile(String mybatisConfigFile) {
		SqlSessionFactoryConfiguration.mybatisConfigFile = mybatisConfigFile;
	}

	@Value("${type_alias_package}")
	private String typeAliasPackage;

	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactoryBean createSqlSessionFactoryBean() throws IOException {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		// 设置mybatis configuration扫描路径
		sqlSessionFactoryBean.setConfigLocation(new ClassPathResource(mybatisConfigFile));
		// 添加mapper扫描路径
		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + mapperPath;
		sqlSessionFactoryBean.setMapperLocations(pathMatchingResourcePatternResolver.getResources(packageSearchPath));
		// 设置DataSource
		sqlSessionFactoryBean.setDataSource(dataSource);
		// 设置typeAlias包扫描路径
		sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasPackage);

		return sqlSessionFactoryBean;
	}

}
