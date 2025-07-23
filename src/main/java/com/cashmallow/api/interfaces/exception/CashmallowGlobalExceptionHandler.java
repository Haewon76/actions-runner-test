// package com.cashmallow.api.interfaces.exception;
//
// import com.cashmallow.api.interfaces.ApiResultVO;
// import com.cashmallow.common.JsonStr;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.ResponseStatus;
//
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
//
//@ControllerAdvice
// public class CashmallowGlobalExceptionHandler {
//    private static final Logger logger = LoggerFactory.getLogger(CashmallowGlobalExceptionHandler.class);
//
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
//    public String handleException(final Exception exception,
//                                  final HttpServletRequest request,
//                                  final HttpServletResponse response) {
//
//        ApiResultVO apiResultVO = new ApiResultVO();
//        apiResultVO.setFailInfo(exception.getMessage());
//
//        logger.info("{} {}", request.getMethod(), request.getRequestURI());
//        logger.info(exception.getMessage());
//
//        return JsonStr.toJsonString(apiResultVO);
//    }
//}
