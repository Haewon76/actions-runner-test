package com.cashmallow.api.interfaces.terms;

import com.cashmallow.api.domain.model.terms.TermsHistory;
import com.cashmallow.api.domain.model.terms.TermsHistoryMapper;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.common.EnvUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class TermsHistoryServiceImplTest {

    @Autowired
    MessageSource messageSource;
    @Autowired
    TermsHistoryService termsHistoryService;

    @Autowired
    TermsHistoryMapper termsHistoryMapper;

    @Autowired
    private EnvUtil envUtil;

    final String TERMS_OLD_PATH = "/html/privacy-terms/traveler/terms-of-service/hk/terms_of_service_v123456789.html";
    final String TERMS_NEW_PATH = "/html/privacy-terms/traveler/terms-of-service/hk/terms_of_service_v1234567890.html";
    final String PRIVACY_PATH = "/html/privacy-terms/traveler/privacy-policy/hk/privacy_policy_v1234567890.html";
    final String TERMS_OPENBANK_PATH = "/html/privacy-terms/traveler/terms-of-openbank/kr/terms_of_openbank_v1234567890.html";
    final String CLAIMS_PATH = "/html/privacy-terms/traveler/claims/kr/claims_v1234567890.html";

    @BeforeEach
    void init() {
        TermsHistory termsNew = new TermsHistory();
        termsNew.setVersion(1234567890);
        termsNew.setType(TermsType.TERMS);
        termsNew.setCountryCode("003");
        termsNew.setPath(TERMS_NEW_PATH);
        termsNew.setRequired(true);
        termsNew.setRequiredReAgreement(true);
        termsNew.setShowSignup(true);
        termsNew.setAnnouncedAt(Timestamp.valueOf(LocalDateTime.of(1999, 1, 1, 1, 1, 1)));
        termsNew.setStartedAt(Timestamp.valueOf(LocalDateTime.of(2000, 1, 1, 1, 1, 1)));

        TermsHistory termsOld = new TermsHistory();
        BeanUtils.copyProperties(termsNew, termsOld);
        termsOld.setVersion(123456789);
        termsOld.setPath(TERMS_OLD_PATH);

        TermsHistory privacy = new TermsHistory();
        BeanUtils.copyProperties(termsNew, privacy);
        privacy.setType(TermsType.PRIVACY);
        privacy.setPath(PRIVACY_PATH);

        TermsHistory termsOpenbank = new TermsHistory();
        BeanUtils.copyProperties(termsNew, termsOpenbank);
        termsOpenbank.setType(TermsType.TERMS_OPENBANK);
        termsOpenbank.setPath(TERMS_OPENBANK_PATH);

        TermsHistory claims = new TermsHistory();
        BeanUtils.copyProperties(termsNew, claims);
        claims.setType(TermsType.CLAIMS);
        claims.setPath(CLAIMS_PATH);
        claims.setShowSignup(false);

        termsHistoryMapper.insertTermsHistory(termsOld);
        termsHistoryMapper.insertTermsHistory(termsNew);
        termsHistoryMapper.insertTermsHistory(privacy);
        termsHistoryMapper.insertTermsHistory(termsOpenbank);
        termsHistoryMapper.insertTermsHistory(claims);
    }

    @Test
    void getRecentVersionHistoryPath_호출_TYPE_TERMS_정상값_반환() {
        // given
        TermsType type = TermsType.TERMS;
        String iso3166 = "KR";

        // when
        String result = termsHistoryService.getRecentVersionHistoryPath(type, iso3166);

        // then
        assertEquals(TERMS_NEW_PATH, result);
    }

    @Test
    void getRecentVersionHistoryPath_호출_TYPE_PRIVACY_정상값_반환() {
        // given
        TermsType type = TermsType.PRIVACY;
        String iso3166 = "KR";

        // when
        String result = termsHistoryService.getRecentVersionHistoryPath(type, iso3166);

        // then
        assertEquals(PRIVACY_PATH, result);
    }

    @Test
    void getRecentVersionHistoryPath_호출_TYPE_TERMS_OPENBANK_정상값_반환() {
        // given
        TermsType type = TermsType.TERMS_OPENBANK;
        String iso3166 = "KR";

        // when
        String result = termsHistoryService.getRecentVersionHistoryPath(type, iso3166);

        // then
        assertEquals(TERMS_OPENBANK_PATH, result);
    }

    @Test
    void getRecentVersionHistoryPath_호출_TYPE_CLAIMS_정상값_반환() {
        // given
        TermsType type = TermsType.CLAIMS;
        String iso3166 = "KR";

        // when
        String result = termsHistoryService.getRecentVersionHistoryPath(type, iso3166);

        // then
        assertEquals(CLAIMS_PATH, result);
    }

    @Test
    void getRecentVersionHistories_호출_정상값_반환_showSignup_null() {
        // given
        String countryCode = "003";
        Boolean showSignup = null;
        Integer version = 1234567890;
        Boolean required = true;
        LocalDateTime testTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);
        String[] pathStrings = new String[]{TERMS_NEW_PATH, PRIVACY_PATH, TERMS_OPENBANK_PATH, CLAIMS_PATH};

        // when
        List<TermsHistory> result = termsHistoryService.getRecentVersionHistories(countryCode, showSignup);
        result.sort(Comparator.comparingInt(o -> o.getType().ordinal()));

        // then
        assertEquals(4, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(pathStrings[i], result.get(i).getPath());
        }

        for (TermsHistory termsHistory : result) {
            assertEquals(version, termsHistory.getVersion());
            assertEquals(countryCode, termsHistory.getCountryCode());
            assertEquals(testTime, termsHistory.getStartedAt().toLocalDateTime());
            assertEquals(required, termsHistory.getRequired());
        }
    }

    @Test
    void getRecentVersionHistories_호출_정상값_반환_showSignup_true() {
        // given
        String countryCode = "003";
        Boolean showSignup = true;
        Integer version = 1234567890;
        Boolean required = true;
        LocalDateTime testTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);
        String[] pathStrings = new String[]{TERMS_NEW_PATH, PRIVACY_PATH, TERMS_OPENBANK_PATH};

        // when
        List<TermsHistory> result = termsHistoryService.getRecentVersionHistories(countryCode, showSignup);
        result.sort(Comparator.comparingInt(o -> o.getType().ordinal()));

        // then
        assertEquals(3, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(pathStrings[i], result.get(i).getPath());
        }

        for (TermsHistory termsHistory : result) {
            assertEquals(version, termsHistory.getVersion());
            assertEquals(countryCode, termsHistory.getCountryCode());
            assertEquals(testTime, termsHistory.getStartedAt().toLocalDateTime());
            assertEquals(required, termsHistory.getRequired());
        }
    }


    @Test
    void getRecentVersionHistoriesByLocale_호출_country_code_미존재() {
        // given
        String countryCode = "999";
        Boolean showSignup = false;
        Locale locale = Locale.ENGLISH;

        // when
        List<TermsHistoryVO> result = termsHistoryService.getRecentVersionHistories(countryCode, showSignup, locale);

        assertEquals(0, result.size());
    }
}
