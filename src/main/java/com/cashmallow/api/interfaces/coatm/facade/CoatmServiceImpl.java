package com.cashmallow.api.interfaces.coatm.facade;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.CashOutServiceImpl;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashOut.CoStatus;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint.CashpointType;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.coatm.dto.InputVO;
import com.cashmallow.api.interfaces.traveler.dto.RequestCashOutVO;
import com.cashmallow.common.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.cashmallow.api.domain.model.country.enums.CountryCode.KR;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
public class CoatmServiceImpl {
    Logger logger = LoggerFactory.getLogger(CoatmServiceImpl.class);

    // Cashmallow sub code from COATM
    private static final String SUB_CODE = "23";

    @Value(value = "${coatm.api.seedEcbKey}")
    private String seedEcbKey;

    @Autowired
    private EnvUtil envUtil;

    public class ResultCode {
        public static final String SUCCESS = "0000";
        public static final String FAILURE_COMPLETE = "0010";
        public static final String FALLURE_ROLLBACK = "0020";
        public static final String INVALID_PARAMS = "0081";
        public static final String DB_ERROR = "0095";

        /**
         * 출금인증번호 또는 개인식별번호 불일치 시 사용
         */
        public static final String INVALID_OTP = "0066";

        /**
         * 출금취소(rollback) 처리 시만 사용. 인출완료 상태가 아니거나 인출데이터 찾을 수 없음.
         */
        public static final String INVALID_CASHOUT = "0031";

        public static final String ETC_ERROR = "0099";
    }

    private static Random random = new SecureRandom();


    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private PartnerServiceImpl storekeeperService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AlarmService alarmService;


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartnerCashpoint> getAtmList(Long withdrawalPartnerId, Double lat, Double lng) throws CashmallowException {

        List<WithdrawalPartnerCashpoint> WithdrawalPartnerCashpoints = storekeeperService.getWithdrawalPartnerCashpointListByWithdrawalPartnerId(withdrawalPartnerId, lat, lng);

        String staticUrl = envUtil.getStaticUrl();
        for (int i = 0; i < WithdrawalPartnerCashpoints.size(); i++) {
            WithdrawalPartnerCashpoint atm = WithdrawalPartnerCashpoints.get(i);
            atm.setDefaultIconImagePath(staticUrl + "/images/atm/cashmallow_atm_icon.png");
            if (atm.getPartnerCashpointName().startsWith("GS25")) {
                atm.setIconImagePath(staticUrl + "/images/atm/gs25_icon.png");
            } else if (atm.getPartnerCashpointName().startsWith("CU")) {
                atm.setIconImagePath(staticUrl + "/images/atm/cu_icon.png");
            } else if (atm.getPartnerCashpointName().startsWith("7-Eleven")) {
                atm.setIconImagePath(staticUrl + "/images/atm/7eleven_icon.png");
            } else if (atm.getPartnerCashpointName().startsWith("MINISTOP")) {
                atm.setIconImagePath(staticUrl + "/images/atm/ministop_icon.png");
            } else {
                atm.setIconImagePath(staticUrl + "/images/atm/coatm_icon.png");
            }
            WithdrawalPartnerCashpoints.set(i, atm);
        }

        return WithdrawalPartnerCashpoints;
    }

