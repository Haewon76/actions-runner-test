package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.withdrawalpartnercalc.WithdrawalPartnerCalcMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EncryptTypeHandlerForWithdrawalWebhookPartnerCalcMapperTest {


    @Autowired
    WithdrawalPartnerCalcMapper withdrawalPartnerCalcMapper;

    @Test
    @Transactional
    void TypeHandler_암복호화_getWithdrawalPartnerFeeCalcInFeeCalc() {

        Map<String, String> params = new HashMap<>();
        params.put("startRow", "0");
        params.put("size", "1");


        List<Object> obj = withdrawalPartnerCalcMapper.getWithdrawalPartnerFeeCalcInFeeCalc(params);

        assertNotNull(obj);


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getWithdrawalPartnerCalcP2Info() {

        Map<String, String> params = new HashMap<>();
        params.put("startRow", "0");
        params.put("size", "1");


        List<Object> obj = withdrawalPartnerCalcMapper.getWithdrawalPartnerCalcP2Info(params);

        assertNotNull(obj);


    }


}