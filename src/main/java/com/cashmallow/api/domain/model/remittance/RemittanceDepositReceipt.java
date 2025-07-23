package com.cashmallow.api.domain.model.remittance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Traveler's deposit receipt for Remittance
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemittanceDepositReceipt {

    private long id;
    private long remitId;
    private String receiptPhoto;
    private Timestamp createdDate;

    @Getter
    @Setter
    private String langKey;

    public RemittanceDepositReceipt() {
    }

    public RemittanceDepositReceipt(long remitId, String receiptPhoto) {
        this.remitId = remitId;
        this.receiptPhoto = receiptPhoto;
        this.createdDate = Timestamp.valueOf(LocalDateTime.now());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRemitId() {
        return remitId;
    }

    public void setRemitId(long remitId) {
        this.remitId = remitId;
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