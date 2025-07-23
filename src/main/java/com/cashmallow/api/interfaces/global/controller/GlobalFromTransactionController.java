package com.cashmallow.api.interfaces.global.controller;

import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.domain.model.company.BankAccount;
import com.cashmallow.api.domain.model.company.TransactionMapping;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.refund.JpRefundAccountInfo;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.remittance.RemittanceMallowlink;
import com.cashmallow.api.domain.model.remittance.RemittanceMallowlinkRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.global.dto.*;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/global")
@RequiredArgsConstructor
public class GlobalFromTransactionController {

    private final PaygateServiceImpl paygateService;
    private final RefundServiceImpl refundService;
    private final RefundRepositoryService refundRepositoryService;
    private final RemittanceAdminService remittanceAdminService;
    private final RemittanceServiceImpl remittanceService;
    private final ExchangeServiceImpl exchangeService;
    private final NotificationServiceImpl notificationService;
    private final UserRepositoryService userRepositoryService;
    private final RemittanceMallowlinkRepositoryService remittanceMallowlinkRepositoryService;

    @PostMapping("/jp/transaction/cancel")
    public ApiResultVO cancelJpTransaction(@RequestBody @Valid GlobalTransactionCancelRequest request) {

        ApiResultVO resultVO = new ApiResultVO();
        try {
            if(request.txnType().equals(TransactionRecord.RelatedTxnType.REMITTANCE)) {
                remittanceService.cancelRemittanceByAdmin(request.relatedTxnId());
            } else if (request.txnType().equals(TransactionRecord.RelatedTxnType.EXCHANGE)) {
                exchangeService.cancelExchangeByAdmin(request.relatedTxnId());
            }

            resultVO.setSuccessInfo();
        } catch (Exception e) {
            resultVO.setFailInfo(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return resultVO;
    }

    /**
     * JP admin 수동 매핑 (현재 JP 는 자동 매핑이 존재하지 않음)
     **/
    @PostMapping("/jp/manual-mapping")
    public ApiResultVO tryManualMapping(@RequestBody @Valid GlobalManualMappingRequest request) throws CashmallowException {
        log.debug("request:{}", request);

        Long systemId = -1L;

        TransactionRecord transactionRecord = new TransactionRecord();
        TransactionMapping transactionMapping = new TransactionMapping();

        transactionRecord.setRelatedTxnId(request.relatedTxnId());
        transactionRecord.setRelatedTxnType(request.txnType().name());
        transactionRecord.setAmount(request.amount());
        transactionRecord.setIso4217(request.currency());

        transactionRecord.setCreator(systemId);

        List<String> paygateRecordIds = request.depositIdList();
        String inputAccountNo = "";
        String inputName = request.senderName();
        transactionMapping.setCreator(systemId);

        BankAccount bankAccount = paygateService.getBankAccount(request.countryCode().getCode(), request.bankCode(), request.accountNo());

        ApiResultVO resultVO = new ApiResultVO();
        try {
            paygateService.matchTransactionMappingForManual(transactionRecord, transactionMapping, paygateRecordIds,
                    bankAccount.getId().longValue(), inputAccountNo, inputName, true);

            if (TransactionRecord.RelatedTxnType.REMITTANCE.equals(request.txnType())) {
                RemittanceMallowlink remittanceMallowlink = remittanceMallowlinkRepositoryService.getRemittanceMallowlinkByRemitId(request.relatedTxnId());
                GlobalMappingResponse globalMappingResponse = new GlobalMappingResponse(remittanceMallowlink.getTransactionId());
                resultVO.setSuccessInfo(globalMappingResponse);
            } else {
                resultVO.setSuccessInfo();
            }
        } catch (Exception e) {
            resultVO.setFailInfo(e.getMessage());
            log.error(e.getMessage(), e);
            // WLF_USER("6002", "Please check the Watch List")
            if (e.getMessage().toUpperCase().contains("WATCH LIST") || e.getMessage().toUpperCase().contains("WLF_USER")) {
                // 멜로링크 코드
                resultVO.setCode("6002");
            }
        }

        return resultVO;
    }

    @PostMapping("/jp/rollback-mapping")
    public ApiResultVO rollbackMapping(@RequestBody @Valid GlobalRollbackMappingRequest request) throws CashmallowException {

        ApiResultVO resultVO = new ApiResultVO();

        try {
            BankAccount bankAccount = paygateService.getBankAccount(CountryCode.JP.getCode(), request.bankCode(), request.accountNo());
            paygateService.rollbackTransactionMapping(request, bankAccount);
            resultVO.setSuccessInfo();
        } catch (Exception e) {
            resultVO.setFailInfo(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return resultVO;
    }

    @PostMapping("/jp/refund/account/re-register")
    public ApiResultVO requestRefundAccountReRegister(@RequestBody List<Long> accountInfoIdList) {
        ApiResultVO resultVO = new ApiResultVO().setSuccessInfo();

        try {
            for (Long accountInfoId : accountInfoIdList) {
                JpRefundAccountInfo jpRefundAccountInfo = refundRepositoryService.getJpRefundAccountInfoById(accountInfoId);
                jpRefundAccountInfo.setNeedReRegister("Y");
                refundRepositoryService.updateJpRefundAccountInfo(jpRefundAccountInfo);
                User user = userRepositoryService.getUserByUserId(jpRefundAccountInfo.getTravelerId());
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RF, FcmEventValue.AR, 0L, "");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return resultVO;
    }

    @PostMapping("/jp/refund/receipt")
    public ApiResultVO receiptRefunds(@RequestBody List<Long> refundIdList) {
        ApiResultVO resultVO = new ApiResultVO().setSuccessInfo();

        try {
            for (Long refundId : refundIdList) {
                NewRefund newRefund = refundRepositoryService.getNewRefundById(refundId);
                newRefund.setRefundStatus(NewRefund.RefundStatusCode.MP);
                refundRepositoryService.updateNewRefund(newRefund);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return resultVO;
    }

    @PostMapping("/jp/refund/{refundId}/cancel")
    public ApiResultVO cancelRefund(@PathVariable Long refundId) {
        ApiResultVO resultVO = new ApiResultVO().setSuccessInfo();

        try {
            refundService.cancelNewRefundByCashmallow(0L, refundId); // admin Id에 System Id 필요
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            resultVO.setFailInfo(e.getMessage());
        }

        return resultVO;
    }

    @PostMapping("/jp/refund/{refundId}/complete")
    public ApiResultVO completeRefund(@PathVariable Long refundId) {
        ApiResultVO resultVO = new ApiResultVO().setSuccessInfo();

        try {
            refundService.completeNewRefund(0L, refundId); // admin Id에 System Id 필요
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return resultVO;
    }

    @PostMapping("/jp/remittance/{remitId}/aml")
    public ApiResultVO confirmRemittanceReceiverAml(@PathVariable Long remitId) {
        ApiResultVO resultVO = new ApiResultVO().setSuccessInfo();

        try {
            remittanceAdminService.confirmReceiverAmlByAdmin(remitId);
        } catch (Exception e) {
            resultVO.setFailInfo(e.getMessage());
            log.error(e.getMessage(), e);
            // WLF_USER("6002", "Please check the Watch List")
            if (e.getMessage().toUpperCase().contains("WATCH LIST")) {
                // 멜로링크 코드
                resultVO.setCode("6002");
            }
        }

        return resultVO;
    }

    @PostMapping("/jp/transaction/change/bank")
    public ApiResultVO changeTransactionBank(@RequestBody @Valid GlobalChangeTransactionBankRequest request) {

        ApiResultVO resultVO = new ApiResultVO();
        try {
            BankAccount bankAccount = paygateService.getBankAccount(CountryCode.JP.getCode(), request.bankCode(), request.bankAccountNo());

            if(request.txnType().equals(TransactionRecord.RelatedTxnType.REMITTANCE)) {
                remittanceService.changeRemittanceBankAccount(request.relatedTxnId(), bankAccount);
            } else if (request.txnType().equals(TransactionRecord.RelatedTxnType.EXCHANGE)) {
                exchangeService.changeExchangeBankAccount(request.relatedTxnId(), bankAccount);
            }

            resultVO.setSuccessInfo();
        } catch (Exception e) {
            resultVO.setFailInfo(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return resultVO;
    }

}
