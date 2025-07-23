package com.cashmallow.api.interfaces.memo;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.memo.Memo;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.common.CommNet;
import com.cashmallow.common.CustomStringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/memo")
@RequiredArgsConstructor
public class MemoController {

    private final AuthService authService;

    private final MemoService memoService;

    @PostMapping(produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String createMemo(@RequestHeader("Authorization") String token,
                             HttpServletRequest request, HttpServletResponse response) {

        String method = "insertMemo";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }
        String encoded = CommNet.extractPostRequestBody(request);
        String jsonStr = CustomStringUtil.decode(token, encoded);
        log.info("{} jsonStr={}", method, jsonStr);

        JSONObject jo = new JSONObject(jsonStr);

        Map<String, Object> params = new HashMap<>();
        params.put("refId", jo.get("refId"));
        params.put("memo", jo.get("memo"));
        params.put("type", jo.get("type"));

        params.put("creatorId", managerId);
        int results = memoService.createMemo(params);

        if (results == 1) {
            resultVO.setSuccessInfo();
        } else {
            resultVO.setFailInfo(Const.MSG_INVALID_PARAMS);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PatchMapping(produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String modifyMemo(@RequestHeader("Authorization") String token,
                             HttpServletRequest request, HttpServletResponse response) {

        String method = "modifyMemo";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }
        String encoded = CommNet.extractPostRequestBody(request);
        String jsonStr = CustomStringUtil.decode(token, encoded);
        log.info("{} jsonStr={}", method, jsonStr);

        JSONObject jo = new JSONObject(jsonStr);

        Map<String, Object> params = new HashMap<>();
        params.put("id", jo.get("id"));
        params.put("refId", jo.get("refId"));
        params.put("memo", jo.get("memo"));
        params.put("type", jo.get("type"));
        params.put("creatorId", managerId);

        if (StringUtils.isEmpty((String) jo.get("id"))) {
            resultVO.setFailInfo(Const.MSG_INVALID_PARAMS);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        int results = memoService.modifyMemo(params);

        if (results == 1) {
            resultVO.setSuccessInfo();
        } else {
            resultVO.setFailInfo(Const.MSG_INVALID_PARAMS);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * EDD Memo List 가져오기
     *
     * @param token
     * @param userEddId
     * @param response
     * @return
     */
    @GetMapping("/{userEddId}/edd-memos")
    public String getUserEddMemoList(@RequestHeader("Authorization") String token,
                                     @PathVariable Long userEddId,
                                     HttpServletResponse response) {
        String method = "getUserEddImageList";
        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("refId", userEddId);

        List<Memo> userEddMemoList = memoService.getMemoList(params);
        resultVO.setSuccessInfo(userEddMemoList);
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }
}
