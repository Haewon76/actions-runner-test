package com.cashmallow.api.domain.model.partner;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalPartner {

    @RequiredArgsConstructor
    public enum KindOfStorekeeper {
        /**
         * The stores in a city
         */
        C(0),
        /**
         * The stores in an airport
         */
        A(0),
        /**
         * The Seven Bank ATM(Machine) in Japan
         */
        M001(0),
        /**
         * The Coocon ATM(Machine), aka COATM in Korea
         */
        M002(0),
        /**
         * The SOCASH(Partner) in Indonesia, Malaysia and Singapore
         */
        P001(0),
        /**
         * SCB
         */
        SCB(0),
        BNI(0),
        RCBC(0),
        AJ(0),
        ;

        @Getter
        private final int partnerId;
    }

    private Long id;
    private Long userId;
    private Long partnerId;
    private String shopName;
    private String businessNo;
    private Timestamp bizExpDate;
    private String shopAddr;
    private String shopPhoto;
    private String businessPhoto;
    private Double shopLat;
    private Double shopLng;
    private BigDecimal feePer;
    private BigDecimal feeRate;
    private String bizNoOk;
    private Timestamp bizNoOkDate;
    private String accountNo;
    private String accountName;
    private String bankName;
    private String accountOk;
    private Timestamp accountOkDate;
    private BigDecimal fxPossibleAmt;
    private String about;
    private String kindOfStorekeeper;
    private String gmCountryCode;

    private Integer cashOutStartAt;
    private Integer cashOutEndAt;
    private String cashOutHours;

    private String holidayDay;    // 요일별 정기 휴무 (ex) 토, 일요일 휴무 인 경우 '[1, 7]'
    private String holidayDate;   // 일자별 휴무 (ex) 2018년 1월 1일 ~ 2018년 1월 3일 인 경우 '["2018-01-01", "2018-01-02", "2018-01-03"]'

    private Timestamp lastCashOutTime;
    private String shopContactNumber;

    private String cashOutService;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;

    private String country;
    @Getter
    @Setter
    private boolean cancelable; // 취소 기능이 있는지 여부 (AJ는 취소 없음)

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGmCountryCode() {
        return gmCountryCode;
    }

    public void setGmCountryCode(String gmCountryCode) {
        this.gmCountryCode = gmCountryCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public Timestamp getBizExpDate() {
        return bizExpDate;
    }

    public void setBizExpDate(Timestamp bizExpDate) {
        this.bizExpDate = bizExpDate;
    }

    public String getShopAddr() {
        return shopAddr;
    }

    public void setShopAddr(String shopAddr) {
        this.shopAddr = shopAddr;
    }

    public String getShopPhoto() {
        return shopPhoto;
    }

    public void setShopPhoto(String shopPhoto) {
        this.shopPhoto = shopPhoto;
    }

    public String getBusinessPhoto() {
        return businessPhoto;
    }

    public void setBusinessPhoto(String businessPhoto) {
        this.businessPhoto = businessPhoto;
    }

    public Double getShopLat() {
        return shopLat;
    }

    public void setShopLat(Double shopLat) {
        this.shopLat = shopLat;
    }

    public Double getShopLng() {
        return shopLng;
    }

    public void setShopLng(Double shopLng) {
        this.shopLng = shopLng;
    }

    public BigDecimal getFeePer() {
        return feePer;
    }

    public void setFeePer(BigDecimal feePer) {
        this.feePer = feePer;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public String getBizNoOk() {
        return bizNoOk;
    }

    public void setBizNoOk(String bizNoOk) {
        this.bizNoOk = bizNoOk;
    }

    public Timestamp getBizNoOkDate() {
        return bizNoOkDate;
    }

    public void setBizNoOkDate(Timestamp bizNoOkDate) {
        this.bizNoOkDate = bizNoOkDate;
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

    public String getAccountOk() {
        return accountOk;
    }

    public void setAccountOk(String accountOk) {
        this.accountOk = accountOk;
    }

    public Timestamp getAccountOkDate() {
        return accountOkDate;
    }

    public void setAccountOkDate(Timestamp accountOkDate) {
        this.accountOkDate = accountOkDate;
    }

    public BigDecimal getFxPossibleAmt() {
        return fxPossibleAmt;
    }

    public void setFxPossibleAmt(BigDecimal fxPossibleAmt) {
        this.fxPossibleAmt = fxPossibleAmt;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Integer getCashOutStartAt() {
        return cashOutStartAt;
    }

    public void setCashOutStartAt(Integer cashOutStartAt) {
        this.cashOutStartAt = cashOutStartAt;
    }

    public Integer getCashOutEndAt() {
        return cashOutEndAt;
    }

    public void setCashOutEndAt(Integer cashOutEndAt) {
        this.cashOutEndAt = cashOutEndAt;
    }

    public String getCashOutHours() {
        return cashOutHours;
    }

    public void setCashOutHours(String cashOutHours) {
        this.cashOutHours = cashOutHours;
    }

    public String getHolidayDay() {
        return holidayDay;
    }

    public void setHolidayDay(String holidayDay) {
        this.holidayDay = holidayDay;
    }

    public String getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate;
    }

    public Timestamp getLastCashOutTime() {
        return lastCashOutTime;
    }

    public void setLastCashOutTime(Timestamp lastCashOutTime) {
        this.lastCashOutTime = lastCashOutTime;
    }

    public String getShopContactNumber() {
        return shopContactNumber;
    }

    public void setShopContactNumber(String shopContactNumber) {
        this.shopContactNumber = shopContactNumber;
    }

    public String getCashOutService() {
        return cashOutService;
    }

    public void setCashOutService(String cashOutService) {
        this.cashOutService = cashOutService;
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

    public String getKindOfStorekeeper() {
        return kindOfStorekeeper;
    }

    public KindOfStorekeeper getStorekeeperType() {
        return KindOfStorekeeper.valueOf(getKindOfStorekeeper());
    }

    public void setKindOfStorekeeper(String kindOfStorekeeper) {
        this.kindOfStorekeeper = kindOfStorekeeper;
    }


}
