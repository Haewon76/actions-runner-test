package com.cashmallow.api.domain.model.aml;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.Traveler.TravelerSex;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class AMLCustomerIndv {
    /**
     * 시스템_구분
     */
    @Builder.Default
    private final String systemDiv = "CASHMFT";
    /**
     * 고객_번호
     */
    private String customerNo;
    /**
     * 실명_번호_구분
     * 01:주민등록번호(개인)
     * 02:주민등록번호(기타단체)
     * 03:사업자등록번호
     * 04:여권번호
     * 05:법인등록번호
     * 06:외국인등록번호
     * 07:재외국민거소신고번호
     * 08:투자자등록번호
     * 09:고유번호/납세번호
     * 11:BIC코드(SWIFT)
     * 12:해당국가법인번호
     * 14:CI번호
     * 99:기타
     */
    private String rnmNoDiv;
    /**
     * 실명_번호
     */
    private String rnmNo;
    /**
     * 여권_번호
     */
    private String passportNo;
    /**
     * 국적
     * KoFIU 코드
     */
    private String countryCd;
    /**
     * 출생_일자
     */
    private String birthDd;
    /**
     * 성별_코드
     * 1:남성
     * 2:여성
     */
    private String sexCd;
    /**
     * 국내_거주_여부
     * Y:국내거주
     * N:해외거주
     */
    @Builder.Default
    private String liveYn = "N";
    /**
     * 내외국인_구분
     * A:내국인
     * B:외국인
     */
    @Builder.Default
    private String foreignerDiv = "B";
    /**
     * 거주_국가
     * KoFIU 코드
     */
    private String liveCountryCd;
    /**
     * 직업_상세_코드
     * Job.java -> octaJobCode 확인
     */
    private String businessDtlCd;
    /**
     * KOFIU_직업구분코드
     * 01:직장인
     * 02:개인사업자
     * 03:무직
     * 91:파악할 수 없음
     */
    private String kofiuJobDivCd;
    /**
     * RA_접근경로
     * 01:대면
     * 02:전화
     * 03:모바일/인터넷
     */
    @Builder.Default
    private String amlRaChannelCd = "03";
    /**
     * 고액자산가_여부
     */
    @Builder.Default
    private String largeAmtAssetsYn = "N";
    /**
     * 사업자등록_번호
     */
    private String permitNo;
    /**
     * 개인_사업자_업종_코드
     */
    private String indvIndustryCd;
    /**
     * 개인_사업자_설립일자
     */
    private String indvCreateDd;
    /**
     * 개인_사업자_상호명
     */
    private String indvPermitNm;
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

    public static class AMLCustomerIndvBuilder {
        public AMLCustomerIndvBuilder kofiuJobDivCd(Job job) {
            this.kofiuJobDivCd = switch (job) {
                case BUSINESS, CLERK -> "01"; // 직장인
                case MEDICAL_INDUSTRY, LEGAL_INDUSTRY -> "01"; // 전문직
                case PUBLIC_ADMINISTRATION -> "01"; // 공무원
                case SERVICE_INDUSTRY, HOTEL_AND_CATERING_INDUSTRY, TRADING_WHOLESALE_AND_RETAIL_TRADES -> "02"; // 개인사업자
                case FREELANCER -> "02"; // 자유직/프리랜서
                case STUDENT, SOLDIER -> "03"; // 학생/군인
                // case HOUSEWIFE:
                //     return "03"; // 주부
                case INOCCUPATION -> "03"; // 무직
                // case REMITTANCE_INDUSTRY -> "91"; // 소액해외송금업 종사자
                // case TRUST_INDUSTRY -> "91"; // 신탁업종사자
                // case POLITICIAN -> "91"; // 정치인
                case FINANCIAL_INDUSTRY -> "91"; // 금융업종사자
                // case RELIGIOUS -> "91"; // 종교인
                case GAMBLING_INDUSTRY -> "91"; // 카지노사업
                case LENDING_INDUSTRY -> "91"; // 대부업
                case PRECIOUS_METAL_INDUSTRY -> "91"; // 귀금속판매업
                case FOREIGN_EXCHANGE_INDUSTRY -> "91"; // 환전업
                // case VIRTUAL_ASSET:
                //     return "91"; // 가상자산사업
                default -> "91"; // 기타 - 주부, 개인사업자, 가상자산사업 포함
            };

            return this;
        }

        public AMLCustomerIndvBuilder customerNo(String customerNo, String fromCountryCode) {
            this.customerNo = "HK".equalsIgnoreCase(fromCountryCode) ? "CM" + customerNo : fromCountryCode + customerNo;
            return this;
        }

        public AMLCustomerIndvBuilder sexCd(TravelerSex sex) {
            this.sexCd = switch (sex) {
                case MALE -> "1";
                case FEMALE -> "2";
                default -> "1";
            };

            return this;
        }
    }
}
