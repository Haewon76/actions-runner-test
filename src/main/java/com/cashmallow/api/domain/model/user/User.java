package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.notification.EmailVerityType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Locale;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    private Long id;
    private String login;
    @JsonIgnore
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private String allowRecvEmail;
    private Boolean activated;
    private String langKey;
    private String profilePhoto;
    private String profilePhotoUrl;
    private String birthDate;
    private String birthMonthDay;
    private String cls;
    private String country;
    private Long recommenderId;
    private Timestamp lastLoginTime;
    private Timestamp lastLogoutTime;
    private Long creator;
    private Timestamp createdDate;
    private Long lastModifier;
    private Timestamp lastModifiedDate;
    private Timestamp deactivatedDate;
    private String instanceId;
    private String deviceType;
    private String versionCode;
    private String agreeTerms;
    private String agreePrivacy;
    private String phoneNumber;
    private String phoneCountry;
    private String deviceOsVersion;
    private int loginFailCount;
    @Getter
    @Setter
    private String bundleVersion;
    private String newToken;

    @Getter
    @Setter
    private String enFirstName; // admin 조회때문에 traveler모델에서 가져옴
    @Getter
    @Setter
    private String enLastName; // admin 조회때문에 traveler모델에서 가져옴

    @JsonIgnore
    public String getNewToken() {
        return newToken;
    }

    @JsonIgnore
    public void setNewToken(String newToken) {
        this.newToken = newToken;
    }

    @JsonIgnore
    public EmailVerityType getEmailVerityType() {
        return EmailVerityType.findByType(loginFailCount);
    }

    @JsonIgnore
    public boolean isNotMatchedPassword(String hashPassword) {
        return !hashPassword.equalsIgnoreCase(passwordHash);
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public String getAgreeTerms() {
        return agreeTerms;
    }

    public void setAgreeTerms(String agreeTerms) {
        this.agreeTerms = agreeTerms;
    }

    public String getAgreePrivacy() {
        return agreePrivacy;
    }

    public void setAgreePrivacy(String agreePrivacy) {
        this.agreePrivacy = agreePrivacy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    @JsonIgnore
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAllowRecvEmail() {
        return allowRecvEmail;
    }

    public void setAllowRecvEmail(String allowRecvEmail) {
        this.allowRecvEmail = allowRecvEmail;
    }

    public Boolean isActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = StringUtils.isEmpty(profilePhoto) ? null : profilePhoto;
    }

    public void setProfilePhotoUrl(String cdnUrl, String profilePhoto) {
        this.profilePhotoUrl = StringUtils.isEmpty(profilePhoto) ? null : cdnUrl + "/PROFILE/" + profilePhoto;
    }

    // ObjectMapper 변환을 통해 사용중
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthMonthDay() {
        return birthMonthDay;
    }

    public void setBirthMonthDay(String birthMonthDay) {
        this.birthMonthDay = birthMonthDay;
    }

    public String getCls() {
        return cls;
    }

    @JsonIgnore
    public boolean isTraveler() {
        return "T".equalsIgnoreCase(cls);
    }

    @JsonIgnore
    public boolean isNotTraveler() {
        return !isTraveler();
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getCountry() {
        return country;
    }

    @JsonIgnore
    public CountryCode getCountryCode() {
        return CountryCode.of(country);
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getRecommenderId() {
        return recommenderId;
    }

    public void setRecommenderId(Long recommenderId) {
        this.recommenderId = recommenderId;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Timestamp getLastLogoutTime() {
        return lastLogoutTime;
    }

    public void setLastLogoutTime(Timestamp lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Long getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(Long lastModifier) {
        this.lastModifier = lastModifier;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Timestamp lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Timestamp getDeactivatedDate() {
        return deactivatedDate;
    }

    public void setDeactivatedDate(Timestamp deactivatedDate) {
        this.deactivatedDate = deactivatedDate;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneCountry() {
        return phoneCountry;
    }

    public void setPhoneCountry(String phoneCountry) {
        this.phoneCountry = phoneCountry;
    }

    public String getDeviceOsVersion() {
        return deviceOsVersion;
    }

    public void setDeviceOsVersion(String deviceOsVersion) {
        this.deviceOsVersion = deviceOsVersion;
    }

    /**
     * 가입 국가의 로케일
     */
    @JsonIgnore
    public Locale getCountryLocale() {
        CountryCode countryCode = this.getCountryCode();
        if (CountryCode.KR.equals(countryCode) || CountryCode.TW.equals(countryCode)) {
            return new Locale("ko");
        } else if (CountryCode.JP.equals(countryCode)) {
            return new Locale("ja");
        }

        return new Locale("en");
    }

    public String getCustomerId() {
        return getCountryCode().name() + getId();
    }
}
