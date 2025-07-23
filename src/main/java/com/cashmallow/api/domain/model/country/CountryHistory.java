package com.cashmallow.api.domain.model.country;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class CountryHistory extends Country {
    private Timestamp createdAt;
    private Long userId;
    private String ip;
}
