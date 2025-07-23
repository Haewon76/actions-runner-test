package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.domain.model.company.BankAccount;
import com.cashmallow.api.domain.model.company.CompanyMapper;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.Currency;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.exchange.Mapping;
import com.cashmallow.api.domain.model.exchange.MappingMapper;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.Remittance.RemittanceStatusCode;
import com.cashmallow.api.domain.model.remittance.RemittanceDepositReceipt;
import com.cashmallow.api.domain.model.remittance.RemittanceMapper;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceRelationship;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.admin.dto.AdminRemittanceAskVO;
import com.cashmallow.api.interfaces.admin.dto.MappingPinRegVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.coupon.dto.CouponCalcResponse;
import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.cashmallow.api.interfaces.edd.UserEddService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceBankServiceImpl;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.cashmallow.api.interfaces.traveler.web.address.AddressEnglishServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.common.EnvUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.INVALID_REGION_VALUE;

@Service
public class RemittanceServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(RemittanceServiceImpl.class);

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExchangeCalculateServiceImpl exchangeCalculateService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private RemittanceMapper remittanceMapper;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DbsProperties dbsProperties;

    @Value("${paygate.bankAccountId}")
    private int paygateAccountId;

    @Value("${openbank.bankAccountId}")
    private long openbankAccountId;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private CouponMobileServiceV2 couponMobileService;

    @Autowired
    private MappingMapper mappingMapper;

    @Autowired
    private UserEddService userEddService;

    @Autowired
    private LimitCheckService limitCheckService;

    @Autowired
    private AmountLimitServiceImpl amountLimitService;

    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private MallowlinkRemittanceBankServiceImpl mallowlinkRemittanceValidateService;

    @Autowired
    private AddressEnglishServiceImpl addressEnglishServiceImpl;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO searchAdminRemittanceForReport(AdminRemittanceAskVO pvo) {
        int size = pvo.getSize() != null ? pvo.getSize() : Const.DEF_PAGE_SIZE;
        int page = (pvo.getStart_row() + size) / size - 1;

        pvo.setPage(page);
        pvo.setSize(size);

        SearchResultVO searchResult = new SearchResultVO(page, size, pvo.getSort());

        List<Object> vos = remittanceMapper.searchAdminRemittanceForReport(pvo);

        List<Object> remittanceResult = new ArrayList<>();

        for (Object obj : vos) {
            Map<String, String> remittance = (Map) obj;

            remittance.put("email", securityService.decryptAES256(remittance.get("email")));
            remittance.put("tr_account_no", securityService.decryptAES256(remittance.get("tr_account_no")));
            remittance.put("tr_account_name", securityService.decryptAES256(remittance.get("tr_account_name")));
            remittance.put("receiver_bank_account_no", securityService.decryptAES256(remittance.get("receiver_bank_account_no")));
            remittance.put("receiver_phone_no", securityService.decryptAES256(remittance.get("receiver_phone_no")));
            remittance.put("receiver_first_name", securityService.decryptAES256(remittance.get("receiver_first_name")));
            remittance.put("receiver_last_name", securityService.decryptAES256(remittance.get("receiver_last_name")));
            remittance.put("receiver_address", securityService.decryptAES256(remittance.get("receiver_address")));
            remittance.put("receiver_address_secondary", securityService.decryptAES256(remittance.get("receiver_address_secondary")));

            remittanceResult.add(remittance);
        }

        int totalCount = remittanceMapper.countAdminRemittanceForReport(pvo);

        searchResult.setResult(remittanceResult, totalCount, page);

        return searchResult;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO searchAdminRemittanceForMapping(AdminRemittanceAskVO pvo) {


        int size = pvo.getSize() != null ? pvo.getSize() : Const.DEF_PAGE_SIZE;
        int page = (pvo.getStart_row() + size) / size - 1;

        pvo.setPage(page);
        pvo.setSize(size);

        SearchResultVO searchResult = new SearchResultVO(page, size, pvo.getSort());

        List<Object> vos = remittanceMapper.searchAdminRemittanceForMapping(pvo);

        List<Object> remittanceResult = new ArrayList<>();

        for (Object obj : vos) {
            Map<String, String> remittance = (Map) obj;
            remittance.put("transaction_type", ServiceType.REMITTANCE.name());
            remittance.put("tr_account_no", securityService.decryptAES256(remittance.get("tr_account_no")));
            remittance.put("email", securityService.decryptAES256(remittance.get("email")));
            remittance.put("receiver_phone_no", securityService.decryptAES256(remittance.get("receiver_phone_no")));
            remittance.put("receiver_first_name", securityService.decryptAES256(remittance.get("receiver_first_name")));
            remittance.put("receiver_last_name", securityService.decryptAES256(remittance.get("receiver_last_name")));
            remittance.put("receiver_address", securityService.decryptAES256(remittance.get("receiver_address")));
            remittance.put("receiver_address_secondary", securityService.decryptAES256(remittance.get("receiver_address_secondary")));
            remittance.put("receiver_bank_account_no", securityService.decryptAES256(remittance.get("receiver_bank_account_no")));

            remittanceResult.add(remittance);
        }

        int totalCount = remittanceResult.size();

        searchResult.setResult(remittanceResult, totalCount, page);

        return searchResult;
    }


    public List<Map<String, String>> getRemittancePurpose(Locale locale) {
        logger.info("{}", "getRemittancePurpose");

        List<Map<String, String>> remitPurpose = new ArrayList<Map<String, String>>();

        Map<String, String> purposeMap = new HashMap<String, String>();

        purposeMap.put("content", messageSource.getMessage("REMITTANCE_PURPOSE_GIFT", null, "REMITTANCE_PURPOSE_GIFT", locale));
        purposeMap.put("code", RemittancePurpose.GIFT.name());
        remitPurpose.add(purposeMap);
        purposeMap = new HashMap<String, String>();

        purposeMap.put("content", messageSource.getMessage("REMITTANCE_PURPOSE_STUDY", null, "REMITTANCE_PURPOSE_STUDY", locale));
        purposeMap.put("code", RemittancePurpose.STUDY.name());
        remitPurpose.add(purposeMap);
        purposeMap = new HashMap<String, String>();

        purposeMap.put("content", messageSource.getMessage("REMITTANCE_PURPOSE_LIVINGEXPENSES", null, "REMITTANCE_PURPOSE_LIVINGEXPENSES", locale));
        purposeMap.put("code", RemittancePurpose.LIVINGEXPENSES.name());
        remitPurpose.add(purposeMap);
        purposeMap = new HashMap<String, String>();

        purposeMap.put("content", messageSource.getMessage("REMITTANCE_PURPOSE_DONATE", null, "REMITTANCE_PURPOSE_DONATE", locale));
        purposeMap.put("code", RemittancePurpose.DONATE.name());
        remitPurpose.add(purposeMap);
        purposeMap = new HashMap<String, String>();

        purposeMap.put("content", messageSource.getMessage("REMITTANCE_PURPOSE_TRAVELEXPENSES", null, "REMITTANCE_PURPOSE_TRAVELEXPENSES", locale));
        purposeMap.put("code", RemittancePurpose.TRAVELEXPENSES.name());
        remitPurpose.add(purposeMap);
        purposeMap = new HashMap<String, String>();

        return remitPurpose;
    }

    public List<Map<String, String>> getRemittanceFundSource(Locale locale) {
        logger.info("{}", "getRemittanceFundSource");

        List<Map<String, String>> remitSource = new ArrayList<Map<String, String>>();

        Map<String, String> sourceMap = new HashMap<String, String>();

        sourceMap.put("content", messageSource.getMessage("REMITTANCE_FUND_SOURCE_WORKINCOME", null, "REMITTANCE_FUND_SOURCE_WORKINCOME", locale));
        sourceMap.put("code", RemittanceFundSource.WORKINCOME.name());
        remitSource.add(sourceMap);
        sourceMap = new HashMap<String, String>();

        sourceMap.put("content", messageSource.getMessage("REMITTANCE_FUND_SOURCE_BUSINESSINCOME", null, "REMITTANCE_FUND_SOURCE_BUSINESSINCOME", locale));
        sourceMap.put("code", RemittanceFundSource.BUSINESSINCOME.name());
        remitSource.add(sourceMap);
        sourceMap = new HashMap<String, String>();

        sourceMap.put("content", messageSource.getMessage("REMITTANCE_FUND_SOURCE_GIFTINCOME", null, "REMITTANCE_FUND_SOURCE_GIFTINCOME", locale));
        sourceMap.put("code", RemittanceFundSource.GIFTINCOME.name());
        remitSource.add(sourceMap);
        sourceMap = new HashMap<String, String>();

        sourceMap.put("content", messageSource.getMessage("REMITTANCE_FUND_SOURCE_OWNPROPERTYDISPOAL", null, "REMITTANCE_FUND_SOURCE_OWNPROPERTYDISPOAL", locale));
        sourceMap.put("code", RemittanceFundSource.OWNPROPERTYDISPOAL.name());
        remitSource.add(sourceMap);
        sourceMap = new HashMap<String, String>();

        sourceMap.put("content", messageSource.getMessage("REMITTANCE_FUND_SOURCE_INTERESTINCOME", null, "REMITTANCE_FUND_SOURCE_INTERESTINCOME", locale));
        sourceMap.put("code", RemittanceFundSource.INTERESTINCOME.name());
        remitSource.add(sourceMap);

        return remitSource;
    }

    public List<Map<String, String>> getRemittanceRelationship(Locale locale) {
        return Arrays.stream(RemittanceRelationship.values())
                .map(relationship -> Map.of(
                        "content", messageSource.getMessage(relationship.getMessageCode(), null, relationship.getDefaultMessage(), locale),
                        "code", relationship.name()
                )).toList();
    }

    /**
     * V3 송금
     * 쿠폰 개선 적용 2025.03.06
     *
     * @param remittance
     * @param addressStateProvince
     * @param couponUserId
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance requestRemittanceV3(Remittance remittance, String addressStateProvince, Long couponUserId) throws CashmallowException {
        final String method = "requestRemittanceV3(): ";

        // JP 송금에 대한 Validation 처리
        mallowlinkRemittanceValidateService.validationRemittanceBankAndBranch(remittance);

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());

        User user = userRepositoryService.getUserByUserId(traveler.getUserId());
        Long userId = traveler.getUserId();
        Long travelerId = traveler.getId();

        amountLimitService.checkRemittanceEnabled(userId, remittance.getFromCd(), remittance.getFromAmt(),
                remittance.getToCd(), remittance.getToAmt(), remittance.getExchangeRate());

        Country fromCountry = countryService.getCountry(remittance.getFromCd());
        Country toCountry = countryService.getCountry(remittance.getToCd());

        CouponCalcResponse calcResponse = new CouponCalcResponse();
        // 쿠폰 정보 계산
        if (isUseCoupon(couponUserId)) {
            calcResponse = couponMobileService.calcCouponV2(fromCountry, toCountry, remittance.getFromAmt(), remittance.getFeePerAmt(), couponUserId, userId, ServiceType.REMITTANCE, Const.TRUE);
        } else {
            calcResponse.setPaymentAmount(remittance.getFromAmt());
        }

        BigDecimal fee = remittance.getFee();
        BigDecimal fromAmt = calcResponse.getPaymentAmount();
        BigDecimal feeRateAmt = remittance.getFeeRateAmt();

        logger.info("{} userId={}", method, userId);

        if (!remittance.checkValidation()) {
            logger.error("{} invalid exchange values (userId={})", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 송금이 진행중인 상태가 있으면, 신청할수 없다.
        Remittance opRemittance = remittanceRepositoryService.getRemittanceInprogress(travelerId);

        if (opRemittance != null) {
            logger.error("{} An remittance for the travelerId={} is on progress.", method, travelerId);
            throw new CashmallowException(MsgCode.PREVIOUS_REMITTANCE_IN_PROGRESS);
        }

        // 계산 검증 (쿠폰 계산 포함)
        ExchangeCalcVO exchangeCalcVO = calcRemittanceV4(fromCountry.getCode(), toCountry.getCode(), BigDecimal.valueOf(0), remittance.getToAmt(), remittance.getRemittanceType(),
                traveler, couponUserId, isUseCoupon(couponUserId) ? Const.Y : Const.N);

        if (StringUtils.isNotEmpty(exchangeCalcVO.getStatus())) {
            logger.warn("{}, 환전 계산 실패. status={}", method, exchangeCalcVO.getStatus());
            throw new CashmallowException(exchangeCalcVO.getMessage());
        }

        // To금액은 같아야한다.
        if (remittance.getToAmt().compareTo(exchangeCalcVO.getTo_money()) != 0) {
            logger.error("{} to_money관련 송금 요청 정보가 올바르지 않습니다. 요청금액={}, 재계산 금액={}", method, remittance.getToAmt(), exchangeCalcVO.getTo_money());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 환율변동 혹은 from 금액이 다를경우 새로 계산한 금액으로 변경한다.
        // 수수료가 다를경우에도 새로 계산
        if (remittance.getExchangeRate().compareTo(exchangeCalcVO.getExchange_rate()) != 0
                || remittance.getFromAmt().compareTo(exchangeCalcVO.getFrom_money()) != 0
                || fee.compareTo(exchangeCalcVO.getFee()) != 0) {
            remittance.updateExchangeRate(exchangeCalcVO);
            couponUserId = exchangeCalcVO.getCouponUserId();
            fromAmt = exchangeCalcVO.getPaymentAmount();
            fee = exchangeCalcVO.getFee();
            feeRateAmt = exchangeCalcVO.getFee_rate_amt();
        }

        // 2018-05-11 신규 버전에서는 환전신청할때 핀이 생성되고 오류 등으로 환전신청이 실패하면 생성되었던 핀은 롤백 된다.
        MappingPinRegVO mprvo = new MappingPinRegVO();
        mprvo.setCountry(remittance.getFromCd());
        mprvo.setPin_value(fromAmt);
        mprvo.setBank_account_id(Integer.valueOf(remittance.getBankAccountId().toString()));

        mprvo.setBank_account_id(Math.toIntExact(dbsProperties.accountId()));

        if (CountryCode.HK.getCode().equals(remittance.getFromCd())) {
            remittance.setBankAccountId(dbsProperties.accountId());
        }

        Mapping mapping = exchangeCalculateService.generatePinValue(userId, mprvo);

        if (ObjectUtils.isEmpty(mapping)) {
            // Pin 생성 Fail 이면 그대로 리턴. 진행 안함.
            logger.error("{} cannot find mapping information in mapping table (userId={})", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittance.setTravelerId(travelerId);

        remittance.setFromAmt(fromAmt);
        remittance.setFee(fee);
        remittance.setFeeRateAmt(feeRateAmt);
        remittance.setFeePerAmt(exchangeCalcVO.getFee_per_amt());
        remittance.setRemitStatus(RemittanceStatusCode.OP);
        remittance.setCouponUserId(calcResponse.getCouponUserId());
        remittance.setCouponDiscountAmt(calcResponse.getDiscountAmount());
        remittance.setFeeRate(exchangeCalcVO.getFee_rate());

        int remitResult = remittanceMapper.insertRemittance(remittance);

        if (remitResult < 1) {
            logger.error("{} 송금 데이터 생성에 실패했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // EUR 통화에서만 송금인 region 필수값. 영어로만 보내야 함.
        CountryCode countryCode = CountryCode.of(remittance.getToCd());
        List<GoogleAddressResultResponse> englishAddress = null;
        String addressStateProvinceEn = null;
        if (Currency.EUR.name().equals(countryCode.getCurrency())) {
            if (addressStateProvince == null) {
                throw new CashmallowException(INVALID_REGION_VALUE);
            }
            englishAddress = addressEnglishServiceImpl.getSearchResultForGlobal(addressStateProvince);
            if(englishAddress.isEmpty()) {
                throw new CashmallowException(INVALID_REGION_VALUE);
            } else {
                // stateProvince 없으면 가장 마지막 주소를 가져옴 (,로 구분되어있으므로 0번째 index 가져옴)
                if (englishAddress.get(0).getStateProvinceName() != null) {
                    addressStateProvinceEn = englishAddress.get(0).getStateProvinceName();
                } else {
                    addressStateProvinceEn = englishAddress.get(0).getFullAddress().split(",")[0];
                }
            }
        }

        int resultRow = remittanceRepositoryService.insertRemitTravelerSnapshot(traveler, user, remittance.getId(), addressStateProvince, addressStateProvinceEn);

        if (resultRow < 1) {
            logger.error("{} 송금 스냅샷 생성에 실패했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int remitStatusResult = remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.OP);

        if (remitStatusResult < 1) {
            logger.error("{} 송금 상태 데이터 생성에 실패했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 쿠폰 사용
        if (isUseCoupon(couponUserId)) {
            // User 쿠폰 사용
            couponMobileService.useCouponUser(fromCountry.getCode(), userId, couponUserId, exchangeCalcVO.getDiscountAmount(), ServiceType.REMITTANCE.getCode());
        }

        // 3. mapping 테이블의 PIN값을 찾아 송금id를 업데이트(연결)한다.
        int affectedRow = updateRemitIdForMapping(remittance.getId(), mapping.getId(), travelerId);

        if (affectedRow == 1) {
            // 4. 환전 신청 정보를 읽는다.
            return remittanceRepositoryService.getRemittanceByRemittanceId(remittance.getId());
        } else {
            logger.error("{} mapping정보의 환전 정보를 업데이트할 수 없습니다. mappingId={}, pinvalue={}, fromAmt={}", method,
                    mapping.getId(), mapping.getPinValue(), remittance.getFromAmt());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    public static boolean isUseCoupon(Long couponUserId) {
        return couponUserId != null && couponUserId > 0;
    }


    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance requestRemittanceKr(Remittance remittance, BigDecimal fromMoney, String addressStateProvince) throws CashmallowException {
        final String method = "requestRemittance(): ";

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
        User user = userRepositoryService.getUserByUserId(traveler.getUserId());
        Long userId = traveler.getUserId();
        Long travelerId = traveler.getId();
        Country fromCountry = countryService.getCountry(remittance.getFromCd());
        Country toCountry = countryService.getCountry(remittance.getToCd());

        CouponCalcResponse calcResponse = new CouponCalcResponse();
        // 쿠폰 정보 계산
        if (isUseCoupon(remittance.getCouponUserId())) {
            calcResponse = couponMobileService.calcCouponV2(fromCountry, toCountry, remittance.getFromAmt(), remittance.getFeePerAmt(), remittance.getCouponUserId(), userId, ServiceType.REMITTANCE, Const.TRUE);
        } else {
            calcResponse.setPaymentAmount(remittance.getFromAmt());
        }


        BigDecimal fee = remittance.getFee();
        BigDecimal fromAmt = calcResponse.getPaymentAmount();
        BigDecimal feeRateAmt = remittance.getFeeRateAmt();

        logger.info("{} userId={}", method, userId);

        if (!remittance.checkValidation()) {
            logger.error("{} invalid exchange values (userId={})", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 송금이 진행중인 상태가 있으면, 신청할수 없다.
        Remittance opRemittance = remittanceRepositoryService.getRemittanceInprogress(travelerId);

        if (opRemittance != null) {
            logger.error("{} An remittance for the travelerId={} is on progress.", method, travelerId);
            throw new CashmallowException(MsgCode.PREVIOUS_REMITTANCE_IN_PROGRESS);
        }

        ExchangeCalcVO exchangeCalcVO = calcRemittanceV4(remittance.getFromCd(), remittance.getToCd(), BigDecimal.valueOf(0), remittance.getToAmt(), remittance.getRemittanceType(),
                traveler, remittance.getCouponUserId(), isUseCoupon(remittance.getCouponUserId()) ? Const.Y : Const.N);

        // feeRange의 범위 안에 있어야하며, To금액은 같아야한다.
        if (remittance.getToAmt().compareTo(exchangeCalcVO.getTo_money()) != 0) {
            logger.error("{} to_money관련 송금 요청 정보가 올바르지 않습니다. 요청금액={}, 재계산 금액={}", method, remittance.getToAmt(), exchangeCalcVO.getTo_money());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittance.setBankAccountId(openbankAccountId);

        remittance.setTravelerId(travelerId);
        remittance.setFromAmt(fromAmt);
        remittance.setFee(fee);
        remittance.setFeeRateAmt(feeRateAmt);
        remittance.setRemitStatus(RemittanceStatusCode.OP);
        remittance.setCouponUserId(calcResponse.getCouponUserId());
        remittance.setCouponDiscountAmt(calcResponse.getDiscountAmount());

        int remitResult = remittanceMapper.insertRemittance(remittance);
        if (remitResult < 1) {
            logger.error("{} 송금 데이터 생성에 실패했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 쿠폰 사용
        if (isUseCoupon(remittance.getCouponUserId())) {
            // User 쿠폰 사용
            couponMobileService.useCouponUser(fromCountry.getCode(), userId, remittance.getCouponUserId(), exchangeCalcVO.getDiscountAmount(), ServiceType.REMITTANCE.getCode());

            // TODO: 친구 쿠폰 사용 추가되면 초대완료 쿠폰 여부 추가
        }

        String addressStateProvinceEn = null; // 나중에 한국 서비스 할때 추가해야 됨
        int resultRow = remittanceRepositoryService.insertRemitTravelerSnapshot(traveler, user, remittance.getId(), addressStateProvince, addressStateProvinceEn);
        if (resultRow < 1) {
            logger.error("{} 송금 스냅샷 생성에 실패했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int remitStatusResult = remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.OP);
        if (remitStatusResult < 1) {
            logger.error("{} 송금 상태 데이터 생성에 실패했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return remittanceRepositoryService.getRemittanceByRemittanceId(remittance.getId());
    }


    /**
     * Upload receipt photo for remittance.
     *
     * @param remitId
     * @param file
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public RemittanceDepositReceipt uploadReceiptPhoto(long remitId, MultipartFile file)
            throws CashmallowException {
        String method = "uploadRemittanceReceiptPhoto()";

        logger.info("{}: remitId={}", method, remitId);

        String receiptPhoto = fileService.upload(file, Const.FILE_SERVER_RECEIPT_REMIT);

        RemittanceDepositReceipt remittanceDepositReceipt = new RemittanceDepositReceipt(remitId, receiptPhoto);

        int rows = remittanceMapper.insertRemittanceDepositReceipt(remittanceDepositReceipt);
        if (rows != 1) {
            logger.error("{}: remittance_deposit_receipt table update failure", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        if (remittance.getRemitStatus().equals(RemittanceStatusCode.DR)) {
            remittance.setRemitStatus(RemittanceStatusCode.OP);

            int updateRow = remittanceRepositoryService.updateRemittance(remittance);

            int insertRow = remittanceRepositoryService.insertRemitStatus(remitId, RemittanceStatusCode.OP);

            if (updateRow != 1 && insertRow != 1) {
                logger.error("{}: 영수증 업로드중 상태값 업데이트에 실패했습니다. remitId={}, updateRow={}, insertRow={}", method, remittance.getId(), updateRow, insertRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        return remittanceDepositReceipt;
    }

    /**
     * Get RemittanceDepositReceipt List
     *
     * @param remitId
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<RemittanceDepositReceipt> getRemittanceDepositReceiptList(long remitId) {
        return remittanceMapper.getRemittanceDepositReceiptList(remitId);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelRemittanceByAdmin(Long remitId) throws CashmallowException {

        String method = "cancelRemittanceByAdmin()";
        logger.info("{}: cancelRemittanceByAdmin. remitId={}", method, remitId);

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        Remittance.RemittanceStatusCode prevRemittanceStatus = remittance.getRemitStatus();

        if (!RemittanceStatusCode.OP.equals(remittance.getRemitStatus()) && !RemittanceStatusCode.DR.equals(remittance.getRemitStatus())) {
            throw new CashmallowException("송금의 상태가 취소할수 없는 상태입니다. remitId=" + remitId + ", 상태-" + remittance.getRemitStatus());
        }

        remittance.setRemitStatus(RemittanceStatusCode.CC);

        int updateRow = remittanceRepositoryService.updateRemittance(remittance);

        // 쿠폰 원복
        // 쿠폰 개선 적용 25.03.07
        if (isUseCoupon(remittance.getCouponUserId())) {
            couponMobileService.cancelCouponUserV2(remittance.getCouponUserId(), prevRemittanceStatus.name());
        }

        int insertRow = remittanceRepositoryService.insertRemitStatus(remitId, RemittanceStatusCode.CC);

        if (updateRow == 1 && insertRow == 1) {
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
            User user = userRepositoryService.getUserByUserId(traveler.getUserId());

            notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.CC, remittance.getId());
        } else {
            logger.error("{}: 어드민 요청에 의한 취소 업데이트에 실패했습니다. remitId={}, updateRow={}, insertRow={}", method, remittance.getId(), updateRow, insertRow);
            throw new CashmallowException("어드민 요청에 의한 취소 업데이트에 실패했습니다. remitId=" + remittance.getId());
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void reregisterRemitReceiptPhoto(Long remitId, String remitStatus, String message) throws CashmallowException {

        String method = "reregisterRemitReceiptPhoto()";
        logger.info("{}: remitId={}", method, remitId);

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        if (RemittanceStatusCode.DR.name().equals(remitStatus)) {
            if (!RemittanceStatusCode.OP.equals(remittance.getRemitStatus())) {
                throw new CashmallowException("송금 영수증 재등록요청을 할수없는 상태입니다. remitId=" + remitId + ", 현재상태=" + remittance.getRemitStatus() + ", 변경상태=" + remitStatus);
            }
        }

        if (RemittanceStatusCode.RR.name().equals(remitStatus)) {
            if (!RemittanceStatusCode.RC.equals(remittance.getRemitStatus())) {
                throw new CashmallowException("송금 수취인 재등록요청을 할수없는 상태입니다. remitId=" + remitId + ", 현재상태=" + remittance.getRemitStatus() + ", 변경상태=" + remitStatus);
            }
        }

        remittance.setRemitStatus(Remittance.RemittanceStatusCode.valueOf(remitStatus));
        int updateRow = remittanceRepositoryService.updateRemittance(remittance);

        int insertRow = remittanceRepositoryService.insertRemitStatus(remitId, RemittanceStatusCode.valueOf(remitStatus), message);

        if (updateRow == 1 && insertRow == 1) {
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
            User user = userRepositoryService.getUserByUserId(traveler.getUserId());

            // 메세지가 존재하는 경우 푸시 알람에 메세지를 포함하여 발송한다
            String slackMessage = null;
            if (StringUtils.isNotEmpty(message)) {
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.valueOf(remitStatus), remittance.getId(), message);
                slackMessage = ", 메세지: " + message;
            } else {
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.valueOf(remitStatus), remittance.getId());
            }

            notificationService.sendEmailReRegisterReceipt(user);

            String msg = "[ADMIN] 송금 거래번호: " + remittance.getId() +
                    "\n유저ID:" + traveler.getUserId() + ", 은행:" + traveler.getBankName() + ", 이름:" + traveler.getAccountName() + ", 코드:" + traveler.getAccountNo() + ", 금액:" + remittance.getFromAmt() +
                    "\n국가:" + remittance.getFromCd() + ", 금액:" + remittance.getFromAmt() + ", 수수료:" + remittance.getFee() + slackMessage;
            alarmService.aAlert("영수증 재등록(DR)", msg, user);

        } else {
            logger.error("{}: 어드민 요청에 의한 취소 업데이트에 실패했습니다. remitId={}, updateRow={}, insertRow={}", method, remittance.getId(), updateRow, insertRow);
            throw new CashmallowException("어드민 요청에 의한 취소 업데이트에 실패했습니다. remitId=" + remittance.getId());
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelRemittanceByBatch(Long remitId) throws CashmallowException {

        String method = "cancelRemittanceByBatch()";
        logger.info("{}: cancelRemittance. remitId={}", method, remitId);

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        Remittance.RemittanceStatusCode prevRemittanceStatus = remittance.getRemitStatus();

        RemittanceStatusCode remitStatus = remittance.getRemitStatus();
        if (!(RemittanceStatusCode.OP.equals(remitStatus) || RemittanceStatusCode.DR.equals(remitStatus))) {
            throw new CashmallowException("The remitStatus of the remittance data is not 'OP' or 'DR'. remitId=" + remitId);
        }

        remittance.setRemitStatus(RemittanceStatusCode.CC);

        int updateRow = remittanceRepositoryService.updateRemittance(remittance);

        // 쿠폰 원복
        // 쿠폰 개선 적용 25.03.07
        CouponIssueUser couponUser = null;
        if (isUseCoupon(remittance.getCouponUserId())) {
            couponMobileService.cancelCouponUserV2(remittance.getCouponUserId(), prevRemittanceStatus.name());
            couponUser = couponUserService.getCouponUserByIdAndStatus(remittance.getCouponUserId(), AvailableStatus.AVAILABLE);
        }

        int insertRow = remittanceRepositoryService.insertRemitStatus(remitId, RemittanceStatusCode.CC);

        if (updateRow == 1 && insertRow == 1) {
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
            User user = userRepositoryService.getUserByUserId(traveler.getUserId());

            notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.CC, remittance.getId());

            // fromJP일 경우 GlobalJP에 전달
            if (CountryCode.JP.getCode().equals(remittance.getFromCd())) {
                // coupon_user(유저 쿠폰) 테이블은 sync_id 가 없으므로 해당 쿠폰을 찾기 위해 couponIssueSyncId 가 필요함.
                // coupon_issue(발급) 테이블에서 user_id 와 sync_id 는 유니크 하므로 2개의 파라미터로 coupon_user 테이블의 id 를 찾을 수 있음
                Long cancelCouponIssueSyncId = null;
                if (remittance.getCouponUserId() != null && remittance.getCouponUserId() != -1L && couponUser != null) { // 롤백 처리된 쿠폰인지 체크
                    cancelCouponIssueSyncId = couponUser.getCouponIssueId();
                }

                globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.REMITTANCE, remittance.getId(), remittance.getRemitStatus().name(), cancelCouponIssueSyncId);
            }
        } else {
            logger.error("{}: 송금 등록시간 초과로 인한 업데이트에 실패했습니다. remitId={}, updateRow={}, insertRow={}", method, remittance.getId(), updateRow, insertRow);
            throw new CashmallowException("송금 등록시간 초과로 인한 업데이트에 실패했습니다. remitId=" + remittance.getId());
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance cancelRemittanceByTraveler(long remitId) throws CashmallowException {
        String method = "cancelRemittanceByTraveler()";

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        Remittance.RemittanceStatusCode prevRemittanceStatus = remittance.getRemitStatus();

        if (remittance == null) {
            logger.error("{}: traveler of remittance is null. remitId={}", method, remitId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        List<RemittanceDepositReceipt> receipts = getRemittanceDepositReceiptList(remitId);

        if (!receipts.isEmpty()) {
            logger.error("{}: 이미 등록된 영수증이 있어서 캔슬이 불가능합니다. remitId={}, travelerId={}", method, remitId, remittance.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();

        if (RemittanceStatusCode.TC.equals(status) || RemittanceStatusCode.CC.equals(status)) {
            // Already canceled. Do nothing.
            return remittance;

        } else if (!RemittanceStatusCode.OP.equals(status)) {
            logger.error("{}: remit_status is not in progress('OP'). remitId={}",
                    method, remitId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittance.setRemitStatus(RemittanceStatusCode.TC);
        remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRow = remittanceMapper.updateRemittance(remittance);

        // 쿠폰 원복
        // 쿠폰 개선 적용 25.03.07
        CouponIssueUser couponUser = null;
        if (isUseCoupon(remittance.getCouponUserId())) {
            couponMobileService.cancelCouponUserV2(remittance.getCouponUserId(), prevRemittanceStatus.name());
            couponUser = couponUserService.getCouponUserByIdAndStatus(remittance.getCouponUserId(), AvailableStatus.AVAILABLE);
        }

        if (affectedRow != 1) {
            logger.error("{}: 1 than more rows update. affectedRow={}", method, affectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.TC);

        // From KR은 pinValue를 사용하지 않음.
        if (!"003".equals(remittance.getFromCd())) {
            cancelMappingPinByRemitId(remittance.getTravelerId(), remitId, remittance.getFromAmt());
        }
        // fromJP일 경우 GlobalJP에 전달
        if (CountryCode.JP.getCode().equals(remittance.getFromCd())) {
            // coupon_user(유저 쿠폰) 테이블은 sync_id 가 없으므로 해당 쿠폰을 찾기 위해 couponIssueSyncId 가 필요함.
            // coupon_issue(발급) 테이블에서 user_id 와 sync_id 는 유니크 하므로 2개의 파라미터로 coupon_user 테이블의 id 를 찾을 수 있음
            Long cancelCouponIssueSyncId = null;
            if (remittance.getCouponUserId() != null && remittance.getCouponUserId() != -1L && couponUser != null ) { // 롤백 처리된 쿠폰인지 체크
                cancelCouponIssueSyncId = couponUser.getCouponIssueId();
            }
            globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.REMITTANCE, remittance.getId(), remittance.getRemitStatus().name(), cancelCouponIssueSyncId);
        }
        return remittance;
    }

    public Remittance completeDeposit(long remitId) throws CashmallowException {
        String method = "completeDeposit()";

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        if (remittance == null) {
            logger.error("{}: traveler of remittance is null. remittance={}", method, remittance);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();

        // 입금이 먼저발생할 경우 입금완료 버튼을 누르기전에 매핑되서 DP상태가 됨으로 DP도 추가 허용
        if (!RemittanceStatusCode.OP.equals(status) && !RemittanceStatusCode.DP.equals(status)) {
            logger.error("{}: remit_status is not in progress('OP'). remitId={}, status={}", method, remittance.getId(), status);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // RN앱에서 영수증 등록 삭제로 인해 영수증 검증 로직때문에 더미 데이터 추가
        RemittanceDepositReceipt remittanceDepositReceipt = new RemittanceDepositReceipt(remitId, "dummy data");
        remittanceMapper.insertRemittanceDepositReceipt(remittanceDepositReceipt);

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
        User user = userRepositoryService.getUserByUserId(traveler);

        BankAccount bankAccount = companyMapper.getBankAccountByBankAccountId(Math.toIntExact(remittance.getBankAccountId()));

        // 입금정보 등록한 건에 대해서 메시지 보낸다.
        String sb = "송금 거래번호: " + remittance.getId() +
                "\n국가:" + remittance.getFromCd() + ", 회사은행:" + bankAccount.getBankName() +
                "\n유저ID:" + traveler.getUserId() + ", 은행:" + traveler.getBankName() + ", 이름:" + traveler.getAccountName() + ", 코드:" + traveler.getAccountNo() + ", 금액:" + remittance.getFromAmt() +
                "\n적용환율:" + remittance.getExchangeRate() +
                "\n국가:" + remittance.getFromCd() + ", 금액:" + remittance.getFromAmt() + ", 수수료:" + remittance.getFee() +
                "\n대상국가:" + remittance.getToCd() + ", 대상금액:" + remittance.getToAmt();

        alarmService.aAlert("송금신청 입금정보등록", sb, user);

        return remittance;
    }

    /**
     * 송금 수취인 재등록 RR -> RC
     *
     * @param remitId
     * @param params
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance reRegisterRemittance(long remitId, Remittance params) throws CashmallowException {
        String method = "reRegisterRemittance()";
        // 기존 DP 상태
        // 다시 등록시 새로운 상태값 추가 : RC - 송금실패로 인해 재등록 완료상태
        RemittanceStatusCode nextStatusCode = RemittanceStatusCode.RC;

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        if (remittance == null) {
            logger.error("{}: traveler of remittance is null. remittance={}", method, remittance);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();
        if (!RemittanceStatusCode.RR.equals(status)) {
            logger.error("{}: remit_status is not 'RR'. remitId={}, status={}", method, remittance.getId(), status);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittance.setReceiverCountry(params.getReceiverCountry());
        remittance.setReceiverPhoneNo(params.getReceiverPhoneNo());
        remittance.setReceiverPhoneCountry(params.getReceiverPhoneCountry());
        remittance.setReceiverFirstName(params.getReceiverFirstName());
        remittance.setReceiverLastName(params.getReceiverLastName());
        remittance.setReceiverBirthDate(params.getReceiverBirthDate());

        remittance.setReceiverBankName(params.getReceiverBankName());
        remittance.setReceiverBankBranchName(params.getReceiverBankBranchName());
        remittance.setReceiverBankCode(params.getReceiverBankCode());
        remittance.setReceiverBankAccountNo(params.getReceiverBankAccountNo());

        remittance.setReceiverAddress(params.getReceiverAddress());
        remittance.setReceiverAddressCountry(params.getReceiverAddressCountry());
        remittance.setReceiverAddressCity(params.getReceiverAddressCity());
        remittance.setReceiverAddressSecondary(params.getReceiverAddressSecondary());

        remittance.setReceiverAccountType(params.getReceiverAccountType());
        remittance.setReceiverIbanCode(params.getReceiverIbanCode());
        remittance.setReceiverSwiftCode(params.getReceiverSwiftCode());
        remittance.setReceiverIfscCode(params.getReceiverIfscCode());
        remittance.setReceiverRoutingNumber(params.getReceiverRoutingNumber());
        remittance.setReceiverCardNumber(params.getReceiverCardNumber());

        remittance.setRemitStatus(RemittanceStatusCode.RC);
        remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRow = remittanceMapper.updateRemittance(remittance);
        if (affectedRow != 1) {
            logger.error("{}: Failed to update remittance table. affectedRow={}", method, affectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittanceRepositoryService.insertRemitStatus(remittance.getId(), nextStatusCode);

        return remittance;
    }

    /**
     * AML 완료시 업데이트 RC -> DP
     *
     * @param remittance
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance updateRemittanceAmlVerified(Remittance remittance) throws CashmallowException {
        String method = "updateRemittanceAmlVerified()";
        RemittanceStatusCode nextStatusCode = RemittanceStatusCode.DP;

        if (remittance == null) {
            logger.error("{}: traveler of remittance is null. remittance={}", method, remittance);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();
        if (!RemittanceStatusCode.RC.equals(status)) {
            logger.error("{}: remit_status is not 'RC'. remitId={}, status={}", method, remittance.getId(), status);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittance.setRemitStatus(nextStatusCode);
        remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRow = remittanceMapper.updateRemittance(remittance);
        if (affectedRow != 1) {
            logger.error("{}: Failed to update remittance table. affectedRow={}", method, affectedRow);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        remittanceRepositoryService.insertRemitStatus(remittance.getId(), nextStatusCode);

        return remittance;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance updateForRefund(long remitId, BigDecimal refundFromAmt) throws CashmallowException {
        String method = "refundRemittance()";

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        if (remittance == null) {
            logger.error("{}: Cannot find the remittance. remitId={}", method, remitId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (remittance.getToAmt().compareTo(refundFromAmt) != 0) {
            logger.error("{}: Refund amount is not same with the remittance amount. remitId={}, remittance.getToAmt()={}, refundFromAmt={}",
                    method, remitId, remittance.getToAmt(), refundFromAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();
        if (RemittanceStatusCode.RR.equals(status) || RemittanceStatusCode.RC.equals(status)) {
            remittance.setRemitStatus(RemittanceStatusCode.RP);
            remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            int affectedRow = remittanceMapper.updateRemittance(remittance);

            if (affectedRow != 1) {
                logger.error("{}: Cannot update the remittance. travelerId={}, remitId={}, remitStatus={}, affectedRow={}",
                        method, remittance.getTravelerId(), remitId, status, affectedRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.RP);
        } else {
            logger.error("{}: Cannot refund the remittance. travelerId={}, remitId={}, remitStatus={}",
                    method, remittance.getTravelerId(), remitId, status);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return remittance;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance updateForRefundCancel(long remitId, BigDecimal refundFromAmt) throws CashmallowException {
        String method = "updateForRefundCancel()";

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        if (remittance == null) {
            logger.error("{}: Cannot find the remittance. remitId={}", method, remitId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (remittance.getToAmt().compareTo(refundFromAmt) != 0) {
            logger.error("{}: Refund amount is not same with the remittance amount. remitId={}, remittance.getToAmt()={}, refundFromAmt={}",
                    method, remitId, remittance.getToAmt(), refundFromAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();
        if (RemittanceStatusCode.RP.equals(status)) {
            remittance.setRemitStatus(RemittanceStatusCode.RR);
            remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            int affectedRow = remittanceMapper.updateRemittance(remittance);

            if (affectedRow != 1) {
                logger.error("{}: Cannot update the remittance. travelerId={}, remitId={}, remitStatus={}, affectedRow={}",
                        method, remittance.getTravelerId(), remitId, status, affectedRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            affectedRow = remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.RR);

        } else {
            logger.error("{}: Cannot refund the remittance. travelerId={}, remitId={}, remitStatus={}",
                    method, remittance.getTravelerId(), remitId, status);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return remittance;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance updateForRefundComplete(long remitId, BigDecimal refundFromAmt) throws CashmallowException {
        String method = "updateForRefundComplete()";

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        if (remittance == null) {
            logger.error("{}: Cannot find the remittance. remitId={}", method, remitId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (remittance.getToAmt().compareTo(refundFromAmt) != 0) {
            logger.error("{}: Refund amount is not same with the remittance amount. remitId={}, remittance.getToAmt()={}, refundFromAmt={}",
                    method, remitId, remittance.getToAmt(), refundFromAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        RemittanceStatusCode status = remittance.getRemitStatus();
        if (RemittanceStatusCode.RP.equals(status)) {
            remittance.setRemitStatus(RemittanceStatusCode.RF);
            remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            int affectedRow = remittanceMapper.updateRemittance(remittance);

            if (affectedRow != 1) {
                logger.error("{}: Cannot update the remittance. travelerId={}, remitId={}, remitStatus={}, affectedRow={}",
                        method, remittance.getTravelerId(), remitId, status, affectedRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.RF);
        } else {
            logger.error("{}: Cannot refund the remittance. travelerId={}, remitId={}, remitStatus={}",
                    method, remittance.getTravelerId(), remitId, status);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return remittance;
    }


    public ExchangeCalcVO calcRemittanceAnonymous(String fromCd, String toCd, BigDecimal fromMoney,
                                                  BigDecimal toMoney, Remittance.RemittanceType remittanceType, Long userId) throws CashmallowException {

        String method = "calcRemittanceExchange(): ";

        logger.info("{}: fromCd={}, toCd={}, fromMoney={}, toMoney={}", method, fromCd, toCd, fromMoney, toMoney);

        try {
            Map<String, Object> params = new HashMap<>();

            params.put("code", toCd);
            Country toCountry = countryService.getCountryList(params).get(0);

            params.put("code", fromCd);
            Country fromCountry = countryService.getCountryList(params).get(0);

            // source 와 target 이 바뀐 것이 아님. to_money 기준으로 환전 금액을 계산하고 있어서 이렇게 함.
            // 즉, to_money 가 10000 원이면 from_money 는 10000 * 적용환율
            // 적용환율 : 기준환율에서 일정 비율로 조정된 환율.
            params.clear();
            params.put("source", toCountry.getIso4217());
            params.put("target", fromCountry.getIso4217());
            CurrencyRate currencyRate = currencyService.getCurrencyRate(params);

            // 수수료 정보 조회
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

            fromMoney = fromMoney == null ? BigDecimal.ZERO : fromMoney;

            toMoney = toMoney == null ? BigDecimal.ZERO : toMoney;

            BigDecimal mappingInc = fromCountry.getMappingInc();
            BigDecimal unitScale = toCountry.getUnitScale();
            BigDecimal baseRate = currencyRate.getRate();
            // mappingInc: 특정 최소 화폐 금액(예: 10원, 100원, 0.01달러 등)에 맞춰 처리하는 단위
            // unitScale: 요청 가능한 금액 단위
            // baseRate: 기준 수수료
            if (mappingInc == null || unitScale == null || baseRate == null ||
                    BigDecimal.ZERO.compareTo(mappingInc) == 0 || BigDecimal.ZERO.compareTo(unitScale) == 0 || BigDecimal.ZERO.compareTo(baseRate) == 0) {
                logger.error("mappingInc, unitScale, baseRate 이 0 이거나 null 일 수 없습니다.");
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            BigDecimal spreadRate = exchangeCalculateService.getSpreadRate(baseRate, exchangeConfig.getFeeRateRemittance());

            Map<String, String> exchangeLimit = getRemittanceExchangeLimit(fromCountry, toCountry, exchangeConfig);
            BigDecimal maxRemittanceExchange = new BigDecimal(exchangeLimit.get("max"));
            BigDecimal minRemittanceExchange = new BigDecimal(exchangeLimit.get("min"));

            if (fromMoney.compareTo(BigDecimal.ZERO) > 0 && toMoney.equals(BigDecimal.ZERO)) {
                toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                        .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
            }

            if (toMoney.compareTo(minRemittanceExchange) < 0) {
                toMoney = minRemittanceExchange;
            }

            // NPR 송금 BANK, WALLET 제한금액
            // TODO: 추후 기획 완료 시, 테이블에서 조회함. WALLET 이 먼저 나가므로 추가함.
            if (CountryCode.NP.getCode().equals(toCd)) {
                if (Remittance.RemittanceType.WALLET == remittanceType) {
                    maxRemittanceExchange = new BigDecimal("50000");
                } else if (Remittance.RemittanceType.CASH_PICKUP == remittanceType) {
                    maxRemittanceExchange = new BigDecimal("999999");
                } else {
                    maxRemittanceExchange = new BigDecimal("1500000");
                }
            }

            if (toMoney.compareTo(maxRemittanceExchange) > 0) {
                toMoney = maxRemittanceExchange;
            }

            // unitScale 단위로 잘라서 반올림한 금액이 min 보다 크고 max 보다 작아야 한다.
            BigDecimal halfUp = toMoney.divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
            // min 보다 작으면 올림
            if (halfUp.compareTo(minRemittanceExchange) < 0) {
                toMoney = toMoney.divide(unitScale, 0, RoundingMode.CEILING).multiply(unitScale);
            }
            // max 보다 크면 내림
            if (halfUp.compareTo(maxRemittanceExchange) > 0) {
                toMoney = toMoney.divide(unitScale, 0, RoundingMode.FLOOR).multiply(unitScale);
            }

            // toMoney 를 mappingInc 기준으로 절사
            BigDecimal toMoneyMappingInc = toCountry.getMappingInc();
            // mappingInc가 0보다 클 경우에만 절사 로직을 수행합니다.
            if (toMoneyMappingInc != null && toMoneyMappingInc.compareTo(BigDecimal.ZERO) > 0) {
                toMoney = toMoney.divide(toMoneyMappingInc, 0, RoundingMode.DOWN)
                        .multiply(toMoneyMappingInc);
            }

            if (toMoney.compareTo(BigDecimal.ZERO) <= 0) {
                String option = String.format("{iso4217:%s, amt:%d}", toCountry.getIso4217(), maxRemittanceExchange.intValue());
                // Exchange is disabled, return failure with exchange_notice.
                throw new CashmallowException("EXCHANGE_EXCEEDED_LIMIT", option);
            }


            // calcExchangeByFromMoney 계산 후에 to_money 기준으로 다시 한번 계산해서 인출 단위로 금액을 맞춤.
            fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);

            BigDecimal feeRateAmt = exchangeCalculateService.getFeeRateAmt(toMoney, baseRate, spreadRate, mappingInc);
            BigDecimal feePerAmt = exchangeConfig.getFeePerRemittance();

            BigDecimal fee = feeRateAmt.add(feePerAmt);

            ExchangeCalcVO vo = new ExchangeCalcVO();
            vo.setFrom_cd(fromCd);
            vo.setTo_cd(toCd);
            vo.setFrom_money(fromMoney);
            vo.setTo_money(toMoney);
            vo.setFee(fee);
            vo.setFee_rate(exchangeConfig.getFeeRateRemittance());
            vo.setFee_per_amt(feePerAmt);
            vo.setFee_rate_amt(feeRateAmt);
            vo.setExchange_rate(spreadRate);
            vo.setBase_exchange_rate(baseRate);

            logger.info("{}: vo={}", method, vo);
            return vo;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * V4 쿠폰 개선 적용
     * 송금 수수료 계산 - 쿠폰 적용 버젼
     *
     * @param fromCd
     * @param toCd
     * @param fromMoney
     * @param toMoney
     * @param traveler
     * @param couponUserId
     * @param couponUseYn
     * @return
     * @throws CashmallowException
     */
    @Transactional
    public ExchangeCalcVO calcRemittanceV4(String fromCd, String toCd, BigDecimal fromMoney, BigDecimal toMoney, Remittance.RemittanceType remittanceType,
                                           Traveler traveler, Long couponUserId, String couponUseYn) throws CashmallowException {

        String method = "calcRemittanceExchangeV3(): ";

        logger.info("{}: fromCd={}, toCd={}, fromMoney={}, toMoney={}, couponUserId={}", method, fromCd, toCd, fromMoney, toMoney, couponUserId);

        Long userId = traveler.getUserId();
        Locale locale = LocaleContextHolder.getLocale();

        try {
            ExchangeCalcVO vo = new ExchangeCalcVO();
            Map<String, Object> params = new HashMap<>();

            // Traveler traveler = travelerService.getTravelerByUserId(userId);

            params.put("code", toCd);
            Country toCountry = countryService.getCountryList(params).get(0);

            params.put("code", fromCd);
            Country fromCountry = countryService.getCountryList(params).get(0);

            // source 와 target 이 바뀐 것이 아님. to_money 기준으로 환전 금액을 계산하고 있어서 이렇게 함.
            // 즉, to_money 가 10000 원이면 from_money 는 10000 * 적용환율
            // 적용환율 : 기준환율에서 일정 비율로 조정된 환율.
            params.clear();
            params.put("source", toCountry.getIso4217());
            params.put("target", fromCountry.getIso4217());
            CurrencyRate currencyRate = currencyService.getCurrencyRate(params);

            fromMoney = fromMoney == null ? BigDecimal.ZERO : fromMoney;

            toMoney = toMoney == null ? BigDecimal.ZERO : toMoney;

            // 수수료 정보 조회
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);
            BigDecimal mappingInc = fromCountry.getMappingInc();
            BigDecimal unitScale = toCountry.getUnitScale();
            BigDecimal baseRate = currencyRate.getRate();
            // mappingInc: 특정 최소 화폐 금액(예: 10원, 100원, 0.01달러 등)에 맞춰 처리하는 단위
            // unitScale: 요청 가능한 금액 단위
            // baseRate: 기준 수수료
            if (mappingInc == null || unitScale == null || baseRate == null ||
                    BigDecimal.ZERO.compareTo(mappingInc) == 0 || BigDecimal.ZERO.compareTo(unitScale) == 0 || BigDecimal.ZERO.compareTo(baseRate) == 0) {
                logger.error("mappingInc, unitScale, baseRate 이 0 이거나 null 일 수 없습니다.");
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            BigDecimal spreadRate = exchangeCalculateService.getSpreadRate(baseRate, exchangeConfig.getFeeRateRemittance());
            // if (envUtil.isDev()) {
            //     unitScale = toCountry.getMappingInc();
            // }

            if (fromMoney.compareTo(BigDecimal.ZERO) > 0 && toMoney.equals(BigDecimal.ZERO)) {
                toMoney = fromMoney.divide(spreadRate, 9, RoundingMode.HALF_UP);
            }

            // Validation 체크를 위해서 fromMoney를 셋팅해준다. 밑에서 최종 toMoney가 결정되면 다시 계산해준다.
            fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);

            Map<String, String> exchangeLimit = getRemittanceExchangeLimit(fromCountry, toCountry, exchangeConfig);
            BigDecimal maxRemittanceExchange = new BigDecimal(exchangeLimit.get("max"));
            BigDecimal minRemittanceExchange = new BigDecimal(exchangeLimit.get("min"));

            // NPR 송금 BANK, WALLET 제한금액
            // TODO: 추후 기획 완료 시, 테이블에서 조회함. WALLET 이 먼저 나가므로 추가함.
            if (CountryCode.NP.getCode().equals(toCd)) {
                if (Remittance.RemittanceType.WALLET == remittanceType) {
                    maxRemittanceExchange = new BigDecimal("50000");
                } else if (Remittance.RemittanceType.CASH_PICKUP == remittanceType) {
                    maxRemittanceExchange = new BigDecimal("999999");
                } else {
                    maxRemittanceExchange = new BigDecimal("1500000");
                }
            }

            if (toMoney.compareTo(minRemittanceExchange) < 0) {
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(minRemittanceExchange));
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxRemittanceExchange));

                vo.setStatus(Const.MONEY_EXCHANGE_LIMIT_STATUS);
                vo.setMessage(messageSource.getMessage("WITHDRAWAL_EXCHANGE_MIN_MAX_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

                toMoney = minRemittanceExchange;
                fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);
            }

            if (toMoney.compareTo(maxRemittanceExchange) > 0) {
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(minRemittanceExchange));
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxRemittanceExchange));

                vo.setStatus(Const.MONEY_EXCHANGE_LIMIT_STATUS);
                vo.setMessage(messageSource.getMessage("WITHDRAWAL_EXCHANGE_MIN_MAX_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));
                toMoney = maxRemittanceExchange;
                fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);
            }

            Map<String, Object> annualLimitForFC = limitCheckService.validateAnnualLimitForFromCountry(traveler, fromMoney, fromCountry, locale, exchangeConfig);
            Map<String, Object> monthLimitForFC = limitCheckService.validateMonthLimitForFromCountry(traveler, fromMoney, fromCountry, locale, exchangeConfig);
            Map<String, Object> dayLimitForFC = limitCheckService.validateDayLimitForFromCountry(traveler, fromMoney, fromCountry, locale, exchangeConfig);

            if (StringUtils.equals((String) annualLimitForFC.get("status"), Const.STATUS_FAILURE)) {
                if (annualLimitForFC.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) annualLimitForFC.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) annualLimitForFC.get("message"));
                    fromMoney = (BigDecimal) annualLimitForFC.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            if (StringUtils.equals((String) monthLimitForFC.get("status"), Const.STATUS_FAILURE)) {
                if (monthLimitForFC.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) monthLimitForFC.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) monthLimitForFC.get("message"));
                    fromMoney = (BigDecimal) monthLimitForFC.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            if (StringUtils.equals((String) dayLimitForFC.get("status"), Const.STATUS_FAILURE)) {
                if (dayLimitForFC.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) dayLimitForFC.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) dayLimitForFC.get("message"));
                    fromMoney = (BigDecimal) dayLimitForFC.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            Map<String, Object> toCountryLimit = limitCheckService.validateLimitForToCountry(traveler, toMoney, toCountry, locale, true, exchangeConfig);

            if (StringUtils.equals((String) toCountryLimit.get("status"), Const.STATUS_FAILURE)) {
                if (toCountryLimit.get("toAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) toCountryLimit.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) toCountryLimit.get("message"));
                    toMoney = (BigDecimal) toCountryLimit.get("toAmt");
                }
            }

            // unitScale 단위로 잘라서 반올림한 금액이 min 보다 크고 max 보다 작아야 한다.
            BigDecimal halfUp = toMoney.divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
            // min 보다 작으면 올림
            if (halfUp.compareTo(minRemittanceExchange) < 0) {
                toMoney = toMoney.divide(unitScale, 0, RoundingMode.CEILING).multiply(unitScale);
            }
            // max 보다 크면 내림
            if (halfUp.compareTo(maxRemittanceExchange) > 0) {
                toMoney = toMoney.divide(unitScale, 0, RoundingMode.FLOOR).multiply(unitScale);
            }

            // toMoney 를 mappingInc 기준으로 절사
            BigDecimal toMoneyMappingInc = toCountry.getMappingInc();
            // mappingInc가 0보다 클 경우에만 절사 로직을 수행합니다.
            if (toMoneyMappingInc != null && toMoneyMappingInc.compareTo(BigDecimal.ZERO) > 0) {
                toMoney = toMoney.divide(toMoneyMappingInc, 0, RoundingMode.DOWN)
                        .multiply(toMoneyMappingInc);
            }

            // unitScale 단위로 맞춰주고 다시 계산한다.
            fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);

            BigDecimal feeRateAmt = exchangeCalculateService.getFeeRateAmt(toMoney, baseRate, spreadRate, mappingInc);
            BigDecimal feePerAmt = exchangeConfig.getFeePerRemittance();

            BigDecimal fee = feeRateAmt.add(feePerAmt);

            CouponCalcResponse calcResponse = new CouponCalcResponse();
            // 쿠폰 정보 계산
            if (Const.Y.equals(couponUseYn)) {
                calcResponse = couponMobileService.calcCouponV2(fromCountry, toCountry, fromMoney.add(feeRateAmt), feePerAmt, couponUserId, userId, ServiceType.REMITTANCE, Const.FALSE);
            } else {
                calcResponse.setPaymentAmount(fromMoney.add(feePerAmt).add(feeRateAmt));
            }

            vo.setFrom_cd(fromCd);
            vo.setTo_cd(toCd);
            vo.setFrom_money(fromMoney);
            vo.setTo_money(toMoney);
            vo.setFee(fee);
            vo.setFee_rate(exchangeConfig.getFeeRateRemittance());
            vo.setFee_per_amt(feePerAmt);
            vo.setFee_rate_amt(feeRateAmt);
            vo.setExchange_rate(spreadRate);
            vo.setBase_exchange_rate(baseRate);
            vo.setCouponUserId(calcResponse.getCouponUserId());
            vo.setDiscountAmount(calcResponse.getDiscountAmount());
            vo.setPaymentAmount(calcResponse.getPaymentAmount());

            // 쿠폰 적용 discount 금액이 있고 기존 메세지가 없을 경우 쿠폰 적용 메세지 전달
            if (couponUserId == null && vo.getDiscountAmount() != null && vo.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                    && StringUtils.isEmpty(vo.getMessage())) {
                vo.setStatus(Const.STATUS_SUCCESS);
                vo.setMessage(messageSource.getMessage("COUPON_USED_AUTOMATICALLY", null, "Coupon has been used automatically", locale));
            }

            logger.info("{}: vo={}", method, vo);
            return vo;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, String> getRemittanceExchangeLimit(Country fromCountry, Country toCountry, ExchangeConfig exchangeConfig) throws CashmallowException {

        String method = "getRemittanceExchangeLimit()";
        Map<String, String> result = new HashMap<>();

        logger.info("{}: fromCd={}, toCd={}", method, fromCountry.getCode(), toCountry.getCode());

        if (fromCountry.equals(toCountry)) {
            return result;
        }

        BigDecimal feeRate = exchangeConfig.getFeeRateRemittance();

        Map<String, Object> params = new HashMap<>();
        params.put("source", toCountry.getIso4217());
        params.put("target", fromCountry.getIso4217());
        BigDecimal currencyRate = currencyService.getCurrencyRate(params).getRate();

        BigDecimal min = exchangeConfig.getToMinRemittance();
        BigDecimal max = exchangeConfig.getToMaxRemittance();

        if (min.compareTo(toCountry.getUnitScale()) < 0) {
            min = toCountry.getUnitScale();
        }

        // from 국가의 fromMax환전값을 환율로 계산해서 toMax를 구한뒤 to국가의 맥스와 비교해서 둘중 작은 것으로 선택함.
        BigDecimal fromMax = exchangeCalculateService.getFromMaxAmountToCurrency(feeRate, exchangeConfig.getFromMaxRemittance(), toCountry.getUnitScale(), currencyRate);

        if (fromMax.compareTo(max) < 0) {
            max = fromMax;
        }

        result.put("min", min.toString());
        result.put("max", max.toString());

        return result;
    }

    @Transactional
    public int updateRemitIdForMapping(Long remitId, Long mappingId, Long travelerId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("remitId", remitId);
        params.put("mappingId", mappingId);
        params.put("travelerId", travelerId);
        return mappingMapper.updateRemitIdAfterReqRemittance(params);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelMappingPinByRemitId(long travelerId, long remitId, BigDecimal fromAmt) throws CashmallowException {
        String method = "cancelPinValueByRemitId()";

        Map<String, Object> params = new HashMap<>();
        params.put("travelerId", travelerId);
        params.put("remitId", remitId);
        Mapping mapping = mappingMapper.getMappingByRemitId(params);

        if (mapping == null) {
            logger.error("{}: Cannot find data. travelerId={}, remitId={}", method, travelerId, remitId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if ("OP".equals(mapping.getStatus())) {
            logger.info("{}: Alread done.  travelerId={}, remitId={}, mapping.getStatus()={}",
                    method, travelerId, remitId, mapping.getStatus());
            return;
        }

        if ("CF".equals(mapping.getStatus()) && mapping.getPinValue().compareTo(fromAmt) == 0) {
            mapping.setStatus("TC");
            int affectedRow = mappingMapper.updateMapping(mapping);
            if (affectedRow != 1) {
                logger.error("{}: Failed to update mapping table. travelerId={}, remitId={}", method, travelerId, remitId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.error("{}: Cannot find data. travelerId={}, remitId={}, pinValue={}", method, travelerId, remitId, fromAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void changeRemittanceBankAccount(long remitId, BankAccount bankAccount) throws CashmallowException {
        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        remittance.setBankAccountId(Long.valueOf(bankAccount.getId()));
        remittanceRepositoryService.updateRemittanceBankAccountId(remittance);
    }

}