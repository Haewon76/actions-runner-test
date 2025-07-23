package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.inactiveuser.InactiveTraveler;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EncryptTypeHandlerForInactiveTravelerTest {

    @Autowired
    private InactiveUserMapper inactiveUserMapper;


    private InactiveTraveler traveler;


    public void setUp() {
        traveler = new InactiveTraveler();

        traveler.setId(100000L);
        traveler.setUserId(200000L);
        traveler.setAccountNo("01072661666");
        traveler.setAccountName("남궁석");
        traveler.setCreatedDate(new Timestamp(new Date().getTime()));
        traveler.setContactType(null);
        traveler.setContactId(null);
        traveler.setLocalFirstName("남궁석");
        traveler.setLocalLastName("test");
        traveler.setEnFirstName("seok");
        traveler.setEnLastName("namkoong");
        traveler.setIdentificationNumber("88ff2a7131746e93feac8f754aa90e01");
        traveler.setCertificationPhoto("fZwLFEoifW3tqVdKnXHk3187b16bc3a5");
        traveler.setAccountBankbookPhoto(null);
        traveler.setAddress("400 Gangseo-ro Gangseo-gu Seoul");
        traveler.setAddressPhoto(null);
        traveler.setAddressSecondary("gang nam gu ro ga ja");

        inactiveUserMapper.insertInactiveTraveler(traveler);


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveTraveler() {
        setUp();
        InactiveTraveler inactiveTravelerTest = inactiveUserMapper.getInactiveTraveler(traveler.getUserId());

        assertEquals(inactiveTravelerTest.getAccountNo(), traveler.getAccountNo());
        assertEquals(inactiveTravelerTest.getAccountName(), traveler.getAccountName());
        assertEquals(inactiveTravelerTest.getLocalFirstName(), traveler.getLocalFirstName());
        assertEquals(inactiveTravelerTest.getLocalLastName(), traveler.getLocalLastName());
        assertEquals(inactiveTravelerTest.getEnFirstName(), traveler.getEnFirstName());
        assertEquals(inactiveTravelerTest.getEnLastName(), traveler.getEnLastName());
        assertEquals(inactiveTravelerTest.getAddress(), traveler.getAddress());
        assertEquals(inactiveTravelerTest.getAddressSecondary(), traveler.getAddressSecondary());

    }

}