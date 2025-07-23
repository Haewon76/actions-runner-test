package com.cashmallow.api.domain.model.openbank;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class OpenbankMapperTest {

    @Autowired
    OpenbankMapper openbankMapper;

    long travelerId = 002L;

    @Test
    void DB암호화() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OpenbankToken openbankToken = new OpenbankToken(
                travelerId,
                "A1234",
                "R1234",
                "USN2134",
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        // when
        openbankMapper.insertOpenbankUserToken(openbankToken);

        // then
        Openbank openbank1 = openbankMapper.getOpenbankByTravelerId(travelerId);
        assertThat(openbank1.getSignYn()).isEqualTo("Y");
        log.debug("getAccessToken: {}", openbank1.getAccessToken());
        log.debug("{}", now.toString());
        log.debug("{}", openbank1.getSignDate());
        log.debug("{}", openbank1.getSignDate().getClass().getName());
        assertThat(openbank1.getSignDate()).isAfterOrEqualTo(now.truncatedTo(ChronoUnit.SECONDS)).isBefore(now.plus(500, ChronoUnit.MILLIS));

    }


}