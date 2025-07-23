package com.cashmallow.api.domain.model.remittance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RemittancePurpose {
    GIFT("GIFT", "贈与"), // 증여/선물
    STUDY("STUDY", "留学"), // 유학
    LIVINGEXPENSES("LIVINGEXPENSES", "生活費"), // 생활비
    DONATE("DONATE", "寄付"), // 기부
    TRAVELEXPENSES("TRAVELEXPENSES", "旅行費"); // 여행비

    private String enDecription;
    private String jpDecription;
}
