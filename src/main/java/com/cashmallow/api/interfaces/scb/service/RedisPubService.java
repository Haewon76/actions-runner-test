package com.cashmallow.api.interfaces.scb.service;

import com.cashmallow.api.interfaces.scb.model.dto.InboundMessage;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.cashmallow.api.config.RedisConfig.SCB_INBOUND_TOPIC;

@Service
@RequiredArgsConstructor
public class RedisPubService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Gson gson;

    public void sendMessage(InboundMessage chatMessage) {
        redisTemplate.convertAndSend(SCB_INBOUND_TOPIC, gson.toJson(chatMessage));
    }
}