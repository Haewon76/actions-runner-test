package com.cashmallow.api.interfaces.admin.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.exchange.ExchangeDepositReceipt;
import com.cashmallow.api.domain.model.remittance.RemittanceDepositReceipt;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.*;
import com.cashmallow.common.CommNet;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StorekeeperCalc, Refund, Company
 *
 * @author swshin
 */
@Controller
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RemittanceServiceImpl remittanceService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private ObjectMapper objectMapper;


    // -------------------------------------------------------------------------------
    // 55. 여행자 환불
    // -------------------------------------------------------------------------------

    @PostMapping(value = "/admin/v2/refund/receipt", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String receiptRefund(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                HttpServletRequest request, HttpServletResponse response) {

        logger.info("receiptRefund(): requestBody={}", requestBody);
        JSONObject jsonObject = new JSONObject(requestBody);
        String country = jsonObject.getString("country");

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        List<String> auths = authService.getUserAuthInfo(token);
        long managerId = authService.getUserId(token);

        if (!auths.contains(Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            refundService.receiptRefund(managerId, country);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/admin/v2/refund/cancel", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String cancelRefundByCashmallow(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                           HttpServletRequest request, HttpServletResponse response) {

        logger.info("cancelRefundByCashmallow(): requestBody={}", requestBody);
        JSONObject jsonObject = new JSONObject(requestBody);
        long refundId = jsonObject.getLong("refund_id");

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        List<String> auths = authService.getUserAuthInfo(token);
        long managerId = authService.getUserId(token);

        if (!auths.contains(Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            refundService.cancelNewRefundByCashmallow(managerId, refundId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            voResult.setFailInfo(e.getMessage());
        }

        // edited by Alex 20170808 : slack message Test !
        // -------------------------------------------
        if (voResult.getCode().equals(Const.CODE_SUCCESS)) {
            alarmService.aAlert("환불취소", "[ADMIN] 여행자 환불 취소 (new_refund ID) : " + refundId, null);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/admin/v2/refund/complete", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String completeRefundByCashmallow(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                             HttpServletRequest request, HttpServletResponse response) {

        logger.info("completeRefundByCashmallow(): requestBody={}", requestBody);
        JSONObject jsonObject = new JSONObject(requestBody);
        long refundId = jsonObject.getLong("refund_id");

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        List<String> auths = authService.getUserAuthInfo(token);
        long managerId = authService.getUserId(token);

        if (!auths.contains(Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            refundService.completeNewRefund(managerId, refundId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/admin/v2/refunds", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String searchNewRefundList(@RequestHeader("Authorization") String token, @RequestParam Map<String, Object> params,
                                      HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        List<String> rolls = authService.getUserAuthInfo(token);

        if (rolls.contains(Const.ROLE_ASSIMAN)) {
            SearchResultVO vo = refundService.searchNewRefundList(params);
            voResult.setSuccessInfo(vo);
        } else {
            voResult.setFailInfo2NeedAuth();
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    //
    // 기능: 12.9. 관리자용 인출 정보 조회
    @PostMapping(value = "/admin/findAdminCashOut", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String findAdminCashOut(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "findAdminCashOut()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        if (token == null) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("findAdminCashOut(): jsonStr={}", jsonStr);
        AdminCashOutAskVO pvo = new AdminCashOutAskVO(jsonStr);

        if (!authService.containsRole(token, Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        voResult = travelerService.findAdminCashOut(managerId, pvo);
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 9.9. 관리자용 환전 정보 조회
    @PostMapping(value = "/admin/findAdminExchange", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String findAdminExchange(@RequestHeader("Authorization") String token, HttpServletRequest request,
                                    HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("findAdminExchange(): Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);
        logger.info("findAdminExchange(): jsonStr={}", jsonStr);
        AdminExchangeAskVO pvo = (AdminExchangeAskVO) JsonStr.toObject(AdminExchangeAskVO.class.getName(), jsonStr);

        SearchResultVO obj = exchangeService.findAdminExchange(pvo);
        voResult.setSuccessInfo(obj);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/admin/remittance", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAdminRemittance(@RequestHeader("Authorization") String token,
                                     @RequestParam(name = "start_row") String startRow,
                                     @RequestParam(name = "size") String size,
                                     @RequestParam(name = "sort") String sort,
                                     @RequestParam(name = "from_cd", required = false) String fromCd,
                                     @RequestParam(name = "to_cd", required = false) String toCd,
                                     @RequestParam(name = "bank_account_id", required = false) String bankAccountId,
                                     @RequestParam(name = "bank_account_no", required = false) String bankAccountNo,
                                     @RequestParam(name = "begin_created_date") String beginCreatedDate,
                                     @RequestParam(name = "end_created_date") String endCreatedDate,
                                     @RequestParam(name = "remit_status") String remitStatus,
                                     @RequestParam(name = "is_exist_txn", required = false) String isExistTxn,
                                     @RequestParam(name = "paygate_rec_id", required = false) String paygate_rec_id,
                                     @RequestParam(name = "paygate_rec_amount", required = false) String paygate_rec_amount,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("getAdminRemittance(): Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        logger.info("getAdminRemittance() ");

        AdminRemittanceAskVO pvo = new AdminRemittanceAskVO();

        pvo.setStart_row(Integer.valueOf(startRow));
        pvo.setSize(Integer.valueOf(size));
        pvo.setSort(sort);

        pvo.setBegin_created_date(Timestamp.valueOf(beginCreatedDate));
        pvo.setEnd_created_date(Timestamp.valueOf(endCreatedDate));
        pvo.setRemit_status(remitStatus);

        if (!StringUtils.isEmpty(fromCd)) {
            pvo.setFrom_cd(fromCd);
        }

        if (!StringUtils.isEmpty(toCd)) {
            pvo.setTo_cd(toCd);
        }

        if (!StringUtils.isEmpty(bankAccountId)) {
            pvo.setBank_account_id(Integer.valueOf(bankAccountId));
        }

        if (!StringUtils.isEmpty(bankAccountNo)) {
            pvo.setBank_account_no(bankAccountNo);
        }

        // isExistTxn이 파라미터가 있으면 mapping에서 조회, 없으면 report에서 조회
        if (!StringUtils.isEmpty(isExistTxn)) {
            pvo.setIsExistTxn(isExistTxn);
            SearchResultVO obj = remittanceService.searchAdminRemittanceForMapping(pvo);

            voResult.setSuccessInfo(obj);
        } else {
            SearchResultVO obj = remittanceService.searchAdminRemittanceForReport(pvo);

            voResult.setSuccessInfo(obj);
        }


        return JsonStr.toJsonString(voResult, response);
    }

    // -------------------------------------------------------------------------------
    // 60. 통계(관리자 용)
    // -------------------------------------------------------------------------------

    @GetMapping(value = "/admin/receipt-photos", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTransactionReceiptPhotoList(@RequestHeader("Authorization") String token,
                                                 @RequestParam String relatedTxnType, @RequestParam String relatedTxnId,
                                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "getTransactionReceiptPhotoList()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!authService.containsRole(token, Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: relatedTxnType={}, relatedTxnId={}", method, relatedTxnType, relatedTxnId);

        if (relatedTxnType.equals(RelatedTxnType.EXCHANGE.name())) {
            Long exchangeId = Long.valueOf(relatedTxnId);
            List<ExchangeDepositReceipt> receipts = exchangeService.getExchangeDepositReceiptList(exchangeId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<HashMap<String, Object>> obj = mapper.convertValue(receipts, new TypeReference<List<HashMap<String, Object>>>() {
            });
            voResult.setSuccessInfo(obj);
        } else if (relatedTxnType.equals(RelatedTxnType.REMITTANCE.name())) {
            Long remittanceId = Long.valueOf(relatedTxnId);
            List<RemittanceDepositReceipt> receipts = remittanceService.getRemittanceDepositReceiptList(remittanceId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<HashMap<String, Object>> obj = mapper.convertValue(receipts, new TypeReference<List<HashMap<String, Object>>>() {
            });
            voResult.setSuccessInfo(obj);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping("/admin/changeBankId")
    @ResponseBody
    public String changeBankAccountId(@RequestHeader("Authorization") String token,
                                      @RequestBody String encryptedBody,
                                      HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (!authService.containsRole(token, Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        try {
            String json = CustomStringUtil.decode(token, encryptedBody);
            ChangeBankAccountIdRequest requestBody = objectMapper.readValue(json, ChangeBankAccountIdRequest.class);
            logger.info("managerId={}, requestBody={}", managerId, requestBody);

            companyService.changeBankAccountId(requestBody);
            voResult.setSuccessInfo();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }


}
