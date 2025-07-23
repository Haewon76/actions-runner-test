package com.cashmallow.api.interfaces.mallowlink.currency;


import com.cashmallow.api.interfaces.authme.AuthMeClientConfig;
import com.cashmallow.api.interfaces.mallowlink.currency.dto.MallowlinkBoBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.currency.dto.MallowlinkCurrencyRateRequest;
import com.cashmallow.api.interfaces.mallowlink.currency.dto.MallowlinkCurrencyRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "MallowlinkBoCurrency",
        url = "${mallowlink.bank.url}/api",
        configuration = AuthMeClientConfig.class
)
public interface MallowlinkBoCurrency {

    @PostMapping("/currency-rate/search")
    MallowlinkBoBaseResponse<List<MallowlinkCurrencyRateResponse>> collectCurrency(@RequestBody MallowlinkCurrencyRateRequest request);

}
