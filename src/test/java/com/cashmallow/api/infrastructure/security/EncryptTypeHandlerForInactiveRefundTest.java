package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.inactiveuser.InactiveRefund;
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
class EncryptTypeHandlerForInactiveRefundTest {

    @Autowired
    private InactiveUserMapper inactiveUserMapper;


    private InactiveRefund inactiveRefund;

    public void setUp() {

        inactiveRefund = new InactiveRefund();
        inactiveRefund.setId(20000000L);
        inactiveRefund.setTravelerId(10000000L);
        inactiveRefund.setAccountNo("829384892384");
        inactiveRefund.setAccountName("홍콩 한국은행 지점");
        inactiveRefund.setTrBankbookPhoto("dkfjldksjfl");
        inactiveRefund.setCreatedDate(new Timestamp(new Date().getTime()));

        inactiveUserMapper.insertInactiveRefund(inactiveRefund);

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveRemittanceList() {
        setUp();

        List<InactiveRefund> inactiveRefunds = inactiveUserMapper.getInactiveRefundList(inactiveRefund.getTravelerId());

        InactiveRefund inactiveRefundTest = inactiveRefunds.get(0);


        assertEquals(inactiveRefundTest.getAccountNo(), inactiveRefund.getAccountNo());
        assertEquals(inactiveRefundTest.getAccountName(), inactiveRefund.getAccountName());


    }

}