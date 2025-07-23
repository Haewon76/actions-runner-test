package com.cashmallow.api.domain.model.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccount {
    private Integer id;
    private Integer companyId;
    private String country;
    private String bankCode;
    private String bankName;
    private String branchName;
    private String bankAccountNo;
    private String accountType;
    private Integer sortOrder;
    private String useYn;
    private String firstName;
    private String lastName;
    private Integer refValue;
    private Timestamp createdDate;

    public enum BankAccountType {
        /**
         * Cashmallow
         */
        CASHMALLOW,
        /**
         * Paygate
         */
        PAYGATE;

        @Value(value = "${paygate.bankAccountId}")
        private List<Long> paygateAccountIds = new ArrayList<>();

        public Boolean isPaygateAccountId(Long bankAccountId) {
            for (Long accountId : paygateAccountIds) {
                if (accountId.equals(bankAccountId)) {
                    return true;
                }
            }
            return false;
        }
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getRefValue() {
        return refValue;
    }

    public void setRefValue(Integer refValue) {
        this.refValue = refValue;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

}
