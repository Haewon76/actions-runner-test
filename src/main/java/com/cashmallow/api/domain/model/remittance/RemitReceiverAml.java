package com.cashmallow.api.domain.model.remittance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;


/**
 * Domain model for Remittance
 *
 * @author bongseok
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemitReceiverAml {

    private Long id;
    private Long travelerId;
    private String receiverFirstName;
    private String receiverLastName;
    private String birthDate;
    private String amlSearchId;
    private Timestamp createdDate;

    public RemitReceiverAml() {
        super();
    }

    public RemitReceiverAml(Long travelerId, String receiverFirstName, String receiverLastName, String birthDate, String amlSerchId) {
        this.travelerId = travelerId;
        this.receiverFirstName = receiverFirstName;
        this.receiverLastName = receiverLastName;
        this.birthDate = birthDate;
        this.amlSearchId = amlSerchId;
    }

    // 탈퇴, 휴면으로 익명화
    public void anonymize() {
        receiverFirstName = "*";
        receiverLastName = "*";
        birthDate = "*";
        amlSearchId = "*";
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
