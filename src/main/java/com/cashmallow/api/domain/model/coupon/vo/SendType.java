package com.cashmallow.api.domain.model.coupon.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum SendType {

    DIRECT("direct", "즉시 발송"),

    RESERVATION("reservation", "예약 발송");

    private static final Map<String, SendType> stringToEnum = Stream.of(values())
            .collect(Collectors.toMap(SendType::getCode, e -> e));

    private final String code;

    private final String kr;

    public static SendType fromString(String code) {
        return stringToEnum.get(code);
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
