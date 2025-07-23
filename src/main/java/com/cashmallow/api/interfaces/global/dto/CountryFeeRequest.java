package com.cashmallow.api.interfaces.global.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CountryFeeRequest {
    private Long id;
    private Long syncId;
    private String fromCd;
    private String toCd;
    private BigDecimal fee;
    private BigDecimal min;
    private BigDecimal max;
    private Long sort;
    private boolean useYn;
}
