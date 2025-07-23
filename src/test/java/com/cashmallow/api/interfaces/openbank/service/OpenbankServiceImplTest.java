package com.cashmallow.api.interfaces.openbank.service;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.openbank.dto.OpenbankAuthResponse;
import com.cashmallow.config.EnableDevLocal;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;

@Slf4j
@SpringBootTest
@EnableDevLocal
class OpenbankServiceImplTest {

    @Autowired
    private OpenbankServiceImpl openbankService;

    @Autowired
    private Gson gson;

    @Disabled
    @Test
    public void testGetAccounts() throws CashmallowException, UnsupportedEncodingException {
        // given
        long userId = 1390; // tiger001@ruu.kr
        String deviceType = "IO";
        String deviceIp = "";
        String deviceId = "asdsadasdsad";
        String deviceVersion = "1";
        OpenbankAuthResponse userOAuth = openbankService.getUserOAuth(userId, deviceType, deviceIp, deviceId, deviceVersion);
        log.info(gson.toJson(userOAuth));
    }

    @Disabled
    @Test
    void 인증요청_URL() throws Exception {
        // given

        // when
        OpenbankAuthResponse response = openbankService.getUserOAuth(1391, "AD", "192.168.0.27", "ABCD", "11");
        System.out.println("gson.toJson(response) = " + gson.toJson(response));

        // then


    }

}