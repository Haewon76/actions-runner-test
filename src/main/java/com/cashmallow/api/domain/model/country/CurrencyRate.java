package com.cashmallow.api.domain.model.country;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain model for Currency Rate
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrencyRate {

    private Long currencyId;
    private String source;
    private String target;
    private BigDecimal rate;
    private BigDecimal baseRate;
    private BigDecimal adjustRate;
    private Timestamp updatedDate;

    public CurrencyRate(long currencyId, String source, String target, BigDecimal rate, BigDecimal baseRate, BigDecimal adjustRate) {
        this.currencyId = currencyId;
        this.source = source;
        this.target = target;
        this.rate = rate;
        this.baseRate = baseRate;
        this.adjustRate = adjustRate;
    }

    public CurrencyRate(String target, String source,  BigDecimal rate, BigDecimal baseRate, BigDecimal adjustRate) {
        this.source = source;
        this.target = target;
        this.rate = rate;
        this.baseRate = baseRate;
        this.adjustRate = adjustRate;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
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

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }

}
