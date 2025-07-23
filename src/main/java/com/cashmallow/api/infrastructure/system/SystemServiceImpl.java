package com.cashmallow.api.infrastructure.system;

import com.cashmallow.api.application.SystemService;
import com.cashmallow.api.domain.model.system.AppVersion;
import com.cashmallow.api.domain.model.system.SystemMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SystemServiceImpl implements SystemService {

    private static final Logger logger = LoggerFactory.getLogger(SystemServiceImpl.class);

    @Autowired
    private SystemMapper systemMapper;

    @Override
    public JSONObject getSystemStatus() throws CashmallowException {

        JSONObject result = new JSONObject();

        boolean isDBHealthy = systemMapper.isDBHealthy();

        if (isDBHealthy) {
            result.put("status", "OK");
        } else {
            throw new CashmallowException("DB connection failed.");
        }

        logger.info("getSystemStatus(): result={}", result);

        return result;
    }

    @Override
    public AppVersion getAppVersion(String applicationId, String deviceType) {
        Map<String, String> params = new HashMap<>();
        params.put("applicationId", applicationId);
        params.put("deviceType", deviceType);

        return systemMapper.getAppVersion(params);
    }
}
