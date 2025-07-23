package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.inactiveuser.InactiveExchange;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EncryptTypeHandlerForInactiveExchangeTest {

    @Autowired
    private InactiveUserMapper inactiveUserMapper;


    private InactiveExchange inactiveExchange;


    public void setUp() {
        inactiveExchange = new InactiveExchange();
        inactiveExchange.setId(2000000L);
        inactiveExchange.setTravelerId(2000000L);
        inactiveExchange.setTrAccountNo("TOW");
        inactiveExchange.setTrAccountName("TIGER");
        inactiveExchange.setIdentificationNumber("49043fb6e82fa884f0fc01be0218a40f");
        inactiveExchange.setTrAddress("한국 이다");
        inactiveExchange.setTrPhoneNumber("+932580963");
        inactiveExchange.setTrAddressSecondary("서울 어딘가");
        inactiveExchange.setCreatedDate(new Timestamp(new Date().getTime()));


        inactiveUserMapper.insertInactiveExchange(inactiveExchange);


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveUser() {
        setUp();

        List<InactiveExchange> inactiveExchanges = inactiveUserMapper.getInactiveExchangeList(inactiveExchange.getTravelerId());
        InactiveExchange inactiveExchangeTest = inactiveExchanges.get(0);

        assertEquals(inactiveExchangeTest.getTrAccountNo(), inactiveExchange.getTrAccountNo());
        assertEquals(inactiveExchangeTest.getTrAccountName(), inactiveExchange.getTrAccountName());
        assertEquals(inactiveExchangeTest.getTrAddress(), inactiveExchange.getTrAddress());
        assertEquals(inactiveExchangeTest.getTrAddressSecondary(), inactiveExchange.getTrAddressSecondary());
        assertEquals(inactiveExchangeTest.getTrPhoneNumber(), inactiveExchange.getTrPhoneNumber());

    }

}