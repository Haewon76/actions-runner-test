package com.cashmallow.api.interfaces.edd;

import com.cashmallow.api.application.UserService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.edd.*;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.cashmallow.api.domain.shared.Const.ROLE_ADMIN;
import static com.cashmallow.api.domain.shared.Const.ROLE_SUPERMAN;

@RestController
@Slf4j
@RequestMapping(value = "/edd")
@RequiredArgsConstructor
public class UserEddController {

    private final AuthService authService;

    private final UserService userService;

    private final UserEddService userEddService;

    private final UserEddLogMapper userEddLogMapper;

    @GetMapping
    public String getEddList(@RequestHeader("Authorization") String token,
                             @RequestParam Map<String, Object> params,
                             HttpServletResponse response) {

        String method = "getEddList";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        List<UserEdd> userEddList = userEddService.getUserEddJoinList(params);

        resultVO.setSuccessInfo(userEddList);
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PatchMapping("/{userEddId}")
    public String updateEdd(@RequestHeader("Authorization") String token,
                            @PathVariable Long userEddId,
                            @RequestBody String requestBody,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        String method = "updateEdd";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            log.info("{}: 권한이 부족합니다. managerId={}", method, managerId);
            resultVO.setFailInfo(Const.MSG_NEED_AUTH);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        String ip = CommonUtil.getRemoteAddr(request);

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        log.info("{}: jsonStr={}", method, jsonStr);
        Map<String, Object> body = JsonStr.toHashMap(jsonStr);
        String limited = body.get("limited").toString();

        try {
            userEddService.updateEdd(managerId, userEddId, limited, ip);
            resultVO.setSuccessInfo();
        } catch (Exception e) {
            log.error("updateEdd {}", e.getMessage());
            resultVO.setFailInfo("updateEdd error");
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PostMapping(value = "/fileupload/{userEddId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String uploadFileList(@RequestHeader("Authorization") String token,
                                 @PathVariable long userEddId,
                                 @RequestPart("file") List<MultipartFile> pictureLists,
                                 HttpServletResponse response) {

        String method = "uploadFileList";

        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        try {
            userEddService.registerUserEddImage(managerId, userEddId, pictureLists);
            resultVO.setSuccessInfo();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo("uploadFileList error");
        }

        return JsonStr.toJsonString(resultVO, response);
    }

    @GetMapping("/{userEddId}/edd-images")
    public String getUserEddImageList(@RequestHeader("Authorization") String token,
                                      @PathVariable Long userEddId,
                                      HttpServletResponse response) {
        String method = "getUserEddImageList";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        List<Long> userEddImageList = userEddService.getUserEddImageList(userEddId);
        resultVO.setSuccessInfo(userEddImageList);
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/edd-images/{userEddImageId}")
    public String getUserEddImage(@RequestHeader("Authorization") String token,
                                  @PathVariable Long userEddImageId,
                                  HttpServletResponse response) throws Exception {
        String method = "getUserEddImage";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        UserEddImage userEddImage = userEddService.getUserEddImage(userEddImageId);
        resultVO.setSuccessInfo(userEddImage);
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping(value = "/fromAmtHistory/{userEddId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getFromAmtHistory(@RequestHeader("Authorization") String token, @PathVariable Long userEddId, HttpServletResponse response) {

        String method = "getFromAmtHistory";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        List<UserEddFromAmtHistory> userEddFromAmtHistoryList = userEddService.getFromAmtHistory(userEddId);

        resultVO.setSuccessInfo(userEddFromAmtHistoryList);
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PostMapping(value = "/addUser/{userId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addUser(@RequestHeader("Authorization") String token,
                          @PathVariable Long userId,
                          HttpServletRequest request,
                          HttpServletResponse response) {

        String method = "addUserEdd";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_SUPERMAN, ROLE_ADMIN)) {
            log.info("{}: 권한이 부족합니다. managerId={}", method, managerId);
            resultVO.setFailInfo(Const.MSG_NEED_AUTH);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        String ip = CommonUtil.getRemoteAddr(request);

        try {
            Calendar cal = Calendar.getInstance();
            Timestamp toDayTimestamp = new Timestamp(cal.getTime().getTime());

            UserEdd userEdd = UserEdd.builder()
                    .userId(userId)
                    .limited(Const.USER_EDD_LIMITED_Y)
                    .amount(BigDecimal.ZERO)
                    .count(0)
                    .creatorId(managerId)
                    .createdAt(toDayTimestamp)
                    .updatedAt(toDayTimestamp)
                    .initIp(ip)
                    .build();

            userEddService.registerUserEdd(userEdd, managerId, ip);
            resultVO.setSuccessInfo();
            try {
                UserEddLog userEddLog = new UserEddLog();
                BeanUtils.copyProperties(userEdd, userEddLog);
                userEddLog.setId(null);
                userEddLog.setUserEddId(userEdd.getId());
                userEddLogMapper.registerUserEddLog(userEddLog);

            } catch (Exception e) {
                log.error("verificationUserEdd registerUserEdd error {}", e);
            }

        } catch (Exception e) {
            log.error("updateEdd " + e.getMessage(), e);
            resultVO.setFailInfo("updateEdd error");
        }


        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

}
