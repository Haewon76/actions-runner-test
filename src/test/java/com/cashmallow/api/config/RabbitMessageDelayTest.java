package com.cashmallow.api.config;

import com.cashmallow.config.EnableDevLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.cashmallow.api.config.RabbitConfig.DELAY_TEST_ROUTING_KEY;
import static com.cashmallow.api.config.RabbitConfig.DELAY_TEST_TOPIC;

@Slf4j
@SpringBootTest
@EnableDevLocal
class RabbitMessageDelayTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void delayedSend() throws InterruptedException {
        String messageContent = "Hello, RabbitMQ!";
        int delayTimes = 5000;
        rabbitTemplate.convertAndSend(DELAY_TEST_TOPIC,
                DELAY_TEST_ROUTING_KEY,
                messageContent,
                message -> {
                    // message 세팅
                    message.getMessageProperties().setHeader("x-delay", delayTimes);
                    // message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    log.info("rabbitTemplate: send message");
                    return message;
                });
        log.info("delayedSend: send message");
        Thread.sleep(10000);
    }
}