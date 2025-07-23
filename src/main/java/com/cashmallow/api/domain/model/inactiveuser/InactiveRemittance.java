package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveRemittance
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveRemittance {

    private Long id;
    private Long travelerId;
    private String receiverBirthDate;
    private String receiverPhoneNo;
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverBankAccountNo;
    private String receiverAddress;
    private String receiverAddressSecondary;
    private Timestamp createdDate;

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

    public String getReceiverBirthDate() {
        return receiverBirthDate;
    }

    public void setReceiverBirthDate(String receiverBirthDate) {
        this.receiverBirthDate = receiverBirthDate;
    }

    public String getReceiverPhoneNo() {
        return receiverPhoneNo;
    }

    public void setReceiverPhoneNo(String receiverPhoneNo) {
        this.receiverPhoneNo = receiverPhoneNo;
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

    public String getReceiverBankAccountNo() {
        return receiverBankAccountNo;
    }

    public void setReceiverBankAccountNo(String receiverBankAccountNo) {
        this.receiverBankAccountNo = receiverBankAccountNo;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverAddressSecondary() {
        return receiverAddressSecondary;
    }

    public void setReceiverAddressSecondary(String receiverAddressSecondary) {
        this.receiverAddressSecondary = receiverAddressSecondary;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}
