package com.cashmallow.api.infrastructure.fcm;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class FcmServiceImplTest {

    @Autowired
    private FcmServiceImpl fcmService;

    @Test
    @DisplayName("Config 서버의 FCM 값을 잘 가져오는지 테스트 한다")
    public void getKeyTest() {
        String fcmAccessToken = fcmService.getFCMAccessToken();
        log.info("fcmAccessToken: " + fcmAccessToken);
        assertThat(fcmAccessToken).isNotEmpty();
    }

}