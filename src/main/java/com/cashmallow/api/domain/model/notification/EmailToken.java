package com.cashmallow.api.domain.model.notification;

import com.cashmallow.common.CommonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailToken {
    private Long userId;
    private String kindOfTs;
    private String accountToken;
    private Timestamp createdDate;


    @Getter
    private String code;


    public EmailToken(Long userId, String kindOfTs, String accountToken) {
        this.userId = userId;
        this.kindOfTs = kindOfTs;
        this.accountToken = accountToken;
        this.code = CommonUtil.getRandomDigits(6);
    }

    public Long getUserId() {
        return userId;
    }

    public String getKindOfTs() {
        return kindOfTs;
    }

    public String getAccountToken() {
        return accountToken;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}
