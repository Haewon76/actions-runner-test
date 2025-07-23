package com.cashmallow.api.config;

import com.cashmallow.common.KeyUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class FcmConfig {

    @Value("${firebase.cloudMessaging.key}")
    private String fcmKey;

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(KeyUtil.getJSONKey(fcmKey).getBytes()))
                .createScoped(SCOPES);
        credentials.refreshIfExpired();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        return FirebaseMessaging.getInstance(getFirebaseApp(options));
    }

    private FirebaseApp getFirebaseApp(FirebaseOptions options) {
        try {
            return FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            return FirebaseApp.initializeApp(options);
        }
    }
}
