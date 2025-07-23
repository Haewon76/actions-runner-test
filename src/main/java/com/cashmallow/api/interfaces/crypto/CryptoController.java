package com.cashmallow.api.interfaces.crypto;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.common.CustomStringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
@RequestMapping(value = "/crypto")
@RequiredArgsConstructor
public class CryptoController {

    private final AuthService authService;

    private final CryptoService cryptoService;


    @PostMapping(value = "/decrypt", produces = GlobalConst.PRODUCES)
    public String decrypt(@RequestHeader("Authorization") String token,
                          @RequestBody String requestBody,
                          HttpServletRequest request,
                          HttpServletResponse response) {

        long managerId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (Const.NO_USER_ID == managerId) {
            log.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        try {
            resultVO.setSuccessInfo(cryptoService.decrypt(token, requestBody));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

}
