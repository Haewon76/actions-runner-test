package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.cashout.*;
import com.cashmallow.api.domain.model.cashout.CashOut.CoStatus;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner.KindOfStorekeeper;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerMaintenance;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.model.withdrawalpartnercalc.WithdrawalPartnerCalc.CaStatus;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.traveler.dto.RequestCashOutVO;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.OLD_VERSION_APP_ERROR;

/**
 * Implementation class for CashOutService.
 */
@Service
public class CashOutServiceImpl {

    private final Logger logger = LoggerFactory.getLogger(CashOutServiceImpl.class);

    private static final String CASHOUT_ERROR_REQUEST_IDENTIFICATION_REQUIRED = "CASHOUT_ERROR_REQUEST_IDENTIFICATION_REQUIRED";
    private static final String CASHOUT_ERROR_REQUEST_ONLY_REFUND = "CASHOUT_ERROR_REQUEST_ONLY_REFUND";


    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private PartnerServiceImpl storekeeperService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    CountryService countryService;

    @Autowired
    CompanyServiceImpl companyService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private CashOutMapper cashOutMapper;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private Gson gson;

    @Autowired
    SecurityService securityService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private EnvUtil envUtil;

    public List<CashOut> getCashOutOpListByStorekeeperId(long storekeeperId) {
        return cashOutMapper.getCashOutOpListByWithdrawalPartnerId(storekeeperId);
    }

    public List<CashOut> getCashOutOpListByTravelerId(Long travelerId) {
        return cashOutMapper.getCashOutOpListByTravelerId(travelerId);
    }

    public Map<String, Object> getCashoutOpByTraveler(Traveler traveler) throws CashmallowException {

        Long travelerId = traveler.getId();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", travelerId);
        params.put("coStatus", "('OP')");
        List<CashOut> cashouts = cashOutMapper.findCashOutList(params);

        if (cashouts != null && !cashouts.isEmpty()) {

            CashOut cashout = cashouts.get(0);

            WithdrawalPartner withdrawalPartner = storekeeperService.getWithdrawalPartnerByWithdrawalPartnerId(cashout.getWithdrawalPartnerId());
            User sUser = userRepositoryService.getUserByUserId(withdrawalPartner.getUserId());

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> cashoutMap = mapper.convertValue(cashout, new TypeReference<Map<String, Object>>() {
            });

            // Cash-out Info
            cashoutMap.put("cash_out_id", cashout.getId());

            // Store-keeper Info
            cashoutMap.put("kind_of_storekeeper", withdrawalPartner.getKindOfStorekeeper());
            cashoutMap.put("shop_name", withdrawalPartner.getShopName());
            cashoutMap.put("shop_addr", withdrawalPartner.getShopAddr());
            cashoutMap.put("about", withdrawalPartner.getAbout());
            cashoutMap.put("shop_photo", withdrawalPartner.getShopPhoto());
            cashoutMap.put("shop_lat", withdrawalPartner.getShopLat());
            cashoutMap.put("shop_lng", withdrawalPartner.getShopLng());
            cashoutMap.put("shop_contact_number", withdrawalPartner.getShopContactNumber());
            cashoutMap.put("cash_out_hours", withdrawalPartner.getCashOutHours());
            cashoutMap.put("cash_out_start_at", withdrawalPartner.getCashOutStartAt());
            cashoutMap.put("cash_out_end_at", withdrawalPartner.getCashOutEndAt());
            cashoutMap.put("s_profile_photo", sUser.getProfilePhoto());
            cashoutMap.put("s_country", sUser.getCountry());

            if (cashout.getCountry().equals(CountryCode.ID.getCode())) {
                CashOutAjOtp cashOutAjOtp = cashoutRepositoryService.getCashOutAjOtpByCashOutId(cashout.getId());
                cashoutMap.put("bank_code_va_number", cashOutAjOtp.getBankCodeVaNumber());
                cashoutMap.put("withdraw_code", cashOutAjOtp.getWithdrawalCode());
                cashoutMap.put("expired_date", cashOutAjOtp.getExpiredDate());
            }

            // Traveler Info
            cashoutMap.put("contact_type", traveler.getContactType());
            cashoutMap.put("contact_id", traveler.getContactId());

            return cashoutMap;
        } else {
            return null;
        }
    }

