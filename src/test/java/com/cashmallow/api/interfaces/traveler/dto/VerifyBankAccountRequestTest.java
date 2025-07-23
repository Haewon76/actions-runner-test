package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VerifyBankAccountRequestTest {

    private ObjectMapper objectMapper = JsonStr.getMapper();

    @Test
    void body() throws Exception {
        // given
        String body = "{\n" +
                "  \"bankName\" : \"한국은행\",\n" +
                "  \"accountNo\" : \"1234\",\n" +
                "  \"accountName\" : \"홍길동\",\n" +
                "  \"addressCountry\" : \"KOR\",\n" +
                "  \"addressCity\" : \"seoul\",\n" +
                "  \"address\" : \"14, Seolleung-ro 94-gil, Gangnam-gu, Seoul, Republic of Korea\",\n" +
                "  \"addressSecondary\" : \"4floor\",\n" +
                "  \"requestTime\" : \"2023-07-23T16:55:00.000Z\"\n" +
                "}";

        // when
        VerifyBankAccountRequest parsedRequest = objectMapper.readValue(body, VerifyBankAccountRequest.class);

        // then
        VerifyBankAccountRequest verifyBankAccountRequest = new VerifyBankAccountRequest(11L,
                "한국은행",
                "1234",
                "홍길동",
                Country3.KOR,
                "seoul",
                "14, Seolleung-ro 94-gil, Gangnam-gu, Seoul, Republic of Korea",
                "4floor",
                ZonedDateTime.of(2023, 7, 24, 1, 55, 0, 0, ZoneId.of("Asia/Seoul")));
        // System.out.println("JsonStr.toJson(verifyBankAccountRequest) = " + JsonStr.toJson(verifyBankAccountRequest));

        assertThat(parsedRequest.getBankName()).isEqualTo(verifyBankAccountRequest.getBankName());
        assertThat(parsedRequest.getAccountNo()).isEqualTo(verifyBankAccountRequest.getAccountNo());
        assertThat(parsedRequest.getAccountName()).isEqualTo(verifyBankAccountRequest.getAccountName());
        assertThat(parsedRequest.getAddressCountry()).isEqualTo(verifyBankAccountRequest.getAddressCountry());
        assertThat(parsedRequest.getAddressCity()).isEqualTo(verifyBankAccountRequest.getAddressCity());
        assertThat(parsedRequest.getAddress()).isEqualTo(verifyBankAccountRequest.getAddress());
        assertThat(parsedRequest.getAddressSecondary()).isEqualTo(verifyBankAccountRequest.getAddressSecondary());
        assertThat(parsedRequest.getRequestTime()).isEqualToIgnoringNanos(verifyBankAccountRequest.getRequestTime());

    }
}