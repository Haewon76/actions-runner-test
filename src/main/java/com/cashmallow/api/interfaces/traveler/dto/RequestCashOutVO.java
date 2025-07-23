package com.cashmallow.api.interfaces.traveler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestCashOutVO {
    private BigDecimal traveler_cash_out_amt;
    private BigDecimal traveler_cash_out_fee;
    private BigDecimal traveler_total_cost;
    private String country;
    private BigDecimal withdrawal_partner_cash_out_amt;
    private BigDecimal withdrawal_partner_cash_out_fee;
    private BigDecimal withdrawal_partner_total_cost;
    private Long withdrawal_partner_id;
    private Long wallet_id;

    // edited by Alex 20170619 : 상점 운영시간 확인시 필요
    private Integer request_time;

    // add reserved date, flight_no, flight_arrival_date columns to cash_out table
    private String cashout_reserved_date;
    private String flight_no;
    private String flight_arrival_date;

    // add contact_type, contact_id columns to user table
    private Boolean privacy_sharing_agreement;
    private String contact_type;
    private String contact_id;

    // add socash parameter
    private String cashpoint_id;
    private String position_lat;
    private String position_lng;
    private BigDecimal amount;

    public void setStorekeeper_cash_out_amt(BigDecimal storekeeper_cash_out_amt) {
        this.withdrawal_partner_cash_out_amt = storekeeper_cash_out_amt;
    }

    public void setStorekeeper_cash_out_fee(BigDecimal storekeeper_cash_out_fee) {
        this.withdrawal_partner_cash_out_fee = storekeeper_cash_out_fee;
    }

    public void setStorekeeper_id(Long storekeeper_id) {
        this.withdrawal_partner_id = storekeeper_id;
    }

    public void setStorekeeper_total_cost(BigDecimal storekeeper_total_cost) {
        this.withdrawal_partner_total_cost = storekeeper_total_cost;
    }

    public String getCashpoint_id() {
        return cashpoint_id;
    }

    public void setCashpoint_id(String cashpoint_id) {
        this.cashpoint_id = cashpoint_id;
    }

    public String getPosition_lat() {
        return position_lat;
    }

    public void setPosition_lat(String position_lat) {
        this.position_lat = position_lat;
    }

    public String getPosition_lng() {
        return position_lng;
    }

    public void setPosition_lng(String position_lng) {
        this.position_lng = position_lng;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getRequest_time() {
        return request_time;
    }

    public void setRequest_time(Integer request_time) {
        this.request_time = request_time;
    }

    public BigDecimal getTraveler_cash_out_amt() {
        return traveler_cash_out_amt;
    }

    public void setTraveler_cash_out_amt(BigDecimal traveler_cash_out_amt) {
        this.traveler_cash_out_amt = traveler_cash_out_amt;
    }

    public BigDecimal getTraveler_cash_out_fee() {
        return traveler_cash_out_fee;
    }

    public void setTraveler_cash_out_fee(BigDecimal traveler_cash_out_fee) {
        this.traveler_cash_out_fee = traveler_cash_out_fee;
    }

    public BigDecimal getTraveler_total_cost() {
        return traveler_total_cost;
    }

    public void setTraveler_total_cost(BigDecimal traveler_total_cost) {
        this.traveler_total_cost = traveler_total_cost;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getWithdrawal_partner_cash_out_amt() {
        return withdrawal_partner_cash_out_amt;
    }

    public void setWithdrawal_partner_cash_out_amt(BigDecimal withdrawal_partner_cash_out_amt) {
        this.withdrawal_partner_cash_out_amt = withdrawal_partner_cash_out_amt;
    }

    public BigDecimal getWithdrawal_partner_cash_out_fee() {
        return withdrawal_partner_cash_out_fee;
    }

    public void setWithdrawal_partner_cash_out_fee(BigDecimal withdrawal_partner_cash_out_fee) {
        this.withdrawal_partner_cash_out_fee = withdrawal_partner_cash_out_fee;
    }

    public BigDecimal getWithdrawal_partner_total_cost() {
        return withdrawal_partner_total_cost;
    }

    public void setWithdrawal_partner_total_cost(BigDecimal withdrawal_partner_total_cost) {
        this.withdrawal_partner_total_cost = withdrawal_partner_total_cost;
    }

    public Long getWithdrawal_partner_id() {
        return withdrawal_partner_id;
    }

    public void setWithdrawal_partner_id(Long withdrawal_partner_id) {
        this.withdrawal_partner_id = withdrawal_partner_id;
    }

    public Long getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(Long wallet_id) {
        this.wallet_id = wallet_id;
    }

    public Boolean getPrivacy_sharing_agreement() {
        return privacy_sharing_agreement;
    }

    @Override
    public String toString() {
        return "RequestCashOutVO [traveler_cash_out_amt=" + traveler_cash_out_amt + ", traveler_cash_out_fee="
                + traveler_cash_out_fee + ", traveler_total_cost=" + traveler_total_cost + ", country=" + country
                + ", wallet_id=" + wallet_id
                + ", withdrawal_partner_cash_out_amt=" + withdrawal_partner_cash_out_amt + ", withdrawal_partner_cash_out_fee="
                + withdrawal_partner_cash_out_fee + ", withdrawal_partner_total_cost=" + withdrawal_partner_total_cost + ", withdrawal_partner_id="
                + withdrawal_partner_id + ", request_time=" + request_time + "]";
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    public String getCashout_reserved_date() {
        return cashout_reserved_date;
    }

    public void setCashout_reserved_date(String cashout_reserved_date) {
        this.cashout_reserved_date = cashout_reserved_date;
    }

    public String getFlight_no() {
        return flight_no;
    }

    public void setFlight_no(String flight_no) {
        this.flight_no = flight_no;
    }

    public String getFlight_arrival_date() {
        return flight_arrival_date;
    }

    public void setFlight_arrival_date(String flight_arrival_date) {
        this.flight_arrival_date = flight_arrival_date;
    }

    public String getContact_type() {
        return contact_type;
    }

    public void setContact_type(String contact_type) {
        this.contact_type = contact_type;
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

    public Boolean isPrivacy_sharing_agreement() {
        return privacy_sharing_agreement;
    }

    public void setPrivacy_sharing_agreement(Boolean privacy_sharing_agreement) {
        this.privacy_sharing_agreement = privacy_sharing_agreement;
    }

}
