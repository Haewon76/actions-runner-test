package com.cashmallow.api.domain.model.partner;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancePartner {

    public enum TypeOfFinance {
        /**
         * 송금
         */
        R,
        /**
         * 환전
         */
        E,
        /**
         * 환전,송금 둘다
         */
        A
    }

    private Long id;
    private Long partnerId;
    private String typeOfFinance;
    private String branchname;
    private String address;
    private String accountNo;
    private String accountName;
    private String bankName;
    private BigDecimal feeRate;
    private BigDecimal feePer;
    private String iso4217;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getTypeOfFinance() {
        return typeOfFinance;
    }

    public void setTypeOfFinance(String typeOfFinance) {
        this.typeOfFinance = typeOfFinance;
    }

    public String getBranchname() {
        return branchname;
    }

    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public BigDecimal getFeePer() {
        return feePer;
    }

    public void setFeePer(BigDecimal feePer) {
        this.feePer = feePer;
    }

    public String getIso4217() {
        return iso4217;
    }

    public void setIso4217(String iso4217) {
        this.iso4217 = iso4217;
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

}
