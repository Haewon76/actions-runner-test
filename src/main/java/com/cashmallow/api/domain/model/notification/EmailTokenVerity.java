package com.cashmallow.api.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class EmailTokenVerity {
    private String token;
    private Long userId;
    private String password;
    private int loginFailCount;
    private EmailVerityType type;

    public EmailTokenVerity(Long userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public EmailTokenVerity(String token, Long userId, String password, EmailVerityType type) {
        this.token = token;
        this.userId = userId;
        this.password = password;
        this.type = type;
    }

    public EmailTokenVerity(String token, Long userId, int loginFailCount, EmailVerityType type) {
        this.token = token;
        this.userId = userId;
        this.loginFailCount = loginFailCount;
        this.type = type;
    }
}
