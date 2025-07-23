package com.cashmallow.api.interfaces.dbs;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.application.impl.RefundServiceImpl;
import com.cashmallow.api.domain.model.bankinfo.BankInfo;
import com.cashmallow.api.domain.model.company.DbsRemittance;
import com.cashmallow.api.domain.model.company.PaygateRecord;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.fx.FxQuotationEntity;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.bank.BankServiceImpl;
import com.cashmallow.api.interfaces.dbs.client.DbsClient;
import com.cashmallow.api.interfaces.dbs.model.dto.*;
import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.cashmallow.api.interfaces.fx.FxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cashmallow.common.CommonUtil.abstractJsonString;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbsService {
    private final DbsProperties dbsProperties;
    private final DbsClient dbsClient;
    private final CompanyServiceImpl companyService;
    private final BankServiceImpl bankService;
    private final SecurityService securityService;
    private final AlarmService alarmService;
    private final FxService fxService;
    private final TravelerRepositoryService travelerRepositoryService;
    private final RefundRepositoryService refundRepositoryService;
    private final RefundServiceImpl refundService;

    public DbsBalanceResponse getDbsAccountBalance(Long managerId) {
        DbsBalanceRequest request = new DbsBalanceRequest();
        request.setAccountNo(dbsProperties.accountNo());
        request.setEndUserId(String.valueOf(managerId));
        request.setAccountCcy("MCA"); // 다중화폐 조회하려면 MCA
        // TEST 123
        return dbsClient.requestBalance(request);
    }

    public FxInfoResponse getDbsFxInfo() {
        return dbsClient.getDbsFxInfo(Const.ACT);
    }

    public CashmallowFxQuotationResponse getDbsFxQuotation(Long endUserId, CashmallowFxQuotationRequest request) throws CashmallowException {
        request.setEndUserId(endUserId.toString());


        final CashmallowFxQuotationResponse dbsFxQuotation = dbsClient.getDbsFxQuotation(request);
        dbsFxQuotation.setQuotationId(fxService.addFxQuotation(request, dbsFxQuotation));
        return dbsFxQuotation;
    }

    public String validateDbsFxQuotation() throws CashmallowException {
        try {
            DbsFxTransactionResponse response = dbsClient.getNotCompletedFxTransaction();
            final List<String> transactionIds = response.getClientTransactionIdList();
            return switch (transactionIds.size()) {
                case 0 -> StringUtils.EMPTY;
                default -> {
                    final var sb = new StringBuilder();
                    transactionIds.forEach(transactionId -> {
                        FxQuotationEntity fxQuotationEntity = fxService.getFxQuotationByTransactionId(transactionId);
                        sb.append("""
                                        ---------------------
                                        from : %s %s
                                        to : %s %s
                                        신청일 : %s
                                        ---------------------
                                """.formatted(fxQuotationEntity.getFromCurrency(), fxQuotationEntity.getFromAmount(), fxQuotationEntity.getToCurrency(), fxQuotationEntity.getToAmount(),
                                fxQuotationEntity.getUpdatedAt().plusHours(9L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    });

                    yield sb.toString();
                }
            };
        } catch (Exception e) {
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    public String getDbsFxQuotationApprove(Long endUserId, Long quotationId) {
        final FxQuotationEntity fxQuotationEntity = fxService.getFxQuotoationResult(quotationId);

        CashmallowFxQuotationApproveRequest request = new CashmallowFxQuotationApproveRequest();
        request.setRateUid(fxQuotationEntity.getApproveId());
        request.setTransactionId(fxQuotationEntity.getTransactionId());
        request.setSenderAccountNo(fxQuotationEntity.getFromAccount());
        request.setReceiverAccountNo(fxQuotationEntity.getToAccount());
        request.setCurrencyPair(fxQuotationEntity.getCurrencyPair());
        request.setEndUserId(endUserId.toString());

        if (CountryCode.TH.getCurrency().equalsIgnoreCase(fxQuotationEntity.getToCurrency())) {
            request.setReceiverName("SCB Tech X Company Limited");
            request.setReceiverSwiftBankCode("SICOTHBKXXX");
            request.setReceiverBankCountryCode("TH");
            request.setReceiverAddress("Bangkok 10900 Thailand");
        } else if (CountryCode.PH.getCurrency().equalsIgnoreCase(fxQuotationEntity.getToCurrency())) {
            request.setReceiverName("RIZAL COMMERCIAL BANKING CORP");
            request.setReceiverSwiftBankCode("RCBCPHMMXXX");
            request.setReceiverBankCountryCode("PH");
            request.setReceiverAddress("GF,Y TowerII Bldg Leviste");
        } else if (CountryCode.ID.getCurrency().equalsIgnoreCase(fxQuotationEntity.getToCurrency())) {
            request.setReceiverName("PT Artajasa Pembayaran Elektronis");
            request.setReceiverSwiftBankCode("BMRIIDJAXXX");
            request.setReceiverBankCountryCode("ID");
            request.setReceiverAddress("Menara Thamrin");
        }

        try {
            dbsClient.getDbsFxQuotationApprove(request);
            fxService.updateFxQuotationStatus(quotationId, "COMPLETED");
            return null;
        } catch (Exception e) {
            final String errorMessage = "FX Quotation 실패 (user ID) : " + endUserId + ", 실패사유 : " + e.getMessage();
            log.error(errorMessage, e);
            fxService.updateFxQuotationStatus(quotationId, "REJECT");
            alarmService.i("FX Quotation", "[ADMIN] " + errorMessage);
            return abstractJsonString(errorMessage);
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void receiveDayRemittanceResult(DbsRemittanceNotificationRequest notificationRequest) throws CashmallowException {
        PaygateRecord remittanceRecord = companyService.getPaygateRecord(notificationRequest.getRemittanceId());
        DbsRemittance dbsRemittance = companyService.getDbsRemittance(notificationRequest.getRemittanceId());

        final boolean isReject = notificationRequest.getRemittanceResultCode().equalsIgnoreCase("RJCT");

        String slackTitle = "DBS 송금";

        if (ObjectUtils.isNotEmpty(dbsRemittance)) {
            // 리젝시, 1. 기존 입금내역 반환가능한 상태로 롤백(workstatus -> ready)
            // 2. DBS송금내역 리젝으로 업데이트
            // 3. 슬랙
            String txnMessage = ", 거래타입 : " + dbsRemittance.getRemittanceType() + ", 거래번호 : " + dbsRemittance.getRelatedTxnId();
            if (isReject) {
                dbsRemittance.setResultStatus(DbsRemittance.ResultStatus.REJECT);
                PaygateRecord depositRecord = companyService.getPaygateRecord(remittanceRecord.getSourceId());
                depositRecord.setWorkStatus(PaygateRecord.WorkStatus.READY);
                companyService.updatePaygateRecord(depositRecord);
                alarmService.aAlert(slackTitle, "[ADMIN] DBS 송금 실패 (user ID) : " + dbsRemittance.getUserId() + ", 실패사유 : " + notificationRequest.getRemittanceRejectReason() + txnMessage, null);
                executeRejectTransaction(dbsRemittance);
            } else {
                dbsRemittance.setResultStatus(DbsRemittance.ResultStatus.COMPLETED);
                alarmService.aAlert(slackTitle, "[ADMIN] DBS 송금 성공 (금액) : " + dbsRemittance.getCurrency() + " " + dbsRemittance.getAmount() + txnMessage, null);
                executeSuccessTransaction(dbsRemittance);
            }

            companyService.updateDbsRemittance(dbsRemittance);
            return;
        }

        final FxQuotationEntity fxQuotationEntity = fxService.getFxQuotationByTransactionId(notificationRequest.getRemittanceId());
        if (ObjectUtils.isNotEmpty(fxQuotationEntity)) {
            if (isReject) {
                alarmService.aAlert(slackTitle, "[ADMIN] DBS 송금 실패 (작업자 ID) : " + fxQuotationEntity.getEndUserId() + ", 실패사유 : " + notificationRequest.getRemittanceRejectReason(), null);
            } else {
                alarmService.aAlert(slackTitle, "[ADMIN] DBS 송금 성공 (금액) : " + fxQuotationEntity.getToCurrency() + " " + fxQuotationEntity.getToAmount(), null);
            }
        }
    }

    private void executeSuccessTransaction(DbsRemittance dbsRemittance) throws CashmallowException {
        switch (dbsRemittance.getRelatedTxnType()) {
            case REMITTANCE -> {
            }
            case REFUND -> refundService.completeNewRefund(dbsRemittance.getUserId(), dbsRemittance.getRelatedTxnId());
            case EXCHANGE -> {
            }
            case REPAYMENT -> {
            }
        }
    }

    private void executeRejectTransaction(DbsRemittance dbsRemittance) {
        switch (dbsRemittance.getRelatedTxnType()) {
            case REMITTANCE -> {
            }
            case REFUND -> {
                NewRefund refund = refundRepositoryService.getNewRefundById(dbsRemittance.getRelatedTxnId());
                refund.setRefundStatus(NewRefund.RefundStatusCode.MP);
                refundRepositoryService.updateNewRefund(refund);
            }
            case EXCHANGE -> {
            }
            case REPAYMENT -> {
            }
        }
    }

    @Transactional
    @Async
    public void tryAutoRefund(NewRefund refund) {
        String method = "tryAutoRefundV2()";
        log.debug("{} : start", method);
        Map<String, Object> params = new HashMap<>();
        long relatedTxnId = 0L;
        if (refund.getRelatedTxnType().equals(RelatedTxnType.EXCHANGE)) {
            relatedTxnId = refund.getExchangeId();
        } else {
            relatedTxnId = refund.getRemitId();
        }
        params.put("relatedTxnType", refund.getRelatedTxnType().name());
        params.put("relatedTxnId", relatedTxnId);

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(refund.getTravelerId());
        TransactionRecord transactionRecord = companyService.getTransactionRecord(params);
        List<PaygateRecord> paygateRecordList = companyService.getPaygateRecordListByTransactionRecordId(transactionRecord.getId());

        if (paygateRecordList.isEmpty()) {
            log.error("환불과 매칭되는 PaygateRecord가 없습니다. 환불ID:{}. 거래타입:{}, 거래ID:{}", refund.getId(), refund.getRelatedTxnType(), relatedTxnId);
            failAutoRefund(refund, traveler.getUserId());
            return;
        }

        try {
            PaygateRecord paygateRecord = paygateRecordList.get(0);
            log.debug("{} : requestRemittance", method);
            DbsRemittance dbsRemittance = requestRemittance(traveler, paygateRecord, refund.getToAmt(), traveler.getUserId(), RelatedTxnType.REFUND, refund.getId());
            // GPP(FPS)인 경우 Notifcation없이 Response로 즉시 완료 됨.
            if (Const.GPP.equalsIgnoreCase(dbsRemittance.getRemittanceType()) && "ACTC".equalsIgnoreCase(dbsRemittance.getResponseCode())) {
                refundService.completeNewRefund(traveler.getUserId(), refund.getId());
            }
        } catch (Exception e) {
            log.error("{} : DBS 송금실패 = {}", method, e.getMessage(), e);
            failAutoRefund(refund, traveler.getUserId());
        }
    }

    private void failAutoRefund(NewRefund refund, Long userId) {
        refund.setRefundStatus(NewRefund.RefundStatusCode.MP);
        refundRepositoryService.updateNewRefund(refund);
        String message = "환불 거래번호: " + refund.getId() +
                "\n유저 ID:" + userId +
                "\n환불 금액: " + refund.getAmount() + " " + CountryCode.of(refund.getToCd()).getCurrency();
        alarmService.aAlert("자동 환불실패", message, null);
    }

    @Transactional
    public DbsRemittance requestRemittance(Traveler traveler, PaygateRecord paygateRecord, BigDecimal amount,
                                           Long endUserId, RelatedTxnType relatedTxnType, Long relatedTxnId) throws CashmallowException {
        BankInfo travelerBankInfo;
        if (ObjectUtils.isEmpty(traveler.getBankInfoId())) {
            travelerBankInfo = bankService.getHkBankInfoByName(traveler.getBankName());
        } else {
            travelerBankInfo = bankService.getBankInfoById(traveler.getBankInfoId());
        }

        if (ObjectUtils.isEmpty(travelerBankInfo) || !travelerBankInfo.getIso3166().equalsIgnoreCase(CountryCode.HK.name())) {
            // bankInfo가 없거나 홍콩이 아니면 보낼 수 없다
            throw new CashmallowException("BankInfo가 없거나, HK은행이 아닐경우 자동송금 실패, travelerBankName={}", traveler.getBankName());
        } else if (StringUtils.isEmpty(traveler.getAccountName())) {
            // 고객통장의 이름이 없는 경우 보낼 수 없다.
            throw new CashmallowException("고객 통장의 명의(이름)이 없습니다., travelerAccountName={}", traveler.getAccountName());
        }

        String remittanceType = getDbsRemittanceType(travelerBankInfo.getName());

        DbsRemittanceRequest dbsRemittanceRequest = DbsRemittanceRequest.of(traveler, paygateRecord, amount, endUserId, remittanceType,
                dbsProperties.accountName(), dbsProperties.accountNo(), travelerBankInfo.getCode(), travelerBankInfo.getSwiftCode());

        DbsRemittanceResponse dbsRemittanceResponse = dbsClient.requestRemittance(dbsRemittanceRequest);
        if (!"ACTC".equals(dbsRemittanceResponse.getDbsTransactionStatus())) {
            log.error("DBS 송금 실패. 이유 : {}", dbsRemittanceResponse.getDbsTransactionStatus());
            throw new CashmallowException("DBS 송금에 실패했습니다. 사유 -" + dbsRemittanceResponse.getDbsTransactionDescription());
        }

        DbsRemittance dbsRemittance = DbsRemittance.of(dbsRemittanceRequest, traveler.getUserId(), dbsRemittanceResponse.getDbsTransactionId(),
                dbsRemittanceResponse.getDbsTransactionStatus(), endUserId, relatedTxnType, relatedTxnId);
        companyService.insertDbsRemittance(dbsRemittance);

        PaygateRecord remittanceRecord = makeRemittanceRecord(paygateRecord, amount, dbsRemittanceResponse, dbsRemittanceRequest);
        companyService.insertPaygateRecord(remittanceRecord);

        // 거래가 끝난 데이터 완료처리
        paygateRecord.setWorkStatus(PaygateRecord.WorkStatus.COMPLETED);
        companyService.updatePaygateRecord(paygateRecord);
        return dbsRemittance;
    }

    private String getDbsRemittanceType(String travelerBankName) {
        // DBS내부 송금은 ACT, 그외 FPS(GPP)
        if (travelerBankName.equalsIgnoreCase("DBS Bank")) {
            return Const.ACT;
        } else {
            return Const.GPP;
        }
    }

    private PaygateRecord makeRemittanceRecord(PaygateRecord paygateRecord, BigDecimal amount, DbsRemittanceResponse dbsRemittanceResponse, DbsRemittanceRequest dbsRemittanceRequest) {
        PaygateRecord remittanceRecord = new PaygateRecord();
        remittanceRecord.setId(dbsRemittanceResponse.getDbsTransactionId());
        remittanceRecord.setCountry(paygateRecord.getCountry());
        remittanceRecord.setBankAccountId(paygateRecord.getBankAccountId());
        remittanceRecord.setIso4217(paygateRecord.getIso4217());
        remittanceRecord.setDepWdrType("WITHDRAW");
        remittanceRecord.setAmount(amount.multiply(new BigDecimal("-1"))); // 빠진 금액은 음수로 표시
        remittanceRecord.setDescription("DBS Remittance Result");
        remittanceRecord.setTxnStatus(dbsRemittanceResponse.getDbsTransactionStatus());
        remittanceRecord.setSourceId(paygateRecord.getId());
        remittanceRecord.setExecutedDate(Timestamp.valueOf(LocalDateTime.now())); // 송금시 실행시간이 없어서 현재시간
        remittanceRecord.setSourceId(paygateRecord.getId());
        // 송금 타입에 따라 상태 변경
        if (Const.GPP.equals(dbsRemittanceRequest.getRemittanceType())) {
            remittanceRecord.setWorkStatus(PaygateRecord.WorkStatus.COMPLETED);
        } else {
            remittanceRecord.setWorkStatus(PaygateRecord.WorkStatus.PROCESSING);
        }

        BigDecimal balance = companyService.getPaygateRecordLastBalance(paygateRecord.getBankAccountId(), paygateRecord.getIso4217());
        remittanceRecord.setBalance(balance.add(remittanceRecord.getAmount()));
        return remittanceRecord;
    }

}
