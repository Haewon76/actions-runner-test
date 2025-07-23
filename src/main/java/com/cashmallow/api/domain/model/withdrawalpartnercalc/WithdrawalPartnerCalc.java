package com.cashmallow.api.domain.model.withdrawalpartnercalc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain model for RefundCalc
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalPartnerCalc {

    /**
     * cash_out table normal   : BF -> P1 ->       CF/PF ->
     * cash_out table cancel   :                                  CF/PF -> BF
     * storekeeper_calc normal :             OP ->          SD -> CF
     * storekeeper_calc pending:             OP ->          PE
     * storekeeper_calc cancel :                                  CF/PE -> delete row
     *
     * @author swshin
     */
    public enum CaStatus {
        BF, // Before store settlement. Used in cash_out table.
        P1, // Phase1 marking for settlement. Used in cash_out table.
        OP, // Operating, Proceeding. 
        SD, // Send Money
        PE, // Pending
        CF, // Cash-out Complete
        PF  // Payment Complete
    }

    private Long id;
    private String country;
    private Long withdrawalPartnerId;
    private Timestamp beginDate;
    private Timestamp endDate;
    private BigDecimal cashOutAmt;
    private BigDecimal cashOutFee;
    private BigDecimal paymentAmt;
    private BigDecimal totalAmt;
    private String bankName;
    private String bankAccountNo;
    private String bankAccountName;
    private String caStatus;
    private Timestamp caStatusDate;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;

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

    public Long getWithdrawalPartnerId() {
        return withdrawalPartnerId;
    }

    public void setWithdrawalPartnerId(Long storekeeperId) {
        this.withdrawalPartnerId = storekeeperId;
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

    public BigDecimal getPaymentAmt() {
        return paymentAmt;
    }

    public void setPaymentAmt(BigDecimal paymentAmt) {
        this.paymentAmt = paymentAmt;
    }

    public BigDecimal getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(BigDecimal totalAmt) {
        this.totalAmt = totalAmt;
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

    public String getCaStatus() {
        return caStatus;
    }

    public void setCaStatus(String caStatus) {
        this.caStatus = caStatus;
    }

    public Timestamp getCaStatusDate() {
        return caStatusDate;
    }

    public void setCaStatusDate(Timestamp caStatusDate) {
        this.caStatusDate = caStatusDate;
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


}