    /**
     * Get cash-out amount statistics by country
     *
     * @param country
     * @param beginDate
     * @param endDate
     * @return : totalCnt, totalAmt, totalFee, totalTotal,
     * comCnt, comAmt, comFee, comTotal,
     * reqCnt, reqAmt, reqFee, reqTotal,
     * canCnt, canAmt, canFee, canTotal
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getCashOutAmountByCountry(String country, Date beginDate, Date endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("country", country);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        params.put("beginDate", df.format(beginDate));
        params.put("endDate", df.format(endDate));

        return cashOutMapper.getCashOutAmountByCountry(params);
    }

    /**
     * Get payment amount statistics by country
     *
     * @param country
     * @param beginDate
     * @param endDate
     * @return : totalCnt, totalAmt, totalFee, totalTotal,
     * comCnt, comAmt, comFee, comTotal,
     * reqCnt, reqAmt, reqFee, reqTotal,
     * canCnt, canAmt, canFee, canTotal
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getPaymentAmountByCountry(String country, Date beginDate, Date endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("country", country);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        params.put("beginDate", df.format(beginDate));
        params.put("endDate", df.format(endDate));

        return cashOutMapper.getPaymentAmountByCountry(params);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void requestTravelerCashOutV2(Long userId, WithdrawalPartner withdrawalPartner, BigDecimal travelerCashoutAmt,
                                         Long travelerWalletId, String withdrawalCountryCode, String cashoutReservedDate, Integer requestTime, String contactType, String contactId) throws CashmallowException {

        String method = "requestTravelerCashOutV2()";

        if (userId == Const.NO_USER_ID || withdrawalPartner == null) {
            logger.error("{}: userId={}, storekeeperId={}", method, userId, withdrawalPartner.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        User user = userRepositoryService.getUserByUserId(userId);

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler == null) {
            logger.error("{}: 여행자 ID가 올바르지 않습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 1. userId의 travelerId 구하기
        Long travelerId = traveler.getId();

        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(travelerWalletId);

        if (travelerWallet == null) {
            logger.error("{}: 인출 가능한 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        checkCashOutRequestV2(traveler, cashoutReservedDate, travelerWallet, withdrawalPartner, travelerCashoutAmt, requestTime, withdrawalCountryCode);

        walletRepositoryService.updateWalletForWithdrawalV2(travelerCashoutAmt, travelerWallet);

        KindOfStorekeeper kindOfStorekeeer = KindOfStorekeeper.valueOf(withdrawalPartner.getKindOfStorekeeper());
        if (KindOfStorekeeper.A.equals(kindOfStorekeeer)
                || KindOfStorekeeper.C.equals(kindOfStorekeeer)) {

            BigDecimal cashoutFee = calcCashoutFee(withdrawalCountryCode, travelerCashoutAmt, withdrawalPartner.getId());

            String qrCodeValue = CustomStringUtil.generateQrCode();
            String qrCodeSource = "Cashmallow QR code";

            CashOut cashOut = new CashOut(travelerId, withdrawalPartner.getId(), withdrawalCountryCode,
                    travelerCashoutAmt, BigDecimal.ZERO,
                    travelerCashoutAmt, cashoutFee, qrCodeValue, qrCodeSource,
                    travelerWallet.getExchangeIds(), travelerWallet.getId());

            cashOut.setCashoutReservedDate(cashoutReservedDate);
            cashOut.setPrivacySharingAgreement(user.getAgreePrivacy().equalsIgnoreCase("Y"));

            cashOut.setCoStatus(CoStatus.OP.name());
            cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

            long cashOutId = registerCashOut(cashOut);

            long sUserId = withdrawalPartner.getUserId();

            User sUser = userRepositoryService.getUserByUserId(sUserId);
            notificationService.sendFcmNotificationMsgAsync(sUser, FcmEventCode.CO, FcmEventValue.OP, cashOutId);

        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // update contact_type, contact_id in traveler table 
        traveler.setContactType(contactType);
        traveler.setContactId(contactId);
        travelerRepositoryService.updateTraveler(traveler);

        // send slack message 
        String msg = "유저ID:" + userId + ", 신청국:" + withdrawalCountryCode + ", 금액:" + travelerCashoutAmt;
        msg += "\n가맹점ID:" + withdrawalPartner.getUserId();
        msg += ", 가맹점이름:" + withdrawalPartner.getShopName();
        if (withdrawalPartner.getAbout() != null) {
            msg += "\n가맹점정보:" + withdrawalPartner.getAbout();
        }

        alarmService.aAlert("인출신청", msg, user);

    }

    /**
     * 여행자 인출 신청
     *
     * @param userId
     * @param withdrawalPartner
     * @param rCashOutVO
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void requestTravelerCashOut(Long userId, WithdrawalPartner withdrawalPartner, RequestCashOutVO rCashOutVO) throws CashmallowException {
        String method = "requestTravelerCashOut()";

        if (rCashOutVO == null) {
            logger.error("{}: Invalid parameters", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (userId == Const.NO_USER_ID || withdrawalPartner == null) {
            logger.error("{}: userId={}, storekeeperId={}", method, userId, withdrawalPartner.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler == null) {
            logger.error("{}: 여행자 ID가 올바르지 않습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 1. userId의 travelerId 구하기
        Long travelerId = traveler.getId();

        //        BigDecimal cashoutFee = calcCashoutFee(rCashOutVO.getCountry(), rCashOutVO.getTraveler_cash_out_amt());
        //        rCashOutVO.setStorekeeper_cash_out_amt(rCashOutVO.getTraveler_cash_out_amt());
        //        rCashOutVO.setStorekeeper_cash_out_fee(cashoutFee);
        //        rCashOutVO.setStorekeeper_total_cost(rCashOutVO.getTraveler_cash_out_amt().add(cashoutFee));

        TravelerWallet travelerWallet = null;
        // 여행자 지갑에서 인출 신청한 국가의 보유 금액 조회
        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletList(userId);
        for (TravelerWallet w : wallets) {
            if (w.getCountry().equals(rCashOutVO.getCountry())) {
                travelerWallet = w;
                break;
            }
        }

        if (rCashOutVO.getWallet_id() != null) {
            travelerWallet = walletRepositoryService.getTravelerWallet(rCashOutVO.getWallet_id());
        }

        if (travelerWallet == null) {
            logger.error("{}: 인출 가능한 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        checkCashOutRequest(traveler, rCashOutVO, travelerWallet, withdrawalPartner);

        walletRepositoryService.updateWalletForWithdrawal(rCashOutVO, travelerWallet);

        KindOfStorekeeper kindOfStorekeeer = KindOfStorekeeper.valueOf(withdrawalPartner.getKindOfStorekeeper());
        if (KindOfStorekeeper.A.equals(kindOfStorekeeer)
                || KindOfStorekeeper.C.equals(kindOfStorekeeer)) {

            BigDecimal cashoutFee = calcCashoutFee(rCashOutVO.getCountry(), rCashOutVO.getTraveler_cash_out_amt(), withdrawalPartner.getId());

            String qrCodeValue = CustomStringUtil.generateQrCode();
            String qrCodeSource = "Cashmallow QR code";

            CashOut cashOut = new CashOut(travelerId, rCashOutVO.getWithdrawal_partner_id(), rCashOutVO.getCountry(),
                    rCashOutVO.getTraveler_cash_out_amt(), rCashOutVO.getTraveler_cash_out_fee(),
                    rCashOutVO.getTraveler_cash_out_amt(), cashoutFee, qrCodeValue, qrCodeSource,
                    travelerWallet.getExchangeIds(), travelerWallet.getId());

            cashOut.setCashoutReservedDate(rCashOutVO.getCashout_reserved_date());
            cashOut.setFlightArrivalDate(rCashOutVO.getFlight_arrival_date());
            cashOut.setFlightNo(rCashOutVO.getFlight_no());
            cashOut.setPrivacySharingAgreement(rCashOutVO.isPrivacy_sharing_agreement());

            cashOut.setCoStatus(CoStatus.OP.name());
            cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

            long cashOutId = registerCashOut(cashOut);

            long sUserId = withdrawalPartner.getUserId();

            User sUser = userRepositoryService.getUserByUserId(sUserId);
            notificationService.sendFcmNotificationMsgAsync(sUser, FcmEventCode.CO, FcmEventValue.OP, cashOutId);

        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // update contact_type, contact_id in traveler table 
        traveler.setContactType(rCashOutVO.getContact_type());
        traveler.setContactId(rCashOutVO.getContact_id());
        travelerRepositoryService.updateTraveler(traveler);

        // send slack message 
        String msg = "유저ID:" + userId + ", 신청국:" + rCashOutVO.getCountry() + ", 금액:"
                + rCashOutVO.getTraveler_cash_out_amt();
        msg += "\n가맹점ID:" + withdrawalPartner.getUserId();
        msg += ", 가맹점이름:" + withdrawalPartner.getShopName();
        if (withdrawalPartner.getAbout() != null) {
            msg += "\n가맹점정보:" + withdrawalPartner.getAbout();
        }

        alarmService.aAlert("인출신청", msg, userRepositoryService.getUserByUserId(traveler));
    }

    // 기능: 12.8.5. 여행자 결제 신청
    @Deprecated
    @Transactional(rollbackFor = CashmallowException.class)
    public String requestTravelerPayment(Long userId, RequestCashOutVO pvo) throws CashmallowException {

        String method = "requestTravelerPayment()";

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        Long storekeeperId = 0L;

        if (traveler == null) {
            logger.error("{}: Can not find traveler by userId. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        long travelerId = traveler.getId();

        BigDecimal paymentAmt = pvo.getTraveler_cash_out_amt();
        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletList(userId);
        TravelerWallet travelerWallet = null;
        for (TravelerWallet w : wallets) {
            if (w.getCountry().equals(pvo.getCountry())) {
                travelerWallet = w;
                break;
            }
        }

        if (travelerWallet == null) {
            logger.error("{}: Can not find traveler wallet for payment. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        travelerWallet = walletRepositoryService.updateWalletForWithdrawalV2(paymentAmt, travelerWallet);

        // 3. 결제 요청 등록
        CashOut cashOut = new CashOut(travelerId, storekeeperId, pvo.getCountry(),
                paymentAmt, BigDecimal.ZERO, paymentAmt, BigDecimal.ZERO, null, null,
                travelerWallet.getExchangeIds(), travelerWallet.getId());

        cashOut.setCoStatus(CashOut.CoStatus.PO.toString());
        cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

        long orgId = registerCashOut(cashOut);

        if (orgId <= 0) {
            logger.error("{}: Can not insert cash-out for payment. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        WithdrawalPartner storekeeper = storekeeperService.getWithdrawalPartnerByWithdrawalPartnerId(storekeeperId);
        if (storekeeper != null) {
            // 결제 요청 알림
            User sUser = userRepositoryService.getUserByUserId(storekeeper.getUserId());
            notificationService.addFcmNotificationMsg(sUser, FcmEventCode.PY, FcmEventValue.OP, orgId);
        } else {
            logger.error("{}: Can not find storekeeper by storekeeperId for payment. storekeeperId={}", method, storekeeperId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        alarmService.aAlert("결제신청", "유저ID:" + userId + " / 신청국 : " + pvo.getCountry(), userRepositoryService.getUserByUserId(traveler));

        return cashOut.getQrCodeValue();

    }

    public void checkCashOutRequest(Traveler traveler, RequestCashOutVO pvo, TravelerWallet travelerWallet, WithdrawalPartner storekeeper) throws CashmallowException {
        String method = "checkCashOutRequest()";

        if (!"Y".equals(traveler.getCertificationOk())) {
            logger.error("{}: 본인 인증 안된 사용자 입니다.", method);
            throw new CashmallowException(CASHOUT_ERROR_REQUEST_IDENTIFICATION_REQUIRED);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!StringUtils.isEmpty(pvo.getFlight_arrival_date())) {
            try {
                sdf.parse(pvo.getFlight_arrival_date());
            } catch (ParseException e) {
                logger.error("Failed parsing Flight_arrival_date : " + e.getMessage(), e);
                throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
            }
        } else if (!StringUtils.isEmpty(pvo.getCashout_reserved_date())) {
            try {
                sdf.parse(pvo.getCashout_reserved_date());
            } catch (ParseException e) {
                logger.error("Failed parsing Cashout_reserved_date : " + e.getMessage(), e);
                throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
            }
        }

        if (travelerWallet.geteMoney().compareTo(pvo.getTraveler_total_cost()) < 0) {
            logger.error("{}: 여행자의 환전 금액이 부족합니다. travelerWallet.geteMoney()={}, pvo.getTraveler_total_cost()={}",
                    method, travelerWallet.geteMoney(), pvo.getTraveler_total_cost());
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_INSUFFICIENT_BALANCE");
        }

        if (travelerWallet.getExchangeIds() != null) {

            JSONObject jsonExchangeIds = new JSONObject(travelerWallet.getExchangeIds());
            JSONArray exchangeArray = jsonExchangeIds.getJSONArray(Const.EXCHANGE_IDS);
            String exchangeId = exchangeArray.get(0).toString();

            List<NewRefund> refunds = refundRepositoryService.getNewRefundNotCancelByExchangeId(exchangeId, traveler.getId());

            if (!refunds.isEmpty()) {
                logger.error("{}: 해당 환전 id로 환불을 받은 기록이 있어 인출이 불가능합니다. walletId={}, exchangeId={}",
                        method, travelerWallet.getId(), exchangeId);
                throw new CashmallowException(CASHOUT_ERROR_REQUEST_ONLY_REFUND);
            }
        }

        // Set cash_out reserved date
        Date reservedDate = null;

        try {
            if (!StringUtils.isEmpty(pvo.getCashout_reserved_date())) {
                reservedDate = sdf.parse(pvo.getCashout_reserved_date());
            } else if (!StringUtils.isEmpty(pvo.getFlight_arrival_date())) {
                reservedDate = sdf.parse(pvo.getFlight_arrival_date());
            } else {
                // 이전 버전 임. 신규 버전 업데이트 후에 인출 신청하도록 가이드.
                logger.error("{}: 예약일이 없는 오래된 버전으로 인출 예약 시도", method);
                throw new CashmallowException(OLD_VERSION_APP_ERROR);
            }
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        /* 인출 서비스 가능 여부 조회 */
        if (!"Y".equals(storekeeper.getCashOutService())) {
            logger.error("{}: 상점 서비스 상태가 아닙니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_STOPPED_SERVICE");
        }

