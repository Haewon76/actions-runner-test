package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveStorekeeperCalc
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveWithdrawalPartnerCalc {

    private Long id;
    private Long withdrawalPartnerId;
    private String bankAccountNo;
    private String bankAccountName;
    private Timestamp createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWithdrawalPartnerId() {
        return withdrawalPartnerId;
    }

    public void setWithdrawalPartnerId(Long withdrawalPartnerId) {
        this.withdrawalPartnerId = withdrawalPartnerId;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

}
