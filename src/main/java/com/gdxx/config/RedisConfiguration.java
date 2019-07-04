package com.gdxx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gdxx.base.JedisPoolWriper;
import com.gdxx.utils.JedisUtil;

import redis.clients.jedis.JedisPoolConfig;
/*
 * redis配置
 */
@Configuration
public class RedisConfiguration {
	@Value("${redis.pool.maxTotal}")
	private int maxTotal;
	@Value("${redis.pool.maxIdle}")
	private int maxIdle;
	@Value("${redis.pool.maxWait}")
	private long maxWaitMillis;
	@Value("${redis.pool.testOnBorrow}")
	private boolean testOnBorrow;
	@Value("${redis.hostname}")
	private String hostName;
	@Value("${redis.port}")
	private int port;

	@Autowired
	private JedisPoolConfig jedisPoolConfig;

	@Autowired
	private JedisPoolWriper jedisPoolWriper;

	@Autowired
	private JedisUtil jedisUtil;

	@Bean(name = "jedisPoolConfig")
	public JedisPoolConfig createJedisPoolConfig() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(maxTotal);
		jedisPoolConfig.setMaxIdle(maxIdle);
		jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
		jedisPoolConfig.setTestOnBorrow(testOnBorrow);
		return jedisPoolConfig;
	}

	@Bean(name = "jedisPoolWriper")
	public JedisPoolWriper createJedisPoolWriper() {
		JedisPoolWriper jedisPoolWriper = new JedisPoolWriper(jedisPoolConfig, hostName, port);
		return jedisPoolWriper;
	}

	@Bean(name = "jedisUtil")
	public JedisUtil createJedisUtil() {
		JedisUtil jedisUtil = new JedisUtil();
		jedisUtil.setJedisPool(jedisPoolWriper);
		return jedisUtil;
	}

	@Bean(name = "jedisKeys")
	public JedisUtil.Keys createJedisKeys() {
		JedisUtil.Keys jedisKeys = jedisUtil.new Keys();
		return jedisKeys;
	}

	@Bean(name = "jedisStrings")
	public JedisUtil.Strings createJedisStrings() {
		JedisUtil.Strings jedisStrings = jedisUtil.new Strings();
		return jedisStrings;
	}
}
