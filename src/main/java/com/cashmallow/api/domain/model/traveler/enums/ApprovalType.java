package com.cashmallow.api.domain.model.traveler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApprovalType {
    NFC("NFC"),
    MANUAL("수동인증");

    private final String text;
}
