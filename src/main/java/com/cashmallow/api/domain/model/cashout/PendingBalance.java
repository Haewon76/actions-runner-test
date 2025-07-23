package com.cashmallow.api.domain.model.cashout;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class PendingBalance {
    private String fromCountry;
    private String fromCountryKorName;
    private String fromAmount;
    private String toCurrency;
    private String toAmount;
    private String r;
    private String e;
    private String c;

    public PendingBalance(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public PendingBalance(List<PendingBalance> pendingBalances) {
        this.toCurrency = "원금";

        this.toAmount = pendingBalances.stream()
                .filter(p -> p.fromAmount != null)
                .map(PendingBalance::getFromAmount)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add).toString();
    }


    public String getFromAmount() {
        return getNumber(fromAmount);
    }

    public String getToAmount() {
        return getNumber(toAmount);
    }

    public String getR() {
        return getNumber(r);
    }

    public String getE() {
        return getNumber(e);
    }

    public String getC() {
        return getNumber(c);
    }


    private String getNumber(String number) {
        return StringUtils.isEmpty(number) ? "0" : number;
    }

}
