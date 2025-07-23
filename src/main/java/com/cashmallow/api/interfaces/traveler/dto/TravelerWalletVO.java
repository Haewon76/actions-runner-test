package com.cashmallow.api.interfaces.traveler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelerWalletVO {

    // TODO v2.6.5에서 id 추가됨. wallet_id는 추후에 삭제할 것 (안드로이드 기준으로 v1.8.8 완전히 배포된 이후).
    private Long id;
    private Long wallet_id;

    private Long traveler_id;
    private String root_cd;
    private String country;
    private String exchange_ids;
    private String can_refund;
    private Long exchange_id;
    @Getter
    @Setter
    private String expired;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal e_money;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal r_money;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal c_money;

    private Timestamp created_date;
    private Timestamp updated_date;
    private Timestamp expired_date; // 생성일로부터 만료일을 계산한 날짜
    @Setter
    @Getter
    private Long expired_day; // jobplan에 등록된 만료일
    private Long creator;

    public Long getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(Long wallet_id) {
        this.wallet_id = wallet_id;
    }

    public Long getTraveler_id() {
        return traveler_id;
    }

    public void setTraveler_id(Long traveler_id) {
        this.traveler_id = traveler_id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getExchangeIds() {
        return exchange_ids;
    }

    public void setexchangeIds(String exchange_ids) {
        this.exchange_ids = exchange_ids;
    }

    public BigDecimal getE_money() {
        return e_money;
    }

    public void setE_money(BigDecimal e_money) {
        this.e_money = e_money;
    }

    public BigDecimal getR_money() {
        return r_money;
    }

    public void setR_money(BigDecimal r_money) {
        this.r_money = r_money;
    }

    public BigDecimal getC_money() {
        return c_money;
    }

    public void setC_money(BigDecimal c_money) {
        this.c_money = c_money;
    }

    public Timestamp getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Timestamp created_date) {
        this.created_date = created_date;
    }

    public Timestamp getUpdated_date() {
        return updated_date;
    }

    public void setUpdated_date(Timestamp updated_date) {
        this.updated_date = updated_date;
    }

    public Timestamp getExpired_date() {
        return expired_date;
    }

    public void setExpired_date(Timestamp expired_date) {
        this.expired_date = expired_date;
    }

    public String getRoot_cd() {
        return root_cd;
    }

    public void setRoot_cd(String root_cd) {
        this.root_cd = root_cd;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCan_refund() {
        return can_refund;
    }

    public void setCan_refund(String can_refund) {
        this.can_refund = can_refund;
    }

    public Long getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(Long exchange_id) {
        this.exchange_id = exchange_id;
    }
}
