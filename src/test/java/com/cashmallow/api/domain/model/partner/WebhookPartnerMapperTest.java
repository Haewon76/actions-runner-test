package com.cashmallow.api.domain.model.partner;

import com.cashmallow.common.JsonStr;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@SpringBootTest
class WebhookPartnerMapperTest {

    @Autowired
    PartnerMapper partnerMapper;

    // @Disabled
    @Test
    void insertWithdrawalPartnerMaintenance() {
        ZonedDateTime startAt = ZonedDateTime.of(2023, 9, 17, 1, 30, 0, 0, ZoneId.of("Asia/Bangkok"));
        ZonedDateTime endAt = ZonedDateTime.of(2023, 9, 17, 6, 30, 0, 0, ZoneId.of("Asia/Bangkok"));
        // partnerMapper.insertWithdrawalPartnerMaintenance(193L, startAt, endAt);

        List<WithdrawalPartnerMaintenance> withdrawalPartnerMaintenances = partnerMapper.selectWithdrawalPartnerMaintenances(193L);
        log.info("withdrawalPartnerMaintenances={}", JsonStr.toJson(withdrawalPartnerMaintenances));
    }

}