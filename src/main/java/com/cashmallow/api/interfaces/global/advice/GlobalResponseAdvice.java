package com.cashmallow.api.interfaces.global.advice;

import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.global.controller.GlobalDepositController;
import com.cashmallow.api.interfaces.global.dto.GlobalBaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = {GlobalDepositController.class})
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !ApiResultVO.class.isAssignableFrom(returnType.getParameterType())
                && !GlobalBaseResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(GlobalBaseResponse.ok(body));
            } catch (JsonProcessingException e) {
                return GlobalBaseResponse.error(e);
            }
        }

        return GlobalBaseResponse.ok(body);
    }
}
