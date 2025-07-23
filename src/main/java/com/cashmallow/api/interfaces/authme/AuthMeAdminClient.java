package com.cashmallow.api.interfaces.authme;

import com.cashmallow.api.interfaces.authme.dto.AuthMeTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(
        name = "authMeClient",
        url = "${authme.url}",
        configuration = AuthMeClientConfig.class
)
public interface AuthMeAdminClient {

    @PostMapping(
            value = "/connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AuthMeTokenResponse getToken(
            @RequestPart(value = "grant_type") String grantType,
            @RequestPart(value = "scope") String scope,
            @RequestPart(value = "client_id") String clientId,
            @RequestPart(value = "client_secret") String clientSecret);
}
