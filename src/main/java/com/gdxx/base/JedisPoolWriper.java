package com.gdxx.base;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/*
 * jedisPool生成
 */
@Setter
@Getter
public class JedisPoolWriper {
	private JedisPool jedisPool;

	public JedisPoolWriper(final JedisPoolConfig poolConfig, final String host, final int port) {
		try {
			jedisPool = new JedisPool(poolConfig, host, port,10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
