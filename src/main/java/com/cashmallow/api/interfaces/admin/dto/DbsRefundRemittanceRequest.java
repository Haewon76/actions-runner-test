package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DbsRefundRemittanceRequest {
    @NotBlank
    String tid;
    Long userId;
    @NotNull
    @Min(1)
    BigDecimal amount;
    BigDecimal fee;
}
