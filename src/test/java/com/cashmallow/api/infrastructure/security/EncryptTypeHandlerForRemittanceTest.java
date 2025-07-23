package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.application.impl.RemittanceServiceImpl;
import com.cashmallow.api.domain.model.exchange.Mapping;
import com.cashmallow.api.domain.model.exchange.MappingMapper;
import com.cashmallow.api.domain.model.remittance.RemitReceiverAml;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceMapper;
import com.cashmallow.api.domain.model.remittance.RemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.api.interfaces.admin.dto.AdminRemittanceAskVO;
import com.cashmallow.api.interfaces.admin.dto.MappingRegVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.common.EnvUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class EncryptTypeHandlerForRemittanceTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TravelerMapper travelerMapper;

    @Autowired
    private RemittanceMapper remittanceMapper;

    @Autowired
    RemittanceServiceImpl remittanceService;

    @Autowired
    MappingMapper mappingMapper;

    @Autowired
    EnvUtil envUtil;

    private User user;

    private Traveler traveler;

    private Remittance remittance;

    private RemittanceTravelerSnapshot remittanceTravelerSnapshot;

    private RemitReceiverAml remitReceiverAml;

    public void setUp() {
        user = new User();
        user.setLogin("tiger10001ruukr");
        user.setPasswordHash("20670EB1808148F231146447015A28327D38F23BF0318BA9810BD9F8AB9E7CE6A8A9FBD8A049F74AAD28C6218A953669EA6ED437DA5AB313EE6A8EB475F23CB2");
        user.setFirstName("TOW");
        user.setLastName("TIGER");
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

        Traveler travelerRe = travelerMapper.getTravelerByUserId(loginIdUser.getId());
        traveler.setId(travelerRe.getId());
        travelerMapper.updateTraveler(traveler);

        remittance = new Remittance();

        remittance.setTravelerId(traveler.getId());
        remittance.setBankAccountId(16L);
        remittance.setFromCd("001");
        remittance.setFromAmt(new BigDecimal(1402));
        remittance.setToCd("003");
        remittance.setToAmt(new BigDecimal(2422132));
        remittance.setExchangeRate(new BigDecimal(0.22));
        remittance.setFee(new BigDecimal(172.11));
        remittance.setFeePerAmt(new BigDecimal(10));
        remittance.setFeeRateAmt(new BigDecimal(162));
        remittance.setRemitPurpose(RemittancePurpose.STUDY);
        remittance.setRemitFundSource(RemittanceFundSource.WORKINCOME);
        remittance.setFinancePartnerId(1L);
        remittance.setReceiverCountry("KR");
        remittance.setReceiverPhoneNo("01099992222");
        remittance.setReceiverPhoneCountry("KOR");
        remittance.setReceiverFirstName(user.getFirstName());
        remittance.setReceiverLastName(user.getFirstName());
        remittance.setReceiverBankName("케이뱅크은행");
        remittance.setReceiverBankBranchName("NULL");
        remittance.setReceiverBankCode("KBANK_089");
        remittance.setReceiverBankAccountNo("19929388");
        remittance.setReceiverAddress("태국은행이쥬");
        remittance.setReceiverAddressCountry("KOR");
        remittance.setReceiverAddressCity("서울특별시");
        remittance.setReceiverAddressSecondary("상세주소쥬");
        remittance.setRemitStatus(Remittance.RemittanceStatusCode.OP);
        remittance.setRefundFee(new BigDecimal(162));
        remittance.setCreatedDate(new Timestamp(new Date().getTime()));
        remittance.setUpdatedDate(new Timestamp(new Date().getTime()));
        remittance.setReceiverBirthDate("19860319");
        remittance.setReceiverEmail(user.getEmail());
        remittance.setIsConfirmedReceiverAml("N");

        remittanceMapper.insertRemittance(remittance);

        Remittance remittanceRe = remittanceMapper.getRemittanceInProgress(traveler.getId());

        remittance.setId(remittanceRe.getId());
        remittanceMapper.updateRemittance(remittance);

        MappingRegVO mappingRegVO = new MappingRegVO();
        mappingRegVO.setCountry(user.getCountry());
        // mappingRegVO.setExchange_id(exchange.getId());
        mappingRegVO.setTraveler_id(traveler.getId());
        mappingRegVO.setBank_account_id(remittance.getBankAccountId().intValue());
        mappingRegVO.setPin_value(remittance.getToAmt());
        mappingRegVO.setRef_value("3933");
        mappingRegVO.setBegin_valid_date(new Timestamp(new Date().getTime()));
        mappingRegVO.setEnd_valid_date(new Timestamp(new Date().getTime()));
        mappingRegVO.setCreated_date(new Timestamp(new Date().getTime()));

        mappingMapper.putPinValue(mappingRegVO);

        Map<String, Object> mappingParams = new HashMap<>();
        mappingParams.put("country", user.getCountry());
        mappingParams.put("bankAccountId", remittance.getBankAccountId());
        mappingParams.put("pinValue", remittance.getToAmt());
        mappingParams.put("refValue", "3933");
        mappingParams.put("travelerId", traveler.getId());


        Mapping mapping = mappingMapper.getMappingForMapping(mappingParams);

        mapping.setRemitId(remittance.getId());
        mapping.setTravelerId(traveler.getId());
        mapping.setStatus("CF");

        mappingMapper.updateMapping(mapping);


        remittanceTravelerSnapshot = new RemittanceTravelerSnapshot();
        remittanceTravelerSnapshot.setRemitId(remittance.getId());
        remittanceTravelerSnapshot.setTravelerId(remittance.getTravelerId());
        remittanceTravelerSnapshot.setIdentificationNumber(traveler.getIdentificationNumber());
        remittanceTravelerSnapshot.setAccountNo(traveler.getAccountNo());
        remittanceTravelerSnapshot.setAccountName(traveler.getAccountName());
        remittanceTravelerSnapshot.setAddress(remittance.getReceiverAddress());
        remittanceTravelerSnapshot.setAddressSecondary(remittance.getReceiverAddressSecondary());
        remittanceTravelerSnapshot.setAddressCountry(remittance.getReceiverAddressCountry());
        remittanceTravelerSnapshot.setAddressCity(remittance.getReceiverAddressCity());
        remittanceTravelerSnapshot.setPhoneNumber(remittance.getReceiverPhoneNo());
        remittanceTravelerSnapshot.setPhoneCountry(remittance.getReceiverPhoneCountry());
        remittanceTravelerSnapshot.setBankName(remittance.getReceiverBankName());

        remittanceMapper.insertRemitTravelerSnapshot(remittanceTravelerSnapshot);


        remitReceiverAml = new RemitReceiverAml();

        remitReceiverAml.setTravelerId(traveler.getId());
        remitReceiverAml.setReceiverFirstName(remittance.getReceiverFirstName());
        remitReceiverAml.setReceiverLastName(remittance.getReceiverLastName());
        remitReceiverAml.setBirthDate("20210327");
        remitReceiverAml.setAmlSearchId("392929292929");
        remitReceiverAml.setCreatedDate(new Timestamp(new Date().getTime()));

        remittanceMapper.insertRemitReceiverAml(remitReceiverAml);

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_searchAdminRemittanceForMapping() {
        setUp();

        AdminRemittanceAskVO pvo = new AdminRemittanceAskVO();
        pvo.setSearchValue(remittanceTravelerSnapshot.getAccountNo());
        pvo.setFirst_name(user.getFirstName());
        pvo.setLast_name(user.getLastName());
        pvo.setEmail(user.getEmail());
        pvo.setStart_row(0);
        pvo.setSize(1);

        SearchResultVO searchResultVO = remittanceService.searchAdminRemittanceForMapping(pvo);

        List<Object> objList = searchResultVO.getContent();
        assertNotNull(objList);

        @SuppressWarnings("unchecked")
        Map<String, String> remittanceTest = (Map<String, String>) objList.get(0);

        assertEquals(remittanceTest.get("tr_account_no"), remittanceTravelerSnapshot.getAccountNo());
        assertEquals(remittanceTest.get("email"), user.getEmail());
        assertEquals(remittanceTest.get("receiver_phone_no"), remittance.getReceiverPhoneNo());
        assertEquals(remittanceTest.get("receiver_first_name"), remittance.getReceiverFirstName());
        assertEquals(remittanceTest.get("receiver_last_name"), remittance.getReceiverLastName());
        assertEquals(remittanceTest.get("receiver_address"), remittance.getReceiverAddress());
        assertEquals(remittanceTest.get("receiver_address_secondary"), remittance.getReceiverAddressSecondary());
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_searchAdminRemittanceForReport() {
        setUp();

        AdminRemittanceAskVO pvo = new AdminRemittanceAskVO();
        pvo.setSearchValue(remittanceTravelerSnapshot.getAccountNo());
        pvo.setFirst_name(user.getFirstName());
        pvo.setLast_name(user.getLastName());
        pvo.setEmail(user.getEmail());
        pvo.setStart_row(0);
        pvo.setSize(1);

        SearchResultVO searchResultVO = remittanceService.searchAdminRemittanceForReport(pvo);

        List<Object> objList = searchResultVO.getContent();
        assertNotNull(objList);

        @SuppressWarnings("unchecked")
        Map<String, String> remittanceTest = (Map<String, String>) objList.get(0);

        assertEquals(remittanceTest.get("tr_account_no"), remittanceTravelerSnapshot.getAccountNo());
        assertEquals(remittanceTest.get("tr_account_name"), remittanceTravelerSnapshot.getAccountName());
        assertEquals(remittanceTest.get("email"), user.getEmail());
        assertEquals(remittanceTest.get("receiver_phone_no"), remittance.getReceiverPhoneNo());
        assertEquals(remittanceTest.get("receiver_first_name"), remittance.getReceiverFirstName());
        assertEquals(remittanceTest.get("receiver_last_name"), remittance.getReceiverLastName());
        assertEquals(remittanceTest.get("receiver_address"), remittance.getReceiverAddress());
        assertEquals(remittanceTest.get("receiver_address_secondary"), remittance.getReceiverAddressSecondary());
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_countAdminRemittanceForReport() {
        setUp();

        AdminRemittanceAskVO pvo = new AdminRemittanceAskVO();
        pvo.setSearchValue(remittanceTravelerSnapshot.getAccountNo());
        pvo.setFirst_name(user.getFirstName());
        pvo.setLast_name(user.getLastName());
        pvo.setEmail(user.getEmail());
        pvo.setStart_row(0);
        pvo.setSize(1);

        int cnt = remittanceMapper.countAdminRemittanceForReport(pvo);

        assertEquals(1, cnt);
    }


    @Test
    @Transactional
    void TypeHandler_암복호화_getRemittanceByRemittanceId() {
        setUp();

        Remittance remittanceTest = remittanceMapper.getRemittanceByRemittanceId(remittance.getId());

        assertEquals(remittanceTest.getReceiverBankAccountNo(), remittance.getReceiverBankAccountNo());
        assertEquals(remittanceTest.getReceiverAddress(), remittance.getReceiverAddress());
        assertEquals(remittanceTest.getReceiverAddressSecondary(), remittance.getReceiverAddressSecondary());
        assertEquals(remittanceTest.getReceiverEmail(), remittance.getReceiverEmail());
        assertEquals(remittanceTest.getReceiverFirstName(), remittance.getReceiverFirstName());
        assertEquals(remittanceTest.getReceiverLastName(), remittance.getReceiverLastName());
        assertEquals(remittanceTest.getReceiverPhoneNo(), remittance.getReceiverPhoneNo());


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getRemittanceTravelerSnapshotByRemittanceId() {
        setUp();

        RemittanceTravelerSnapshot remittanceTravelerSnapshotTest = remittanceMapper.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());

        assertEquals(remittanceTravelerSnapshotTest.getAccountNo(), remittanceTravelerSnapshot.getAccountNo());
        assertEquals(remittanceTravelerSnapshotTest.getAccountName(), remittanceTravelerSnapshot.getAccountName());
        assertEquals(remittanceTravelerSnapshotTest.getAddress(), remittanceTravelerSnapshot.getAddress());
        assertEquals(remittanceTravelerSnapshotTest.getAddressSecondary(), remittanceTravelerSnapshot.getAddressSecondary());
        assertEquals(remittanceTravelerSnapshotTest.getPhoneNumber(), remittanceTravelerSnapshot.getPhoneNumber());


    }


    @Test
    @Transactional
    void TypeHandler_암복호화_getRemitReceiverAml() {
        setUp();

        Map<String, String> params = new HashMap<>();
        params.put("receiverFirstName", remitReceiverAml.getReceiverFirstName());
        params.put("receiverLastName", remitReceiverAml.getReceiverLastName());
        params.put("traveler_id", String.valueOf(traveler.getId()));
        params.put("birthDate", remitReceiverAml.getBirthDate());

        List<RemitReceiverAml> amlTests = remittanceMapper.getRemitReceiverAml(params);
        assertNotNull(amlTests);

        RemitReceiverAml amlTest = amlTests.get(0);

        assertEquals(amlTest.getReceiverFirstName(), remitReceiverAml.getReceiverFirstName());
        assertEquals(amlTest.getReceiverLastName(), remitReceiverAml.getReceiverLastName());
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getRemitReceiverAmlById() {
        setUp();
        Map<String, String> params = new HashMap<>();
        params.put("receiverFirstName", remitReceiverAml.getReceiverFirstName());
        params.put("receiverLastName", remitReceiverAml.getReceiverLastName());
        params.put("traveler_id", String.valueOf(traveler.getId()));
        params.put("birthDate", remitReceiverAml.getBirthDate());

        List<RemitReceiverAml> amlTests = remittanceMapper.getRemitReceiverAml(params);
        assertNotNull(amlTests);

        RemitReceiverAml amlTest = amlTests.get(0);

        RemitReceiverAml amlResult = remittanceMapper.getRemitReceiverAmlById(amlTest.getId());

        assertEquals(amlResult.getReceiverFirstName(), remitReceiverAml.getReceiverFirstName());
        assertEquals(amlResult.getReceiverLastName(), remitReceiverAml.getReceiverLastName());
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getRemittanceInProgress() {
        setUp();

        Remittance remittanceTest = remittanceMapper.getRemittanceInProgress(traveler.getId());

        assertEquals(remittanceTest.getReceiverBankAccountNo(), remittance.getReceiverBankAccountNo());
        assertEquals(remittanceTest.getReceiverAddress(), remittance.getReceiverAddress());
        assertEquals(remittanceTest.getReceiverAddressSecondary(), remittance.getReceiverAddressSecondary());
        assertEquals(remittanceTest.getReceiverEmail(), remittance.getReceiverEmail());
        assertEquals(remittanceTest.getReceiverFirstName(), remittance.getReceiverFirstName());
        assertEquals(remittanceTest.getReceiverLastName(), remittance.getReceiverLastName());
        assertEquals(remittanceTest.getReceiverPhoneNo(), remittance.getReceiverPhoneNo());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getRemittanceListByTravelerId() {
        setUp();

        Map<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("startRow", 0);
        params.put("size", 1);

        List<Remittance> remittances = remittanceMapper.getRemittanceListByTravelerId(params);

        Remittance remittanceTest = remittances.get(0);

        assertEquals(remittanceTest.getReceiverBankAccountNo(), remittance.getReceiverBankAccountNo());
        assertEquals(remittanceTest.getReceiverAddress(), remittance.getReceiverAddress());
        assertEquals(remittanceTest.getReceiverAddressSecondary(), remittance.getReceiverAddressSecondary());
        assertEquals(remittanceTest.getReceiverEmail(), remittance.getReceiverEmail());
        assertEquals(remittanceTest.getReceiverFirstName(), remittance.getReceiverFirstName());
        assertEquals(remittanceTest.getReceiverLastName(), remittance.getReceiverLastName());
        assertEquals(remittanceTest.getReceiverPhoneNo(), remittance.getReceiverPhoneNo());

    }


}