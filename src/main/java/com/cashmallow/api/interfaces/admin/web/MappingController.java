package com.cashmallow.api.interfaces.admin.web;

import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.application.impl.ExchangeServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.BankAccountAskVO;
import com.cashmallow.api.interfaces.admin.dto.BankAccountVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.common.CommNet;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles requests for the application home page.
 */
@Controller
// @RequestMapping("/restful")  // Spring MVC 3 Restful 맛보기 : http://knight76.tistory.com/entry/Spring-MVC-3-Restful-%EB%A7%9B%EB%B3%B4%EA%B8%B0
public class MappingController {

    private static final Logger logger = LoggerFactory.getLogger(MappingController.class);

    @Autowired
    private ExchangeServiceImpl exchangeService; // root-context.xml에 정의된 bean 이름과 mapping됨.

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private AuthService authService;

    //-------------------------------------------------------------------------------
    // 62. 은행정보(mapping용)
    //-------------------------------------------------------------------------------

    // 기능: 62.1. 사용 은행 정보 등록
    @PostMapping(value = "/admin/mapping/putBankAccount", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String putBankAccount(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {
        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);
        logger.info("putBankAccount(): jsonStr={}", jsonStr);
        BankAccountVO pvo = (BankAccountVO) JsonStr.toObject(BankAccountVO.class.getName(), jsonStr);

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        if (token == null) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (!authService.containsRole(token, Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        try {
            voResult = exchangeService.putBankAccount(managerId, pvo);
        } catch (CashmallowException e) {
            logger.debug(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 62.2. 사용 은행 정보 검색
    @PostMapping(value = "/admin/mapping/findBankAccount", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String findBankAccount(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {
        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);
        logger.info("findBankAccount(): jsonStr={}", jsonStr);
        BankAccountAskVO pvo = (BankAccountAskVO) JsonStr.toObject(BankAccountAskVO.class.getName(), jsonStr);

        //        ApiResultVO resultVO = exchangeService.findBankAccount(token, pvo);

        SearchResultVO searchResultVO = companyService.findBankAccountList(pvo);
        ApiResultVO resultVO = new ApiResultVO();
        resultVO.setSuccessInfo(searchResultVO);

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

}

