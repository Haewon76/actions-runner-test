package com.cashmallow.api.interfaces.hyphen;

import com.cashmallow.api.interfaces.hyphen.dto.CheckAccount1VO;
import com.cashmallow.api.interfaces.hyphen.dto.CheckAccount2VO;
import com.cashmallow.api.interfaces.hyphen.dto.FCS;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 하이픈 API 문서 - https://hyphen.im/product-api/view?seq=20
 */
@Slf4j
@SpringBootTest
@Disabled
class HyphenClientTest {

    @Autowired
    HyphenClient hyphenClient;

    @Test
    @Disabled
    void 계좌주확인_성공() {
        // given
        String bank_cd = "004"; // 국민은행 : 004
        String acct_no = "123412341234"; // 계좌번호

        FCS.request request = new FCS.request(bank_cd, acct_no);

        // when
        FCS.response response = hyphenClient.postFCS(request);

        // then
        Assertions.assertThat(response.getReply()).isEqualTo("0000");
        Assertions.assertThat(response.getName()).isEqualTo("홍길동");
        Assertions.assertThat(response.getReplyMsg()).isEqualTo("정상처리");

        /*
        {
          "name": "홍길동",
          "reply": "0000",
          "reply_msg": "정상처리"
        }
         */
    }

    @Test
    void 계좌주확인_계좌번호_없음() {
        // given
        String bank_cd = "004";
        String acct_no = "123412341234";

        FCS.request request = new FCS.request(bank_cd, acct_no);

        // when
        FCS.response response = hyphenClient.postFCS(request);

        // then
        Assertions.assertThat(response.getReply()).isNotEqualTo("0000");

        /*
        {
          "name": "",
          "reply": "0122",
          "reply_msg": "입금계좌오류"
        }
         */
    }


    @Test
    @Disabled
    void 인증1원_발송_성공() {
        // given
        String inBankCode = "004"; // 국민은행 : 004
        String inAccount = "123412341234"; // 계좌번호
        CheckAccount1VO.Request request = new CheckAccount1VO.Request(inBankCode, inAccount);

        // when
        CheckAccount1VO.Response response = hyphenClient.checkAccount1(request);
        System.out.println("response = " + response);

        // then
        Assertions.assertThat(response.getReplyCode()).isEqualTo("0000");
        Assertions.assertThat(response.getSuccessYn()).isEqualTo("Y");

        /*
        {
          "replyCode": "0000",
          "successYn": "Y",
          "sign": " ",
          "svcCharge": "000000000",
          "tradeTime": "135832",
          "oriSeqNo": "000070",
          "inPrintContent": "추운마루",
          "tr_date": "2023-02-17"
        }
         */
    }

    @Test
    @Disabled
    void 인증1원_검증_성공() {
        // given
        // 1원 인증 발송
        String inBankCode = "004"; // 국민은행 : 004
        String inAccount = "123412341234"; // 계좌번호
        CheckAccount1VO.Request request = new CheckAccount1VO.Request(inBankCode, inAccount);

        CheckAccount1VO.Response response = hyphenClient.checkAccount1(request);
        log.debug("response = " + response);

        String oriSeqNo = response.getOriSeqNo();
        String inPrintContent = response.getInPrintContent();
        CheckAccount2VO.Request request2 = new CheckAccount2VO.Request(oriSeqNo, inPrintContent);

        // when
        CheckAccount2VO.Response response2 = hyphenClient.checkAccount2(request2);
        log.debug("response2 = " + response2);

        // then
        Assertions.assertThat(response2.getSuccessYn()).isEqualTo("Y");
        Assertions.assertThat(response2.getError()).isEqualTo("N");

        /*
        {
          "successYn": "Y",
          "error": "N"
        }
         */
    }
}