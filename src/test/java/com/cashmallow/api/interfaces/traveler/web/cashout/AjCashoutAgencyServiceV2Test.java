package com.cashmallow.api.interfaces.traveler.web.cashout;

import com.cashmallow.common.JsonUtil;
import com.cashmallow.config.EnableDevLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@Slf4j
@Disabled
@EnableDevLocal
class AjCashoutAgencyServiceV2Test {

    @Autowired
    AjCashoutAgencyServiceV2 ajCashoutAgencyServiceV2;

    @Autowired
    JsonUtil jsonUtil;

    @Test
    void getAgencies() {
        List<CashoutAgencyV2> agencies = ajCashoutAgencyServiceV2.getAgencies(new BigDecimal(100000));
        log.info(jsonUtil.toJsonPretty(agencies));
        Assertions.assertEquals(3, agencies.size());
    }
}