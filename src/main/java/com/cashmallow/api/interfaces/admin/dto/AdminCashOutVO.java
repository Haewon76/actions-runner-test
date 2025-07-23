package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;

import java.math.BigDecimal;
import java.sql.Timestamp;

// 기능: 관리자용 인출 조회 결과용 class
public class AdminCashOutVO {
    // 사용자 정보
    private String uCountry; // 사용자 국가
    private String uCountryName;
    private Long userId; // 사용자 ID.
    private String firstName; // 사용자 이름
    private String lastName; // 사용자 성
    private String email; // E-Mail
    private String birthDate; // birthday
    private String profilePhoto; // 사용자 profile 경로
    private String cls; // classification
    private String activated; // 사용여부

    // 여행자 정보
    private Long travelerId; // 여행자 ID.
    private String contactType;    // 연락처 유형
    private String contactId;      // 연락처 ID 또는 휴대폰번호

    // 가맹점 정보
    private String sCountry; // 가맹점주의 국가
    private String sCountryName; // 가맹점주의 국가
    private Long sUserId; // 가맹점주의 사용자 ID.
    private Long withdrawalPartnerId; // 가맹점 ID.
    private String shopName; // 가맹점 명
    private String businessNo; // 사업자 등록 번호
    private String shopAddr; // 가맹점 주소
    private String sFirstName; // 가맹점주의 이름
    private String sLastName; // 가맹점주의 성

    // 인출 정보
    private Long cashoutId;   // 인출 id.
    private String country;     // 가맹점주의 국가
    private String cashOutReservedDate;   // 인출 예약 일시
    private String flightArrivalDate;     // 항공편 도착 일시
    private String flightNo;               // 항공편명 
    private BigDecimal travelerCashOutAmt;
    private BigDecimal travelerCashOutFee;
    private BigDecimal travelerTotalCost;
    private BigDecimal withdrawalPartnerCashOutAmt;
    private BigDecimal withdrawalPartnerCashOutFee;
    private BigDecimal withdrawalPartnerTotalCost;
    private String coStatus; // 인출 상태
    private Timestamp createdDate; // 생성일(인출신청일)
    private Timestamp updatedDate; // 업데이트 일(인출취소일, 인출완료일)
    private String exchangeId; // 환전id
    private String mallowlinkTxnId;

    public String getU_country() {
        return uCountry;
    }

    public String getU_country_name() {
        return CountryCode.of(uCountry).getName();
    }
    public String getS_country_name() {
        return CountryCode.of(sCountry).getName();
    }

    public void setU_country(String u_country) {
        this.uCountry = u_country;
    }

    public Long getUser_id() {
        return userId;
    }

    public void setUser_id(Long user_id) {
        this.userId = user_id;
    }

    public String getFirst_name() {
        return firstName;
    }

    public void setFirst_name(String first_name) {
        this.firstName = first_name;
    }

    public String getLast_name() {
        return lastName;
    }

