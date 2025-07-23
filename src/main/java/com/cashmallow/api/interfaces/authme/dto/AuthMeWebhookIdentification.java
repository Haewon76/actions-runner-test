package com.cashmallow.api.interfaces.authme.dto;

/*
{
  "data": {
    "id": "3a126dcf-bc17-aec5-649a-7f4277c0ba73",
    "status": "Approved",
    "source": "System",
    "verification": {
      "documentType": "PASSPORT",
      "documentCountry": "ALL",
      "details": {
        "surname": "AEVARSDOTTIR",
        "sex": "",
        "personalNumber": "1212121239",
        "optionalData": "",
        "nationality": "ISL",
        "givenName": "THURIDUR OESP",
        "expiryDate": "2031-03-10",
        "documentType": "PA",
        "documentNumber": "A3536444",
        "country": "ISL",
        "birthDate": "2012-12-12"
      }
    }
  },
  "type": "IdentityVerification.Identification",
  "customerId": "HK986414",
  "timestamp": 1715229364305,
  "event": "default"
}

{
  "data": {
    "id": "3a1281f3-877f-3558-b2df-b0aea215149d",
    "status": "Pending",
    "code": "IDP106",
    "source": "System",
    "verification": {
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
    }
  },
  "type": "IdentityVerification.Identification",
  "customerId": "HK985738",
  "timestamp": 1715567337187,
  "event": "default"
}
*/
public record AuthMeWebhookIdentification(
        AuthMeWebhookData data,
        String type,
        String customerId,
        String timestamp,
        String event
) {
    public record AuthMeWebhookData(
            String id,
            String status,
            String code,
            String source,
            AuthMeWebhookVerification verification
    ) {
    }

    public boolean isIdCard() {
        return "IDCARD".equals(data.verification.documentType());
    }

    public boolean isPassport() {
        return "PASSPORT".equals(data.verification.documentType());
    }

    public boolean isApproved() {
        return "Approved".equals(data.status);
    }

    // public boolean hasCertificationData() {
    //     return "Approved".equals(data.status) || "Pending".equals(data.status);
    //     // 	Rejected - "type" : "IdentityVerification.ChangeState",  "type" : "IdentityVerification.FaceCompare"
    // }

    public Long getTravelerId() {
        return Long.parseLong(customerId.substring(2));
    }
}