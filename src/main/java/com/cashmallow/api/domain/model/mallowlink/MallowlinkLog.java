package com.cashmallow.api.domain.model.mallowlink;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class MallowlinkLog {
    private Long id;
    private String method;
    private String request;
    private String status;
    private String response;
    private Long elapsedTime;
    private ZonedDateTime createdAt;

    public MallowlinkLog(String method, String request, int status, String response, Long elapsedTime) {
        this.method = method;
        this.request = request;
        this.status = String.valueOf(status);
        this.response = response;
        this.elapsedTime = elapsedTime;
    }

}
