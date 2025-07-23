package com.cashmallow.api.domain.model.country;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class CountryFeeHistory {

    private Long id;
    private Long countryFeeId;
    private String fromCd;
    private String toCd;
    private BigDecimal fee;
    private BigDecimal min;
    private BigDecimal max;
    private Integer sort;
    private String useYn;
    private Timestamp createdAt;
    private Long userId;
    private String ip;
}
