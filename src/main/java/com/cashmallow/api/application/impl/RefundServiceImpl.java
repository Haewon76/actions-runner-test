package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.domain.model.aml.AmlAccountBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranSendReceipt;
import com.cashmallow.api.domain.model.aml.AmlProdBase;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.*;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.refund.*;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.aml.OctaAMLKYCService;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLKYCRequest;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLKYCResponse;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.cashmallow.api.interfaces.traveler.dto.JpRefundAccountInfoRequest;
import com.cashmallow.api.interfaces.traveler.dto.RefundJpRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cashmallow.api.domain.shared.Const.REFUND_CHANGED_EXCHANGE_RATE;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.JP_REFUND_IN_PROGRESS_NOT_CHANGE_ACCOUNT;

@Service
@Validated
public class RefundServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RefundServiceImpl.class);

    private static final String REFUND_ERROR_REQUEST_IDENTIFICATION_REQUIRED = "REFUND_ERROR_REQUEST_IDENTIFICATION_REQUIRED";
    private static final String REFUND_ERROR_REQUEST_BANK_ACCOUNT_REQUIRED = "REFUND_ERROR_REQUEST_BANK_ACCOUNT_REQUIRED";
    private static final String REFUND_ERROR_REQUEST_TRAVELER_NOT_ENOUGH_MONEY = "REFUND_ERROR_REQUEST_TRAVELER_NOT_ENOUGH_MONEY";

    @Autowired
    RemittanceServiceImpl remittanceService;

    @Autowired
    RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    TravelerRepositoryService travelerRepositoryService;

    @Autowired
    WalletRepositoryService walletRepositoryService;

    @Autowired
    UserRepositoryService userRepositoryService;

    @Autowired
    UserAdminService userAdminService;

    @Autowired
    CurrencyService currencyService;

    @Autowired
    CountryService countryService;

    @Autowired
    FileService fileService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private PaygateServiceImpl paygateService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    RefundMapper refundMapper;

    @Autowired
    RefundRepositoryService refundRepositoryService;

    @Autowired
    SecurityService securityService;

    @Autowired
    private OctaAMLKYCService octaAMLKYCService;
    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private ExchangeCalculateServiceImpl exchangeCalculateService;
    @Autowired
    private CouponMobileServiceV2 couponMobileService;
    @Autowired
    private CouponUserService couponUserService;

    // -------------------------------------------------------------------------------
    // 29. 환불
    // -------------------------------------------------------------------------------

    /**
     * 기능: 어드민 환불 조회(NewRefund 테이블 조회)
     *
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO searchNewRefundList(Map<String, Object> params) {
        int startRow = Integer.parseInt(params.get("start_row").toString());
        int size = Integer.parseInt(params.get("size").toString());
        String sort = params.get("sort").toString();

        int page = (startRow + size) / size - 1;
        SearchResultVO vo = new SearchResultVO(page, size, sort);

        int count = refundMapper.countSearchNewRefundList(params);
        List<Object> vos = refundMapper.searchNewRefundList(params);

        List<Object> refundResults = new ArrayList<>();

        for (Object obj : vos) {
            Map<String, String> refund = (Map) obj;
            refund.put("first_name", securityService.decryptAES256(refund.get("first_name")));
            refund.put("last_name", securityService.decryptAES256(refund.get("last_name")));
            refund.put("account_no", securityService.decryptAES256(refund.get("account_no")));
            refund.put("account_name", securityService.decryptAES256(refund.get("account_name")));
            refund.put("email", securityService.decryptAES256(refund.get("email")));
            refund.put("from_cd_name", CountryCode.of(refund.get("from_cd")).getName());
            refund.put("to_cd_name", CountryCode.of(refund.get("to_cd")).getName());

            refundResults.add(refund);
        }

        vo.setResult(refundResults, count, page);

        return vo;
    }

    /**
     * Get refund amount statistics by country
     *
     * @param country
     * @return : totalCnt, totalTotal, comCnt, comTotal, reqCnt, reqTotal
     */
    public Map<String, Object> getRefundAmountByCountry(String country) {
        return refundMapper.getRefundAmountByCountry(country);
    }

    public ExchangeCalcVO calcRefundRemit(Long remitId) throws CashmallowException {
        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        ExchangeCalcVO pvo = new ExchangeCalcVO();
        pvo.setFrom_cd(remittance.getToCd());
        pvo.setTo_cd(remittance.getFromCd());
        pvo.setFrom_money(remittance.getToAmt());
        pvo.setCouponUserId(remittance.getCouponUserId());
        pvo.setDiscountAmount(remittance.getCouponDiscountAmt());

        return calcRefund(pvo);
    }

    public ExchangeCalcVO calcRefundExchange(Long walletId) throws CashmallowException {
        TravelerWallet wallet = walletRepositoryService.getTravelerWallet(walletId);

        ExchangeCalcVO pvo = new ExchangeCalcVO();
        pvo.setFrom_cd(wallet.getCountry());
        pvo.setTo_cd(wallet.getRootCd());
        pvo.setFrom_money(sumWalletEMoney(walletId));

        Exchange exchange = getExchangeByWallet(wallet);
        pvo.setCouponUserId(exchange.getCouponUserId());
        pvo.setDiscountAmount(exchange.getCouponDiscountAmt());

        return calcRefund(pvo);
    }

    // edited by Alex 20170719 기능 : 29.1.0 여행자 환불 신청 통화가 타국일 경우 환전 계산
    public ExchangeCalcVO calcRefund(ExchangeCalcVO pvo) throws CashmallowException {

        String method = "calcRefund()";

        logger.info("{}: pvo={}", method, pvo);

        if (pvo == null) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Country country = countryService.getCountry(pvo.getTo_cd());

        BigDecimal mappingInc = country.getMappingInc();
        int scale = -1 * (int) Math.floor(Math.log10(mappingInc.doubleValue()));

        // 1. 기준환율
        BigDecimal baseRate = currencyService.getCurrencyTarget(pvo.getFrom_cd(), pvo.getTo_cd());

        // 2. 국가간 수수료율
        // 환전 fromCd = HKD, toCd = KRW
        // 하지만 앱에서 환불 시 fromCd = KRW, toCd = HKD 반대로 오고있음.
        // 환전시 HKD -> KRW 는 환불 시 HKD -> KRW 수수료를 적용해야함.
        // 기존 코드는 환불 시 KRW -> HKD의 수수료를 적용하고 있어서 수정함.
        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(pvo.getTo_cd(), pvo.getFrom_cd());
        BigDecimal spreadRate = exchangeCalculateService.getSpreadRate(baseRate, exchangeConfig.getFeeRateExchange());

        BigDecimal refundFeePer = exchangeConfig.getRefundFeePer() == null ? BigDecimal.ZERO : exchangeConfig.getRefundFeePer();

        // 3. to_money 계산
        BigDecimal fromMoney = pvo.getFrom_money();

        BigDecimal toMoney = fromMoney.divide(baseRate, scale, RoundingMode.FLOOR);
        BigDecimal spreadToMoney = fromMoney.divide(spreadRate, scale, RoundingMode.FLOOR);

        // 4. 환전 수수료 계산
        BigDecimal feeRateAmt = toMoney.subtract(spreadToMoney);

        BigDecimal fee = feeRateAmt.add(refundFeePer);

        BigDecimal refundAmount = toMoney.subtract(fee);
        // 5. 쿠폰 존재시 쿠폰 값 제외
        if (pvo.getDiscountAmount() != null) {
            refundAmount = refundAmount.subtract(pvo.getDiscountAmount());
        }

        pvo.setFee_rate_amt(feeRateAmt);
        pvo.setFee_per_amt(refundFeePer);
        pvo.setTo_money(toMoney.subtract(feeRateAmt));
        pvo.setFee(fee);
        pvo.setExchange_rate(baseRate);
        pvo.setFee_rate(exchangeConfig.getFeeRateExchange());
        pvo.setRefundAmount(refundAmount);

        logger.info("{}: pvoResult={}", method, pvo);

        return pvo;
    }

    /**
     * Request newRefund according to RelatedTxnType.
     *
     * @param traveler
     * @param newRefund
     * @param txnType
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public NewRefund requestNewRefund(Traveler traveler, NewRefund newRefund, RelatedTxnType txnType) throws CashmallowException {
        String method = "requestNewRefund()";

        if (newRefund == null) {
            logger.error("{}: {}", method, Const.CODE_INVALID_PARAMS);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        validateRequestRefund(traveler.getUserId(), traveler, method);

        newRefund.setTravelerId(traveler.getId());
        newRefund.setRelatedTxnType(txnType);

        // from JP가 아닐때만 traveler snapshot 추가
        if (!CountryCode.JP.getCode().equals(newRefund.getToCd())) {
            newRefund.setTrBankName(traveler.getBankName());
            newRefund.setTrAccountNo(traveler.getAccountNo());
            newRefund.setTrAccountName(traveler.getAccountName());
        }

        List<NewRefund> inProgressNewRefundList = refundRepositoryService.getNewRefundListInProgressByTravelerId(traveler.getId());

        if (!inProgressNewRefundList.isEmpty()) {
            logger.error("{}: There is a refund in progress. userId={}", method, traveler.getUserId());
            throw new CashmallowException("REFUND_ERROR_REQUEST_PROCESS_IN_REFUND");
        }

        // to_amt 계산을 위한 mapping_inc 가져오기.
        Country toCountry = countryService.getCountry(newRefund.getToCd());
        Country fromCountry = countryService.getCountry(newRefund.getFromCd());
        BigDecimal mappingInc = toCountry.getMappingInc();
        int scale = -1 * (int) Math.floor(Math.log10(mappingInc.doubleValue()));

        BigDecimal fromMoney = newRefund.getFromAmt();

        logger.info("{}: ###### 환불 신청 정보 : fromMoney:{}", method, fromMoney);

        if (fromMoney.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("{}: {}", method, REFUND_ERROR_REQUEST_TRAVELER_NOT_ENOUGH_MONEY);
            throw new CashmallowException(REFUND_ERROR_REQUEST_TRAVELER_NOT_ENOUGH_MONEY);
        }

        String fromCountryCd = newRefund.getFromCd();
        String toCountryCd = newRefund.getToCd();
        if (fromCountryCd.equals(toCountryCd)) {
            // 환불 화폐와 from_cd 동일하면 에러
            logger.error("{}: 환불 화폐와 from_cd 동일함.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int walletCountForSameExchange = 1;
        if (RelatedTxnType.EXCHANGE.equals(txnType)) {
            TravelerWallet wallet = walletRepositoryService.getTravelerWallet(newRefund.getWalletId());
            walletCountForSameExchange = getWalletCountForSameExchange(wallet.getExchangeIds());
            wallet = walletRepositoryService.updateRelatedWalletForRefund(newRefund.getWalletId(), fromMoney);

            Exchange exchange = getExchangeByWallet(wallet);
            newRefund.setCouponUserId(exchange.getCouponUserId());
            newRefund.setCouponDiscountAmount(exchange.getCouponDiscountAmt());
            newRefund.setExchangeId(exchange.getId());

        } else if (RelatedTxnType.REMITTANCE.equals(txnType)) {
            Remittance remittance = remittanceService.updateForRefund(newRefund.getRemitId(), fromMoney);
            newRefund.setCouponUserId(remittance.getCouponUserId());
            newRefund.setCouponDiscountAmount(remittance.getCouponDiscountAmt());
        }

        logger.info("{}: ###### 환불 신청 정보 ===============================================", method);
        logger.info("{}: from_country={}, to_country={}, from_money={}, fee_per_amt={}, fee_rate_amt={}", method, fromCountryCd, toCountryCd, fromMoney, newRefund.getFeePerAmt(), newRefund.getFeeRateAmt());
        logger.info("{}: fee={}, from_amt={}, to_amt={}, exchange_rate={}", method, newRefund.getFee(), newRefund.getFromAmt(), newRefund.getToAmt(), newRefund.getExchangeRate());
        logger.info("{}: coupon_user_id={}", method, newRefund.getCouponUserId());

        CurrencyRate currencyRate = getCurrencyRate(toCountry, fromCountry);
        // 환불 당시의 FeeRate 수수료율 기록 추가
        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(toCountry.getCode(), fromCountry.getCode());
        newRefund.setFeeRate(exchangeConfig.getFeeRateExchange());

        BigDecimal baseRate = currencyRate.getRate();
        newRefund.setBaseExchangeRate(currencyRate.getBaseRate());

        logger.info("{}: targetCurrencyRate={}", method, baseRate);

        if (newRefund.getExchangeRate().compareTo(baseRate) != 0) {
            logger.error("{}: 환불신청 오류 : 요청 환율이 다릅니다. - 요청 : {}, 기준 : {}", method, newRefund.getExchangeRate(), baseRate); // transaction 처리용 메시지
            throw new CashmallowException(REFUND_CHANGED_EXCHANGE_RATE);
        }

        BigDecimal toAmt = newRefund.getFromAmt().divide(baseRate, scale, RoundingMode.FLOOR);
        toAmt = toAmt.subtract(newRefund.getFee());

        // 쿠폰 금액 차감
        if (newRefund.getCouponDiscountAmount() != null) {
            toAmt = toAmt.subtract(newRefund.getCouponDiscountAmount());
        }

        validateRefundAmount(newRefund.getToAmt(), toAmt, method);

        // 5. 환불 신청한다.
        boolean isValidRequestRefund = checkRequestRefund(newRefund.getToCd(), newRefund.getToAmt(), newRefund.getFromCd());
        if (!isValidRequestRefund) {
            logger.error("{}: Refund data is not correct.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int affectedRow = refundMapper.insertNewRefund(newRefund);
        if (affectedRow != 1) {
            logger.error("{}: Cannot insert refund", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        postSlackMessage(traveler.getUserId(), newRefund, txnType, walletCountForSameExchange, fromCountry, toCountry);

        return newRefund;
    }

    private Integer getWalletCountForSameExchange(String exchangeIds) {

        JSONObject jsonExchangeIds = new JSONObject(exchangeIds);
        JSONArray exchangeArray = jsonExchangeIds.getJSONArray(Const.EXCHANGE_IDS);
        String exchangeId = exchangeArray.get(0).toString();

        List<TravelerWallet> walletLists = walletRepositoryService.getTravelerWalletByExchangeIds(exchangeId);

        return walletLists.size();
    }

    private Boolean checkRequestRefund(String toCd, BigDecimal toAmt, String fromCd) throws CashmallowException {

        String method = "checkRequestRefund()";

        logger.info("{}: country={}, amount={}", method, toCd, toAmt);

        if (StringUtils.isEmpty(toCd) || toAmt.compareTo(new BigDecimal(0)) <= 0) {
            logger.error("{}: Invalid parameters", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        try {
            // 환불은 toCd,와 fromCd가 반대
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(toCd, fromCd);

            BigDecimal maxRefund = exchangeConfig.getFromMaxRefund();
            BigDecimal minRefund = BigDecimal.valueOf(0.001);
            logger.info("{}: minRefund={}, maxRefund={}",
                    method, minRefund, maxRefund);
            logger.info("{}: to_amt={}", method, toAmt);

            return (minRefund.compareTo(toAmt) <= 0 && toAmt.compareTo(maxRefund) <= 0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Admin에서 OP상태인 Refund들 접수(OP -> MP)
     **/
    @Transactional(rollbackFor = CashmallowException.class)
    public void receiptRefund(Long adminUserId, String country) throws CashmallowException {
        String method = "receiptRefund()";
        logger.info("{}: adminUserId={}, country={}", method, adminUserId, country);

        List<NewRefund> opRefundList = refundRepositoryService.getNewRefundByCountry(country, NewRefund.RefundStatusCode.OP);

        if (opRefundList.isEmpty()) {
            return;
        }

        for (NewRefund refund : opRefundList) {
            refund.setRefundStatus(NewRefund.RefundStatusCode.MP);
        }
        refundRepositoryService.updateNewRefundList(opRefundList);

        List<RefundStatus> statusList = opRefundList.stream().map(refund -> RefundStatus.Of(refund, adminUserId, null)).toList();
        refundMapper.insertRefundStatusList(statusList);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelNewRefundByCashmallow(Long adminUserId, Long refundId) throws CashmallowException {
        String method = "cancelRefundByRefundId()";
        logger.info("{}: adminUserId={}, refundId={}", method, adminUserId, refundId);

        NewRefund newRefund = refundRepositoryService.getNewRefundById(refundId);

        if (newRefund == null) {
            logger.error("{}: newRefundId를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // MP인것만 통과
        if (!NewRefund.RefundStatusCode.MP.equals(newRefund.getRefundStatus())) {
            logger.error("{}: cannot find the newRefund information in progress. refundStatus={}", method, newRefund.getRefundStatus());
            throw new CashmallowException("REFUND_CANCEL_ERROR_IN_PROCESS");
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(newRefund.getTravelerId());

        if (traveler == null) {
            logger.error("{}: travelerId를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        User user = userRepositoryService.getUserByTravelerId(traveler.getId());
        if (RelatedTxnType.EXCHANGE.equals(newRefund.getRelatedTxnType())) {
            walletRepositoryService.updateRelatedWalletForRefundCancel(newRefund.getWalletId(), newRefund.getFromAmt());
        } else if (RelatedTxnType.REMITTANCE.equals(newRefund.getRelatedTxnType())) {
            remittanceService.updateForRefundCancel(newRefund.getRemitId(), newRefund.getFromAmt());
        }
        newRefund.setRefundStatus(NewRefund.RefundStatusCode.CC);

        refundMapper.updateNewRefund(newRefund);
        refundMapper.insertRefundStatus(RefundStatus.Of(newRefund, adminUserId, null));

        // Send FCM to traveler
        notificationService.addFcmNotificationMsg(user, FcmEventCode.RF, FcmEventValue.CC, 0L);

        // alarmService.aAlert("환불취소(어드민)", "작업자 ID:" + adminUserId);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelNewRefundByTraveler(Long refundId) throws CashmallowException {
        String method = "cancelNewRefundByTraveler()";
        logger.info("{}: refundId={}", method, refundId);

        NewRefund newRefund = refundRepositoryService.getNewRefundById(refundId);

        if (newRefund == null) {
            logger.error("{}: newRefundId를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // OP인것만 통과
        if (!NewRefund.RefundStatusCode.OP.equals(newRefund.getRefundStatus())) {
            logger.error("{}: cannot find the newRefund information in progress. refundStatus={}", method, newRefund.getRefundStatus());
            throw new CashmallowException("REFUND_CANCEL_ERROR_IN_PROCESS");
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(newRefund.getTravelerId());

        if (traveler == null) {
            logger.error("{}: travelerId를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (RelatedTxnType.EXCHANGE.equals(newRefund.getRelatedTxnType())) {
            walletRepositoryService.updateRelatedWalletForRefundCancel(newRefund.getWalletId(), newRefund.getFromAmt());
        } else if (RelatedTxnType.REMITTANCE.equals(newRefund.getRelatedTxnType())) {
            remittanceService.updateForRefundCancel(newRefund.getRemitId(), newRefund.getFromAmt());
        }
        newRefund.setRefundStatus(NewRefund.RefundStatusCode.TC);

        refundMapper.updateNewRefund(newRefund);
        refundMapper.insertRefundStatus(RefundStatus.Of(newRefund, traveler.getUserId(), null));

        if (newRefund.getToCd().equals(CountryCode.JP.getCode())) {
            // 환불 취소 시에서는 거래 그대로 진행하기 때문에 쿠폰 롤백 하지 않으므로 -1L(사용안함)로 지정함
            globalQueueService.sendTransactionCancel(RelatedTxnType.REFUND, newRefund.getId(), newRefund.getRefundStatus().name(), -1L);
        }

        alarmService.aAlert("환불취소(여행자)", "여행자 User ID:" + traveler.getUserId(), userRepositoryService.getUserByUserId(traveler.getUserId()));
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void completeNewRefund(Long managerId, Long refundId) throws CashmallowException {
        String method = "completeNewRefund(): ";

        NewRefund refund = refundMapper.getNewRefundById(refundId);

        // MP, AP가 아니면 완료 불가능
        if (NewRefund.RefundStatusCode.CC.equals(refund.getRefundStatus()) ||
                NewRefund.RefundStatusCode.TC.equals(refund.getRefundStatus()) ||
                NewRefund.RefundStatusCode.CF.equals(refund.getRefundStatus()) ||
                NewRefund.RefundStatusCode.OP.equals(refund.getRefundStatus())) {
            logger.error("{}: Do not complete New Refund. status={}, refundId={}", method, refund.getRefundStatus(), refundId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(refund.getTravelerId());
        if (traveler == null) {
            logger.error("{}: Can not find the traveler. userId={}", method, refund.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        User user = userRepositoryService.getUserByUserId(traveler.getUserId());

        refund.setRefundStatus(NewRefund.RefundStatusCode.CF);

        int affectedRow = refundMapper.updateNewRefund(refund);
        refundMapper.insertRefundStatus(RefundStatus.Of(refund, managerId, null));

        if (affectedRow != 1) {
            logger.error("{}: Failed to update Refund Calc. affectedRow={}", method, affectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 환불 완료 시점에 쿠폰이 원복 되어야 하므로 여기서 쿠폰 원복 처리
        // 송금일 경우 remit_id 컬럼에, 화전일 경우 exchange_id/wallet_id 컬럼에서 가져와야하고 필요한 값만 가져오기 위해 추가함
        CouponNewRefund couponNewRefund = refundRepositoryService.getNewRefundExchangeOrRemitById(refundId);
        if (couponNewRefund.getCouponUserId() != null) {
            Long thankYouTooCouponUserId = couponMobileService.cancelCouponUserV2(couponNewRefund.getCouponUserId(), null);
            logger.info("thankYouTooCouponUserId:{}", thankYouTooCouponUserId);
            // 환불 후, REVOKED 되거나 EXPIRED 될 경우, JP 서버도 쿠폰 상태 동기화를 위해 추가
            List<UpdateStatusUserCoupon> userCouponList = new ArrayList<>();
            if (CountryCode.JP.getCode().equals(refund.getToCd())) { // 환불은 from 과 to 가 반대
                CouponIssueUser couponIssueUser = couponUserService.getCouponUserById(couponNewRefund.getCouponUserId());

                UpdateStatusUserCoupon updateStatusUserCoupon = null;
                String availableStatus = null;
                if (AvailableStatus.REVOKED.name().equals(couponIssueUser.getAvailableStatus())) {
                    availableStatus = AvailableStatus.REVOKED.name();
                    updateStatusUserCoupon = UpdateStatusUserCoupon.ofRevoke(couponIssueUser);

                    // 초대 완료 쿠폰 JP 서버도 REVOKED 될 경우, 쿠폰 상태 동기화를 위한 작업 추가
                    // 초대완료 쿠폰을 받은 유저가 target_user_id, 초대쿠폰 사용한 유저가 invite_user_id
                    CouponIssueUser couponIssueUserThxToo = couponUserService.getCouponUserById(thankYouTooCouponUserId);
                    if (couponIssueUserThxToo != null) {
                        UpdateStatusUserCoupon updateStatusUserCouponThxToo = UpdateStatusUserCoupon.ofRevoke(couponIssueUserThxToo);
                        userCouponList.add(updateStatusUserCouponThxToo);
                    }
                    userCouponList.add(updateStatusUserCoupon);
                } else if (AvailableStatus.EXPIRED.name().equals(couponIssueUser.getAvailableStatus())) {
                    availableStatus = AvailableStatus.EXPIRED.name();
                    updateStatusUserCoupon = UpdateStatusUserCoupon.builder()
                                                                    .couponIssueUserSyncId(couponIssueUser.getId())
                                                                    .couponIssueSyncId(couponIssueUser.getCouponIssueId())
                                                                    .userId(couponIssueUser.getTargetUserId())
                                                                    .build();
                    userCouponList.add(updateStatusUserCoupon);
                }

                globalQueueService.setUpdateStatusUserCoupon(userCouponList, availableStatus);
            }
        }

        Country fromCountry = countryService.getCountry(refund.getFromCd());
        Country toCountry = countryService.getCountry(refund.getToCd());

        if (RelatedTxnType.EXCHANGE.equals(refund.getRelatedTxnType())) {
            TravelerWallet w = walletRepositoryService.getTravelerWallet(refund.getWalletId());

            // Delete traveler wallet
            walletRepositoryService.deleteWalletForRefund(w.getId(), refund.getFromAmt());

            // Send Email to traveler
            notificationService.sendEmailConfirmNewRefundForExchange(user, traveler, refund, fromCountry, toCountry);
        } else if (RelatedTxnType.REMITTANCE.equals(refund.getRelatedTxnType())) {
            // Update remittance
            remittanceService.updateForRefundComplete(refund.getRemitId(), refund.getFromAmt());

            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(refund.getRemitId());

            //  DEV-671에 의해 환불시 송금완료 e-mail을 먼저 발송후 환불 영수증 발송
            // 환불의 Tocountry = 송금의 FromCountry라서, 바꿔서 넣음.
            notificationService.sendEmailConfirmRemittance(user, traveler, remittance, toCountry, fromCountry);
            notificationService.sendEmailConfirmNewRefundForRemittance(user, traveler, refund, fromCountry, toCountry, remittance);
        }

        // Send FCM to traveler
        notificationService.addFcmNotificationMsg(user, FcmEventCode.RF, FcmEventValue.CF, 0L);

        // Send Slack message
        alarmService.aAlert("환불완료", "[ADMIN] 여행자 환불 완료 (user_id) :" + user.getId() + " / new_refund ID : " + refundId, user);

        if (RelatedTxnType.EXCHANGE.equals(refund.getRelatedTxnType())) {
            // octa 환전 환불 완료 데이터 전송
            sendRefund2OctaAML(refund, user, fromCountry, toCountry, traveler);
        }
    }

    private BigDecimal sumWalletEMoney(Long walletId) {
        List<TravelerWallet> relatedWallets = walletRepositoryService.getRelatedWallets(walletId);

        return relatedWallets.stream()
                .map(TravelerWallet::geteMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Exchange getExchangeByWallet(TravelerWallet wallet) {
        Long exchangeId = getExchangeId(wallet);
        return exchangeRepositoryService.getExchangeByExchangeId(exchangeId);
    }

    private Long getExchangeId(TravelerWallet wallet) {
        if (wallet.getExchangeIds() == null) {
            return null;
        }
        JSONObject exchangeIds = new JSONObject(wallet.getExchangeIds());
        JSONArray exchangeArray = exchangeIds.getJSONArray(Const.EXCHANGE_IDS);
        return Long.parseLong(exchangeArray.get(0).toString());
    }

    private CurrencyRate getCurrencyRate(Country toCountry, Country fromCountry) {
        // to_money 에 환율을 곱하는 방식으로 계산하고 있어서 source 에 toCountry, target에 fromContry 로 조회해야 함.
        // KRW를 TWD로 환젼할 경우, 1TWD(source)를 매입하는데 필요한 KRW(target)가 얼마인지 계산하는 방식.
        Map<String, Object> params = new HashMap<>();
        params.put("source", toCountry.getIso4217());
        params.put("target", fromCountry.getIso4217());
        return currencyService.getCurrencyRate(params);
    }

    private void validateRefundAmount(BigDecimal clientAmount, BigDecimal serverAmount, String method) throws CashmallowException {
        logger.info("{}: requestRefundAmount={}, serverRequestRefundAmount={}", method,
                clientAmount, serverAmount);

        if (clientAmount.compareTo(serverAmount) != 0) {
            logger.error("{}: 환불 신청 오류 : 요청 금액이 다릅니다. - 요청={}, 기준={}: ", method, clientAmount, serverAmount);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    private void validateRequestRefund(Long userId, Traveler traveler, String method) throws CashmallowException {
        if (traveler == null) {
            logger.error("{}: traveler 정보가 없습니다. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (!"Y".equals(traveler.getCertificationOk())) {
            logger.error("{}: 여권 승인되지 않은 사용자 입니다. passportOk={}", method, traveler.getCertificationOk());
            throw new CashmallowException(REFUND_ERROR_REQUEST_IDENTIFICATION_REQUIRED);
        }

        if (!"Y".equals(traveler.getAccountOk())) {
            logger.error("{}: 통장 인증되지 않은 사용자 입니다. accountOk={}", method, traveler.getAccountOk());
            throw new CashmallowException(REFUND_ERROR_REQUEST_BANK_ACCOUNT_REQUIRED);
        }
    }

    private void postSlackMessage(Long userId, NewRefund refund, RelatedTxnType txnType, int walletCountForSameExchange, Country fromCountry, Country toCountry) {
        String txnIdmsg = "";
        if (RelatedTxnType.EXCHANGE.equals(txnType)) {
            JSONObject jsonExchangeIds = new JSONObject(refund.getExchangeIds());
            JSONArray exchangeArray = jsonExchangeIds.getJSONArray(Const.EXCHANGE_IDS);
            txnIdmsg = "\n환전 거래번호: " + exchangeArray;
        } else if (RelatedTxnType.REMITTANCE.equals(txnType)) {
            txnIdmsg = "\n송금 거래번호: " + refund.getRemitId();
        }

        String message = "환불 거래번호: " + refund.getId() +
                "\n유저 ID:" + userId + " (지갑" + walletCountForSameExchange + "개)" + txnIdmsg +
                "\n지갑 금액: " + refund.getFromAmt() + " " + fromCountry.getIso4217() +
                "\n환불 금액: " + refund.getAmount() + " " + toCountry.getIso4217();

        alarmService.aAlert("자동 환불신청(여행자)", message, userRepositoryService.getUserByUserId(userId));
    }

    public int setRefundTidOutId(Long refundId, String paygateRecOutId) {
        // set refund data
        NewRefund refund = new NewRefund();
        refund.setId(refundId);
        refund.setPaygateRecOutId(paygateRecOutId);

        return refundMapper.setRefundTidOutId(refund);
    }


    /**
     * 환전 신청 완료시 거래내역 AML 전송
     *
     * @param refund
     * @param user
     * @param fromCountry
     * @param toCountry
     * @param traveler
     */
    void sendRefund2OctaAML(NewRefund refund, User user, Country fromCountry, Country toCountry, Traveler traveler) {
        Map<String, CurrencyRate> rates = currencyService.getCurrencyRateByKrwAndUsd(fromCountry.getIso4217(), refund.getCreatedDate());
        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(refund.getExchangeId());

        // AccountBase
        String prefix = "EX-RF";
        AmlAccountBase amlAccountBase = AmlAccountBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + refund.getId().toString()) // 송금 거래 번호
                .customerNo(user.getId().toString(), user.getCountryCode().name())
                .accountStsCd("01")
                .currencyCd(fromCountry.getIso4217())
                .prodCd("03")
                .prodRaCd("03")
                .prodRepCd("01")
                .accountOpenDd(refund.getCreatedDate())
                // .closeDd("99991231") // default value is 99991231
                .accountOpenPurposeCd(exchange.getExchangePurpose()) // 기타
                // .accountOpenPurposeNm("") // accountOpenPurposeCd 입력시 생성
                // .mainTranDeptCd("") // 관리부서
                // .mainTranDeptCd("") // 영업부서
                // .accountOpenDeptNm("") // 영업부서
                // .accountOpenDeptPostNo("") // 영업점 우편번호
                // .accountDiv("03") // 송금 : 03, default value is 03
                .build();

        // ProdBase
        AmlProdBase amlProdBase = AmlProdBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + refund.getId().toString()) // 송금 거래 번호
                // .prodCd("03") // 상품 코드 - default value is "03" mean "해외송금"
                // .prodNm("해외송금") // 상품명 - default value is "해외송금"
                .build();

        // AccountTranBase
        AmlAccountTranBase amlAccountTranBase = AmlAccountTranBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + refund.getId().toString()) // 송금 거래 번호
                .prodCd("03")
                .tranDd(refund.getCreatedDate()) // 거래 시간 (YYYYMMDDHHMMSS)
                .tranSeqNo(1) // 거래_일련_번호
                .customerNo(user.getId().toString(), user.getCountryCode().name())
                .tranTime(refund.getCreatedDate())
                // .tranChannelCd("19") // 거래 방법 OR 경로 - default value is "19" mean "모바일"
                // .tranChannelNm("모바일") // default value is "모바일"
                // .tranWayCd("05") // 재화의 종류 - default value is "05" mean "외(해외송금)"
                // .tranWayNm("외환(해외송금)") // default value is "외환(해외송금)"
                .tranKindCd("03") // 거래 분류 - default value is "05" mean "송금(해외)"
                .tranKindNm("환전, 환불") // default value is "해외송금"
                // .summaryCd("05") // 거래 내용 - default value is "05" mean "송금(해외)"
                // .summaryNm("송금, 이체영수") // default value is "송금, 이체영수"
                .rltFinanOrgCountryCd(fromCountry.getIso3166()) // 고객 국가
                .rltFinanOrgCd(traveler.getBankCode()) // 고객 은행 코드 - default value is "001" mean "한국은행"
                .rltFinanOrgNm(traveler.getBankName()) // 고객 은행 이름 - default value is "한국은행"
                .rltaccNo(traveler.getAccountNo()) // 수취인 계좌번호
                .rltaccOwnerNm(traveler.getAccountName()) // 수취인 계좌주명
                .toFcFrFcDiv("2") // 당타행 구분 - default value is "2" mean "타행"
                // .accountTpDiv("02") // 계좌의 종류 - default value is "02" mean "송.수취계좌"
                // .accountDivTpNm("송.수취계좌") // default value is "송.수취계좌"
                .currencyCd(fromCountry.getIso4217()) // 통화코드
                .tranAmt(refund.getFromAmt().multiply(rates.get("KRW").getRate()).setScale(0, RoundingMode.DOWN)) // 거래금액(송금) - ex) HKD -> KRW, 원화라 소수점 제거
                .wonTranAmt(refund.getToAmt()) // 원화환산금액 - ex) toAmt 화폐 고정, 소수점 제거
                .fexTranAmt(refund.getFromAmt()) // 외화거래금액 - ex) HKD
                .usdExchangeAmt(refund.getFromAmt().multiply(rates.get("USD").getRate())) // 달러환산금액 - ex) HKD -> USD
                .tranPurposeCd(exchange.getExchangePurpose()) // 거래의 목적코드
                // .tranPurposeNm("") // 거래 목적명
                .fexTranPurposeCd(exchange.getExchangePurpose()) // 외화거래의 목적코드
                // .fexTranPurposeNm("") // 외환거래 목적명
                .build();

        // AccountTranSendReceipt
        AmlAccountTranSendReceipt amlAccountTranSendReceipt = AmlAccountTranSendReceipt.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + refund.getId().toString()) // 송금 거래 번호
                .prodCd("03") // 상품 코드 - default value is "03" mean "해외송금"
                .tranDd(refund.getCreatedDate())
                .tranSeqNo(1) // 거래_일련_번호
                // .repayDivisionCd("01") // 파트너 구분(송금) - default value is "01" mean "계좌번호"
                .rltFinanOrgCountryCd(toCountry.getIso3166()) // 상대_은행_국가_코드 - 수취인
                .rltFinanOrgCd(traveler.getBankCode()) // 상대_은행_코드 - 수취인 - default value is "001" mean "한국은행"
                .rltFinanOrgNm(traveler.getBankName()) // 상대_은행_명 - 수취인 - default value is "한국은행"
                .rltaccNo(traveler.getAccountNo()) // 수취인 계좌번호
                .rltaccOwnerDiv("01") // 수취인 계좌주명 구분 - default value is "01" mean "개인"
                .rltaccOwnerDivNm("개인") // default value is "개인"
                .rltaccOwnerNm(user.getFirstName() + " " + user.getLastName()) // 수취인 계좌주명
                .rltaccOwnerPhoneNo(user.getPhoneNumber()) // 수취인(소유자) 연락처
                .rltOpenPurposeCd(exchange.getExchangePurpose()) // 개설_목적_코드 - default value is "03" mean "상속증여성 거래"
                // .rltOpenPurposeNm("") // 개설 목적명
                .build();

        OctaAMLKYCRequest request = new OctaAMLKYCRequest(amlAccountBase, amlProdBase, amlAccountTranBase, amlAccountTranSendReceipt);
        ResponseEntity<OctaAMLKYCResponse> execute = octaAMLKYCService.execute(request); // 에러 발생하여도 무시
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void registerJpRefundAccountInfo(@Valid JpRefundAccountInfoRequest accountRequest, Long travelerId) throws CashmallowException {
        refundRepositoryService.registerJpRefundAccountInfo(accountRequest, travelerId);

        List<NewRefund> inStandbyRefundList = refundRepositoryService.getNewRefundListInStandbyByTravelerId(travelerId);

        if (!inStandbyRefundList.isEmpty()) {
            // List는 exchange_id의 asc순이라 가장 오래된 환전건부터 환불 신청 진행
            NewRefund newRefund = inStandbyRefundList.get(0);
            requestRefundForStandbyRefund(newRefund.getId());
        }
    }

    public JpRefundAccountInfo updateJpRefundAccountInfo(@Valid JpRefundAccountInfoRequest jpRefundAccountInfoRequest,
                                          Long requestUserId, Long jpRefundAccountId) throws CashmallowException {
        String method = "updateJpRefundAccountInfo";
        Traveler requestedTraveler = travelerRepositoryService.getTravelerByUserId(requestUserId);
        JpRefundAccountInfo jpRefundAccountInfo = refundRepositoryService.getJpRefundAccountInfoById(jpRefundAccountId);

        if (ObjectUtils.isEmpty(jpRefundAccountInfo)) {
            logger.error("{} : jpRefundAccountInfo is Null", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        List<NewRefund> inProgressNewRefundList = refundRepositoryService.getNewRefundListInProgressByTravelerId(requestedTraveler.getId());

        if (jpRefundAccountInfo.getNeedReRegister().equals("N") && !inProgressNewRefundList.isEmpty()) {
            logger.error("{} : 신청중인 환불 건이 있으면서, refundAccountInfo가 N이면 수정할수 없습니다. needReRegister={}, inProgressRefundList={}",
                    method, jpRefundAccountInfo.getNeedReRegister(), inProgressNewRefundList.size());
            throw new CashmallowException(JP_REFUND_IN_PROGRESS_NOT_CHANGE_ACCOUNT);
        }

        if (!jpRefundAccountInfo.getTravelerId().equals(requestedTraveler.getId())) {
            logger.error("{} : 요청자와 등록된 Traveler의 ID가 다릅니다. requestTravelerId={}, accountInfoTravelerId={}",
                    method, requestedTraveler.getId(), jpRefundAccountInfo.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        jpRefundAccountInfo.reRegisterJpRefundAccountInfo(jpRefundAccountInfoRequest);
        refundRepositoryService.updateJpRefundAccountInfo(jpRefundAccountInfo);

        if (!inProgressNewRefundList.isEmpty()) {
            NewRefund newRefund = inProgressNewRefundList.get(0);
            newRefund.updateRefundAccountInfo(jpRefundAccountInfo);
            refundRepositoryService.updateNewRefund(newRefund);
            globalQueueService.reRegisterRefundAccount(inProgressNewRefundList.get(0).getId(), jpRefundAccountInfo);
        }

        return jpRefundAccountInfo;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void requestJpNewRefund(RefundJpRequest refundJpRequest, Long userId) throws CashmallowException {

        // To 일본환불이 아니면 오류
        if (!CountryCode.JP.getCode().equals(refundJpRequest.to_cd())) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
        // 일본은 최초 접수시 바로 MP상태(고객이 취소 불가능)
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        JpRefundAccountInfo jpRefundAccountInfo = refundRepositoryService.getJpRefundAccountInfoByTravelerId(traveler.getId());
        NewRefund requestNewRefund = NewRefund.of(refundJpRequest, jpRefundAccountInfo);

        if (ObjectUtils.isNotEmpty(refundJpRequest.wallet_id())) {
            requestNewRefund.setWalletId(refundJpRequest.wallet_id());
            requestNewRefund(traveler, requestNewRefund, RelatedTxnType.EXCHANGE);
        } else if (ObjectUtils.isNotEmpty(refundJpRequest.remit_id())) {
            requestNewRefund.setRemitId(refundJpRequest.remit_id());
            requestNewRefund(traveler, requestNewRefund, RelatedTxnType.REMITTANCE);
        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        globalQueueService.sendRefund(requestNewRefund, jpRefundAccountInfo);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public NewRefund standbyNewRefund(Traveler traveler, NewRefund newRefund, RelatedTxnType txnType, JpRefundAccountInfo jpRefundAccountInfo) throws CashmallowException {
        String method = "standbyNewRefund()";

        newRefund.setTravelerId(traveler.getId());
        newRefund.setRelatedTxnType(txnType);

        // to_amt 계산을 위한 mapping_inc 가져오기.
        Country toCountry = countryService.getCountry(newRefund.getToCd());
        Country fromCountry = countryService.getCountry(newRefund.getFromCd());
        BigDecimal mappingInc = toCountry.getMappingInc();
        int scale = -1 * (int) Math.floor(Math.log10(mappingInc.doubleValue()));

        BigDecimal fromMoney = newRefund.getFromAmt();

        logger.info("{}: ###### 환불 신청 정보 : fromMoney:{}", method, fromMoney);

        if (fromMoney.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("{}: {}", method, REFUND_ERROR_REQUEST_TRAVELER_NOT_ENOUGH_MONEY);
            throw new CashmallowException(REFUND_ERROR_REQUEST_TRAVELER_NOT_ENOUGH_MONEY);
        }

        String fromCountryCd = newRefund.getFromCd();
        String toCountryCd = newRefund.getToCd();
        if (fromCountryCd.equals(toCountryCd)) {
            // 환불 화폐와 from_cd 동일하면 에러
            logger.error("{}: 환불 화폐와 from_cd 동일함.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int walletCountForSameExchange = 1;
        if (RelatedTxnType.EXCHANGE.equals(txnType)) {
            TravelerWallet wallet = walletRepositoryService.getTravelerWallet(newRefund.getWalletId());
            walletCountForSameExchange = getWalletCountForSameExchange(wallet.getExchangeIds());
            wallet = walletRepositoryService.updateRelatedWalletForRefund(newRefund.getWalletId(), fromMoney);

            Exchange exchange = getExchangeByWallet(wallet);
            newRefund.setCouponUserId(exchange.getCouponUserId());
            newRefund.setCouponDiscountAmount(exchange.getCouponDiscountAmt());
            newRefund.setExchangeId(exchange.getId());
        }

        logger.info("{}: ###### 환불 대기 정보 ===============================================", method);
        logger.info("{}: from_country={}, to_country={}, from_money={}", method, fromCountryCd, toCountryCd, fromMoney);
        logger.info("{}: fee={}, from_amt={}, to_amt={}, exchange_rate={}", method, newRefund.getFee(), newRefund.getFromAmt(), newRefund.getToAmt(), newRefund.getExchangeRate());
        logger.info("{}: coupon_user_id={}", method, newRefund.getCouponUserId());

        CurrencyRate currencyRate = getCurrencyRate(toCountry, fromCountry);
        BigDecimal targetCurrencyRate = currencyRate.getRate();
        newRefund.setBaseExchangeRate(currencyRate.getBaseRate());
        // 환불 당시의 FeeRate 수수료율 기록 추가
        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(toCountry.getCode(), fromCountry.getCode());
        newRefund.setFeeRate(exchangeConfig.getFeeRateExchange());

        logger.info("{}: targetCurrencyRate={}", method, targetCurrencyRate);

        if (newRefund.getExchangeRate().compareTo(targetCurrencyRate) != 0) {
            logger.error("{}: 환불대기 오류 : 요청 환율이 다릅니다. - 요청 : {}, 기준 : {}", method, newRefund.getExchangeRate(), targetCurrencyRate); // transaction 처리용 메시지
            throw new CashmallowException(REFUND_CHANGED_EXCHANGE_RATE);
        }

        BigDecimal toAmt = newRefund.getFromAmt().divide(targetCurrencyRate, scale, RoundingMode.FLOOR);
        toAmt = toAmt.subtract(newRefund.getFee());

        // 쿠폰 금액 차감
        if (newRefund.getCouponDiscountAmount() != null) {
            toAmt = toAmt.subtract(newRefund.getCouponDiscountAmount());
        }

        validateRefundAmount(newRefund.getToAmt(), toAmt, method);

        if (ObjectUtils.isNotEmpty(jpRefundAccountInfo)) {
            newRefund.updateRefundAccountInfo(jpRefundAccountInfo);
        }

        int affectedRow = refundMapper.insertNewRefund(newRefund);
        if (affectedRow != 1) {
            logger.error("{}: Cannot insert refund", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return newRefund;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void standbyRefundOfExpiredWallet(Long walletId) throws CashmallowException {
        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(walletId);
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerWallet.getTravelerId());
        ExchangeCalcVO calcVO = calcRefundExchange(walletId);
        BigDecimal discountAmount = ObjectUtils.isEmpty(calcVO.getDiscountAmount()) ? BigDecimal.ZERO : calcVO.getDiscountAmount();

        if (travelerWallet.getRootCd().equals(CountryCode.JP.getCode())) {
            sendStandbyRefundToJp(traveler, travelerWallet, calcVO, discountAmount);
        } else {
            BigDecimal toAmt = calcVO.getTo_money().subtract(calcVO.getFee_per_amt()).subtract(discountAmount);
            NewRefund newRefund = NewRefund.of(calcVO, travelerWallet, toAmt, traveler);
            standbyNewRefund(traveler, newRefund, RelatedTxnType.EXCHANGE, null);
        }

        User user = userRepositoryService.getUserByUserId(traveler.getUserId());
        notificationService.sendEmailExpiredWallet(user);
        notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.WL, FcmEventValue.XP, null);
    }

    private void sendStandbyRefundToJp(Traveler traveler, TravelerWallet travelerWallet, ExchangeCalcVO calcVO, BigDecimal discountAmount) throws CashmallowException {
        JpRefundAccountInfo jpRefundAccountInfo = refundRepositoryService.getJpRefundAccountInfoByTravelerId(travelerWallet.getTravelerId());
        Long jpRefundAccountInfoId = ObjectUtils.isEmpty(jpRefundAccountInfo) ? null : jpRefundAccountInfo.getId();

        RefundJpRequest refundJpRequest = new RefundJpRequest(
                travelerWallet.getCountry(),
                calcVO.getFrom_money(),
                travelerWallet.getRootCd(),
                calcVO.getTo_money().subtract(calcVO.getFee_per_amt()).subtract(discountAmount),
                calcVO.getFee(),
                calcVO.getExchange_rate(),
                calcVO.getFee_per_amt(),
                calcVO.getFee_rate_amt(),
                jpRefundAccountInfoId,
                travelerWallet.getId(),
                null
        );
        // e-money 만료 접수시 stanby로 일단 접수
        NewRefund requestNewRefund = NewRefund.of(refundJpRequest);
        requestNewRefund.setWalletId(refundJpRequest.wallet_id());

        standbyNewRefund(traveler, requestNewRefund, RelatedTxnType.EXCHANGE, jpRefundAccountInfo);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void requestRefundForStandbyRefund(Long refundId) {
        String method = "requestRefundForStandbyRefund()";
        NewRefund newRefund = refundRepositoryService.getNewRefundById(refundId);

        newRefund.setRefundStatus(NewRefund.RefundStatusCode.MP);

        if (newRefund.getToCd().equals(CountryCode.JP.getCode())) {
            JpRefundAccountInfo jpRefundAccountInfo = refundRepositoryService.getJpRefundAccountInfoByTravelerId(newRefund.getTravelerId());

            if (ObjectUtils.isEmpty(jpRefundAccountInfo)) {
                logger.info("{} : Not Exist JpRefundAccountInfo. travelerId={}", method, newRefund.getTravelerId());
                return;
            }
            newRefund.updateRefundAccountInfo(jpRefundAccountInfo);
            globalQueueService.sendRefund(newRefund, jpRefundAccountInfo);
        }

        refundRepositoryService.updateNewRefund(newRefund);
    }
}
