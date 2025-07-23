package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.interfaces.TestService;
import com.cashmallow.config.EnableDevLocal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootTest
@EnableDevLocal
@Disabled
class TravelerControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Autowired
    TravelerController travelerController;

    @Autowired
    TestService testService;

    @Test
    public void getStorekeeperListByCountry_테스트() {
        String country = "005";
        String accessToken = testService.getAccessToken();

        testService.execute(() -> travelerController.getStorekeeperListByCountry(accessToken, country, request, response));
    }

    @Test
    void getWithdrawalPartnerCashpointListNearby() {

        String accessToken = testService.getAccessToken();

        testService.execute(
                () -> travelerController.getWithdrawalPartnerCashpointListNearby(
                        accessToken,
                        201L,
                        -6.1263167723803384,
                        106.65499203755039,
                        "1000",
                        TestService.request,
                        TestService.response
                )
        );
    }

    @Test
    void getStorekeeperListByCountry() {
        String accessToken = testService.getAccessToken();

        testService.execute(
                () -> travelerController.getStorekeeperListByCountry(
                        accessToken,
                        "005",
                        TestService.request,
                        TestService.response
                )
        );
    }

}