package com.cashmallow.api.interfaces.scb.service;

import com.cashmallow.api.interfaces.scb.model.dto.InboundMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubService implements MessageListener {
    public static List<String> messageList = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebNotificationService notificationService;
    private final Gson gson;
    private RedisMessageListener redisMessageListener;

    public void setRedisMessageListener(RedisMessageListener redisMessageListener) {
        this.redisMessageListener = redisMessageListener;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            InboundMessage inboundMessage = mapper.readValue(message.getBody(), InboundMessage.class);
            messageList.add(message.toString());
            log.debug("message = {}", gson.toJson(message));
            log.debug("inboundMessage = {}", gson.toJson(inboundMessage));
            notificationService.sendToClient(inboundMessage);
            if (redisMessageListener != null) {
                this.redisMessageListener.onMessage(inboundMessage);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    interface RedisMessageListener {
        void onMessage(InboundMessage message);
    }
}