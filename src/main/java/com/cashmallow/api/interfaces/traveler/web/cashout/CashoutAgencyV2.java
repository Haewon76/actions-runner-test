package com.cashmallow.api.interfaces.traveler.web.cashout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record CashoutAgencyV2(
        Long id,
        String type,
        String iconActiveUrl,
        String iconInActiveUrl,
        String title,
        String description,
        String atmUrl,
        boolean active,
        List<Guide> guides
) implements CashAgency {
    // Guide.of(guideUrl, processTextGuide(textGuide))
    public static CashoutAgencyV2 of(Long id,
                                     String type,
                                     String iconActiveUrl,
                                     String iconInActiveUrl,
                                     String title,
                                     String description,
                                     String atmUrl,
                                     boolean active,
                                     List<Guide> guides) {
        return new CashoutAgencyV2(
                id,
                type,
                iconActiveUrl,
                iconInActiveUrl,
                title,
                description,
                atmUrl,
                active,
                guides
        );
    }

    public static List<String> processTextGuide(String textGuide) {
        return Arrays.stream(textGuide.split("[\n♥]"))
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", ""))
                .collect(Collectors.toList());
    }

    public record Guide(
            String title,
            String guideTitle,
            String description,
            String imageUrl,
            String linkUrl,
            String linkText,
            List<String> textGuides
    ) {
        public static Guide of(String title,
                               String guideTitle,
                               String description,
                               String imageUrl,
                               String guideUrl,
                               String linkText,
                               List<String> textGuides) {
            return new Guide(title, guideTitle, description, imageUrl, guideUrl, linkText, textGuides);
        }

        public static Guide of(String title,
                               String guideTitle,
                               String description,
                               String guideUrl,
                               String linkText,
                               List<String> textGuides) {
            return new Guide(title, guideTitle, description, "", guideUrl, linkText, textGuides);
        }
    }
}