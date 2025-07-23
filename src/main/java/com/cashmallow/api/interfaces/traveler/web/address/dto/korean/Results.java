package com.cashmallow.api.interfaces.traveler.web.address.dto.korean;

import lombok.Data;

import java.util.List;

@Data
public class Results {
    private Common common;
    private List<JusoKoreanItem> juso;
}