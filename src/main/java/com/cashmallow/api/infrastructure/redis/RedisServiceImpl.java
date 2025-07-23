package com.cashmallow.api.infrastructure.redis;

import com.cashmallow.api.infrastructure.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void put(String key, String value, long minutes) {
        put(key, value, minutes, TimeUnit.MINUTES);
    }

    @Override
    public void put(String key, String value, long time, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set(key, value, time, timeUnit);
    }

    @Override
    public void put(String key, String value) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set(key, value);
    }

    @Override
    public String get(String key) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        return valueOps.get(key);
    }

    @Override
    public boolean contains(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Boolean remove(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public String generateRedisKey(String object, String id, String property) {
        return String.format("%s:%s:%s", object, id, property);
    }

    @Override
    public Long increaseAndGetCount(String key, Long value) {
        // 20231010
        // 00002 (5자리)
        return redisTemplate.opsForHash().increment(key, key, value);
    }

    @Override
    public void setTimeout(String key, int seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void put(String type, String key, String value, long time, TimeUnit timeUnit) {
        String s = type + key;
        put(s, value, time, timeUnit);
    }

    @Override
    public boolean isMatch(String type, String key, String value) {
        final String redisKey = type + key;
        if (!this.contains(redisKey)) {
            return false;
        }

        final String redisValue = get(redisKey);

        boolean result = StringUtils.equals(redisValue, value);
        remove(redisKey);

        return result;
    }

    @Override
    public boolean putIfAbsent(String key, String value, long time, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, time, timeUnit));
    }
}
