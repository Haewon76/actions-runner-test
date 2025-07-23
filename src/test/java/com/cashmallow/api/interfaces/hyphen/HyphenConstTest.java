package com.cashmallow.api.interfaces.hyphen;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HyphenConstTest {

    @Autowired
    HyphenConst hyphenConst;

    @Test
    void 프로퍼티_가져오기() {
        // given

        // when
        String userId = hyphenConst.getUserId();
        String key = hyphenConst.getHKey();
        String url = hyphenConst.getURL();

        // then
        Assertions.assertThat(url).isEqualTo("https://api.hyphen.im/");
        Assertions.assertThat(userId).isEqualTo("drg513");
        Assertions.assertThat(key).isNotEmpty();
    }
}