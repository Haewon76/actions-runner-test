package com.cashmallow.api.domain.model.aml;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AMLCustomerBase {
    /**
     * 시스템_구분
     */
    @Builder.Default
    private final String systemDiv = "CASHMFT";
    /**
     * 고객_번호
     */
    private String customerNo;
    // AML_고객_기본 : AML_CUSTOMER_BASE
    /**
     * 고객_구분
     * 01 : 개인
     * 02 : 법인
     */
    @Builder.Default
    private String customerDiv = "01";
    /**
     * TMS고객_구분
     * 01 : 개인
     * 02 : 법인
     * 03 : 개인사업자
     */
    @Builder.Default
    private String tmsCustomerDiv = "01";
    /**
     * 고객_유형_코드
     * 01:비영리단체
     * 02:고액자산가
     * 04:금융기관
     * 05:국가 ,지방자치단체
     * 06:UN산하 국제자선기구
     * 07:상장회사
     * 08:기타 (개인의 경우 )
     */
    @Builder.Default
    private String customerTpCd = "08";
    /**
     * 고객_명
     */
    private String customerNm;
    /**
     * 고객_영문_명
     */
    private String customerEngNm;
    /**
     * 고객_상태_코드
     * 01:활동
     * 02:휴면(탈회)
     */
    @Builder.Default
    private String customerStsCd = "01";
    /**
     * 고객_등록_일자
     * YYYYMMDD
     */
    private String customerRegDd;
    /**
     * 고객_수정_일자
     * YYYYMMDD
     */
    private String customerEditDd;
    /**
     * 고객_삭제_일자
     * YYYYMMDD
     */
    private String customerDelDd;
    /**
     * 가상자산_취급_사업자_여부
     */
    private String virtualMoneyBusinessYn;
    /**
     * KYC_다음_수행일자
     */
    private String kycNextExecDd;
    /**
     * 투자자분류코드
     */
    private String investTypCd;
    /**
     * 등록_직원_ID
     */
    private String regUserId;
    /**
     * 최종_변경_직원_ID
     */
    private String lastChangeUserId;
    /**
     * 등록_일시
     */
    // private String regDt;
    /**
     * 최종_변경_일시
     */
    // private String lastChangeDt;
    /**
     * 생성_일시
     */
    // private String createDt;

    public static class AMLCustomerBaseBuilder {
        public AMLCustomerBaseBuilder customerNo(String customerNo, String fromCountryCode) {
            this.customerNo = "HK".equalsIgnoreCase(fromCountryCode) ? "CM" + customerNo : fromCountryCode + customerNo;
            return this;
        }
    }
}
