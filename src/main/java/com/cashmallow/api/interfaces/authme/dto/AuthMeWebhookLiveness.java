package com.cashmallow.api.interfaces.authme.dto;

/*
{
  "data" : {
    "id" : "3a12826f-d152-676e-15c9-b9867eb1f6fd",
    "status" : "Rejected",
    "code" : "IDT200",
    "source" : "",
    "verification" : { }
  },
  "type" : "IdentityVerification.Liveness",
  "customerId" : "HK985738",
  "timestamp" : "1715576114363",
  "event" : "default"
}
*/
public record AuthMeWebhookLiveness(
        AuthMeWebhookData data,
        String customerId
) {
    public record AuthMeWebhookData(
            String id,
            String status,
            String code,
            String source,
            AuthMeWebhookVerification verification
    ) {
    }

    public boolean isApproved() {
        return "Approved".equals(data.status);
    }
}
