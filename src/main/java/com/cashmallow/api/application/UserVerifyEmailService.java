package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.shared.CashmallowException;

public interface UserVerifyEmailService {

    void addEmailCertNum(String email, String code) throws CashmallowException;

    /**
     * Add verified email password
     *
     * @param emailTokenVerity
     */
    void addVerifiedEmailPassword(EmailTokenVerity emailTokenVerity);

}