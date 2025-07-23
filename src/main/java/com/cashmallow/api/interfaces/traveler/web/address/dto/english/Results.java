package com.cashmallow.api.interfaces.traveler.web.address.dto.english;

import lombok.Data;

import java.util.List;

@Data
public class Results {
    private Common common;
    private List<JusoEnglishItem> juso;
}