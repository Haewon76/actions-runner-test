package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.Convert;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.JsonStr;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

// 기능: 관리자용 인출 조회 질의용 class
public class AdminCashOutAskVO {
    // 조회 컬럼들

    // 여행자 정보
    private String u_country; // 일치검색
    private Long user_id; // 일치검색
    private String first_name; // LIKE 검색
    private String last_name; // LIKE 검색
    private String email; // LIKE 검색

    private Long traveler_id; // 일치검색
    private String contact_type;    // LIKE 검색
    private String contact_id;      // LIKE 검색

    // 가맹점 정보
    private String s_country; // 일치검색
    private Long s_user_id; // 일치검색
    private Long withdrawal_partner_id; // 일치검색
    private String shop_name; // LIKE 검색
    private String business_no; // LIKE 검색
    private String shop_addr; // LIKE 검색
    private String s_first_name; // LIKE 검색
    private String s_last_name; // LIKE 검색
    private BigDecimal begin_fx_possible_amt; // Range 검색(begin_fx_possible_amt ~ end_fx_possible_amt)
    private BigDecimal end_fx_possible_amt;

    // 인출 정보
    private Long cashout_id; // 일치검색
    private String cashout_reserved_date;   // LIKE 검색
    private String flight_arrival_date;     // LIKE 검색
    private String flight_no;               // LIKE 검색 
    private String country; // 일치검색
    private BigDecimal begin_traveler_total_cost; // Range 검색(begin_traveler_total_cost ~ end_traveler_total_cost)
    private BigDecimal end_traveler_total_cost;
    private BigDecimal begin_withdrawal_partner_cash_out_fee; // Range 검색(begin_storekeeper_cash_out_fee ~ end_storekeeper_cash_out_fee) 
    private BigDecimal end_withdrawal_partner_cash_out_fee;
    private BigDecimal begin_withdrawal_partner_total_cost; // Range 검색(begin_storekeeper_total_cost ~ end_storekeeper_total_cost) 
    private BigDecimal end_withdrawal_partner_total_cost;
    private String co_status; // 일치검색
    private Timestamp begin_created_date; // 등록일 또는 갱신일 기준 Range 검색(begin_created_date ~ end_created_date)
    private Timestamp end_created_date;

    private Integer start_row;
    private Integer page;
    private Integer size;
    private String sort;
    private String searchValue;

    // 정렬 옵션(column_name {ASC | DESC})
    // - u_country                : 여행자의 사용자 국가
    // - user_id                  : 여행자의 사용자 ID.
    // - first_name               : 여행자의 이름
    // - last_name                : 여행자의 성
    // - email DESC               : 여행자의 e-mail     
    // - traveler_id              : 여행자 ID.
    // - s_country                : 가맹점 국가
    // - s_user_id                : 가맹점의 사용자 ID.
    // - storekeeper_id           : 가맹정 ID.
    // - shop_name                : 가맹점 이름
    // - s_first_name             : 가맹점주의 이름
    // - s_last_name              : 가맹점주의 성
    // - s_fx_possible_amt        : 가맹점의 현재 인출 가능액
    // - cash_out_id              : 인출ID.
    // - country                  : 인출 국가
    // - traveler_total_cost      : 여행자 인출 총액
    // - storekeeper_total_cost   : 가맹점 인출 총액
    // - storekeeper_cash_out_fee : 가맹점 수수료
    // - co_status                : 인출상태
    // - created_date             : 생성일
    // - updated_date             : 수정일

    public String getU_country() {
        return u_country;
    }

