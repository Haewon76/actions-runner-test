package com.cashmallow.api.interfaces.global.dto;

public record TravelerEkycUpdateDto(
        Long travelerId, // traveler id
        String sex, // 성별
        String birthDate, // 생년월일
        String certificationNumber, // 개인번호
        String hanjaFirstName, // [성명]한자 성
        String hanjaLastName, // [성명]한자 이름
        String enFirstName, // [성명]영문 성
        String enLastName, // [성명]영문 성
        String accountFirstName, // [성명]계좌명의 성
        String accountLastName // [성명]계좌명의 이름
) {
}
