package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.system.AppVersion;
import com.cashmallow.api.domain.shared.CashmallowException;
import org.json.JSONObject;

public interface SystemService {

    JSONObject getSystemStatus() throws CashmallowException;

    AppVersion getAppVersion(String applicationId, String deviceType);

}