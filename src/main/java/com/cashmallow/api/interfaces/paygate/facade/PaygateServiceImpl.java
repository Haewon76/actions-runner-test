package com.cashmallow.api.interfaces.paygate.facade;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.UserAdminService;
import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.domain.model.company.*;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.MatchPaygateRecordRequest;
import com.cashmallow.api.interfaces.admin.dto.PaygateRecordDeleteRequest;
import com.cashmallow.api.interfaces.admin.dto.PaygateRecordRequestManual;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsDepositRecordRequest;
import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.cashmallow.api.interfaces.global.dto.GlobalRollbackMappingRequest;
import com.cashmallow.api.interfaces.global.dto.InsertDepositBulkRequest;
import com.cashmallow.api.interfaces.global.dto.InsertDepositRequest;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceServiceImpl;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.application.impl.RemittanceServiceImpl.isUseCoupon;
import static com.cashmallow.api.domain.model.country.enums.CountryCode.HK;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
public class PaygateServiceImpl {
    Logger logger = LoggerFactory.getLogger(PaygateServiceImpl.class);

    public static final int FIXED_BITS = 256;
    public static final String COMMON_ENC = "UTF-8";

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private DbsProperties dbsProperties;

    @Value(value = "${paygate.bankAccountId}")
    private Long paygateAccountId;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private MallowlinkRemittanceServiceImpl mallowlinkRemittanceService;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponMobileServiceV2 couponMobileService;
    @Autowired
    private CouponServiceV2 couponService;


    /**
     * 수기 충전
     *
     * @param record
     * @return
     * @throws CashmallowException
     */
    @Transactional
    public PaygateRecord insertPaygateTransaction(PaygateRecordRequestManual record) throws CashmallowException {

        PaygateRecord paygateRecord = new PaygateRecord();
        paygateRecord.setAmount(record.getAmount());

        paygateRecord.setIso4217("HKD");
        paygateRecord.setExecutedDate(Timestamp.valueOf(LocalDateTime.now()));

        StringBuilder txnId = new StringBuilder();
        txnId.append("EX").append(record.getExchangeId());
        txnId.append("-").append(UUID.randomUUID().toString().substring(0, 5).toUpperCase()).toString();
        paygateRecord.setId(txnId.toString());

        paygateRecord.setDepWdrType("DEPOSIT");
        paygateRecord.setCountry(HK.getCode());
        paygateRecord.setBankAccountId(record.getBankAccountId());
        paygateRecord.setDescription("수기 충전 tid=" + txnId.toString());
        paygateRecord.setWorkStatus(PaygateRecord.WorkStatus.READY);

        BigDecimal balance = companyService.getPaygateRecordLastBalance(paygateRecord.getBankAccountId(), paygateRecord.getIso4217());
        paygateRecord.setBalance(balance.add(paygateRecord.getAmount()));

        companyService.insertPaygateRecord(paygateRecord);

        return paygateRecord;
    }

    @Transactional
    public void deletePaygateTransaction(PaygateRecordDeleteRequest record) throws CashmallowException {

        String workerName = "unknown";
        final String mdcUserId = MDC.get("userId");

        if (StringUtils.isNotBlank(mdcUserId)) {
            workerName = userAdminService.getAdminName(Long.parseLong(mdcUserId));
        }

        record.setDescription("worker: " + workerName + ", " + record.getDescription());
        companyService.deletePaygateRecord(record);
    }


