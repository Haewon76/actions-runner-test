package com.cashmallow.api.interfaces.traveler.web.cashout;

import com.cashmallow.common.CommonUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

abstract class AbstCashoutAgency<T extends CashAgency> {

    protected abstract List<T> getAgencies(BigDecimal cashoutAmount);

    protected T getAgency(int agencyId,
                                      BigDecimal cashoutAmount) {
        return getAgencies(cashoutAmount).stream()
                .filter(agency -> agency.id() == agencyId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Agency not found"));
    }

    @NotNull
    protected Locale getCurrentLocale() {
        Locale locale = Locale.US;
        HttpServletRequest httpServletRequest = CommonUtil.getHttpServletRequest();
        if (httpServletRequest != null) {
            locale = getLocaleResolver().resolveLocale(httpServletRequest);
        }
        return locale;
    }

    protected String getManualHtml(String type) {
        return null;
    }

    protected abstract LocaleResolver getLocaleResolver();
    protected abstract String getMessage(String key);
}
