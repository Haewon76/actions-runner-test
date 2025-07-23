package com.cashmallow.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.time.zone.ZoneRules;
import java.util.Date;

public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // static true인 경우 00:00 시 기준으로, false인 경우 현재 시간 기준으로
    public static String[] getYearly(ZoneId zone, boolean isStatic) {
        ZonedDateTime now = ZonedDateTime.now().plusHours(getOffsetHours(zone));
        if (isStatic) {
            ZonedDateTime start = now.withDayOfYear(1).with(LocalTime.MIN).minusHours(getOffsetHours(zone));
            ZonedDateTime end = now.with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX).minusHours(getOffsetHours(zone));
            return new String[]{FORMATTER.format(start), FORMATTER.format(end)};
        } else {
            ZonedDateTime start = now.minusYears(1).plusSeconds(1).minusHours(getOffsetHours(zone));
            ZonedDateTime end = now.minusHours(getOffsetHours(zone));
            return new String[]{FORMATTER.format(start), FORMATTER.format(end)};
        }
    }

    public static String[] getMonthly(ZoneId zone, boolean isStatic) {
        ZonedDateTime now = ZonedDateTime.now().plusHours(getOffsetHours(zone));

        if (isStatic) {
            ZonedDateTime start = now.withDayOfMonth(1).with(LocalTime.MIN).minusHours(getOffsetHours(zone));
            ZonedDateTime end = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX).minusHours(getOffsetHours(zone));
            return new String[]{FORMATTER.format(start), FORMATTER.format(end)};
        } else {
            ZonedDateTime start = now.minusMonths(1).plusSeconds(1).minusHours(getOffsetHours(zone));
            ZonedDateTime end = now.minusHours(getOffsetHours(zone));
            return new String[]{FORMATTER.format(start), FORMATTER.format(end)};
        }

    }

    public static String[] getDaily(ZoneId zone, boolean isStatic) {
        ZonedDateTime now = ZonedDateTime.now().plusHours(getOffsetHours(zone));

        if (isStatic) {
            ZonedDateTime start = now.with(LocalTime.MIN).minusHours(getOffsetHours(zone));
            ZonedDateTime end = now.with(LocalTime.MAX).minusHours(getOffsetHours(zone));
            return new String[]{FORMATTER.format(start), FORMATTER.format(end)};
        } else {
            ZonedDateTime start = now.minusDays(1).plusSeconds(1).minusHours(getOffsetHours(zone));
            ZonedDateTime end = now.minusHours(getOffsetHours(zone));
            return new String[]{FORMATTER.format(start), FORMATTER.format(end)};
        }
    }

    private static int getOffsetHours(ZoneId zone) {
        ZoneRules rules = zone.getRules();
        ZoneOffset standardOffset = rules.getStandardOffset(Instant.now());
        return standardOffset.getTotalSeconds() / 3600;
    }

    public static String getTimestampToOctaKstFormat(Timestamp timestamp) {
        return LocalDateTime.ofInstant(
                timestamp.toInstant(),
                ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMdd")
        );
    }

    public static String getTimestampToKst(Timestamp timestamp) {
        return LocalDateTime.ofInstant(
                timestamp.toInstant(),
                ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    public static String getTimestampToLocalDate(String countryCode, Timestamp timestamp) {
        return LocalDateTime.ofInstant(
                timestamp.toInstant(),
                CountryCode.of(countryCode).getZoneId()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd")
        );
    }

    public static String getTimestampToLocalDateTime(String countryCode, Timestamp timestamp) {
        return LocalDateTime.ofInstant(
                timestamp.toInstant(),
                CountryCode.of(countryCode).getZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    public static boolean isDate(String date) {
        if (date == null) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // n일 전 계산
    public static LocalDate beforeDay(LocalDate date, int beforeExpireDays) {
        return date.plusDays(beforeExpireDays);
    }

    // LocalDate 를 yyyyMMdd 패턴 String 으로 변환
    public static String fromLocalDateToYMD(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    // LocalDate 를 yyyy-MM-dd 패턴 String 으로 변환
    public static String fromLocalDateToY_M_D(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // LocalDateTime 를 yyyy-MM-dd HH:mm:ss 패턴 String 으로 변환
    public static String fromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // 해당 국가의 현재 LocalDate
    // fromCountryCode 는 캐시멜로에서 관리하는 국가 코드 ex: 001(홍콩), 004(일본)
    public static LocalDate toLocalDate(String fromCountryCode) {
        CountryCode countryCode = CountryCode.of(fromCountryCode);
        return ZonedDateTime.now(countryCode.getZoneId()).toLocalDate();
    }
    // 해당 국가의 현재 LocalDateTime
    // fromCountryCode 는 캐시멜로에서 관리하는 국가 코드 ex: 001(홍콩), 004(일본)
    public static LocalDateTime toLocalDateTime(String fromCountryCode) {
        CountryCode countryCode = CountryCode.of(fromCountryCode);
        return ZonedDateTime.now(countryCode.getZoneId()).toLocalDateTime();
    }

    // 해당 국가의 특정 시각 LocalDateTime (LocalDate 가 해당 국가이 날짜로 들어옴)
    public static LocalDateTime toLocalDateTimeAtTime(LocalDate localDate, int hour, int minute, int second) {
        // 현재 날짜의 특정 시각으로 LocalDateTime 생성합니다.
        return LocalDateTime.of(localDate, LocalTime.of(hour, minute, second));
    }

    // yyyy-MM-dd HH:mm:ss 패턴 String 을 LocalDateTime 으로 변환
    public static LocalDateTime fromY_M_D_H_M_S(String dateTime) throws CashmallowException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTime, formatter);
        } catch (IllegalArgumentException e) {
            throw new CashmallowException("입력 형태가 잘못되었습니다. ex) yyyy-mm-dd hh:mm:ss", Const.CODE_FAILURE);
        }
    }

    // yyyy-MM-dd 패턴 String 을 LocalDate 으로 변환
    public static LocalDate fromY_M_D(String date) throws CashmallowException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (date == null || date.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(date, formatter);
        } catch (IllegalArgumentException e) {
            throw new CashmallowException("입력 형태가 잘못되었습니다. ex) yyyy-mm-dd", Const.CODE_FAILURE);
        }
    }

    public static String fromTimestampToY_M_D_H_M_S(String fromCountryCode, Timestamp timestamp) {
        CountryCode countryCode = CountryCode.of(fromCountryCode);
        return LocalDateTime.ofInstant(timestamp.toInstant(), countryCode.getZoneId())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
