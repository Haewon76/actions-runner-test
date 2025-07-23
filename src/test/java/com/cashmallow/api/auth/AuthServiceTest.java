package com.cashmallow.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    @Description("HEX 코드에 대한 [성공] 테스트를 진행합니다.")
    public void checkHexCodeIfSuccess() {
        String token = "1111111111";
        boolean hexaStr = authService.isHexaStr(token);
        assertEquals(hexaStr, true);
    }

    @Test
    @Description("HEX 코드에 대한 [실패] 테스트를 진행합니다.")
    public void checkHexCodeIfFail() {
        String token = "33333";
        boolean hexaStr = authService.isHexaStr(token);
        assertEquals(hexaStr, false);
    }

}