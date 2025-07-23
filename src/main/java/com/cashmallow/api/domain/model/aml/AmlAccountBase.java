package com.cashmallow.api.domain.model.aml;

import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.cashmallow.api.domain.model.aml.AmlAccountBase.RltOpenPurposeCd.mappingPurpose;
import static com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose.DONATE;

@Builder
@Getter
public class AmlAccountBase {
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
    private String regUserId;

    /**
     * 최종 변경 사번
     */
    private String lastChangeUserId;

    // 계약 기본
    /**
     * 실명번호가 아닌 고유번호
     * traveler.userId
     */
    private String customerNo;

    /**
     * 거래한 투자/대출의 상태구분코드
     * 01:활동   ,08:해지
     */
    @Builder.Default
    private String accountStsCd = "01";

    /**
     * 단체/개인구분 코드
     * 01:개인,   02:법인
     */
    @Builder.Default
    private String accountOrgDiv = "01";

    /**
     * 거래하는 통화를 관리
     * 'KRW'
     */
    private String currencyCd;

    /**
     * 해외송금 상품코드
     * 01 해외송금, 03:타발송금
     */
    @Builder.Default
    private String prodRaCd = "03";

    /**
     * 상품분류
     * 01 수시입출금(송금)
     */
    @Builder.Default
    private String prodRepCd = "01";

    /**
     * 해외송금 실행일자
     * 개시일자
     */
    private String accountOpenDd;

    /**
     * 해외송금 해지(완제)일자
     * 실제 종료일자 (초기값 : 99991231 )
     */
    @Builder.Default
    private String closeDd = "99991231";

    /**
     * 해외송금 개설목적코드
     * 09:상속
     */
    private String accountOpenPurposeCd;

    /**
     * 계약 개설목적명
     * 한글명
     */
    private String accountOpenPurposeNm;

    /**
     * 주거래 영업점 코드
     * 관리부서
     */
    private String mainTranDeptCd;

    /**
     * 계좌 개설 영업점 코드
     * 영업부서
     */
    private String accountOpenDeptCd;

    /**
     * 계좌 개설 영업점명
     */
    private String accountOpenDeptNm;

    /**
     * 계좌 개설 영업점 우편번호
     */
    private String accountOpenDeptPostNo;

    /**
     * 해외송금 의 종류를 구분하는 코드
     * 03: 송금
     */
    @Builder.Default
    private String accountDiv = "03";

    /**
     * 등록 일시
     * 년월일시분초
     */
    // private String regDt;


    /**
     * 최종 변경 일시
     * 년월일시분초
     */
    // private String lastChangeDt;

    /**
     * AML Data적재일시
     * 년월일시분초
     */
    // private String createDt;

    public static class AmlAccountBaseBuilder {
        public AmlAccountBaseBuilder accountOpenPurposeCd(RemittancePurpose purpose) {

            RltOpenPurposeCd e = mappingPurpose(purpose);

            this.accountOpenPurposeCd = e.getCode();
            this.accountOpenPurposeNm = e.getName();

            // 기타인 경우 계약 개설목적명 직접 입력
            if (DONATE.equals(purpose)) {
                this.accountOpenPurposeNm = "기부";
            }
            return this;
        }

        public AmlAccountBaseBuilder accountOpenPurposeCd(RltOpenPurposeCd purposeCd) {
            this.accountOpenPurposeCd = purposeCd.getCode();
            this.accountOpenPurposeNm = purposeCd.getName();

            return this;
        }

        /*public OctaAMLKYCRequestBuilder accountOpenPurposeNm(String accountOpenPurposeNm) { // accountOpenPurposeCd에서 처리하여 사용하지 않음
            if ("01".equals(accountOpenPurposeNm)) {
                this.accountOpenPurposeNm = "OK";
            } else {
                this.accountOpenPurposeNm = accountOpenPurposeNm;
            }
            return this;
        }*/

        /**
         * Sets the account opening date of the OctaAMLKYCRequest.
         *
         * @param accountOpenDd The account opening date to be set.
         * @return The OctaAMLKYCRequestBuilder object.
         */
        public AmlAccountBaseBuilder accountOpenDd(Timestamp accountOpenDd) {
            if (accountOpenDd == null) {
                return this;
            }

            DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
            ZonedDateTime kstDateTime = accountOpenDd.toLocalDateTime()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));

            this.accountOpenDd = kstDateTime.format(yyyyMMdd); // UTC -> KST
            return this;
        }

        public AmlAccountBaseBuilder customerNo(String customerNo, String fromCountryCode) {
            this.customerNo = "HK".equalsIgnoreCase(fromCountryCode) ? "CM" + customerNo : fromCountryCode + customerNo;
            return this;
        }
    }

    /**
     * 01: 물품등사업상 대금결제
     * 02: 차입 등 부채상환
     * 03: 상속증여성거래
     * 99: 기타(기재)
     */
    @Getter
    @RequiredArgsConstructor
    public enum RltOpenPurposeCd {
        _01("01", "물품등사업상 대금결제"),
        _02("02", "차입 등 부채상환"),
        _03("03", "상속증여성거래"),
        _99("99", "기타(기재)"),
        UNKNOWN("UNKNOWN", "미지정")
        ;

        private final String code;
        private final String name;

        private static final Map<String, RltOpenPurposeCd> BY_CODE = new HashMap<>();

        static {
            for (RltOpenPurposeCd e : values()) {
                BY_CODE.put(e.code, e);
            }
        }

        public static RltOpenPurposeCd mappingPurpose(RemittancePurpose purpose) {
            if (purpose == null) {
                return UNKNOWN;
            }
            return switch (purpose) {
                case STUDY, LIVINGEXPENSES, TRAVELEXPENSES -> _03; // 상속증여성거래
                case GIFT -> _03; // 상속증여성거래
                case DONATE -> _99; // 기타(기재)
                default -> _99; // default: 기타(기재)
            };
        }
    }


}