    public void setU_country(String u_country) {
        this.u_country = u_country;
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

    public String getS_country() {
        return s_country;
    }

    public void setS_country(String s_country) {
        this.s_country = s_country;
    }

    public Long getS_user_id() {
        return s_user_id;
    }

    public void setS_user_id(Long s_user_id) {
        this.s_user_id = s_user_id;
    }

    public Long getWithdrawal_partner_id() {
        return withdrawal_partner_id;
    }

    public void setWithdrawal_partner_id(Long withdrawal_partner_id) {
        this.withdrawal_partner_id = withdrawal_partner_id;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getBusiness_no() {
        return business_no;
    }

    public void setBusiness_no(String business_no) {
        this.business_no = business_no;
    }

    public String getShop_addr() {
        return shop_addr;
    }

    public void setShop_addr(String shop_addr) {
        this.shop_addr = shop_addr;
    }

    public String getS_first_name() {
        return s_first_name;
    }

    public void setS_first_name(String s_first_name) {
        this.s_first_name = s_first_name;
    }

    public String getS_last_name() {
        return s_last_name;
    }

    public void setS_last_name(String s_last_name) {
        this.s_last_name = s_last_name;
    }

    public BigDecimal getBegin_fx_possible_amt() {
        return begin_fx_possible_amt;
    }

    public void setBegin_fx_possible_amt(BigDecimal begin_fx_possible_amt) {
        this.begin_fx_possible_amt = begin_fx_possible_amt;
    }

    public BigDecimal getEnd_fx_possible_amt() {
        return end_fx_possible_amt;
    }

    public void setEnd_fx_possible_amt(BigDecimal end_fx_possible_amt) {
        this.end_fx_possible_amt = end_fx_possible_amt;
    }

    public Long getCashout_id() {
        return cashout_id;
    }

    public void setCashout_id(Long cashout_id) {
        this.cashout_id = cashout_id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getBegin_traveler_total_cost() {
        return begin_traveler_total_cost;
    }

    public void setBegin_traveler_total_cost(BigDecimal begin_traveler_total_cost) {
        this.begin_traveler_total_cost = begin_traveler_total_cost;
    }

    public BigDecimal getEnd_traveler_total_cost() {
        return end_traveler_total_cost;
    }

    public void setEnd_traveler_total_cost(BigDecimal end_traveler_total_cost) {
        this.end_traveler_total_cost = end_traveler_total_cost;
    }

    public BigDecimal getBegin_withdrawal_partner_cash_out_fee() {
        return begin_withdrawal_partner_cash_out_fee;
    }

    public void setBegin_withdrawal_partner_cash_out_fee(BigDecimal begin_withdrawal_partner_cash_out_fee) {
        this.begin_withdrawal_partner_cash_out_fee = begin_withdrawal_partner_cash_out_fee;
    }

    public BigDecimal getEnd_withdrawal_partner_cash_out_fee() {
        return end_withdrawal_partner_cash_out_fee;
    }

    public void setEnd_withdrawal_partner_cash_out_fee(BigDecimal end_withdrawal_partner_cash_out_fee) {
        this.end_withdrawal_partner_cash_out_fee = end_withdrawal_partner_cash_out_fee;
    }

    public BigDecimal getBegin_withdrawal_partner_total_cost() {
        return begin_withdrawal_partner_total_cost;
    }

    public void setBegin_withdrawal_partner_total_cost(BigDecimal begin_withdrawal_partner_total_cost) {
        this.begin_withdrawal_partner_total_cost = begin_withdrawal_partner_total_cost;
    }

    public BigDecimal getEnd_withdrawal_partner_total_cost() {
        return end_withdrawal_partner_total_cost;
    }

    public void setEnd_withdrawal_partner_total_cost(BigDecimal end_withdrawal_partner_total_cost) {
        this.end_withdrawal_partner_total_cost = end_withdrawal_partner_total_cost;
    }

    public String getCo_status() {
        return co_status;
    }

    public void setCo_status(String co_status) {
        this.co_status = co_status;
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


    public AdminCashOutAskVO(String jsonStr) {
        Map<String, Object> map = JsonStr.toHashMap(jsonStr);

        u_country = (String) map.get("u_country");
        user_id = Convert.objToLongDef(map.get("user_id"), null);
        first_name = (String) map.get("first_name");
        last_name = (String) map.get("last_name");
        email = (String) map.get("email");
        traveler_id = Convert.objToLongDef(map.get("traveler_id"), null);
        contact_type = (String) map.get("contact_type");
        contact_id = (String) map.get("contact_id");
        cashout_reserved_date = (String) map.get("cashout_reserved_date");
        flight_arrival_date = (String) map.get("flight_arrival_date");
        flight_no = (String) map.get("flight_no");
        s_country = (String) map.get("s_country");
        s_user_id = Convert.objToLongDef(map.get("s_user_id"), null);
        withdrawal_partner_id = Convert.objToLongDef(map.get("withdrawal_partner_id"), null);
        shop_name = (String) map.get("shop_name");
        business_no = (String) map.get("business_no");
        shop_addr = (String) map.get("shop_addr");
        s_first_name = (String) map.get("s_first_name");
        s_last_name = (String) map.get("s_last_name");
        begin_fx_possible_amt = Convert.objToBigDecimalDef(map.get("begin_fx_possible_amt"), null);
        end_fx_possible_amt = Convert.objToBigDecimalDef(map.get("end_fx_possible_amt"), null);
        cashout_id = Convert.objToLongDef(map.get("cashout_id"), null);
        country = (String) map.get("country");
        begin_traveler_total_cost = Convert.objToBigDecimalDef(map.get("begin_traveler_total_cost"), null);
        end_traveler_total_cost = Convert.objToBigDecimalDef(map.get("end_traveler_total_cost"), null);
        begin_withdrawal_partner_cash_out_fee = Convert.objToBigDecimalDef(map.get("begin_withdrawal_partner_cash_out_fee"), null);
        end_withdrawal_partner_cash_out_fee = Convert.objToBigDecimalDef(map.get("end_withdrawal_partner_cash_out_fee"), null);
        begin_withdrawal_partner_total_cost = Convert.objToBigDecimalDef(map.get("begin_withdrawal_partner_total_cost "), null);
        end_withdrawal_partner_total_cost = Convert.objToBigDecimalDef(map.get("end_withdrawal_partner_total_cost"), null);
        co_status = (String) map.get("co_status");
        begin_created_date = CommDateTime.objToTimestamp(map.get("begin_created_date"));
        end_created_date = CommDateTime.objToTimestamp(map.get("end_created_date"));

        page = Convert.objToIntDef(map.get("page"), Const.DEF_PAGE_NO);
        start_row = Convert.objToIntDef(map.get("start_row"), 0);
        size = Convert.objToIntDef(map.get("size"), Const.DEF_PAGE_SIZE);
        sort = (String) map.get("sort");
        searchValue = (String) map.get("searchValue");
    }

    @Override
    public String toString() {
        return "u_country:" + u_country
                + ", user_id:" + user_id
                + ", first_name:" + first_name
                + ", last_name:" + last_name
                + ", email:" + email

                + ", traveler_id:" + traveler_id

                + ", s_country:" + s_country
                + ", s_user_id:" + s_user_id
                + ", withdrawal_partner_id:" + withdrawal_partner_id
                + ", shop_name:" + shop_name
                + ", business_no:" + business_no
                + ", shop_addr:" + shop_addr
                + ", s_first_name:" + s_first_name
                + ", s_last_name:" + s_last_name

                + ", cashout_id:" + cashout_id
                + ", country:" + country
                + ", begin_traveler_total_cost:" + begin_traveler_total_cost
                + ", end_traveler_total_cost:" + end_traveler_total_cost
                + ", begin_withdrawal_partner_cash_out_fee:" + begin_withdrawal_partner_cash_out_fee
                + ", end_withdrawal_partner_cash_out_fee:" + end_withdrawal_partner_cash_out_fee
                + ", begin_withdrawal_partner_total_cost:" + begin_withdrawal_partner_total_cost
                + ", end_withdrawal_partner_total_cost:" + end_withdrawal_partner_total_cost
                + ", co_status:" + co_status
                + ", begin_created_date:" + begin_created_date
                + ", end_created_date:" + end_created_date

                + ", start_row:" + start_row
                + ", page:" + page
                + ", size:" + size
                + ", sort:" + sort;
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    public static String toString(AdminCashOutAskVO vo) {
        return vo != null ? vo.toString() : "null";
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

    public String getCashout_reserved_date() {
        return cashout_reserved_date;
    }

    public void setCashout_reserved_date(String cashout_reserved_date) {
        this.cashout_reserved_date = cashout_reserved_date;
    }

    public String getFlight_arrival_date() {
        return flight_arrival_date;
    }

    public void setFlight_arrival_date(String flight_arrival_date) {
        this.flight_arrival_date = flight_arrival_date;
    }

    public String getFlight_no() {
        return flight_no;
    }

    public void setFlight_no(String flight_no) {
        this.flight_no = flight_no;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

}
