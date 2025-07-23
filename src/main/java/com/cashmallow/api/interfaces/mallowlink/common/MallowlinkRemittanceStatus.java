package com.cashmallow.api.interfaces.mallowlink.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MallowlinkRemittanceStatus {

    REQUEST("0000", "신청"),
    SUCCESS("0000", "성공"),
    INVALID_RECEIVER("1001", "수취인 에러"),
    FAIL("1000", "실패");

    private final String code;
    private final String message;

}
