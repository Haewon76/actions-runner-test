package com.cashmallow.api.interfaces.authme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthMeCustomerDto {
    private List<AuthMeCustomerEventMediaImageResponse> medias = new ArrayList<>();
    private AuthMeCustomerEventResponse customerEvent;
    private String status; // Approved, Rejected

    public boolean isApproved() {
        return "Approved".equals(status);
    }

    public boolean isRejected() {
        return "Rejected".equals(status);
    }

    @AllArgsConstructor
    @Getter
    public static class AuthMeCustomerEventMediaImageResponse {
        private String content;
        private File file;
        private String base64Image;
    }
}
