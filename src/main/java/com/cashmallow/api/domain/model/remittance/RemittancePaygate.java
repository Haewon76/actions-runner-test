package com.cashmallow.api.domain.model.remittance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.cloud.Timestamp;

/**
 * Domain model for Remittance
 *
 * @author bongseok
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemittancePaygate {

    private Long remitId;
    private String memberId;
    private String kycRefId;
    private String smaRefId;
    private String smaId;
    private String smTid;
    private String kycStatus;
    private String sendmoneyStatus;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public Long getRemitId() {
        return remitId;
    }

    public void setRemitId(Long remitId) {
        this.remitId = remitId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getSmaId() {
        return smaId;
    }

    public void setSmaId(String smaId) {
        this.smaId = smaId;
    }

    public String getSmTid() {
        return smTid;
    }

    public void setSmTid(String smTid) {
        this.smTid = smTid;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getSendmoneyStatus() {
        return sendmoneyStatus;
    }

    public void setSendmoneyStatus(String sendmoneyStatus) {
        this.sendmoneyStatus = sendmoneyStatus;
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

    public String getKycRefId() {
        return this.kycRefId;
    }

    public void setKycRefId(String kycRefId) {
        this.kycRefId = kycRefId;
    }

    public String getSmaRefId() {
        return smaRefId;
    }

    public void setSmaRefId(String smaRefId) {
        this.smaRefId = smaRefId;
    }

}
