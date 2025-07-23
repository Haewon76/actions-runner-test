package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EncryptTypeHandlerForInactiveUserTest {

    @Autowired
    private InactiveUserMapper inactiveUserMapper;

    private InactiveUser user;


    public void setUp() {
        user = new InactiveUser();
        user.setId(10000000L);
        user.setLogin("tiger10001ruukr");
        user.setFirstName("TOW");
        user.setLastName("TIGER");
        user.setEmail("tiger10001@ruu.kr");
        user.setProfilePhoto("C0OmmNx3rm0uAuVmuanhX1874af5a015");
        user.setInactiveType(InactiveUser.InactiveType.DEL);
        user.setPhoneNumber("+932580963");
        user.setCreatedDate(new Timestamp(new Date().getTime()));


        inactiveUserMapper.insertInactiveUser(user);


    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveUser() {
        setUp();

        InactiveUser inactiveUser = inactiveUserMapper.getInactiveUser(user.getId());

        assertEquals(user.getFirstName(), inactiveUser.getFirstName());
        assertEquals(user.getLastName(), inactiveUser.getLastName());
        assertEquals(user.getEmail(), inactiveUser.getEmail());
        assertEquals(user.getPhoneNumber(), inactiveUser.getPhoneNumber());
    }

    @Test
    @Transactional
    void TypeHandler_암복호화_getInactiveUserByLoginNInactiveType() {
        setUp();

        Map<String, Object> params = new HashMap<>();
        params.put("inactiveType", "DEL");
        params.put("login", user.getLogin());

        InactiveUser inactiveUser = inactiveUserMapper.getInactiveUserByLoginNInactiveType(params);

        assertEquals(user.getFirstName(), inactiveUser.getFirstName());
        assertEquals(user.getLastName(), inactiveUser.getLastName());
        assertEquals(user.getEmail(), inactiveUser.getEmail());
        assertEquals(user.getPhoneNumber(), inactiveUser.getPhoneNumber());
    }


}