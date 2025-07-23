package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelerWallet {
    private Long id;
    private Long travelerId;
    private String rootCd;
    private String country;
    private BigDecimal eMoney;
    private BigDecimal rMoney;
    private BigDecimal cMoney;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private String expired;
    private Long creator;
    private String exchangeIds;
    private String canRefund;
    private Long exchangeId;

    public TravelerWallet() {
    }

    public TravelerWallet(Long travelerId, String rootCd, String country, BigDecimal eMoney, Long creator) {
        this.travelerId = travelerId;
        this.rootCd = rootCd;
        this.country = country;
        this.eMoney = eMoney;
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public String getCountry() {
        return country;
    }

    @JsonIgnore
    public CountryCode getToCountry() {
        return CountryCode.of(country);
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal geteMoney() {
        return eMoney == null ? new BigDecimal(0) : eMoney;
    }

    public void seteMoney(BigDecimal eMoney) {
        this.eMoney = eMoney;
    }

    public BigDecimal getcMoney() {
        return cMoney == null ? new BigDecimal(0) : cMoney;
    }

    public void setcMoney(BigDecimal cMoney) {
        this.cMoney = cMoney;
    }

    public BigDecimal getrMoney() {
        return rMoney == null ? new BigDecimal(0) : rMoney;
    }

    public void setrMoney(BigDecimal rMoney) {
        this.rMoney = rMoney;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public String getRootCd() {
        return rootCd;
    }

    public void setRootCd(String rootCd) {
        this.rootCd = rootCd;
    }

    public String getExchangeIds() {
        return exchangeIds;
    }

    public void setExchangeIds(String exchangeIds) {
        this.exchangeIds = exchangeIds;
    }

    public String getCanRefund() {
        return canRefund;
    }

    public void setCanRefund(String canRefund) {
        this.canRefund = canRefund;
    }

    public Long getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }
}
