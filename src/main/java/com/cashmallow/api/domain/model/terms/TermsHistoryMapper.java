package com.cashmallow.api.domain.model.terms;

import java.util.List;

public interface TermsHistoryMapper {
    List<TermsHistory> getRecentVersionHistories(String countryCode);

    List<TermsHistory> getRecentVersionHistoriesByShowSignup(String countryCode, Boolean showSignup);

    String getRecentVersionHistoryPath(TermsType type, String iso3166);

    int insertTermsHistory(TermsHistory termsHistory);
}
