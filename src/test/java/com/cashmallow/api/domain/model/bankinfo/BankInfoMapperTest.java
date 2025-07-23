package com.cashmallow.api.domain.model.bankinfo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BankInfoMapperTest {

    @Autowired
    BankInfoMapper bankInfoMapper;

    @Test
    void getBankInfoKrByCode() {
        // given

        // when
        BankInfo bankInfoKrByCode = bankInfoMapper.getBankInfoKrByCode("023");

        // then
        Assertions.assertThat(bankInfoKrByCode.getName()).isEqualTo("SC제일은행");
    }
}