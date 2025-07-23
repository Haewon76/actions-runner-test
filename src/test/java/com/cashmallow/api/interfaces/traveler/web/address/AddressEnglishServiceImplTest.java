package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.common.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class AddressEnglishServiceImplTest {

    @Autowired
    AddressEnglishServiceImpl addressEnglishService;

    @Autowired
    JsonUtil jsonUtil;

    @Disabled
    @Test
    void getSearchResultForGlobal() {
        // String address = "澳門高地鳥街";
        String address = "성원타워";
        // String address = "McDonald's at Costa";

        List<GoogleAddressResultResponse> searchResultForGlobal = addressEnglishService.getSearchResultForGlobal(address);

        log.info("searchResultForGlobal:{}", searchResultForGlobal);
        // log.info("searchResultForGlobal:{}", jsonUtil.toJson(searchResultForGlobal));

    }
}