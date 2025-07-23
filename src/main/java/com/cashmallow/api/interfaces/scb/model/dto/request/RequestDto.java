package com.cashmallow.api.interfaces.scb.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestDto {
    private long userId;
    private long walletId;
    private long withdrawalPartnerId;
    private Integer withdrawalAgencyId;
    // private BigDecimal travelerCashoutAmt;
    // private String countryCode;
    // private String cashoutReservedDate;
    // private Integer requestTime;

}
