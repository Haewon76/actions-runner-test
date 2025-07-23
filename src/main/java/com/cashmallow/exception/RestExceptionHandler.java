package com.cashmallow.exception;

import com.cashmallow.api.config.ReplicationRoutingDataSource;
import com.cashmallow.api.domain.shared.AuthException;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Locale;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
@Order(1)
@RestControllerAdvice
public class RestExceptionHandler {

    // StackTrace 에러 로그 출력 길이
    private final static int MAX_STACKTRACE_LENGTH = 1000;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private DataSource dataSource;


    @ExceptionHandler(AuthException.class)
    protected String handleAuthException(final HttpServletRequest request, final HttpServletResponse response, final Exception e) {
        ApiResultVO voResult = new ApiResultVO(e.getMessage());
        final String token = request.getHeader("Authorization");
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class, HttpMessageNotReadableException.class})
    protected String handleRequestException(final Exception e) {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_PARAMS);
        voResult.setFailInfo(e.getMessage());
        return JsonStr.toJson(voResult);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleException(final HttpServletRequest request, final HttpServletResponse response, final Exception e) {
        // {"code":"200","status":"Success","message":"","obj":[]}
        // {"code":"400","status":"Failure","message":"Unexpected Error","obj":[]}
        // {"status":"Failure","message":"Unexpected Error","obj":[]}
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage("INTERNAL_SERVER_ERROR", null, locale);

        ApiResultVO voResult = new ApiResultVO();
        voResult.setCode(CODE_FAILURE);
        voResult.setStatus(STATUS_FAILURE);
        voResult.setMessage(message);

        String token = CommonUtil.getAuthorization(request);

        log.error("handleException", e);

        if (e instanceof UnrecognizedPropertyException) {
            String errorMessage = "요청에 알 수 없는 필드가 포함되어 있습니다: " + ((UnrecognizedPropertyException) e).getPropertyName();
            log.error("UnrecognizedPropertyException(): {}", errorMessage);
            voResult.setMessage(e.getMessage());
        }

        if(e instanceof DataAccessException) {
            if (dataSource instanceof LazyConnectionDataSourceProxy proxy) {
                DataSource target = ((ReplicationRoutingDataSource) proxy.getTargetDataSource()).getResolvedDefaultDataSource();
                if (target instanceof HikariDataSource hikariDataSource) {
                    log.info("HikariCP: active={}, idle={}, total={}, max={}",
                            hikariDataSource.getHikariPoolMXBean().getActiveConnections(),
                            hikariDataSource.getHikariPoolMXBean().getIdleConnections(),
                            hikariDataSource.getHikariPoolMXBean().getTotalConnections(),
                            hikariDataSource.getMaximumPoolSize()
                    );
                }
            }
        }

        if (e instanceof CashmallowException) {
            voResult.setMessage(e.getMessage());
        }

        if (StringUtils.isEmpty(token)) {
            // 암호화 하지 않음
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(JsonStr.toJsonString(voResult));
        }

        // 암호화 함
        return new ResponseEntity<>(CustomStringUtil.encryptJsonString(token, voResult, response), HttpStatus.OK);
    }

}
