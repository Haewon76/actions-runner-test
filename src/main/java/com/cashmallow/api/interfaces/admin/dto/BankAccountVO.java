package com.cashmallow.api.interfaces.admin.dto;

public class BankAccountVO {
    private Integer id;                    // 계좌 ID.(순차 번호)
    private Integer company_id;         // 지사 ID.
    private String country;             // 국가코드
    private String bank_code;           // 은행코드
    private String bank_name;           // 은행명
    private String bank_account_no;     // 은행계좌번호
    private String account_type;        // 계좌 타입
    private Integer sort_order;         // 정렬순서
    private String use_yn;              // Y:사용, N:비사용 
    private String first_name;          // 예금주 명의 이름
    private String last_name;           // 예금주 명의 성
    private Integer ref_value;              // 참조값

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

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


    public Integer getSort_order() {
        return sort_order;
    }

    public void setSort_order(Integer sort_order) {
        this.sort_order = sort_order;
    }

    public String getUse_yn() {
        return use_yn;
    }

    public void setUse_yn(String use_yn) {
        this.use_yn = use_yn;
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


    @Override
    public String toString() {
        return "id:" + id
                + ", company_id:" + company_id
                + ", country:" + country
                + ", bank_code:" + bank_code
                + ", bankName:" + bank_name
                + ", bank_account_no:" + bank_account_no
                + ", accountType:" + account_type
                + ", sort_order: " + sort_order
                + ", use_yn:" + use_yn
                + ", first_name:" + first_name
                + ", last_name:" + last_name
                + ", ref_value:" + ref_value;
    }

    public String toString(String postfix) {
        return toString() + postfix;
    }

    public boolean checkValidation() {
        return country != null && !country.isEmpty();
    }

}
