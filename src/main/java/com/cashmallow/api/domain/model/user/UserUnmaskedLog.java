package com.cashmallow.api.domain.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * admin save user unmasked log
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserUnmaskedLog {
    /**
     * 유저 ID
     */
    private Long userId;
    /**
     * 여행자 ID
     */
    private Long travelerId;
    /**
     * 화면명 - 요청화면
     */
    private String viewName;
    /**
     * 요청자/관리자 ID
     */
    private Long creator;
    /**
     * 생성일
     */
    private Date createdAt;
}
