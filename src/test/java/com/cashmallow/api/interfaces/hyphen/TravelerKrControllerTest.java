package com.cashmallow.api.interfaces.hyphen;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerKrRequest;
import com.cashmallow.api.interfaces.traveler.web.TravelerKrController;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class TravelerKrControllerTest {

    @Autowired
    TravelerKrController travelerKrController;

    @Test
    void validRegisterTravelerKrRequest_성공() {
        // given
        RegisterTravelerKrRequest registerTravelerKrRequest = new RegisterTravelerKrRequest(
                CertificationType.ID_CARD,
                "홍길동",
                "gildong",
                "hong",
                "1234561234567",
                "20230404",
                Job.FINANCIAL_INDUSTRY,
                "",
                "509 Teheran-ro, Gangnam-gu, Seoul",
                "",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        List<String> strings = travelerKrController.validRegisterTravelerKrRequest(registerTravelerKrRequest);

        // then
        Assertions.assertThat(strings).isEqualTo(null);
        log.debug("{}", strings);

    }

    @Test
    void validRegisterTravelerKrRequest_빈주소() {
        // given
        RegisterTravelerKrRequest registerTravelerKrRequest = new RegisterTravelerKrRequest(
                CertificationType.ID_CARD,
                "홍길동",
                "gildong",
                "hong",
                "12345678",
                "20230404",
                Job.FINANCIAL_INDUSTRY,
                null,
                " ",
                "",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        List<String> strings = travelerKrController.validRegisterTravelerKrRequest(registerTravelerKrRequest);

        // then
        Assertions.assertThat(strings).isNotEmpty();
        log.debug("{}", strings);
    }

    @Test
    void validRegisterTravelerKrRequest_빈정보() {
        // given
        RegisterTravelerKrRequest registerTravelerKrRequest = new RegisterTravelerKrRequest(
                CertificationType.ID_CARD,
                " ",
                " ",
                " ",
                " ",
                " ",
                null,
                null,
                " ",
                "",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        List<String> strings = travelerKrController.validRegisterTravelerKrRequest(registerTravelerKrRequest);

        // then
        Assertions.assertThat(strings).isNotEmpty();
        log.debug("{}", strings);
    }

    @Test
    void validRegisterTravelerKrRequest_길이가_안맞는_주민번호() {
        // given
        RegisterTravelerKrRequest registerTravelerKrRequest = new RegisterTravelerKrRequest(
                CertificationType.ID_CARD,
                "홍길동",
                "gildong",
                "hong",
                "123456-1234567",
                "20230404",
                Job.FINANCIAL_INDUSTRY,
                null,
                "509 Teheran-ro, Gangnam-gu, Seoul",
                "",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        List<String> strings = travelerKrController.validRegisterTravelerKrRequest(registerTravelerKrRequest);

        // then
        Assertions.assertThat(strings).isNotEmpty();
        log.debug("{}", strings);
    }
}
