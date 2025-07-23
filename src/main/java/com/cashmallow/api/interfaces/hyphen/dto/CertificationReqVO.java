package com.cashmallow.api.interfaces.hyphen.dto;


import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CertificationReqVO {
    private final CertificationType certificationType;
    private final String localName;
    private final String identificationNumber;
    private final String issueDate;
    private final String birthDay;
    private final String licenceNo;
    private final String serialNo;
}
