package com.cashmallow.api.interfaces.slack.web.model;

import lombok.Data;

@Data
public class Payload {
    private String ipAddress;
    private Message message;
    private String userAgent;
}