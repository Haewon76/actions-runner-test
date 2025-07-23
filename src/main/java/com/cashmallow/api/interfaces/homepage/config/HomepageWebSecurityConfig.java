package com.cashmallow.api.interfaces.homepage.config;

import com.cashmallow.common.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;


@Order(105)
@Configuration
@EnableWebSecurity
public class HomepageWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(HomepageWebSecurityConfig.class);

    // Homepage API key header field name.
    private String keyFieldName = "X-API-Key";

    @Value("${cashmallow.homepage.whitelist}")
    private Set<String> homepageWhitelist;

    @Value("${cashmallow.homepage.keyValue}")
    private String homepageKeyValue;

    @Autowired
    private EnvUtil envUtil;


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String method = "configure()";

        HomepageAuthFilter filter = new HomepageAuthFilter(keyFieldName);

        filter.setAuthenticationManager(authentication -> {

            // if (!envUtil.isPrd()) {
            //     String credential = (String) authentication.getCredentials();
            //     if (!homepageKeyValue.equals(credential)) {
            //         logger.error("{}: Invalid API key value. credential={}", method, credential);
            //         throw new BadCredentialsException("The API key was not found or wrong key value.");
            //     }
            // } else {
            WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
            String userIp = details.getRemoteAddress();
            if (!homepageWhitelist.contains(userIp)) {
                logger.error("{}: Not allowed IP address. userIp={}", method, userIp);
                throw new BadCredentialsException("Invalid IP Address");
            }
            // }

            authentication.setAuthenticated(true);

            return authentication;
        });

        http.antMatcher("/homepage/**")
                .antMatcher("/devoffice/**")
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
    }

    private class HomepageAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

        private String principalRequestHeader;

        public HomepageAuthFilter(String principalRequestHeader) {
            this.principalRequestHeader = principalRequestHeader;
        }

        @Override
        protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
            return "N/A";
        }

        @Override
        protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
            return request.getHeader(principalRequestHeader);
        }
    }
}
