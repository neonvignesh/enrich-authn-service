package com.enrich.authn.util.external;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class CacheUtil {
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public boolean set(String key, String object,int TTL) {
		boolean cacheStatus = false;		
		try {
			if(null != object && getConnection()) {
				redisTemplate.opsForValue().set(key, object);
				cacheStatus = true;
				redisTemplate.expire(key,TTL, TimeUnit.SECONDS); // Cache Timeout 6 Hours (21600 Seconds)
			}
		} catch (Exception e) {
			log.error("Error occured in CacheUtil Set Method {} ", e);
		}
		return cacheStatus;
	}
	
	public Object get(String key) {
		Object object = null;
		try {
			if(null != key && getConnection()) {
				object = redisTemplate.opsForValue().get(key);
			}
		} catch (Exception e) {
			log.error("Error occured in CacheUtil Get Method {} ", e);
		}
		return object;
	}  
	   public boolean delete(String key) {
	        boolean deleteStatus = false;
	        try {
	            if (null != key && getConnection()) {
	                deleteStatus = redisTemplate.delete(key);
	            }
	        } catch (Exception e) {
	            log.error("Error occurred in CacheUtil Delete Method {}", e);
	        }
	        return deleteStatus;
	    }
	/**
	 * Check the Redis Connection and pass true/false flag
	 * @return boolean
	 */
	private boolean getConnection() {
		boolean connectionStatus = false;

		RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();

		if(null != redisConnection) {
			connectionStatus = true;
		}
		return connectionStatus;
	}
	
	public ListOperations<String, Object> opsForList() {
		// TODO Auto-generated method stub
		return null;
	}
}