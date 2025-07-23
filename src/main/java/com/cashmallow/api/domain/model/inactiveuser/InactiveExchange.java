package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveExchange
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveExchange {

    private Long id;
    private Long travelerId;
    private String identificationNumber;
    private String trAccountName;
    private String trAccountNo;
    private String trAccountBankbookPhoto;
    private String trPhoneNumber;
    private String trAddress;
    private String trAddressSecondary;
    private String trAddressPhoto;
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

    public String getTrAccountName() {
        return trAccountName;
    }

    public void setTrAccountName(String trAccountName) {
        this.trAccountName = trAccountName;
    }

    public String getTrAccountNo() {
        return trAccountNo;
    }

    public void setTrAccountNo(String trAccountNo) {
        this.trAccountNo = trAccountNo;
    }

    public String getTrAccountBankbookPhoto() {
        return trAccountBankbookPhoto;
    }

    public void setTrAccountBankbookPhoto(String trAccountBankbookPhoto) {
        this.trAccountBankbookPhoto = trAccountBankbookPhoto;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getTrPhoneNumber() {
        return trPhoneNumber;
    }

    public void setTrPhoneNumber(String trPhoneNumber) {
        this.trPhoneNumber = trPhoneNumber;
    }

    public String getTrAddress() {
        return trAddress;
    }

    public void setTrAddress(String trAddress) {
        this.trAddress = trAddress;
    }

    public String getTrAddressPhoto() {
        return trAddressPhoto;
    }

    public void setTrAddressPhoto(String trAddressPhoto) {
        this.trAddressPhoto = trAddressPhoto;
    }

    public String getTrAddressSecondary() {
        return trAddressSecondary;
    }

    public void setTrAddressSecondary(String trAddressSecondary) {
        this.trAddressSecondary = trAddressSecondary;
    }

}
