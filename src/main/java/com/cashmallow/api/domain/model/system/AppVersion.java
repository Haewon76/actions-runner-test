package com.cashmallow.api.domain.model.system;

import com.cashmallow.api.domain.model.bundle.Bundle;

public class AppVersion {

    enum DeviceType {
        /**
         * Android APP
         */
        A,
        /**
         * iPhone APP
         */
        I
    }

    /**
     * Android : Application ID, IOS: Bundle ID
     */
    private String applicationId;

    /**
     * Android : 'A', iPhone: 'I'
     */
    private DeviceType deviceType;

    /**
     * Android : Version code, IOS: Bundle version
     */
    private Integer minVersionCode;

    /**
     * APP update guide message
     */
    private String message;

    // APP bundle information
    private String bundleUrl;
    private String bundleHashSha1;

    public String getApplicationId() {
        return applicationId;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public Integer getMinVersionCode() {
        return minVersionCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMinVersionCode(Integer minVersionCode) {
        this.minVersionCode = minVersionCode;
    }

    public String getBundleUrl() {
        return bundleUrl;
    }

    public void setBundleUrl(String bundleUrl) {
        this.bundleUrl = bundleUrl;
    }

    public String getBundleHashSha1() {
        return bundleHashSha1;
    }

    public void setBundleHashSha1(String bundleHashSha1) {
        this.bundleHashSha1 = bundleHashSha1;
    }
}
