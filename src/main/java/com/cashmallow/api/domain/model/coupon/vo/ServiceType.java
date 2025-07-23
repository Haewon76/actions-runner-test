package com.cashmallow.api.domain.model.coupon.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
    ALL("all"),
    EXCHANGE("exchange"),
    REMITTANCE("remittance");

    private static final List<ServiceType> exchangeList = new ArrayList<>();
    private static final List<ServiceType> remittanceList = new ArrayList<>();

    static {
        exchangeList.add(ALL);
        exchangeList.add(EXCHANGE);

        remittanceList.add(ALL);
        remittanceList.add(REMITTANCE);
    }

    private static final Map<String, ServiceType> stringToEnum = Stream.of(values())
            .collect(Collectors.toMap(ServiceType::getCode, e -> e));

    public static List<ServiceType> getDependencyList(ServiceType serviceType) {
        if (serviceType.equals(ServiceType.EXCHANGE)) {
            return exchangeList;
        } else if (serviceType.equals(ServiceType.REMITTANCE)) {
            return remittanceList;
        } else {
            return null;
        }
    }

    private final String code;

    public static ServiceType fromString(String code) {
        return stringToEnum.get(code);
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
