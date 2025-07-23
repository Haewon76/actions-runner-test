package com.cashmallow.common.resolver;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.common.annotation.UserInfo;
import com.cashmallow.common.annotation.dto.CustomUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;
    private final LocaleResolver localeResolver;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CustomUserInfo.class) &&
                parameter.hasParameterAnnotation(UserInfo.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        String token = webRequest.getHeader("Authorization");
        String language = webRequest.getHeader("Accept-Language");

        long userId = authService.getUserId(token);
        Locale locale = Locale.ENGLISH;
        if (StringUtils.isNotBlank(language)) {
            locale = Locale.forLanguageTag(language);
        }

        return new CustomUserInfo(
                userId,
                token,
                locale
        );
    }
}