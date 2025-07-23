package com.cashmallow.api.interfaces.global.dto;

public record TravelerUpdateDto(
        // section 1 - 기본정보
        Long travelerId,
        String hanjaFirstName,
        String hanjaLastName,
        String localFirstName,
        String localLastName,
        String enFirstName,
        String enLastName,
        String accountFirstName,
        String accountLastName,
        String certificationNumber,
        String callingCode,
        String phoneNumber,
        String addressFull // 전체 주소(일본어, 영문 주소로 변환 후 저장)
) {
}