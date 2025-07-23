package com.cashmallow.api.domain.model.country;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain model for Notice
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrencyAdjust {

    private Long id;
    private String source;
    private String target;
    private BigDecimal adjustRate;
    private Timestamp createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public BigDecimal getAdjustRate() {
        return adjustRate;
    }

    public void setAdjustRate(BigDecimal adjustRate) {
        this.adjustRate = adjustRate;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

}
