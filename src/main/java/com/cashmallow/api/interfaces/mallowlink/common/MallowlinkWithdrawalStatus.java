package com.cashmallow.api.interfaces.mallowlink.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MallowlinkWithdrawalStatus {

    REQUEST("0000", "Request received"),
    WAIT_QR("0000", "Waiting for QR"),
    WAIT_INBOUND_RESULT("0000", "Waiting for results"),

    SUCCESS("0000", "Success"),
    FAIL("0000", "Fail"),
    REVERT("0000", "Revert or rollback"),
    CANCELED("0000", "Cancellation by system"),

    CANCEL_USER("0000", "Cancellation by user");

    private final String code;
    private final String message;

}
