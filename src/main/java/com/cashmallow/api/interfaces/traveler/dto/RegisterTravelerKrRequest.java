package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

@Data
public class RegisterTravelerKrRequest {

    @NotNull
    private final CertificationType certificationType;

    @NotBlank
    private final String localName;

    @NotBlank
    private final String enFirstName;

    @NotBlank
    private final String enLastName;

    @NotBlank
    @Size(min = 13, max = 13)
    private final String identificationNumber; // 주민등록번호, 외국인 등록번호

    private final String issueDate; // 발급일자
    private final Job job;
    protected final String jobDetail;

    @NotBlank
    private final String address; // 주소 API 영문 도로명
    private final String addressSecondary; // 상세 주소

    private final String licenseNo; // 운전면허 번호
    private final String serialNo;  // 운전면허 일련번호

    private final String passportCountry; // 외국인등록증 국적

    protected final Traveler.TravelerSex sex;
    protected final String fundPurpose; // LIVINGEXPENSES, DONATE
    protected final String fundSource; // INTERESTINCOME, GIFTINCOME

    public Traveler.TravelerSex getSex() {
        return Optional.ofNullable(sex).orElse(Traveler.TravelerSex.MALE);
    }

    public String getFundPurpose() {
        return Optional.ofNullable(fundPurpose).orElse(null);
    }

    public String getFundSource() {
        return Optional.ofNullable(fundSource).orElse(null);
    }

    @Override
    public String toString() {
        return "RegisterTravelerKrRequest{" +
                "certificationType=" + certificationType +
                ", localName='" + localName + '\'' +
                ", enFirstName='" + enFirstName + '\'' +
                ", enLastName='" + enLastName + '\'' +
                ", job=" + job +
                ", jobDetail='" + jobDetail + '\'' +
                ", address='" + address + '\'' +
                ", addressSecondary='" + addressSecondary + '\'' +
                ", passportCountry='" + passportCountry + '\'' +
                '}';
    }
}
