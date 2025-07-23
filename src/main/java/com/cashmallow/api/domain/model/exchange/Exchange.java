package com.cashmallow.api.domain.model.exchange;

import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Exchenge Domain model
 * 2018.03.02 ExchangeVO를 복사하여 camel case로 변경하여 생성
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Exchange {

    public enum ExStatus {
        OP,     // Exchange in progress
        DR,        // Deposit receipt Reregistration 영수증 재등록
        CF,     // Exchange is complete
        TC,     // Traveler cancel
        CC      // Cashmallow cancel
    }

    // exchange 정보
    private Long id;
    private Integer bankAccountId;
    private Long travelerId;

    // 여권정보 추가
    private String identificationNumber;

    private String fromCd;
    private String toCd;
    private BigDecimal fromAmt;
    private BigDecimal toAmt;
    private BigDecimal fee;
    private String exStatus;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private RemittancePurpose exchangePurpose;
    @Getter
    @Setter
    private RemittanceFundSource exchangeFundSource;

    @Getter
    @Setter
    private String rejectMessage;
    private BigDecimal exchangeRate;
    private BigDecimal feePerAmt;
    private BigDecimal feeRateAmt;
    @Getter
    @Setter
    private BigDecimal feeRate;


    // 테이블의 컬럼과 일치 시키기 위해 vo추가 2017.08.11 추가
    private String fcmYn;
    private String trBankName;
    private String trAccountName;
    private String trAccountNo;
    private BigDecimal trFromAmt;
    private Timestamp trDepositDate;
    private String trReceiptPhoto;

    private String trAddress;

    @Getter
    @Setter
    private String message;
    private String trAddressCountry;
    private String trAddressCity;
    private String trAddressSecondary;
    private String trAddressPhoto;
    private String trPhoneNumber;
    private String trPhoneCountry;

    private Timestamp exStatusDate;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;

    // bank_account 정보
    private String country;
    private String bankCode;
    private String bankName;
    private String bankAccountNo;
    private String accountType;
    private String firstName;
    private String lastName;
    private String trAccountBankbookPhoto;

    // mapping 정보
    private Integer refValue; // 2016.12.30 추가

    private Long couponUserId;

    private BigDecimal couponDiscountAmt;

    public BigDecimal getCouponDiscountAmt() {
        return couponDiscountAmt;
    }

    public void setCouponDiscountAmt(BigDecimal couponDiscountAmt) {
        this.couponDiscountAmt = couponDiscountAmt;
    }

    public Long getCouponUserId() {
        return couponUserId;
    }

    public void setCouponUserId(Long couponUserId) {
        this.couponUserId = couponUserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Integer bankAccountId) {
        this.bankAccountId = bankAccountId;
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

    public String getFromCd() {
        return fromCd;
    }

    public void setFromCd(String fromCd) {
        this.fromCd = fromCd;
    }

    public String getToCd() {
        return toCd;
    }

    public void setToCd(String toCd) {
        this.toCd = toCd;
    }

    public BigDecimal getFromAmt() {
        return fromAmt;
    }

    public void setFromAmt(BigDecimal fromAmt) {
        this.fromAmt = fromAmt;
    }

    public BigDecimal getToAmt() {
        return toAmt;
    }

    public void setToAmt(BigDecimal toAmt) {
        this.toAmt = toAmt;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getExStatus() {
        return exStatus;
    }

    public void setExStatus(String exStatus) {
        this.exStatus = exStatus;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getFcmYn() {
        return fcmYn;
    }

    public void setFcmYn(String fcmYn) {
        this.fcmYn = fcmYn;
    }

    public String getTrBankName() {
        return trBankName;
    }

    public void setTrBankName(String trBankName) {
        this.trBankName = trBankName;
    }

    public String getTrAccountName() {
        return trAccountName;
    }

    public void setTrAccountName(String trAccountName) {
        this.trAccountName = trAccountName;
    }

    public String getTrAccountNo() {
        return trAccountNo;
    }

    public void setTrAccountNo(String trAccountNo) {
        this.trAccountNo = trAccountNo;
    }

    public BigDecimal getTrFromAmt() {
        return trFromAmt;
    }

    public void setTrFromAmt(BigDecimal trFromAmt) {
        this.trFromAmt = trFromAmt;
    }

    public Timestamp getTrDepositDate() {
        return trDepositDate;
    }

    public void setTrDepositDate(Timestamp trDepositDate) {
        this.trDepositDate = trDepositDate;
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

    public Timestamp getExStatusDate() {
        return exStatusDate;
    }

    public void setExStatusDate(Timestamp exStatusDate) {
        this.exStatusDate = exStatusDate;
    }

    public String getTrReceiptPhoto() {
        return trReceiptPhoto;
    }

    public void setTrReceiptPhoto(String trReceiptPhoto) {
        this.trReceiptPhoto = trReceiptPhoto;
    }

    public String getTrAccountBankbookPhoto() {
        return trAccountBankbookPhoto;
    }

    public void setTrAccountBankbookPhoto(String trAccountBankbookPhoto) {
        this.trAccountBankbookPhoto = trAccountBankbookPhoto;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public String getTrAddress() {
        return trAddress;
    }

    public void setTrAddress(String trAddress) {
        this.trAddress = trAddress;
    }

    public String getTrAddressPhoto() {
        return trAddressPhoto;
    }

    public void setTrAddressPhoto(String trAddressPhoto) {
        this.trAddressPhoto = trAddressPhoto;
    }

    public String getTrPhoneNumber() {
        return trPhoneNumber;
    }

    public void setTrPhoneNumber(String trPhoneNumber) {
        this.trPhoneNumber = trPhoneNumber;
    }

    public String getTrAddressCountry() {
        return trAddressCountry;
    }

    public void setTrAddressCountry(String trAddressCountry) {
        this.trAddressCountry = trAddressCountry;
    }

    public String getTrAddressCity() {
        return trAddressCity;
    }

    public void setTrAddressCity(String trAddressCity) {
        this.trAddressCity = trAddressCity;
    }

    public String getTrAddressSecondary() {
        return trAddressSecondary;
    }

    public void setTrAddressSecondary(String trAddressSecondary) {
        this.trAddressSecondary = trAddressSecondary;
    }

    public String getTrPhoneCountry() {
        return trPhoneCountry;
    }

    public void setTrPhoneCountry(String trPhoneCountry) {
        this.trPhoneCountry = trPhoneCountry;
    }

    public BigDecimal getFeePerAmt() {
        return feePerAmt;
    }

    public void setFeePerAmt(BigDecimal feePerAmt) {
        this.feePerAmt = feePerAmt;
    }

    public BigDecimal getFeeRateAmt() {
        return feeRateAmt;
    }

    public void setFeeRateAmt(BigDecimal feeRateAmt) {
        this.feeRateAmt = feeRateAmt;
    }

    public boolean checkValidation() {
        return bankAccountId != null
                && fromCd != null && !fromCd.isEmpty()
                && fee != null
                && fromAmt != null
                && toCd != null && !toCd.isEmpty()
                && toAmt != null
                && exchangeRate != null;
    }

    public boolean checkValidationKr() {
        return fromCd != null && !fromCd.isEmpty()
                && fee != null
                && fromAmt != null
                && toCd != null && !toCd.isEmpty()
                && toAmt != null
                && exchangeRate != null;
    }

    public void updateExchangeRate(ExchangeCalcVO exchangeCalcVO) {
        this.exchangeRate = exchangeCalcVO.getExchange_rate();

        this.fee = exchangeCalcVO.getFee();
        this.feePerAmt = exchangeCalcVO.getFee_per_amt();
        this.feeRateAmt = exchangeCalcVO.getFee_rate_amt();

        this.fromAmt = exchangeCalcVO.getFee().add(exchangeCalcVO.getFrom_money());
        this.fromCd = exchangeCalcVO.getFrom_cd();

        this.toCd = exchangeCalcVO.getTo_cd();
        this.toAmt = exchangeCalcVO.getTo_money();

        this.couponUserId = exchangeCalcVO.getCouponUserId();
    }

}
