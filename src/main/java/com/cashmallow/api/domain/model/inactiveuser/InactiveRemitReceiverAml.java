package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveTraveler
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveRemitReceiverAml {

    private Long remitReceiverAmlId;
    private Long travelerId;
    private String receiverFirstName;
    private String receiverLastName;
    private String birthDate;
    private String amlSearchId;
    private Timestamp createdDate;

    public Long getRemitReceiverAmlId() {
        return remitReceiverAmlId;
    }

    public void setRemitReceiverAmlId(Long remitReceiverAmlId) {
        this.remitReceiverAmlId = remitReceiverAmlId;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public String getReceiverFirstName() {
        return receiverFirstName;
    }

    public void setReceiverFirstName(String receiverFirstName) {
        this.receiverFirstName = receiverFirstName;
    }

    public String getReceiverLastName() {
        return receiverLastName;
    }

    public void setReceiverLastName(String receiverLastName) {
        this.receiverLastName = receiverLastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAmlSearchId() {
        return amlSearchId;
    }

    public void setAmlSearchId(String amlSearchId) {
        this.amlSearchId = amlSearchId;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}
