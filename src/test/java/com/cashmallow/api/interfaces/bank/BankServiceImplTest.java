package com.cashmallow.api.interfaces.bank;

import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.interfaces.bank.dto.BankInfoVO;
import com.cashmallow.common.JsonStr;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class BankServiceImplTest {

    @Autowired
    BankServiceImpl bankService;

    @Autowired
    Gson gson;

    @Test
    void 은행목록_가져오기() {
        // given

        // when
        List<BankInfoVO> kr = bankService.getBankInfos(CountryInfo.KR, BigDecimal.ZERO);
        List<BankInfoVO> hk = bankService.getBankInfos(CountryInfo.HK, BigDecimal.ZERO);
        List<BankInfoVO> id1 = bankService.getBankInfos(CountryInfo.ID, BigDecimal.ZERO);
        List<BankInfoVO> id2 = bankService.getBankInfos(CountryInfo.ID, BigDecimal.valueOf(500_000_001));

        // then
        log.debug("bankInfos={}", JsonStr.toJson(bankService.getBankIconMap()));
        log.debug("kr={}", JsonStr.toJson(kr));
        log.debug("hk={}", JsonStr.toJson(hk));
        log.debug("id1={}", JsonStr.toJson(id1));
        log.debug("id2={}", JsonStr.toJson(id2));

        assertThat(kr).isNotEmpty();
        assertThat(hk).isNotEmpty();
        assertThat(id1).isNotEmpty();
        assertThat(id2).isNotEmpty();
        assertThat(id1.size()).isNotEqualTo(id2.size());
    }
}