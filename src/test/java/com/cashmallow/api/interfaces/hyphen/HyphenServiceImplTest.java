package com.cashmallow.api.interfaces.hyphen;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.hyphen.dto.CertificationReqVO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * 하이픈 API 문서 - https://hyphen.im/product-api/view?seq=20
 * 진품 로직은 본인 데이터를 넣고 테스트 해보세요.
 */
@Transactional
@SpringBootTest
@Disabled
class HyphenServiceImplTest {


    @Autowired
    private HyphenServiceImpl hyphenService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Test
    @Disabled
    void 주민등록증_진품() {
        // given
        CertificationType idCard = CertificationType.ID_CARD;
        String localName = "홍길동";
        String identificationNumber = "1234561234567";
        String issueDate = "20230123";
        String birthDay = "";
        String licenceNo = "";
        String serialNo = "";

        CertificationReqVO certificationReqVO = new CertificationReqVO(idCard, localName, identificationNumber, issueDate, birthDay, licenceNo, serialNo);

        // when
        boolean certification = hyphenService.certification(certificationReqVO);

        // then
        assertThat(certification).isEqualTo(true);

        /* 응답
        {
          "common": {
            "userTrNo": "",
            "hyphenTrNo": "10202302170000003890",
            "errYn": "N",
            "errMsg": ""
          },
          "data": {
            "juminNo": "880217-*******",
            "name": "아이유",
            "issueDt": "20140919",
            "truthYn": "Y",
            "truthMsg": "입력하신 내용은 등록된 내용과 일치합니다."
          },
          "resCd": "0000",
          "resMsg": "정상",
          "out": {
            "errMsg": "",
            "resMsg": "",
            "outB0001": {
              "errYn": "N",
              "errMsg": "",
              "juminNo": "880217-*******",
              "name": "아이유",
              "issueDt": "20140919",
              "truthYn": "Y",
              "truthMsg": "입력하신 내용은 등록된 내용과 일치합니다."
            },
            "resCd": "0000",
            "device": "Windows",
            "errYn": "N"
          }
        }
        */
    }

    @Test
    void 주민등록증_위조() {
        // given
        CertificationType idCard = CertificationType.ID_CARD;
        String localName = "홍길동";
        String identificationNumber = "1234561234567";
        String issueDate = "20230123";
        String birthDay = "";
        String licenceNo = "";
        String serialNo = "";

        CertificationReqVO certificationReqVO = new CertificationReqVO(idCard, localName, identificationNumber, issueDate, birthDay, licenceNo, serialNo);

        // when
        boolean certification = hyphenService.certification(certificationReqVO);

        // then
        assertThat(certification).isEqualTo(false);

        /*
        {
          "common": {
            "userTrNo": "",
            "hyphenTrNo": "10202302170000003940",
            "errYn": "N",
            "errMsg": ""
          },
          "data": {
            "juminNo": "780217-*******",
            "name": "아이유",
            "issueDt": "20140919",
            "truthYn": "N",
            "truthMsg": "입력하신 내용은 사용할 수 없는 주민등록증입니다. 궁금하신 사항은 가까운 읍면동에 문의하시기 바랍니다."
          },
          "resCd": "0000",
          "resMsg": "정상",
          "out": {
            "errMsg": "",
            "resMsg": "",
            "outB0001": {
              "errYn": "N",
              "errMsg": "",
              "juminNo": "780217-*******",
              "name": "아이유",
              "issueDt": "20140919",
              "truthYn": "N",
              "truthMsg": "입력하신 내용은 사용할 수 없는 주민등록증입니다. 궁금하신 사항은 가까운 읍면동에 문의하시기 바랍니다."
            },
            "resCd": "0000",
            "device": "Windows",
            "errYn": "N"
          }
        }
         */
    }


    @Test
    @Disabled
    void 운전면허증_진품() {
        // given
        CertificationType idCard = CertificationType.DRIVER_LICENSE;
        String localName = "홍길동";
        String identificationNumber = "";
        String issueDate = "";
        String birthDay = "19890316";
        String licenceNo = "121212345612";
        String serialNo = "a1b2c3";

        CertificationReqVO certificationReqVO = new CertificationReqVO(idCard, localName, identificationNumber, issueDate, birthDay, licenceNo, serialNo);

        // when
        boolean certification = hyphenService.certification(certificationReqVO);

        // then
        assertThat(certification).isEqualTo(true);

        /*
        {
          "common": {
            "userTrNo": "",
            "hyphenTrNo": "10202302170000004969",
            "errYn": "N",
            "errMsg": ""
          },
          "data": {
            "licenceTruthYn": "Y",
            "licenceTruthMsg": "도로교통공단 전산 자료와 일치합니다.",
            "serialNoTruthYn": "Y",
            "serialNoTruthMsg": "암호일련번호가 일치합니다.",
            "name": "홍길동",
            "juminNo": "19890316",
            "licenceNo": "121212345612",
            "serialNo": "a1b2c3",
            "searchTime": "2023년 02월 17일 13:11"
          },
          "resCd": "0000",
          "resMsg": "정상",
          "out": {
            "errMsg": "",
            "resMsg": "",
            "infotechLog": [],
            "site": "efine",
            "outB0001": {
              "errYn": "N",
              "errMsg": "",
              "licenceTruthYn": "Y",
              "licenceTruthMsg": "도로교통공단 전산 자료와 일치합니다.",
              "serialNoTruthYn": "Y",
              "serialNoTruthMsg": "암호일련번호가 일치합니다.",
              "name": "홍길동",
              "juminNo": "19890316",
              "licenceNo": "121212345612",
              "serialNo": "a1b2c3",
              "searchTime": "2023년 02월 17일 13:11"
            },
            "resCd": "0000",
            "device": "linux",
            "errYn": "N"
          }
        }
         */
    }

