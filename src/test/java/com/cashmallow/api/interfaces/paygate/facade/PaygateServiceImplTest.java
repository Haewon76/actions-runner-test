package com.cashmallow.api.interfaces.paygate.facade;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsDepositRecordRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@Slf4j
@Disabled
class PaygateServiceImplTest {

    @Autowired
    private PaygateServiceImpl paygateService;

    @Test
    void DBS_입금내역_저장_테스트()  {
        String txnId = "test_gtop" + System.currentTimeMillis();
        DbsDepositRecordRequest dbsDepositRecordRequest = new DbsDepositRecordRequest();
        dbsDepositRecordRequest.setAmount(new BigDecimal("3027.56"));
        dbsDepositRecordRequest.setCurrency("HKD");
        dbsDepositRecordRequest.setTransactionId(txnId);
        dbsDepositRecordRequest.setExecutedDate(LocalDateTime.parse("2024-10-24T10:11:12"));
        dbsDepositRecordRequest.setSenderName("senderName");
        if(System.currentTimeMillis() % 2 == 0) {
            dbsDepositRecordRequest.setSenderAccountNo("senderTest");
        }

        try {
            paygateService.addDbsNotificationRecord(dbsDepositRecordRequest);
        } catch (CashmallowException e) {
            log.debug(e.getMessage());
        }
    }

}