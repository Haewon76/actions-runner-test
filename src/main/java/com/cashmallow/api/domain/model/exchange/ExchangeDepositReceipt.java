package com.cashmallow.api.domain.model.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Traveler's deposit receipt for Remittance
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeDepositReceipt {

    private long id;
    private long exchangeId;
    private String receiptPhoto;

    @Getter
    @Setter
    private String langKey;
    private Timestamp createdDate;

    public ExchangeDepositReceipt() {
    }

    public ExchangeDepositReceipt(long exchangeId, String receiptPhoto) {
        this.exchangeId = exchangeId;
        this.receiptPhoto = receiptPhoto;
        this.createdDate = Timestamp.valueOf(LocalDateTime.now());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(long exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getReceiptPhoto() {
        return receiptPhoto;
    }

    public void setReceiptPhoto(String receiptPhoto) {
        this.receiptPhoto = receiptPhoto;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}