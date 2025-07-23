package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.interfaces.global.enums.JpRefundAccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundResponse {

    private Long id;
    private Long traveler_id;
    private String from_cd;
    private BigDecimal from_amt;
    private String country;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal exchange_rate;
    private BigDecimal base_exchange_rate;
    private String rf_request_status;
    private Timestamp created_date;
    private Timestamp updated_date;
    private BigDecimal fee_per_amt;
    private BigDecimal fee_rate_amt;

    private String tr_bank_name;
    private String tr_account_no;
    private String tr_account_name;
    private JpRefundAccountType jp_account_type;
    private String jp_bank_branch_name;

    private String related_txn_type;

    private Long wallet_id;
    private String exchange_ids;
    private Long remit_id;

    private Long refund_calc_id;
    private String refund_calc_status;

    private Long couponUserId;
    private BigDecimal discountAmount;

    private BigDecimal convertedAmount;

    public static RefundResponse of(NewRefund newRefund) {
        if (newRefund == null) {
            return null;
        }

        RefundResponse response = new RefundResponse();
        response.setId(newRefund.getId());
        response.setTraveler_id(newRefund.getTravelerId());
        response.setFrom_cd(newRefund.getFromCd());
        response.setFrom_amt(newRefund.getFromAmt());
        response.setCountry(newRefund.getToCd());
        response.setAmount(newRefund.getToAmt());
        response.setFee(newRefund.getFee());
        // response.setRate(refund.getRate());
        response.setExchange_rate(newRefund.getExchangeRate());
        response.setBase_exchange_rate(newRefund.getBaseExchangeRate());
        response.setRf_request_status(newRefund.getRefundStatus().name());
        response.setCreated_date(newRefund.getCreatedDate());
        response.setUpdated_date(newRefund.getUpdatedDate());
        response.setFee_per_amt(newRefund.getFeePerAmt());
        response.setFee_rate_amt(newRefund.getFeeRateAmt());
        response.setTr_bank_name(newRefund.getTrBankName());
        response.setTr_account_no(newRefund.getTrAccountNo());
        response.setTr_account_name(newRefund.getTrAccountName());
        response.setRelated_txn_type(newRefund.getRelatedTxnType().name());
        // response.setPaygate_rec_out_id(ㅜ.getPaygateRecOutId());
        response.setWallet_id(newRefund.getWalletId());
        response.setExchange_ids(newRefund.getExchangeIds());
        response.setRemit_id(newRefund.getRemitId());
        response.setRefund_calc_id(newRefund.getId());
        response.setRefund_calc_status(newRefund.getRefundStatus().name());
        response.setCouponUserId(newRefund.getCouponUserId());
        response.setDiscountAmount(newRefund.getCouponDiscountAmount());
        response.setConvertedAmount(calculateConvertedAmount(newRefund.getToAmt(), newRefund.getFeePerAmt(), newRefund.getCouponDiscountAmount()));

        response.setJp_account_type(newRefund.getJpAccountType());
        response.setJp_bank_branch_name(newRefund.getJpBankBranchName());
        return response;
    }

    private static BigDecimal calculateConvertedAmount(BigDecimal amount, BigDecimal feePerAmt, BigDecimal discountAmount) {
        if (ObjectUtils.isEmpty(feePerAmt)) {
            feePerAmt = BigDecimal.ZERO;
        }
        if (ObjectUtils.isEmpty(discountAmount)) {
            discountAmount = BigDecimal.ZERO;
        }
        return amount.add(feePerAmt).add(discountAmount);
    }
}
