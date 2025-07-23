package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.inactiveuser.InactiveRemitReceiverAml;
import com.cashmallow.api.domain.model.inactiveuser.InactiveRemittance;
import com.cashmallow.api.domain.model.inactiveuser.InactiveRemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EncryptTypeHandlerForInactiveRemittanceTest {

    @Autowired
    private InactiveUserMapper inactiveUserMapper;


    private InactiveRemittance inactiveRemittance;

    private InactiveRemittanceTravelerSnapshot remittanceTravelerSnapshot;

    private InactiveRemitReceiverAml remitReceiverAml;


    public void setUp() {

        inactiveRemittance = new InactiveRemittance();
        inactiveRemittance.setId(20000000L);
        inactiveRemittance.setTravelerId(10000000L);
        inactiveRemittance.setReceiverPhoneNo("01099992222");
        inactiveRemittance.setReceiverFirstName("케시멜롱");
        inactiveRemittance.setReceiverLastName("멜롱멜롱");
        inactiveRemittance.setReceiverBankAccountNo("19929388");
        inactiveRemittance.setReceiverAddress("태국은행이쥬");
        inactiveRemittance.setReceiverAddressSecondary("상세주소쥬");
        inactiveRemittance.setCreatedDate(new Timestamp(new Date().getTime()));
        inactiveRemittance.setReceiverBirthDate("19860319");


        inactiveUserMapper.insertInactiveRemittance(inactiveRemittance);

        remittanceTravelerSnapshot = new InactiveRemittanceTravelerSnapshot();
        remittanceTravelerSnapshot.setRemitId(inactiveRemittance.getId());
        remittanceTravelerSnapshot.setTravelerId(inactiveRemittance.getTravelerId());
        remittanceTravelerSnapshot.setIdentificationNumber("1920392302");
        remittanceTravelerSnapshot.setAccountNo(inactiveRemittance.getReceiverBankAccountNo());
        remittanceTravelerSnapshot.setAccountName("한국은행");
        remittanceTravelerSnapshot.setAddress(inactiveRemittance.getReceiverAddress());
        remittanceTravelerSnapshot.setAddressSecondary(inactiveRemittance.getReceiverAddressSecondary());
        remittanceTravelerSnapshot.setPhoneNumber(inactiveRemittance.getReceiverPhoneNo());
        remittanceTravelerSnapshot.setCreatedDate(new Timestamp(new Date().getTime()));


        inactiveUserMapper.insertInactiveRemittanceTravelerSnapshot(remittanceTravelerSnapshot);


        remitReceiverAml = new InactiveRemitReceiverAml();

        remitReceiverAml.setAmlSearchId("1000000");
        remitReceiverAml.setRemitReceiverAmlId(100000L);
        remitReceiverAml.setTravelerId(inactiveRemittance.getTravelerId());
        remitReceiverAml.setReceiverFirstName(inactiveRemittance.getReceiverFirstName());
        remitReceiverAml.setReceiverLastName(inactiveRemittance.getReceiverLastName());
        remitReceiverAml.setBirthDate("20210327");
        remitReceiverAml.setCreatedDate(new Timestamp(new Date().getTime()));

        inactiveUserMapper.insertInactiveRemitReceiverAml(remitReceiverAml);


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveRemittanceList() {
        setUp();

        List<InactiveRemittance> inactiveRemittances = inactiveUserMapper.getInactiveRemittanceList(inactiveRemittance.getTravelerId());
        InactiveRemittance inactiveRemittanceTest = inactiveRemittances.get(0);


        assertEquals(inactiveRemittanceTest.getReceiverAddress(), inactiveRemittance.getReceiverAddress());
        assertEquals(inactiveRemittanceTest.getReceiverAddress(), inactiveRemittance.getReceiverAddress());
        assertEquals(inactiveRemittanceTest.getReceiverAddressSecondary(), inactiveRemittance.getReceiverAddressSecondary());
        assertEquals(inactiveRemittanceTest.getReceiverFirstName(), inactiveRemittance.getReceiverFirstName());
        assertEquals(inactiveRemittanceTest.getReceiverLastName(), inactiveRemittance.getReceiverLastName());
        assertEquals(inactiveRemittanceTest.getReceiverPhoneNo(), inactiveRemittance.getReceiverPhoneNo());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveRemittanceTravelerSnapshotList() {
        setUp();

        List<InactiveRemittanceTravelerSnapshot> inactiveRemittanceTravelerSnapshots = inactiveUserMapper.getInactiveRemittanceTravelerSnapshotList(inactiveRemittance.getTravelerId());
        InactiveRemittanceTravelerSnapshot inactiveRemittanceTravelerSnapshotTest = inactiveRemittanceTravelerSnapshots.get(0);

        assertEquals(inactiveRemittanceTravelerSnapshotTest.getAddress(), remittanceTravelerSnapshot.getAddress());
        assertEquals(inactiveRemittanceTravelerSnapshotTest.getAccountName(), remittanceTravelerSnapshot.getAccountName());
        assertEquals(inactiveRemittanceTravelerSnapshotTest.getAccountNo(), remittanceTravelerSnapshot.getAccountNo());
        assertEquals(inactiveRemittanceTravelerSnapshotTest.getPhoneNumber(), remittanceTravelerSnapshot.getPhoneNumber());
        assertEquals(inactiveRemittanceTravelerSnapshotTest.getAddressSecondary(), remittanceTravelerSnapshot.getAddressSecondary());

    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveRemitReceiverAml() {
        setUp();

        List<InactiveRemitReceiverAml> inactiveRemitReceiverAmls = inactiveUserMapper.getInactiveRemitReceiverAml(inactiveRemittance.getTravelerId());
        InactiveRemitReceiverAml inactiveRemitReceiverAmlTest = inactiveRemitReceiverAmls.get(0);

        assertEquals(inactiveRemitReceiverAmlTest.getReceiverFirstName(), remitReceiverAml.getReceiverFirstName());
        assertEquals(inactiveRemitReceiverAmlTest.getReceiverLastName(), remitReceiverAml.getReceiverLastName());

    }

}