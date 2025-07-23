package com.cashmallow.api.interfaces.mallowlink.remittance.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum MallowlinkRemittancePurpose {
    GIFT("GIFT"), // 증여/선물
    EDUCATION_COST("STUDY"), // 유학
    LIVING_EXPENSES("LIVINGEXPENSES"), // 생활비
    DONATION("DONATE"), // 기부
    TRAVEL_EXPENSES("TRAVELEXPENSES"); // 여행비

    private final String cashmallowPurpose;

    public static MallowlinkRemittancePurpose of(String cashmallowPurpose) {
        return Arrays.stream(MallowlinkRemittancePurpose.values())
                .filter(e -> e.cashmallowPurpose.equals(cashmallowPurpose))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}