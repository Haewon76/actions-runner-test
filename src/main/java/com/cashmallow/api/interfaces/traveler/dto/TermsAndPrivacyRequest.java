package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.terms.TermsType;
import lombok.Data;

import java.util.List;

@Data
public class TermsAndPrivacyRequest {
    private final List<TermsType> termsTypeList;
}