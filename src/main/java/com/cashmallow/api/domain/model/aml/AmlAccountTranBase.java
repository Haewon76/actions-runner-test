package com.cashmallow.api.domain.model.aml;

import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.cashmallow.api.domain.model.aml.AmlAccountBase.RltOpenPurposeCd;
import static com.cashmallow.api.domain.model.aml.AmlAccountBase.RltOpenPurposeCd.mappingPurpose;

@Builder
@Getter
public class AmlAccountTranBase {
    // 공통 필드
    /**
     * 시스템 구분코드
     * CASHMFT
     */
    @Builder.Default
    private String systemDiv = "CASHMFT";
    /**
     * 입출금거래의 대출번호 OR 투자번호
     */
    private String accountNo;

    /**
     * 해외송금 상품코드
     * 01 해외송금, 03:타발송금
     */
    @Builder.Default
    private String prodCd = "03";

    /**
     * 등록 사번
     */
    @Builder.Default
    private String regUserId = "PRISM_ADMIN";

    /**
     * 최종 변경 사번
     */
    @Builder.Default
    private String lastChangeUserId = "PRISM_ADMIN";

    // 계약 거래 기본
    /**
     * 거래가 발생한 일자
     * YYYYMMDD
     */
    private String tranDd;

    /**
     * 거래가 발생한 일자의 유니크한 일련번호
     */
    private long tranSeqNo;

    /**
     * 실명번호가 아닌 고유번호
     */
    private String customerNo;

    /**
     * 거래가 발생한 시간
     * 년월일시분초
     */
    private String tranTime;

    /**
     * 거래 방법 OR 경로
     * 19:모바일
     */
    @Builder.Default
    private String tranChannelCd = "19";

    /**
     * 거래 방법 OR 경로
     * 모바일
     */
    @Builder.Default
    private String tranChannelNm = "모바일";

    /**
     * 거래를 이루는 재화의 종류
     * 05:외환 (해외송금)
     */
    @Builder.Default
    private String tranWayCd = "05";

    /**
     * 거래를 이루는 재화의 종류명
     * 해외송금
     */
    @Builder.Default
    private String tranWayNm = "해외송금";

    /**
     * 거래를 분류하는 종류
     * 05:송금(해외)
     */
    @Builder.Default
    private String tranKindCd = "05";

    /**
     * 거래를 분류하는 종류명
     * 송금 , 이체영수
     */
    @Builder.Default
    private String tranKindNm = "송금, 이체영수";

    /**
     * 거래를 세분화한 코드
     */
    @Builder.Default
    private String summaryCd = "05";

    /**
     * 거래를 세분화한 코드명
     */
    @Builder.Default
    private String summaryNm = "송금, 이체영수";

    /**
     * STR거래를 분류하는 종류코드
     * 05:송금(해외)
     */
    @Builder.Default
    private String strTranKindCd = "05";

    /**
     * STR거래를 분류하는 종류명
     * 송금 , 이체영수
     */
    @Builder.Default
    private String strTranKindNm = "송금";

    /**
     * 은행_국가_코드
     * 고객
     */
    private String rltFinanOrgCountryCd;

    /**
     * 은행_코드(KOFIU코드 3byte)
     * 고객
     */
    @Builder.Default
    private String rltFinanOrgCd = "001";

    /**
     * 은행명칭(한글)
     * 고객
     */
    @Builder.Default
    private String rltFinanOrgNm = "한국은행";

    /**
     * 계좌_번호
     * Bank transfer: 계좌번호
     * Cash pickup: 전화번호(콜링코드를 제외한 번호)
     * Cash card: 카드번호
     * Mobile wallet: 모바일월렛 번호
     * *이외 송금방식은 수취의 기준이 되는 숫자열을 기준으로 이행
     * *당발타행 : 수취자 정보
     */
    private String rltaccNo;

    /**
     * 계좌주명
     * 고객
     */
    private String rltaccOwnerNm;

    /**
     * 당타발 구분
     * 1:당발,  2:타발
     */
    @Builder.Default
    private String toFcFrFcDiv = "2";

    /**
     * 계좌의 종류코드
     * 02송.수취계좌
     */
    @Builder.Default
    private String accountTpDiv = "02";

    /**
     * 계좌의 종류코드 명
     */
    @Builder.Default
    private String accountDivTpNm = "송.수취계좌";

    /**
     * 거래하는 통화를 관리
     * 'KRW'
     */
    private String currencyCd;

    /**
     * 거래금액(송금)
     */
    private BigDecimal tranAmt;

    /**
     * 원화환산금액
     */
    private BigDecimal wonTranAmt;

    /**
     * 외화거래금액
     */
    private BigDecimal fexTranAmt;

    /**
     * 달러환산금액
     */
    private BigDecimal usdExchangeAmt;

    /**
     * 거래의 목적코드
     * 01:물품/사업상 대금결재
     * 02:차입/부채상환
     * 03:상속증여성 거래
     * 99:기타(기재)
     */
    private String tranPurposeCd;

    /**
     * 거래의 목적코드명
     */
    private String tranPurposeNm;

    /**
     * 외환거래의 목적코드
     * 01 증여송금
     * 02 여행경비
     * 03 유학경비
     * 04 해외체제비
     * 05 해외이주비
     * 06 외화예금
     * 07 해외직접투자
     * 08 무역거래
     * 09 무역외거래
     */
    private String fexTranPurposeCd;

    /**
     * 외환거래의 목적코드명
     * 증여송금
     */
    private String fexTranPurposeNm;

    public static class AmlAccountTranBaseBuilder {
        public AmlAccountTranBaseBuilder tranDd(Timestamp tranDd) {
            if (tranDd == null) {
                return this;
            }

            // UTC -> KST
            final ZonedDateTime zonedDateTime = tranDd.toLocalDateTime()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));

            this.tranDd = zonedDateTime.toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            return this;

        }

        public AmlAccountTranBaseBuilder tranTime(Timestamp tranTime) {
            if (tranTime == null) {
                return this;
            }

            // UTC -> KST
            final ZonedDateTime zonedDateTime = tranTime.toLocalDateTime()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));

            this.tranTime = zonedDateTime.toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            return this;
        }

        public AmlAccountTranBaseBuilder customerNo(String customerNo, String fromCountryCode) {
            this.customerNo = "HK".equalsIgnoreCase(fromCountryCode) ? "CM" + customerNo : fromCountryCode + customerNo;
            return this;
        }

        public AmlAccountTranBaseBuilder fexTranPurposeCd(RemittancePurpose purposeCd) {
            RltOpenPurposeCd e = mappingPurpose(purposeCd);
            this.fexTranPurposeCd = e.getCode();
            this.fexTranPurposeNm = e.getName();
            return this;
        }

        public AmlAccountTranBaseBuilder tranPurposeCd(RemittancePurpose purposeCd) {
            RltOpenPurposeCd e = mappingPurpose(purposeCd);
            this.tranPurposeCd = e.getCode();
            this.tranPurposeNm = e.getName();
            return this;
        }
    }
}
