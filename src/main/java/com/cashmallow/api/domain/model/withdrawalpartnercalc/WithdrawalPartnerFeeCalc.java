package com.cashmallow.api.domain.model.withdrawalpartnercalc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain model for StorekeeperFeeCalc
 *
 * @author bongseok
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalPartnerFeeCalc {
    /**
     * storekeeper_calc fee_status normal : BF -> OP -> CF
     * storekeeper_calc fee_status cancle : BF -> OP -> BF / BF -> OP -> CF -> BF
     * storekeeper_calc fee_status delete : BF -> PE -> Delete Row
     *
     * @author bongseok
     */
    public enum FeeStatus {
        BF, // Before store settlement. 수수료 정산전
        OP, // Operating, Proceeding. 수수료 송금중
        PE, // Pending 수수료 정산 보류중
        CF, // Pay Confirm. 수수료 송금 완료
    }

    private Long id;
    private String country;
    private Long storekeeperId;
    private Timestamp beginDate;
    private Timestamp endDate;
    private BigDecimal cashOutAmt;
    private BigDecimal cashOutFee;
    private String bankName;
    private String bankAccountNo;
    private String bankAccountName;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;
    private String feeStatus;
    private String targetMonth;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getStorekeeperId() {
        return storekeeperId;
    }

    public void setStorekeeperId(Long storekeeperId) {
        this.storekeeperId = storekeeperId;
    }

    public Timestamp getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Timestamp beginDate) {
        this.beginDate = beginDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getCashOutAmt() {
        return cashOutAmt;
    }

    public void setCashOutAmt(BigDecimal cashOutAmt) {
        this.cashOutAmt = cashOutAmt;
    }

    public BigDecimal getCashOutFee() {
        return cashOutFee;
    }

    public void setCashOutFee(BigDecimal cashOutFee) {
        this.cashOutFee = cashOutFee;
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

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public String getFeeStatus() {
        return feeStatus;
    }

    public void setFeeStatus(String feeStatus) {
        this.feeStatus = feeStatus;
    }

    public String getTargetMonth() {
        return targetMonth;
    }

    public void setTargetMonth(String targetMonth) {
        this.targetMonth = targetMonth;
    }

}
