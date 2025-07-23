package com.cashmallow.api.interfaces.aml.complyadvantage.client;

import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageTokenRequest;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "complyadvantageAuthClient",
        url = "${complyadvantage.url}",
        configuration = ComplyadvantageAuthFeignConfig.class
)
public interface ComplyadvantageAuthClient {

    @PostMapping("/v2/token")
    ComplyAdvantageTokenResponse getAccessToken(@RequestBody ComplyAdvantageTokenRequest complyAdvantageTokenRequest);
}
