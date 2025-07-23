package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveTraveler
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveTraveler {

    private Long id;
    private Long userId;
    private String identificationNumber;
    private String enFirstName;
    private String enLastName;
    private String localFirstName;
    private String localLastName;
    private String certificationPhoto;
    private String accountNo;
    private String accountName;
    private String accountBankbookPhoto;
    private String contactType;
    private String contactId;
    private String address;
    private String addressSecondary;
    private String addressPhoto;
    private Timestamp createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getEnFirstName() {
        return enFirstName;
    }

    public void setEnFirstName(String enFirstName) {
        this.enFirstName = enFirstName;
    }

    public String getEnLastName() {
        return enLastName;
    }

    public void setEnLastName(String enLastName) {
        this.enLastName = enLastName;
    }

    public String getLocalFirstName() {
        return localFirstName;
    }

    public void setLocalFirstName(String localFirstName) {
        this.localFirstName = localFirstName;
    }

    public String getLocalLastName() {
        return localLastName;
    }

    public void setLocalLastName(String localLastName) {
        this.localLastName = localLastName;
    }

    public String getAccountBankbookPhoto() {
        return accountBankbookPhoto;
    }

    public void setAccountBankbookPhoto(String accountBankbookPhoto) {
        this.accountBankbookPhoto = accountBankbookPhoto;
    }

    public String getCertificationPhoto() {
        return certificationPhoto;
    }

    public void setCertificationPhoto(String certificationPhoto) {
        this.certificationPhoto = certificationPhoto;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressPhoto() {
        return addressPhoto;
    }

    public void setAddressPhoto(String addressPhoto) {
        this.addressPhoto = addressPhoto;
    }

    public String getAddressSecondary() {
        return addressSecondary;
    }

    public void setAddressSecondary(String addressSecondary) {
        this.addressSecondary = addressSecondary;
    }

}
