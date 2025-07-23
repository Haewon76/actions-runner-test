package com.cashmallow.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Locale;

@Slf4j
class CommDateTimeTest {

    @Test
    void getTimeFormat() {
        String hongKong = CommDateTime.getTimeFormat(CountryCode.of("001").getZoneId(), Locale.CHINA);
        String taiwan = CommDateTime.getTimeFormat(CountryCode.of("002").getZoneId(), Locale.TAIWAN);
        String korea = CommDateTime.getTimeFormat(CountryCode.of("003").getZoneId(), Locale.KOREA);
        String japan = CommDateTime.getTimeFormat(CountryCode.of("004").getZoneId(), Locale.JAPAN);
        String Thailand = CommDateTime.getTimeFormat(CountryCode.of("010").getZoneId(), Locale.TAIWAN);

        log.info("hongKong time : " + hongKong);
        log.info("taiwan time : " + taiwan);
        log.info("korea time : " + korea);
        log.info("japan time : " + japan);
        log.info("Thailand time : " + Thailand);

    }
}
