package com.cashmallow.api.interfaces.traveler.web.cashout;

import com.cashmallow.common.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import java.math.BigDecimal;
import java.util.List;

import static com.cashmallow.api.interfaces.traveler.web.cashout.CashoutAgencyV2.processTextGuide;

@Service
@RequiredArgsConstructor
@Slf4j
public class RcbcCashoutAgencyService extends AbstCashoutAgency<CashoutAgencyV2> {

    public static final String RCBC = "RCBC";

    private final EnvUtil envUtil;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public List<CashoutAgencyV2> getAgencies(BigDecimal cashoutAmount) {
        return List.of(
                CashoutAgencyV2.of(
                        0L,
                        RCBC,
                        envUtil.getStaticUrl() + getMessage("RCBC_WITHDRAWAL_ACTIVE_IMAGE_URL"),
                        envUtil.getStaticUrl() + getMessage("RCBC_WITHDRAWAL_INACTIVE_IMAGE_URL"),
                        "",
                        "",
                        envUtil.getStaticUrl() + getMessage("RCBC_WITHDRAWAL_ATM_IMAGE_URL"),
                        true,
                        List.of(
                                CashoutAgencyV2.Guide.of(
                                        "RCBC",
                                        "RCBC",
                                        getMessage("RCBC_WITHDRAWAL_GUIDE_DESCRIPTION"),
                                        getMessage("RCBC_WITHDRAWAL_GUIDE_BUTTON_URL"),
                                        getMessage("RCBC_WITHDRAWAL_GUIDE_BUTTON_TEXT"),
                                        processTextGuide(getMessage("RCBC_WITHDRAWAL_GUIDE_TEXT"))
                                )
                        )
                )
        );
    }

    @Override
    protected LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    @Override
    protected String getMessage(String code) {
        return messageSource.getMessage(code, null, "", getCurrentLocale());
    }
}