        Calendar cal = Calendar.getInstance();

        cal.setTime(reservedDate);
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        /* 휴무일 체크 */
        String holydayDay = storekeeper.getHolidayDay();
        holydayDay = StringUtils.isEmpty(holydayDay) ? "" : holydayDay;

        String holydayDate = storekeeper.getHolidayDate();
        holydayDate = StringUtils.isEmpty(holydayDate) ? "" : holydayDate;

        if (holydayDay.indexOf(String.valueOf(dayNum)) >= 0 || holydayDate.indexOf(sdf.format(reservedDate).substring(0, 10)) >= 0) {
            logger.error("{}: 상점 휴무일이 입니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_NO_BUSINESS_DAYS");
        }

        JSONObject cashOutHours = new JSONObject(storekeeper.getCashOutHours()); // 요일별 인출 가능 시간 전체 
        JSONObject cashOutHour = null; // 특정 요일 인출 가능 시간 

        /* 영업 시간 체크 */
        // default 영업 시간 : key = 0
        cashOutHour = cashOutHours.getJSONObject(String.valueOf(0));

        // 예외인 요일은 따로 등록되어 있는 영업 시간을 가져온다.
        if (cashOutHours.has(String.valueOf(dayNum))) {
            cashOutHour = cashOutHours.getJSONObject(String.valueOf(dayNum));
        }

