package com.cashmallow.api.interfaces;

import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.model.ApiResponse;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.config.EnableDevLocal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@Service
@RequiredArgsConstructor
@EnableDevLocal
public class TestService {

    private final AuthService authService;
    private final TravelerServiceImpl travelerService;
    private final UserRepositoryService userRepositoryService;
    private final TravelerMapper travelerMapper;

    public static MockHttpServletRequest request;
    public static MockHttpServletResponse response;
    public static ServletRequestAttributes sra;
    private final JsonUtil jsonUtil;

    @Setter
    @Getter
    // private Long userId = 1006182L; // tiger001@auu.kr
    private Long userId = 994103L; // tiger002@auu.kr

    public String getAccessToken() {
        String token = null;
        try {
            init();
            User user = userRepositoryService.getUserByUserId(userId);
            String refreshToken = authService.issueRefreshToken(user.getLogin(), userId, "[\"ROLE_ANONYMOUS\", \"ROLE_USER\"]");
            token = authService.issueAccessToken(refreshToken);
        } catch (Exception e) {
        }

        assertNotNull(token, "token is null");

        return token;
    }

    public Traveler getTraveler() {
        return travelerMapper.getTravelerByUserId(userId);
    }

    private void getDecodeString(String token, String jsonStr) {
        try {
            ApiResponse apiResponse = jsonUtil.fromJson(jsonStr, ApiResponse.class);

            String result = jsonStr;
            // 암호화 되어있는 경우
            if(apiResponse == null) {
                result = CryptAES.decode(token, jsonStr);
                apiResponse = jsonUtil.fromJson(result, ApiResponse.class);
            }

            System.out.println("result json = " + result);
            assertEquals("200", apiResponse.code(), "code is not 200");
        } catch (Exception ignore) {
        }
    }

    private void init() {
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 13; SM-A325N Build/TP1A.220624.014)");

        sra = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(sra);
    }

    public void setLocale(Locale locale) {
        request.addPreferredLocale(locale);
    }

    public void setUri(String uri) {
        request.setRequestURI(uri);
    }

    public void execute(TestExecutor executor) {
        assertDoesNotThrow(() -> {
            String cashOutGuide = executor.execute();
            // 복호화 처리
            getDecodeString(getAccessToken(), cashOutGuide);
        });
    }

    @FunctionalInterface
    public interface TestExecutor {
        String execute();
    }

    public String toPretty(String json) {
        return jsonUtil.toJsonPretty(jsonUtil.fromJson(json, Object.class));
    }
}
