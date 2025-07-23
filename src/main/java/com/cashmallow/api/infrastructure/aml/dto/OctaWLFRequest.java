package com.cashmallow.api.infrastructure.aml.dto;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


@Getter
public class OctaWLFRequest {

    private final String userId; // 캐시멜로 유저ID
    private final String remittanceId; // 캐시멜로 유저ID
    private final String senderName; // 송금인
    private final String receiverName; // 수취인
    private final String requestAdminName; // 요청한 어드민 명
    private final String senderBirthDate;  // 송금인 생년월일 YYYYMMDD
    private final String senderCountryCd;  // 송금인 국가코드 2byte
    private final String receiverBirthDate;  // 송금인 생년월일 YYYYMMDD
    private final String receiverCountryCd;  // 송금인 국가코드 2byte

    public OctaWLFRequest(String userId,
                          String remittanceId,
                          String senderFirstName,
                          String senderLastName,
                          String receiverFirstName,
                          String receiverLastName,
                          String requestAdminName,
                          String senderBirthDate,
                          String senderCountryCd,
                          String receiverBirthDate,
                          String receiverCountryCd,
                          String fromCountryCode
    ) {
        this.userId = "HK".equalsIgnoreCase(fromCountryCode) ? "CM" + userId : fromCountryCode + userId;
        this.senderName = (senderFirstName + " " + senderLastName).toUpperCase(); // 옥타에서는 대문자로 검색해야 검색됨
        this.requestAdminName = requestAdminName;
        this.senderBirthDate = senderBirthDate;
        this.senderCountryCd = senderCountryCd;

        if (StringUtils.isNotBlank(receiverFirstName) && StringUtils.isNotBlank(receiverLastName)) {
            this.remittanceId = remittanceId;
            this.receiverName = (receiverFirstName + " " + receiverLastName).toUpperCase(); // 옥타에서는 대문자로 검색해야 검색됨
            this.receiverBirthDate = receiverBirthDate;
            this.receiverCountryCd = receiverCountryCd;
        } else {
            this.remittanceId = null;
            this.receiverName = null;
            this.receiverBirthDate = null;
            this.receiverCountryCd = null;
        }
    }
}
