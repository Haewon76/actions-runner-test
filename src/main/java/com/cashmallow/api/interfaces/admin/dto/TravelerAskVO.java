package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.Convert;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.JsonStr;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Map;

public class TravelerAskVO {
    private String country;
    private Long user_id;
    private String first_name;
    private String last_name;
    @Getter
    @Setter
    private String phone_number;
    private String email;

    private Long traveler_id;

    private String passport_country;
    private String identification_number;
    private String en_first_name;
    private String en_last_name;
    private String begin_exp_date;
    private String end_exp_date;
    private String certification_ok;
    private Timestamp begin_certification_ok_date;
    private Timestamp end_certification_ok_date;
    private String certification_type;
    private String address;
    private String address_photo;

    private String account_no;
    private String bank_name;
    private String account_ok;
    private Timestamp begin_account_ok_date;
    private Timestamp end_account_ok_date;

    private String paygate_member_id;

    private Integer start_row;
    private Integer page;
    private Integer size;
    private String sort;

    @Getter
    @Setter
    private String gender;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPaygate_member_id() {
        return paygate_member_id;
    }

    public void setPaygate_member_id(String paygate_member_id) {
        this.paygate_member_id = paygate_member_id;
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

    public String getIdentification_number() {
        return identification_number;
    }

    public void setIdentification_number(String identification_number) {
        this.identification_number = identification_number;
    }

    public String getBegin_exp_date() {
        return begin_exp_date;
    }

    public void setBegin_exp_date(String begin_exp_date) {
        this.begin_exp_date = begin_exp_date;
    }

    public String getEnd_exp_date() {
        return end_exp_date;
    }

    public void setEnd_exp_date(String end_exp_date) {
        this.end_exp_date = end_exp_date;
    }

    public String getCertification_type() {
        return certification_type;
    }

    public void setCertification_type(String certification_type) {
        this.certification_type = certification_type;
    }

    public String getCertification_ok() {
        return certification_ok;
    }

    public void setCertification_ok(String certification_ok) {
        this.certification_ok = certification_ok;
    }

    public Timestamp getBegin_certification_ok_date() {
        return begin_certification_ok_date;
    }

    public void setBegin_certification_ok_date(Timestamp begin_certification_ok_date) {
        this.begin_certification_ok_date = begin_certification_ok_date;
    }

    public Timestamp getEnd_certification_ok_date() {
        return end_certification_ok_date;
    }

    public void setEnd_passport_ok_date(Timestamp end_passport_ok_date) {
        this.end_certification_ok_date = end_passport_ok_date;
    }

    public String getAccount_no() {
        return account_no;
    }

    public void setAccount_no(String account_no) {
        this.account_no = account_no;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getAccount_ok() {
        return account_ok;
    }

    public void setAccount_ok(String account_ok) {
        this.account_ok = account_ok;
    }

    public Timestamp getBegin_account_ok_date() {
        return begin_account_ok_date;
    }

    public void setBegin_account_ok_date(Timestamp begin_account_ok_date) {
        this.begin_account_ok_date = begin_account_ok_date;
    }

    public Timestamp getEnd_account_ok_date() {
        return end_account_ok_date;
    }

    public void setEnd_account_ok_date(Timestamp end_account_ok_date) {
        this.end_account_ok_date = end_account_ok_date;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress_photo() {
        return address_photo;
    }

    public void setAddress_photo(String address_photo) {
        this.address_photo = address_photo;
    }

    public TravelerAskVO() {
        this.page = Const.DEF_PAGE_NO;
        this.size = Const.DEF_PAGE_SIZE;
    }

    public TravelerAskVO(String jsonStr) {
        Map<String, Object> map = JsonStr.toHashMap(jsonStr);

        country = (String) map.get("country");
        user_id = Convert.objToLongDef(map.get("user_id"), null);
        first_name = (String) map.get("first_name");
        last_name = (String) map.get("last_name");
        email = (String) map.get("email");
        traveler_id = Convert.objToLongDef(map.get("traveler_id"), null);

        passport_country = (String) map.get("passport_country");
        identification_number = (String) map.get("identification_number");
        en_first_name = (String) map.get("en_first_name");
        en_last_name = (String) map.get("en_last_name");
        begin_exp_date = (String) map.get("begin_exp_date");
        end_exp_date = (String) map.get("end_exp_date");
        certification_ok = (String) map.get("certification_ok");
        begin_certification_ok_date = CommDateTime.objToTimestamp(map.get("begin_certification_ok_date"));
        end_certification_ok_date = CommDateTime.objToTimestamp(map.get("end_certification_ok_date"));
        account_no = (String) map.get("account_no");
        bank_name = (String) map.get("bank_name");
        account_ok = (String) map.get("account_ok");
        begin_account_ok_date = CommDateTime.objToTimestamp(map.get("begin_account_ok_date"));
        end_account_ok_date = CommDateTime.objToTimestamp(map.get("end_account_ok_date"));
        certification_type = (String) map.get("certification_type");
        address = (String) map.get("address");

        page = Convert.objToIntDef(map.get("page"), Const.DEF_PAGE_NO);
        size = Convert.objToIntDef(map.get("size"), Const.DEF_PAGE_SIZE);
        sort = (String) map.get("sort");
        try {
            phone_number = (String) map.get("phone_number");
            phone_number = phone_number.replaceAll("&#43;", "+");
        } catch (Exception ignored)  {}
    }

    public TravelerAskVO(Map<String, String> map) {

        country = (String) map.get("country");
        user_id = Convert.objToLongDef(map.get("user_id"), null);
        first_name = (String) map.get("first_name");
        last_name = (String) map.get("last_name");
        email = (String) map.get("email");
        traveler_id = Convert.objToLongDef(map.get("traveler_id"), null);

        passport_country = (String) map.get("passport_country");
        identification_number = (String) map.get("identification_number");
        en_first_name = (String) map.get("en_first_name");
        en_last_name = (String) map.get("en_last_name");
        begin_exp_date = (String) map.get("begin_exp_date");
        end_exp_date = (String) map.get("end_exp_date");
        certification_ok = (String) map.get("certification_ok");
        begin_certification_ok_date = CommDateTime.objToTimestamp(map.get("begin_certification_ok_date"));
        end_certification_ok_date = CommDateTime.objToTimestamp(map.get("end_certification_ok_date"));
        account_no = (String) map.get("account_no");
        bank_name = (String) map.get("bank_name");
        account_ok = (String) map.get("account_ok");
        begin_account_ok_date = CommDateTime.objToTimestamp(map.get("begin_account_ok_date"));
        end_account_ok_date = CommDateTime.objToTimestamp(map.get("end_account_ok_date"));
        certification_type = (String) map.get("certification_type");

        page = Convert.objToIntDef(map.get("page"), Const.DEF_PAGE_NO);
        size = Convert.objToIntDef(map.get("size"), Const.DEF_PAGE_SIZE);
        sort = (String) map.get("sort");
        phone_number = (String) map.get("phone_number");
    }

    public String getPassport_country() {
        return passport_country;
    }

    public void setPassport_country(String passport_country) {
        this.passport_country = passport_country;
    }

    public String getEn_first_name() {
        return en_first_name;
    }

    public void setEn_first_name(String en_first_name) {
        this.en_first_name = en_first_name;
    }

    public String getEn_last_name() {
        return en_last_name;
    }

    public void setEn_last_name(String en_last_name) {
        this.en_last_name = en_last_name;
    }
}
