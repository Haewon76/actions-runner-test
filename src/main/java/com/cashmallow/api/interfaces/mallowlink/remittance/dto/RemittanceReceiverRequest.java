package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceReceiverRequest {
    @NotBlank
    private final String transactionId;
    @NotNull
    private final RemittanceReceiver receiver;

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

}
