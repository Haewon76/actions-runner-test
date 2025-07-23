package com.cashmallow.api.domain.model.traveler;


import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.traveler.enums.ApprovalType;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.interfaces.authme.dto.AuthmeEvent;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class TravelerRequestSender {

    private Long id;
    private String localFirstName;
    private String localLastName;
    private String enFirstName;
    private String enLastName;
    private String hanjaFirstName;
    private String hanjaLastName;
    private String accountFirstName;
    private String accountLastName;
    private String email;
    private String birthDate;
    private String expDate;
    private Double faceRate;
    private String imageIntegrity; // 신분증 위조방지 여부 Y,N
    private String visualAuthenticity; // 실제 사람인지 여부 Y,N
    private LocalDateTime lastLoginTime;
    private String phoneNumber; // 01012345678
    private String callingCode; // 82
    private boolean domestic; // 내국인, 외국인
    private String nationality; // 국적
    private boolean eddUser; // EDD 등록 여부
    private String certificationOk; // 최종 승인에 대한 값(Y/N/R)
    private String certificationNumber;
    private CertificationType certificationType;
    private LocalDateTime certificationOkDate;  // 최근 승인에 대한 값
    private String addressCountry; // 국가코드 Korea
    private String addressCity; // Seoul
    private String address; // 일본어 전체 주소
    private String addressEn; // 영문 전체 주소
    private String zipCode; // 우편번호
    private String job;
    private String sex;
    private String fundPurpose;
    private String fundSource;
    private ApprovalType approvalType;

    public TravelerRequestSender(@NotNull User user,
                                 @NotNull Traveler traveler,
                                 @NotNull AuthmeEvent authmeEvent,
                                 @NotNull String identificationNumber) {
        this.id = traveler.getId();
        this.localFirstName = traveler.getLocalFirstName();
        this.localLastName = traveler.getLocalLastName();
        this.enFirstName = traveler.getEnFirstName();
        this.enLastName = traveler.getEnLastName();
        this.hanjaFirstName = traveler.getHanjaFirstName();
        this.hanjaLastName = traveler.getHanjaLastName();
        this.email = user.getEmail();
        this.birthDate = user.getBirthDate();
        this.expDate = traveler.getPassportExpDate();
        try {
            this.faceRate = authmeEvent.customerEvent().getDocument().faceMatchScore();
        } catch (Exception ignore) {
            this.faceRate = 0.0;
        }
        try {
            this.imageIntegrity = authmeEvent.customerEvent().getDocument().isImageIntegrity(); // 이미지 무결성
        } catch (Exception ignore) {
            this.imageIntegrity = "";
        }

        try {
            this.visualAuthenticity = authmeEvent.customerEvent().getDocument().isVisualAuthenticity(); // 시각적 진위여부
        } catch (Exception ignore) {
            this.visualAuthenticity = "";
        }

        if (user.getLastLoginTime() != null) {
            this.lastLoginTime = user.getLastLoginTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        this.phoneNumber = user.getPhoneNumber();

        try {
            this.callingCode = Country3.valueOf(user.getPhoneCountry().toUpperCase()).getCalling().replace("+", "");
        } catch (Exception ignored) {
        }

        // 외국인 전용
        this.domestic = true;
        this.nationality = "JPN";
        if (CertificationType.RESIDENCE_CARD.equals(traveler.getCertificationType()) ||
                CertificationType.SPECIAL_RESIDENT_CERTIFICATE.equals(traveler.getCertificationType())) {
            this.domestic = false;
            this.nationality = traveler.getPassportCountry();
        }

        this.eddUser = traveler.isEddUser();
        this.certificationOk = traveler.getCertificationOk(); // 최종 승인에 대한 값(Y/N/R)
        this.certificationNumber = identificationNumber;
        this.certificationType = traveler.getCertificationType();
        this.certificationOkDate = ObjectUtils.isEmpty(traveler.getCertificationOkDate()) ? null : traveler.getCertificationOkDate().toLocalDateTime();  // 최근 승인에 대한 값
        this.addressCountry = traveler.getAddressCountry(); // 국가코드 Korea
        this.addressCity = traveler.getAddressCity(); // Seoul
        this.address = traveler.getAddress(); // 일본어 전체 주소
        this.addressEn = traveler.getAddressEn(); // 영문 전체 주소
        this.zipCode = traveler.getZipCode(); // 우편번호
        this.job = traveler.getJob().name();
        this.sex = traveler.getSex().name();
        this.fundPurpose = traveler.getFundPurpose();
        this.fundSource = traveler.getFundSource();
        this.approvalType = traveler.getApprovalType();
        String[] accountName = StringUtils.isNoneEmpty(traveler.getAccountName()) ? traveler.getAccountName().split(" ") : new String[]{"", ""};
        this.accountLastName = accountName.length == 2 ? accountName[0] : "";
        this.accountFirstName = accountName.length == 2 ? accountName[1] : "";
    }

    public TravelerRequestSender(User user,
                                 Traveler traveler,
                                 String identificationNumber) {
        this.id = traveler.getId();
        this.localFirstName = traveler.getLocalFirstName();
        this.localLastName = traveler.getLocalLastName();
        this.enFirstName = traveler.getEnFirstName();
        this.enLastName = traveler.getEnLastName();
        this.hanjaFirstName = traveler.getHanjaFirstName();
        this.hanjaLastName = traveler.getHanjaLastName();
        this.email = user.getEmail();
        this.birthDate = user.getBirthDate();
        this.expDate = traveler.getPassportExpDate();

        if (user.getLastLoginTime() != null) {
            this.lastLoginTime = user.getLastLoginTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        this.phoneNumber = user.getPhoneNumber();

        try {
            this.callingCode = Country3.valueOf(user.getPhoneCountry().toUpperCase()).getCalling().replace("+", "");
        } catch (Exception ignored) {
        }

        // 외국인 전용
        this.domestic = true;
        this.nationality = "JPN";
        if (CertificationType.RESIDENCE_CARD.equals(traveler.getCertificationType()) ||
                CertificationType.SPECIAL_RESIDENT_CERTIFICATE.equals(traveler.getCertificationType())) {
            this.domestic = false;
            this.nationality = traveler.getPassportCountry();
        }

        this.eddUser = traveler.isEddUser();
        this.certificationOk = traveler.getCertificationOk(); // 최종 승인에 대한 값(Y/N/R)
        this.certificationNumber = identificationNumber;
        this.certificationType = traveler.getCertificationType();
        this.certificationOkDate = ObjectUtils.isEmpty(traveler.getCertificationOkDate()) ? null : traveler.getCertificationOkDate().toLocalDateTime();  // 최근 승인에 대한 값
        this.addressCountry = traveler.getAddressCountry(); // 국가코드 Korea
        this.addressCity = traveler.getAddressCity(); // Seoul
        this.address = traveler.getAddress(); // 일본어 전체 주소
        this.addressEn = traveler.getAddressEn(); // 영문 전체 주소
        this.zipCode = traveler.getZipCode(); // 우편번호
        this.job = traveler.getJob().name();
        this.sex = traveler.getSex().name();
        this.fundPurpose = traveler.getFundPurpose();
        this.fundSource = traveler.getFundSource();
        this.approvalType = traveler.getApprovalType();
        String[] accountName = traveler.getAccountName().split(" ");
        this.accountLastName = accountName.length == 2 ? accountName[0] : "";
        this.accountFirstName = accountName.length == 2 ? accountName[1] : "";
    }

    public TravelerRequestSender(@NotNull User user) {
        this.id = user.getId();
        this.localFirstName = user.getFirstName();
        this.localLastName = user.getLastName();
        this.enFirstName = user.getFirstName();
        this.enLastName = user.getLastName();
        this.email = user.getEmail();
        this.birthDate = user.getBirthDate();
        this.faceRate = 0.0;
        this.imageIntegrity = "";

        this.visualAuthenticity = "";

        if (user.getLastLoginTime() != null) {
            this.lastLoginTime = user.getLastLoginTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        this.phoneNumber = user.getPhoneNumber();

        try {
            this.callingCode = Country3.valueOf(user.getPhoneCountry().toUpperCase()).getCalling().replace("+", "");
        } catch (Exception ignored) {
        }
    }
}

