package com.cashmallow.api.domain.model.aml;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AmlProdBase {
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

    // 계약 상품
    /**
     * 상품명
     */
    @Builder.Default
    private String prodNm = "해외송금";

}
