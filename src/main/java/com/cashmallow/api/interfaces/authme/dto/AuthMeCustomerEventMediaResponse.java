package com.cashmallow.api.interfaces.authme.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public record AuthMeCustomerEventMediaResponse(
        List<AuthMeCustomerEventMediaResponseItem> items
) {
    public record AuthMeCustomerEventMediaResponseItem(
            String type,
            String mediaId,
            String documentType,
            String documentCountry
    ) {

        public String getType() {
            if(StringUtils.isEmpty(documentType)) {
                return type;
            }

            return documentType;
        }
    }
}
