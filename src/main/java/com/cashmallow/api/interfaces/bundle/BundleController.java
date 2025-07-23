package com.cashmallow.api.interfaces.bundle;

import com.cashmallow.api.application.*;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.bundle.Bundle;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.common.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.*;

@Controller
public class BundleController {

    private static final Logger logger = LoggerFactory.getLogger(BundleController.class);

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private BundleService rnBundleService;

    @GetMapping(value = "/admin/bundle/list", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getBundleList(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        logger.info("getBundleList()");

        // 번들 리스트 조회
        List<Bundle> rnBundles = rnBundleService.getBundleList();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Map<String, String>> results = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId kstZoneId = ZoneId.of("Asia/Seoul");

        for (Bundle bundle : rnBundles) {
            Map<String, String> result = new HashMap<>();

            result = mapper.convertValue(bundle, new TypeReference<Map<String, String>>() {
            });
            LocalDateTime createdAt = bundle.getCreatedAt().toLocalDateTime();
            ZonedDateTime kstDateTime = createdAt.atZone(ZoneId.systemDefault()).withZoneSameInstant(kstZoneId);
            result.put("releaseDate", kstDateTime.format(formatter));

            if (bundle.getUpdatedAt() != null) {
                LocalDateTime updatedAt = bundle.getUpdatedAt().toLocalDateTime();
                ZonedDateTime kstUpdateDateTime = updatedAt.atZone(ZoneId.systemDefault()).withZoneSameInstant(kstZoneId);
                result.put("updateDate", kstUpdateDateTime.format(formatter));
            }

            results.add(result);
        }

        ApiResultVO voResult = new ApiResultVO();
        voResult.setSuccessInfo(results);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = "/admin/bundle", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String registerBundle(@RequestHeader("Authorization") String token,
                                 @RequestPart("file") MultipartFile bundleFile,
                                 HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, CashmallowException {
        Long managerId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(managerId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            long result = rnBundleService.registerBundle(bundleFile, request, managerId);
            if (result > 0) {
                voResult.setSuccessInfo();
            } else {
                voResult.setFailInfo("Bundle 등록에 실패하였습니다.");
            }
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PutMapping(value = "/admin/bundle/status", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String setIsActive(@RequestHeader("Authorization") String token,
                              @RequestBody String requestBody,
                              HttpServletResponse response) {
        String jsonStr = CustomStringUtil.decode(token, requestBody);
        JSONObject body = new JSONObject(jsonStr);

        long id = body.getLong("id");
        String isActive = body.getString("isActive");
        String description = body.getString("description");
        Long userId = authService.getUserId(token);

        logger.info("setIsActive(): id={}, isActive={} managerId={}", id, isActive, userId);

        rnBundleService.setIsActive(id, isActive, userId, description);

        ApiResultVO voResult = new ApiResultVO();
        voResult.setSuccessInfo();

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @DeleteMapping(value = "/admin/bundle/{bundleId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String deleteBundleById(@RequestHeader("Authorization") String token,
                                   @PathVariable("bundleId") long bundleId,
                                   HttpServletResponse response) throws CashmallowException {
        Long managerId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(managerId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            rnBundleService.deleteBundle(bundleId);
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }
}
