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
public class WithdrawalPartnerFeeCalcRecord {
    /**
     * storekeeper_calc fee_status normal : BF -> OP -> CF
     * storekeeper_calc fee_status cancle : BF -> OP -> BF / BF -> OP -> CF -> BF
     * storekeeper_calc fee_status delete : BF -> PE -> Delete Row
     *
     * @author bongseok
     */

    private Long cashOutId;
    private Long travelerId;
    private Long withdrawalPartnerId;
    private Long withdrawalPartnerFeeCalcId;
    private BigDecimal withdrawalPartnerCashOutAmt;
    private BigDecimal withdrawalPartnerCashOutFee;
    private Timestamp createdDate;
    private Timestamp coStatusDate;

    public Long getCashOutId() {
        return cashOutId;
    }

    public void setCashOutId(Long cashOutId) {
        this.cashOutId = cashOutId;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public Long getWithdrawalPartnerId() {
        return withdrawalPartnerId;
    }

    public void setWithdrawalPartnerId(Long withdrawalPartnerId) {
        this.withdrawalPartnerId = withdrawalPartnerId;
    }

    public Long getWithdrawalPartnerFeeCalcId() {
        return withdrawalPartnerFeeCalcId;
    }

    public void setWithdrawalPartnerFeeCalcId(Long withdrawalPartnerFeeCalcId) {
        this.withdrawalPartnerFeeCalcId = withdrawalPartnerFeeCalcId;
    }

    public BigDecimal getWithdrawalPartnerCashOutAmt() {
        return withdrawalPartnerCashOutAmt;
    }

    public void setWithdrawalPartnerCashOutAmt(BigDecimal withdrawalPartnerCashOutAmt) {
        this.withdrawalPartnerCashOutAmt = withdrawalPartnerCashOutAmt;
    }

    public BigDecimal getWithdrawalPartnerCashOutFee() {
        return withdrawalPartnerCashOutFee;
    }

    public void setWithdrawalPartnerCashOutFee(BigDecimal withdrawalPartnerCashOutFee) {
        this.withdrawalPartnerCashOutFee = withdrawalPartnerCashOutFee;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getCoStatusDate() {
        return coStatusDate;
    }

    public void setCoStatusDate(Timestamp coStatusDate) {
        this.coStatusDate = coStatusDate;
    }
}
