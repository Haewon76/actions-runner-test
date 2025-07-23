package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.shared.Const;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessToken {

    private String login;
    private String token;
    private Timestamp accessTime;
    private Long userId = Const.NO_USER_ID;
    private Long travelerId = Const.NO_USER_ID;
    private Long storekeeperId = Const.NO_USER_ID;
    private String auths;
    private String instanceId;

    //    public AccessToken(String login, long userId, long travelerId, long storekeeperId, String auths, String instanceId) {
    //        this.accessTime = CommDateTime.getCurrentDateTime();
    //        this.login = login;
    //        this.userId = userId;
    //        this.travelerId = travelerId;
    //        this.storekeeperId = storekeeperId;
    //        this.auths = auths;
    //        this.instanceId = instanceId;
    //    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Timestamp accessTime) {
        this.accessTime = accessTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public Long getStorekeeperId() {
        return storekeeperId;
    }

    public void setStorekeeperId(Long storekeeperId) {
        this.storekeeperId = storekeeperId;
    }

    public String getAuths() {
        return auths;
    }

    public void setAuths(String auths) {
        this.auths = auths;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }


}
