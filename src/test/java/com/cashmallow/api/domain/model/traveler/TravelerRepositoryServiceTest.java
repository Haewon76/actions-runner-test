package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.config.EnableDevLocal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@EnableDevLocal
@SpringBootTest
class TravelerRepositoryServiceTest {

    @Autowired
    TravelerRepositoryService travelerRepositoryService;

    @Test
    public void MYBATIS_PARAMS_TEST() {
        travelerRepositoryService.getTravelerImages(991844L);
    }

}