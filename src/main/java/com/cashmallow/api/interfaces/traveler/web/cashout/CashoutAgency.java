package com.cashmallow.api.interfaces.traveler.web.cashout;

public record CashoutAgency(
        Long id,
        String type,
        String iconActiveUrl,
        String iconInActiveUrl,
        String title,
        String description,
        boolean active,
        String guideUrl,
        String textGuide,
        String htmlGuide
) implements CashAgency {
}