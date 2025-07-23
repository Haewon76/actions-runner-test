package com.cashmallow.api.interfaces.scb.model.dto;

public enum LogType {
    INBOUND, // 레거시
    INBOUND_CONFIRM, // 정상 출금 처리
    INBOUND_REVERT,  // 출금시 오류 발생
    CANCEL,
    REQUEST,
    CONFIRM;

    public static LogType findInboundType(String func) {
        if (func.contains("ConfirmWithdrawal")) {
            return INBOUND_CONFIRM;
        }
        if (func.contains("RevertWithdrawal")) {
            return INBOUND_REVERT;
        }
        return INBOUND;
    }
}
