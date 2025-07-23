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
public class RemittanceTravelerSnapshot {

    private Long remitId;
    private Long travelerId;
    private String identificationNumber;
    private String bankName;
    private String accountNo;
    private String accountName;
    private String address;
    private String addressCountry;
    private String addressCity;
    private String addressStateProvince;
    private String addressStateProvinceEn;
    private String addressSecondary;
    private String phoneNumber;
    private String phoneCountry;
    private Timestamp createdDate;

    // 탈퇴, 휴면으로 익명화
    public void anonymize() {
        identificationNumber = "*";
        accountNo = "*";
        accountName = "*";
        address = "*";
        addressSecondary = "*";
        phoneNumber = "*";
    }

    public Long getRemitId() {
        return remitId;
    }

    public void setRemitId(Long remitId) {
        this.remitId = remitId;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressStateProvince() {
        return addressStateProvince;
    }

    public void setAddressStateProvince(String addressStateProvince) {
        this.addressStateProvince = addressStateProvince;
    }

    public String getAddressStateProvinceEn() {
        return addressStateProvinceEn;
    }

    public void setAddressStateProvinceEn(String addressStateProvinceEn) {
        this.addressStateProvinceEn = addressStateProvinceEn;
    }

    public String getAddressSecondary() {
        return addressSecondary;
    }

    public void setAddressSecondary(String addressSecondary) {
        this.addressSecondary = addressSecondary;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneCountry() {
        return phoneCountry;
    }

    public void setPhoneCountry(String phoneCountry) {
        this.phoneCountry = phoneCountry;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}
