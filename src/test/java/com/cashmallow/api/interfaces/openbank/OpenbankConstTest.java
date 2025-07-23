package com.cashmallow.api.interfaces.openbank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OpenbankConstTest {

    @Autowired
    OpenbankConst openbankConst;

    @Test
    void 오픈뱅킹_설정_불러오기() {
        // given

        // when
        String url = openbankConst.getURL();
        String clientId = openbankConst.getClientId();
        String secret = openbankConst.getSecret();
        String cashmallowCd = openbankConst.getCashmallowCd();
        String cntrBankCode = openbankConst.getCntrBankCode();
        String cntrAccountNum = openbankConst.getCntrAccountNum();
        String cntrAccountName = openbankConst.getCntrAccountName();

        // then
        assertThat(url).isEqualTo("https://testapi.openbanking.or.kr");
        assertThat(secret).isNotEmpty();
        assertThat(clientId).isNotEmpty();
        assertThat(cashmallowCd).isNotEmpty();
        assertThat(cntrBankCode).isNotEmpty();
        assertThat(cntrAccountNum).isNotEmpty();
        assertThat(cntrAccountName).isNotEmpty();
    }


}