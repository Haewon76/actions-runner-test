package com.cashmallow.api.interfaces.slack.web.model;

import lombok.Data;

@Data
public class Message {
    private String spamStatus;
    private String subject;
    private String messageId;
    private String from;
    private Integer id;
    private String to;
    private Object tag;
    private String token;
    private String direction;
    private Object timestamp;
}