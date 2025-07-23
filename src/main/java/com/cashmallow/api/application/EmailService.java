package com.cashmallow.api.application;

import javax.mail.MessagingException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Future;


public interface EmailService {

    /**
     * Send mail with only text
     *
     * @param emailTo
     * @param subject
     * @param text
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    void sendMail(String emailTo, String subject, String text) throws MessagingException, UnsupportedEncodingException;

    /**
     * Send mail with attached file
     *
     * @param emailTo
     * @param subject
     * @param text
     * @param attachedFile
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    Future<Boolean> sendMail(String emailTo, String subject, String text, File attachedFile) throws MessagingException, UnsupportedEncodingException;

}