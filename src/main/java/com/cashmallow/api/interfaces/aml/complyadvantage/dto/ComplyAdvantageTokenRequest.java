package com.cashmallow.api.interfaces.aml.complyadvantage.dto;

public record ComplyAdvantageTokenRequest(String username,
                                          String password,
                                          String realm) {
}
