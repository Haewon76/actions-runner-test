package com.cashmallow.api.interfaces.authme.dto;

/*
  "documentType": "IDCARD",
  "documentCountry": "HKG",
  "details": {
    "country": "",
    "dateOfBirth": "",
    "dateOfIssue": "",
    "documentNumber": "",
    "documentType": "",
    "expiryDate": "",
    "gender": "",
    "name": " ",
    "nationality": ""
  }
*/
public record AuthMeWebhookVerification(
        String documentType,
        String documentCountry,
        String confidence, // FaceCompare confidence e.g. 0.08491
        AuthMeWebhookVerificationDetails details
) {

    public boolean isPassport() {
        return "PASSPORT".equals(documentType);
    }

    public boolean isIdCard() {
        return "IDCARD".equals(documentType);
    }

    public record AuthMeWebhookVerificationDetails(
            String country,
            String dateOfBirth,
            String dateOfIssue,
            String documentNumber,
            String documentType,
            String expiryDate,
            String gender,
            String name,
            String nationality
    ) {
    }
}