    // 기능: 12.0. 여행자 인출 신청
    @Transactional(rollbackFor = CashmallowException.class)
    public Map<String, Object> requestCashOut(Long userId, WithdrawalPartner storekeeper, RequestCashOutVO rCashOutVO) throws CashmallowException {
        String method = "requestCashOut()";

        Map<String, Object> result = new HashMap<>();

        if (rCashOutVO == null) {
            logger.error("{}: Invalid parameters", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        if (user == null || storekeeper == null) {
            logger.error("{}: userId={}, storekeeperId={}", method, userId, rCashOutVO.getWithdrawal_partner_id());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        // 1. userId의 travelerId 구하기
        if (traveler == null) {
            logger.error("{}: userId로 여행자 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        BigDecimal cashoutFee = cashOutService.calcCashoutFee(rCashOutVO.getCountry(), rCashOutVO.getTraveler_cash_out_amt(), storekeeper.getId());
        rCashOutVO.setWithdrawal_partner_cash_out_amt(rCashOutVO.getTraveler_cash_out_amt());
        rCashOutVO.setWithdrawal_partner_cash_out_fee(cashoutFee);
        rCashOutVO.setWithdrawal_partner_total_cost(rCashOutVO.getTraveler_cash_out_amt().add(cashoutFee));

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

        cashOutService.checkCashOutRequest(traveler, rCashOutVO, travelerWallet, storekeeper);

        walletRepositoryService.updateWalletForWithdrawal(rCashOutVO, travelerWallet);

        if (storekeeper.getShopName().contains("COATM")) {

            String otp = generateOTP();

            // make new QR code for avoiding DB exception by the unique index in qr_code_value column.
            String qrCodeValue = makeQRCodeValue(rCashOutVO.getCountry(), CashpointType.ATM, user.getBirthDate(), otp);
            qrCodeValue = securityService.encryptSHA2(qrCodeValue);

            CashOut cashOut = new CashOut(traveler.getId(), rCashOutVO.getWithdrawal_partner_id(), rCashOutVO.getCountry(),
                    rCashOutVO.getTraveler_cash_out_amt(), rCashOutVO.getTraveler_cash_out_fee(),
                    rCashOutVO.getTraveler_cash_out_amt(), cashoutFee, qrCodeValue, "Cashmallow OTP for COATM",
                    travelerWallet.getExchangeIds(), travelerWallet.getId());

            cashOut.setCashoutReservedDate(rCashOutVO.getCashout_reserved_date());
            cashOut.setFlightArrivalDate(rCashOutVO.getFlight_arrival_date());
            cashOut.setFlightNo(rCashOutVO.getFlight_no());
            cashOut.setPrivacySharingAgreement(rCashOutVO.isPrivacy_sharing_agreement());

            cashOut.setCoStatus(CoStatus.OP.name());
            cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

            long cashOutId = cashOutService.registerCashOut(cashOut);

            result.put("confirm_no", otp);
            result.put("cash_out_id", cashOutId);

        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // send slack message 
        String msg = "유저ID:" + userId + ", 신청국:" + rCashOutVO.getCountry() + ", 금액:"
                + rCashOutVO.getTraveler_cash_out_amt();
        msg += "\n가맹점ID:" + storekeeper.getUserId();
        msg += ", 가맹점이름:" + storekeeper.getShopName();
        if (storekeeper.getAbout() != null) {
            msg += "\n가맹점정보:" + storekeeper.getAbout();
        }

        alarmService.aAlert("인출신청", msg, user);

        return result;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Map<String, Object> requestCashOutV2(Long userId, WithdrawalPartner withdrawalPartner, BigDecimal travelerCashoutAmt,
                                                Long travelerWalletId, String countryCode, String cashoutReservedDate, Integer requestTime) throws CashmallowException {
        String method = "requestCashOut()";

        Map<String, Object> result = new HashMap<>();

        User user = userRepositoryService.getUserByUserId(userId);
        if (user == null || withdrawalPartner == null) {
            logger.error("{}: userId={}, withdrawalPartnerId={}", method, userId, withdrawalPartner.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        // 1. userId의 travelerId 구하기
        if (traveler == null) {
            logger.error("{}: userId로 여행자 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        BigDecimal cashoutFee = cashOutService.calcCashoutFee(countryCode, travelerCashoutAmt, withdrawalPartner.getId());

        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(travelerWalletId);

        if (travelerWallet == null) {
            logger.error("{}: 인출 가능한 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        try {
            cashOutService.checkCashOutRequestV2(traveler, cashoutReservedDate, travelerWallet, withdrawalPartner, travelerCashoutAmt, requestTime, countryCode);
        } catch (CashmallowException e) {
            if ("CASHOUT_ERROR_REQUEST_MAINTENANCE".equals(e.getMessage())) {
                throw new CashmallowException("EXCHANGE_STOP_NOT_YET_SERVICE", e);
            } else {
                throw e;
            }
        }

        walletRepositoryService.updateWalletForWithdrawalV2(travelerCashoutAmt, travelerWallet);

        if (!withdrawalPartner.getShopName().contains("COATM")) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String otp = generateOTP();

        // make new QR code for avoiding DB exception by the unique index in
        // qr_code_value column.
        String qrCodeValue = makeQRCodeValue(countryCode, CashpointType.ATM, user.getBirthDate(), otp);
        qrCodeValue = securityService.encryptSHA2(qrCodeValue);

        CashOut cashOut = new CashOut(traveler.getId(), withdrawalPartner.getId(), countryCode,
                travelerCashoutAmt, BigDecimal.ZERO,
                travelerCashoutAmt, cashoutFee, qrCodeValue, "Cashmallow OTP for COATM",
                travelerWallet.getExchangeIds(), travelerWallet.getId());

        cashOut.setCashoutReservedDate(cashoutReservedDate);
        cashOut.setPrivacySharingAgreement(user.getAgreePrivacy().equalsIgnoreCase("Y"));

        cashOut.setCoStatus(CoStatus.OP.name());
        cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

        long cashOutId = cashOutService.registerCashOut(cashOut);

        result.put("confirm_no", otp);
        result.put("cash_out_id", cashOutId);

        // send slack message 
        String msg = "유저ID:" + userId + ", 신청국:" + countryCode + ", 금액:" + travelerCashoutAmt;
        msg += "\n가맹점ID:" + withdrawalPartner.getUserId();
        msg += ", 가맹점이름:" + withdrawalPartner.getShopName();
        if (withdrawalPartner.getAbout() != null) {
            msg += "\n가맹점정보:" + withdrawalPartner.getAbout();
        }

        alarmService.aAlert("인출신청", msg, user);

        return result;
    }

    /**
     * Complete cash-out for COATM
     *
     * @param birthDate
     * @param otp
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public CashOut completeCashOut(String memberPin, String paymentKey, String partnerTxnId) throws CashmallowException {

        String method = "completeCashOut()";

        String country = KR.getCode();
        String birthDate = securityService.decryptSeedEcb(seedEcbKey, memberPin);
        String otp = securityService.decryptSeedEcb(seedEcbKey, paymentKey);
        logger.info("{}: OTP={}, birthDate={}", method, otp, birthDate);
        String qrCodeValue = makeQRCodeValue(country, CashpointType.ATM, birthDate, otp);
        qrCodeValue = securityService.encryptSHA2(qrCodeValue);

        CashOut cashOut = cashoutRepositoryService.getCashOutByQrCodeValue(country, CoStatus.OP.name(), qrCodeValue);

        if (cashOut == null) {
            logger.warn("{}: date of birth or one time password does not match. qrCodeValue={}", method, qrCodeValue);
            throw new CashmallowException("Your date of birth or one time password does not match. Please check your withdrawal information.",
                    ResultCode.INVALID_OTP);
        }

        WithdrawalPartner storekeeper = storekeeperService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        if (storekeeper == null) {
            logger.error("{}: storekeeper is null. cashOut.getStorekeeperId()={}", method, cashOut.getWithdrawalPartnerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR, ResultCode.ETC_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(cashOut.getTravelerId());
        if (traveler == null) {
            logger.error("{}: traveler is null. cashOut.getTravelerId()={}", method, cashOut.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR, ResultCode.ETC_ERROR);
        }

        User user = userRepositoryService.getUserByUserId(traveler.getUserId());
        if (user == null) {
            logger.error("{}: user is null. traveler.getUserId()={}", method, traveler.getUserId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR, ResultCode.ETC_ERROR);
        }

        cashOut.setPartnerTxnId(partnerTxnId);
        try {
            cashOutService.completeCashOutConfirm(cashOut);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, ResultCode.FAILURE_COMPLETE);
        }

        return cashOut;
    }

    /**
     * Rollback due to abnormal case after cash-out is completed.
     * No error message is shown to the customer.
     *
     * @param payToken
     * @param memberPin
     * @param paymentKey
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void rollbackCashOut(InputVO inputVO) throws CashmallowException {
        String payToken = inputVO.getPayToken();
        String memberPin = inputVO.getRqMemberPin();
        String paymentKey = inputVO.getRqPaymentKey();
        String transactionNo = inputVO.getRqWithdrawSeqNo();

        String method = "rollbackCashOut()";

        long cashOutId = makeCashoutId(payToken);

        CashOut cashOut = cashoutRepositoryService.getCashOut(cashOutId);

        if (cashOut == null) {
            logger.error("{}: Can't find cash-out data. cashOutId={}", method, cashOutId);
            throw new CashmallowException("Can't find withdrawal data.", ResultCode.INVALID_CASHOUT);
        }

        alarmService.aAlert("COATM롤백", "cashOutId: " + cashOutId, userRepositoryService.getUserByWithdrawalCashoutId(cashOutId));

        String birthDate = securityService.decryptSeedEcb(seedEcbKey, memberPin);
        String otp = securityService.decryptSeedEcb(seedEcbKey, paymentKey);

        String qrCodeValue = makeQRCodeValue(cashOut.getCountry(), CashpointType.ATM, birthDate, otp);
        qrCodeValue = securityService.encryptSHA2(qrCodeValue);

        if (!cashOut.getQrCodeValue().equals(qrCodeValue)) {
            logger.error("{}: Doesn't match cash-out information. cashOut.getQrCodeValue()={}, qrCodeValue={}",
                    method, cashOut.getQrCodeValue(), qrCodeValue);
            throw new CashmallowException("Doesn't match withdrawal information.", ResultCode.INVALID_CASHOUT);
        }

        CoStatus currentCoStatus = CoStatus.valueOf(cashOut.getCoStatus());
        if (!CoStatus.CF.equals(currentCoStatus)) {
            logger.error("{}: The status is not complete. currentCoStatus={}", method, currentCoStatus);
            throw new CashmallowException("The status is not complete.", ResultCode.INVALID_CASHOUT);
        }

        cashOut.setPartnerTxnId(transactionNo);
        cashOutService.rollbackCashoutWithNotification(cashOut);
    }

    /**
     * make payToken
     * It must be shorter than 100 bytes.
     *
     * @param cashOutId
     * @return
     */
    public String makePayToken(Long cashOutId) {
        // Just make Hexadecimal
        return Long.toHexString(cashOutId).toUpperCase();
    }

    /**
     * Make cashoutId from payToken
     *
     * @param payToken
     * @return
     */
    public Long makeCashoutId(String payToken) {
        // Convert hexadecimal string to long value
        return new BigInteger(payToken, 16).longValue();
    }

    /**
     * Make OTP. The traveler will enter it into ATM for withdrawal.
     * SUB_CODE(2 digits) + PIN(6 digits)
     *
     * @return OTP(8 digits)
     */
    private String generateOTP() {
        StringBuilder sb = new StringBuilder(SUB_CODE);
        int pinLength = 6;
        for (int i = 0; i < pinLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Make QR code value by combining multiple values to avoid duplication of cash_out.qr_code_value column.
     *
     * @param country
     * @param kindOfStorekeeper
     * @param birthDate
     * @param otp
     * @return
     */
    private String makeQRCodeValue(String country, CashpointType cashpointType, String birthDate, String otp) {
        return String.format("%s-%s-%s-%s", country, cashpointType, birthDate, otp);
    }
}
