package com.cashmallow.api.domain.model.coupon.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cashmallow.api.domain.shared.Const.INVALID_DISCOUNT_TYPE_CODE;

@Getter
@RequiredArgsConstructor
public enum DiscountType {

    FIXED_AMOUNT("fixedAmount", "정액"),

    RATE_AMOUNT("rateAmount", "비율"),

    FEE_WAIVER("feeWaiver", "수수료 면제"),

    ETC("etc", "기타");

    private static final Map<String, DiscountType> stringToEnum = Stream.of(values())
            .collect(Collectors.toMap(DiscountType::getCode, e -> e));

    private final String code;

    private final String kr;

    public static DiscountType fromString(String code) {
        return stringToEnum.get(code);
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static DiscountType fromCode(String code) {
        return Arrays.stream(DiscountType.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(INVALID_DISCOUNT_TYPE_CODE));
    }
}
