package com.cashmallow.api.infrastructure.aml;

import com.cashmallow.api.infrastructure.aml.dto.OctaWLFRequest;
import com.cashmallow.api.infrastructure.aml.dto.WLFResponse;
import com.cashmallow.config.EnableDevLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
@EnableDevLocal
class OctaWLFServiceTest {

    @Autowired
    OctaWLFService octaWLFService;

    String userId = "CMTEST002";
    String senderFirstName = "Gim";
    String senderLastName = "GILDONG";
    String senderBirthDate = "19900101";
    String senderCountryCd = "HK";

    String receiverFirstName = "gIL DoNG";
    String receiverLastName = "QIM";

    @Test
    void WLF_송금없는_경우_생년월일_없음() {
        OctaWLFRequest request = new OctaWLFRequest(
                userId,
                null,
                senderFirstName,
                senderLastName,
                null,
                null,
                "TEST_ADMIN",
                null,
                senderCountryCd,
                null,
                null,
                "HK"
        );
        final WLFResponse execute = octaWLFService.execute(request);
        log.info("execute: " + execute);
    }

    @Test
    void WLF_송금없는_경우_생년월일_존재() {
        OctaWLFRequest request = new OctaWLFRequest(
                userId,
                null,
                senderFirstName,
                senderLastName,
                null,
                null,
                "TEST_ADMIN",
                senderBirthDate,
                senderCountryCd,
                null,
                null,
                "HK"
        );
        octaWLFService.execute(request);
    }

    @Test
    void WLF_송금있는_경우_생년월일_존재() {
        final String remittanceId = "RM001";
        OctaWLFRequest request = new OctaWLFRequest(
                userId,
                remittanceId,
                senderFirstName,
                senderLastName,
                receiverFirstName,
                receiverLastName,
                "TEST_ADMIN",
                senderBirthDate,
                senderCountryCd,
                "19880202",
                "KR",
                "HK"
        );
        octaWLFService.execute(request);
    }

    @Test
    void WLF_송금있는_경우_생년월일_없음() {
        final String remittanceId = "RM001";
        OctaWLFRequest request = new OctaWLFRequest(
                userId,
                remittanceId,
                senderFirstName,
                senderLastName,
                receiverFirstName,
                receiverLastName,
                "TEST_ADMIN",
                senderBirthDate,
                senderCountryCd,
                null,
                "KR",
                "HK"
        );
        octaWLFService.execute(request);
    }
}