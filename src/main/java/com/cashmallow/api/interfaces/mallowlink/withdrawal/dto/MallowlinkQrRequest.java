package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;


import javax.validation.constraints.NotBlank;
import java.time.ZonedDateTime;

public record MallowlinkQrRequest(
        @NotBlank
        String transactionId,

        @NotBlank
        String qrData,

        @NotBlank
        ZonedDateTime requestTime
) {

    public static MallowlinkQrRequest of(String clientTransactionId, String qrData) {
        return new MallowlinkQrRequest(clientTransactionId,
                qrData,
                ZonedDateTime.now());
    }
}
