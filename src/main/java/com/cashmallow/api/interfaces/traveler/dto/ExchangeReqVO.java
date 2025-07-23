package com.cashmallow.api.interfaces.traveler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

// 기능: 환전 신청 시 사용되는 class
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeReqVO {
    // 환전신청 정보
    private Long user_id;
    private Integer bank_account_id;
    private Long traveler_id;
    private String from_cd;
    private BigDecimal from_money;
    private BigDecimal fee;
    private BigDecimal from_amt;
    private String to_cd;
    private BigDecimal to_money;
    private BigDecimal to_amt;
    private BigDecimal exchange_rate;
    private BigDecimal fee_per_amt;
    private BigDecimal fee_rate_amt;
    private String exchange_purpose;
    private String exchange_fund_source;

    // mapping 정보
    private Long mapping_id;

    private Long coupon_user_id;

    public Long getCoupon_user_id() {
        return coupon_user_id;
    }

    public void setCoupon_user_id(Long coupon_user_id) {
        this.coupon_user_id = coupon_user_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Integer getBank_account_id() {
        return bank_account_id;
    }

    public void setBank_account_id(Integer bank_account_id) {
        this.bank_account_id = bank_account_id;
    }

    public Long getTraveler_id() {
        return traveler_id;
    }

    public void setTraveler_id(Long traveler_id) {
        this.traveler_id = traveler_id;
    }

    public String getFrom_cd() {
        return from_cd;
    }

    public void setFrom_cd(String from_cd) {
        this.from_cd = from_cd;
    }

    public BigDecimal getFrom_money() {
        return from_money;
    }

    public void setFrom_money(BigDecimal from_money) {
        this.from_money = from_money;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getFrom_amt() {
        return from_amt;
    }

    public void setFrom_amt(BigDecimal from_amt) {
        this.from_amt = from_amt;
    }

    public String getTo_cd() {
        return to_cd;
    }

    public void setTo_cd(String to_cd) {
        this.to_cd = to_cd;
    }

    public BigDecimal getTo_money() {
        return to_money;
    }

    public void setTo_money(BigDecimal to_money) {
        this.to_money = to_money;
    }

    public BigDecimal getTo_amt() {
        return to_amt;
    }

    public void setTo_amt(BigDecimal to_amt) {
        this.to_amt = to_amt;
    }

    public BigDecimal getExchange_rate() {
        return exchange_rate;
    }

    public void setExchange_rate(BigDecimal exchange_rate) {
        this.exchange_rate = exchange_rate;
    }

    public Long getMapping_id() {
        return mapping_id;
    }

    public void setMapping_id(Long mapping_id) {
        this.mapping_id = mapping_id;
    }

    public BigDecimal getFee_per_amt() {
        return fee_per_amt;
    }

    public void setFee_per_amt(BigDecimal fee_per_amt) {
        this.fee_per_amt = fee_per_amt;
    }

    public BigDecimal getFee_rate_amt() {
        return fee_rate_amt;
    }

    public void setFee_rate_amt(BigDecimal fee_rate_amt) {
        this.fee_rate_amt = fee_rate_amt;
    }

    public String getExchange_purpose() {
        return exchange_purpose;
    }

    public void setExchange_purpose(String exchange_purpose) {
        this.exchange_purpose = exchange_purpose;
    }

    public String getExchange_fund_source() {
        return exchange_fund_source;
    }

    public void setExchange_fund_source(String exchange_fund_source) {
        this.exchange_fund_source = exchange_fund_source;
    }


    public boolean checkValidation() {
        return bank_account_id != null
                && from_cd != null && !from_cd.isEmpty()
                //            && from_money != null
                && fee != null
                && from_amt != null
                && to_cd != null && !to_cd.isEmpty()
                //            && to_money != null
                && to_amt != null
                && exchange_rate != null;
        //            && ((from_money.compareTo(new BigDecimal(0)) > 0 && to_amt.compareTo(new BigDecimal(0)) == 0)
        //                    || (from_money.compareTo(new BigDecimal(0)) == 0 && to_amt.compareTo(new BigDecimal(0)) > 0));
        //        && ((from_money.compareTo(new BigDecimal(0)) > 0 && to_money.compareTo(new BigDecimal(0)) == 0)
        //                || (from_money.compareTo(new BigDecimal(0)) == 0 && to_money.compareTo(new BigDecimal(0)) > 0));
    }

}
