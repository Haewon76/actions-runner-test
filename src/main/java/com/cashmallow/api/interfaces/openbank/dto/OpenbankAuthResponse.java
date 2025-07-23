package com.cashmallow.api.interfaces.openbank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenbankAuthResponse {
    private final String requestUrl;
    private final Header header;

    @Data
    public static class Header {
        private final String userSeqNoKey = "Kftc-Bfop-UserSeqNo";
        private final String userSeqNoValue;
        private final String userCiKey = "Kftc-Bfop-UserCI";
        private final String userCiValue;
        private final String accessTokenKey = "Kftc-Bfop-AccessToken";
        private final String accessTokenValue;
    }
}
