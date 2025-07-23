package com.cashmallow.api.application;

import com.cashmallow.api.application.impl.WebhookServiceImpl;
import com.cashmallow.api.domain.model.terms.TermsHistory;
import com.cashmallow.api.domain.model.terms.TermsHistoryMapper;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.domain.model.user.*;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.api.interfaces.user.dto.UserSearchRequest;
import com.cashmallow.common.EnvUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.cashmallow.api.domain.model.terms.TermsType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    MessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    UserAgreeHistoryMapper userAgreeHistoryMapper;

    @Autowired
    TermsHistoryMapper termsHistoryMapper;

    @Autowired
    WebhookServiceImpl webhookService;

    @Autowired
    private EnvUtil envUtil;

    final String LOGIN_HK = "testtestHK";
    final String LOGIN_KR = "testtestKR";

    final String TERMS_OLD_PATH = "/html/privacy-terms/traveler/terms-of-service/hk/terms_of_service_v123456789.html";
    final String TERMS_NEW_PATH = "/html/privacy-terms/traveler/terms-of-service/hk/terms_of_service_v1234567890.html";
    final String PRIVACY_PATH = "/html/privacy-terms/traveler/privacy-policy/hk/privacy_policy_v1234567890.html";
    final String TERMS_OPENBANK_PATH = "/html/privacy-terms/traveler/terms-of-openbank/kr/terms_of_openbank_v1234567890.html";
    final String CLAIMS_PATH = "/html/privacy-terms/traveler/claims/kr/claims_v1234567890.html";

    @BeforeEach
    void init() {
        User userHk = new User();
        userHk.setLogin(LOGIN_HK);
        userHk.setPasswordHash("testtest");
        userHk.setFirstName("first");
        userHk.setLastName("last");
        userHk.setEmail("email");
        userHk.setAllowRecvEmail("Y");
        userHk.setLangKey("test");
        userHk.setBirthDate("22220101");
        userHk.setCls("X");
        userHk.setCountry("001");
        userHk.setAgreeTerms("N");
        userHk.setAgreePrivacy("N");
        userHk.setLangKey("en");

        User userKr = new User();
        BeanUtils.copyProperties(userHk, userKr);
        userKr.setLogin(LOGIN_KR);
        userKr.setCountry("003");
        userKr.setLangKey("ko");

        userMapper.insertUser(userHk);
        userMapper.insertUser(userKr);

        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Integer version = 1234567890;

        UserAgreeHistory userAgreeHistory1 = new UserAgreeHistory();
        UserAgreeHistory userAgreeHistory2 = new UserAgreeHistory();
        UserAgreeHistory userAgreeHistory3 = new UserAgreeHistory();

        userAgreeHistory1.setUserId(user.getId());
        userAgreeHistory1.setVersion(version);
        userAgreeHistory1.setTermsType(TERMS);

        userAgreeHistory2.setUserId(user.getId());
        userAgreeHistory2.setVersion(version);
        userAgreeHistory2.setTermsType(TermsType.PRIVACY);

        userAgreeHistory3.setUserId(user.getId());
        userAgreeHistory3.setVersion(version);
        userAgreeHistory3.setTermsType(TermsType.TERMS_OPENBANK);

        List<UserAgreeHistory> userAgreeHistoryList = new ArrayList<>();
        userAgreeHistoryList.add(userAgreeHistory1);
        userAgreeHistoryList.add(userAgreeHistory2);
        userAgreeHistoryList.add(userAgreeHistory3);

        userAgreeHistoryMapper.insertUserAgreeHistory(userAgreeHistoryList);

        TermsHistory termsNew = new TermsHistory();
        termsNew.setVersion(version);
        termsNew.setType(TERMS);
        termsNew.setCountryCode("003");
        termsNew.setPath(TERMS_NEW_PATH);
        termsNew.setRequired(true);
        termsNew.setRequiredReAgreement(true);
        termsNew.setShowSignup(true);
        termsNew.setAnnouncedAt(Timestamp.valueOf(LocalDateTime.of(1999, 1, 1, 1, 1, 1)));
        termsNew.setStartedAt(Timestamp.valueOf(LocalDateTime.of(2000, 1, 1, 1, 1, 1)));

        TermsHistory termsOld = new TermsHistory();
        BeanUtils.copyProperties(termsNew, termsOld);
        termsOld.setVersion(version - 1);
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
        claims.setRequired(false);

        termsHistoryMapper.insertTermsHistory(termsOld);
        termsHistoryMapper.insertTermsHistory(termsNew);
        termsHistoryMapper.insertTermsHistory(privacy);
        termsHistoryMapper.insertTermsHistory(termsOpenbank);
        termsHistoryMapper.insertTermsHistory(claims);
    }

    /**
     * 실패 케이스 - DB 에 lang_key가 없음 or utf 인 경우 발생하여 db 수정함
     * 향후 유효하지 않은 경우 문제를 본 테스트로 발경 하기를 희망
     * stg 유저ID: 1309,langKey: utf ==> expected: <true> but was: <false>
     */
    @Test
    @DisplayName("user lang_key 유효성 검증")
    void getLangKey() {
        // Given
        List<Long> userIds = userService.getOrderByDescNewUserIds(100);

        // When
        Long userId = 1L;
        String langKey = userService.getLangKey(userId);

        // Then
        List<String> langKeyList = List.of(
                "ko",
                "zh",
                "en",
                "ja",
                "fr",
                "vi",
                "th",
                "de"
        );
        assertTrue(langKeyList.contains(langKey));
        for (Long uid : userIds) {
            langKey = userService.getLangKey(uid);
            assertTrue(langKeyList.contains(langKey), String.format("유저ID: %s,langKey: %s", uid, langKey));
        }
    }

    @Test
    void 회원가입수_조회() {
        // given

        // when
        String startDate = "2023-02-05";
        String endDate = "2023-03-09";
        int countNewUsers = userService.getCountNewUsers(startDate, endDate);

        // then


    }

    @Test
    void 국가별_가입수_인증수_조회() {
        // given
        String startDate = "2025-05-01";
        String endDate = "2025-05-14";

        // when
        String result = webhookService.getCountNewUsers(startDate, endDate);

        // then
        System.out.println(result);
    }

    @Test
    void getUnreadTermsList_호출_빈값_리턴() {
        // given
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        String countryCode = "003";
        Locale locale = Locale.ENGLISH;

        // when
        List<TermsHistoryVO> result = userService.getUnreadTermsList(user.getId(), countryCode, locale);

        // then
        assertEquals(0, result.size());
    }

    @Test
    void getTermsReAgreement_호출_Unread_terms_재인증() {
        // given
        Integer version = 1234567890;
        String countryCode = "003";
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Locale locale = Locale.ENGLISH;
        List<UserAgreeHistory> userAgreeHistoryList = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());

        for (UserAgreeHistory userAgreeHistory : userAgreeHistoryList) {
            if (TERMS.equals(userAgreeHistory.getTermsType())) {
                userAgreeHistoryMapper.deleteUserAgreeHistory(userAgreeHistory.getId());
            }
        }

        // when
        List<TermsHistoryVO> result = userService.getUnreadTermsList(user.getId(), countryCode, locale);

        // then
        assertEquals(1, result.size());
        assertEquals(TERMS, result.get(0).getType());
        assertEquals(version, result.get(0).getVersion());
        assertEquals(messageSource.getMessage(TERMS.getViewTitle(), null, locale), result.get(0).getTitle());
        assertEquals(envUtil.getStaticUrl() + TERMS_NEW_PATH, result.get(0).getUrl());
    }

    @Test
    void getUnreadTermsList_호출_privacy_재인증() {
        // given
        Integer version = 1234567890;
        String countryCode = "003";
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Locale locale = Locale.ENGLISH;
        List<UserAgreeHistory> userAgreeHistoryList = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());

        for (UserAgreeHistory userAgreeHistory : userAgreeHistoryList) {
            if (TermsType.PRIVACY.equals(userAgreeHistory.getTermsType())) {
                userAgreeHistoryMapper.deleteUserAgreeHistory(userAgreeHistory.getId());
            }
        }

        // when
        List<TermsHistoryVO> result = userService.getUnreadTermsList(user.getId(), countryCode, locale);

        // then
        assertEquals(1, result.size());
        assertEquals(PRIVACY, result.get(0).getType());
        assertEquals(version, result.get(0).getVersion());
        assertEquals(messageSource.getMessage(PRIVACY.getViewTitle(), null, locale), result.get(0).getTitle());
        assertEquals(envUtil.getStaticUrl() + PRIVACY_PATH, result.get(0).getUrl());
    }

    @Test
    void getTermsReAgreement_호출_Unread_terms_openbank_재인증() {
        // given
        Integer version = 1234567890;
        String countryCode = "003";
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Locale locale = Locale.ENGLISH;
        List<UserAgreeHistory> userAgreeHistoryList = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());

        for (UserAgreeHistory userAgreeHistory : userAgreeHistoryList) {
            if (TermsType.TERMS_OPENBANK.equals(userAgreeHistory.getTermsType())) {
                userAgreeHistoryMapper.deleteUserAgreeHistory(userAgreeHistory.getId());
            }
        }

        // when
        List<TermsHistoryVO> result = userService.getUnreadTermsList(user.getId(), countryCode, locale);

        // then
        assertEquals(1, result.size());
        assertEquals(TERMS_OPENBANK, result.get(0).getType());
        assertEquals(version, result.get(0).getVersion());
        assertEquals(messageSource.getMessage(TERMS_OPENBANK.getViewTitle(), null, locale), result.get(0).getTitle());
        assertEquals(envUtil.getStaticUrl() + TERMS_OPENBANK_PATH, result.get(0).getUrl());
    }

    @Test
    void getTermsReAgreement_호출_terms_and_Unread_terms_openbank_재인증() {
        // given
        Integer version = 1234567890;
        String countryCode = "003";
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Locale locale = Locale.ENGLISH;
        List<UserAgreeHistory> userAgreeHistoryList = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());

        for (UserAgreeHistory userAgreeHistory : userAgreeHistoryList) {
            if (TERMS.equals(userAgreeHistory.getTermsType()) ||
                    TermsType.TERMS_OPENBANK.equals(userAgreeHistory.getTermsType())) {
                userAgreeHistoryMapper.deleteUserAgreeHistory(userAgreeHistory.getId());
            }
        }

        // when
        List<TermsHistoryVO> result = userService.getUnreadTermsList(user.getId(), countryCode, locale);

        // then
        assertEquals(2, result.size());
        assertEquals(TERMS, result.get(0).getType());
        assertEquals(version, result.get(0).getVersion());
        assertEquals(messageSource.getMessage(TERMS.getViewTitle(), null, locale), result.get(0).getTitle());
        assertEquals(envUtil.getStaticUrl() + TERMS_NEW_PATH, result.get(0).getUrl());
        assertEquals(TERMS_OPENBANK, result.get(1).getType());
        assertEquals(version, result.get(1).getVersion());
        assertEquals(messageSource.getMessage(TERMS_OPENBANK.getViewTitle(), null, locale), result.get(1).getTitle());
        assertEquals(envUtil.getStaticUrl() + TERMS_OPENBANK_PATH, result.get(1).getUrl());
    }

    @Test
    void getUsers_조회_성공_ids_조회_country_null() {
        //given
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setLogin("testLogin" + i);
            user.setPasswordHash("123");
            user.setLangKey("ko");
            user.setCountry("003");
            user.setAllowRecvEmail("Y");
            user.setCls("T");
            user.setAgreeTerms("N");
            user.setAgreePrivacy("N");

            userMapper.insertUser(user);
            ids.add(user.getId());
        }

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setIds(ids);

        //when
        List<User> result = userRepositoryService.getUsers(userSearchRequest);

        //then
        assertEquals(ids.size(), result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals("testLogin" + i, result.get(i).getLogin());
            assertEquals("123", result.get(i).getPasswordHash());
            assertEquals("ko", result.get(i).getLangKey());
            assertEquals("003", result.get(i).getCountry());
            assertEquals("Y", result.get(i).getAllowRecvEmail());
        }
    }

    @Test
    void getUsers_조회_성공_country_조회_ids_null() {
        //given
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setLogin("testLogin" + i);
            user.setPasswordHash("123");
            user.setLangKey("ko");
            user.setCountry("999");
            user.setAllowRecvEmail("Y");
            user.setCls("T");
            user.setAgreeTerms("N");
            user.setAgreePrivacy("N");

            userMapper.insertUser(user);
        }

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setCountry("999");

        //when
        List<User> result = userRepositoryService.getUsers(userSearchRequest);

        //then
        assertEquals(3, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals("testLogin" + i, result.get(i).getLogin());
            assertEquals("123", result.get(i).getPasswordHash());
            assertEquals("ko", result.get(i).getLangKey());
            assertEquals("999", result.get(i).getCountry());
            assertEquals("Y", result.get(i).getAllowRecvEmail());
        }
    }

    @Test
    void getUsers_조회_성공_birthMonthDay_조회() {
        //given
        String[] birthDate = new String[]{"19991301", "20001301", "20231301"};
        String birthMonthDay = "1301";

        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setLogin("testLogin" + i);
            user.setPasswordHash("123");
            user.setLangKey("ko");
            user.setCountry("999");
            user.setAllowRecvEmail("Y");
            user.setCls("T");
            user.setAgreeTerms("N");
            user.setAgreePrivacy("N");
            user.setBirthDate(birthDate[i]);

            userMapper.insertUser(user);
        }

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setBirthMonthDay(birthMonthDay);

        //when
        List<User> result = userRepositoryService.getUsers(userSearchRequest);

        //then
        assertEquals(3, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals("testLogin" + i, result.get(i).getLogin());
            assertEquals("123", result.get(i).getPasswordHash());
            assertEquals("ko", result.get(i).getLangKey());
            assertEquals("999", result.get(i).getCountry());
            assertEquals("Y", result.get(i).getAllowRecvEmail());
            assertEquals(birthDate[i], result.get(i).getBirthDate());
        }
    }
}