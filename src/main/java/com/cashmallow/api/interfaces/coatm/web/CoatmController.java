package com.cashmallow.api.interfaces.coatm.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.coatm.dto.InputVO;
import com.cashmallow.api.interfaces.coatm.dto.OutputVO;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl.ResultCode;
import com.cashmallow.common.EnvUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Controller
@RequestMapping(value = "/coatm")
public class CoatmController {

    Logger logger = LoggerFactory.getLogger(CoatmController.class);

    @Value(value = "${coatm.api.companyCode}")
    private String companyCode;

    @Value(value = "${coatm.api.addresses}")
    private List<String> addresses = new ArrayList<>();

    @Value(value = "${coatm.api.seedEcbKey}")
    private String seedEcbKey;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private CoatmServiceImpl coatmService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AlarmService alarmService;

    /**
     * complete cash-out
     *
     * @param params   consumes is 'application/x-www-form-urlencoded' type
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/cashout/complete", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String completeCashOut(@RequestParam Map<String, String> params,
                                  HttpServletRequest request, HttpServletResponse response) {

        String method = "completeCashOut()";

        String result = null;
        OutputVO outputVO = new OutputVO();

        ObjectMapper mapper = new ObjectMapper();

        logger.info("{}: requestParams={}", method, params);

        if (!isAllowedIP(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try {
            InputVO inputVO = mapper.convertValue(params, InputVO.class);

            if (!companyCode.equals(inputVO.getRqCompanyCode())) {
                logger.error("{}: Invalid company code. RqCompanyCode={}",
                        method, inputVO.getRqCompanyCode());
                throw new CashmallowException(INTERNAL_SERVER_ERROR, ResultCode.ETC_ERROR);
            }

            outputVO.setRsMemberPin(inputVO.getRqMemberPin());
            outputVO.setRsPaymentKey(inputVO.getRqPaymentKey());
            outputVO.setRsWithdrawSeqNo(inputVO.getRqWithdrawSeqNo());
            outputVO.setRsAtmCode(inputVO.getRqAtmCode());
            outputVO.setRsAtmWithdrawRequestTime(inputVO.getRqAtmWithdrawRequestTime());
            outputVO.setRsAtmWithdrawRequestDate(inputVO.getRqAtmWithdrawRequestDate());
            outputVO.setRsOrgCD(inputVO.getRqOrgCd());

            CashOut cashOut = coatmService.completeCashOut(inputVO.getRqMemberPin(), inputVO.getRqPaymentKey(),
                    inputVO.getRqWithdrawSeqNo());

            String payToken = coatmService.makePayToken(cashOut.getId());

            outputVO.setRsAmount(String.valueOf(cashOut.getTravelerCashOutAmt().intValue()));
            outputVO.setRsFee(String.valueOf(cashOut.getTravelerCashOutFee().intValue()));
            outputVO.setPayToken(payToken);

            outputVO.setRsResultCode(ResultCode.SUCCESS);
            outputVO.setRsResultMsg("Thank you for using Cashmallow. Withdrawal is complete.");

        } catch (CashmallowException e) {
            logger.warn(e.getMessage());

            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), Locale.ENGLISH);

            outputVO.setRsResultCode(e.getOption());
            outputVO.setRsResultMsg(errMsg);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, "Internal Server Error. Please contact the Cashmallow customer center.", Locale.ENGLISH);

            outputVO.setRsResultCode(ResultCode.ETC_ERROR);
            outputVO.setRsResultMsg(errMsg);
        }

        try {
            result = mapper.writeValueAsString(outputVO);

            logger.info("{}: It returns successfully. result={}", method, result);

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, "Internal Server Error. Please contact the Cashmallow customer center.", Locale.ENGLISH);

            outputVO.setRsResultCode(ResultCode.ETC_ERROR);
            outputVO.setRsResultMsg(errMsg);
        }

        return result;
    }

    /**
     * Test complete cash-out. This method working in 'dev-local' environment only.
     *
     * @param params   consumes is 'application/x-www-form-urlencoded' type
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/test/cashout/complete", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String completeCashOutTest(@RequestParam("birth_date") String birthDate, @RequestParam String otp,
                                      HttpServletRequest request, HttpServletResponse response) {

        String method = "completeCashOutTest()";

        if (!envUtil.isDev()) {
            logger.error("{}: This method is working in 'dev-local' only.", method);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        String result = null;
        OutputVO outputVO = new OutputVO();

        ObjectMapper mapper = new ObjectMapper();

        logger.info("{}: birthDate={}, otp={}", method, birthDate, otp);

        if (!isAllowedIP(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try {

            String encrytedBirthDate = securityService.encryptSeedEcb(seedEcbKey, birthDate);
            String encrytedOtp = securityService.encryptSeedEcb(seedEcbKey, otp);

            String partnerTxnId = String.valueOf(new Date().getTime());
            CashOut cashOut = coatmService.completeCashOut(encrytedBirthDate, encrytedOtp, partnerTxnId);

            String payToken = coatmService.makePayToken(cashOut.getId());

            outputVO.setRsAmount(String.valueOf(cashOut.getTravelerCashOutAmt().intValue()));
            outputVO.setRsFee(String.valueOf(cashOut.getTravelerCashOutFee().intValue()));
            outputVO.setPayToken(payToken);

            outputVO.setRsResultCode(ResultCode.SUCCESS);
            outputVO.setRsResultMsg("Thank you for using Cashmallow. Withdrawal is complete.");

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), Locale.ENGLISH);

            outputVO.setRsResultCode(e.getOption());
            outputVO.setRsResultMsg(errMsg);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, "Internal Server Error. Please contact the Cashmallow customer center.", Locale.ENGLISH);

            outputVO.setRsResultCode(ResultCode.ETC_ERROR);
            outputVO.setRsResultMsg(errMsg);
        }

        try {
            result = mapper.writeValueAsString(outputVO);

            logger.info("{}: It returns successfully. result={}", method, result);

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, "Internal Server Error. Please contact the Cashmallow customer center.", Locale.ENGLISH);

            outputVO.setRsResultCode(ResultCode.ETC_ERROR);
            outputVO.setRsResultMsg(errMsg);
        }

        return result;
    }

    /**
     * rollback cash-out
     *
     * @param params   consumes is 'application/x-www-form-urlencoded' type
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/cashout/rollback", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String rollbackCashOut(@RequestParam Map<String, String> params,
                                  HttpServletRequest request, HttpServletResponse response) {

        String method = "rollbackCashOut()";

        String result = null;

        ObjectMapper mapper = new ObjectMapper();
        OutputVO outputVO = new OutputVO();

        logger.info("{}: params={}", method, params);

        alarmService.aAlert("COATM롤백", params.toString(), null);

        if (!isAllowedIP(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try {
            InputVO inputVO = mapper.convertValue(params, InputVO.class);

            if (!companyCode.equals(inputVO.getRqCompanyCode())) {
                logger.error("{}: Invalid company code. RqCompanyCode={}",
                        method, inputVO.getRqCompanyCode());
                throw new CashmallowException(INTERNAL_SERVER_ERROR, ResultCode.ETC_ERROR);
            }

            outputVO.setRsMemberPin(inputVO.getRqMemberPin());
            outputVO.setRsPaymentKey(inputVO.getRqPaymentKey());
            outputVO.setRsWithdrawSeqNo(inputVO.getRqWithdrawSeqNo());
            outputVO.setRsAtmCode(inputVO.getRqAtmCode());
            outputVO.setRsAtmWithdrawRequestTime(inputVO.getRqAtmWithdrawRequestTime());
            outputVO.setRsAtmWithdrawRequestDate(inputVO.getRqAtmWithdrawRequestDate());
            outputVO.setRsOrgCD(inputVO.getRqOrgCd());

            coatmService.rollbackCashOut(inputVO);

            outputVO.setRsResultCode(ResultCode.SUCCESS);
            outputVO.setRsResultMsg("Withdrawal canceled.");

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), Locale.ENGLISH);

            outputVO.setRsResultCode(e.getOption());
            outputVO.setRsResultMsg(errMsg);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, "Internal Server Error. Please contact the Cashmallow customer center.", Locale.ENGLISH);

            outputVO.setRsResultCode(ResultCode.ETC_ERROR);
            outputVO.setRsResultMsg(errMsg);
        }

        try {
            result = mapper.writeValueAsString(outputVO);

            logger.info("{}: It returns successfully. result={}", method, result);

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);

            String errMsg = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, "Internal Server Error. Please contact the Cashmallow customer center.", Locale.ENGLISH);

            outputVO.setRsResultCode(ResultCode.ETC_ERROR);
            outputVO.setRsResultMsg(errMsg);
        }

        return result;
    }

    private boolean isAllowedIP(HttpServletRequest request) {
        final String method = "checkRemoteIP()";
        final String unknown = "unknown";

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        logger.info("{}: ip={}", method, ip);

        return addresses.contains(ip);
    }

}
