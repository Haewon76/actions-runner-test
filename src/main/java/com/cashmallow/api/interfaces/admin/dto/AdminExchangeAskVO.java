package com.cashmallow.api.interfaces.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.sql.Timestamp;

// 기능: 관리자용 환전 조회 질의용 class
public class AdminExchangeAskVO {
    // 조회 컬럼들

    // 여행자 정보
    private String country;                 // 일치검색
    private Long user_id;                   // 일치검색
    private String first_name;              // LIKE 검색
    private String last_name;               // LIKE 검색
    private String email;                   // LIKE 검색

    private Long traveler_id;               // 일치검색

    private Long exchange_id;               // 일치검색
    private String from_cd;                 // 일치검색
    private BigDecimal begin_from_amt;          // range 검색?(begin_from_amt ~ end_from_amt)
    private BigDecimal end_from_amt;
    private String to_cd;                   // 일치검색
    private BigDecimal begin_to_amt;            // range 검색?(begin_to_amt ~ end_to_amt)
    private BigDecimal end_to_amt;
    private BigDecimal begin_fee;               // range 검색?(begin_fee ~ end_fee)
    private BigDecimal end_fee;
    private String ex_status;               // OP, TC, CC, CF 중 하나

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp begin_created_date;   // 생성일 또는 업데이트일에 대하여 range 검색(begin_created_date ~ end_created_date)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp end_created_date;

    private Integer bank_account_id;        // 일치검색    
    private String bank_code;               // 일치검색    
    private String bank_name;               // LIKE 검색
    private String bank_account_no;         // LIKE 검색
    private String tr_account_no;

    private Integer start_row;
    private Integer page;
    private Integer size;
    private String sort;
    private String searchValue;
    private String isExistTxn;

    private BigDecimal fee_per_amt;
    private BigDecimal fee_rate_amt;

    private Long coupon_user_id;
    private BigDecimal coupon_discount_amt;

    public Long getCoupon_user_id() {
        return coupon_user_id;
    }

    public void setCoupon_user_id(Long coupon_user_id) {
        this.coupon_user_id = coupon_user_id;
    }

    public BigDecimal getCoupon_discount_amt() {
        return coupon_discount_amt;
    }

