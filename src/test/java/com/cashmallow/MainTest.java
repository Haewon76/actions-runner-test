package com.cashmallow;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.aml.AmlAccountTranBase;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.common.CRC16Util;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.DateUtil;
import io.github.mngsk.devicedetector.Detection;
import io.github.mngsk.devicedetector.DeviceDetector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cashmallow.common.CommDateTime.getAge;
import static com.cashmallow.common.CommonUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MainTest {


    @Test
    public void 테스트명1() {
        int age = getAge("19970202");
        System.out.println("age = " + age);

    }

    @Test
    public void 신분증번호_테스트() {
        String oldId = "Y4165096";
        String newId = "Y416509(6)";
        assertTrue(isSameCertification(oldId, newId));
    }

    @Test
    void 타임존_테스트() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Timestamp tranTime = new Timestamp(System.currentTimeMillis());
        AmlAccountTranBase amlAccountTranBase = AmlAccountTranBase.builder()
                .tranTime(tranTime)
                .tranDd(tranTime)
                .build();

        System.out.println(amlAccountTranBase.getTranTime());
        System.out.println(amlAccountTranBase.getTranDd());
    }

    @Test
    public void test() {
        Stream.of("７／Ｆ ＢＬＫ Ｍ\u00ADＮ ＫＷＯＮＧ ＦＵＮＧ ＢＬＤＧ ３ ＫＡＭ ＬＡＭ ＳＴＲＥＥＴ",
                        "３３４４ Ｗ Ａｌａｍｅｄａ Ａｖｅｎｕｅ, Ｌａｋｅｗｏｏｄ, ＣＯ ８０２２２",
                        "日本、〒556-0003 大阪府大阪市浪速区恵美須西２丁目１４−２１ サザンパークス",
                        "7/F BLK M\u00ADN KWONG FUNG BLDG 3 KAM LAM STREET",
                        "서울시 강남구 선릉로 94-14 4F 층 104호",
                        "Hello, ADB （street 9）, 12345, USA")
                .forEach(CommonUtil::textToNormalize);
        // 日本、〒556-0003 大阪府大阪市浪速区恵美須西２丁目１４−２１ サザンパークス
        // 日本、〒556-0003 大阪府大阪市浪速区恵美須西2丁目14−21 サザンパークス
        // Hello, ADB （street 9）, 12345, USA
        // Hello, ADB (street 9), 12345, USA
        // 서울시 강남구 선릉로 94-14 4F 층 104호
    }


    @Test
    public void 숫자로_국가코드_잘_받아오는지_체크() {
        final CountryCode countryCodeByCode = CountryCode.of("009");
        assertEquals("SG", countryCodeByCode.name());
    }

    @Test
    public void 존데이트타임_테스트() {
        String datetime = "2022-06-28T17:38:00+07:00";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(datetime, formatter);
        System.out.println(zonedDateTime);

    }

    @Test
    public void SCB_QR_VALIDATION_체크() {
        assertTrue(CRC16Util.isValidScbQR(";S1G1669PK       000121253886757748407414388F0FA"));
        assertTrue(CRC16Util.isValidScbQR(";S1G030223       1229628423690765030472143023A01"));
        assertTrue(CRC16Util.isValidScbQR(";S1G030223       122962842369076503047214309BA38"));
        assertTrue(CRC16Util.isValidScbQR(";S1G030223       122962842369076503047214306BA1A"));
        assertTrue(CRC16Util.isValidScbQR(";S1G030223       1229628423690765030472143013A0B"));
        assertFalse(CRC16Util.isValidScbQR("S1G030223"));
        assertFalse(CRC16Util.isValidScbQR("https://github.com/CashmallowCorp/cashmallow-info/issues/37"));
    }

    @Test
    public void 유저_에이전트_파싱() {
        DeviceDetector dd = new DeviceDetector.DeviceDetectorBuilder().build();
        // SMARTPHONE, TABLET, DESKTOP, BOT, UNKNOWN, ETC

        Stream.of("Mozilla/5.0 (Ipad; CPU OS 13_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/79.0.3945.73 Mobile/15E148 Safari/ 604.1",
                        "Mozilla/5.0 (IPhone; CPU iPhone OS 13_3 like Mac OS X) AppleWebKit/605.1.15(KHTML, like Gecko) CriOS/80.0.3987.95 Mobile/15E148 Safari/604.1",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36",
                        "Mozilla/5.0 (Linux; Android 13; SAMSUNG SM-G998N) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/21.0 Chrome/110.0.5481.154 Mobile Safari/537.36")
                .forEach(userAgent -> {
                    Detection detection = dd.detect(userAgent);
                    System.out.println(detection.getDevice().map(d -> d.toString()).orElse("unknown"));
                    System.out.println(detection.getOperatingSystem().map(d -> d.toString()).orElse("unknown"));
                    System.out.println(detection.getClient().map(d -> d.toString()).orElse("unknown"));

                    if (detection.getDevice().isPresent()) {
                        System.out.println(detection.getDevice().get().getType()); // bot, browser, feed reader...
                        System.out.println(detection.getDevice().get().getBrand().orElse("unknown"));
                        System.out.println(detection.getDevice().get().getModel().orElse("unknown"));
                    }
                    System.out.println("--------------------------------------");
                });
    }

    @Test
    void 자동매핑_테스트() {
        String fromAccountNo = "122333444";
        String toAccountNo = "016122333444";
        BigDecimal fromAmt = new BigDecimal("68.98");
        BigDecimal toAmt = new BigDecimal("68.98");

        Assertions.assertThat(isValidateDeposit(toAccountNo, toAccountNo, "", fromAmt, toAmt)).isTrue();
        Assertions.assertThat(isValidateDeposit(fromAccountNo, toAccountNo, "", fromAmt, toAmt)).isTrue();
        Assertions.assertThat(isValidateDeposit(fromAccountNo, toAccountNo, "016", fromAmt, toAmt)).isTrue();
        Assertions.assertThat(isValidateDeposit(fromAccountNo, toAccountNo, null, fromAmt, toAmt)).isTrue();
        Assertions.assertThat(isValidateDeposit(fromAccountNo + "1", toAccountNo, "", fromAmt, toAmt)).isFalse();
        Assertions.assertThat(isValidateDeposit(fromAccountNo + "1", toAccountNo, "016", fromAmt, toAmt)).isFalse();
        Assertions.assertThat(isValidateDeposit(fromAccountNo + "1", toAccountNo, null, fromAmt, toAmt)).isFalse();
        Assertions.assertThat(isValidateDeposit(fromAccountNo, toAccountNo, "0", fromAmt, toAmt)).isFalse();
    }

    @Test
    public void 옥타_직업코드_리스트_출력() {
        final String s = Job.octaJobCodes();
        System.out.println(s);

    }

    @Test
    public void 캘린더_테스트() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Calendar cal = Calendar.getInstance();
        String toDate = new Timestamp(cal.getTime().getTime()).toString();

        cal.add(Calendar.YEAR, -1);
        String fromDate = new Timestamp(cal.getTime().getTime()).toString();

        final ZoneId zoneId = CountryCode.HK.getZoneId();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);

        System.out.println("fromDate: " + fromDate);
        System.out.println("toDate: " + toDate);
        System.out.println("zonedDateTime: " + zonedDateTime);
        System.out.println("zonedDateTime UTC: " + ZonedDateTime.now());

        String formattedDateTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        System.out.println(formattedDateTime);

        System.out.println("-------------------------");


        final String[] daily = DateUtil.getDaily(zoneId, true);
        final String[] monthly = DateUtil.getMonthly(zoneId, true);
        final String[] yearly = DateUtil.getYearly(zoneId, true);

        System.out.println("getDaily static: " + daily[0] + ", " + daily[1]);
        System.out.println("getMonthly static: " + monthly[0] + ", " + monthly[1]);
        System.out.println("getYearly static: " + yearly[0] + ", " + yearly[1]);


        final String[] daily2 = DateUtil.getDaily(zoneId, false);
        final String[] monthly2 = DateUtil.getMonthly(zoneId, false);
        final String[] yearly2 = DateUtil.getYearly(zoneId, false);

        System.out.println("getDaily non static: " + daily2[0] + ", " + daily2[1]);
        System.out.println("getMonthly non static: " + monthly2[0] + ", " + monthly2[1]);
        System.out.println("getYearly non static: " + yearly2[0] + ", " + yearly2[1]);
    }

    @Test
    public void 테스트명() {
        // Assume apiResponseTimes is a List of integers represents
        // the api response times in milliseconds
        List<Integer> apiResponseTimes = new ArrayList<>();
        apiResponseTimes.add(100);
        apiResponseTimes.add(120);
        apiResponseTimes.add(200);
        apiResponseTimes.add(300);
        apiResponseTimes.add(400);
        apiResponseTimes.add(500);
        apiResponseTimes.add(600);
        apiResponseTimes.add(1000);
        apiResponseTimes.add(1800);

        // Sort the list
        Collections.sort(apiResponseTimes);

        // Find the median
        double median;
        if (apiResponseTimes.size() % 2 == 0) {
            // calculate average of middle elements
            median = (apiResponseTimes.get(apiResponseTimes.size() / 2)
                    + apiResponseTimes.get(apiResponseTimes.size() / 2 - 1)) / 2.0;
        } else {
            median = apiResponseTimes.get(apiResponseTimes.size() / 2);
        }

        System.out.println("Median Latency: " + median);
    }

    @Test
    public void 리플레이스_테스트() {
        String travelerId = "T1222";
        String str = "AuthmeServices customer_id:{travelerId} event_name:default";
        String result = str.replace("{travelerId}", travelerId);
        System.out.println("result = " + result);

        String jp = "JP123";
        System.out.println("test -> " + jp.substring(0, 2));

    }

    @Test
    public void authme_영문이름_테스트() {
        String englishName = "SAN, Chi Nan";
        List<String> list = Arrays.asList(textToNormalize(englishName).split(" |,"));
        String firstName = list.stream().skip(1).map(String::trim).map(String::toUpperCase).collect(Collectors.joining(" ")).trim();
        String lastName = list.get(0).toUpperCase().trim();

        System.out.println("firstName = " + firstName);
        System.out.println("lastName = " + lastName);

        assertEquals("CHI NAN", firstName);
        assertEquals("SAN", lastName);

    }
}
