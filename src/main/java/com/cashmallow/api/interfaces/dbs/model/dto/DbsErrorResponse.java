package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DbsErrorResponse {

    private String code;

    private String message;

}
