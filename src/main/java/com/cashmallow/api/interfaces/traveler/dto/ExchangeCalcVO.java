package com.cashmallow.api.interfaces.traveler.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.ToString;

import java.math.BigDecimal;

// 기능: 환전 계산 결과를 구하기 위한 VO
@ToString
public class ExchangeCalcVO {
    private String from_cd; // from_cd

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal from_money; // 환전할 금액

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal fee;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal fee_rate; // 환전 수수료 율

    private String to_cd; // to_cd

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal to_money;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal exchange_rate; // 환율(to_cd 기준 from_cd의 환율)

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal base_exchange_rate; // 조정 전 환율(to_cd 기준 from_cd의 환율)

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal fee_per_amt; // 고정 수수료

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal fee_rate_amt; // 수수료 율에 의해서 사용자에게 청구될 수수료 금액

    @JsonSerialize(using = ToStringSerializer.class)
    private String status; // 인출 가능 상태

    @JsonSerialize(using = ToStringSerializer.class)
    private String message; // 모바일 전달 메세지

    @JsonSerialize(using = ToStringSerializer.class)
    private String title; // 모바일 팝업창 title

    @JsonSerialize(using = ToStringSerializer.class)
    private String buttonName; // 모바일 팝업창 buttonName

    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponUserId; // 쿠폰 발행 유져 아이디
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal discountAmount; // 할인 금액

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal paymentAmount; // 결재 금액

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal refundAmount;

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Long getCouponUserId() {
        return couponUserId;
    }

    public void setCouponUserId(Long couponUserId) {
        this.couponUserId = couponUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public BigDecimal getFee_rate() {
        return fee_rate;
    }

    public void setFee_rate(BigDecimal fee_rate) {
        this.fee_rate = fee_rate;
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

    public BigDecimal getExchange_rate() {
        return exchange_rate;
    }

    public void setExchange_rate(BigDecimal exchange_rate) {
        this.exchange_rate = exchange_rate;
    }

    public BigDecimal getBase_exchange_rate() {
        return base_exchange_rate;
    }

    public void setBase_exchange_rate(BigDecimal base_exchange_rate) {
        this.base_exchange_rate = base_exchange_rate;
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

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

}