        if (cashOutHour != null
                && (cashOutHour.getInt("cash_out_start_at") > pvo.getRequest_time()
                || cashOutHour.getInt("cash_out_end_at") < pvo.getRequest_time())) {
            logger.error("{}: 상점 영업시간이 아닙니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_NO_BUSINESS_HOURS");
        } else if (cashOutHour == null) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        /* 일일 인출 한도 조회 */
        BigDecimal possibleAmt = storekeeper.getFxPossibleAmt();

        logger.info("{}: 가맹점 일일 인출 가능액: {}", method, possibleAmt);
        logger.info("{}: 인출 요청액: {}", method, pvo.getTraveler_cash_out_amt());

        // Get store's reserved total cash-out amount
        HashMap<String, Object> params = new HashMap<>();
        params.put("withdrawalPartnerId", pvo.getWithdrawal_partner_id());
        params.put("coStatus", "('OP', 'CF', 'PO', 'PF')"); // TC 를 제외한 나머지 모두 해당
        if (!StringUtils.isEmpty(pvo.getFlight_arrival_date())) {
            String date = pvo.getFlight_arrival_date().substring(0, 10);
            params.put("flightArrivalDate", date);
        } else if (!StringUtils.isEmpty(pvo.getCashout_reserved_date())) {
            String date = pvo.getCashout_reserved_date().substring(0, 10);
            params.put("cashoutReservedDate", date);
        }
        List<CashOut> cashOuts = cashOutMapper.findCashOutList(params);
        BigDecimal reservedTotalAmt = BigDecimal.valueOf(0);
        for (CashOut co : cashOuts) {
            reservedTotalAmt = reservedTotalAmt.add(co.getTravelerCashOutAmt());
        }
        reservedTotalAmt = reservedTotalAmt.add(pvo.getTraveler_total_cost());

