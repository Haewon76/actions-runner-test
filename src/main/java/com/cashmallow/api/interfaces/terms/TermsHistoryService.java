package com.cashmallow.api.interfaces.terms;

import com.cashmallow.api.domain.model.terms.TermsHistory;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;

import java.util.List;
import java.util.Locale;

public interface TermsHistoryService {

    List<TermsHistory> getRecentVersionHistories(String countryCode);

    List<TermsHistory> getRecentVersionHistories(String countryCode, Boolean showSignup);

    List<TermsHistoryVO> getRecentVersionHistories(String countryCode, Boolean showSignup, Locale locale);

    String getRecentVersionHistoryPath(TermsType type, String iso3166);
}
