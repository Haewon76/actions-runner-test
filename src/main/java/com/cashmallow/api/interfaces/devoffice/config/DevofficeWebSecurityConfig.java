package com.cashmallow.api.interfaces.devoffice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Order(901)
@Configuration
@EnableWebSecurity
public class DevofficeWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(DevofficeWebSecurityConfig.class);

    @Value("${cashmallow.homepage.whitelist}")
    private Set<String> whitelist;


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String method = "configure()";

        AuthenticationFilter filter = new AuthenticationFilter();

        filter.setAuthenticationManager(new AuthenticationManager() {

            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {

                WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
                String userIp = details.getRemoteAddress();

                if (!whitelist.contains(userIp)) {
                    logger.info("{}: Invalid IP address. userIp={}", method, userIp);
                    throw new BadCredentialsException("Invalid IP Address");
                }

                authentication.setAuthenticated(true);

                return authentication;
            }
        });

        http.antMatcher("/devoffice/**")
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
    }

    private class AuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
        @Override
        protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
            return "N/A";
        }

        @Override
        protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
            return "N/A";
        }
    }
}
