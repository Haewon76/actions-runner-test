package com.cashmallow.api.domain.model.terms;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TermsType {
    TERMS("TERMS_AGREE_VIEW_PAGE_SERVICE"),
    PRIVACY("TERMS_AGREE_VIEW_PAGE_PRIVACY"),
    TERMS_OPENBANK("TERMS_AGREE_VIEW_PAGE_TERMS_OPENBANK"),
    CLAIMS("TERMS_AGREE_VIEW_PAGE_CLAIMS"),
    TERMS_ATM("TERMS_AGREE_VIEW_PAGE_TERMS_ATM"),
    ;

    private final String viewTitle;

    public String getViewTitle(String countryCode) {
        if (this == CLAIMS && "004".equalsIgnoreCase(countryCode)) {
            return TERMS_ATM.name();
        }
        return viewTitle;
    }
}
