package com.cashmallow.api.interfaces.admin.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

// 기능: mapping 테이블 등록시 사용되는 class
public class MappingRegVO {
    private String country;
    private Integer bank_account_id;
    private BigDecimal pin_value;
    private String ref_value;
    private Integer pin_seq_no;
    private Timestamp begin_valid_date;
    private Timestamp end_valid_date;
    private Timestamp created_date;
    private Timestamp canceled_date;
    // private String status;
    private Long traveler_id;
    private Long exchange_id;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getBank_account_id() {
        return bank_account_id;
    }

    public void setBank_account_id(Integer bank_account_id) {
        this.bank_account_id = bank_account_id;
    }

    public BigDecimal getPin_value() {
        return pin_value;
    }

    public void setPin_value(BigDecimal pin_value) {
        this.pin_value = pin_value;
    }

    public String getRef_value() {
        return ref_value;
    }

    public void setRef_value(String ref_value) {
        this.ref_value = ref_value;
    }

    public Integer getPin_seq_no() {
        return pin_seq_no;
    }

    public void setPin_seq_no(Integer pin_seq_no) {
        this.pin_seq_no = pin_seq_no;
    }

    public Timestamp getBegin_valid_date() {
        return begin_valid_date;
    }

    public void setBegin_valid_date(Timestamp begin_valid_date) {
        this.begin_valid_date = begin_valid_date;
    }

    public Timestamp getEnd_valid_date() {
        return end_valid_date;
    }

    public void setEnd_valid_date(Timestamp end_valid_date) {
        this.end_valid_date = end_valid_date;
    }

    public Timestamp getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Timestamp created_date) {
        this.created_date = created_date;
    }

    public Timestamp getCanceled_date() {
        return canceled_date;
    }

    public void setCanceled_date(Timestamp canceled_date) {
        this.canceled_date = canceled_date;
    }

    public Long getTraveler_id() {
        return traveler_id;
    }

    public void setTraveler_id(Long traveler_id) {
        this.traveler_id = traveler_id;
    }

    public Long getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(Long exchange_id) {
        this.exchange_id = exchange_id;
    }


    @Override
    public String toString() {
        return "country:" + country
                + ", bank_account_id:" + bank_account_id
                + ", pin_value:" + pin_value
                + ", ref_value:" + ref_value
                + ", pin_seq_no:" + pin_seq_no
                + ", begin_valid_date:" + begin_valid_date
                + ", end_valid_date" + end_valid_date
                + ", created_date:" + created_date
                + ", canceled_date:" + canceled_date
                + ", traveler_id:" + traveler_id
                + ", exchange_id:" + exchange_id;
    }

    public String toString(String postfix) {
        return toString() + postfix;
    }

}
