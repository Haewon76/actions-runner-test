package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.impl.UserServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.interfaces.authme.AuthMeService;
import com.cashmallow.api.interfaces.crypto.CryptoService;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.config.EnableDevLocal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@SpringBootTest
@EnableDevLocal
@Slf4j
class TravelerV3ControllerTest {

    @Autowired
    AuthService authService;
    @Autowired
    AuthMeService authMeService;
    @Autowired
    CryptoService cryptoService;
    @Autowired
    UserServiceImpl userService;

    @Autowired
    TravelerV3Controller travelerV3Controller;

    @Test
    @SneakyThrows
    void testGetAuthMeToken() {
        // TravelerV3Controller controller = new TravelerV3Controller(
        //         authService,
        //         authMeService,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null,
        //         null
        // );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // String accessToken = "Bearer " + "012345678912";
        // System.out.println("accessToken = " + accessToken);
        // String responseString = controller.getAuthMeToken(accessToken, request, response);
        // System.out.println("responseString = " + responseString);
        // System.out.println("actualResult = " + cryptoService.decrypt(accessToken, responseString));
        // assertNotNull(expectedResult, actualResult);
    }

    @Test
    public void testRegisterTravelerV4() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String loginId = "skjo134ruukr";
        Long userId = 986414L;
        String refreshToken = authService.issueRefreshToken(loginId, userId, "[\"ROLE_ANONYMOUS\", \"ROLE_USER\"]");

        String token = "Bearer " + authService.issueAccessToken(refreshToken);
        String travelerRequestBody = CustomStringUtil.toJsonEncode(token, """
                    {
                      "certificationType" : "ID_CARD",
                      "identificationNumber" : "",
                      "job" : "SOCIAL_WORK_AND_HEALTHCARE_ACTIVITIES",
                      "sex" : "MALE",
                      "fundPurpose" : "TRAVELEXPENSES",
                      "fundSource" : "OWNPROPERTYDISPOAL",
                      "jobString" : "SOCIAL_WORK_AND_HEALTHCARE_ACTIVITIES"
                    }
                """);

        String responseJson = CustomStringUtil.decode(token, travelerV3Controller.registerTravelerV4(token, travelerRequestBody, request, response));
        log.debug("responseJson = {}", responseJson);

    }
}
