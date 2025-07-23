package com.cashmallow.api.interfaces.openbank.dto.client;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class OpenbankTokenResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
    private String scope;
    private String userSeqNo;

    // error message
    private String rspCode;
    private String rspMessage;

    @Override
    public String toString() {
        return "OpenbankTokenResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", scope='" + scope + '\'' +
                ", userSeqNo='" + userSeqNo + '\'' +
                ", rspCode='" + rspCode + '\'' +
                ", rspMessage='" + rspMessage + '\'' +
                '}';
    }

    public boolean isSuccess() {
        // todo 성공 코드는 없나?
        return StringUtils.isBlank(rspCode);
    }

    public boolean isFail() {
        return !isSuccess();
    }
}
