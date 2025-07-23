package com.cashmallow.api.domain.model.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Mapping Domain model
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Mapping {

    private Long id;
    private String country;
    private Integer bankAccountId;

    private BigDecimal pinValue;
    private String refValue;
    private Integer pinSeqNo;

    private Timestamp beginValidDate;
    private Timestamp endValidDate;
    private Timestamp createdDate;
    private Timestamp canceledDate;

    private String status;

    private Long travelerId;
    private Long exchangeId;
    private Long remitId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Integer bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public BigDecimal getPinValue() {
        return pinValue;
    }

    public void setPinValue(BigDecimal pinValue) {
        this.pinValue = pinValue;
    }

    public String getRefValue() {
        return refValue;
    }

    public void setRefValue(String refValue) {
        this.refValue = refValue;
    }

    public Integer getPinSeqNo() {
        return pinSeqNo;
    }

    public void setPinSeqNo(Integer pinSeqNo) {
        this.pinSeqNo = pinSeqNo;
    }

    public Timestamp getBeginValidDate() {
        return beginValidDate;
    }

    public void setBeginValidDate(Timestamp beginValidDate) {
        this.beginValidDate = beginValidDate;
    }

    public Timestamp getEndValidDate() {
        return endValidDate;
    }

    public void setEndValidDate(Timestamp endValidDate) {
        this.endValidDate = endValidDate;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getCanceledDate() {
        return canceledDate;
    }

    public void setCanceledDate(Timestamp canceledDate) {
        this.canceledDate = canceledDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public Long getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
    }

    public Long getRemitId() {
        return remitId;
    }

    public void setRemitId(Long remitId) {
        this.remitId = remitId;
    }

}
