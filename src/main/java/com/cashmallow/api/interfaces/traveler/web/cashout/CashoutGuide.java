package com.cashmallow.api.interfaces.traveler.web.cashout;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CashoutGuide(
        @JsonProperty("text_guide")
        String textGuide,
        @JsonProperty("html_guide")
        String htmlGuide,

        List<CashoutAgency> agencies
) {
}