    public void setLast_name(String last_name) {
        this.lastName = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirth_date() {
        return birthDate;
    }

    public void setBirth_date(String birth_date) {
        this.birthDate = birth_date;
    }

    public String getProfile_photo() {
        return profilePhoto;
    }

    public void setProfile_photo(String profile_photo) {
        this.profilePhoto = profile_photo;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getActivated() {
        return activated;
    }

    public void setActivated(String activated) {
        this.activated = activated;
    }

    public Long getTraveler_id() {
        return travelerId;
    }

    public void setTraveler_id(Long traveler_id) {
        this.travelerId = traveler_id;
    }

    public String getS_country() {
        return sCountry;
    }

    public void setS_country(String s_country) {
        this.sCountry = s_country;
    }

    public Long getS_userId() {
        return sUserId;
    }

    public void setS_userId(Long sUserId) {
        this.sUserId = sUserId;
    }

    public Long getWithdrawal_partner_id() {
        return withdrawalPartnerId;
    }

    public void setWithdrawal_partner_id(Long withdrawal_partner_id) {
        this.withdrawalPartnerId = withdrawal_partner_id;
    }

    public String getShop_name() {
        return shopName;
    }

    public void setShop_name(String shop_name) {
        this.shopName = shop_name;
    }

    public String getBusiness_no() {
        return businessNo;
    }

    public void setBusiness_no(String business_no) {
        this.businessNo = business_no;
    }

    public String getShop_addr() {
        return shopAddr;
    }

    public void setShop_addr(String shop_addr) {
        this.shopAddr = shop_addr;
    }

    public String getS_first_name() {
        return sFirstName;
    }

    public void setS_first_name(String s_first_name) {
        this.sFirstName = s_first_name;
    }

    public String getS_last_name() {
        return sLastName;
    }

    public void setS_last_name(String s_last_name) {
        this.sLastName = s_last_name;
    }

    public Long getCashout_id() {
        return cashoutId;
    }

    public void setCashout_id(Long cashout_id) {
        this.cashoutId = cashout_id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getTraveler_cash_out_amt() {
        return travelerCashOutAmt;
    }

    public void setTraveler_cash_out_amt(BigDecimal traveler_cash_out_amt) {
        this.travelerCashOutAmt = traveler_cash_out_amt;
    }

    public BigDecimal getTraveler_cash_out_fee() {
        return travelerCashOutFee;
    }

    public void setTraveler_cash_out_fee(BigDecimal traveler_cash_out_fee) {
        this.travelerCashOutFee = traveler_cash_out_fee;
    }

    public BigDecimal getTraveler_total_cost() {
        return travelerTotalCost;
    }

    public void setTraveler_total_cost(BigDecimal traveler_total_cost) {
        this.travelerTotalCost = traveler_total_cost;
    }

    public BigDecimal getWithdrawal_partner_cash_out_amt() {
        return withdrawalPartnerCashOutAmt;
    }

    public void setWithdrawal_partner_cash_out_amt(BigDecimal withdrawal_partner_cash_out_amt) {
        this.withdrawalPartnerCashOutAmt = withdrawal_partner_cash_out_amt;
    }

    public BigDecimal getWithdrawal_partner_cash_out_fee() {
        return withdrawalPartnerCashOutFee;
    }

    public void setWithdrawal_partner_cash_out_fee(BigDecimal withdrawal_partner_cash_out_fee) {
        this.withdrawalPartnerCashOutFee = withdrawal_partner_cash_out_fee;
    }

    public BigDecimal getWithdrawal_partner_total_cost() {
        return withdrawalPartnerTotalCost;
    }

    public void setWithdrawal_partner_total_cost(BigDecimal withdrawal_partner_total_cost) {
        this.withdrawalPartnerTotalCost = withdrawal_partner_total_cost;
    }

    public String getCo_status() {
        return coStatus;
    }

    public void setCo_status(String co_status) {
        this.coStatus = co_status;
    }

    public Timestamp getCreated_date() {
        return createdDate;
    }

    public void setCreated_date(Timestamp created_date) {
        this.createdDate = created_date;
    }

    public Timestamp getUpdated_date() {
        return updatedDate;
    }

    public void setUpdated_date(Timestamp updated_date) {
        this.updatedDate = updated_date;
    }

    public String getContact_type() {
        return contactType;
    }

    public void setContact_type(String contact_type) {
        this.contactType = contact_type;
    }

    public String getContact_id() {
        return contactId;
    }

    public void setContact_id(String contact_id) {
        this.contactId = contact_id;
    }

    public String getCashout_reserved_date() {
        return cashOutReservedDate;
    }

    public void setCashOutReservedDate(String cashOutReservedDate) {
        this.cashOutReservedDate = cashOutReservedDate;
    }

    public String getFlight_arrival_date() {
        return flightArrivalDate;
    }

    public void setFlightArrivalDate(String flightArrivalDate) {
        this.flightArrivalDate = flightArrivalDate;
    }

    public String getFlight_no() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getExchange_id() {
        return exchangeId;
    }

    public void setExchange_id(String exchange_id) {
        this.exchangeId = exchange_id;
    }

    public String getMallowlink_txn_id() {
        return mallowlinkTxnId;
    }
    public void setMallowlinkTxnId(String mallowlinkTxnId) {
        this.mallowlinkTxnId = mallowlinkTxnId;
    }

}
