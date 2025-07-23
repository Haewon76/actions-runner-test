package com.cashmallow.api.domain.model.country;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CMCurrency {
    private Long id;
    private String source;
    private BigDecimal usd;
    private BigDecimal hkd;
    private BigDecimal twd;
    private BigDecimal krw;
    private BigDecimal jpy;
    private Long tstamp;
    private String tstampString;
    private Timestamp createdDate;

    public CMCurrency(String source, long tstamp, String tstampString) {
        this.source = source;
        this.tstamp = tstamp;
        this.tstampString = tstampString;
    }

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

    public BigDecimal getKrw() {
        return krw;
    }

    public void setKrw(BigDecimal krw) {
        this.krw = krw;
    }

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }

    public BigDecimal getTwd() {
        return twd;
    }

    public void setTwd(BigDecimal twd) {
        this.twd = twd;
    }

    public BigDecimal getHkd() {
        return hkd;
    }

    public void setHkd(BigDecimal hkd) {
        this.hkd = hkd;
    }

    public BigDecimal getJpy() {
        return jpy;
    }

    public void setJpy(BigDecimal jpy) {
        this.jpy = jpy;
    }

    public Long getTstamp() {
        return tstamp;
    }

    public void setTstamp(Long tstamp) {
        this.tstamp = tstamp;
    }

    public String getTstampString() {
        return tstampString;
    }

    public void setTstampString(String tstampString) {
        this.tstampString = tstampString;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

}
