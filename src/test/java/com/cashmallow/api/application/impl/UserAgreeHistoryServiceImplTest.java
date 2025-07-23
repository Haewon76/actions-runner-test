package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.UserAgreeHistoryService;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserAgreeHistory;
import com.cashmallow.api.domain.model.user.UserAgreeHistoryMapper;
import com.cashmallow.api.domain.model.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class UserAgreeHistoryServiceImplTest {
    @Autowired
    UserAgreeHistoryService userAgreeHistoryService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserAgreeHistoryMapper userAgreeHistoryMapper;

    final String LOGIN_HK = "testtestHK";
    final String LOGIN_KR = "testtestKR";

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

        User userKr = new User();
        BeanUtils.copyProperties(userHk, userKr);
        userKr.setLogin(LOGIN_KR);
        userKr.setCountry("003");

        userMapper.insertUser(userHk);
        userMapper.insertUser(userKr);
    }

    @Test
    void insertUserAgreeHistory_호출_약관_모두_동의_저장_성공_Hk() {
        // given
        User user = userMapper.getUserByLoginId(LOGIN_HK);
        Integer version = 1234567890;
        UserAgreeHistory userAgreeHistory1 = UserAgreeHistory.builder()
                .userId(user.getId())
                .version(version)
                .termsType(TermsType.TERMS)
                .agreed(true)
                .build();
        UserAgreeHistory userAgreeHistory2 = UserAgreeHistory.builder()
                .userId(user.getId())
                .version(version)
                .termsType(TermsType.PRIVACY)
                .agreed(true)
                .build();
        List<UserAgreeHistory> userAgreeHistoryList = List.of(userAgreeHistory1, userAgreeHistory2);

        // when
        userAgreeHistoryService.insertUserAgreeHistory(userAgreeHistoryList);

        // then
        List<UserAgreeHistory> result = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());
        result.sort(Comparator.comparingInt(o -> o.getTermsType().ordinal()));
        assertEquals(2, result.size());
        for (int i = 0; i < userAgreeHistoryList.size(); i++) {
            assertEquals(userAgreeHistoryList.get(i).getUserId(), result.get(i).getUserId());
            assertEquals(userAgreeHistoryList.get(i).getVersion(), result.get(i).getVersion());
            assertEquals(userAgreeHistoryList.get(i).getTermsType(), result.get(i).getTermsType());
            assertEquals(userAgreeHistoryList.get(i).isAgreed(), result.get(i).isAgreed());
        }
    }

    @Test
    void insertUserAgreeHistory_호출_약관_모두_동의_저장_성공_Kr() {
        // given
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Integer version = 1234567890;
        UserAgreeHistory userAgreeHistory1 = UserAgreeHistory.builder()
                .userId(user.getId())
                .version(version)
                .termsType(TermsType.TERMS)
                .agreed(true)
                .build();
        UserAgreeHistory userAgreeHistory2 = UserAgreeHistory.builder()
                .userId(user.getId())
                .version(version)
                .termsType(TermsType.PRIVACY)
                .agreed(true)
                .build();
        UserAgreeHistory userAgreeHistory3 = UserAgreeHistory.builder()
                .userId(user.getId())
                .version(version)
                .termsType(TermsType.TERMS_OPENBANK)
                .agreed(true)
                .build();
        List<UserAgreeHistory> userAgreeHistoryList = List.of(userAgreeHistory1, userAgreeHistory2, userAgreeHistory3);


        // when
        userAgreeHistoryService.insertUserAgreeHistory(userAgreeHistoryList);

        // then
        List<UserAgreeHistory> result = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());
        result.sort(Comparator.comparingInt(o -> o.getTermsType().ordinal()));
        assertEquals(3, result.size());
        for (int i = 0; i < userAgreeHistoryList.size(); i++) {
            assertEquals(userAgreeHistoryList.get(i).getUserId(), result.get(i).getUserId());
            assertEquals(userAgreeHistoryList.get(i).getVersion(), result.get(i).getVersion());
            assertEquals(userAgreeHistoryList.get(i).getTermsType(), result.get(i).getTermsType());
            assertEquals(userAgreeHistoryList.get(i).isAgreed(), result.get(i).isAgreed());
        }
    }

    @Test
    void insertUserAgreeHistory_호출_약관_저장_성공_Kr_openbank() {
        // given
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Integer version = 1234567890;
        UserAgreeHistory userAgreeHistory = UserAgreeHistory.builder()
                .userId(user.getId())
                .version(version)
                .termsType(TermsType.TERMS_OPENBANK)
                .agreed(true)
                .build();
        List<UserAgreeHistory> userAgreeHistoryList = List.of(userAgreeHistory);

        // when
        userAgreeHistoryService.insertUserAgreeHistory(userAgreeHistoryList);

        // then
        List<UserAgreeHistory> result = userAgreeHistoryMapper.getUserAgreeHistoriesByUserId(user.getId());
        assertEquals(1, result.size());
        assertEquals(userAgreeHistoryList.get(0).getUserId(), result.get(0).getUserId());
        assertEquals(userAgreeHistoryList.get(0).getVersion(), result.get(0).getVersion());
        assertEquals(userAgreeHistoryList.get(0).getTermsType(), result.get(0).getTermsType());
        assertEquals(userAgreeHistoryList.get(0).isAgreed(), result.get(0).isAgreed());
    }

    @Test
    void getMaxVersionUserAgreeHistories_호출_정상_값_조회() {
        // given
        User user = userMapper.getUserByLoginId(LOGIN_KR);
        Integer version = 1234567890;

        UserAgreeHistory userAgreeHistory1 = new UserAgreeHistory();
        UserAgreeHistory userAgreeHistory2 = new UserAgreeHistory();
        UserAgreeHistory userAgreeHistory3 = new UserAgreeHistory();

        userAgreeHistory1.setUserId(user.getId());
        userAgreeHistory1.setVersion(version);
        userAgreeHistory1.setTermsType(TermsType.TERMS);

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

        // when
        List<UserAgreeHistory> result = userAgreeHistoryService.getMaxVersionUserAgreeHistories(user.getId());
        result.sort(Comparator.comparingInt(value -> value.getTermsType().ordinal()));

        // then
        assertEquals(userAgreeHistoryList.size(), result.size());
        assertEquals(TermsType.TERMS, result.get(0).getTermsType());
        assertEquals(TermsType.PRIVACY, result.get(1).getTermsType());
        assertEquals(TermsType.TERMS_OPENBANK, result.get(2).getTermsType());

        for (UserAgreeHistory userAgreeHistory : result) {
            assertEquals(user.getId(), userAgreeHistory.getUserId());
            assertEquals(version, userAgreeHistory.getVersion());
        }
    }
}
