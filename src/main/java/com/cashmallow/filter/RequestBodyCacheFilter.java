package com.cashmallow.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSON Body를 여러번 읽을 수 있게 커스텀 필터를 추가
 */
@Slf4j
@Component
@Order(1)
public class RequestBodyCacheFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (isCacheableRequestBody(request)) {
            chain.doFilter(new CacheReadHttpServletRequest(request), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isCacheableRequestBody(HttpServletRequest request) {
        final String contentType = request.getContentType();
        if (StringUtils.isEmpty(contentType)) {
            return false;
        }

        if (!StringUtils.equalsAny(request.getMethod(), "POST", "PUT")) {
            return false;
        }

        return StringUtils.containsAny(contentType.toLowerCase(), MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE);
    }
}