        if (possibleAmt.compareTo(reservedTotalAmt) < 0) {
            logger.error("{}: 인출 신청 금액이 가맹점의 일일 인출 가능 한도 금액보다 많습니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_INSUFFICIENT_MONEY");
        }
    }

    public void checkCashOutRequestV2(Traveler traveler, String cashoutReservedDate, TravelerWallet travelerWallet,
                                      WithdrawalPartner withdrawalPartner, BigDecimal cashoutAmt, Integer requestTime, String withdrawalCountryCode) throws CashmallowException {
        String method = "checkCashOutRequestV2()";

        if (!"Y".equals(traveler.getCertificationOk())) {
            logger.warn("{}: 본인 인증 안된 사용자 입니다.", method);
            throw new CashmallowException(CASHOUT_ERROR_REQUEST_IDENTIFICATION_REQUIRED);
        }

        if (!travelerWallet.getTravelerId().equals(traveler.getId())) {
            logger.error("{}: 지갑의 소유자가 다릅니다. travelerId={}, wallet의travelerId={}, walletId={}", method, traveler.getId(), travelerWallet.getTravelerId(), travelerWallet.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // cashoutReservedDate 따로 입력 받지 않음. 인출 국가의 현지 시각을 서버에서 처리.
        ZonedDateTime cashoutLocalTime = ZonedDateTime.now().withZoneSameInstant(CountryCode.of(withdrawalCountryCode).getZoneId());
        Date reservedDate = Date.from(cashoutLocalTime.toInstant());
        String cashoutLocalDateStr = cashoutLocalTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (travelerWallet.geteMoney().compareTo(cashoutAmt) < 0) {
            logger.error("{}: 여행자의 환전 금액이 부족합니다. travelerWallet.geteMoney()={}, pvo.getTraveler_total_cost()={}",
                    method, travelerWallet.geteMoney(), cashoutAmt);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_INSUFFICIENT_BALANCE");
        }

        if (travelerWallet.getExchangeIds() != null) {

            JSONObject jsonExchangeIds = new JSONObject(travelerWallet.getExchangeIds());
            JSONArray exchangeArray = jsonExchangeIds.getJSONArray(Const.EXCHANGE_IDS);
            String exchangeId = exchangeArray.get(0).toString();

            List<NewRefund> refunds = refundRepositoryService.getNewRefundNotCancelByExchangeId(exchangeId, traveler.getId());

            if (!refunds.isEmpty()) {
                logger.error("{}: 해당 환전 id로 환불을 받은 기록이 있어 인출이 불가능합니다. walletId={}, exchangeId={}", method,
                        travelerWallet.getId(), exchangeId);
                throw new CashmallowException(CASHOUT_ERROR_REQUEST_ONLY_REFUND);
            }
        }

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(travelerWallet.getRootCd(), withdrawalCountryCode);

        if (exchangeConfig.getToMaxWithdrawal().compareTo(cashoutAmt) < 0) {
            logger.error("{}: 인출하려는 금액이 인출 제한금액보다 큽니다. maxToCashout={}, cashoutAmt={}",
                    method, exchangeConfig.getToMaxWithdrawal(), cashoutAmt);

            throw new CashmallowException("CASHOUT_ERROR_REQUEST_AMOUNT_MAX_EXCEED", exchangeConfig.getToMaxWithdrawal().toString());
        }

        if (exchangeConfig.getToMinWithdrawal().compareTo(cashoutAmt) > 0) {
            logger.error("{}: 인출하려는 금액이 인출 최소 금액보다 작습니다. minToCashout={}, cashoutAmt={}",
                    method, exchangeConfig.getToMinWithdrawal(), cashoutAmt);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_AMOUNT_MIN_EXCEED", exchangeConfig.getToMinWithdrawal().toString());
        }

        // 점검 시간 검사
        List<WithdrawalPartnerMaintenance> partnerMaintenances = partnerService.getPartnerMaintenances(withdrawalPartner);
        ZonedDateTime now = ZonedDateTime.now();
        for (var wpm : partnerMaintenances) {
            if (wpm.getStartAt().isBefore(now) && now.isBefore(wpm.getEndAt())) {
                logger.error("{}: 상점 서비스 상태가 아닙니다.", method);
                throw new CashmallowException("CASHOUT_ERROR_REQUEST_MAINTENANCE");
            }
        }

        /* 인출 서비스 가능 여부 조회 */
        if (!"Y".equals(withdrawalPartner.getCashOutService())) {
            logger.error("{}: 상점 서비스 상태가 아닙니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_STOPPED_SERVICE");
        }

        Calendar cal = Calendar.getInstance();

        cal.setTime(reservedDate);
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        /* 휴무일 체크 */
        String holydayDay = withdrawalPartner.getHolidayDay();
        holydayDay = StringUtils.isEmpty(holydayDay) ? "" : holydayDay;

        String holydayDate = withdrawalPartner.getHolidayDate();
        holydayDate = StringUtils.isEmpty(holydayDate) ? "" : holydayDate;

        if (holydayDay.contains(String.valueOf(dayNum)) || holydayDate.contains(cashoutLocalDateStr)) {
            logger.error("{}: 상점 휴무일이 입니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_NO_BUSINESS_DAYS");
        }

        JSONObject cashOutHours = new JSONObject(withdrawalPartner.getCashOutHours()); // 요일별 인출 가능 시간 전체 
        JSONObject cashOutHour = null; // 특정 요일 인출 가능 시간 

        /* 영업 시간 체크 */
        // default 영업 시간 : key = 0
        cashOutHour = cashOutHours.getJSONObject(String.valueOf(0));

        // 예외인 요일은 따로 등록되어 있는 영업 시간을 가져온다.
        if (cashOutHours.has(String.valueOf(dayNum))) {
            cashOutHour = cashOutHours.getJSONObject(String.valueOf(dayNum));
        }

        if (cashOutHour != null
                && (cashOutHour.getInt("cash_out_start_at") > requestTime
                || cashOutHour.getInt("cash_out_end_at") < requestTime)) {
            logger.warn("{}: 상점 영업시간이 아닙니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_NO_BUSINESS_HOURS");
        } else if (cashOutHour == null) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        /* 일일 인출 한도 조회 */
        BigDecimal possibleAmt = withdrawalPartner.getFxPossibleAmt();

        logger.info("{}: 가맹점 일일 인출 가능액: {}", method, possibleAmt);
        logger.info("{}: 인출 요청액: {}", method, cashoutAmt);

        // Get store's reserved total cash-out amount
        HashMap<String, Object> params = new HashMap<>();
        params.put("withdrawalPartnerId", withdrawalPartner.getId());
        params.put("coStatus", "('OP', 'CF', 'PO', 'PF')"); // TC 를 제외한 나머지 모두 해당
        params.put("cashoutReservedDate", cashoutLocalDateStr);
        List<CashOut> cashOuts = cashOutMapper.findCashOutList(params);
        BigDecimal reservedTotalAmt = BigDecimal.valueOf(0);
        for (CashOut co : cashOuts) {
            reservedTotalAmt = reservedTotalAmt.add(co.getTravelerCashOutAmt());
        }
        reservedTotalAmt = reservedTotalAmt.add(cashoutAmt);

        if (possibleAmt.compareTo(reservedTotalAmt) < 0) {
            logger.error("{}: 인출 신청 금액이 가맹점의 일일 인출 가능 한도 금액보다 많습니다.", method);
            throw new CashmallowException("CASHOUT_ERROR_REQUEST_INSUFFICIENT_MONEY");
        }
    }

    //    public void calcCashOutFee(RequestCashOutVO cashOutVo) {
    //        //가맹점 수수료 계산을 위해 국가 정보 가져옴
    //        Country country = countryService.getCountry(cashOutVo.getCountry());
    //
    //        //가맹점 수수료도 각 나라의 mappingInc 단위로 조정함.
    //        BigDecimal storekeeperCashOutAmt = cashOutVo.getTraveler_cash_out_amt();
    //
    //        int scale = -1 * (int) Math.floor(Math.log10(country.getMappingInc().doubleValue()));
    //        BigDecimal storekeeperCashOutFee = storekeeperCashOutAmt.multiply(country.getStorekeeperFeeRate()).divide(BigDecimal.valueOf(100), scale, RoundingMode.FLOOR);
    //        BigDecimal storekeeperTotalCost = storekeeperCashOutAmt.add(storekeeperCashOutFee);
    //
    //        cashOutVo.setStorekeeper_cash_out_amt(storekeeperCashOutAmt);
    //        cashOutVo.setStorekeeper_cash_out_fee(storekeeperCashOutFee);
    //        cashOutVo.setStorekeeper_total_cost(storekeeperTotalCost);
    //    }

    /**
     * 인출 금액으로 가맹점 수수료 및 지급액 계산
     *
     * @param countryCode
     * @param cashoutAmt
     * @return BigDecimal cashoutFee
     */
    public BigDecimal calcCashoutFee(String countryCode, BigDecimal cashoutAmt, Long storekeeperId) {

        // 가맹점 수수료 계산을 위해 국가 정보 가져옴
        Country country = countryService.getCountry(countryCode);

        WithdrawalPartner storekeeper = storekeeperService.getWithdrawalPartnerByWithdrawalPartnerId(storekeeperId);

        // 가맹점 수수료도 각 나라의 mappingInc 단위로 조정함. 단위 이하는 절사 (FLOOR)
        int scale = -1 * (int) Math.floor(Math.log10(country.getMappingInc().doubleValue()));

        BigDecimal feePer = storekeeper.getFeePer() == null ? BigDecimal.ZERO : storekeeper.getFeePer();
        BigDecimal feeRate = storekeeper.getFeeRate() == null ? BigDecimal.ZERO : storekeeper.getFeeRate();

        BigDecimal calcFee;

        // rate 계산
        calcFee = cashoutAmt.multiply(feeRate).divide(BigDecimal.valueOf(100), scale, RoundingMode.FLOOR);
        // 고정값 계산
        calcFee = calcFee.add(feePer.divide(BigDecimal.valueOf(1), scale, RoundingMode.FLOOR));

        return calcFee;
    }

    /**
     * register new cash_out
     *
     * @param cashOut
     * @return
     * @throws CashmallowException
     */
    public long registerCashOut(CashOut cashOut) throws CashmallowException {

        String method = "registerCashOut()";

        if (cashOut.getTravelerCashOutAmt().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("{}: Invalid Params. cashOut.getTravelerCashOutAmt()={}", method, cashOut.getTravelerCashOutAmt());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (cashOut.getWithdrawalPartnerCashOutAmt().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("{}: Invalid Params. cashOut.getStorekeeperCashOutAmt()={}", method, cashOut.getWithdrawalPartnerCashOutAmt());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (StringUtils.isEmpty(cashOut.getQrCodeValue())) {
            logger.error("{}: Invalid Params. cashOut.getQrCodeValue()={}", method, cashOut.getQrCodeValue());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CoStatus coStatus = CoStatus.valueOf(cashOut.getCoStatus());
        if (!CoStatus.OP.equals(coStatus) && !CoStatus.PO.equals(coStatus)) {
            logger.error("{}: Invalid Params. cashOut.getCoStatus()={}", method, cashOut.getCoStatus());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (StringUtils.isEmpty(cashOut.getCaStatus())) {
            cashOut.setCaStatus(CaStatus.BF.name());
        }

        // 5. 인출 요청 등록
        int affectedRow = cashOutMapper.insertCashOut(cashOut);
        long cashOutId = cashOut.getId();

        // If AUTO_INCREMENT failed, returns 0. 
        if (affectedRow != 1 || cashOutId <= 0) {
            logger.error("{}: Failed to insert into cash_out table, affectedRow={}, cashOutId={}", method, affectedRow, cashOutId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return cashOutId;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public CashOut regenerateCashoutQrCode(Traveler traveler, Long cashoutId) throws CashmallowException {
        String method = "regenerateCashoutQrCode()";

        CashOut cashout = cashoutRepositoryService.getCashOut(cashoutId);

        if (!traveler.getId().equals(cashout.getTravelerId())) {
            logger.error("{}: The travelerIds do not match. cashoutId={}, traveler.getId()={}, cashout.getTravelerId()={}",
                    method, cashoutId, traveler.getId(), cashout.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cashout.setQrCodeValue(CustomStringUtil.generateQrCode());
        cashout.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int effectedRow = cashoutRepositoryService.updateCashOut(cashout);

        if (effectedRow != 1) {
            logger.error("{}: failed to update. cashoutId={}, effectedRow={}", method, cashoutId, effectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return cashout;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public CashOut updateCashoutQrCode(Traveler traveler, Long cashOutId,
                                       String qrCodeValue, String qrCodeSource) throws CashmallowException {

        String method = "updateCashoutQrCode()";

        CashOut cashout = cashoutRepositoryService.getCashOut(cashOutId);

        if (!traveler.getId().equals(cashout.getTravelerId())) {
            logger.error("{}: The travelerIds do not match. cashoutId={}, traveler.getId()={}, cashout.getTravelerId()={}",
                    method, cashOutId, traveler.getId(), cashout.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (StringUtils.isEmpty(qrCodeValue)) {
            qrCodeValue = CustomStringUtil.generateQrCode();
            qrCodeSource = "Cashmallow QR code";
        }

        cashout.setQrCodeValue(qrCodeValue);
        cashout.setQrCodeSource(qrCodeSource);
        cashout.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int effectedRow = cashoutRepositoryService.updateCashOut(cashout);

        if (effectedRow != 1) {
            logger.error("{}: failed to update. cashoutId={}, effectedRow={}", method, cashOutId, effectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return cashout;
    }


    /**
     * 기능: 12.8. 여행자 인출 완료
     * Complete cash-out
     *
     * @param cashOut
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void completeCashOutConfirm(CashOut cashOut)
            throws CashmallowException {

        String method = "completeCashOutConfirm";

        if (cashOut == null) {
            logger.error("{}: Cannot find CashOut data.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        WithdrawalPartner storekeeper = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        if (storekeeper == null) {
            logger.error("{}: Cannot find storekeeper data.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CoStatus coStatus = CoStatus.valueOf(cashOut.getCoStatus());
        if (!CoStatus.OP.equals(coStatus)) {
            logger.error("{}: CoStatus is not OP. coStatus={}", method, coStatus);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(cashOut.getTravelerId());
        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(cashOut.getWalletId());

        if (traveler == null) {
            logger.error("{}: Cannot find Traveler data with travelerId.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        } else if (travelerWallet == null) {
            logger.error("{}: Cannot find TravelerWallet data with walletId.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (travelerWallet.getExchangeIds() != null) {
            cashOut.setExchangeIds(travelerWallet.getExchangeIds());
        }

        cashOut.setCoStatus(CoStatus.CF.name());
        cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        cashOut.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int walletRow = walletRepositoryService.deleteWalletForCashOut(travelerWallet.getId(), cashOut.getTravelerTotalCost());

        if (walletRow != 1) {
            logger.error("{}: 지갑데이터로 인해 인출 완료 처리를 할 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int affectedRow = cashoutRepositoryService.updateCashOut(cashOut);

        if (affectedRow != 1) {
            logger.error("{}: 인출 완료 처리를 할 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // create transaction_record
        Country country = countryService.getCountry(cashOut.getCountry());
        companyService.createTransactionRecordByCashOut(traveler.getUserId(), cashOut.getId(),
                country.getIso4217(), cashOut.getTravelerCashOutAmt());

        // Slack post alarm
        long tUserId = traveler.getUserId(); // 여행자의 user_id
        String tCashOutAmt = cashOut.getTravelerCashOutAmt().toString(); // 인출 금액
        String tCashOutCountry = cashOut.getCountry(); // 신청국가

        String msg = "인출 거래번호: " + cashOut.getId() +
                "\n유저ID:" + tUserId + ", 가맹점ID:" + storekeeper.getUserId() + ", 신청국:" + tCashOutCountry + ", 금액:" + tCashOutAmt +
                "\n여행자 여권 이름:" + traveler.getEnFirstName() + " " + traveler.getEnLastName() +
                "\n가맹점 이름:" + storekeeper.getShopName();

        alarmService.aAlert("인출완료", msg, userRepositoryService.getUserByUserId(traveler));

        // Traveler 인출 완료 알림
        User tUser = userRepositoryService.getUserByUserId(tUserId);
        Long orgId = cashOut.getId(); // cash_out.id
        notificationService.sendFcmNotificationMsgAsync(tUser, FcmEventCode.CO, FcmEventValue.CF, orgId);

        if (tUser.getCountryCode().equals(CountryCode.JP)) {
            globalQueueService.sendWithdrawalCompleted(cashOut.getId());
        }
    }

    // 여행자에 의한 인출 신청 취소
    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelCashoutByTraveler(Long userId, Long cashOutId) throws CashmallowException {
        String method = "cancelCashOut()";

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            logger.error("{}: traveler 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CashOut cashOut = cashoutRepositoryService.getCashOut(cashOutId);

        if (!cashOut.getTravelerId().equals(traveler.getId())) {
            logger.error("{}: cashout의 traveler 정보와 로그인한 userId의 traveler 정보가 일치하지 않습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        processCancelCashout(cashOut.getId(), CoStatus.TC);

        WithdrawalPartner storekeeper = storekeeperService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        User sUser = userRepositoryService.getUserByUserId(storekeeper.getUserId());
        //      if(!storekeeper.getKindOfStorekeeper().equals(KindOfStorekeeper.M002.name())) {
        //    	notificationService.sendFcmNotificationMsgAsync(sUser, FcmEventCode.CO, FcmEventValue.TC, cashOut.getId());
        //      }
        notificationService.sendFcmNotificationMsgAsync(sUser, FcmEventCode.CO, FcmEventValue.TC, cashOut.getId());

    }

    /**
     * 인출 완료 처리 후 ATM에서 현금을 가져가지 않아 인출이 완료되지 않은 경우. 해당 인출을 취소하고 여행자에게 FCM 발송
     *
     * @param cashOut
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void rollbackCashoutWithNotification(CashOut cashOut) throws CashmallowException {

        String method = "rollbackCashoutWithNotification()";

        if (!CoStatus.CF.name().equals(cashOut.getCoStatus())) {
            logger.error("{}: The status is not complete. currentCoStatus={}", method, cashOut.getCoStatus());
            throw new CashmallowException("The status is not complete.", CoatmServiceImpl.ResultCode.INVALID_CASHOUT);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(cashOut.getTravelerId());
        if (traveler == null) {
            logger.error("{}: Can't find traveler by travelerId. cashout.getTravelerId()={}", method, cashOut.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CashOut updatedCashout = cashoutRepositoryService.getCashOut(cashOut.getId());
        updatedCashout.setCoStatus(CoStatus.SC.name());
        updatedCashout.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        updatedCashout.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRow = cashoutRepositoryService.updateCashOut(updatedCashout);

        if (affectedRow == 1) {

            long withdrawalPartnerId = updatedCashout.getWithdrawalPartnerId();

            if (withdrawalPartnerId == Const.NO_USER_ID) {
                logger.error("{}: 사용자의 인출 요청 정보에 잘못된 Withdrawal Partner ID.가 있습니다.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            JSONObject jo = new JSONObject(cashOut.getExchangeIds());

            JSONArray exchangeIds = jo.getJSONArray("exchange_ids");
            Long exchangeId = 0L;
            for (Object o : exchangeIds) {
                exchangeId = Long.parseLong(o.toString());
            }

            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(exchangeId);

            walletRepositoryService.addTravelerWallet(traveler.getId(), exchange.getFromCd(), exchange.getToCd(), updatedCashout.getTravelerCashOutAmt(), withdrawalPartnerId, exchange.getId());

            companyService.deleteTransactionRecordByCashOut(updatedCashout.getId());

            // 여행자에게 인출 신청 취소 알림 (FCM Notification)
            // FCM Notification 으로 앱에서 처리하기 위해 보낸다. 
            User tUser = userRepositoryService.getUserByUserId(traveler.getUserId());
            notificationService.sendFcmNotificationMsgAsync(tUser, FcmEventCode.CO, FcmEventValue.SC, cashOut.getId());

            WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);
            String shopName = withdrawalPartner.getShopName();

            // slack 에 인출 취소 알림
            String message = "인출 거래번호: " + cashOut.getId() +
                    "\n" + shopName + " 유저 ID:" + traveler.getUserId() + " 금액:" + updatedCashout.getTravelerCashOutAmt();
            alarmService.aAlert("인출롤백", message, tUser);

            if (CountryCode.JP.equals(tUser.getCountryCode())) {
                globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.CASH_OUT, cashOut.getId(), updatedCashout.getCoStatus(), -1L);
            }
        } else {
            logger.error("{}: Failed update cash_out table. affectedRow={}", method, affectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    // 인출 신청 취소
    @Transactional(rollbackFor = CashmallowException.class)
    public void processCancelCashout(Long cashoutId, CoStatus coStatus) throws CashmallowException {
        String method = "processCashOut()";

        CashOut cashout = cashoutRepositoryService.getCashOut(cashoutId);

        logger.info("cashoutId={}, 현재 status={}, 변경 status={}, partnerId={}", cashoutId, cashout.getCoStatus(), coStatus, cashout.getWithdrawalPartnerId());

        if (cashout == null) {
            logger.warn("{}: 잘못된 인출 요청 정보입니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CoStatus status = CoStatus.valueOf(cashout.getCoStatus());
        if (CoStatus.TC.equals(status) || CoStatus.SC.equals(status) || CoStatus.CC.equals(status)) {
            logger.warn("{}: 이미 취소된 인출 건 입니다. cashout id:{}, getCoStatus:{}", method, cashoutId, cashout.getCoStatus());
            return;
        } else if (!CoStatus.OP.equals(status)) {
            logger.warn("{}: 진행 중인 인출이 아니므로 취소할 수 없습니다. cashout id:{}, getCoStatus:{}", method, cashoutId, cashout.getCoStatus());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(cashout.getTravelerId());
        User user = userRepositoryService.getUserByUserId(traveler.getUserId());

        long storekeeperId = cashout.getWithdrawalPartnerId();

        if (storekeeperId == Const.NO_USER_ID) {
            logger.warn("{}: 사용자의 인출 요청 정보에 잘못된 가맹정 ID.가 있습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (!CoStatus.TC.equals(coStatus) && !CoStatus.SC.equals(coStatus) && !CoStatus.CC.equals(coStatus)) {
            logger.warn("{}: 인출 취소 상태 파라미터 오류 입니다. coStatus={}", method, coStatus);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 인출 요청의 상태 변경함.
        cashout.setCoStatus(coStatus.name());
        cashout.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        cashout.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        int affectedRow = cashoutRepositoryService.updateCashOut(cashout);

        if (affectedRow != 1) {
            logger.error("{}: 인출 요청 상태를 변경할 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        walletRepositoryService.updateWalletForWithdrawalCancel(cashout.getWalletId(), cashout.getTravelerTotalCost(),
                getWalletCanRefundPossible(cashout.getWalletId()));

        // slack 에 인출 취소 알림 
        String message = "인출 거래번호: " + cashout.getId() +
                "\n유저ID:" + traveler.getUserId();
        alarmService.aAlert("인출취소", message, user);

        if (CountryCode.JP.equals(user.getCountryCode())) {
            // 인출 취소에서는 쿠폰 롤백을 할 수 없으므로 -1L(사용안함)로 지정함
            globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.CASH_OUT, cashoutId, cashout.getCoStatus(), -1L);
        }
    }

    private String getWalletCanRefundPossible(Long walletId) {

        TravelerWallet targetWallet = walletRepositoryService.getTravelerWallet(walletId);

        if (targetWallet.getCanRefund().equals("Y")) {
            return "Y";
        }

        JSONObject jsonExchangeIds = new JSONObject(targetWallet.getExchangeIds());
        JSONArray exchangeArray = jsonExchangeIds.getJSONArray(Const.EXCHANGE_IDS);
        String exchangeId = exchangeArray.get(0).toString();

        List<TravelerWallet> walletLists = walletRepositoryService.getTravelerWalletByExchangeIds(exchangeId);

        if (walletLists.isEmpty()) {
            logger.error("walletCanRefundEnabled() :  {}", targetWallet.getExchangeIds());
        }

        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(Long.valueOf(exchangeId));

        BigDecimal walletEMoneyTotal = BigDecimal.ZERO;
        for (TravelerWallet wallet : walletLists) {
            walletEMoneyTotal = walletEMoneyTotal.add(wallet.geteMoney());
        }

        if (walletEMoneyTotal.compareTo(exchange.getToAmt()) == 0) {
            return "Y";
        } else {
            return "N";
        }
    }


    // 가맹점 정산
    // 기능: 50.2. 정산중인 가맹점이 있는 지 조회한다(국가별 , 기간 무관)

    /**
     * 인출 정산이 진행 중인 데이터가 있는지 여부와 시작일/종료일을 리턴한다.
     *
     * @param country
     * @param beginDate
     * @param endDate
     * @return
     */
    public Map<String, Object> isCalcualtingCashOut(String country, Timestamp beginDate, Timestamp endDate) {
        String method = "isCalcualtingCashOut(): ";
        logger.info("{}: country={}, beginDate={}, endDate={}", method, country, beginDate, endDate);

        HashMap<String, Object> params = new HashMap<>();

        // 결과: kind='1' : 기간 내 인출 신청 또는 결제 신청이 있는 경우
        //      kind='2' : 기간 내 가맹점 정산 중인 경우
        params.put("country", country);
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);

        return cashOutMapper.isCalcualtingCashOut(params);
    }

    public Map<String, List<PendingBalance>> getPendingBalances(LocalDateTime start, LocalDateTime end) {
        String fromDate = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String toDate = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        final List<PendingBalance> pendingBalances = cashOutMapper.getPendingBalance(fromDate, toDate);
        final List<String> iso4217s = cashOutMapper.getISO4217s();

        final Map<String, List<PendingBalance>> generatedMap = pendingBalances.stream().collect(Collectors.groupingBy(PendingBalance::getFromCountryKorName));
        generatedMap.keySet().forEach(k -> {
            iso4217s.forEach(currency -> {
                if (generatedMap.get(k).stream().noneMatch(p -> p.getToCurrency().equals(currency))) {
                    generatedMap.get(k).add(new PendingBalance(currency));
                }
            });

            generatedMap.get(k).sort(Comparator.comparing(PendingBalance::getToCurrency));
            generatedMap.get(k).add(0, new PendingBalance(generatedMap.get(k)));
        });
        return generatedMap;
    }

    public List<PendingBalanceDetailVo> getPendingBalanceDetails(LocalDateTime start, LocalDateTime end) {
        String fromDate = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String toDate = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<PendingBalanceDetailVo> pendingBalanceDetailVos = new ArrayList<>();
        pendingBalanceDetailVos.add(new PendingBalanceDetailVo());
        final List<PendingBalanceDetailVo> pendingBalanceDetails = cashOutMapper.getPendingBalanceDetails(fromDate, toDate);
        IntStream.range(0, pendingBalanceDetails.size()).forEach(i -> pendingBalanceDetailVos.add(new PendingBalanceDetailVo(i + 1, pendingBalanceDetails.get(i))));
        return pendingBalanceDetailVos;
    }


}
