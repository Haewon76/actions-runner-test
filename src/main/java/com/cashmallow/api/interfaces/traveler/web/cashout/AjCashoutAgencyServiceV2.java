package com.cashmallow.api.interfaces.traveler.web.cashout;

import com.cashmallow.common.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.cashmallow.api.interfaces.traveler.web.cashout.CashoutAgencyV2.processTextGuide;

@Service
@RequiredArgsConstructor
@Slf4j
public class AjCashoutAgencyServiceV2 extends AbstCashoutAgency<CashoutAgencyV2> {

    public static final String BNI = "BNI";
    public static final String ATMP = "ATM+";

    private final EnvUtil envUtil;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public List<CashoutAgencyV2> getAgencies(BigDecimal cashoutAmount) {
        // https://docs.google.com/presentation/d/1CgjDKxqL8awTpaceG1RzeJu5S2th-lyW2A2Wkrb184g/edit#slide=id.g2edcca33a19_2_0
        // 130~250만 : 4번문구 표시 - Withdrawal at the ATM available for 100,000
        // 10~120만 : 7번 문구 표시 - Withdrawal at the ATM available for 50,000 or 100,000
        int index = cashoutAmount.compareTo(BigDecimal.valueOf(120000)) <= 0 ? 2 : 1;
        Locale locale = getCurrentLocale();
        return List.of(
                CashoutAgencyV2.of(
                        0L,
                        ATMP,
                        envUtil.getStaticUrl() + getMessage("CASHOUT_TEXT_GUIDE_AJ_ATMP_ACTIVE_URL"),
                        envUtil.getStaticUrl() + getMessage("CASHOUT_TEXT_GUIDE_AJ_ATMP_INACTIVE_URL"),
                        "",
                        "",
                        envUtil.getStaticUrl() + getMessage("CASHOUT_TEXT_GUIDE_AJ_ATM_IMAGE_URL"),
                        true,
                        List.of(
                                CashoutAgencyV2.Guide.of(
                                        "ATM+",
                                        "ATM+",
                                        getMessage("CASHOUT_TEXT_GUIDE_AJ_ATMP_DESCRIPTION" + index),
                                        getMessage("AJ_WITHDRAWAL_ATMP_GUIDE_BUTTON_URL"),
                                        getMessage("AJ_WITHDRAWAL_GUIDE_BUTTON_TEXT"),
                                        processTextGuide(getMessage("CASHOUT_TEXT_GUIDE_AJ_ATMP"))
                                )
                        )
                ),
                CashoutAgencyV2.of(
                        1L,
                        BNI,
                        envUtil.getStaticUrl() + getMessage("CASHOUT_TEXT_GUIDE_AJ_BNI_ACTIVE_URL"),
                        envUtil.getStaticUrl() + getMessage("CASHOUT_TEXT_GUIDE_AJ_BNI_INACTIVE_URL"),
                        "",
                        "",
                        envUtil.getStaticUrl() + getMessage("CASHOUT_TEXT_GUIDE_AJ_ATM_IMAGE_URL"),
                        !envUtil.isPrd() && new Random().nextBoolean(),
                        List.of(
                                CashoutAgencyV2.Guide.of(
                                        "BNI BANK",
                                        "BNI BANK",
                                        getMessage("CASHOUT_TEXT_GUIDE_AJ_BNI_DESCRIPTION" + index),
                                        getMessage("AJ_WITHDRAWAL_BNI_GUIDE_BUTTON_URL"),
                                        getMessage("AJ_WITHDRAWAL_GUIDE_BUTTON_TEXT"),
                                        processTextGuide(getMessage("CASHOUT_TEXT_GUIDE_AJ_BNI"))
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
