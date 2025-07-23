package com.cashmallow.api.interfaces.traveler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

// 기능: 환전 요청 결과를 보관하기 위한 class.
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeReqResultVO {
    // exchange 정보
    private Long id;
    private Integer bank_account_id;
    // private long traveler_id;
    private String from_cd;
    private BigDecimal from_amt;
    private String to_cd;
    private BigDecimal to_amt;
    private BigDecimal fee;
    private BigDecimal exchange_rate;
    private String ex_status;
    private Timestamp created_date;
    private Timestamp updated_date;
    // private long creator;

    // bank_account 정보
    private String country;

    @Getter
    @Setter
    private String rejectMessage;

    private String bank_code;
    private String bank_name;
    @Getter
    @Setter
    private String branch_name;
    private String bank_account_no;
    private String account_type;
    @Getter
    @Setter
    private String jp_account_type;
    @Getter
    @Setter
    private String jp_post_account_no;
    @Getter
    @Setter
    private String jp_post_branch_name;


    private String first_name;
    private String last_name;

    // exchange 정보 추가
    private String fcm_yn;
    private String tr_bank_name;
    private String tr_account_name;
    private String tr_account_no;
    private String tr_from_amt;
    private String tr_receipt_photo;
    private Timestamp tr_deposit_date;


    public String getFcm_yn() {
        return fcm_yn;
    }

    public void setFcm_yn(String fcm_yn) {
        this.fcm_yn = fcm_yn;
    }

    public String getTr_bank_name() {
        return tr_bank_name;
    }

    public void setTr_bank_name(String tr_bank_name) {
        this.tr_bank_name = tr_bank_name;
    }

    public String getTr_account_name() {
        return tr_account_name;
    }

    public void setTr_account_name(String tr_account_name) {
        this.tr_account_name = tr_account_name;
    }

    public String getTr_account_no() {
        return tr_account_no;
    }

    public void setTr_account_no(String tr_account_no) {
        this.tr_account_no = tr_account_no;
    }

    public String getTr_from_amt() {
        return tr_from_amt;
    }

    public void setTr_from_amt(String tr_from_amt) {
        this.tr_from_amt = tr_from_amt;
    }

    public Timestamp getTr_deposit_date() {
        return tr_deposit_date;
    }

    public void setTr_deposit_date(Timestamp tr_deposit_date) {
        this.tr_deposit_date = tr_deposit_date;
    }


    // mapping 정보
    private Integer ref_value; // 2016.12.30 추가

    // 여행자 정보
    //    private TravelerVO traveler;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBank_account_id() {
        return bank_account_id;
    }

    public void setBank_account_id(Integer bank_account_id) {
        this.bank_account_id = bank_account_id;
    }

    public String getFrom_cd() {
        return from_cd;
    }

    public void setFrom_cd(String from_cd) {
        this.from_cd = from_cd;
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

    public BigDecimal getTo_amt() {
        return to_amt;
    }

    public void setTo_amt(BigDecimal to_amt) {
        this.to_amt = to_amt;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getExchange_rate() {
        return exchange_rate;
    }

    public void setExchange_rate(BigDecimal exchange_rate) {
        this.exchange_rate = exchange_rate;
    }

    public String getEx_status() {
        return ex_status;
    }

    public void setEx_status(String ex_status) {
        this.ex_status = ex_status;
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

    //    public TravelerVO getTraveler() {
    //        return traveler;
    //    }
    //
    //    public void setTraveler(TravelerVO traveler) {
    //        this.traveler = traveler;
    //    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBank_code() {
        return bank_code;
    }

    public void setBank_code(String bank_code) {
        this.bank_code = bank_code;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getBank_account_no() {
        return bank_account_no;
    }

    public void setBank_account_no(String bank_account_no) {
        this.bank_account_no = bank_account_no;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Integer getRef_value() {
        return ref_value;
    }

    public void setRef_value(Integer ref_value) {
        this.ref_value = ref_value;
    }

    public String getTr_receipt_photo() {
        return tr_receipt_photo;
    }

    public void setTr_receipt_photo(String tr_receipt_photo) {
        this.tr_receipt_photo = tr_receipt_photo;
    }

}
