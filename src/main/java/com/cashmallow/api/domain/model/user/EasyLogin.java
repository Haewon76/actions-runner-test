package com.cashmallow.api.domain.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class EasyLogin {

    private Long id;
    private String refreshToken;
    private Long userId;
    private Timestamp refreshTime;
    private String pinCodeHash;
    private Integer failCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;


}
