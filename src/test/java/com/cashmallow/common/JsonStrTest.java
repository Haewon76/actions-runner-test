package com.cashmallow.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class JsonStrTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void toJson과_springObjectMapper_일치() throws Exception {
        // given
        BigDecimal bigDecimalVal = new BigDecimal("2134.1234");
        int intVal = 1234;
        float floatVal = 1234.1234f;
        Double doubleVal = Double.valueOf("2134.1234");
        Long longVal = Long.valueOf("2134");
        TestObject testObject = new TestObject();

        // when
        String bigDecimalJson = JsonStr.toJson(bigDecimalVal);
        String intJson = JsonStr.toJson(intVal);
        String floatJson = JsonStr.toJson(floatVal);
        String doubleJson = JsonStr.toJson(doubleVal);
        String longJson = JsonStr.toJson(longVal);
        String testObjectJson = JsonStr.toJson(testObject);


        log.debug("testObjectJson1={}", testObjectJson);

        // then
        assertThat(bigDecimalJson).isEqualTo(objectMapper.writeValueAsString(bigDecimalVal));
        assertThat(intJson).isEqualTo(objectMapper.writeValueAsString(intVal));
        assertThat(floatJson).isEqualTo(objectMapper.writeValueAsString(floatVal));
        assertThat(doubleJson).isEqualTo(objectMapper.writeValueAsString(doubleVal));
        assertThat(longJson).isEqualTo(objectMapper.writeValueAsString(longVal));
        assertThat(testObjectJson).isEqualTo(objectMapper.writeValueAsString(testObject));

    }

    @Getter
    private static class TestObject {
        BigDecimal bigDecimalVal = new BigDecimal("2134.1234");
        int intVal = 1234;
        float floatVal = 1234.1234f;
        Double doubleVal = Double.valueOf("2134.1234");
        Long longVal = Long.valueOf("2134");
        ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}