package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
public final class RemittanceCancelRequest {
    @NotBlank
    private final String transactionId;
    @NotNull
    private final ZonedDateTime requestTime;

}
