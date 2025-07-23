package com.cashmallow.api.interfaces.admin;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.interfaces.admin.dto.CountryStatsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AdminStatsServiceImplTest {

    @Autowired
    AdminStatsServiceImpl adminStatisticsService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getReconciliation() throws Exception {
        // ReconciliationResponse reconciliation = adminStatisticsService.getReconciliation(CountryCode.HK, 2022);
        List<CountryStatsDto> reconciliation1 = adminStatisticsService.getReconciliation(CountryCode.HK, 2023);

        // System.out.println("reconciliation2022 = " + JsonStr.toJson(reconciliation));
        System.out.println("reconciliation2023 = " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reconciliation1));
    }
}