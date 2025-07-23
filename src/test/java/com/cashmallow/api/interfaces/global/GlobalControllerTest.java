package com.cashmallow.api.interfaces.global;

import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.dbs.controller.DbsController;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsDepositRecordRequest;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsRemittanceNotificationRequest;
import com.cashmallow.api.interfaces.global.controller.GlobalDepositController;
import com.cashmallow.api.interfaces.global.controller.GlobalFromTransactionController;
import com.cashmallow.api.interfaces.global.dto.GlobalManualMappingRequest;
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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Slf4j
@Disabled
public class GlobalControllerTest {
    MockMvc mockMvc;

    @Autowired
    private GlobalFromTransactionController globalFromTransactionController;

    @Autowired
    private Gson gson;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(globalFromTransactionController).build();
    }

    @Test
    void FROM_일본_수동매핑_테스트() throws Exception {

        // 환전 cc ID
        // 38392 68.01  972726
        // 38025
        // 37197
        // 37021

        GlobalManualMappingRequest globalManualMappingRequest = new GlobalManualMappingRequest(
                TransactionRecord.RelatedTxnType.EXCHANGE,
                38392L,
                new BigDecimal("68.01"),
                "JPY",
                972726L,
                "CHO OWNS",
                List.of("TEMP4696040"),
                CountryCode.JP,
                "0009",
                "123412341234"
        );

        mockMvc.perform(post("/global/jp/manual-mapping")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(globalManualMappingRequest)))
                .andExpect(status().isOk());
    }

}
