package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveUser
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveUser {

    public enum InactiveType {
        DEL,     // Deleted user account (회원 탈퇴)
        DOR      // Dormant user account (개인정보 유효기간제에 의한 휴면 : 1년간 로그인 기록 없는 경우)
    }

    private Long id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePhoto;
    private InactiveType inactiveType;
    private String phoneNumber;
    private Timestamp createdDate;

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

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public InactiveType getInactiveType() {
        return inactiveType;
    }

    public void setInactiveType(InactiveType inactiveType) {
        this.inactiveType = inactiveType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
