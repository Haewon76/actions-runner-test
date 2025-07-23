package com.cashmallow.api.interfaces.scb.service;

import com.cashmallow.api.interfaces.scb.model.dto.InboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashSet;
import java.util.Set;

import static com.cashmallow.api.config.RedisConfig.SCB_INBOUND_TOPIC;

@Service
@Slf4j
public class WebNotificationService {
    private static final Long DEFAULT_TIMEOUT = 20L * 1000; // 2 mins
    private Set<SseEmitter> sseEmitters = new HashSet<>();


    public SseEmitter subscribe(String withdrawalRequestNo) {
        final SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitter.onCompletion(() -> {
            synchronized (this.sseEmitters) {
                this.sseEmitters.remove(sseEmitter);
            }
        });
        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError(sseEmitter::completeWithError);

        // Put context in a map
        sseEmitters.add(sseEmitter);

        // time out 방지 코드
        // sendToClient(InboundMessage.builder().code(333).withdrawalRequestNo(withdrawalRequestNo).build());
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(withdrawalRequestNo)
                    .name(SCB_INBOUND_TOPIC)
                    .data(InboundMessage.builder().code(333).withdrawalRequestNo(withdrawalRequestNo).build()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return sseEmitter;
    }

    // 3
    public void sendToClient(InboundMessage message) {
        sseEmitters.forEach(emitter -> {
            String withdrawalRequestNo = message.withdrawalRequestNo();
            // String id = withdrawalRequestNo + "_" + System.currentTimeMillis();
            try {
                emitter.send(SseEmitter.event()
                        .id(withdrawalRequestNo)
                        .name(SCB_INBOUND_TOPIC)
                        .data(message));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Connection error!");
            }
        });
    }
}
