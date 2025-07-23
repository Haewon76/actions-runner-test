package com.cashmallow.api.interfaces.authme;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "authMeApiClient",
        url = "${authme.urlApi}",
        configuration = AuthMeApiClientConfig.class
)
public interface AuthMeApiClient {

    @GetMapping("/api/identity-verification/v1/customers/{travelerId}/events/{eventName}")
    String getCustomerEvent(@RequestHeader("Authorization") String authorization,
                            @PathVariable String travelerId,
                            @PathVariable String eventName);

    @GetMapping("/api/identity-verification/v1/customers/{travelerId}/events/{eventName}/media")
    String getCustomerEventMedia(@RequestHeader("Authorization") String authorization,
                                 @PathVariable String travelerId,
                                 @PathVariable String eventName);

    @GetMapping("/api/identity-verification/v1/customers/{travelerId}/events/{eventName}/media/{mediaId}")
    String getCustomerEventMediaImage(@RequestHeader("Authorization") String authorization,
                                      @PathVariable String travelerId,
                                      @PathVariable String mediaId,
                                      @PathVariable String eventName);
}
