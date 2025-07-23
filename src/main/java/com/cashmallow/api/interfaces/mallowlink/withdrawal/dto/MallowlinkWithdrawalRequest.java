package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public final class MallowlinkWithdrawalRequest {

    @NotBlank
    private final String transactionId;
    @NotBlank
    private final String userId;

    private final CountryCode country;

    private final String currency;

    @NotBlank
    private final BigDecimal amount;

    // 옵셔널 (Nabilbank에서만 사용)
    private final BigDecimal fromAmount;

    private final String ip;

    private final ZonedDateTime requestTime;

    private final int partnerId;
    private final int agencyId;

    public static MallowlinkWithdrawalRequest of(String transactionId,
                                                 Traveler traveler,
                                                 TravelerWallet wallet,
                                                 BigDecimal amount,
                                                 BigDecimal fromAmount,
                                                 String ip,
                                                 int partnerId,
                                                 int agencyId) {
        final CountryCode countryCode = wallet.getToCountry();

        return new MallowlinkWithdrawalRequest(
                transactionId,
                traveler.getUserId().toString(),
                countryCode,
                countryCode.getCurrency(),
                amount,
                fromAmount,
                ip,
                ZonedDateTime.now(),
                partnerId,
                agencyId
        );
    }
}
