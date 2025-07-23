package com.cashmallow.api.interfaces.dbs;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.dbs.controller.DbsController;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsDepositRecordRequest;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsRemittanceNotificationRequest;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Slf4j
@Disabled
public class DbsControllerTest {
    MockMvc mockMvc;

    @Autowired
    private DbsController dbsController;

    @Autowired
    private Gson gson;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(dbsController).build();
    }

    @Test
    void DBS_송금_완료_노티피케이션_테스트() throws Exception {
        DbsRemittanceNotificationRequest request = new DbsRemittanceNotificationRequest();
        request.setRemittanceId("68413073CMTEMP9622231");
        request.setRemittanceResultCode("RJCT");
        request.setRemittanceRejectReason("테스트라서 실패");

        mockMvc.perform(post("/dbs/remittance/notification")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    void DBS_입금_내역_테스트() throws Exception {
        String tempTxnId = "TEMP" + String.valueOf(System.currentTimeMillis()).substring(6);
        ZonedDateTime hongkongTime = ZonedDateTime.now(ZoneId.of("UTC+8"));
        DbsDepositRecordRequest requestBody = new DbsDepositRecordRequest();
        requestBody.setCurrency(CountryCode.HK.getCurrency());
        requestBody.setAmount(new BigDecimal("74.31"));
        requestBody.setExecutedDate(hongkongTime.toLocalDateTime());
        requestBody.setTransactionId(tempTxnId);
        requestBody.setSenderAccountNo("123456");
        requestBody.setSenderName("CHO HYUN WOO");
        requestBody.setDepositType(Const.GPP);

        mockMvc.perform(post("/dbs/bank-accounts/records")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(requestBody)))
                .andExpect(status().isOk());
    }

}
