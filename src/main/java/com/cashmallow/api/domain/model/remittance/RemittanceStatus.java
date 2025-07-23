package com.cashmallow.api.domain.model.remittance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for Remittance
 *
 * @author bongseok
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemittanceStatus {

    private Long remitId;
    private String remitStatus;

    @Getter
    @Setter
    private String message;
    private Timestamp createdDate;

    public RemittanceStatus(Long remitId, String remitStatus, String message) {
        this.remitId = remitId;
        this.remitStatus = remitStatus;
        this.message = message;
    }


    public Long getRemitId() {
        return remitId;
    }

    public void setRemitId(Long remitId) {
        this.remitId = remitId;
    }

    public String getRemitStatus() {
        return remitStatus;
    }

    public void setRemitStatus(String remitStatus) {
        this.remitStatus = remitStatus;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

}
