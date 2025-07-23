package com.cashmallow.api.interfaces.scb.model.dto.inbound;

import com.cashmallow.common.CommDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SCBInboundRequestTest {

    @BeforeEach
    void before() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void SCB날짜_UTC_변환_테스트() {
        // given
        String date = "2023-02-14T15:40:06+07:00";

        // when
        LocalDateTime localDateTime = CommDateTime.localDateTimeToUTCDateTime(date);

        // then
        log.debug(localDateTime.toString());
        String localDate = "2023-02-14T08:40:06";
        LocalDateTime excpectLocalDateTime = LocalDateTime.parse(localDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(localDateTime).isEqualTo(excpectLocalDateTime);
    }

    @Test
    void 날짜_변환_테스트() {
        System.out.println(ZoneId.getAvailableZoneIds());
        System.out.println(CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("Asia/Seoul")));
        System.out.println(CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("Asia/Tokyo")));
        System.out.println(CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("Asia/Hong_Kong")));
        System.out.println(CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("+09:00")));
        System.out.println(CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("+01:00")));
        System.out.println(CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("+00:00")));
    }
}