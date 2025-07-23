package com.cashmallow.api.infrastructure.email;

import com.cashmallow.api.application.EmailService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@Disabled
class EmailServiceImplTest {

    @Autowired
    private EmailService emailService;

    @Test
    void 파일첨부없이_발송_테스트() throws MessagingException, IOException, ExecutionException, InterruptedException {
        Assertions.assertEquals(true, emailService.sendMail("jd@cashmallow.com", UUID.randomUUID().toString(), "Test", null).get());
    }

    @Test
    void 파일첨부_발송_테스트() throws MessagingException, IOException, ExecutionException, InterruptedException {
        final File testFile = new File(FileUtils.getTempDirectory() + "/aaa.txt");
        FileUtils.writeStringToFile(testFile, "test", "UTF-8");
        Assertions.assertEquals(true, emailService.sendMail("jd@cashmallow.com", UUID.randomUUID().toString(), "Test", testFile).get());
        testFile.delete();
    }
}