    /**
     * 수기 입력한 record를 실제 record로 교체.
     *
     * @param matchRequest
     * @return
     * @throws CashmallowException
     */
    @Transactional
    public void replacePaygateTransaction(MatchPaygateRecordRequest matchRequest) throws CashmallowException {
        PaygateRecord tempRecord = companyService.getPaygateRecord(matchRequest.getCmTempTid());
        PaygateRecord realRecord = companyService.getPaygateRecord(matchRequest.getRealTid());

        if (realRecord.getId().startsWith("EX")) {
            logger.error(INTERNAL_SERVER_ERROR + "올바르지 않은 트랜잭션 tid={}", tempRecord.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (tempRecord.getAmount().compareTo(realRecord.getAmount()) != 0) {
            String message = "서로 금액이 다른 트랜잭션입니다." +
                    "\nManual:{Tid=" + tempRecord.getId() + ", Amount=" + tempRecord.getAmount() + "}" +
                    "\nPaygate:{Tid=" + realRecord.getId() + ", Amount=" + realRecord.getAmount() + "}";
            throw new CashmallowException(message);
        }

        // transactionMapping Tid 변경
        List<TransactionMapping> transactionMappingByPaygateRecordId = companyService.getTransactionMappingByPaygateRecordId(tempRecord.getId());
        if (transactionMappingByPaygateRecordId.size() != 1) {
            logger.error(INTERNAL_SERVER_ERROR + "존재하지 않거나 맵핑 되지 않은 임시 트랜잭션 tid={}", tempRecord.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
        TransactionMapping transactionMapping = transactionMappingByPaygateRecordId.get(0);
        companyService.changeTransactionMappingId(transactionMapping, realRecord.getId());

        // real paygate record에 내역 기록
        realRecord.setDescription("실제 Tid 교체 완료 " + realRecord.getDescription() + ", " + tempRecord.getDescription());
        companyService.updatePaygateRecord(realRecord);

        PaygateRecordDeleteRequest request = new PaygateRecordDeleteRequest();
        request.setExchangeId(tempRecord.getId());
        request.setDescription(realRecord.getDescription());
        // 임시 record 제거
        companyService.deletePaygateRecord(request);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void rollbackTransactionMapping(GlobalRollbackMappingRequest rollbackMappingRequest, BankAccount bankAccount) throws CashmallowException {

        if (!RelatedTxnType.EXCHANGE.equals(rollbackMappingRequest.txnType())) {
            throw new CashmallowException("환전거래만 매핑을 롤백할수 있습니다.");
        }

        Map<String, String> txnRecParams = new HashMap<>();

        txnRecParams.put("relatedTxnType", rollbackMappingRequest.txnType().name());
        txnRecParams.put("relatedTxnId", rollbackMappingRequest.relatedTxnId().toString());

        List<TransactionRecord> transactionRecordsList = companyService.getTransactionRecordsList(txnRecParams);

        TransactionRecord originalTxnRec = transactionRecordsList.stream()
                .filter(txnRec -> ObjectUtils.isEmpty(txnRec.getRootId()))
                .findFirst().orElseThrow(() -> new CashmallowException("유효하지 않은 거래 ID입니다. txnId=" + rollbackMappingRequest.relatedTxnId()));


        Map<String, Object> paygateRecParams = new HashMap<>();
        List<PaygateRecord> paygateRecords = new ArrayList<>();

        for (String paygateRecId : rollbackMappingRequest.depositIdList()) {
            paygateRecParams.put("id", paygateRecId);
            paygateRecParams.put("bankAccountId", bankAccount.getId().toString());
            paygateRecords.addAll(companyService.getPaygateRecords(paygateRecParams));
        }

        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(rollbackMappingRequest.relatedTxnId());

        exchange.setExStatus("OP");
        exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        exchangeRepositoryService.updateExchange(exchange);

        List<TravelerWallet> walletList = walletRepositoryService.getTravelerWalletByExchangeIds(exchange.getId().toString());

        if (walletList.isEmpty()) {
            throw new CashmallowException("이미 지갑을 비운 사용자입니다. exchangeId=" + exchange.getId());
        }

        walletList.forEach(wallet -> walletRepositoryService.deleteTravelerWallet(wallet.getId()));

        String paygateRecordIdList = paygateRecords.stream().map(PaygateRecord::getId).collect(Collectors.joining(","));

        RollbackMappingHistory rollbackMappingHistory = new RollbackMappingHistory(originalTxnRec, paygateRecordIdList);
        companyService.insertRollbackMappingHistory(rollbackMappingHistory);

        companyService.deleteTransactionMapping(originalTxnRec.getId());
        transactionRecordsList.forEach(txnRec -> companyService.deleteTrasactionRecord(txnRec));
    }


    @Transactional(rollbackFor = CashmallowException.class)
    public TransactionMapping matchTransactionMappingForManual(TransactionRecord transactionRecord,
                                                               TransactionMapping transactionMapping,
                                                               List<String> paygateRecdIds,
                                                               Long bankAccountId,
                                                               String inputAccountNo,
                                                               String inputName,
                                                               boolean manual)
            throws CashmallowException {
        String method = "matchTransactionMappingForManual()";

        Long couponUserId = -1L;
        Long userId = -1L;
        String fromCountryCode = null;

        if (transactionRecord.getRelatedTxnType().equals(RelatedTxnType.REMITTANCE.name())) {
            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(transactionRecord.getRelatedTxnId());
            transactionMapping.setTravelerId(remittance.getTravelerId());
            Remittance inProgressRemittance = remittanceRepositoryService.getRemittanceInprogress(remittance.getTravelerId());
            if (inProgressRemittance != null && !remittance.getId().equals(inProgressRemittance.getId())) {
                logger.error("{}: 해당 유저가 진행중인 다른 송금건이 있습니다. remittance={}, inProgressRemittance={}", method, remittance, inProgressRemittance);
                throw new CashmallowException("해당 유저가 진행중인 다른 송금건이 있습니다.");
            }
            couponUserId = remittance.getCouponUserId();
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
            userId = traveler.getUserId();
            fromCountryCode = remittance.getFromCd();

        } else if (transactionRecord.getRelatedTxnType().equals(RelatedTxnType.EXCHANGE.name())) {
            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(transactionRecord.getRelatedTxnId());
            transactionMapping.setTravelerId(exchange.getTravelerId());
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(exchange.getTravelerId());
            Exchange inProgressExchange = exchangeRepositoryService.getLatestExchangeInProgress(traveler.getUserId());

            if (inProgressExchange != null && !exchange.getId().equals(inProgressExchange.getId())) {
                logger.error("{}: 해당 유저가 진행중인 다른 환전건이 있습니다. exchange={}, inProgressExchange={}", method, exchange, inProgressExchange);
                throw new CashmallowException("해당 유저가 진행중인 다른 환전건이 있습니다.");
            }
            couponUserId = exchange.getCouponUserId();
            userId = traveler.getUserId();
            fromCountryCode = exchange.getFromCd();
        }

        logger.info("transactionRecord:{}, transactionMapping:{}, paygateRecdIds:{}, inputAccountNo:{}, inputName:{}, manual:{}",
                transactionRecord, transactionMapping, paygateRecdIds, inputAccountNo, inputName, manual);

        TransactionMapping txnMapping = companyService.matchTransactionMappingForManual(transactionRecord, transactionMapping,
                paygateRecdIds, bankAccountId, inputAccountNo, inputName);

        RelatedTxnType relatedTxnType = RelatedTxnType.valueOf(transactionRecord.getRelatedTxnType());
        long relatedTxnId = transactionRecord.getRelatedTxnId();

        Map<String, String> params = new HashMap<>();
        params.put("relatedTxnType", relatedTxnType.name());
        params.put("relatedTxnId", String.valueOf(relatedTxnId));

        if (transactionRecord.getRelatedTxnType().equals(RelatedTxnType.REMITTANCE.name())) {
            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(transactionRecord.getRelatedTxnId());

            mallowlinkRemittanceService.requestRemittance(remittance);
        }

        if (isUseCoupon(couponUserId)) {

            CouponIssueUser couponIssueUser = couponUserService.getCouponUserByIdAndStatus(couponUserId, AvailableStatus.USED);
            if (couponIssueUser != null) {
                CountryCode countryCode = CountryCode.of(fromCountryCode);
                // 사용한 쿠폰이 초대 쿠폰인지 확인
                Coupon coupon = couponService.getCouponById(couponIssueUser.getCouponId());
                // 신규 가입자가 사용하는 쿠폰이 초대 쿠폰이 맞으면 초대완료 쿠폰 발급을 위해 진행
                if (coupon != null && coupon.getCouponCode().startsWith(SystemCouponType.thankYouMyFriend.getCode())) {
                    // 초대완료 쿠폰 발급
                    couponMobileService.issueMobileCouponsV3(SystemCouponType.thankYouToo.getCode(), countryCode.name(), countryCode.getCurrency(), userId, countryCode.getZoneId(), null, couponUserId);
                }
            }
        }

        companyService.sendMappingResultNotification(relatedTxnType, relatedTxnId, true, paygateRecdIds);

        return txnMapping;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void replaceDbsTransaction(MatchPaygateRecordRequest matchRequest) throws CashmallowException {
        PaygateRecord tempRecord = companyService.getPaygateRecord(matchRequest.getCmTempTid());

        tempRecord.setId(matchRequest.getRealTid());
        // 최초 record에서 금액을 잔액에 반영되어 있어서 최신 잔액으로만 업데이트
        // 여기서 잔액에 amount를 한번 더 더하면 중복반영이 되어서 안함.
        BigDecimal balance = companyService.getPaygateRecordLastBalance(dbsProperties.accountId(), tempRecord.getIso4217());
        tempRecord.setBalance(balance);
        companyService.insertPaygateRecord(tempRecord);
        PaygateRecord realRecord = companyService.getPaygateRecord(matchRequest.getRealTid());

        // transactionMapping Tid 변경
        List<TransactionMapping> transactionMappingByPaygateRecordId = companyService.getTransactionMappingByPaygateRecordId(matchRequest.getCmTempTid());
        if (transactionMappingByPaygateRecordId.size() != 1) {
            logger.error(INTERNAL_SERVER_ERROR + "존재하지 않거나 맵핑 되지 않은 임시 Paygate 트랜잭션 tid={}", matchRequest.getCmTempTid());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        TransactionMapping transactionMapping = transactionMappingByPaygateRecordId.get(0);
        companyService.changeTransactionMappingId(transactionMapping, realRecord.getId());

        // real paygate record에 내역 기록
        realRecord.setDescription("교체 완료 old tid=" + matchRequest.getCmTempTid());
        companyService.updatePaygateRecord(realRecord);

        PaygateRecordDeleteRequest request = new PaygateRecordDeleteRequest();
        request.setDescription(realRecord.getDescription());
        request.setExchangeId(matchRequest.getCmTempTid());
        // 임시 record 제거
        companyService.deletePaygateRecord(request);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public PaygateRecord addDbsNotificationRecord(DbsDepositRecordRequest dbsDepositRecordRequest) throws CashmallowException {

        PaygateRecord paygateRecord = new PaygateRecord();

        // 홍콩 시간으로 처리
        LocalDateTime utcTime = dbsDepositRecordRequest.getExecutedDate().minusHours(8);
        paygateRecord.setId(dbsDepositRecordRequest.getTransactionId());
        paygateRecord.setCountry(CountryCode.HK.getCode());
        paygateRecord.setBankAccountId(dbsProperties.accountId());
        paygateRecord.setIso4217(dbsDepositRecordRequest.getCurrency());
        paygateRecord.setDepWdrType("DEPOSIT");
        paygateRecord.setAmount(dbsDepositRecordRequest.getAmount());
        paygateRecord.setDescription("by DBS Notification");
        paygateRecord.setSenderName(dbsDepositRecordRequest.getSenderName());
        paygateRecord.setSenderAccountNo(dbsDepositRecordRequest.getSenderAccountNo());
        paygateRecord.setExecutedDate(Timestamp.valueOf(utcTime));
        paygateRecord.setDepositType(dbsDepositRecordRequest.getDepositType());
        paygateRecord.setWorkStatus(PaygateRecord.WorkStatus.READY);

        // 마지막 잔액 들고와서 입금금액 추가
        BigDecimal balance = companyService.getPaygateRecordLastBalance(dbsProperties.accountId(), dbsDepositRecordRequest.getCurrency())
                .add(dbsDepositRecordRequest.getAmount());
        paygateRecord.setBalance(balance);

        try {
            companyService.insertPaygateRecord(paygateRecord);
        } catch (DuplicateKeyException e) {
            throw new CashmallowException("DUPLICATE_KEY_ERROR");
        }
        return paygateRecord;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void insertDeposit(InsertDepositRequest request) throws CashmallowException {
        // 국가와 은행 코드로 계좌 찾기
        final String countryCode = request.country().getCode();
        final String currency = request.currency();
        BankAccount bankAccount = getBankAccount(countryCode, request.bankCode(), request.bankAccountNumber());
        Long bankAccountId = Long.valueOf(bankAccount.getId());

        // 마지막 잔액 들고와서 입금금액 추가
        BigDecimal balance = companyService.getPaygateRecordLastBalance(bankAccountId, currency).add(request.deposit().amount());

        // insert deposit
        PaygateRecord paygateRecord = PaygateRecord.of(countryCode, currency, bankAccountId, request.deposit(), balance);
        companyService.insertPaygateRecord(paygateRecord);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void insertDepositBulk(InsertDepositBulkRequest request) throws CashmallowException {
        // 국가와 은행 코드로 계좌 찾기
        final String countryCode = request.country().getCode();
        final String currency = request.currency();
        BankAccount bankAccount = getBankAccount(countryCode, request.bankCode(), request.bankAccountNumber());
        Long bankAccountId = Long.valueOf(bankAccount.getId());

        // 마지막 잔액 들고와서 입금금액 추가
        BigDecimal balance = companyService.getPaygateRecordLastBalance(bankAccountId, currency);

        List<PaygateRecord> records = new ArrayList<>();
        for (var deposit : request.deposits()) {
            balance = balance.add(deposit.amount());
            records.add(PaygateRecord.of(countryCode, currency, bankAccountId, deposit, balance));
        }
        companyService.insertPaygateRecordBulk(records);
    }

    public BankAccount getBankAccount(String countryCode, String bankCode, String bankAccountNumber) throws CashmallowException {
        List<BankAccount> bankAccounts = companyMapper.findBankAccountByCountryCodeAndBankCode(countryCode, bankCode);
        if (bankAccounts.isEmpty()) {
            throw new CashmallowException("BANK_ACCOUNT_NOT_FOUND");
        }

        // 요청에 계좌 번호가 있으면 찾아서 리턴, 없으면 최상위 리턴
        BankAccount bankAccount;
        if (StringUtils.isNotBlank(bankAccountNumber)) {
            bankAccount = bankAccounts.stream()
                    .filter(bank -> CustomStringUtil.equalOnlyNumbers(bank.getBankAccountNo(), bankAccountNumber))
                    .findAny()
                    .orElseThrow(() -> new CashmallowException("BANK_ACCOUNT_NOT_FOUND"));
        } else {
            bankAccounts.sort(Comparator.comparingInt(BankAccount::getSortOrder));
            bankAccount = bankAccounts.get(0);
        }
        return bankAccount;
    }
}
