package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.enums.ApprovalType;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.interfaces.global.dto.TravelerEkycUpdateDto;
import com.cashmallow.api.interfaces.global.dto.TravelerUpdateDto;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerJpRequest;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerKrRequest;
import com.cashmallow.api.interfaces.traveler.dto.TravelersHkPassportDto;
import com.cashmallow.api.interfaces.traveler.dto.TravelersRequest;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

import static com.cashmallow.common.CommonUtil.removeNonNumeric;
import static com.cashmallow.common.CommonUtil.textToNormalize;

// 기능: traveler 테이블의 거의 대부분 내용.
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Traveler {

    // 암호화된 필드를 복호화 하는 처리
    public void decryptData(SecurityService securityService) {
        identificationNumber = securityService.decryptAES256(getIdentificationNumber());
    }

    public void updateGlobalTraveler(TravelerUpdateDto request,
                                     GoogleAddressResultResponse address,
                                     SecurityService securityService) {
        this.hanjaFirstName = request.hanjaFirstName();
        this.hanjaLastName = request.hanjaLastName();
        this.localFirstName = request.localFirstName();
        this.localLastName = request.localLastName();
        this.enFirstName = request.enFirstName();
        this.enLastName = request.enLastName();
        // JP 글로벌에서 넘어온 certificationNumber 값 암호화 처리
        this.identificationNumber = securityService.encryptAES256(request.certificationNumber());
        this.addressSecondary = "";
        this.address = request.addressFull();
        this.addressEn = address.getFullAddress();
        if (StringUtils.isNotBlank(address.getZipCode()) && address.getZipCode().length() <= 8) {
            this.zipCode = address.getZipCode();
        }
        this.addressCity = address.getCityName();
        this.accountName = request.accountLastName() + " " + request.accountFirstName();
    }

    public void updateGlobalTravelerEkyc(TravelerEkycUpdateDto request,
                                         SecurityService securityService) {
        // this.ex = request.expirationDate();
        this.sex = TravelerSex.valueOf(request.sex());
        this.birthDate = request.birthDate();
        this.identificationNumber = securityService.encryptAES256(request.certificationNumber());
        this.hanjaFirstName = request.hanjaFirstName();
        this.hanjaLastName = request.hanjaLastName();
        this.enFirstName = request.enFirstName();
        this.enLastName = request.enLastName();
        this.accountName = request.accountLastName() + " " + request.accountFirstName();
    }

    public enum TravelerSex {
        MALE,
        FEMALE
    }

    public enum VerificationType {
        /**
         * 신분인증(여권, 신분증)
         */
        CERTIFICATION,
        /**
         * 통장인증
         */
        BANKACCOUNT,
        ADDRESS_PHOTO,
    }

    private Long id;
    private Long userId;
    private String passportIssueDate;
    private String passportExpDate;
    private String identificationNumber;
    private CertificationType certificationType;

    private String passportCountry;
    private String certificationPhoto;

    private String hanjaFirstName;
    private String hanjaLastName;

    private String enFirstName;
    private String enLastName;
    private String localFirstName;
    private String localLastName;
    private String certificationOk;
    private Timestamp certificationOkDate;
    private String accountNo;
    private String accountName;

    private Long bankInfoId;
    private String bankCode;
    private String bankName;
    private String accountBankbookPhoto;
    private String AMLSearchId;
    private String addressCountry;
    private String addressCity;
    private String addressSecondary;
    private String addressPhoto;
    private String addressEn; // 영문으로 변환한 주소
    private String address; // 로컬 주소
    private String zipCode;

    private String needJpAccountRegister;

    private String birthDate;

    private String accountOk;
    private Timestamp accountOkDate;

    private String contactType;
    private String contactId;

    private String paygateMemberId;
    private String paygateKycStatus;
    private String paygateKycRefId;

    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;

    private Job job;

    private TravelerSex sex;

    private String fundPurpose;
    private String fundSource;
    private ApprovalType approvalType;

    private boolean eddUser;

    private double faceRate;
    private String imageIntegrity; // Y,N
    private String visualAuthenticity; // Y,N

    public String getKanjiFirstName() {
        return hanjaFirstName;
    }

    public String getKanjiLastName() {
        return hanjaLastName;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = textToNormalize(identificationNumber);
    }

    public void setAddress(String address) {
        this.address = textToNormalize(address);
    }

    public void setAddressSecondary(String addressSecondary) {
        this.addressSecondary = textToNormalize(addressSecondary);
    }

    public boolean isCertificationProgress() {
        return "W".equals(certificationOk) || "N".equals(certificationOk);
    }

    public boolean isBankAccountProgress() {
        return StringUtils.isNotEmpty(accountBankbookPhoto) && "N".equals(accountOk);
    }


    @JsonIgnore
    public void updateTraveler(TravelersRequest travelersRequest) {
        this.certificationType = CertificationType.ID_CARD;
        this.identificationNumber = travelersRequest.getIdentificationNumber();

        this.enFirstName = travelersRequest.getEnFirstName();
        this.enLastName = travelersRequest.getEnLastName();
        this.localFirstName = travelersRequest.getEnFirstName();
        this.localLastName = travelersRequest.getEnLastName();
        this.job = travelersRequest.getJob();
        this.sex = travelersRequest.getSex();
        this.fundPurpose = travelersRequest.getFundPurpose();
        this.fundSource = travelersRequest.getFundSource();
        this.approvalType = ApprovalType.NFC;

        this.accountOk = "N";
        this.certificationOk = "W"; // 어스미 승인/거절 까지 대기 상태

        if (travelersRequest instanceof TravelersHkPassportDto) {
            TravelersHkPassportDto hkPassportDto = (TravelersHkPassportDto) travelersRequest;
            this.certificationType = CertificationType.PASSPORT;
            this.localFirstName = hkPassportDto.getLocalFirstName();
            this.localLastName = hkPassportDto.getLocalLastName();
            this.passportCountry = hkPassportDto.getPassportCountry().name();
            this.passportIssueDate = hkPassportDto.getPassportIssueDate();
            this.passportExpDate = hkPassportDto.getPassportExpDate();
        }
    }

    @JsonIgnore
    public void updateTraveler(RegisterTravelerKrRequest registerRequest) {
        // traveler 세팅
        String[] addressParts = registerRequest.getAddress().split(",");
        String addressCity = addressParts[addressParts.length - 1].trim();

        this.certificationType = CertificationType.ID_CARD;
        this.identificationNumber = registerRequest.getIdentificationNumber();

        this.enFirstName = registerRequest.getEnFirstName();
        this.enLastName = registerRequest.getEnLastName();
        this.localFirstName = registerRequest.getLocalName();
        this.localLastName = "";

        this.address = registerRequest.getAddress();
        this.addressCity = addressCity;
        this.addressCountry = "KOR";
        this.addressSecondary = registerRequest.getAddressSecondary();

        this.passportCountry = registerRequest.getPassportCountry();

        this.job = registerRequest.getJob();
        this.sex = registerRequest.getSex();
        this.fundPurpose = registerRequest.getFundPurpose();
        this.fundSource = registerRequest.getFundSource();
    }

    @JsonIgnore
    public void updateTraveler(RegisterTravelerJpRequest registerRequest) {
        this.certificationType = registerRequest.certificationType();

        // mynumber, driver license only
        this.hanjaFirstName = registerRequest.kanjiFirstName();
        this.hanjaLastName = registerRequest.kanjiLastName();

        this.localFirstName = registerRequest.localFirstName();
        this.localLastName = registerRequest.localLastName();
        this.enFirstName = registerRequest.enFirstName();
        this.enLastName = registerRequest.enLastName();
        this.identificationNumber = registerRequest.identificationNumber();
        // this.dateOfBirth = registerRequest.dateOfBirth(); 외부에서 처리
        // issueDate is null
        this.passportExpDate = registerRequest.expirationDate();
        this.job = registerRequest.job();

        this.accountName = registerRequest.accountLastName() + " " + registerRequest.accountFirstName();

        // 아래 메소드는 외부에서 세팅해주고 있음
        // travelerVo.setAddressSecondary("");
        // travelerVo.setAddress(request.address());
        // travelerVo.setAddressCity(addressEn.getCityName());
        // travelerVo.setEnAddress(addressEn.getFullAddress());
        // travelerVo.setZipCode(addressEn.getZipCode());

        this.addressCountry = "JPN";
        this.passportCountry = registerRequest.getPassportCountry() != null ? registerRequest.getPassportCountry().name() : null;

        this.sex = registerRequest.sex();
        this.fundPurpose = registerRequest.fundPurpose().name();
        this.fundSource = registerRequest.fundSource().name();
    }

    public boolean isPendingAuthme() {
        return "W".equals(certificationOk);
    }

    public String getZipCodeDigit() {
        if(StringUtils.isBlank(zipCode)) {
            return "000000";
        }
        return removeNonNumeric(zipCode); // traveler의 zipCode는 숫자만 포함하도록 처리
    }
}
