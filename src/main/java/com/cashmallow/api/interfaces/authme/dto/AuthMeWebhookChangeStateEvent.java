package com.cashmallow.api.interfaces.authme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
{
  "type": "IdentityVerification.ChangeStateEventV2",
  "customerId": "HK985738",
  "timestamp": 1715568427008,
  "event": "default",
  "data": {
    "id": "3a12202e-0747-05d8-ea59-d605c60699e8",
    "customerId": "HK985738",
    "event": "default",
    "state": "Rejected",
    "code": "IDT200",
    "verification": [
      {
        "type": "Liveness",
        "id": "3a1262a6-5078-2b06-169e-5f0de318a0ad",
        "state": "Rejected",
        "code": "IDT200",
        "verification": {
          "stages": [
            {}
          ]
        },
        "createTime": "2024-05-07T00:34:49.106317Z",
        "updateTime": "2024-05-07T00:45:12.748992Z"
      },
      {
        "type": "Identification",
        "id": "3a1281f3-877f-3558-b2df-b0aea215149d",
        "state": "Pending",
        "code": "IDP106",
        "source": "System",
        "verification": {
          "documentType": "IDCARD",
          "documentCountry": "HKG",
          "result": [
            {
              "key": "country",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "dateOfBirth",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "dateOfIssue",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "documentNumber",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "documentType",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "expiryDate",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "gender",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "name",
              "value": " ",
              "source": "OCR"
            },
            {
              "key": "nationality",
              "value": "",
              "source": "OCR"
            }
          ],
          "history": [
            {
              "key": "country",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "dateOfBirth",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "dateOfIssue",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "documentNumber",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "documentType",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "expiryDate",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "gender",
              "value": "",
              "source": "OCR"
            },
            {
              "key": "name",
              "value": " ",
              "source": "OCR"
            },
            {
              "key": "nationality",
              "value": "",
              "source": "OCR"
            }
          ],
          "additional": [
            {
              "event": "image_integrity",
              "state": "Error",
              "specificEvent": "HKG_IDCARD_FRONT"
            },
            {
              "event": "visual_authenticity",
              "state": "Error",
              "specificEvent": "HKG_IDCARD_FRONT"
            }
          ]
        },
        "createTime": "2024-05-13T02:27:24.060451Z",
        "updateTime": "2024-05-13T02:28:56.912474Z"
      },
      {
        "type": "Identification",
        "id": "3a128205-3d84-8a6c-fe0d-933f3c505e34",
        "state": "Approved",
        "source": "System",
        "verification": {
          "documentType": "PASSPORT",
          "documentCountry": "ALL",
          "result": [
            {
              "key": "gender",
              "value": "F",
              "source": "MRZ"
            },
            {
              "key": "idNumber",
              "value": "2154710V176278",
              "source": "MRZ"
            },
            {
              "key": "expiryDate",
              "value": "2024-04-15",
              "source": "MRZ"
            },
            {
              "key": "country",
              "value": "KOR",
              "source": "MRZ"
            },
            {
              "key": "documentNumber",
              "value": "M70689098",
              "source": "MRZ"
            },
            {
              "key": "dateOfBirth",
              "value": "1985-07-02",
              "source": "MRZ"
            },
            {
              "key": "documentType",
              "value": "PM",
              "source": "MRZ"
            },
            {
              "key": "nationality",
              "value": "KOR",
              "source": "MRZ"
            },
            {
              "key": "surname",
              "value": "LEE",
              "source": "MRZ"
            },
            {
              "key": "givenName",
              "value": "SUYEON",
              "source": "MRZ"
            }
          ],
          "history": [
            {
              "key": "gender",
              "value": "F",
              "source": "MRZ"
            },
            {
              "key": "idNumber",
              "value": "2154710V176278",
              "source": "MRZ"
            },
            {
              "key": "expiryDate",
              "value": "2024-04-15",
              "source": "MRZ"
            },
            {
              "key": "country",
              "value": "KOR",
              "source": "MRZ"
            },
            {
              "key": "documentNumber",
              "value": "M70689098",
              "source": "MRZ"
            },
            {
              "key": "dateOfBirth",
              "value": "1985-07-02",
              "source": "MRZ"
            },
            {
              "key": "documentType",
              "value": "PM",
              "source": "MRZ"
            },
            {
              "key": "nationality",
              "value": "KOR",
              "source": "MRZ"
            },
            {
              "key": "surname",
              "value": "LEE",
              "source": "MRZ"
            },
            {
              "key": "givenName",
              "value": "SUYEON",
              "source": "MRZ"
            }
          ]
        },
        "createTime": "2024-05-13T02:46:44.180779Z",
        "updateTime": "2024-05-13T02:47:06.933149Z"
      }
    ],
    "createTime": "2024-04-24T02:48:29.769584Z",
    "updateTime": "2024-05-13T02:47:07.331164Z"
  }
}
*/
public record AuthMeWebhookChangeStateEvent(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn, // 3600 초

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("scope")
        String scope
) {
}
