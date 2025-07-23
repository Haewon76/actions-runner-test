package com.cashmallow.api.interfaces.mallowlink.controller.dto;

import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
class WebhookResultRequestTest {

    ObjectMapper objectMapper = JsonStr.getMapper();

    @Test
    void withdrawalResultRequest() throws JsonProcessingException {
        // given
        String transactionResult = "{\n" +
                "  \"type\": \"WITHDRAWAL\",\n" +
                "  \"status\": \"SUCCESS_REVERT\",\n" +
                "  \"requestTime\": \"2023-06-28T17:38:00+09:00\",\n" +
                "  \"data\": {\n" +
                "    \"clientTransactionId\": \"23051600ABC123\",\n" +
                "    \"userId\": \"A12345678\",\n" +
                "    \"currency\": \"EUR\",\n" +
                "    \"amount\": \"1000.00\",\n" +
                "    \"latitude\": \"12.123456\",\n" +
                "    \"longitude\": \"123.123456\",\n" +
                "    \"ip\": \"123.123.0.123\"\n" +
                "  }\n" +
                "}";

        // when
        WebhookResultRequest request = objectMapper.readValue(transactionResult, WebhookResultRequest.class);

        // then
        log.info("request={}", request);
        // assertThat(request).isInstanceOf(WithdrawalResultRequest.class);
        // WithdrawalResultRequest remittanceResultRequest = (WithdrawalResultRequest) request;
        // assertThat(remittanceResultRequest.getData().getTransactionId()).isEqualTo("23051600ABC123");
        // assertThat(remittanceResultRequest.getStatus()).isEqualTo(SUCCESS_REVERT);

    }

    @Test
    void remittanceResultRequest() throws JsonProcessingException {
        // given
        String transactionResult = "{\n" +
                "  \"type\": \"REMITTANCE\",\n" +
                "  \"status\": \"RECEIVER_ERROR\",\n" +
                "  \"requestTime\": \"2023-06-28T17:38:00+09:00\",\n" +
                "  \"data\": {\n" +
                "    \"clientTransactionId\": \"23051600ABC123\",\n" +
                "    \"userId\": \"A12345678\",\n" +
                "    \"currency\": \"EUR\",\n" +
                "    \"amount\": \"1000.00\",\n" +
                "    \"latitude\": \"12.123456\",\n" +
                "    \"longitude\": \"123.123456\",\n" +
                "    \"ip\": \"123.123.0.123\"\n" +
                "  }\n" +
                "}";

        // when
        WebhookResultRequest request = objectMapper.readValue(transactionResult, WebhookResultRequest.class);

        // then
        log.info("request={}", request);
        // assertThat(request).isInstanceOf(RemittanceResultRequest.class);
        // RemittanceResultRequest remittanceResultRequest = (RemittanceResultRequest) request;
        // assertThat(remittanceResultRequest.getData().getTransactionId()).isEqualTo("23051600ABC123");
        // assertThat(remittanceResultRequest.getStatus()).isEqualTo(RECEIVER_ERROR);

    }

}