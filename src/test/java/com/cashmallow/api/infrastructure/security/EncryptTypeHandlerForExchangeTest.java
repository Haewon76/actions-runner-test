package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.application.impl.ExchangeServiceImpl;
import com.cashmallow.api.domain.model.exchange.*;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.api.interfaces.admin.dto.AdminExchangeAskVO;
import com.cashmallow.api.interfaces.admin.dto.MappingRegVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.common.EnvUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EncryptTypeHandlerForExchangeTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TravelerMapper travelerMapper;

    @Autowired
    ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Autowired
    ExchangeServiceImpl exchangeService;

    @Autowired
    MappingMapper mappingMapper;

    @Autowired
    EnvUtil envUtil;

    private User user;

    private Traveler traveler;

    private Exchange exchange;


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

        exchange = new Exchange();

        exchange.setBankAccountId(16);
        exchange.setTravelerId(traveler.getId());
        exchange.setFromCd("001");
        exchange.setFromAmt(new BigDecimal(32868.64));
        exchange.setToCd("003");
        exchange.setToAmt(new BigDecimal(5500000));
        exchange.setFee(new BigDecimal(352.64));
        exchange.setExStatus("OP");
        exchange.setFcmYn("N");
        exchange.setExchangeRate(new BigDecimal(0.005912));
        exchange.setTrBankName("Bank of China");
        exchange.setTrAccountName("중국 은행");
        exchange.setTrAccountNo("2020399023");
        exchange.setTrFromAmt(new BigDecimal(32868));
        exchange.setTrDepositDate(new Timestamp(new Date().getTime()));
        exchange.setExStatusDate(new Timestamp(new Date().getTime()));
        exchange.setCreatedDate(new Timestamp(new Date().getTime()));
        exchange.setUpdatedDate(new Timestamp(new Date().getTime()));
        exchange.setCreator(1399L);
        exchange.setTrReceiptPhoto("NxbtwJGpnmXERGJeHfSfj1879c77247f");
        exchange.setTrAccountBankbookPhoto("C0eA425Alg83WLEzjWpbx186e81a5bab");
        exchange.setIdentificationNumber("fd3bd68f7903bfe6ecbeb24cb005ce0a");
        exchange.setTrAddress("한국 어느 하늘 아래");
        exchange.setTrAddressPhoto("V1mq2VJp1AoLv0MQqI06b186e80c1459");
        exchange.setTrPhoneNumber("+85201072661662");
        exchange.setTrAddressCountry("HKG");
        exchange.setTrAddressCity("Kowloon");
        exchange.setTrAddressSecondary("우리집");
        exchange.setTrPhoneCountry("HKG");
        exchange.setFeePerAmt(new BigDecimal(60));
        exchange.setFeeRateAmt(new BigDecimal(292.64));

        exchangeMapper.insertExchange(exchange);

        List<Exchange> exchanges = exchangeMapper.getExchangeListByTravelerId(traveler.getId());
        Exchange exchange1 = exchanges.get(0);
        exchange.setId(exchange1.getId());

        exchangeMapper.updateExchange(exchange);

        MappingRegVO mappingRegVO = new MappingRegVO();
        mappingRegVO.setCountry(user.getCountry());
        // mappingRegVO.setExchange_id(exchange.getId());
        mappingRegVO.setTraveler_id(traveler.getId());
        mappingRegVO.setBank_account_id(exchange.getBankAccountId());
        mappingRegVO.setPin_value(exchange.getToAmt());
        mappingRegVO.setRef_value("3933");
        mappingRegVO.setBegin_valid_date(new Timestamp(new Date().getTime()));
        mappingRegVO.setEnd_valid_date(new Timestamp(new Date().getTime()));
        mappingRegVO.setCreated_date(new Timestamp(new Date().getTime()));

        mappingMapper.putPinValue(mappingRegVO);

        Map<String, Object> mappingParams = new HashMap<>();
        mappingParams.put("country", user.getCountry());
        mappingParams.put("bankAccountId", exchange.getBankAccountId());
        mappingParams.put("pinValue", exchange.getToAmt());
        mappingParams.put("refValue", "3933");
        mappingParams.put("travelerId", traveler.getId());


        Mapping mapping = mappingMapper.getMappingForMapping(mappingParams);

        mapping.setExchangeId(exchange.getId());
        mapping.setTravelerId(traveler.getId());
        mapping.setStatus("CF");

        mappingMapper.updateMapping(mapping);

        ExchangeDepositReceipt exchangeDepositReceipt = new ExchangeDepositReceipt();
        exchangeDepositReceipt.setExchangeId(exchange.getId());
        exchangeDepositReceipt.setReceiptPhoto(exchange.getTrReceiptPhoto());
        exchangeDepositReceipt.setCreatedDate(new Timestamp(new Date().getTime()));
        exchangeMapper.insertExchangeDepositReceipt(exchangeDepositReceipt);


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getLastestExchangeInProgressByTravelerId() {
        setUp();

        List<Exchange> exchangeList = exchangeMapper.getLastestExchangeInProgressByTravelerId(traveler.getId());
        assertNotNull(exchangeList);

        Exchange exchangeTest = exchangeList.get(0);


        assertEquals(exchangeTest.getTrAccountNo(), exchange.getTrAccountNo());
        assertEquals(exchangeTest.getTrAccountName(), exchange.getTrAccountName());
        assertEquals(exchangeTest.getTrAddress(), exchange.getTrAddress());
        assertEquals(exchangeTest.getTrAddressSecondary(), exchange.getTrAddressSecondary());
        assertEquals(exchangeTest.getTrPhoneNumber(), exchange.getTrPhoneNumber());


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getExchangeByExchangeId() {
        setUp();

        List<Exchange> exchangeList = exchangeMapper.getLastestExchangeInProgressByTravelerId(traveler.getId());
        assertNotNull(exchangeList);

        Exchange exchangeTest = exchangeList.get(0);

        Exchange exchangeById = exchangeMapper.getExchangeByExchangeId(exchangeTest.getId());


        assertEquals(exchangeById.getTrAccountNo(), exchange.getTrAccountNo());
        assertEquals(exchangeById.getTrAccountName(), exchange.getTrAccountName());
        assertEquals(exchangeById.getTrAddress(), exchange.getTrAddress());
        assertEquals(exchangeById.getTrAddressSecondary(), exchange.getTrAddressSecondary());
        assertEquals(exchangeById.getTrPhoneNumber(), exchange.getTrPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getExchangeListByExchangeIds() {
        setUp();

        List<Exchange> exchangeList = exchangeMapper.getLastestExchangeInProgressByTravelerId(traveler.getId());
        assertNotNull(exchangeList);

        Exchange exchangeTest = exchangeList.get(0);

        List<Long> exchangeIds = new ArrayList<>();
        exchangeIds.add(exchangeTest.getId());

        List<Exchange> exchangeByIds = exchangeMapper.getExchangeListByExchangeIds(exchangeIds);
        assertNotNull(exchangeByIds);

        exchangeTest = exchangeList.get(0);

        assertEquals(exchangeTest.getTrAccountNo(), exchange.getTrAccountNo());
        assertEquals(exchangeTest.getTrAccountName(), exchange.getTrAccountName());
        assertEquals(exchangeTest.getTrAddress(), exchange.getTrAddress());
        assertEquals(exchangeTest.getTrAddressSecondary(), exchange.getTrAddressSecondary());
        assertEquals(exchangeTest.getTrPhoneNumber(), exchange.getTrPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getExchangeOpListByTravelerId() {
        setUp();
        List<Exchange> exchangeById = exchangeMapper.getExchangeOpListByTravelerId(traveler.getId());
        assertNotNull(exchangeById);

        Exchange exchangeTest = exchangeById.get(0);

        assertEquals(exchangeTest.getTrAccountNo(), exchange.getTrAccountNo());
        assertEquals(exchangeTest.getTrAccountName(), exchange.getTrAccountName());
        assertEquals(exchangeTest.getTrAddress(), exchange.getTrAddress());
        assertEquals(exchangeTest.getTrAddressSecondary(), exchange.getTrAddressSecondary());
        assertEquals(exchangeTest.getTrPhoneNumber(), exchange.getTrPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getExchangeListByTravelerId() {
        setUp();
        List<Exchange> exchangeById = exchangeMapper.getExchangeListByTravelerId(traveler.getId());
        assertNotNull(exchangeById);

        Exchange exchangeTest = exchangeById.get(0);

        assertEquals(exchangeTest.getTrAccountNo(), exchange.getTrAccountNo());
        assertEquals(exchangeTest.getTrAccountName(), exchange.getTrAccountName());
        assertEquals(exchangeTest.getTrAddress(), exchange.getTrAddress());
        assertEquals(exchangeTest.getTrAddressSecondary(), exchange.getTrAddressSecondary());
        assertEquals(exchangeTest.getTrPhoneNumber(), exchange.getTrPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getExchangeByTravelerId() throws Exception {
        setUp();

        SearchResultVO searchResultVO = exchangeRepositoryService.getExchangeByTravelerId(user.getId(), 0, 1);

        List<Object> objList = searchResultVO.getContent();
        assertNotNull(objList);

        @SuppressWarnings("unchecked")
        Map<String, String> exchangeTest = (Map<String, String>) objList.get(0);

        assertEquals(exchangeTest.get("tr_account_no"), exchange.getTrAccountNo());
        assertEquals(exchangeTest.get("tr_account_name"), exchange.getTrAccountName());
        assertEquals(exchangeTest.get("tr_address"), exchange.getTrAddress());
        assertEquals(exchangeTest.get("tr_address_secondary"), exchange.getTrAddressSecondary());
        assertEquals(exchangeTest.get("tr_phone_number"), exchange.getTrPhoneNumber());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_countAdminExchange() throws Exception {
        setUp();

        AdminExchangeAskVO adminExchangeAskVO = new AdminExchangeAskVO();
        adminExchangeAskVO.setFirst_name(user.getFirstName());
        adminExchangeAskVO.setLast_name(user.getLastName());
        adminExchangeAskVO.setEmail(user.getEmail());


        int cnt = exchangeMapper.countAdminExchange(adminExchangeAskVO);

        assertEquals(cnt, 1);

    }


    @Test
    @Transactional
    @SuppressWarnings("unchecked")
    void TypeHandler_암복호화_findAdminExchange() throws Exception {
        setUp();

        AdminExchangeAskVO adminExchangeAskVO = new AdminExchangeAskVO();
        adminExchangeAskVO.setFirst_name(user.getFirstName());
        adminExchangeAskVO.setLast_name(user.getLastName());
        adminExchangeAskVO.setEmail(user.getEmail());
        adminExchangeAskVO.setSearchValue(exchange.getTrAccountNo());
        adminExchangeAskVO.setStart_row(0);
        adminExchangeAskVO.setSize(1);


        SearchResultVO searchResultVO = exchangeService.findAdminExchange(adminExchangeAskVO);

        List<Object> objList = searchResultVO.getContent();
        assertNotNull(objList);

        Map<String, String> exchangeTest = (Map<String, String>) objList.get(0);

        assertEquals(exchangeTest.get("first_name"), user.getFirstName());
        assertEquals(exchangeTest.get("last_name"), user.getLastName());
        assertEquals(exchangeTest.get("email"), user.getEmail());
        assertEquals(exchangeTest.get("tr_account_no"), exchange.getTrAccountNo());
        assertEquals(exchangeTest.get("tr_account_name"), exchange.getTrAccountName());

        adminExchangeAskVO.setFirst_name(user.getFirstName());
        adminExchangeAskVO.setLast_name(user.getLastName());
        adminExchangeAskVO.setEmail(user.getEmail());
        adminExchangeAskVO.setSearchValue(exchange.getTrAccountName());
        adminExchangeAskVO.setStart_row(0);
        adminExchangeAskVO.setSize(1);

        searchResultVO = exchangeService.findAdminExchange(adminExchangeAskVO);

        objList = searchResultVO.getContent();
        assertNotNull(objList);

        exchangeTest = (Map<String, String>) objList.get(0);

        assertEquals(exchangeTest.get("first_name"), user.getFirstName());
        assertEquals(exchangeTest.get("last_name"), user.getLastName());
        assertEquals(exchangeTest.get("email"), user.getEmail());
        assertEquals(exchangeTest.get("tr_account_no"), exchange.getTrAccountNo());
        assertEquals(exchangeTest.get("tr_account_name"), exchange.getTrAccountName());
    }


}