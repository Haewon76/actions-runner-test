package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.interfaces.mallowlink.remittance.enums.MallowlinkRemittanceFundSource;
import com.cashmallow.api.interfaces.mallowlink.remittance.enums.MallowlinkRemittancePurpose;
import com.cashmallow.common.CommonUtil;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;


public record RemittanceRequestV3(

        @NotBlank
        String clientTransactionId,
        @NotBlank
        String clientEndUserId,

        CountryCode countryCode,
        String currency,

        @DecimalMin(value = "0", inclusive = false)
        BigDecimal amount,

        // 옵셔널 (Nabilbank에서만 사용)
        BigDecimal fromAmount,

        MallowlinkRemittancePurpose purpose, // 송금 목적

        MallowlinkRemittanceFundSource fundSource, // 자금 출처

        @NotNull
        RemittanceReceiverV3 receiver,

        String ip,

        IdData endUserIdData,
        Address endUserAddress,
        Address receiverAddress,

        // String bsbCode,         // AU
        // String transitCode,     // CA
        // String sortCode,        // GB

        @NotNull
        ZonedDateTime requestTime

) {

    public static RemittanceRequestV3 of(String transactionId, Remittance remittance, Traveler traveler, RemittanceTravelerSnapshot snapshot) {

        String currency = CountryInfo.valueOf(remittance.getToCountry().name()).getCurrency();

        RemittanceReceiverV3 remittanceReceiver = RemittanceReceiverV3.of(remittance);

        IdData endUserIdData = null;
        Address endUserAddress = null;
        Address receiverAddress = null;

        boolean isFromJpRemittance = CountryCode.JP.getCode().equals(remittance.getFromCd());

        switch (remittance.getToCountry()) {
            case KR, JP, HK -> {
                // nothing to do
            }
            case SG, MY, PH, TH, GB, VN, NP, IN, CN,
                 IE, DK, CY, EE, FI, PT, LU, BE, BG, MC, SE, PL, AT, IT, NL, LT, FR, DE, LV, MT, ES, BD, MN -> {
                endUserIdData = IdData.of(traveler);
                endUserAddress = Address.ofEnduser(traveler, snapshot, isFromJpRemittance);
            }
            case AU, ID, CA, US -> {
                endUserIdData = IdData.of(traveler);
                endUserAddress = Address.ofEnduser(traveler, snapshot, isFromJpRemittance);
                receiverAddress = Address.ofReceiver(remittance);
            }
            default -> throw new IllegalStateException("Unexpected value: " + remittance.getToCountry());
        }

        return new RemittanceRequestV3(
                transactionId,
                traveler.getUserId().toString(),
                remittance.getToCountry(),
                currency,
                remittance.getToAmt(),
                remittance.getFromAmt(),
                MallowlinkRemittancePurpose.of(remittance.getRemitPurpose().name()),
                MallowlinkRemittanceFundSource.of(remittance.getRemitFundSource().name()),
                remittanceReceiver,
                CommonUtil.getRequestIp(),
                endUserIdData,
                endUserAddress,
                receiverAddress,
                ZonedDateTime.now()
        );
    }
}
