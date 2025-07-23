package com.cashmallow.api.interfaces.slack.web.model;

import lombok.Data;

@Data
public class EmailWebhook {
    private Payload payload;
    private String event;
    private String uuid;
    private Object timestamp;
}