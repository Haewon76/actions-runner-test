package com.cashmallow.api.domain.model.cashout;

import com.cashmallow.api.domain.shared.Entity;
import com.cashmallow.common.CustomStringUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain model for Refund
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashOut implements Entity<CashOut> {

    public enum CoStatus {
        /**
         * Cash-out in progress
         */
        OP,
        /**
         * Cash-out is complete
         */
        CF,
        /**
         * Cash-out is canceled by Cashmallow
         */
        CC,
        /**
         * Cash-out is canceled by Storekeeper
         */
        SC,
        /**
         * Cash-out is canceled by Traveler
         */
        TC,
        /**
         * Payment in progress
         */
        PO,
        /**
         * Payment is complete
         */
        PF,
        /**
         * Payment is canceled by Cashmallow
         */
        PT
    }

    // for CaStatus, see StorekeeperCalc class

    private Long id;
    private Long travelerId;
    private Long walletId;
    private Long withdrawalPartnerId;
    private Long withdrawalPartnerCalcId;
    private Long withdrawalPartnerFeeCalcId;
    private BigDecimal travelerCashOutAmt;
    private BigDecimal travelerCashOutFee;
    private BigDecimal travelerTotalCost;
    private String country;
    private String exchangeIds;
    private BigDecimal withdrawalPartnerCashOutAmt;
    private BigDecimal withdrawalPartnerCashOutFee;
    private BigDecimal withdrawalPartnerTotalCost;
    private String qrCodeValue;
    private String qrCodeSource;
    private String coStatus;
    private String fcmYn;
    private String caStatus;
    private Timestamp coStatusDate;
    private String partnerTxnId;// 외부 파트너의 거래번호. 파트너가 지정.
    private String casmTxnId; // 외부에 요청할 때 거래번호. 우리가 지정.
    private Long exchangeId;

    private Boolean privacySharingAgreement;
    private String cashoutReservedDate;
    private String flightArrivalDate;
    private String flightNo;

    private Timestamp createdDate;
    private Timestamp updatedDate;

    public CashOut() {
        super();
    }

    public CashOut(Long travelerId, Long withdrawalPartnerId, String country,
                   BigDecimal travelerCashOutAmt, BigDecimal travelerCashOutFee,
                   BigDecimal withdrawalPartnerCashOutAmt, BigDecimal withdrawalPartnerCashOutFee,
                   String qrCodeValue, String qrCodeSource, String exchangeIds, Long walletId) {

        this.travelerId = travelerId;
        this.withdrawalPartnerId = withdrawalPartnerId;
        this.country = country;
        this.travelerCashOutAmt = travelerCashOutAmt;
        this.travelerCashOutFee = travelerCashOutFee;
        this.withdrawalPartnerCashOutAmt = withdrawalPartnerCashOutAmt;
        this.withdrawalPartnerCashOutFee = withdrawalPartnerCashOutFee;

        this.travelerTotalCost = travelerCashOutAmt.add(travelerCashOutFee);
        this.withdrawalPartnerTotalCost = withdrawalPartnerCashOutAmt.add(withdrawalPartnerCashOutFee);

        if (StringUtils.isEmpty(qrCodeValue)) {
            qrCodeValue = CustomStringUtil.generateQrCode();
            qrCodeSource = "Cashmallow QR code";
        }

        this.qrCodeValue = qrCodeValue;
        this.qrCodeSource = qrCodeSource;

        this.exchangeIds = exchangeIds;
        this.walletId = walletId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getExchangeIds() {
        return exchangeIds;
    }

    public void setExchangeIds(String exchangeIds) {
        this.exchangeIds = exchangeIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getWithdrawalPartnerCalcId() {
        return withdrawalPartnerCalcId;
    }

    public void setWithdrawalPartnerCalcId(Long withdrawalPartnerCalcId) {
        this.withdrawalPartnerCalcId = withdrawalPartnerCalcId;
    }

    public Long getWithdrawalPartnerFeeCalcId() {
        return withdrawalPartnerFeeCalcId;
    }

    public void setWithdrawalPartnerFeeCalcId(Long withdrawalPartnerFeeCalcId) {
        this.withdrawalPartnerFeeCalcId = withdrawalPartnerFeeCalcId;
    }

    public BigDecimal getTravelerCashOutAmt() {
        return travelerCashOutAmt;
    }

    public void setTravelerCashOutAmt(BigDecimal travelerCashOutAmt) {
        this.travelerCashOutAmt = travelerCashOutAmt;
    }

    public BigDecimal getTravelerCashOutFee() {
        return travelerCashOutFee;
    }

    public void setTravelerCashOutFee(BigDecimal travelerCashOutFee) {
        this.travelerCashOutFee = travelerCashOutFee;
    }

    public BigDecimal getTravelerTotalCost() {
        return travelerTotalCost;
    }

    public void setTravelerTotalCost(BigDecimal travelerTotalCost) {
        this.travelerTotalCost = travelerTotalCost;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public BigDecimal getWithdrawalPartnerTotalCost() {
        return withdrawalPartnerTotalCost;
    }

    public void setWithdrawalPartnerTotalCost(BigDecimal withdrawalPartnerTotalCost) {
        this.withdrawalPartnerTotalCost = withdrawalPartnerTotalCost;
    }

    public String getQrCodeValue() {
        return qrCodeValue;
    }

    public void setQrCodeValue(String qrCodeValue) {
        this.qrCodeValue = qrCodeValue;
    }

    public String getQrCodeSource() {
        return qrCodeSource;
    }

    public void setQrCodeSource(String qrCodeSource) {
        this.qrCodeSource = qrCodeSource;
    }

    public String getCoStatus() {
        return coStatus;
    }

    public CoStatus getCoStatusEnum() {
        return CoStatus.valueOf(coStatus);
    }

    public void setCoStatus(String coStatus) {
        this.coStatus = coStatus;
    }

    public String getFcmYn() {
        return fcmYn;
    }

    public void setFcmYn(String fcmYn) {
        this.fcmYn = fcmYn;
    }

    public String getCaStatus() {
        return caStatus;
    }

    public void setCaStatus(String caStatus) {
        this.caStatus = caStatus;
    }

    public Timestamp getCoStatusDate() {
        return coStatusDate;
    }

    public void setCoStatusDate(Timestamp coStatusDate) {
        this.coStatusDate = coStatusDate;
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

    public String getPartnerTxnId() {
        return partnerTxnId;
    }

    public void setPartnerTxnId(String partnerTxnId) {
        this.partnerTxnId = partnerTxnId;
    }


    @Override
    public boolean sameIdentityAs(CashOut other) {
        return other != null && other.id == this.id;
    }

    public Boolean getPrivacySharingAgreement() {
        return privacySharingAgreement;
    }

    public void setPrivacySharingAgreement(Boolean privacySharingAgreement) {
        this.privacySharingAgreement = privacySharingAgreement;
    }

    public String getCashoutReservedDate() {
        return cashoutReservedDate;
    }

    public void setCashoutReservedDate(String cashoutReservedDate) {
        this.cashoutReservedDate = cashoutReservedDate;
    }

    public String getFlightArrivalDate() {
        return flightArrivalDate;
    }

    public void setFlightArrivalDate(String flightArrivalDate) {
        this.flightArrivalDate = flightArrivalDate;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getCasmTxnId() {
        return casmTxnId;
    }

    public void setCasmTxnId(String casmTxnId) {
        this.casmTxnId = casmTxnId;
    }

    public Long getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
    }

}