package com.cashmallow.api.interfaces.traveler.web.address.dto.english;

import lombok.Data;

@Data
public class Common {
    private String errorMessage;
    private String countPerPage;
    private String errorCode;
    private String totalCount;
    private String currentPage;
}