package com.cashmallow.api.domain.model.country;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class CountryFee {
    private Long id;
    private String fromCd;
    private String toCd;
    private BigDecimal fee;
    private BigDecimal min;
    private BigDecimal max;
    private Integer sort;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String useYn;

    private String createdAtString;
    private String updatedAtString;

    @Builder
    public CountryFee(Long id, String fromCd, String toCd, BigDecimal fee,
                      BigDecimal min, BigDecimal max, Integer sort, Timestamp createdAt,
                      Timestamp updatedAt, String useYn) {
        this.id = id;
        this.fromCd = fromCd;
        this.toCd = toCd;
        this.fee = fee;
        this.min = min;
        this.max = max;
        this.sort = sort;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.useYn = useYn;
    }
}
