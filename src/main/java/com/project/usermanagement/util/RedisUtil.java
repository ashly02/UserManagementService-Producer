package com.project.usermanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtil {
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public void storeDataInCache(String key,Object data){
        try{
            redisTemplate.opsForValue().set(key,data);
            redisTemplate.expire(key, Duration.ofMinutes(3));
            log.info("Data stored in redis cache with key: {}" , key);
        }catch (Exception e){
            log.error("Error storing data in redis cache", e);
            throw new RuntimeException("Error storing data in redis cache");
        }
    }
    public Object getCachedData(String key){
        try{
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                log.info("Data retrieved from Redis cache with key: {}" , key);
            }
            else {
                log.warn("No data found in Redis cache to retrieve for key: {}",key);
            }
            return data;
        } catch (Exception e) {
            log.error("Error retrieving data from Redis cache", e);
            throw new RuntimeException("Error retrieving data from Redis cache");
        }
    }

    public Object deleteCachedData(String key){
        Object data=null;
        try{
            data=redisTemplate.opsForValue().getAndDelete(key);
            if (data != null) {
                log.info("Data retrieved and deleted from Redis cache with key: {}" , key);
            }
            else{
                log.warn("No data found in Redis cache to delete for key: {}",key);
            }
        }catch (Exception e){
            log.error("Error deleting data from Redis cache", e);
            throw new RuntimeException("Error deleting data from Redis cache");
        }
        return data;
    }
}
