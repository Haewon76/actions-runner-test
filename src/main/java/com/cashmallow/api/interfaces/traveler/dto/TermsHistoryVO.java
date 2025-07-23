package com.cashmallow.api.interfaces.traveler.dto;


import com.cashmallow.api.domain.model.terms.TermsType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TermsHistoryVO {

    private TermsType type;
    private String title;
    private String url;
    private Integer version;
    private Boolean required;
    private String startedAt;

    public TermsHistoryVO(TermsType type, String title, String url,
                          Integer version, Boolean required, String startedAt) {
        this.type = type;
        this.title = title;
        this.url = url;
        this.version = version;
        this.required = required;
        this.startedAt = startedAt;
    }
}
