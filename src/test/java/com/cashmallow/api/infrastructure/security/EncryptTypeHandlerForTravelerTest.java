package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.api.interfaces.admin.dto.TravelerAskVO;
import com.cashmallow.common.EnvUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EncryptTypeHandlerForTravelerTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TravelerMapper travelerMapper;

    private User user;

    private Traveler traveler;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    EnvUtil envUtil;

    public void setUp() {
        user = new User();
        user.setLogin("tiger10001ruukr");
        user.setPasswordHash("20670EB1808148F231146447015A28327D38F23BF0318BA9810BD9F8AB9E7CE6A8A9FBD8A049F74AAD28C6218A953669EA6ED437DA5AB313EE6A8EB475F23CB2");
        user.setFirstName("TOW1");
        user.setLastName("TIGER2");
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
    void TypeHandler_암복호화_insertTraveler() {
        setUp();
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getTravelerByUserId() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        Traveler travelerTest = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerTest.getId());
        travelerMapper.updateTraveler(traveler);

        Traveler travelerUpdate = travelerMapper.getTravelerByUserId(loginIdUser.getId());

        assertEquals(travelerUpdate.getAccountNo(), traveler.getAccountNo());
        assertEquals(travelerUpdate.getAccountName(), traveler.getAccountName());
        assertEquals(travelerUpdate.getLocalLastName(), traveler.getLocalLastName());
        assertEquals(travelerUpdate.getLocalFirstName(), traveler.getLocalFirstName());
        assertEquals(travelerUpdate.getEnFirstName(), traveler.getEnFirstName());
        assertEquals(travelerUpdate.getEnLastName(), traveler.getEnLastName());
        assertEquals(travelerUpdate.getAddress(), traveler.getAddress());
        assertEquals(travelerUpdate.getAddressSecondary(), traveler.getAddressSecondary());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getTravelerByTravelerId() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        Traveler travelerTest = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerTest.getId());
        travelerMapper.updateTraveler(traveler);

        Traveler travelerUpdate = travelerMapper.getTravelerByTravelerId(travelerTest.getId());

        assertEquals(travelerUpdate.getAccountNo(), traveler.getAccountNo());
        assertEquals(travelerUpdate.getAccountName(), traveler.getAccountName());
        assertEquals(travelerUpdate.getLocalLastName(), traveler.getLocalLastName());
        assertEquals(travelerUpdate.getLocalFirstName(), traveler.getLocalFirstName());
        assertEquals(travelerUpdate.getEnFirstName(), traveler.getEnFirstName());
        assertEquals(travelerUpdate.getEnLastName(), traveler.getEnLastName());
        assertEquals(travelerUpdate.getAddress(), traveler.getAddress());
        assertEquals(travelerUpdate.getAddressSecondary(), traveler.getAddressSecondary());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_countTravelerCertificationInfo() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        Traveler travelerTest = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerTest.getId());
        travelerMapper.updateTraveler(traveler);

        TravelerAskVO travelerAskVO = new TravelerAskVO();
        travelerAskVO.setFirst_name(user.getFirstName());
        travelerAskVO.setLast_name(user.getLastName());
        travelerAskVO.setEmail(user.getEmail());

        int cnt = travelerMapper.countTravelerCertificationInfo(travelerAskVO);

        assertEquals(1, cnt);

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getTravelerCertificationInfo() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        Traveler travelerTest = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerTest.getId());
        travelerMapper.updateTraveler(traveler);

        Traveler travelerUpdate = travelerMapper.getTravelerByTravelerId(travelerTest.getId());

        TravelerAskVO travelerAskVO = new TravelerAskVO();
        travelerAskVO.setFirst_name(user.getFirstName());
        travelerAskVO.setLast_name(user.getLastName());
        travelerAskVO.setEmail(user.getEmail());
        travelerAskVO.setStart_row(0);
        travelerAskVO.setSize(1);

        List<Map<String, Object>> travelers = travelerMapper.getTravelerCertificationInfo(travelerAskVO);

        Map<String, Object> tMap = travelers.get(0);

        tMap.put("first_name", securityService.decryptAES256((String) tMap.get("first_name")));
        tMap.put("last_name", securityService.decryptAES256((String) tMap.get("last_name")));
        tMap.put("email", securityService.decryptAES256((String) tMap.get("email")));
        tMap.put("en_first_name", securityService.decryptAES256((String) tMap.get("en_first_name")));
        tMap.put("en_last_name", securityService.decryptAES256((String) tMap.get("en_last_name")));
        tMap.put("account_no", securityService.decryptAES256((String) tMap.get("account_no")));
        tMap.put("account_name", securityService.decryptAES256((String) tMap.get("account_name")));
        tMap.put("address", securityService.decryptAES256((String) tMap.get("address")));
        tMap.put("address_secondary", securityService.decryptAES256((String) tMap.get("address_secondary")));

        assertEquals(user.getFirstName(), tMap.get("first_name"));
        assertEquals(user.getLastName(), tMap.get("last_name"));
        assertEquals(user.getEmail(), tMap.get("email"));

        assertEquals(travelerUpdate.getEnFirstName(), tMap.get("en_first_name"));
        assertEquals(travelerUpdate.getEnLastName(), tMap.get("en_last_name"));

        assertEquals(travelerUpdate.getAccountNo(), tMap.get("account_no"));
        assertEquals(travelerUpdate.getAccountName(), tMap.get("account_name"));

        assertEquals(travelerUpdate.getAddress(), tMap.get("address"));
        assertEquals(travelerUpdate.getAddressSecondary(), tMap.get("address_secondary"));
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_countTravelerAccountInfo() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        Traveler travelerTest = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerTest.getId());
        travelerMapper.updateTraveler(traveler);

        TravelerAskVO travelerAskVO = new TravelerAskVO();
        travelerAskVO.setFirst_name(user.getFirstName());
        travelerAskVO.setLast_name(user.getLastName());
        travelerAskVO.setEmail(user.getEmail());

        int cnt = travelerMapper.countTravelerAccountInfo(travelerAskVO);

        assertEquals(1, cnt);

    }


    @Test
    @Transactional
    void TypeHandler_암복호화_getTravelerAccountInfo() {
        setUp();
        User loginIdUser = userMapper.getUserByLoginId(user.getLogin());
        Traveler travelerTest = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerTest.getId());
        travelerMapper.updateTraveler(traveler);

        Traveler travelerUpdate = travelerMapper.getTravelerByTravelerId(travelerTest.getId());

        TravelerAskVO travelerAskVO = new TravelerAskVO();
        travelerAskVO.setFirst_name(user.getFirstName());
        travelerAskVO.setLast_name(user.getLastName());
        travelerAskVO.setEmail(user.getEmail());
        travelerAskVO.setStart_row(0);
        travelerAskVO.setSize(1);

        List<Map<String, Object>> travelers = travelerMapper.getTravelerAccountInfo(travelerAskVO);

        Map<String, Object> tMap = travelers.get(0);

        tMap.put("first_name", securityService.decryptAES256((String) tMap.get("first_name")));
        tMap.put("last_name", securityService.decryptAES256((String) tMap.get("last_name")));
        tMap.put("email", securityService.decryptAES256((String) tMap.get("email")));
        tMap.put("en_first_name", securityService.decryptAES256((String) tMap.get("en_first_name")));
        tMap.put("en_last_name", securityService.decryptAES256((String) tMap.get("en_last_name")));
        tMap.put("account_no", securityService.decryptAES256((String) tMap.get("account_no")));
        tMap.put("account_name", securityService.decryptAES256((String) tMap.get("account_name")));
        tMap.put("address", securityService.decryptAES256((String) tMap.get("address")));
        tMap.put("address_secondary", securityService.decryptAES256((String) tMap.get("address_secondary")));

        assertEquals(user.getFirstName(), tMap.get("first_name"));
        assertEquals(user.getLastName(), tMap.get("last_name"));
        assertEquals(user.getEmail(), tMap.get("email"));

        assertEquals(travelerUpdate.getEnFirstName(), tMap.get("en_first_name"));
        assertEquals(travelerUpdate.getEnLastName(), tMap.get("en_last_name"));

        assertEquals(travelerUpdate.getAccountNo(), tMap.get("account_no"));
        assertEquals(travelerUpdate.getAccountName(), tMap.get("account_name"));

        assertEquals(travelerUpdate.getAddress(), tMap.get("address"));
        assertEquals(travelerUpdate.getAddressSecondary(), tMap.get("address_secondary"));

    }

}