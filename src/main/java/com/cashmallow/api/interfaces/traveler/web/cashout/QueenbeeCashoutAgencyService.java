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
public class QueenbeeCashoutAgencyService extends AbstCashoutAgency<CashoutAgencyV2> {

    public static final String QBC = "M001";

    private final EnvUtil envUtil;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public List<CashoutAgencyV2> getAgencies(BigDecimal cashoutAmount) {
        return List.of(
                CashoutAgencyV2.of(
                        0L,
                        QBC,
                        envUtil.getStaticUrl() + getMessage("QUEENBEE_CASHOUT_TEXT_GUIDE_7VEN_ACTIVE_URL"),
                        envUtil.getStaticUrl() + getMessage("QUEENBEE_CASHOUT_TEXT_GUIDE_7VEN_INACTIVE_URL"),
                        getMessage("QUEENBEE_CASHOUT_TITLE"),
                        "",
                        envUtil.getStaticUrl() + getMessage("QUEENBEE_CASHOUT_ATM_IMAGE_URL"),
                        true,
                        List.of(
                                CashoutAgencyV2.Guide.of(
                                        "SEVEN BANK",
                                        "SEVEN BANK",
                                        getMessage("QUEENBEE_WITHDRAWAL_GUIDE_DESCRIPTION"),
                                        getMessage("QUEENBEE_WITHDRAWAL_GUIDE_BUTTON_URL"),
                                        getMessage("QUEENBEE_WITHDRAWAL_GUIDE_BUTTON_TEXT"),
                                        processTextGuide(getMessage("CASHOUT_TEXT_GUIDE_M001"))
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
