package com.cashmallow.api.domain.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class Receiver {

    Logger logger = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message) {
        logger.info("receiveMessage(): message={}", message);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
