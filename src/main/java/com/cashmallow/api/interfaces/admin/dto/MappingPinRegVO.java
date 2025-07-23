package com.cashmallow.api.interfaces.admin.dto;

import java.math.BigDecimal;

// 기능: pin값 생성 요청시 사용되는 class
public class MappingPinRegVO {
    private Long company_id;
    private String use_yn;

    private String country;
    private Integer bank_account_id;
    private BigDecimal pin_value;

    private BigDecimal min_pin_value;
    private BigDecimal max_pin_value;

    private Long traveler_id;
    // private Long exchange_id;

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }


    public String getUse_yn() {
        return use_yn;
    }

    public void setUse_yn(String use_yn) {
        this.use_yn = use_yn;
    }

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

    public BigDecimal getMin_pin_value() {
        return min_pin_value;
    }

    public void setMin_pin_value(BigDecimal min_pin_value) {
        this.min_pin_value = min_pin_value;
    }

    public BigDecimal getMax_pin_value() {
        return max_pin_value;
    }

    public void setMax_pin_value(BigDecimal max_pin_value) {
        this.max_pin_value = max_pin_value;
    }

    public Long getTraveler_id() {
        return traveler_id;
    }

    public void setTraveler_id(Long traveler_id) {
        this.traveler_id = traveler_id;
    }

    //    public Long getExchange_id() {
    //        return exchange_id;
    //    }
    //
    //    public void setExchange_id(Long exchange_id) {
    //        this.exchange_id = exchange_id;
    //    }


    @Override
    public String toString() {
        return "company_id:" + company_id
                //         + ", use_yn:" + use_yn
                + ", country:" + country
                //         + ", bank_account_id:" + bank_account_id
                + ", pin_value:" + pin_value
                //         + ", min_pin_value:" + min_pin_value
                //         + ", max_pin_value:" + max_pin_value
                + ", traveler_id:" + traveler_id
                //         + ", exchange_id:" + exchange_id
                ;
    }

    public String toString(String postfix) {
        return toString() + postfix;
    }

    public boolean checkValidation() {
        return true;
    }

}
