package com.cashmallow.api.interfaces.mallowlink.common;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class MallowlinkServiceImplTest {

    @Autowired
    MallowlinkServiceImpl mallowlinkService;

    @Disabled
    @Test
    void isHealth() {
        boolean isHealth = mallowlinkService.isHealth();
        log.debug("health={}", isHealth);
    }

    @Disabled
    @Test
    void getScbCountIncreaseAndGet() {
        // given
        long scbCountIncreaseAndGet = mallowlinkService.getScbCountIncreaseAndGet();
        log.debug("scbCountIncreaseAndGet={}", scbCountIncreaseAndGet);

        // when
        long scbCountIncreaseAndGet2 = mallowlinkService.getScbCountIncreaseAndGet();

        // then
        Assertions.assertThat(scbCountIncreaseAndGet2).isEqualTo(scbCountIncreaseAndGet + 1);

    }
}