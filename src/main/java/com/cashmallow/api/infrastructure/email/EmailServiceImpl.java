package com.cashmallow.api.infrastructure.email;

import com.cashmallow.api.application.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Future;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final String FROM_NAME = "Cashmallow";

    @Autowired
    private AsyncTaskExecutor asyncTaskExecutor;

    @Value("${mail.username}")
    private String emailId;


    @Autowired
    private JavaMailSender mailSender;


    @Override
    public void sendMail(String emailTo, String subject, String text) throws MessagingException, UnsupportedEncodingException {
        sendMail(emailTo, subject, text, null);
    }

    @Override
    public Future<Boolean> sendMail(String emailTo, String subject, String text, File attachedFile) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(emailId, FROM_NAME);
        helper.setTo(emailTo);
        helper.setSubject(subject);
        helper.setText(text, true);

        if (attachedFile != null && attachedFile.exists()) {
            helper.addAttachment(attachedFile.getName(), attachedFile);
        }

        return asyncTaskExecutor.submit(() -> {
            try {
                mailSender.send(message);
                logger.info("sendMail complete, emailTo: {}, subject: {}, attached: {}", emailTo, subject, attachedFile != null ? "O" : "X");
            } catch (Exception e) {
                logger.error("sendMail() error=" + e.getMessage(), e);
            }
            return true;
        });
    }
}


