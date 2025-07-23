package com.cashmallow.api.interfaces.traveler.dto;

import java.math.BigDecimal;

public record RefundJpRequest(
        String from_cd,
        BigDecimal from_amt,
        String to_cd,
        BigDecimal to_amt,
        BigDecimal fee,
        BigDecimal exchange_rate,
        BigDecimal fee_per_amt,
        BigDecimal fee_rate_amt,
        Long jp_refund_account_info_id,
        Long wallet_id,
        Long remit_id
) {
}
