package com.cashmallow.api.interfaces;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// - CORS (CROSS-ORIGIN RESOURCE SHARING) 서비스가 다른 크로스도메인 해결방법: http://blog.moramcnt.com/?p=676
// - CORS 크로스 도메인 이슈 (No 'Access-Control-Allow-Origin' header is present on the requested resource) 컴파일 오류 발생함: http://ooz.co.kr/232
//@Component
public class SimpleCORSFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod())) ;
        {
            // CORS "pre-flight" request
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.addHeader("Access-Control-Allow-Headers", "Authorization");
            response.addHeader("Access-Control-Max-Age", "1728000");
        }

        filterChain.doFilter(request, response);
    }
}

