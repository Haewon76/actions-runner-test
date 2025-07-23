package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerJpRequest;
import com.cashmallow.api.interfaces.traveler.dto.TravelerCertificationJpRequest;
import com.cashmallow.api.interfaces.traveler.dto.TravelerCertificationJpResponse;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

/**
 * App 에서 요청하는 JP 본인인증 요청
 **/

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/jp/travelers")
public class TravelerJpController {
    private final AuthService authService;
    private final EnvUtil envUtil;

    private final MessageSource messageSource;

    private final Validator validator;
    private final JsonUtil jsonUtil;
    private final AlarmService alarmService;
    private final TravelerJpService travelerJpService;

    /**
     * NFC(신분증 스캐닝) JP 자동 본인인증
     * 본인인증은 캐시멜로에서 진행하므로 NFC 와 상관없음
     **/
    @PostMapping(value = "")
    public String registerTravelerJpForAuto(@RequestHeader("Authorization") String token,
                                            @RequestPart(value = "traveler") String requestBody,
                                            @RequestPart(required = false) MultipartFile certificationPicture,
                                            HttpServletRequest request, HttpServletResponse response) throws CashmallowException {
        // Token 검사 및 복호화
        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }
        String jsonStr = CustomStringUtil.decode(token, requestBody);
        log.debug("decodeBody={}", jsonStr);

        if (envUtil.isDev()) {
            alarmService.i("registerTravelerJp", "userId: " + userId + ", " + jsonStr);
        }

        // body mapping & validation
        RegisterTravelerJpRequest registerRequest = jsonUtil.fromJson(jsonStr, RegisterTravelerJpRequest.class);
        log.info("registerRequest={}", registerRequest.toString());
        List<String> errorString = validRequest(registerRequest);
        if (errorString != null) {
            log.error("errorString:{}", errorString);
            voResult.setFailInfo(errorString.toString());
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        // traveler 저장
        try {
            Traveler resultTraveler = travelerJpService.registerTravelerJpForAuto(userId, registerRequest, certificationPicture);
            voResult.setSuccessInfo(resultTraveler);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), LocaleContextHolder.getLocale()));
        log.debug("JsonStr.toJson(voResult):{}", JsonStr.toJson(voResult));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    private List<String> validRequest(RegisterTravelerJpRequest request) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);

        if (errors.hasErrors()) {
            List<FieldError> fieldErrors = errors.getFieldErrors();
            fieldErrors.forEach(e -> log.info("{}: {}", e.getField(), e.getDefaultMessage()));

            return fieldErrors
                    .stream()
                    .map(e -> e.getField() + ":" + e.getDefaultMessage())
                    .toList();
        }
        return null;
    }

    /**
     * JP 수동 본인인증
     **/
    @PostMapping(value = "/manual")
    public String registerTravelerJpForManual(@RequestHeader("Authorization") String token,
                                              @RequestBody String requestBody,
                                              HttpServletRequest request, HttpServletResponse response) throws CashmallowException {
        // Token 검사 및 복호화
        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }
        log.debug("token={}", token);
        String jsonStr = CustomStringUtil.decode(token, requestBody);
        log.debug("decodeBody={}", jsonStr);

        // body mapping & validation
        RegisterTravelerJpRequest registerRequest = jsonUtil.fromJson(jsonStr, RegisterTravelerJpRequest.class);
        log.info("registerRequest={}", registerRequest.toString());
        List<String> errorString = validRequest(registerRequest);
        if (errorString != null) {
            log.error("errorString:{}", errorString);
            voResult.setFailInfo(errorString.toString());
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        // traveler 저장
        try {
            Traveler resultTraveler = travelerJpService.registerTravelerJpForManual(userId, registerRequest);
            voResult.setSuccessInfo(resultTraveler);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), LocaleContextHolder.getLocale()));
        log.debug("JsonStr.toJson(voResult):{}", JsonStr.toJson(voResult));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = "/certification/photo")
    public String uploadCertificationPhoto(@RequestHeader("Authorization") String token,
                                           @RequestPart(value = "certification_step") String requestBody,
                                           @RequestPart(required = false) MultipartFile certificationPhoto,
                                           HttpServletRequest request, HttpServletResponse response) throws CashmallowException {
        // Token 검사 및 복호화
        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        log.debug("uploadCertificationPhoto(): test token={}", token);
        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }
        String jsonStr = CustomStringUtil.decode(token, requestBody);
        log.debug("decodeBody={}", jsonStr);

        // body mapping & validation
        TravelerCertificationJpRequest certificationJpRequest = jsonUtil.fromJson(jsonStr, TravelerCertificationJpRequest.class);
        log.info("certificationJpRequest={}", certificationJpRequest.toString());

        // traveler 저장
        try {
            travelerJpService.updateTravelerCertificationStep(certificationJpRequest, certificationPhoto);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), LocaleContextHolder.getLocale()));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/{userId}/certification")
    public String getActiveCertificationStepList(@RequestHeader("Authorization") String token,
                                                 @PathVariable Long userId,
                                                 HttpServletRequest request, HttpServletResponse response) throws CashmallowException {
        // Token 검사 및 복호화
        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        long tokenUserId = authService.getUserId(token);
        if (tokenUserId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        try {
            List<TravelerCertificationJpResponse> result = travelerJpService.getActiveCertificationStepList(userId)
                    .stream().map(TravelerCertificationJpResponse::of).toList();
            voResult.setSuccessInfo(result);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), LocaleContextHolder.getLocale()));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

}
