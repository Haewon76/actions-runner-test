package com.cashmallow.api;

import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.impl.AuthServiceImpl;
import com.cashmallow.api.domain.shared.CashmallowException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class UserLogin {

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private AuthServiceImpl authService;

    @Disabled
    @Test
    void login() throws CashmallowException {
        String userId = "tiger002@ruu.kr".replaceAll("@", "").replaceAll("\\.", "");
        String password = "tiger002!";
        String refreshToken = travelerService.travelerLogin(userId, password, "fake", null, null, null, null, null, null, null);
        String accessToken = authService.issueAccessToken(refreshToken);
        log.info(accessToken);
    }
}
