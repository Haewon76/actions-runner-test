package com.cashmallow.api.interfaces.dbs.model.dto;

import com.cashmallow.api.domain.model.company.PaygateRecord;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.dbs.model.enums.DbsRemittanceChargeType;
import com.cashmallow.api.interfaces.dbs.model.enums.DbsRemittancePurpose;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Builder
public class DbsRemittanceRequest {
    private String transactionId;
    private String remittanceType;
    // 보내는사람(Cashmallow)의 은행 지점 코드(HK-HongKong Limited, HB - Hong Kong Branch)
    private String bankCountryCode;
    private String currency; // 받는사람의 커런시 (iso-3자리)
    private BigDecimal amount;
    // CXSALA 급여&복리후생, CXBSNS 일반업무, DDBILL 청구서지불, DDTOPU 계좌충전결제, DDECOM 전자상거래결제, DDOTHR 기타(일반자동이체지불)
    private DbsRemittancePurpose purpose;
    // ChargeType = CRED 받는사람지불, DEBT 보내는사람 지불, SHAR 반반지불
    private DbsRemittanceChargeType chargeType;
    private String debitCurrency; // 빠져나갈 화폐 (iso-3자리)
    private String senderName;
    private String senderAccountNo;
    private String receiverName;
    private String receiverAccountNo;
    private String receiverBankName;
    private String receiverHongKongBankCode;
    private String receiverSwiftBankCode;
    private String receiverBankCountryCode;
    @Valid
    @Size(max = 35)
    private String receiverAddress;
    private String endUserId;

    public static DbsRemittanceRequest of(Traveler traveler, PaygateRecord paygateRecord, BigDecimal amount, Long endUserId, String remittanceType,
                                          String cashmallowName, String cashmallowAccountNo, String hongkongBankCode, String swiftBankCode) {
        DbsRemittanceChargeType chargeType = null;
        if (!Const.ACT.equalsIgnoreCase(remittanceType) && !Const.GPP.equalsIgnoreCase(remittanceType)) {
            chargeType = DbsRemittanceChargeType.DEBT;
        }

        DbsRemittanceRequest returnValue = DbsRemittanceRequest.builder()
                .transactionId(paygateRecord.getId())
                .remittanceType(remittanceType)
                .amount(amount)
                .bankCountryCode("HK") // 우선 HK Limited 고정
                .currency(paygateRecord.getIso4217())
                .purpose(DbsRemittancePurpose.CXBSNS)
                .chargeType(chargeType)
                .debitCurrency(paygateRecord.getIso4217())
                .senderName(cashmallowName)
                .senderAccountNo(cashmallowAccountNo)
                .receiverName(traveler.getAccountName())
                .receiverAccountNo(traveler.getAccountNo())
                .receiverBankName(traveler.getBankName())
                .receiverHongKongBankCode(hongkongBankCode)
                .receiverBankCountryCode("HK")
                .receiverSwiftBankCode(swiftBankCode)
                .receiverAddress(getLimitedAddress(traveler))
                .endUserId(String.valueOf(endUserId))
                .build();

        return returnValue;
    }

    private static String getLimitedAddress(Traveler traveler) {
        if(traveler.getAddress().length() > 35) {
            return traveler.getAddressCity() + ", " + traveler.getAddressCountry();
        }
        return traveler.getAddress();
    }

}
