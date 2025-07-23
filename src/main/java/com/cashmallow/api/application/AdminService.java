package com.cashmallow.api.application;

import com.cashmallow.api.domain.shared.CashmallowException;

import java.util.Locale;
import java.util.Map;

public interface AdminService {

    String loginAdmin(String userName, String password,
                      String instanceId, String deviceInfo, String ip,
                      Locale locale) throws CashmallowException;

}

//testestsetsetsetset