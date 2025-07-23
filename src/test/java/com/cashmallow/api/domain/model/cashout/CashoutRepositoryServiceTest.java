package com.cashmallow.api.domain.model.cashout;

import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import com.cashmallow.config.EnableDevLocal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl.makeCustomerId;

@SpringBootTest
@EnableDevLocal
class CashoutRepositoryServiceTest {

    @Autowired
    CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    SevenBankServiceImpl sevenBankServiceImpl;

    @Test
    public void 퀸비_유니크아이디_테스트1() {
        Long userId = 1038827L;
        String customerId = cashoutRepositoryService.isCmCustomer(userId) ? makeCustomerId(userId) : makeCustomerId(userId, userId);
        System.out.println("customerId: " + customerId);
        Assertions.assertEquals("CM1038827", customerId);
    }

    @Test
    public void 기존거래_고객() {
        Long userId = 1038751L;

        String customerId = sevenBankServiceImpl.getCustomerId(userId, userId);
        System.out.println("customerId: " + customerId);
        Assertions.assertEquals("87518751", customerId);
    }

    @Test
    public void 최초거래_고객() {
        Long userId = 1038827L;

        String customerId = sevenBankServiceImpl.getCustomerId(userId, userId);
        System.out.println("customerId: " + customerId);
        Assertions.assertEquals("CM1038827", customerId);
    }

    @Test
    public void 기존거래_조건날짜_이전() {
        Long userId = 991089L;

        String customerId = sevenBankServiceImpl.getCustomerId(userId, userId);
        System.out.println("customerId: " + customerId);
        Assertions.assertEquals("10891089", customerId);
    }

    @Test
    public void 기존거래_조건날짜_이후() {
        Long userId = 1039954L;

        String customerId = sevenBankServiceImpl.getCustomerId(userId, userId);
        System.out.println("customerId: " + customerId);
        Assertions.assertEquals("CM1039954", customerId);
    }
}