package com.cashmallow.api.interfaces.authme.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record AuthMeCustomerResponseDocument(
        String documentType,
        String documentCountry,
        AuthMeCustomerResponseDocumentDetails details,
        double faceMatchScore,
        List<Label> labels,
        ZonedDateTime createTime,
        ZonedDateTime updateTime
) {

    private record Label(
            String event,
            String state,
            String specificEvent,
            String specificState
    ) {
    }

    public String isImageIntegrity() {
        if(labels == null || labels.isEmpty()) {
            return "";
        }
        return labels.stream().anyMatch(label -> "image_integrity".equalsIgnoreCase(label.event) && "Success".equalsIgnoreCase(label.state)) ? "Y" : "N";
    }

    public String isVisualAuthenticity() {
        if(labels == null || labels.isEmpty()) {
            return "";
        }
        return labels.stream().anyMatch(label -> "visual_authenticity".equalsIgnoreCase(label.event) && "Success".equalsIgnoreCase(label.state)) ? "Y" : "N";
    }
}
