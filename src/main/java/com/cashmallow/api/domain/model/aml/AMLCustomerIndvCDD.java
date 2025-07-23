package com.cashmallow.api.domain.model.aml;

import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static com.cashmallow.api.domain.model.aml.AMLCustomerIndvCDD.TranFundSourceDiv.mappingFundSource;

@Builder
@Getter
public class AMLCustomerIndvCDD {
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
     * CDD 수행일자(이력생성)
     */
    private String cddPerDt;
    /**
     * CDD 수행일자
     */
    // private String cddPerDt;
    /**
     * 거주구분_코드
     */
    private String liveDivCd;
    /**
     * 자택_주소_국가_코드
     */
    private String homeAddrCountryCd;
    /**
     * 자택_주소_표시_구분
     */
    @Builder.Default
    private String homeAddrDisplayDiv = "KZ";
    /**
     * 자택_우편_번호
     */
    private String homePostNo;
    /**
     * 자택_주소
     */
    private String homeAddr;
    /**
     * 자택_상세_주소
     */
    private String homeDtlAddr;
    /**
     * 자택_전화_국가_코드
     */
    private String homePhoneCountryCd;
    /**
     * 자택_지역_전화_번호
     */
    private String homeAreaPhoneNo;
    /**
     * 자택_전화_번호
     */
    private String homePhoneNo;
    /**
     * 휴대_전화_번호
     */
    private String cellPhoneNo;
    /**
     * EMAIL_주소
     */
    private String emailAddr;
    /**
     * 직장_명
     */
    private String workNm;
    /**
     * 부서_명
     */
    private String deptNm;
    /**
     * 직위_명
     */
    private String posiNm;
    /**
     * HOMEPAGE_주소
     */
    private String homepageAddr;
    /**
     * 직장_주소_국가_코드
     */
    private String workAddrCountryCd;
    /**
     * 직장_주소_표시_구분
     */
    @Builder.Default
    private String workAddrDisplayDiv = "KZ";
    /**
     * 직장_우편_번호
     */
    private String workPostNo;
    /**
     * 직장_주소
     */
    private String workAddr;
    /**
     * 직장_상세_주소
     */
    private String workDtlAddr;
    /**
     * 직장_전화_국가_코드
     */
    private String workPhoneCountryCd;
    /**
     * 직장_지역_전화_번호
     */
    private String workAreaPhoneNo;
    /**
     * 직장_전화_번호
     */
    private String workPhoneNo;
    /**
     * 직장_FAX_번호
     */
    private String workFaxNo;
    /**
     * 자금_원천_구분
     */
    private String tranFundSourceDiv;
    /**
     * 자금_원천_명
     */
    private String tranFundSourceNm;
    /**
     * 자금_원천_기타
     */
    private String tranFundSourceOther;
    /**
     * 거래_목적_코드
     */
    private String accountNewPurposeCd;
    /**
     * 거래_목적_명
     */
    private String accountNewPurposeNm;
    /**
     * 거래_목적_기타
     */
    private String accountNewPurposeOther;
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


    @Getter
    @RequiredArgsConstructor
    public enum TranFundSourceDiv {
        A03("A03", "사업소득"),
        A99("A99", "기타"),
        ;

        private final String code;
        private final String name;

        public static TranFundSourceDiv mappingFundSource(RemittanceFundSource remittanceFundSource) {
            if (remittanceFundSource == null) {
                return A99;
            }
            return switch (remittanceFundSource) {
                case WORKINCOME, BUSINESSINCOME, INTERESTINCOME -> A03; // A03: 사업소득
                case GIFTINCOME, OWNPROPERTYDISPOAL -> A99; // A99: 기타(명시필수)
            };
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum AccountNewPurposeCd {
        A02("A02", "노후준비"),
        A03("A03", "상속준비"),
        A04("A04", "자녀양육"),
        A11("A11", "급여 및 생활비"),
        A12("A12", "저축 및 투자"),
        A16("A16", "대출금 상환 결제"),
        A99("A99", "기타(명시)"),
        ;

        private final String code;
        private final String name;

        public static AccountNewPurposeCd mappingAccountNewPurposeCd(RemittancePurpose remittancePurpose) {
            if (remittancePurpose == null) {
                return A99;
            }

            return switch (remittancePurpose) {
                case GIFT -> A03; // A03: 상속준비
                case STUDY -> A04; // A04: 자녀양육
                case LIVINGEXPENSES, TRAVELEXPENSES -> A11; // A11: 급여 및 생활비
                case DONATE -> A99; // A99: 기타(명시)
            };
        }
    }

    public static class AMLCustomerIndvCDDBuilder {

        public AMLCustomerIndvCDDBuilder customerNo(String customerNo, String fromCountryCode) {
            this.customerNo = "HK".equalsIgnoreCase(fromCountryCode) ? "CM" + customerNo : fromCountryCode + customerNo;
            return this;
        }

        public AMLCustomerIndvCDDBuilder tranFundSourceDiv(String fundSource) {
            if (StringUtils.isEmpty(fundSource)) {
                this.tranFundSourceDiv = "A99";
                this.tranFundSourceNm = "ETC";
                this.tranFundSourceOther = "기타";
                return this;
            }

            RemittanceFundSource remittanceFundSource = RemittanceFundSource.valueOf(fundSource);
            TranFundSourceDiv tranFundSourceCode = mappingFundSource(remittanceFundSource);
            this.tranFundSourceDiv = tranFundSourceCode.getCode();
            this.tranFundSourceNm = tranFundSourceCode.getName();

            return this;
        }

        public AMLCustomerIndvCDDBuilder accountNewPurposeCd(String fundPurpose) {
            if (StringUtils.isEmpty(fundPurpose)) {
                this.accountNewPurposeCd = "A99";
                this.accountNewPurposeNm = "기타";
                this.accountNewPurposeOther = "기타";
                return this;
            }

            RemittancePurpose remittancePurpose = RemittancePurpose.valueOf(fundPurpose);
            AccountNewPurposeCd accountNewPurposeCd = AccountNewPurposeCd.mappingAccountNewPurposeCd(remittancePurpose);
            this.accountNewPurposeCd = accountNewPurposeCd.getCode();
            this.accountNewPurposeNm = accountNewPurposeCd.getName();

            return this;
        }
    }
}
