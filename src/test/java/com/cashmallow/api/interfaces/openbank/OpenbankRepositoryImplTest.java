package com.cashmallow.api.interfaces.openbank;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.openbank.Openbank;
import com.cashmallow.api.domain.model.openbank.OpenbankMapper;
import com.cashmallow.api.domain.model.openbank.OpenbankToken;
import com.cashmallow.api.interfaces.openbank.service.OpenbankRepositoryImpl;
import com.cashmallow.common.JsonStr;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class OpenbankRepositoryImplTest {

    @Autowired
    OpenbankRepositoryImpl openbankRepository;
    @Autowired
    OpenbankMapper openbankMapper;
    @Autowired
    SecurityService securityService;
    @Autowired
    Gson gsonPretty;

    private long travelerId = 2L;
    private String userSeqNo = "123456";
    private String accessToken = "acQEWR1234";
    private String refreshToken = "reQWER1234";

    @Test
    void insertOpenbankToken() {
        // given
        OpenbankToken openbankToken = new OpenbankToken(this.travelerId,
                this.accessToken,
                this.refreshToken,
                this.userSeqNo,
                ZonedDateTime.now(),
                ZonedDateTime.now());

        // when
        int i = openbankRepository.insertOpenbankToken(openbankToken);

        // then
        Openbank openbank = openbankMapper.getOpenbankByTravelerId(travelerId);
        assertThat(openbank.getTravelerId()).isEqualTo(openbankToken.getTravelerId());
        String accessToken = openbank.getAccessToken();
        log.debug("accessToken: {}", accessToken);
        assertThat(accessToken).isNotEmpty();
        assertThat(accessToken).isNotEqualTo(openbankToken.getAccessToken());
        assertThat(securityService.decryptAES256(accessToken)).isEqualTo(openbankToken.getAccessToken());
        String refreshToken = openbank.getRefreshToken();
        log.debug("refreshToken: {}", refreshToken);
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken).isNotEqualTo(openbankToken.getRefreshToken());
        assertThat(securityService.decryptAES256(refreshToken)).isEqualTo(openbankToken.getRefreshToken());
        assertThat(openbank.getSignYn()).isEqualTo("Y");
        // assertThat(openbank.getUpdatedDate()).isAfter(Date.from(LocalDateTime.now().minusSeconds(1).toInstant(ZoneOffset.UTC)))
        //         .isBefore(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));

    }

    @Disabled
    @Test
    void 리스트가져오기() {
        // given
        OpenbankToken openbankToken = new OpenbankToken(this.travelerId,
                this.accessToken,
                this.refreshToken,
                this.userSeqNo,
                ZonedDateTime.now().minus(91, ChronoUnit.DAYS),
                ZonedDateTime.now());

        int i = openbankRepository.insertOpenbankToken(openbankToken);

        // when
        List<Openbank> expiredTokenUser = openbankRepository.getExpiredTokenUser();

        System.out.println("JsonStr.toJson(expiredTokenUser) = " + JsonStr.toJson(expiredTokenUser));

        // then
        assertThat(expiredTokenUser)
                .filteredOn("accessToken", this.accessToken)
                .filteredOn("refreshToken", this.refreshToken)
                .filteredOn("userSeqNo", this.userSeqNo)
                .hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void updateOpenbankToken() {
        // given
        insertOpenbankToken();
        Openbank beforeOpenbank = openbankMapper.getOpenbankByTravelerId(travelerId);
        OpenbankToken openbankToken = new OpenbankToken(travelerId,
                "123456",
                "acQEWR4321",
                "reQWER4321",
                ZonedDateTime.now(),
                ZonedDateTime.now());

        // when
        int i = openbankRepository.updateOpenbankToken(openbankToken);

        // then
        Openbank openbank = openbankMapper.getOpenbankByTravelerId(travelerId);
        assertThat(openbank.getTravelerId()).isEqualTo(openbankToken.getTravelerId());
        String accessToken = openbank.getAccessToken();
        log.debug("accessToken: {}", accessToken);
        assertThat(accessToken).isNotEmpty();
        assertThat(accessToken).isNotEqualTo(openbankToken.getAccessToken());
        assertThat(securityService.decryptAES256(accessToken)).isEqualTo(openbankToken.getAccessToken());
        assertThat(accessToken).isNotEqualTo(beforeOpenbank.getAccessToken());
        String refreshToken = openbank.getRefreshToken();
        log.debug("refreshToken: {}", refreshToken);
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken).isNotEqualTo(openbankToken.getRefreshToken());
        assertThat(securityService.decryptAES256(refreshToken)).isEqualTo(openbankToken.getRefreshToken());
        assertThat(refreshToken).isNotEqualTo(beforeOpenbank.getRefreshToken());
        assertThat(openbank.getSignYn()).isEqualTo("Y");
    }

    @Test
    void updateOpenbankUserInfo() {
        // given
        insertOpenbankToken();
        // OpenbankUserInfoVO openbankUserInfoVO = new OpenbankUserInfoVO();

        // when

        // then


    }

    @Test
    void getOpenbank() {
        // given
        insertOpenbankToken();

        // when
        Openbank openbank = openbankRepository.getOpenbank(this.travelerId);

        // then
        assertThat(openbank.getTravelerId()).isEqualTo(this.travelerId);
        assertThat(openbank.getAccessToken()).isEqualTo(this.accessToken);
        assertThat(openbank.getRefreshToken()).isEqualTo(this.refreshToken);
    }

    @Test
    void deleteOpenbankUser() throws InterruptedException {
        // given
        insertOpenbankToken();
        Thread.sleep(1_000);

        // when
        int i = openbankRepository.deleteOpenbankUser(this.travelerId);

        // then
        Openbank openbank = openbankRepository.getOpenbank(this.travelerId);
        // assertThat(openbank.getOpenbankSignYn()).isEmpty();
        assertThat(openbank.getSignYn()).isEqualTo("N");
        assertThat(openbank.getAccessToken()).isEqualTo(null);
        assertThat(openbank.getRefreshToken()).isEqualTo(null);
        assertThat(openbank.getFintechUseNum()).isEqualTo("");
    }

    @Test
    void deleteOpenbankAccount() {

    }

}