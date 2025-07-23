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
public class CoatmCashoutAgencyService extends AbstCashoutAgency<CashoutAgencyV2> {

    public static final String COATM = "M002";

    private final EnvUtil envUtil;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public List<CashoutAgencyV2> getAgencies(BigDecimal cashoutAmount) {
        return List.of(
                CashoutAgencyV2.of(
                        0L,
                        COATM,
                        envUtil.getStaticUrl() + "/images/atm/AJ_CIMB_ACTIVE.png",
                        envUtil.getStaticUrl() + "/images/atm/AJ_CIMB_INACTIVE.png",
                        getMessage("COATM_WITHDRAWAL_TITLE"),
                        getMessage("COATM_WITHDRAWAL_DESCRIPTION"),
                        "",
                        true,
                        List.of(
                                CashoutAgencyV2.Guide.of(
                                        "NICEPARK",
                                        getMessage("COATM_WITHDRAWAL_NICEPARK_GUIDE_DESCRIPTION"),
                                        envUtil.getStaticUrl() + getMessage("COATM_WITHDRAWAL_NICEPARK_IMAGE_URL"),
                                        getMessage("COATM_WITHDRAWAL_NICEPARK_BUTTON_URL"),
                                        getMessage("COATM_WITHDRAWAL_NICEPARK_BUTTON_TEXT"),
                                        processTextGuide(getMessage("COATM_WITHDRAWAL_NICEPARK_GUIDE_TEXT"))
                                ),
                                CashoutAgencyV2.Guide.of(
                                        "HANNET",
                                        getMessage("COATM_WITHDRAWAL_HANNET_GUIDE_DESCRIPTION"),
                                        envUtil.getStaticUrl() + getMessage("COATM_WITHDRAWAL_HANNET_IMAGE_URL"),
                                        getMessage("COATM_WITHDRAWAL_HANNET_BUTTON_URL"),
                                        getMessage("COATM_WITHDRAWAL_HANNET_BUTTON_TEXT"),
                                        processTextGuide(getMessage("COATM_WITHDRAWAL_HANNET_GUIDE_TEXT"))
                                ),
                                CashoutAgencyV2.Guide.of(
                                        "HS",
                                        getMessage("COATM_WITHDRAWAL_HS_GUIDE_DESCRIPTION"),
                                        envUtil.getStaticUrl() + getMessage("COATM_WITHDRAWAL_HS_IMAGE_URL"),
                                        getMessage("COATM_WITHDRAWAL_HS_BUTTON_URL"),
                                        getMessage("COATM_WITHDRAWAL_HS_BUTTON_TEXT"),
                                        processTextGuide(getMessage("COATM_WITHDRAWAL_HS_GUIDE_TEXT"))
                                ),
                                CashoutAgencyV2.Guide.of(
                                        "LOTTE",
                                        getMessage("COATM_WITHDRAWAL_LOTTE_GUIDE_DESCRIPTION"),
                                        envUtil.getStaticUrl() + getMessage("COATM_WITHDRAWAL_LOTTE_IMAGE_URL"),
                                        getMessage("COATM_WITHDRAWAL_LOTTE_BUTTON_URL"),
                                        getMessage("COATM_WITHDRAWAL_LOTTE_BUTTON_TEXT"),
                                        processTextGuide(getMessage("COATM_WITHDRAWAL_LOTTE_GUIDE_TEXT"))
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
