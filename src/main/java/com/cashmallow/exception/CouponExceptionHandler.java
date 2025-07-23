package com.cashmallow.exception;


import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.DatatablesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.cashmallow.api.interfaces.coupon")
public class CouponExceptionHandler {

    @ExceptionHandler(CashmallowException.class)
    public ResponseEntity<DatatablesResponse<String>> handleCashmallowException(CashmallowException e) {
        log.error("handleCashmallowException(): {}", e.getMessage());

        String status = String.valueOf(e.getMessage());
        String message = e.getOption();

        DatatablesResponse<String> res = new DatatablesResponse<>(message, String.valueOf(status));
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(res);
    }
}
