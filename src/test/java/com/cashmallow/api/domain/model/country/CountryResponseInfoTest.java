package com.cashmallow.api.domain.model.country;


import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountryResponseInfoTest {

    @Test
    void Alpha3로_CallingNumber가져오기() {
        // given
        String country = "KOR";

        // when
        String call = CountryInfo.callingPrefix.get(country);

        // then
        assertThat(call).isEqualTo("+82");
    }
}