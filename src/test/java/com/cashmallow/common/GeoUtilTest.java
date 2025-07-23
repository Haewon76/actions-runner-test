package com.cashmallow.common;

import com.cashmallow.api.domain.model.geo.GeoLocation;
import com.cashmallow.common.geoutil.GeoUtil;
import com.cashmallow.config.EnableDevLocal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@EnableDevLocal
@SpringBootTest
class GeoUtilTest {

    @Autowired
    GeoUtil geoUtil;

    @Test
    void getMyCountryCode() {
        // given
        List<String> collect = List.of("120.52.22.96", "205.251.249.0", "192.168.0.7", "222.106.187.9");

        // when
        List<GeoLocation> geos = collect.stream().map(ip -> geoUtil.getMyCountryCode(ip)).collect(Collectors.toList());
        // System.out.println("geos = " + JsonStr.toJson(geos));

        // then
        // assertThat(geos.get(0).getCountryShort()).isEqualTo("CN");
        // assertThat(geos.get(1).getCountryShort()).isEqualTo("FR");
        // assertThat(geos.get(2).getCountryShort()).isEqualTo("-");
        // assertThat(geos.get(3).getCountryShort()).isEqualTo("KR");

    }

    @Test
    void getMyCountryCode2() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // given
        String ip = "222.106.187.9";

        // when
        final GeoLocation myCountryCode = geoUtil.getMyCountryCode(ip);
        System.out.println("myCountryCode = " + JsonStr.toJson(myCountryCode));


        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("+09:00"));
        final String currentDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("now = " + now);
        System.out.println("now = " + currentDateTime);

        // then
        // assertThat(myCountryCode.getCountryShort()).isEqualTo("CN");

    }
}