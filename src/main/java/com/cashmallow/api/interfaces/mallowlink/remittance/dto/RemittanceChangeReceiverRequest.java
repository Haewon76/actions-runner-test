package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceChangeReceiverRequest {
    @NotBlank
    String transactionId;
    @NotNull
    RemittanceReceiver receiver;

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

}
