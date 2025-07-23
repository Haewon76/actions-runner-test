package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.config.EnableDevLocal;
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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableDevLocal
@Slf4j
@Disabled
@SuppressWarnings({"unchecked", "rawtypes", "unused", "deprecation"})
public class TravelerV4ControllerTest {

    MockMvc mockMvc;

    @Autowired
    private TravelerController travelerController;

    @Autowired
    private TravelerExchangeController travelerExchangeController;

    @Autowired
    private Gson gson;

    // @BeforeEach
    // void init() {
    //     MockitoAnnotations.initMocks(this);
    //     this.mockMvc = MockMvcBuilders.standaloneSetup(travelerController).build();
    // }

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(travelerExchangeController).build();
    }

    @Test
    void 일본_환전_신청_테스트() throws Exception {
        mockMvc.perform(get("/traveler/v2/exchange/in-progress")
                        .header("Authorization", "")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void 자동_환불_신청_테스트() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("exchange_rate", new BigDecimal("0.171509"));
        requestBody.put("fee", 1);
        requestBody.put("fee_per_amt", 0);
        requestBody.put("fee_rate_amt", 1);
        requestBody.put("from_amt", 10);
        requestBody.put("from_cd", "009");
        requestBody.put("remit_id", 10491L);
        requestBody.put("to_amt", 57.3);
        requestBody.put("to_cd", "001");
        requestBody.put("timestamp", 1706765758);
        requestBody.put("deviceId", "40f1827c7350d7f6");

        mockMvc.perform(post("/traveler/v5/refunds")
                        .header("Authorization", "")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(requestBody)))
                .andExpect(status().isOk());
    }

    @Test
    void New_REFUND_신청_테스트() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from_cd", "003");
        requestBody.put("from_amt", new BigDecimal("10000.0"));
        requestBody.put("to_cd", "001");
        requestBody.put("to_amt", new BigDecimal("23.98"));
        requestBody.put("fee", new BigDecimal("35.54"));
        requestBody.put("exchange_rate", new BigDecimal("167.985241"));
        requestBody.put("fee_per_amt", new BigDecimal("35"));
        requestBody.put("fee_rate_amt", new BigDecimal("0.54"));

        // 둘중 하나만
        requestBody.put("wallet_id", 10617);
        // requestBody.put("remit_id", "");

        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyMDI0MDEwOS45NSIsImlhdCI6MTcwNDc4NDQxMCwiZXhwIjoxNzA0Nzg1MDEwLCJ1c2VyX2lkIjoiOTQ2NzE4Iiwicm9sZSI6IltcIlJPTEVfQU5PTllNT1VTXCIsIFwiUk9MRV9VU0VSXCJdIiwiaW5zdGFuY2VJZCI6IjM0N0JDRTdENjAyOEJFODg3MzNERTJGRUQ4M0NDNTM1MDVERDQ2NTM1REEwQ0ZCN0RCOTkxNURBMzE5NUVDQjQ1QjJCRDAzRkQwMDY4RUY4RjdEMUJBQTBCQTRENURGMkQ1MDAwMzIzQUFDNDkyNzFGRjEyNEEwRTg1MDBERUZCIn0.qaVTk4wZyULIxdAde1TQcQe2TsEnWleNA2Pvii6B4IE";

        mockMvc.perform(post("/traveler/v4/refunds")
                        .header("Authorization", token)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content(gson.toJson(requestBody)))
                .andExpect(status().isOk());
    }

    @Test
    void New_REFUND_취소_테스트() throws Exception {
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyMDI0MDEwOS45NSIsImlhdCI6MTcwNDc4NDQxMCwiZXhwIjoxNzA0Nzg1MDEwLCJ1c2VyX2lkIjoiOTQ2NzE4Iiwicm9sZSI6IltcIlJPTEVfQU5PTllNT1VTXCIsIFwiUk9MRV9VU0VSXCJdIiwiaW5zdGFuY2VJZCI6IjM0N0JDRTdENjAyOEJFODg3MzNERTJGRUQ4M0NDNTM1MDVERDQ2NTM1REEwQ0ZCN0RCOTkxNURBMzE5NUVDQjQ1QjJCRDAzRkQwMDY4RUY4RjdEMUJBQTBCQTRENURGMkQ1MDAwMzIzQUFDNDkyNzFGRjEyNEEwRTg1MDBERUZCIn0.qaVTk4wZyULIxdAde1TQcQe2TsEnWleNA2Pvii6B4IE";

        mockMvc.perform(delete("/traveler/v4/refunds/1026")
                        .header("Authorization", token)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }
}
