package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.terms.TermsType;
import lombok.Data;

import java.util.List;

@Data
public class UserRegV2VO {
    private String login;                   // 사용자 아이디
    private String password;                // 비밀번호
    private String firstName;              // 이름
    private String lastName;               // 성
    private String email;                   // 이메일
    private String allowRecvEmail;        // E-Mail 수신 여부
    private String langKey;                // 가입자 사용 언어
    private String profilePhoto;           // 사진 저장 경로
    private String birthDate;              // 생일
    private String cls;                     // 가입자 구분
    private String country;                 // 국가코드
    private String recommenderEmail;       // 내 추천인 E-Mail
    private String phoneNumber;            // 핸드폰 번호
    private String phoneCountry;            // 핸드폰 번호 서비스 국가
    private List<TermsType> termsTypeList;  // 동의한 약관 리스트
}
