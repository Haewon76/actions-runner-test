package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.shared.Const;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class EasyLoginHist {

    private Long id;
    private String refreshToken;
    private Long userId = Const.NO_USER_ID;
    private Timestamp refreshTime;
    private String pinCodeHash;
    private Integer failCount;
    private String loginSuccess;
    private Timestamp createdAt;


}
