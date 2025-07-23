package com.cashmallow.api.interfaces.admin.web;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.application.impl.CountryServiceImpl;
import com.cashmallow.api.application.impl.UserServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.company.*;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.*;
import com.cashmallow.api.interfaces.dbs.DbsService;
import com.cashmallow.api.interfaces.dbs.model.dto.CashmallowFxQuotationRequest;
import com.cashmallow.api.interfaces.dbs.model.dto.CashmallowFxQuotationResponse;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsBalanceResponse;
import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import com.cashmallow.api.interfaces.sentbe.facade.SentbeServiceImpl;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.DELETED_API;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

/**
 * Refund, Company
 *
 * @author swshin
 */
@SuppressWarnings({"unchecked","deprecation"})
@RestController
public class BankAccountController {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private DbsProperties dbsProperties;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private PaygateServiceImpl paygateService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private DbsService dbsService;

    @Autowired
    private SentbeServiceImpl sentbeService;

    @GetMapping(value = "/admin/bank-accounts/sentbe/balance", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getSentbeBalance(@RequestHeader("Authorization") String token,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "getSentbeBalance()";
        ApiResultVO resultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            logger.info("{}: No permission.", method);
            resultVO.setFailInfo2NeedAuth();
        }

        try {
            Map<String, Object> result = sentbeService.getSentbeBalance();

            resultVO.setSuccessInfo(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(resultVO, response);
    }

    /**
     * 기간내 통장내역 조회
     *
     * @param token
     * @param response
     * @return
     */
    @GetMapping(value = "/admin/bank-accounts/{bankAccountId}/paygate-records", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String searchPaygateRecords(@RequestHeader("Authorization") String token,
                                       @RequestParam Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) {

        String method = "searchPaygateRecords()";

        List<String> auths = authService.getUserAuthInfo(token);

        logger.info("{}: queryMap={}", method, params);

        ApiResultVO resultVO = new ApiResultVO();
        if (!auths.contains(ROLE_ASSIMAN)) {
            logger.info("{}: No permission.", method);
            resultVO.setFailInfo2NeedAuth();
        }

        List<PaygateRecord> paygateRecordsList;

        if (params.get("hasTransactionRecordId").equals("true")) {
            Long transactionRecordId = Long.valueOf(params.get("transactionRecordId").toString());
            paygateRecordsList = companyService.getPaygateRecordListByTransactionRecordId(transactionRecordId);
        } else {

            String fromCd = params.get("fromCd").toString();

            Country country = countryService.getCountry(fromCd);

            params.put("iso4217", country.getIso4217());

            paygateRecordsList = companyService.getPaygateRecords(params);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Map<String, Object>> result = mapper.convertValue(paygateRecordsList, new TypeReference<List<Map<String, Object>>>() {
        });

        resultVO.setSuccessInfo(result);

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * 수동 맵핑
     * 메뉴 위치 Paygate - 수동 맵핑 - 매칭
     *
     * @param token
     * @param bankAccountId
     * @param manual
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/admin/bank-accounts/{bank_account_id}/transaction-mappings", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String matchTransactionMapping(@RequestHeader("Authorization") final String token,
                                          @PathVariable("bank_account_id") final Long bankAccountId,
                                          @RequestParam(value = "manual", required = false) final boolean manual,
                                          @RequestBody String requestBody,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response) {
        String method = "matchTransactionMapping()";
        logger.info("token={}", token);
        logger.info("bankAccountId={}", bankAccountId);
        logger.info("manual={}", manual);
        logger.info("requestBody={}", requestBody);

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        Long managerId = authService.getUserId(token);

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("{}: jsonStr={}", method, jsonStr);
        logger.info("{}: managerId={}", method, managerId);

        TransactionRecord transactionRecord = new TransactionRecord();
        TransactionMapping transactionMapping = new TransactionMapping();

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);

        transactionRecord.setRelatedTxnId(Long.valueOf(map.get("relatedTxnId").toString()));
        transactionRecord.setRelatedTxnType((String) map.get("relatedTxnType"));
        transactionRecord.setAmount((BigDecimal.valueOf(Double.valueOf(map.get("amount").toString()))));
        transactionRecord.setIso4217((String) map.get("iso4217"));

        transactionRecord.setCreator(managerId);

        ArrayList<String> paygateRecdIds = (ArrayList<String>) map.get("paygateRecIds");
        String inputAccountNo = map.get("inputAccountNo").toString();
        String inputName = map.get("inputName").toString();
        transactionMapping.setCreator(managerId);

        try {
            // if (paygateAccountId.intValue() != bankAccountId) {
            //     logger.error("{}: 이 화면에선 paygate 통장이 아닌 계좌는 매핑할수 없습니다. ", method);
            //     throw new CashmallowException("이 화면에선 paygate통장이 아닌 계좌는 매핑할수 없습니다.");
            // }


            paygateService.matchTransactionMappingForManual(transactionRecord, transactionMapping, paygateRecdIds, bankAccountId, inputAccountNo, inputName, manual);

            voResult.setSuccessInfo();
        } catch (Exception e) {
            voResult.setFailInfo(e.getMessage());
            logger.error(e.getMessage(), e);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = "/admin/bank-accounts/{bankAccountId}/transaction-records", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String createTransactionRecordAndMapping(@RequestHeader("Authorization") String token, @PathVariable Long bankAccountId,
                                                    @RequestBody String requestBody, HttpServletRequest request, HttpServletResponse response) {
        String method = "createTransactionRecord()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        Long managerId = authService.getUserId(token);

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("{}: jsonStr={}", method, jsonStr);

        TransactionRecord transactionRecord = new TransactionRecord();
        TransactionMapping transactionMapping = new TransactionMapping();

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);

        Long paygateRecordId = Long.valueOf((String) map.get("paygateRecordId"));

        transactionRecord.setRelatedTxnType((String) map.get("relatedTxnType"));
        transactionRecord.setAmount((BigDecimal.valueOf(Double.valueOf(map.get("amount").toString()))));
        transactionRecord.setIso4217((String) map.get("iso4217"));
        transactionRecord.setDescription((String) map.get("description"));
        transactionRecord.setCreator(managerId);

        transactionMapping.setCreator(managerId);

        try {
            companyService.createTransactionRecordAndMapping(transactionRecord, transactionMapping, paygateRecordId.toString(), bankAccountId);

            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.debug(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/admin/bank-accounts/{bankAccountId}/transaction-mappings", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTransactionMappingLists(@RequestHeader("Authorization") String token, @PathVariable Long bankAccountId,
                                             HttpServletRequest request, HttpServletResponse response) {

        String method = "getTransactionRecordsListForPaygateRecordId()";

        List<String> auths = authService.getUserAuthInfo(token);

        logger.info("{}: bankAccountId={}", method, bankAccountId);

        Long transactionRecordId = Long.valueOf("0");
        Long paygateRecordId = Long.valueOf("0");

        if (request.getParameter("transactionRecordId") != null) {
            transactionRecordId = Long.valueOf(request.getParameter("transactionRecordId"));
        }
        if (request.getParameter("paygateRecordId") != null) {
            paygateRecordId = Long.valueOf(request.getParameter("paygateRecordId"));
        }

        ApiResultVO resultVO = new ApiResultVO();
        if (auths.contains(ROLE_ASSIMAN)) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            if (!transactionRecordId.equals(Long.valueOf("0"))) {
                List<TransactionMapping> transactionMappingList = companyService
                        .getTransactionMappingByTransactionRecordId(transactionRecordId);

                resultVO.setSuccessInfo(
                        mapper.convertValue(transactionMappingList, new TypeReference<List<Map<String, Object>>>() {
                        }));
            } else if (!paygateRecordId.equals(Long.valueOf("0"))) {
                List<TransactionMapping> transactionMappingList = companyService
                        .getTransactionMappingByPaygateRecordId(paygateRecordId.toString());

                resultVO.setSuccessInfo(
                        mapper.convertValue(transactionMappingList, new TypeReference<List<Map<String, Object>>>() {
                        }));
            }

        } else {
            logger.info("{}: No permission.", method);
            resultVO.setFailInfo2NeedAuth();
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * 기간내 거래내역 조회
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/admin/bank-accounts/{bankAccountId}/transaction-records", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String searchTransactionRecords(@RequestHeader("Authorization") String token,
                                           @RequestParam Map<String, String> params, HttpServletRequest request, HttpServletResponse response) {

        String method = "searchTransactionRecords()";

        List<String> auths = authService.getUserAuthInfo(token);

        logger.info("{}: queryMap={}", method, params);

        ApiResultVO resultVO = new ApiResultVO();
        if (!auths.contains(ROLE_ASSIMAN)) {
            logger.info("{}: No permission.", method);
            resultVO.setFailInfo2NeedAuth();
        }

        List<TransactionRecord> transactionRecordsList;

        if (params.get("hasPaygateRecordId").equals("true")) {
            String paygateRecordId = params.get("paygateRecordId");
            transactionRecordsList = companyService.getTransactionRecordsListByPaygateRecordId(paygateRecordId);
        } else {
            transactionRecordsList = companyService.getTransactionRecordsList(params);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Map<String, Object>> result = mapper.convertValue(transactionRecordsList,
                new TypeReference<List<Map<String, Object>>>() {
                });

        resultVO.setSuccessInfo(result);

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    // Paygate에서 풀링하기 위한 환전 대상 목록들 검색
    @GetMapping(value = "/admin/paygate/exchange-target", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getPaygateExchangeTargetList(@RequestHeader("Authorization") String token,
                                               @RequestParam String targetDate, @RequestParam String toCountryCode,
                                               HttpServletRequest request, HttpServletResponse response) {

        String method = "getPaygateExchangeTargetList()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            logger.info("{}: Insufficient Permissions. managerId={}", method, managerId);
            voResult.setFailInfo2NeedAuth();
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: targetDate={}", method, targetDate);

        try {
            throw new CashmallowException(DELETED_API);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // Paygate에서 환전하기전 환율정보 가져오기
    @GetMapping(value = "/admin/paygate/exchange-rate", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String expectPaygateExchangeRate(@RequestHeader("Authorization") String token,
                                            @RequestParam String inputHKD,
                                            HttpServletRequest request, HttpServletResponse response) {

        String method = "expectPaygateExchangeRate()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: inputHKD={}", method, inputHKD);

        try {
            throw new CashmallowException(DELETED_API);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }


    /**
     * 수기 충전 API
     * paygate 장애로 금액이 조회 되지 않을 때 임시로 충전.
     *
     * @param token
     * @param record
     * @param request
     * @param response
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/admin/paygate/records", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addPaygateRecord(@RequestHeader("Authorization") String token,
                                   @RequestBody PaygateRecordRequestManual record,
                                   HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult) throws Exception {

        String method = "addPaygateRecord()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("request={}", request);
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
            String defaultMessage = objectError.getDefaultMessage();
            logger.error("request error={}", defaultMessage);
            voResult.setFailInfo(defaultMessage);
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            PaygateRecord paygateRecord = paygateService.insertPaygateTransaction(record);
            voResult.setSuccessInfo(paygateRecord);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/admin/paygate/records/delete", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String deletePaygateRecord(@RequestHeader("Authorization") String token,
                                      @RequestBody PaygateRecordDeleteRequest record,
                                      HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult) throws Exception {

        String method = "deletePaygateRecord()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("request={}", request);
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
            String defaultMessage = objectError.getDefaultMessage();
            logger.error("request error={}", defaultMessage);
            voResult.setFailInfo(defaultMessage);
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            paygateService.deletePaygateTransaction(record);
            voResult.setSuccessInfo(record);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 임시로 충전한 내역을 실제 paygate tid로 교체
     *
     * @param token
     * @param matchRequest
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/admin/paygate/replace-records", produces = GlobalConst.PRODUCES)
    public String replacePaygateRecord(@RequestHeader("Authorization") String token,
                                       @RequestBody MatchPaygateRecordRequest matchRequest,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            paygateService.replacePaygateTransaction(matchRequest);
            voResult.setSuccessInfo();
        } catch (Exception e) {
            logger.info(e.getMessage());
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 수기 충전 후 맵핑한 내역 조회.
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/admin/paygate/mapped-temp-records")
    public String getMappedTempRecords(@RequestHeader("Authorization") String token,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        List<PaygateRecord> tempPaygateRecordsByMapped = companyService.findTempPaygateRecordsByMapped();

        voResult.setSuccessInfo(tempPaygateRecordsByMapped);
        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 임시로 충전한 내역을 실제 DBS tid로 교체
     *
     * @param token
     * @param matchRequest
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/admin/dbs/replace-records", produces = GlobalConst.PRODUCES)
    public String replaceDbsRecord(@RequestHeader("Authorization") String token,
                                   @RequestBody MatchPaygateRecordRequest matchRequest,
                                   HttpServletRequest request, HttpServletResponse response) throws Exception {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_ASSIMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            paygateService.replaceDbsTransaction(matchRequest);
            voResult.setSuccessInfo();
        } catch (Exception e) {
            logger.info(e.getMessage());
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 수기 충전 후 DBS 맵핑되지 않은 내역 조회.
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/admin/dbs/mapped-temp-records")
    public String getMappedTempDbsRecords(@RequestHeader("Authorization") String token,
                                          HttpServletRequest request, HttpServletResponse response) throws Exception {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        List<DbsDto> tempPaygateRecordsByMapped = companyService.findTempDbsRecordsByMapped().stream().map(m -> {
            m.setFirstName(securityService.decryptAES256(m.getFirstName()));
            m.setLastName(securityService.decryptAES256(m.getLastName()));
            return m;
        }).collect(Collectors.toList());

        voResult.setSuccessInfo(tempPaygateRecordsByMapped);
        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * dbs 계좌 정보를 조회(추후 list로 변할수 있다.)
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/admin/dbs/account-list")
    public String getDbsAccountList(@RequestHeader("Authorization") String token,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        BankAccount bankAccount = companyService.getBankAccountByBankAccountId(Math.toIntExact(dbsProperties.accountId()));

        voResult.setSuccessInfo(bankAccount);
        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/admin/dbs/fx/info")
    public String getDbsFxInfo(@RequestHeader("Authorization") String token,
                               HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        voResult.setSuccessInfo(dbsService.getDbsFxInfo());
        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/admin/dbs/fx/quotation")
    public String getDbsFxQuotation(@RequestHeader("Authorization") String token,
                                    @RequestBody CashmallowFxQuotationRequest req,
                                    HttpServletResponse response) throws CashmallowException {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        final CashmallowFxQuotationResponse dbsFxQuotation = dbsService.getDbsFxQuotation(managerId, req);
        voResult.setSuccessInfo(dbsFxQuotation);
        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/admin/dbs/fx/quotation/validate")
    public String validateDbsFxQuotation(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            voResult.setSuccessInfo(dbsService.validateDbsFxQuotation());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/admin/dbs/fx/quotation/approve/{quotationId}")
    public String getDbsFxQuotationApprove(@RequestHeader("Authorization") String token,
                                           HttpServletResponse response,
                                           @PathVariable Long quotationId) {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        final String errorMessage = dbsService.getDbsFxQuotationApprove(managerId, quotationId);
        if (StringUtils.isEmpty(errorMessage)) {
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(errorMessage);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * dbs 계좌 정보를 조회(추후 list로 변할수 있다.)
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/admin/paygate-records/repayment")
    public String getPaygateRecordForRepayment(@RequestHeader("Authorization") String token,
                                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        List<PaygateRecord> paygateRecordsList = companyService.getPaygateRecordForRepayment().stream().map(m -> {
            m.setSenderName(securityService.decryptAES256(m.getSenderName()));
            m.setSenderAccountNo(securityService.decryptAES256(m.getSenderAccountNo()));
            return m;
        }).toList();

        voResult.setSuccessInfo(paygateRecordsList);

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * dbs 계좌 정보를 조회(추후 list로 변할수 있다.)
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/admin/dbs/sender/validate")
    public String validateDbsSenderInfo(@RequestHeader("Authorization") String token, @RequestParam Long userId,
                                        @RequestParam String senderName, @RequestParam String senderAccountNo,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        // travler에 typeHandler 걸려있음.
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (ObjectUtils.isEmpty(traveler)) {
            voResult.setSuccessInfo(false);
            voResult.setMessage("정보가 일치하지 않습니다.");

            return JsonStr.toJsonString(voResult, response);
        }

        if (senderName.startsWith("MR ")) {
            senderName = senderName.substring(3);
        } else if (senderName.startsWith("MISS ")) {
            senderName = senderName.substring(5);
        }

        // name이 같을경우 true
        boolean isSamePerson = senderName.equals(traveler.getAccountName());

        voResult.setSuccessInfo(isSamePerson);
        if (!isSamePerson) {
            voResult.setMessage("정보가 일치하지 않습니다.");
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * ADMIN에서 DBS에 반환이체 요청
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/admin/dbs/remittance")
    public String remittanceToDbs(@RequestHeader("Authorization") String token, @RequestBody @Valid DbsRefundRemittanceRequest requestBody,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(requestBody.getUserId());
        PaygateRecord paygateRecord = companyService.getPaygateRecord(requestBody.getTid());

        try {
            RepaymentHistory repaymentHistory = RepaymentHistory.of(paygateRecord.getIso4217(), requestBody.getAmount(),
                    requestBody.getFee(), traveler.getId(), managerId);
            companyService.insertRepaymentHistory(repaymentHistory);
            dbsService.requestRemittance(traveler, paygateRecord, requestBody.getAmount(), managerId, RelatedTxnType.REPAYMENT, repaymentHistory.getId());
            voResult.setSuccessInfo("이체 성공");
        } catch (CashmallowException e) {
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * ADMIN에서 반환이체 내역 조회
     */
    @GetMapping(value = "/admin/dbs/remittance", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAdminDbsRemittance(@RequestHeader("Authorization") String token,
                                        @RequestParam(name = "startRow") String startRow,
                                        @RequestParam(name = "size") String size,
                                        @RequestParam(name = "sort") String sort,
                                        @RequestParam(name = "id", required = false) String id,
                                        @RequestParam(name = "userId", required = false) Long userId,
                                        @RequestParam(name = "amount", required = false) BigDecimal amount,
                                        @RequestParam(name = "currency", required = false) String currency,
                                        @RequestParam(name = "beginCreatedDate") String beginCreatedDate,
                                        @RequestParam(name = "endCreatedDate") String endCreatedDate,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("getAdminDbsRemittance(): Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        logger.info("getAdminDbsRemittance()");

        AdminDbsRemittanceAskVO pvo = new AdminDbsRemittanceAskVO();

        pvo.setStartRow(Integer.valueOf(startRow));
        pvo.setSize(Integer.valueOf(size));
        pvo.setSort(sort);

        pvo.setBeginCreatedDate(Timestamp.valueOf(beginCreatedDate));
        pvo.setEndCreatedDate(Timestamp.valueOf(endCreatedDate));

        if (!ObjectUtils.isEmpty(id)) {
            pvo.setId(id);
        }

        if (!ObjectUtils.isEmpty(userId)) {
            pvo.setUserId(userId);
        }

        if (!ObjectUtils.isEmpty(amount)) {
            pvo.setAmount(amount);
        }

        if (!ObjectUtils.isEmpty(currency)) {
            pvo.setCurrency(currency);
        }
        pvo.setRelatedTxnType(RelatedTxnType.REPAYMENT.name());

        SearchResultVO obj = companyService.getAdminDbsRemittance(pvo);

        voResult.setSuccessInfo(obj);

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * Dbs 계좌에 있는 금액정보 조회
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/admin/dbs/balance")
    public String getDbsBalance(@RequestHeader("Authorization") String token,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        if (!userService.isVerifyRole(managerId, ROLE_ADMIN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        DbsBalanceResponse balanceResult = dbsService.getDbsAccountBalance(managerId);
        AdminBalanceVo balanceVo = getAdminBalanceVo(balanceResult);

        voResult.setSuccessInfo(balanceVo);

        return JsonStr.toJsonString(voResult, response);
    }

    private static AdminBalanceVo getAdminBalanceVo(DbsBalanceResponse balanceResult) {
        AdminBalanceVo balanceVo = new AdminBalanceVo();

        for (DbsBalanceResponse.BalanceData balance : balanceResult.getBalanceList()) {
            switch (balance.getCurrency()) {
                case "HKD" -> balanceVo.setHKD(new AdminBalanceVo.AdminBalance(balance.getCurrency(), balance.getBalance()));
                case "USD" -> balanceVo.setUSD(new AdminBalanceVo.AdminBalance(balance.getCurrency(), balance.getBalance()));
                case "KRW" -> balanceVo.setKRW(new AdminBalanceVo.AdminBalance(balance.getCurrency(), balance.getBalance()));
            }
        }
        return balanceVo;
    }

    /**
     * PaygateRecord 테이블의 잔액조회
     *
     * @param token
     * @param response
     * @return
     */
    @GetMapping(value = "/admin/paygate-record/balance", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getPaygateRecordBalance(@RequestHeader("Authorization") String token,
                                          HttpServletRequest request, HttpServletResponse response) {
        String method = "getPaygateRecordBalance()";
        ApiResultVO resultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            logger.info("{}: No permission.", method);
            resultVO.setFailInfo2NeedAuth();
        }

        try {
            AdminBalanceVo balanceVo = new AdminBalanceVo();
            BigDecimal hkdAmount = companyService.getPaygateRecordLastBalance(dbsProperties.accountId(), "HKD");
            BigDecimal usdAmount = companyService.getPaygateRecordLastBalance(dbsProperties.accountId(), "USD");
            BigDecimal krwAmount = companyService.getPaygateRecordLastBalance(dbsProperties.accountId(), "KRW");

            balanceVo.setHKD(new AdminBalanceVo.AdminBalance("HKD", hkdAmount));
            balanceVo.setUSD(new AdminBalanceVo.AdminBalance("USD", usdAmount));
            balanceVo.setKRW(new AdminBalanceVo.AdminBalance("KRW", krwAmount));

            resultVO.setSuccessInfo(balanceVo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(resultVO, response);
    }

    @PostMapping(value = "/admin/paygate-record/{paygateRecordId}/repayment", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addRepayment(@RequestHeader("Authorization") String token,
                               @PathVariable String paygateRecordId,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("addRepayment(): paygateRecordId={}", paygateRecordId);

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);
        if (managerId == NO_USER_ID) {
            logger.info("addRepayment(): Invalid token. managerId={}", managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        Long userId = authService.getUserId(token);
        if (!userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            voResult.setFailInfo(MSG_NEED_AUTH);
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            PaygateRecord paygateRecord = companyService.getPaygateRecord(paygateRecordId);
            if (StringUtils.isEmpty(paygateRecord.getSenderName())) {
                throw new CashmallowException("Sender 정보가 없으면 반환 등록 할수 없습니다.");
            }
            paygateRecord.setWorkStatus(PaygateRecord.WorkStatus.REPAYMENT);
            companyService.updatePaygateRecord(paygateRecord);

            voResult.setSuccessInfo(paygateRecordId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

}
