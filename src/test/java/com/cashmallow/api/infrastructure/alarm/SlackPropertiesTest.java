package com.cashmallow.api.infrastructure.alarm;

import com.cashmallow.common.JsonUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled
class SlackPropertiesTest {

    @Autowired
    private SlackProperties slackProperties;
    @Autowired
    private JsonUtil jsonUtil;

    @Test
    void 프로퍼티_로드() {
        System.out.println("slackProperties = " + jsonUtil.toJsonPretty(slackProperties));
    }

}