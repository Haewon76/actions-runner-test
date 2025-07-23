package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CashmallowFxQuotationApproveRequest {

    private String rateUid; // UUID

    private String transactionId; // transactionId 처음 환전요청시에 거래번호 동일하게 세팅
    private String senderAccountNo;
    private String receiverAccountNo;
    private String currencyPair;
    private String endUserId;

    // private String bankCountryCode = "HK"; - dbs처리
    // private String remittanceType = "ACT/TT" - dbs처리;
    // private String senderName; - dbs처리
    private String receiverName; // SCB일때만 사용, 임시
    private String receiverSwiftBankCode; // SCB일때만 사용, 임시
    private String receiverBankCountryCode; // SCB일때만 사용, 임시
    private String receiverAddress; // SCB일때만 사용, 임시

}
