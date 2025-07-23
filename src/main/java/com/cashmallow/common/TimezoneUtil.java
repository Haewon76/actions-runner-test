package com.cashmallow.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Getter
@ToString
public class TimezoneUtil {

    public static String countryTimeZone(String code, Timestamp timestamp) {
        ZoneId zoneId = CountryCode.ofZoneId(code).getZoneId();
        return LocalDateTime.ofInstant(timestamp.toInstant(), zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static Timestamp fromLocalDate(String fromCountryCode, LocalDate localDate) {
        CountryCode countryCode = CountryCode.of(fromCountryCode);
        if (localDate != null) {
            return Timestamp.from(localDate.atStartOfDay().atZone(countryCode.getZoneId()).toInstant());
        } else {
            return null;
        }
    }

    public static Timestamp fromLocalDateTime(String fromCountryCode, LocalDateTime localDateTime) throws CashmallowException {
        CountryCode countryCode = CountryCode.of(fromCountryCode);
        if (localDateTime == null) {
            return null;
        }

        try {
            return Timestamp.from(localDateTime.atZone(countryCode.getZoneId()).toInstant());
        } catch (IllegalArgumentException e) {
            throw new CashmallowException("입력 형태가 잘못되었습니다. ex) yyyy-mm-dd hh:mm:ss", Const.CODE_FAILURE);
        }
    }
}