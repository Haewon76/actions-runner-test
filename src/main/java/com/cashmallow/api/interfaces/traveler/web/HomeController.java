package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.interfaces.terms.TermsHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final TermsHistoryService termsHistoryService;


    /**
     * Get the terms of service for traveler
     *
     * @param iso3166
     * @return
     */
    @GetMapping(value = "/traveler/terms-of-service")
    public String getTermsOfService(@RequestParam String iso3166) {
        return termsHistoryService
                .getRecentVersionHistoryPath(TermsType.TERMS, iso3166)
                .replaceFirst("/html/", "");
    }

    /**
     * Get the privacy policy for traveler
     *
     * @param iso3166
     * @return
     */
    @GetMapping(value = "/traveler/privacy-policy")
    public String getPrivacyPolicy(@RequestParam String iso3166) {
        return termsHistoryService
                .getRecentVersionHistoryPath(TermsType.PRIVACY, iso3166)
                .replaceFirst("/html/", "");
    }

    /**
     * Get advertisement for traveler(Replaced with logo until AD)
     *
     * @param locale
     * @return
     */
    @GetMapping(value = "/traveler/ad")
    public String getAdvertisement(@RequestParam String locale) {
        Set<String> lang = Set.of("en", "ko", "zh", "ja");
        if (lang.contains(locale)) {
            return "advertisement/" + locale.toLowerCase();
        }
        log.info("invalid lang={}", locale);
        return "advertisement/en";
    }

    @GetMapping(value = "/")
    public String index() {
        return "redirect:https://cashmallow.com";
    }


    @GetMapping(value = "/health")
    public String health() {
        return "error/200";
    }

    @GetMapping(value = "/500")
    public String error500() {
        return "error/5xx";
    }
}
