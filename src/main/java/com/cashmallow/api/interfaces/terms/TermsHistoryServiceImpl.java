package com.cashmallow.api.interfaces.terms;

import com.cashmallow.api.domain.model.terms.TermsHistory;
import com.cashmallow.api.domain.model.terms.TermsHistoryMapper;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.common.DateUtil;
import com.cashmallow.common.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsHistoryServiceImpl implements TermsHistoryService {

    @Autowired
    private EnvUtil envUtil;

    private final TermsHistoryMapper termsHistoryMapper;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    @Override
    public List<TermsHistory> getRecentVersionHistories(String countryCode) {
        return termsHistoryMapper.getRecentVersionHistories(countryCode);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TermsHistory> getRecentVersionHistories(String countryCode, Boolean showSignup) {
        return termsHistoryMapper.getRecentVersionHistoriesByShowSignup(countryCode, showSignup);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TermsHistoryVO> getRecentVersionHistories(String countryCode, Boolean showSignup, Locale locale) {
        List<TermsHistory> recentVersionHistories = termsHistoryMapper.getRecentVersionHistoriesByShowSignup(countryCode, showSignup);
        recentVersionHistories.sort(Comparator.comparingInt(o -> o.getType().ordinal()));

        return recentVersionHistories.stream().map(termsHistory -> new TermsHistoryVO(termsHistory.getType(),
                messageSource.getMessage(termsHistory.getType().getViewTitle(countryCode), null, null, locale),
                envUtil.getStaticUrl().concat(termsHistory.getPath()),
                termsHistory.getVersion(),
                termsHistory.getRequired(),
                DateUtil.getTimestampToLocalDate(countryCode, termsHistory.getStartedAt()))
        ).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public String getRecentVersionHistoryPath(TermsType type, String iso3166) {
        return termsHistoryMapper.getRecentVersionHistoryPath(type, iso3166);
    }
}