    public void setCoupon_discount_amt(BigDecimal coupon_discount_amt) {
        this.coupon_discount_amt = coupon_discount_amt;
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
    // 정렬 옵션(column_name {ASC | DESC})
    // - country         : 사용자 국가
    // - user_id         : 사용자 ID.
    // - first_name      : 이름
    // - last_name       : 성
    // - email           : 이메일
    // - traveler_id     : 여행자 ID.
    // - exchange_id     : 환전 ID.
    // - bank_account_id : 은행 ID.
    // - from_cd 
    // - to_cd 
    // - fee             : 수수료 
    // - ex_status       : 환전 상태
    // - created_date    : 생성일
    // - updated_date    : 수정일

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getFrom_cd() {
        return from_cd;
    }

    public void setFrom_cd(String from_cd) {
        this.from_cd = from_cd;
    }

    public BigDecimal getBegin_from_amt() {
        return begin_from_amt;
    }

    public void setBegin_from_amt(BigDecimal begin_from_amt) {
        this.begin_from_amt = begin_from_amt;
    }

    public BigDecimal getEnd_from_amt() {
        return end_from_amt;
    }

    public void setEnd_from_amt(BigDecimal end_from_amt) {
        this.end_from_amt = end_from_amt;
    }

    public String getTo_cd() {
        return to_cd;
    }

    public void setTo_cd(String to_cd) {
        this.to_cd = to_cd;
    }

    public BigDecimal getBegin_to_amt() {
        return begin_to_amt;
    }

    public void setBegin_to_amt(BigDecimal begin_to_amt) {
        this.begin_to_amt = begin_to_amt;
    }

    public BigDecimal getEnd_to_amt() {
        return end_to_amt;
    }

    public void setEnd_to_amt(BigDecimal end_to_amt) {
        this.end_to_amt = end_to_amt;
    }

    public BigDecimal getBegin_fee() {
        return begin_fee;
    }

    public void setBegin_fee(BigDecimal begin_fee) {
        this.begin_fee = begin_fee;
    }

    public BigDecimal getEnd_fee() {
        return end_fee;
    }

    public void setEnd_fee(BigDecimal end_fee) {
        this.end_fee = end_fee;
    }

    public String getEx_status() {
        return ex_status;
    }

    public void setEx_status(String ex_status) {
        this.ex_status = ex_status;
    }

    public Timestamp getBegin_created_date() {
        return begin_created_date;
    }

    public void setBegin_created_date(Timestamp begin_created_date) {
        this.begin_created_date = begin_created_date;
    }

    public Timestamp getEnd_created_date() {
        return end_created_date;
    }

    public void setEnd_created_date(Timestamp end_created_date) {
        this.end_created_date = end_created_date;
    }

    public Integer getBank_account_id() {
        return bank_account_id;
    }

    public void setBank_account_id(Integer bank_account_id) {
        this.bank_account_id = bank_account_id;
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

    public String getTr_account_no() {
        return tr_account_no;
    }

    public void setTr_account_no(String tr_account_no) {
        this.tr_account_no = tr_account_no;
    }


    public Integer getStart_row() {
        return start_row;
    }

    public void setStart_row(Integer start_row) {
        this.start_row = start_row;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }


    @Override
    public String toString() {
        return "country:" + country
                + ", user_id:" + user_id
                + ", first_name:" + first_name
                + ", last_name:" + last_name
                + ", email:" + email

                + ", traveler_id:" + traveler_id

                + ", exchange_id:" + exchange_id
                + ", from_cd:" + from_cd
                + ", begin_from_amt:" + begin_from_amt
                + ", end_from_amt:" + end_from_amt
                + ", to_cd:" + to_cd
                + ", begin_to_amt:" + begin_to_amt
                + ", end_to_amt:" + end_to_amt
                + ", begin_fee:" + begin_fee
                + ", end_fee:" + end_fee
                + ", ex_status:" + ex_status
                + ", begin_created_date:" + begin_created_date
                + ", end_created_date:" + end_created_date

                + ", bank_account_id:" + bank_account_id
                + ", bank_code:" + bank_code
                + ", bankName:" + bank_name
                + ", bank_account_no:" + bank_account_no

                + ", start_row:" + start_row
                + ", page:" + page
                + ", size:" + size
                + ", sort:" + sort
                + ", searchValue:" + searchValue
                + ", paygate_rec_id:" + paygate_rec_id
                + ", paygate_rec_amount:" + paygate_rec_amount
                ;
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    public String getIsExistTxn() {
        return isExistTxn;
    }

    public void setIsExistTxn(String isExistTxn) {
        this.isExistTxn = isExistTxn;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    // 매핑 정보
    private String paygate_rec_id; // TID
    private String paygate_rec_amount; // TID amount

    public String getPaygate_rec_id() {
        return paygate_rec_id;
    }

    public void setPaygate_rec_id(String paygate_rec_id) {
        this.paygate_rec_id = paygate_rec_id;
    }

    public String getPaygate_rec_amount() {
        return paygate_rec_amount;
    }

    public void setPaygate_rec_amount(String paygate_rec_amount) {
        this.paygate_rec_amount = paygate_rec_amount;
    }


    private String cash_out_id; // 인출 번호
    private String cash_out_amt; // 인출금액
    private String from_amt_calc; // 원금

    public String getCash_out_id() {
        return cash_out_id;
    }

    public void setCash_out_id(String cash_out_id) {
        this.cash_out_id = cash_out_id;
    }

    public String getCash_out_amt() {
        return cash_out_amt;
    }

    public void setCash_out_amt(String cash_out_amt) {
        this.cash_out_amt = cash_out_amt;
    }

    public String getFrom_amt_calc() {
        return from_amt_calc;
    }

    public void setFrom_amt_calc(String from_amt_calc) {
        this.from_amt_calc = from_amt_calc;
    }
}
