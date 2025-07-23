package com.cashmallow.api.domain.model.aml;

import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.cashmallow.api.domain.model.aml.AmlAccountBase.RltOpenPurposeCd;
import static com.cashmallow.api.domain.model.aml.AmlAccountBase.RltOpenPurposeCd.mappingPurpose;

@Builder
@Getter
public class AmlAccountTranSendReceipt {
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
     * 취급하는 상품코드
     * 01 PG , 21:기타(결재)
     */
    private String prodCd;

    /**
     * 거래가 발생한 일자
     * YYYYMMDD
     */
    private String tranDd;

    /**
     * 거래가 발생한 일자의 유니크한 일련번호
     */
    private BigDecimal tranSeqNo;

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

    // 계약 송수취
    /**
     * 당타행 구분
     * 1:(타발)당행,  2:(당발)타행
     */
    @Builder.Default
    private String bankOtherbankDiv = "1";

    /**
     * 파트너 구분(송금)
     * 01:계좌번호,02:계좌주명,03:연락처
     */
    @Builder.Default
    private String repayDivisionCd = "01";


    /**
     * 상대_은행_국가_코드
     * 수취인
     */
    private String rltFinanOrgCountryCd;

    /**
     * 상대_은행_코드
     * 수취인
     */
    private String rltFinanOrgCd;

    /**
     * 상대_은행명
     * 수취인
     */
    private String rltFinanOrgNm;

    /**
     * 거래 상대방 계좌번호
     * Bank transfer: 계좌번호
     * Cash pickup: 전화번호(콜링코드를 제외한 번호)
     * Cash card: 카드번호
     * Mobile wallet: 모바일월렛 번호
     * *이외 송금방식은 수취의 기준이 되는 숫자열을 기준으로 이행
     * *당발타행 : 수취자 정보
     */
    private String rltaccNo;

    /**
     * 상대_계좌주_구분
     * 01 개인, 02 법인
     */
    private String rltaccOwnerDiv;

    /**
     * 상대_계좌주_구분명
     * 개인/법인
     */
    private String rltaccOwnerDivNm;

    /**
     * 상대_계좌소유자_국가
     * 국가코드,
     */
    private String rltaccOwnerCountryCd;

    /**
     * 상대_계좌주명
     */
    private String rltaccOwnerNm;

    /**
     * 상대_계좌소유자_연락처
     */
    private String rltaccOwnerPhoneNo;

    /**
     * 거래의 목적코드
     * 01:물품/사업상 대금결재
     * 02:차입/부채상환
     * 03:상속증여성 거래
     * 99:기타(기재)
     */
    private String rltOpenPurposeCd;

    /**
     * 거래의 목적코드명
     */
    private String rltOpenPurposeNm;

    public static class AmlAccountTranSendReceiptBuilder {
        public AmlAccountTranSendReceiptBuilder tranDd(Timestamp tranDd) {
            if (tranDd == null) {
                return this;
            }

            DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDateTime localDateTime = tranDd.toLocalDateTime();
            ZoneId zoneId = ZoneId.of("Asia/Seoul"); // KST
            LocalDateTime kstDateTime = localDateTime.atZone(zoneId).toLocalDateTime();
            this.tranDd = kstDateTime.format(yyyyMMdd); // UTC -> KST
            return this;
        }

        public AmlAccountTranSendReceiptBuilder tranSeqNo(long tranSeqNo) {
            this.tranSeqNo = BigDecimal.valueOf(tranSeqNo);
            return this;
        }

        public AmlAccountTranSendReceiptBuilder rltOpenPurposeCd(RltOpenPurposeCd purposeCd) {
            this.rltOpenPurposeCd = purposeCd.getCode();
            this.rltOpenPurposeNm = purposeCd.getName();
            return this;
        }

        public AmlAccountTranSendReceiptBuilder rltOpenPurposeCd(RemittancePurpose purposeCd) {
            RltOpenPurposeCd e = mappingPurpose(purposeCd);
            this.rltOpenPurposeCd = e.getCode();
            this.rltOpenPurposeNm = e.getName();
            return this;
        }
    }
}
