package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.interfaces.traveler.web.address.dto.JusoResponse;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.config.EnableDevLocal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@EnableDevLocal
class AddressJapanServiceImplTest {

    @Autowired
    AddressJapanServiceImpl addressJapanService;

    @Autowired
    JsonUtil jsonUtil;

    @Test
    void getAddress_VALID() {
        List<JusoResponse> address = addressJapanService.getAddress("453-0809");
        System.out.println("address = " + jsonUtil.toJsonPretty(address));
    }

    // @Test
    // void getAddress_INVALID() {
    //     List<JusoResponse> address = addressJapanService.getAddress("453-08091");
    //     System.out.println("address = " + jsonUtil.toJsonPretty(address));
    // }
}