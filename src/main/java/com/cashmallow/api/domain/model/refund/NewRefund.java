package com.cashmallow.api.domain.model.refund;

import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.interfaces.global.enums.JpRefundAccountType;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.cashmallow.api.interfaces.traveler.dto.RefundJpRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

/**
 * Refund 신규 모델
 *
 * @author gtop
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class NewRefund {

    public enum RefundStatusCode {
        OP, // 환불 신청
        AP, // 자동진행 중(Auto Process)
        MP, // 수동진행 중(Manual Process), 접수상태
        CC, // 회사 취소(Cashmallow Cancel)
        TC, // 고객 취소(Traveler Cancel)
        CF,  // 환불 완료(Confirm)
        SB  // 환불 신청 대기상태(Standby)
    }

    private Long id;
    private Long travelerId;
    private TransactionRecord.RelatedTxnType relatedTxnType;
    private Long remitId;
    private Long exchangeId;
    private Long walletId;
    private String fromCd;
    private BigDecimal fromAmt;
    private String toCd;
    private BigDecimal toAmt;
    private BigDecimal fee;
    private BigDecimal feePerAmt;
    private BigDecimal feeRateAmt;
    private BigDecimal feeRate;
    private BigDecimal exchangeRate;
    private BigDecimal baseExchangeRate;
    private RefundStatusCode refundStatus;
    private Long trBankInfoId;
    private String trBankName;
    private String trAccountNo;
    private String trAccountName;
    private JpRefundAccountType jpAccountType;
    private String jpBankBranchName;
    private Long couponUserId;
    private BigDecimal couponDiscountAmount;
    private String paygateRecOutId;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public String getExchangeIds() {
        if(exchangeId == null) {
            return null;
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        array.add(exchangeId);
        // {"exchange_ids": [28897]}
        json.add("exchange_ids", array);
        return json.toString();
    }

    // 기존 앱에서 조회 가능하게 레거시 필드 추가
    public String getRefundCalcStatus() {
        return refundStatus.name();
    }

    public String getRfRequestStatus() {
        return refundStatus.name();
    }

    public String getCountry() {
        return toCd;
    }
    public BigDecimal getAmount() {
        return toAmt;
    }

    public static NewRefund of(RefundJpRequest refundJpRequest, JpRefundAccountInfo jpRefundAccountInfo) {
        NewRefund requestNewRefund = new NewRefund();
        requestNewRefund.setFromCd(refundJpRequest.from_cd());
        requestNewRefund.setFromAmt(refundJpRequest.from_amt());
        requestNewRefund.setToCd(refundJpRequest.to_cd());
        requestNewRefund.setToAmt(refundJpRequest.to_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFee(refundJpRequest.fee().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setExchangeRate(refundJpRequest.exchange_rate().setScale(6, RoundingMode.HALF_UP));
        requestNewRefund.setFeePerAmt(refundJpRequest.fee_per_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFeeRateAmt(refundJpRequest.fee_rate_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setRefundStatus(NewRefund.RefundStatusCode.MP);

        requestNewRefund.setTrBankName(jpRefundAccountInfo.getBankName());
        requestNewRefund.setTrAccountNo(jpRefundAccountInfo.getAccountNo());
        requestNewRefund.setTrAccountName(jpRefundAccountInfo.getLocalLastName() + jpRefundAccountInfo.getLocalFirstName());
        requestNewRefund.setJpAccountType(jpRefundAccountInfo.getAccountType());
        requestNewRefund.setJpBankBranchName(jpRefundAccountInfo.getBranchName());
        return requestNewRefund;
    }

    public static NewRefund of(RefundJpRequest refundJpRequest) {
        NewRefund requestNewRefund = new NewRefund();
        requestNewRefund.setFromCd(refundJpRequest.from_cd());
        requestNewRefund.setFromAmt(refundJpRequest.from_amt());
        requestNewRefund.setToCd(refundJpRequest.to_cd());
        requestNewRefund.setToAmt(refundJpRequest.to_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFee(refundJpRequest.fee().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setExchangeRate(refundJpRequest.exchange_rate().setScale(6, RoundingMode.HALF_UP));
        requestNewRefund.setFeePerAmt(refundJpRequest.fee_per_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFeeRateAmt(refundJpRequest.fee_rate_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setRefundStatus(NewRefund.RefundStatusCode.SB);

        requestNewRefund.setTrBankName("");
        requestNewRefund.setTrAccountNo("");
        requestNewRefund.setTrAccountName("");
        requestNewRefund.setJpAccountType(JpRefundAccountType.NORMAL);
        requestNewRefund.setJpBankBranchName("");
        return requestNewRefund;
    }

    public static NewRefund of(ExchangeCalcVO calcVO, TravelerWallet wallet, BigDecimal toAmt, Traveler traveler) {
        NewRefund requestNewRefund = new NewRefund();
        requestNewRefund.setFromCd(wallet.getToCountry().getCode());
        requestNewRefund.setFromAmt(calcVO.getFrom_money());
        requestNewRefund.setToCd(wallet.getRootCd());
        requestNewRefund.setToAmt(toAmt);
        requestNewRefund.setFee(calcVO.getFee().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setExchangeRate(calcVO.getExchange_rate().setScale(6, RoundingMode.HALF_UP));
        requestNewRefund.setFeePerAmt(calcVO.getFee_per_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFeeRateAmt(calcVO.getFee_rate_amt().setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setRefundStatus(NewRefund.RefundStatusCode.SB);
        requestNewRefund.setWalletId(wallet.getId());

        requestNewRefund.setTrBankName(traveler.getBankName());
        requestNewRefund.setTrAccountNo(traveler.getAccountNo());
        requestNewRefund.setTrAccountName(traveler.getAccountName());
        requestNewRefund.setJpAccountType(JpRefundAccountType.NORMAL);
        requestNewRefund.setJpBankBranchName("");
        return requestNewRefund;
    }

    public void updateRefundAccountInfo(JpRefundAccountInfo jpRefundAccountInfo) {
        this.setTrBankName(jpRefundAccountInfo.getBankName());
        this.setTrAccountNo(jpRefundAccountInfo.getAccountNo());
        this.setTrAccountName(jpRefundAccountInfo.getLocalLastName() + jpRefundAccountInfo.getLocalFirstName());
        this.setJpAccountType(jpRefundAccountInfo.getAccountType());
        this.setJpBankBranchName(jpRefundAccountInfo.getBranchName());
    }
}
