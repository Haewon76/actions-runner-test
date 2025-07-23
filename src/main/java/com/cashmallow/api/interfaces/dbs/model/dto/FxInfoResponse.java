package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class FxInfoResponse {

    private Map<String, Set<String>> currencies;

    private Set<String> supportedCurrencies;

    private List<Map<String, String>> banks;

}
