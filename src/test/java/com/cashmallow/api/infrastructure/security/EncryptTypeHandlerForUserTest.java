package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.application.UserService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.common.EnvUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EncryptTypeHandlerForUserTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private TravelerMapper travelerMapper;

    private User user;

    private Traveler traveler;

    @Autowired
    EnvUtil envUtil;

    public void setUp() {
        user = new User();
        user.setLogin("tiger10001ruukr");
        user.setPasswordHash("20670EB1808148F231146447015A28327D38F23BF0318BA9810BD9F8AB9E7CE6A8A9FBD8A049F74AAD28C6218A953669EA6ED437DA5AB313EE6A8EB475F23CB2");
        user.setFirstName("한글");
        user.setLastName("테스트");
        user.setEmail("tiger10001@ruu.kr");
        user.setAllowRecvEmail("Y");
        user.setActivated(true);
        user.setLangKey("ko");
        user.setProfilePhoto("C0OmmNx3rm0uAuVmuanhX1874af5a015");
        user.setProfilePhotoUrl(envUtil.getCdnUrl(), "C0OmmNx3rm0uAuVmuanhX1874af5a015");
        user.setBirthDate("19910215");
        user.setCls("A");
        user.setCountry("001");
        user.setRecommenderId(null);
        user.setLastLoginTime(null);
        user.setLastLogoutTime(null);
        user.setCreator(null);
        user.setLastModifier(null);
        user.setLastModifiedDate(null);
        user.setDeactivatedDate(null);
        user.setInstanceId("88FE2163EF9DEE04CE0B58C7E0C121C010898A9FD04B382F70E1984766AC2F7D36FD57B7DA08A6334C87A8110907AB8D69D183F3AC64721AE7215E4F62D126CA");
        user.setDeviceType("A");
        user.setVersionCode("610");
        user.setAgreePrivacy("Y");
        user.setAgreeTerms("Y");
        user.setPhoneNumber("+932580963");
        user.setPhoneCountry("AFG");
        user.setDeviceOsVersion("31");

        userMapper.insertUser(user);

        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());

        traveler = new Traveler();

        traveler.setUserId(loginIdUser.getId());
        traveler.setAccountNo("01072661666");
        traveler.setAccountName("남궁석");
        traveler.setBankName("기업은행");
        traveler.setAccountOk("Y");
        traveler.setAccountOkDate(new Timestamp(new Date().getTime()));
        traveler.setCreatedDate(new Timestamp(new Date().getTime()));
        traveler.setUpdatedDate(new Timestamp(new Date().getTime()));
        traveler.setCreator(1712L);
        traveler.setContactType(null);
        traveler.setContactId(null);
        traveler.setPassportExpDate(null);
        traveler.setPassportCountry(null);
        traveler.setLocalFirstName("남궁석");
        traveler.setLocalLastName("test");
        traveler.setEnFirstName("seok");
        traveler.setEnLastName("namkoong");
        traveler.setCertificationType(CertificationType.ID_CARD);
        traveler.setIdentificationNumber("88ff2a7131746e93feac8f754aa90e01");
        traveler.setCertificationOkDate(null);
        traveler.setCertificationPhoto("fZwLFEoifW3tqVdKnXHk3187b16bc3a5");
        traveler.setCertificationOk("Y");
        traveler.setAccountBankbookPhoto(null);
        traveler.setAMLSearchId(null);
        traveler.setAddress("400 Gangseo-ro Gangseo-gu Seoul");
        traveler.setAddressPhoto(null);
        traveler.setPaygateMemberId(null);
        traveler.setPaygateKycStatus(null);
        traveler.setAddressCountry("KOR");
        traveler.setAddressCity("Seoul");
        traveler.setAddressSecondary("gang nam gu ro ga ja");
        traveler.setPaygateKycRefId(null);
        traveler.setSex(Traveler.TravelerSex.FEMALE);

        travelerMapper.insertTraveler(traveler);

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getUserByLoginId_getUserByUserId() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());

        assertEquals(user.getFirstName(), loginIdUser.getFirstName());
        assertEquals(user.getLastName(), loginIdUser.getLastName());
        assertEquals(user.getEmail(), loginIdUser.getEmail());
        assertEquals(user.getPhoneNumber(), loginIdUser.getPhoneNumber());

        loginIdUser = userMapper.getUserByUserId(loginIdUser.getId());

        assertEquals(user.getFirstName(), loginIdUser.getFirstName());
        assertEquals(user.getLastName(), loginIdUser.getLastName());
        assertEquals(user.getEmail(), loginIdUser.getEmail());
        assertEquals(user.getPhoneNumber(), loginIdUser.getPhoneNumber());
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getFirstUser() {
        setUp();
        User loginIdUser = userMapper.getFirstUser();

        assertEquals(user.getFirstName(), loginIdUser.getFirstName());
        assertEquals(user.getLastName(), loginIdUser.getLastName());
        assertEquals(user.getEmail(), loginIdUser.getEmail());
        assertEquals(user.getPhoneNumber(), loginIdUser.getPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getUserByEmail() {
        setUp();
        User loginIdUser = userMapper.getUserByEmail(user.getEmail());

        assertEquals(user.getFirstName(), loginIdUser.getFirstName());
        assertEquals(user.getLastName(), loginIdUser.getLastName());
        assertEquals(user.getEmail(), loginIdUser.getEmail());
        assertEquals(user.getPhoneNumber(), loginIdUser.getPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_searchUsers() {
        setUp();
        List<User> userList = userMapper.searchUsers(user.getEmail());

        assertNotNull(userList);
        User loginIdUser = userList.get(0);
        assertEquals(user.getFirstName(), loginIdUser.getFirstName());
        assertEquals(user.getLastName(), loginIdUser.getLastName());
        assertEquals(user.getEmail(), loginIdUser.getEmail());
        assertEquals(user.getPhoneNumber(), loginIdUser.getPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_searchUsers_byId() {
        setUp();
        User loginIdUser = userMapper.getUserByEmail(user.getEmail());
        List<User> userList = userMapper.searchUsers(String.valueOf(loginIdUser.getId()));

        assertNotNull(userList);
        User user1 = userList.get(0);
        assertEquals(user1.getFirstName(), loginIdUser.getFirstName());
        assertEquals(user1.getLastName(), loginIdUser.getLastName());
        assertEquals(user1.getEmail(), loginIdUser.getEmail());
        assertEquals(user1.getPhoneNumber(), loginIdUser.getPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_update_암복호화_테스트() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        loginIdUser.setFirstName("tiger022222");
        loginIdUser.setActivated(false);

        userMapper.updateUser(loginIdUser);


        User loginIdUserNew = userMapper.getUserByLoginId(user.getLogin());
        assertEquals(loginIdUser.getFirstName(), loginIdUserNew.getFirstName());
        assertEquals(loginIdUser.isActivated(), loginIdUserNew.isActivated());

    }

    @Test
    @Transactional
    void TypeHandler_update_userMapper() {
        setUp();

        Map<String, String> params = new HashMap<>();
        params.put("start", String.valueOf(0));
        params.put("size", String.valueOf(10));

        User user = userMapper.getUserByUserId(3L);

        assertNotNull(user);


    }

}