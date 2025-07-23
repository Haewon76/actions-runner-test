package com.cashmallow.api.interfaces.mallowlink.currency.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MallowlinkCurrencyRateResponse {
    private String source;
    private String target;
    private BigDecimal rate;
    private LocalDateTime updatedAt;
}
