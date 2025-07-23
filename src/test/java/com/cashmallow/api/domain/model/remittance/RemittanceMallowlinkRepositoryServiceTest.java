package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.common.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@Slf4j
@SpringBootTest
class RemittanceMallowlinkRepositoryServiceTest {

    @Autowired
    RemittanceMallowlinkRepositoryService remittanceMallowlinkRepositoryService;

    @Test
    void getRemittanceByMallowlinkTransactionId_optional처리() throws Exception {
        // given
        String id = "0" + RandomUtil.generateRandomString(RandomUtil.CAPITAL_ALPHA_NUMERIC, 15);

        // when
        Optional<Remittance> cm24011200000011 = remittanceMallowlinkRepositoryService.getRemittanceByMallowlinkTransactionId(id);

        // then
        Assertions.assertThat(cm24011200000011).isEmpty();

    }
}