package com.cashmallow.api.interfaces.traveler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 기능: 국가별 관리 정보 VO
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CountryExtVO {
    private String code;
    private String iso_3166;
    private String iso_4217;
    private String calling_code;
    private String service;
    private String can_signup;
    private Integer timezone_interval;
    private String is_family_name_after_first_name;
    private String type_of_ref_value;
    private BigDecimal mapping_upper_range;
    private BigDecimal mapping_lower_range;
    private BigDecimal mapping_inc;

    private BigDecimal unit_scale;

    private BigDecimal default_lat;
    private BigDecimal default_lng;

    private String cash_out_type; // F:Full withdrawal, P:Partial withdrawal
    private String dateCalculationStandard;
    private Integer last_ref_value;

    /**
     * 각 나라의 국기 이미지 URL을 저장하는 변수
     */
    private String flag_image_url;
}
