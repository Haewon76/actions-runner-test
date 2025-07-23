package com.cashmallow.api.interfaces.aml.complyadvantage.client;

import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCreateCustomerRequest;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCustomerCasesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "complyadvantageBizClient",
        url = "${complyadvantage.url}",
        configuration = ComplyadvantageBizFeignConfig.class
)
public interface ComplyadvantageBizClient {

    @PostMapping("/v2/workflows/sync/create-and-screen")
    Map<String, Object> createCustomer(@RequestBody ComplyAdvantageCreateCustomerRequest createCustomerRequest);

    @GetMapping("/v2/cases")
    ComplyAdvantageCustomerCasesResponse getCustomerCase(@RequestParam("customer.identifier") String customerId);
}