    /**
     * 운전면허증 거짓
     * 인적 정보 불일치, 면허 번호 부정확, 암호일련번호 불일치
     */
    @Test
    void 운전면허증_위조() {
        // given
        var idCard = CertificationType.DRIVER_LICENSE;
        String localName = "홍길동";
        String identificationNumber = "";
        String issueDate = "";
        String birthDay = "20890316";
        String licenceNo = "121212345612";
        String serialNo = "a1b2c3";

        CertificationReqVO certificationReqVO = new CertificationReqVO(idCard, localName, identificationNumber, issueDate, birthDay, licenceNo, serialNo);

        // when
        boolean certification = hyphenService.certification(certificationReqVO);

        // then
        assertThat(certification).isEqualTo(false);

        /*
        {
          "common": {
            "userTrNo": "",
            "hyphenTrNo": "10202302170000005037",
            "errYn": "N",
            "errMsg": ""
          },
          "data": {
            "licenceTruthYn": "N",
            "licenceTruthMsg": "면허번호가 잘못 입력되었습니다.",
            "serialNoTruthYn": "N",
            "serialNoTruthMsg": "",
            "name": "홍길동",
            "juminNo": "19890316",
            "licenceNo": "부산1212345612",
            "serialNo": "a1b2c3",
            "searchTime": "2023년 02월 17일 13:21"
          },
          "resCd": "0000",
          "resMsg": "정상",
          "out": {
            "errMsg": "",
            "resMsg": "",
            "infotechLog": [],
            "site": "efine",
            "outB0001": {
              "errYn": "N",
              "errMsg": "",
              "licenceTruthYn": "N",
              "licenceTruthMsg": "면허번호가 잘못 입력되었습니다.",
              "serialNoTruthYn": "N",
              "serialNoTruthMsg": "",
              "name": "홍길동",
              "juminNo": "19890316",
              "licenceNo": "부산1212345612",
              "serialNo": "a1b2c3",
              "searchTime": "2023년 02월 17일 13:21"
            },
            "resCd": "0000",
            "device": "Windows",
            "errYn": "N"
          }
        }
        */
    }


    @Test
    @Disabled
    void 외국인등록증_진품() {
        // given
        var idCard = CertificationType.RESIDENCE_CARD;
        String localName = "HONGGILDONG";
        String identificationNumber = "1234561234567";
        String issueDate = "20230123";
        String birthDay = "";
        String licenceNo = "";
        String serialNo = "";

        CertificationReqVO certificationReqVO = new CertificationReqVO(idCard, localName, identificationNumber, issueDate, birthDay, licenceNo, serialNo);

        // when
        boolean certification = hyphenService.certification(certificationReqVO);

        // then
        ApiResultVO expect = new ApiResultVO();
        expect.setSuccessInfo();
        assertThat(certification).isEqualTo(true);
    }


    @Test
    void 외국인등록증_위조() {
        // given
        var idCard = CertificationType.RESIDENCE_CARD;
        String localName = "HONGGILDONG";
        String identificationNumber = "1234561234567";
        String issueDate = "20230123";
        String birthDay = "";
        String licenceNo = "";
        String serialNo = "";

        CertificationReqVO certificationReqVO = new CertificationReqVO(idCard, localName, identificationNumber, issueDate, birthDay, licenceNo, serialNo);

        // when
        boolean certification = hyphenService.certification(certificationReqVO);

        // then
        assertThat(certification).isEqualTo(false);
    }


    @Test
    @Disabled
    void 계좌주확인_성공() {
        // given
        long travelerId = 1234L;
        String bankCode = "009";
        String accountNumber = "91234123456";

        // when
        String name = hyphenService.checkFCS(travelerId, bankCode, accountNumber);

        // then
        Traveler travelerByTravelerId = travelerRepositoryService.getTravelerByTravelerId(1234L);
        assertThat(name).isEqualTo(travelerByTravelerId.getLocalLastName() + travelerByTravelerId.getLocalFirstName());

        /*
        {
          "name": "홍길동",
          "reply": "0000",
          "reply_msg": "정상처리"
        }
         */
    }

    @Test
    void 계좌주확인_실패_계좌번호다름() {
        // given
        long travelerId = 2L;
        String bankCode = "009";
        String accountNumber = "91234123456";

        // when
        Throwable thrown = Assertions.catchThrowable(() -> hyphenService.checkFCS(travelerId, bankCode, accountNumber));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessage("WRONG_ACCOUNT_INFO");

        /*
        {
            "name": "",
            "reply": "0122",
            "reply_msg": "입금계좌오류"
        }
        */
    }

    @Test
    void 계좌주확인_실패_여행자없음() {
        // given
        long travelerId = -1L;
        String bankCode = "009";
        String accountNumber = "91234123456";

        // when
        Throwable thrown = Assertions.catchThrowable(() -> hyphenService.checkFCS(travelerId, bankCode, accountNumber));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessage("TRAVELER_CANNOT_FIND");
    }

}