package com.cashmallow.api.domain.model.coupon.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ExpireType {
    DATE_RANGE("dateRange"),            // 기간 지정
    DAYS_FROM_ISSUE("daysFromIssue");   // 발급일로부터 n일

    private static final Map<String, ExpireType> stringToEnum = Stream.of(values())
            .collect(Collectors.toMap(ExpireType::getCode, e -> e));

    private final String code;

    public static ExpireType fromString(String code) {
        return stringToEnum.get(code);
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
