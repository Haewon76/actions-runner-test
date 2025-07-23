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
public class ScbCashoutAgencyService extends AbstCashoutAgency<CashoutAgencyV2> {

    public static final String SCB = "SCB";

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final EnvUtil envUtil;

    @Override
    public List<CashoutAgencyV2> getAgencies(BigDecimal cashoutAmount) {
        return List.of(
                CashoutAgencyV2.of(
                        0L,
                        SCB,
                        "",
                        "",
                        "",
                        messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION", null, "", getCurrentLocale()),
                        envUtil.getStaticUrl() + getMessage("SCB_WITHDRAWAL_ATM_IMAGE_URL"),
                        true,
                        List.of(
                                CashoutAgencyV2.Guide.of(
                                        "ATM",
                                        getMessage("SCB_WITHDRAWAL_GUIDE_TITLE_ATMS"),
                                        "",
                                        getMessage("SCB_WITHDRAWAL_ATM_URL"),
                                        getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_LINK"),
                                        processTextGuide(getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_ATMS"))
                                ),
                                CashoutAgencyV2.Guide.of(
                                        "CDM/ATM",
                                        getMessage("SCB_WITHDRAWAL_GUIDE_TITLE_CDMS"),
                                        "",
                                        getMessage("SCB_WITHDRAWAL_CDM_URL"),
                                        getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_LINK"),
                                        processTextGuide(getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_CDMS"))
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
