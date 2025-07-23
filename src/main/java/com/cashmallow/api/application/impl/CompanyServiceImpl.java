package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.CurrencyService;
import com.cashmallow.api.domain.model.aml.AmlAccountBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranSendReceipt;
import com.cashmallow.api.domain.model.aml.AmlProdBase;
import com.cashmallow.api.domain.model.company.*;
import com.cashmallow.api.domain.model.company.TransactionRecord.Description;
import com.cashmallow.api.domain.model.company.TransactionRecord.FundingStatus;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.Exchange.ExStatus;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.Remittance.RemittanceStatusCode;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.remittance.RemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.remittance.RemittanceTraverlerInfo;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.aml.OctaAMLKYCService;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLKYCRequest;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.admin.dto.*;
import com.cashmallow.api.interfaces.aml.octa.OctaAmlService;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.application.impl.RemittanceServiceImpl.isUseCoupon;
import static com.cashmallow.api.domain.model.country.enums.CountryCode.HK;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.common.CommonUtil.*;

@Service
public class CompanyServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private PaygateMapper paygateRecordMapper;

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private OctaAmlService octaAmlService;

    @Autowired
    private MallowlinkRemittanceServiceImpl mallowlinkRemittanceService;

    @Autowired
    private DbsProperties dbsProperties;

    @Value(value = "${paygate.bankAccountId}")
    private Integer paygateAccountId;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private OctaAMLKYCService octaAMLKYCService;

    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponServiceV2 couponService;
    @Autowired
    private CouponMobileServiceV2 couponMobileService;

    /**
     * 캐시멜로 계좌 정보 조회
     *
     * @param bankAccountId
     * @return
     */
    public BankAccount getBankAccountByBankAccountId(Integer bankAccountId) {
        return companyMapper.getBankAccountByBankAccountId(bankAccountId);
    }

    public PaygateRecord getPaygateRecord(String paygateRecordId) {
        return paygateRecordMapper.getPaygateRecordById(paygateRecordId);
    }

    public DbsRemittance getDbsRemittance(String dbsRemittanceId) {
        return paygateRecordMapper.getDbsRemittance(dbsRemittanceId);
    }

    public List<PaygateRecord> getPaygateRecords(Map<String, Object> params) {
        return paygateRecordMapper.getPaygateRecordList(params);
    }

    public List<PaygateRecord> getPaygateRecordForRepayment() {
        return paygateRecordMapper.getPaygateRecordForRepayment(dbsProperties.accountId());
    }

    public List<PaygateRecord> getPaygateRecordListByTransactionRecordId(Long transactionRecordId) {

        List<TransactionMapping> transactionMappingList = getTransactionMappingByTransactionRecordId(transactionRecordId);

        if (transactionMappingList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("list", transactionMappingList);

        return paygateRecordMapper.getPaygateRecordListByIdList(params);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO getAdminDbsRemittance(AdminDbsRemittanceAskVO pvo) {
        int size = pvo.getSize() != null ? pvo.getSize() : Const.DEF_PAGE_SIZE;
        int page = (pvo.getStartRow() + size) / size - 1;

        pvo.setPage(page);
        pvo.setSize(size);

        SearchResultVO searchResult = new SearchResultVO(page, size, pvo.getSort());

        List<Object> vos = paygateRecordMapper.getAdminDbsRemittanceList(pvo);

        int totalCount = paygateRecordMapper.countAdminDbsRemittanceList(pvo);

        searchResult.setResult(vos, totalCount, page);

        return searchResult;
    }

    public List<TransactionRecord> getTransactionRecordsList(Map<String, String> params) {
        return transactionMapper.getTransactionRecordsList(params);
    }

    public TransactionRecord getTransactionRecord(Map<String, Object> params) {
        return transactionMapper.getTransactionRecord(params);
    }

    public List<TransactionRecord> getTransactionRecordsListByPaygateRecordId(String paygateRecordId) {

        List<TransactionMapping> transactionMappingList = getTransactionMappingByPaygateRecordId(paygateRecordId);

        if (transactionMappingList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("list", transactionMappingList);

        return transactionMapper.getTransactionRecordListByIdList(params);
    }

    public List<TransactionRecord> getTransactionRecordForPaygateRecord(String paygateRecordId) {
        return transactionMapper.getTransactionRecordForPaygateRecord(paygateRecordId);
    }

    public List<TransactionMapping> getTransactionMappingByPaygateRecordId(String paygateRecordId) {
        return transactionMapper.getTransactionMappingListByPaygateRecordId(paygateRecordId);
    }

    public List<TransactionMapping> getTransactionMappingByTransactionRecordId(Long transactionRecordId) {
        return transactionMapper.getTransactionMappingListByTransactionRecordId(transactionRecordId);
    }

    // edited by Aelx 20170810 기능: 28.4.2 환전신청시 회사 계좌 조회
    public List<Map<String, Object>> getBankAccountList(String country, String useYn) {
        String method = "getBankAccountList()";

        HashMap<String, Object> params = new HashMap<>();
        params.put("country", country);
        params.put("useYn", useYn);
        params.put("sort", "sort_order");
        params.put("startRow", 0);
        params.put("size", 100);
        List<BankAccount> bankAccounts = companyMapper.findBankAccount(params);

        // todo 강제로 1개만 보냄 실제 은행은 toCd에 의해서 결정되게 환전신청, 송금신청에서 결정함.
        if (country.equals(HK.getCode())) {
            bankAccounts = bankAccounts.stream().filter(b -> b.getId().equals(paygateAccountId)).collect(Collectors.toList());
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Map<String, Object>> result = mapper.convertValue(bankAccounts,
                new TypeReference<List<Map<String, Object>>>() {
                });

        // 나중에 다 삭제할 것.
        if (HK.getCode().equals(country)) {
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> map = result.get(i);

                map.put("depositable_bank_name", "[\"恆生銀行(Hang Seng)\", \"匯豐銀行(HSBC)\", \"中國銀行(BOC)\"]");
                result.set(i, map);
            }
        }

        logger.info("{}: country={}, result.size()={}", method, country, result.size());

        return result;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO findBankAccountList(BankAccountAskVO pvo) {
        String method = "getBankAccountList()";

        List<Map<String, String>> bankAccountMapList;
        List<Object> result = new ArrayList<>();

        String country = pvo.getCountry();

        HashMap<String, Object> params = new HashMap<>();
        params.put("country", country);
        params.put("useYn", pvo.getUseYn());
        params.put("startRow", pvo.getStartRow());
        params.put("size", pvo.getSize());
        params.put("sort", pvo.getSort());

        int count = companyMapper.countBankAccount(params);
        List<BankAccount> bankAccounts = companyMapper.findBankAccount(params);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        bankAccountMapList = mapper.convertValue(bankAccounts, new TypeReference<List<Map<String, String>>>() {
        });

        for (int i = 0; i < bankAccountMapList.size(); i++) {
            Map<String, String> map = bankAccountMapList.get(i);

            if (HK.getCode().equals(country)) {
                if (map.get("id").equals("1")) {
                    map.put("depositable_bank_name", "[\"恆生銀行(Hang Seng)\", \"匯豐銀行(HSBC)\", \"中國銀行(BOC)\"]");
                } else if (map.get("id").equals("12")) {
                    map.put("depositable_bank_name", "[\"中國銀行(BOC)\"]");
                }
            }
            result.add(map);
        }

        int page = (pvo.getStartRow() + pvo.getSize()) / pvo.getSize() - 1;
        SearchResultVO searchResult = new SearchResultVO(page, pvo.getSize(), pvo.getSort());
        searchResult.setResult(result, count, page);

        logger.info("{}: country={}, result.size()={}", method, country, bankAccountMapList.size());

        return searchResult;

    }

    // -------------------------------------------------------------------------------
    // 60. 통계(관리자 용)
    // -------------------------------------------------------------------------------

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public BigDecimal getPaygateRecordLastBalance(Long bankAccountId, String currency) {
        return paygateRecordMapper.getPaygateRecordLastBalance(bankAccountId.intValue(), currency);
    }

    @Transactional
    public int insertRepaymentHistory(RepaymentHistory repaymentHistory) throws CashmallowException {
        return companyMapper.insertRepaymentHistory(repaymentHistory);
    }

    @Transactional
    public int insertDbsRemittance(DbsRemittance dbsRemittance) throws CashmallowException {
        return paygateRecordMapper.insertDbsRemittance(dbsRemittance);
    }

    @Transactional
    public int updateDbsRemittance(DbsRemittance dbsRemittance) throws CashmallowException {
        return paygateRecordMapper.updateDbsRemittance(dbsRemittance);
    }

    // paygateRecord 데이터 생성
    @Transactional
    public int insertPaygateRecord(PaygateRecord paygateRecord) throws CashmallowException {
        return paygateRecordMapper.insertPaygateRecord(paygateRecord);
    }

    @Transactional
    public int insertPaygateRecordBulk(List<PaygateRecord> paygateRecord) throws CashmallowException {
        return paygateRecordMapper.insertPaygateRecordBulk(paygateRecord);
    }

    @Transactional
    public int updatePaygateRecord(PaygateRecord paygateRecord) throws CashmallowException {
        return paygateRecordMapper.updatePaygateRecord(paygateRecord);
    }

    @Transactional
    public void updatePaygateRecordComplteStatus(TransactionRecord txnRec) throws CashmallowException {
        List<PaygateRecord> paygateRecords = getPaygateRecordListByTransactionRecordId(txnRec.getId());
        for (PaygateRecord paygateRecord : paygateRecords) {
            paygateRecord.setWorkStatus(PaygateRecord.WorkStatus.COMPLETED);
        }
    }

    @Transactional
    public int deletePaygateRecord(PaygateRecordDeleteRequest record) {
        return paygateRecordMapper.deletePaygateRecord(record);
    }

    @Transactional
    public List<PaygateRecord> findTempPaygateRecordsByMapped() {
        return paygateRecordMapper.findTempPaygateRecordsByMapped();
    }

    @Transactional
    public List<DbsDto> findTempDbsRecordsByMapped() {
        return paygateRecordMapper.findTempDbsRecordsByMapped(dbsProperties.accountId());
    }

    // Transaction 데이터 생성
    @Transactional
    public int insertTrasactionRecord(TransactionRecord transactionRecord) throws CashmallowException {
        return transactionMapper.insertTransactionRecord(transactionRecord);
    }

    // Transaction 데이터 수정
    @Transactional
    public int updateTrasactionRecord(TransactionRecord transactionRecord) throws CashmallowException {
        return transactionMapper.updateTransactionRecord(transactionRecord);
    }

    @Transactional
    public int deleteTrasactionRecord(TransactionRecord transactionRecord) {
        return transactionMapper.deleteTransactionRecord(transactionRecord);
    }

    @Transactional
    public int deleteTransactionMapping(Long transactionRecId) {
        return transactionMapper.deleteTransactionMapping(transactionRecId);
    }

    @Transactional
    public int insertRollbackMappingHistory(RollbackMappingHistory rollbackMappingHistory) {
        return transactionMapper.insertRollbackMappingHistory(rollbackMappingHistory);
    }

    // Transaction 데이터 생성
    @Transactional(rollbackFor = CashmallowException.class)
    public int insertTransactionMapping(TransactionMapping transactionMapping) throws CashmallowException {

        int affectedRow = transactionMapper.insertTransactionMapping(transactionMapping);

        if (affectedRow < 1) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        logger.info("거래매핑 결과: {}", affectedRow);

        return affectedRow;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void changeTransactionMappingId(TransactionMapping transactionMapping, String newPaygateTid) throws CashmallowException {
        transactionMapping.setNewPaygateRecId(newPaygateTid);

        int affectedRow = transactionMapper.updateTransactionMapping(transactionMapping);
        if (affectedRow != 1) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    // transaction 데이터를 생성하면서 연관된 통장내역과 mapping을 해줌.
    @Transactional(rollbackFor = CashmallowException.class)
    public TransactionMapping matchTransactionMappingForManual(TransactionRecord transactionRecord, TransactionMapping transactionMapping,
                                                               List<String> paygateRecdIds, Long bankAccountId, String inputAccountNo, String inputName)
            throws CashmallowException {
        String method = "matchTransactionMappingForManual()";

        logger.info("transactionRecord:{}, transactionMapping:{}, paygateRecdIds:{}, inputAccountNo:{}, inputName:{}",
                transactionRecord, transactionMapping, paygateRecdIds, inputAccountNo, inputName);

        int duplicateRow = transactionMapper.countDuplicateTransactionRecord(transactionRecord);

        if (duplicateRow > 0) {
            logger.info("{}: Duplicate transactionRecord. RelatedTxnType={}, RelatedTxnId={}", method,
                    transactionRecord.getRelatedTxnType(), transactionRecord.getRelatedTxnId());
            throw new CashmallowException("이미 매핑된 데이터입니다.");
        }

        Map<String, Object> params = new HashMap<>();

        List<PaygateRecord> paygateRecords = new ArrayList<>();

        for (String paygateRecId : paygateRecdIds) {
            params.put("id", paygateRecId);
            params.put("bankAccountId", bankAccountId.toString());
            paygateRecords.addAll(getPaygateRecords(params));
        }

        int needRowCount = 1;

        BigDecimal paygateRecSumAmount = new BigDecimal("0.00");

        for (PaygateRecord ba : paygateRecords) {
            paygateRecSumAmount = paygateRecSumAmount.add(ba.getAmount());
        }

        // 신청 100, 입금 110
        // com = -10
        BigDecimal compareAmount = transactionRecord.getAmount().subtract(paygateRecSumAmount);

        // 통장에 입금된 금액들의 합계가 사용자가 신청한 금액보다 클 경우
        if (compareAmount.compareTo(BigDecimal.ZERO) < 0) {
            needRowCount = 2;
        } else if (compareAmount.compareTo(BigDecimal.ZERO) >= 1) {
            // 통장에 입금된 금액들의 합계가 신청한 금액보다 작을 경우
            logger.error("{}: The deposit amount is less than the requested amount. DepositAmountSum={}, transactionAmount={}",
                    method, paygateRecSumAmount, transactionRecord.getAmount());

            throw new CashmallowException("통장에 입금된 내역이 신청금액보다 작을경우 매핑할 수 없습니다.");
        }

        Long upperId = Long.valueOf("0");
        for (int i = 0; i < needRowCount; i++) {
            transactionRecord.setId(null);
            transactionRecord.setFundingStatus(FundingStatus.CONFIRM.name());

            if (i == 1) {
                transactionRecord.setRootId(upperId);
                transactionRecord.setUpperId(upperId);
                transactionRecord.setAmount(compareAmount.abs());
                transactionRecord.setDescription(Description.OTHER_REVENUE.name());
                transactionRecord.setFundingStatus(FundingStatus.NA.name());
            }

            int insertRow = insertTrasactionRecord(transactionRecord);

            if (insertRow < 1) {
                logger.error("{}: Failed to insert TransactionRocord. RelatedType={}, RelatedId={}", method,
                        transactionRecord.getRelatedTxnType(), transactionRecord.getRelatedTxnId());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            upperId = transactionRecord.getId();
        }

        if (transactionRecord.getRootId() != null && !transactionRecord.getRootId().equals(0L)) {
            transactionMapping.setTransactionRecId(transactionRecord.getRootId());
        } else {
            transactionMapping.setTransactionRecId(transactionRecord.getId());
        }

        int affectedRow = 0;

        for (String paygateRecId : paygateRecdIds) {
            transactionMapping.setPaygateRecId(paygateRecId);

            affectedRow = insertTransactionMapping(transactionMapping);

            if (affectedRow < 1) {
                logger.error("{}: Failed to insert TransactionMapping. txnId={}, paygateRecId={}", method,
                        transactionMapping.getTransactionRecId(), transactionMapping.getPaygateRecId());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        int updatedRow = 0;
        Long headerTransactionId = transactionRecord.getRootId() == null ? transactionRecord.getId() : transactionRecord.getRootId();
        TransactionRecord headerRecord = transactionMapper.getTransactionRecordByTransactionRecordId(headerTransactionId);

        if (headerRecord.getRelatedTxnType().equals(RelatedTxnType.EXCHANGE.name())) {
            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(headerRecord.getRelatedTxnId());
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(exchange.getTravelerId());

            if (!traveler.getAccountOk().equals("Y") || !traveler.getCertificationOk().equals("Y")) {
                logger.info("{}: Traveler accountOk or CertificationOk is No. Can't mapping. Exchange_id={}, accountOk={}, certificationOk={}",
                        method, exchange.getId(), traveler.getAccountOk(), traveler.getCertificationOk());
                throw new CashmallowException("해당 고객의 신분인증 또는 통장인증이 No입니다.");
            }

            exchange.setTrAccountNo(traveler.getAccountNo());
            exchange.setTrAccountName(traveler.getAccountName());
            exchange.setTrBankName(traveler.getBankName());
            exchange.setTrDepositDate(Timestamp.valueOf(LocalDateTime.now()));
            exchange.setTrFromAmt(exchange.getFromAmt());

            if (!exchange.getExStatus().equals(ExStatus.OP.name()) && !exchange.getExStatus().equals(ExStatus.CC.name())) {
                logger.info("{}: Exchange status is CF.Can't mapping. Exchange_id={}", method, exchange.getId());
                throw new CashmallowException("이미 매핑된 데이터입니다.");
            }

            String travelerAccount = exchange.getTrAccountNo();
            String travelerName = convertSmallKanaToLarge(textToNormalize(exchange.getTrAccountName().replace(" ", "")));
            inputName = convertSmallKanaToLarge(textToNormalize((inputName.replace(" ", ""))));
            boolean isPassMapping = false;
            if (StringUtils.isBlank(inputAccountNo)) {
                // 입력한 이름과 Traveler AccountName이 같으면 통과
                isPassMapping = isMatchName(inputName, travelerName);
            } else {
                // 계좌번호 6자리 매치가 안되면 false
                isPassMapping = isMatchAccountNo(inputAccountNo, travelerAccount);
            }

            if (!isPassMapping) {
                logger.error("{}: Not Match InputInfo. Exchange_id={}, tr_account={}, input_account={}, tr_name={}, input_name={}",
                        method, exchange.getId(), travelerAccount, inputAccountNo, travelerName, inputName);
                String errorMessage = "계좌번호 불일치로 인한 매핑실패";
                if (StringUtils.isEmpty(inputAccountNo)) {
                    errorMessage = "계좌 이름 불일치로 인한 매핑실패";
                }
                throw new CashmallowException(errorMessage);
            }

            exchange.setExStatus(ExStatus.CF.toString());
            exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
            exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            updatedRow = exchangeRepositoryService.updateExchange(exchange);

            walletRepositoryService.addTravelerWallet(exchange.getTravelerId(), exchange.getFromCd(), exchange.getToCd(),
                    exchange.getToAmt(), headerRecord.getCreator(), exchange.getId());

        } else if (headerRecord.getRelatedTxnType().equals(RelatedTxnType.REMITTANCE.name())) {

            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(headerRecord.getRelatedTxnId());
            RemittanceTravelerSnapshot remitTravelerSnapshot = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());

            if (!traveler.getAccountOk().equals("Y") || !traveler.getCertificationOk().equals("Y")) {
                logger.info("{}: Traveler accountOk or CertificationOk is No. Can't mapping. Remit_id={}, accountOk={}, certificationOk={}",
                        method, remittance.getId(), traveler.getAccountOk(), traveler.getCertificationOk());
                throw new CashmallowException("해당 고객의 신분인증 또는 통장인증이 No입니다.");
            }

            remitTravelerSnapshot.setAccountNo(traveler.getAccountNo());
            remitTravelerSnapshot.setAccountName(traveler.getAccountName());
            remitTravelerSnapshot.setBankName(traveler.getBankName());
            remitTravelerSnapshot.setIdentificationNumber(traveler.getIdentificationNumber());

            if (!remittance.getRemitStatus().equals(RemittanceStatusCode.OP) && !remittance.getRemitStatus().equals(RemittanceStatusCode.CC)) {
                logger.info("{}: 송금의 상태가 매핑할수 없는 상태입니다. Remit_status={}, Remit_id={}", method, remittance.getRemitStatus(), remittance.getId());
                throw new CashmallowException("매핑할수 없는 상태입니다.");
            }

            List<Map<String, Object>> resultMap = octaAmlService.validateRemittanceAmlList(remittance);

            if (resultMap.size() > 0 && remittance.getIsConfirmedReceiverAml().equals("N")) {
                logger.info("{} : 해당 수취인의 AML리스트가 존재합니다. 확인 바랍니다. listCount={}", method, resultMap.size());
                throw new CashmallowException("해당 수취인의 AML리스트가 존재합니다. 확인 바랍니다.");
            }

            String travelerAccount = remitTravelerSnapshot.getAccountNo();
            String travelerName = convertSmallKanaToLarge(textToNormalize(remitTravelerSnapshot.getAccountName().replace(" ", "")));
            inputName = convertSmallKanaToLarge(textToNormalize(inputName.replace(" ", "")));
            boolean isPassMapping = false;
            if (StringUtils.isBlank(inputAccountNo)) {
                // 입력한 이름과 Traveler AccountName이 같으면 통과
                isPassMapping = isMatchName(inputName, travelerName);
            } else {
                // 계좌번호 6자리 매치가 안되면 false
                isPassMapping = isMatchAccountNo(inputAccountNo, travelerAccount);
            }

            if (!isPassMapping) {
                logger.error("{}: Not Match InputInfo. Remit_id={}, tr_account={}, input_account={}, tr_name={}, input_name={}",
                        method, remittance.getId(), travelerAccount, inputAccountNo, travelerName, inputName);
                String errorMessage = "계좌번호 불일치로 인한 매핑실패";
                if (StringUtils.isEmpty(inputAccountNo)) {
                    errorMessage = "계좌 이름 불일치로 인한 매핑실패";
                }
                throw new CashmallowException(errorMessage);
            }

            remittance.setRemitStatus(RemittanceStatusCode.DP);
            remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            updatedRow = remittanceRepositoryService.updateRemittance(remittance);
            remittanceRepositoryService.updateRemitTravelerSnapshot(remitTravelerSnapshot);
            remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.DP);
        }

        if (updatedRow <= 0) {
            logger.error("{}: Failed to update {}. id={} ", method, headerRecord.getRelatedTxnType(),
                    headerRecord.getRelatedTxnId());
            throw new CashmallowException("{} 업데이트중 오류가 발생했습니다.", headerRecord.getRelatedTxnType());
        }

        updatePaygateRecordComplteStatus(transactionRecord);

        return transactionMapping;
    }

    private boolean isMatchName(String inputName, String travelerName) {
        String[] inputNameArray = inputName.toUpperCase().replace(" ", "").split("\\s+");
        String[] travelerNameArray = travelerName.toUpperCase().replace(" ", "").split("\\s+");
        return Arrays.equals(inputNameArray, travelerNameArray);
    }

    private boolean isMatchAccountNo(String inputAccountNo, String travelerAccount) {
        inputAccountNo = inputAccountNo.replace(" ", "");
        travelerAccount = travelerAccount.replace(" ", "");
        int matchNo = 0;
        for (int i = 0; i < inputAccountNo.length(); i++) {
            if (i == travelerAccount.length()) {
                break;
            }

            // *는 검사 제외
            if (inputAccountNo.charAt(i) == '*') {
                continue;
            }

            // 입력한 숫자중 하나라도 틀리면
            if (inputAccountNo.charAt(i) == travelerAccount.charAt(i)) {
                matchNo++;
            } else {
                matchNo = 0;
                break;
            }
        }

        return matchNo >= 6;
    }

    // DBS에서 입금된 내용중 매핑 가능한 건들은 매핑한다.
    private void matchTransactionMappingForAuto(TransactionRecord transactionRecord,
                                                TransactionMapping transactionMapping, PaygateRecord paygateRecord)
            throws CashmallowException {
        String method = "matchTransactionMappingForAuto()";

        int duplicateRow = transactionMapper.countDuplicateTransactionRecord(transactionRecord);

        if (duplicateRow > 0) {
            logger.info("{}: Duplicate transactionRecord. RelatedTxnType={}, RelatedTxnId={}", method,
                    transactionRecord.getRelatedTxnType(), transactionRecord.getRelatedTxnId());
            throw new CashmallowException("이미 매핑된 데이터입니다.");
        }

        transactionRecord.setId(null);
        transactionRecord.setFundingStatus(FundingStatus.CONFIRM.name());

        int insertRow = insertTrasactionRecord(transactionRecord);

        if (insertRow < 1) {
            logger.error("{}: Failed to insert TransactionRocord. RelatedType={}, RelatedId={}", method,
                    transactionRecord.getRelatedTxnType(), transactionRecord.getRelatedTxnId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        transactionMapping.setTransactionRecId(transactionRecord.getId());

        int affectedRow = 0;

        transactionMapping.setPaygateRecId(paygateRecord.getId());
        affectedRow = insertTransactionMapping(transactionMapping);

        if (affectedRow < 1) {
            logger.error("{}: Failed to insert TransactionMapping. txnId={}, paygateRecId={}", method,
                    transactionMapping.getTransactionRecId(), transactionMapping.getPaygateRecId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int updatedRow = 0;

        if (transactionRecord.getRelatedTxnType().equals(RelatedTxnType.EXCHANGE.name())) {
            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(transactionRecord.getRelatedTxnId());
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(exchange.getTravelerId());

            if (!traveler.getAccountOk().equals("Y") || !traveler.getCertificationOk().equals("Y")) {
                logger.info("{}: Traveler accountOk or CertificationOk is No. Can't mapping. Exchange_id={}, accountOk={}, certificationOk={}",
                        method, exchange.getId(), traveler.getAccountOk(), traveler.getCertificationOk());
                throw new CashmallowException("해당 고객의 신분인증 또는 통장인증이 No입니다.");
            }

            exchange.setTrAccountNo(traveler.getAccountNo());
            exchange.setTrAccountName(traveler.getAccountName());
            exchange.setTrBankName(traveler.getBankName());
            exchange.setTrDepositDate(Timestamp.valueOf(LocalDateTime.now()));
            exchange.setTrFromAmt(exchange.getFromAmt());

            exchange.setExStatus(ExStatus.CF.toString());
            exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
            exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            updatedRow = exchangeRepositoryService.updateExchange(exchange);

            walletRepositoryService.addTravelerWallet(exchange.getTravelerId(), exchange.getFromCd(), exchange.getToCd(),
                    exchange.getToAmt(), transactionRecord.getCreator(), exchange.getId());

        } else if (transactionRecord.getRelatedTxnType().equals(RelatedTxnType.REMITTANCE.name())) {

            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(transactionRecord.getRelatedTxnId());
            RemittanceTravelerSnapshot remitTravelerSnapshot = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());

            if (!traveler.getAccountOk().equals("Y") || !traveler.getCertificationOk().equals("Y")) {
                logger.info("{}: Traveler accountOk or CertificationOk is No. Can't mapping. Remit_id={}, accountOk={}, certificationOk={}",
                        method, remittance.getId(), traveler.getAccountOk(), traveler.getCertificationOk());
                throw new CashmallowException("해당 고객의 신분인증 또는 통장인증이 No입니다.");
            }

            remitTravelerSnapshot.setAccountNo(traveler.getAccountNo());
            remitTravelerSnapshot.setAccountName(traveler.getAccountName());
            remitTravelerSnapshot.setBankName(traveler.getBankName());
            remitTravelerSnapshot.setIdentificationNumber(traveler.getIdentificationNumber());

            if (!remittance.getRemitStatus().equals(RemittanceStatusCode.OP) && !remittance.getRemitStatus().equals(RemittanceStatusCode.CC)) {
                logger.info("{}: 송금의 상태가 매핑할수 없는 상태입니다. Remit_status={}, Remit_id={}", method, remittance.getRemitStatus(), remittance.getId());
                throw new CashmallowException("매핑할수 없는 상태입니다.");
            }

            try {
                octaAmlService.validateRemittanceAmlList(remittance);
            } catch (Exception e) {
                logger.info("{} : 해당 수취인의 AML리스트가 존재합니다. 확인 바랍니다.", method);
                String message = "수취인 AML 확인 필요, 송금 거래번호:" + remittance.getId() + " 유저ID:" + traveler.getUserId() +
                        " 수취인이름:" + remittance.getReceiverFirstName() + " " + remittance.getReceiverLastName();
                alarmService.aAlert("송금 AML", message, userRepositoryService.getUserByUserId(traveler));
                throw new CashmallowException("해당 수취인의 AML리스트가 존재합니다. 확인 바랍니다.", e.getMessage());
            }

            remittance.setRemitStatus(RemittanceStatusCode.DP);
            remittance.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            updatedRow = remittanceRepositoryService.updateRemittance(remittance);
            remittanceRepositoryService.updateRemitTravelerSnapshot(remitTravelerSnapshot);
            remittanceRepositoryService.insertRemitStatus(remittance.getId(), RemittanceStatusCode.DP);
        }

        // if (updatedRow <= 0) {
        //     logger.error("{}: Failed to update {}. id={} ", method, headerRecord.getRelatedTxnType(),
        //             headerRecord.getRelatedTxnId());
        //     throw new CashmallowException("{} 업데이트중 오류가 발생했습니다.", headerRecord.getRelatedTxnType());
        // }

        updatePaygateRecordComplteStatus(transactionRecord);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendMappingResultNotification(RelatedTxnType txnType, long txnId, boolean isManual) {
        sendMappingResultNotification(txnType, txnId, isManual, null);
    }

    /**
     * Send notifications : Slack, FCM, Email
     *
     * @param countries
     * @param traveler
     * @param csv
     * @param exchange
     * @param isManual
     * @param paygateRecdIds
     * @throws CashmallowException
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendMappingResultNotification(RelatedTxnType txnType, long txnId, boolean isManual, List<String> paygateRecdIds) {

        String method = "sendMappingResultNotification()";

        Traveler traveler = null;
        User user = null;

        // Send email parameters
        Country fromCountry = null;
        Country toCountry = null;

        StringBuilder msg = new StringBuilder("[ADMIN]");
        msg.append(" 거래유형:" + txnType.name());
        msg.append(", 거래번호:" + txnId);

        if (txnType.equals(RelatedTxnType.EXCHANGE)) {
            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(txnId);

            fromCountry = countryService.getCountry(exchange.getFromCd());
            toCountry = countryService.getCountry(exchange.getToCd());

            traveler = travelerRepositoryService.getTravelerByTravelerId(exchange.getTravelerId());
            user = userRepositoryService.getUserByUserId(traveler.getUserId());

            msg.append("\n");
            msg.append("화폐:" + fromCountry.getIso4217());
            msg.append(", 매핑 금액:" + exchange.getTrFromAmt());
            msg.append("\n");
            msg.append("입금은행:" + exchange.getTrBankName());
            msg.append(", 유저ID:" + traveler.getUserId());

            // Send email
            try {
                // 환전 거래 완료 데이터 전송
                sendExchange2OctaAML(exchange, user, fromCountry, toCountry, traveler);

                notificationService.sendEmailConfirmExchange(user, traveler, exchange, fromCountry, toCountry);
            } catch (CashmallowException e) {
                logger.error(e.getMessage(), e);
            }

            // Send FCM notification to the user.
            notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.EX, FcmEventValue.CF, txnId);

        } else if (txnType.equals(RelatedTxnType.REMITTANCE)) {
            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(txnId);
            RemittanceTravelerSnapshot remitTravelerSnapshot = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(txnId);

            fromCountry = countryService.getCountry(remittance.getFromCd());
            toCountry = countryService.getCountry(remittance.getToCd());

            traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
            user = userRepositoryService.getUserByUserId(traveler.getUserId());

            msg.append("\n");
            msg.append("화폐:" + fromCountry.getIso4217());
            msg.append(", 매핑 금액:" + remittance.getFromAmt());
            msg.append("\n");
            msg.append("입금은행:" + remitTravelerSnapshot.getBankName());
            msg.append(", 유저ID:" + traveler.getUserId());

            // Do not send FCM for Remittance.
        }

        if (paygateRecdIds != null) {
            msg.append("맵핑 Tid:").append(paygateRecdIds);
        }

        // Send Slack
        if (isManual) {
            alarmService.aAlert("수동매핑완료", msg.toString(), user);
        } else {
            alarmService.aAlert("자동매핑완료", msg.toString(), user);
        }

        long userId = (user == null) ? -1 : user.getId();
        logger.info("{}: 매핑 완료 알림. user_id={}, relatedTxnType={}, relatedTxnId={}",
                method, userId, txnType, txnId);

    }

    // transaction 데이터를 생성하면서 연관된 통장내역과 mapping을 해줌.
    @Transactional(rollbackFor = CashmallowException.class)
    public TransactionMapping createTransactionRecordAndMapping(TransactionRecord transactionRecord,
                                                                TransactionMapping transactionMapping, String paygateRecdId, Long bankAccountId)
            throws CashmallowException {
        String method = "createTransactionRecordAndMapping()";

        int insertTxnRow = insertTrasactionRecord(transactionRecord);

        if (insertTxnRow < 1) {
            logger.error("{}: Failed to insert TransactionRocord. RelatedType={}, paygateRecordId={}", method,
                    transactionRecord.getRelatedTxnType(), paygateRecdId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        transactionMapping.setTransactionRecId(transactionRecord.getId());
        transactionMapping.setPaygateRecId(paygateRecdId);

        int insertMappingRow = 0;

        insertMappingRow = insertTransactionMapping(transactionMapping);

        if (insertMappingRow < 1) {
            logger.error("{}: Failed to insert TransactionMapping. txnId={}, paygateRecId={}", method,
                    transactionMapping.getTransactionRecId(), transactionMapping.getPaygateRecId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return transactionMapping;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public TransactionRecord createTransactionRecordByCashOut(long creatorId, long cashOutId, String iso4217, BigDecimal cashOutAmt)
            throws CashmallowException {
        String method = "createTransactionRecordByCashOut()";

        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setFundingStatus(FundingStatus.CONFIRM.name());
        transactionRecord.setIso4217(iso4217);
        transactionRecord.setAmount(cashOutAmt);
        transactionRecord.setRelatedTxnType(RelatedTxnType.CASH_OUT.name());
        transactionRecord.setRelatedTxnId(cashOutId);
        transactionRecord.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        transactionRecord.setCreator(creatorId);

        int insertTxnRow = insertTrasactionRecord(transactionRecord);

        if (insertTxnRow != 1) {
            logger.error("{}: Failed to insert TransactionRocord. RelatedType={}, cashOutId={}", method,
                    transactionRecord.getRelatedTxnType(), cashOutId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return transactionRecord;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void deleteTransactionRecordByCashOut(long cashOutId)
            throws CashmallowException {
        String method = "deleteTransactionRecordByCashOut()";

        Map<String, Object> searchParams = new HashMap<>();

        searchParams.put("relatedTxnType", RelatedTxnType.CASH_OUT.name());
        searchParams.put("fundingStatus", FundingStatus.CONFIRM.name());
        searchParams.put("relatedTxnId", cashOutId);

        try {
            TransactionRecord transactionRecord = getTransactionRecord(searchParams);

            int deleteTxnRow = deleteTrasactionRecord(transactionRecord);

            if (deleteTxnRow != 1) {
                logger.error("{}: Failed to delete TransactionRocord. RelatedType={}, cashOutId={}", method,
                        transactionRecord.getRelatedTxnType(), cashOutId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void changeBankAccountId(ChangeBankAccountIdRequest request) {
        if (request.getTxnType().equals(ChangeBankAccountIdRequest.TxnType.EXCHANGE)) {
            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(request.getId());
            exchange.setBankAccountId(Math.toIntExact(request.getBankAccountId()));
            exchangeRepositoryService.updateExchangeBankAccountId(exchange);

        } else if (request.getTxnType().equals(ChangeBankAccountIdRequest.TxnType.REMITTANCE)) {
            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(request.getId());
            remittance.setBankAccountId(request.getBankAccountId());
            remittanceRepositoryService.updateRemittanceBankAccountId(remittance);
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    @Async
    public void tryAutoMappingForDbsRecord(PaygateRecord paygateRecord) throws CashmallowException {
        try {
            List<RemittanceTraverlerInfo> remittanceBeforeMappingList = remittanceRepositoryService.getRemittanceBeforeMappingList(paygateRecord.getSenderAccountNo());
            List<Exchange> exchangeBeforeMappingList = exchangeRepositoryService.getExchangeBeforeMappingList(paygateRecord.getSenderAccountNo());

            for (RemittanceTraverlerInfo remittance : remittanceBeforeMappingList) {
                logger.debug("auto remittance : remit account no=" + remittance.getAccountNo() + ", record account no=" + paygateRecord.getSenderAccountNo());
                if (isValidateDeposit(paygateRecord.getSenderAccountNo(),
                        remittance.getAccountNo(),
                        remittance.getBankCode(),
                        paygateRecord.getAmount(),
                        remittance.getFromAmt())) {
                    logger.info("tryAutoMappingForDbsRecord() : auto mapping remittance start, remitId={}", remittance.getId());
                    mappingForRemittance(paygateRecord, remittance);

                    // mallowlink 응답 성공 후, DP 상태로 바뀌면 초대완료 쿠폰 발급
                    Remittance remit = remittanceRepositoryService.getRemittanceByRemittanceId(remittance.getId());
                    if (remit.getRemitStatus().equals(RemittanceStatusCode.DP)) {
                        if (isUseCoupon(remit.getCouponUserId())) {
                            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remit.getTravelerId());
                            CouponIssueUser couponIssueUser = couponUserService.getCouponUserByIdAndStatus(remit.getCouponUserId(), AvailableStatus.USED);

                            if (couponIssueUser != null) {
                                CountryCode countryCode = CountryCode.of(remit.getFromCd());
                                // 사용한 쿠폰이 초대 쿠폰인지 확인
                                Coupon coupon = couponService.getCouponById(couponIssueUser.getCouponId());
                                // 신규 가입자가 사용하는 쿠폰이 초대 쿠폰이 맞으면 초대완료 쿠폰 발급을 위해 진행
                                if (coupon != null && coupon.getCouponCode().startsWith(SystemCouponType.thankYouMyFriend.getCode())) {
                                    // 초대완료 쿠폰 발급
                                    couponMobileService.issueMobileCouponsV3(SystemCouponType.thankYouToo.getCode(), countryCode.name(), countryCode.getCurrency(), traveler.getUserId(), countryCode.getZoneId(), null, remit.getCouponUserId());
                                }
                            }
                        }
                    }

                    break;
                }
            }

            for (Exchange exchange : exchangeBeforeMappingList) {
                logger.debug("auto exchange : exchange account no=" + exchange.getTrAccountNo() + ", record account no=" + paygateRecord.getSenderAccountNo());
                if (isValidateDeposit(paygateRecord.getSenderAccountNo(),
                        exchange.getTrAccountNo(),
                        exchange.getBankCode(),
                        paygateRecord.getAmount(),
                        exchange.getFromAmt())) {
                    logger.info("tryAutoMappingForDbsRecord() : auto mapping exchange start, exchangeId={}", exchange.getId());
                    mappingForExchange(paygateRecord, exchange);

                    // mallowlink 응답 성공 후, CF 상태로 바뀌면 초대완료 쿠폰 발급
                    Exchange ex = exchangeRepositoryService.getExchangeByExchangeId(exchange.getId());
                    if (ex.getExStatus().equals(ExStatus.CF.name())) {
                        if (isUseCoupon(ex.getCouponUserId())) {
                            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(ex.getTravelerId());
                            CouponIssueUser couponIssueUser = couponUserService.getCouponUserByIdAndStatus(ex.getCouponUserId(), AvailableStatus.USED);

                            if (couponIssueUser != null) {
                                CountryCode countryCode = CountryCode.of(ex.getFromCd());
                                // 사용한 쿠폰이 초대 쿠폰인지 확인
                                Coupon coupon = couponService.getCouponById(couponIssueUser.getCouponId());
                                // 신규 가입자가 사용하는 쿠폰이 초대 쿠폰이 맞으면 초대완료 쿠폰 발급을 위해 진행
                                if (coupon != null && coupon.getCouponCode().startsWith(SystemCouponType.thankYouMyFriend.getCode())) {
                                    // 초대완료 쿠폰 발급
                                    couponMobileService.issueMobileCouponsV3(SystemCouponType.thankYouToo.getCode(), countryCode.name(), countryCode.getCurrency(), traveler.getUserId(), countryCode.getZoneId(), null, ex.getCouponUserId());
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("tryAutoMappingForDbsRecord() : {}", e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    @Async
    public void tryAutoMappingForExchange(long exchangeId) throws CashmallowException {
        try {
            Exchange exchange = exchangeRepositoryService.getExchangeBeforeMapping(exchangeId);
            List<PaygateRecord> notMappingRecordList = paygateRecordMapper.getNotMappingPaygateRecordId(dbsProperties.accountId());
            for (PaygateRecord paygateRecord : notMappingRecordList) {
                logger.debug("auto exchange : exchange account no=" + exchange.getTrAccountNo() + ", record account no=" + paygateRecord.getSenderAccountNo());
                if (isValidateDeposit(paygateRecord.getSenderAccountNo(),
                        exchange.getTrAccountNo(),
                        exchange.getBankCode(),
                        paygateRecord.getAmount(),
                        exchange.getFromAmt())) {
                    logger.info("tryAutoMappingForExchange() : auto mapping start");
                    mappingForExchange(paygateRecord, exchange);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("tryAutoMappingForExchange() : {}", e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    private void mappingForExchange(PaygateRecord paygateRecord, Exchange exchange) throws CashmallowException {
        TransactionRecord transactionRecord = initTransactionRecord(RelatedTxnType.EXCHANGE, exchange.getId(), exchange.getFromAmt(), exchange.getTravelerId(), paygateRecord.getIso4217());
        TransactionMapping transactionMapping = initTransactionMapping(exchange.getTravelerId());

        matchTransactionMappingForAuto(transactionRecord, transactionMapping, paygateRecord);

        List<String> paygateRecordIds = new ArrayList<>();
        paygateRecordIds.add(paygateRecord.getId());

        sendMappingResultNotification(RelatedTxnType.valueOf(transactionRecord.getRelatedTxnType()),
                transactionRecord.getRelatedTxnId(), false, paygateRecordIds);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    @Async
    public void tryAutoMappingForRemittance(long remitId) throws CashmallowException {
        try {
            RemittanceTraverlerInfo remittance = remittanceRepositoryService.getRemittanceBeforeMapping(remitId);

            if (ObjectUtils.isEmpty(remittance)) {
                return;
            }

            List<PaygateRecord> notMappingRecordList = paygateRecordMapper.getNotMappingPaygateRecordId(dbsProperties.accountId());
            for (PaygateRecord paygateRecord : notMappingRecordList) {
                logger.debug("auto remittance : remit account no=" + remittance.getAccountNo() + ", record account no=" + paygateRecord.getSenderAccountNo());
                if (isValidateDeposit(paygateRecord.getSenderAccountNo(),
                        remittance.getAccountNo(),
                        remittance.getBankCode(),
                        paygateRecord.getAmount(),
                        remittance.getFromAmt())) {
                    logger.info("tryAutoMappingForRemittance() : auto mapping start");
                    mappingForRemittance(paygateRecord, remittance);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("tryAutoMappingForRemittance() : {}", e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    private void mappingForRemittance(PaygateRecord paygateRecord, Remittance remittance) throws CashmallowException {
        TransactionRecord transactionRecord = initTransactionRecord(RelatedTxnType.REMITTANCE, remittance.getId(),
                remittance.getFromAmt(), remittance.getTravelerId(), paygateRecord.getIso4217());
        TransactionMapping transactionMapping = initTransactionMapping(remittance.getTravelerId());

        matchTransactionMappingForAuto(transactionRecord, transactionMapping, paygateRecord);

        mallowlinkRemittanceService.requestRemittance(remittance);

        List<String> paygateRecordIds = new ArrayList<>();
        paygateRecordIds.add(paygateRecord.getId());
        sendMappingResultNotification(RelatedTxnType.valueOf(transactionRecord.getRelatedTxnType()),
                transactionRecord.getRelatedTxnId(), false, paygateRecordIds);
    }

    private TransactionMapping initTransactionMapping(Long travelerId) {
        TransactionMapping returnValue = new TransactionMapping();
        returnValue.setCreator(travelerId);
        returnValue.setTravelerId(travelerId);
        return returnValue;
    }

    private TransactionRecord initTransactionRecord(RelatedTxnType txnType, Long id, BigDecimal amount, Long creator, String currency) {
        TransactionRecord returnValue = new TransactionRecord();
        returnValue.setRelatedTxnId(id);
        returnValue.setRelatedTxnType(txnType.name());
        returnValue.setIso4217(currency);
        returnValue.setAmount(amount);
        returnValue.setCreator(creator);
        return returnValue;
    }

    /**
     * 환전 신청 완료시 거래내역 AML 전송
     *
     * @param exchange
     * @param user
     * @param fromCountry
     * @param toCountry
     * @param traveler
     */
    private void sendExchange2OctaAML(Exchange exchange, User user, Country fromCountry, Country toCountry, Traveler traveler) {
        Map<String, CurrencyRate> rates = currencyService.getCurrencyRateByKrwAndUsd(fromCountry.getIso4217(), exchange.getCreatedDate());

        // AccountBase
        String prefix = "EX";
        AmlAccountBase amlAccountBase = AmlAccountBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + exchange.getId().toString()) // 송금 거래 번호
                .customerNo(user.getId().toString(), user.getCountryCode().name())
                .accountStsCd("01")
                .currencyCd(fromCountry.getIso4217())
                .prodCd("03")
                .prodRaCd("03")
                .prodRepCd("01")
                .accountOpenDd(exchange.getCreatedDate())
                // .closeDd("99991231") // default value is 99991231
                .accountOpenPurposeCd(exchange.getExchangePurpose())
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
                .accountNo(prefix + exchange.getId().toString()) // 송금 거래 번호
                // .prodCd("03") // 상품 코드 - default value is "03" mean "해외송금"
                // .prodNm("해외송금") // 상품명 - default value is "해외송금"
                .build();

        // AccountTranBase
        AmlAccountTranBase amlAccountTranBase = AmlAccountTranBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + exchange.getId().toString()) // 송금 거래 번호
                .prodCd("03")
                .tranDd(exchange.getCreatedDate()) // 거래 시간 (YYYYMMDDHHMMSS)
                .tranSeqNo(1) // 거래_일련_번호
                .customerNo(user.getId().toString(), user.getCountryCode().name())
                .tranTime(exchange.getCreatedDate())
                // .tranChannelCd("19") // 거래 방법 OR 경로 - default value is "19" mean "모바일"
                // .tranChannelNm("모바일") // default value is "모바일"
                // .tranWayCd("05") // 재화의 종류 - default value is "05" mean "외환(해외송금)"
                // .tranWayNm("외환(해외송금)") // default value is "외환(해외송금)"
                .tranKindCd("03") // 거래 분류 - default value is "05" mean "송금(해외)"
                .tranKindNm("환전") // default value is "해외송금"
                .summaryCd("03") // 거래 내용 - default value is "05" mean "송금(해외)"
                .summaryNm("환전") // default value is "송금, 이체영수"
                .rltFinanOrgCountryCd(fromCountry.getIso3166()) // 고객 국가
                .rltFinanOrgCd(traveler.getBankCode()) // 고객 은행 코드 - default value is "001" mean "한국은행"
                .rltFinanOrgNm(traveler.getBankName()) // 고객 은행 이름 - default value is "한국은행"
                .rltaccNo(traveler.getAccountNo()) // 수취인 계좌번호
                .rltaccOwnerNm(traveler.getAccountName()) // 수취인 계좌주명
                .toFcFrFcDiv("2") // 당타행 구분 - default value is "2" mean "타행"
                // .accountTpDiv("02") // 계좌의 종류 - default value is "02" mean "송.수취계좌"
                // .accountDivTpNm("송.수취계좌") // default value is "송.수취계좌"
                .currencyCd(fromCountry.getIso4217()) // 통화코드
                .tranAmt(exchange.getFromAmt().multiply(rates.get("KRW").getRate()).setScale(0, RoundingMode.DOWN)) // 거래금액(송금) - ex) HKD -> KRW, 원화라 소수점 제거
                .wonTranAmt(exchange.getToAmt()) // 원화환산금액 - ex) toAmt 화폐 고정, 소수점 제거
                .fexTranAmt(exchange.getFromAmt()) // 외화거래금액 - ex) HKD
                .usdExchangeAmt(exchange.getFromAmt().multiply(rates.get("USD").getRate())) // 달러환산금액 - ex) HKD -> USD
                .tranPurposeCd(exchange.getExchangePurpose()) // 거래의 목적코드
                // .tranPurposeNm("") // 거래 목적명
                .fexTranPurposeCd(exchange.getExchangePurpose()) // 외화거래의 목적코드
                // .fexTranPurposeNm("") // 외화거래 목적명
                .build();

        // AccountTranSendReceipt
        AmlAccountTranSendReceipt amlAccountTranSendReceipt = AmlAccountTranSendReceipt.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + exchange.getId().toString()) // 송금 거래 번호
                .prodCd("03") // 상품 코드 - default value is "03" mean "해외송금"
                .tranDd(exchange.getCreatedDate())
                .tranSeqNo(1) // 거래_일련_번호
                // .repayDivisionCd("01") // 파트너 구분(송금) - default value is "01" mean "계좌번호"
                .rltFinanOrgCountryCd(toCountry.getIso3166()) // 상대_은행_국가_코드 - 수취인
                .rltFinanOrgCd("") // 상대_은행_코드 - 수취인 - default value is "001" mean "한국은행"
                .rltFinanOrgNm(exchange.getTrBankName()) // 상대_은행_명 - 수취인 - default value is "한국은행"
                .rltaccNo(exchange.getTrAccountNo()) // 수취인 계좌번호
                .rltaccOwnerDiv("01") // 수취인 계좌주명 구분 - default value is "01" mean "개인"
                .rltaccOwnerDivNm("개인") // default value is "개인"
                .rltaccOwnerNm(exchange.getTrAccountName()) // 수취인 계좌주명
                .rltaccOwnerPhoneNo(exchange.getTrPhoneNumber()) // 수취인(소유자) 연락처
                .rltOpenPurposeCd(exchange.getExchangePurpose()) // 개설_목적_코드 - default value is "99" mean "기타"
                // .rltOpenPurposeNm("기타") // default value is "기타"
                .build();

        OctaAMLKYCRequest request = new OctaAMLKYCRequest(amlAccountBase, amlProdBase, amlAccountTranBase, amlAccountTranSendReceipt);
        octaAMLKYCService.execute(request);
    }
}
