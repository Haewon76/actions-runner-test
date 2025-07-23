package com.cashmallow.api.interfaces.mallowlink.common;

import com.cashmallow.api.interfaces.mallowlink.common.dto.EchoResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
class MallowlinkClientTest {

    @Autowired
    MallowlinkClient mallowlinkClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MallowlinkProperties properties;

    @Disabled
    @Test
    void echo() throws JsonProcessingException {
        String health = mallowlinkClient.health().getData();
        System.out.println("health = " + health);

        UserData userData = new UserData("U1234", "Sur Naem");
        EchoTest t1325 = new EchoTest(EchoTest.Type.WITHDRAWAL, "T1325", userData, BigDecimal.valueOf(932.81));

        EchoResponse<EchoTest> echo = mallowlinkClient.echo(t1325).getData();
        System.out.println("echo = " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(echo.getBody()));
    }

    @Data
    @RequiredArgsConstructor
    private static class EchoTest {
        private final Type type;
        private final String transactionId;
        private final UserData data;
        private final BigDecimal amount;

        private static enum Type {
            WITHDRAWAL, REMITTANCE
        }
    }

    @Data
    @RequiredArgsConstructor
    private static class UserData {
        private final String userId;
        private final String name;
    }

}