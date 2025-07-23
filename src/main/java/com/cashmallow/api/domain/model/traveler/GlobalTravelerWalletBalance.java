package com.cashmallow.api.domain.model.traveler;

import java.math.BigDecimal;

public record GlobalTravelerWalletBalance(
        String country,
        BigDecimal totalBalance
) {
}
