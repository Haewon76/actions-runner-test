package com.cashmallow.api.interfaces.mallowlink.remittance;

import com.cashmallow.api.application.*;
import com.cashmallow.api.application.impl.RemittanceServiceImpl;
import com.cashmallow.api.domain.model.aml.AmlAccountBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranSendReceipt;
import com.cashmallow.api.domain.model.aml.AmlProdBase;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.country.enums.Currency;
import com.cashmallow.api.domain.model.remittance.*;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.aml.OctaAMLKYCService;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLKYCRequest;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.aml.octa.OctaAmlService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkRemittanceStatus;
import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkNotFoundEnduserException;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookResultRequest;
import com.cashmallow.api.interfaces.mallowlink.enduser.MallowlinkEnduserServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.RemittanceRequestV3;
import com.cashmallow.common.JsonStr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
public class MallowlinkRemittanceServiceImpl {

    private final MallowlinkServiceImpl mallowlinkService;
    private final MallowlinkEnduserServiceImpl enduserService;
    private final MallowlinkRemittanceClient remittanceClient;

    private final TravelerRepositoryService travelerRepositoryService;
    private final UserRepositoryService userRepositoryService;
    private final OctaAmlService octaAmlService;
    private final CountryService countryService;

    private final RemittanceServiceImpl remittanceService;
    private final RemittanceRepositoryService remittanceRepositoryService;
    private final RemittanceMallowlinkRepositoryService remittanceMallowlinkRepositoryService;

    private final SecurityService securityService;
    private final NotificationService notificationService;
    private final AlarmService alarmService;
    private final MessageSource messageSource;

    private final OctaAMLKYCService octaAMLKYCService;

    private final CurrencyService currencyService;

    private final GlobalQueueService globalQueueService;


    public void requestRemittance(Remittance remittance) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
        traveler.setIdentificationNumber(securityService.decryptAES256(traveler.getIdentificationNumber()));

        User user = userRepositoryService.getUserByUserId(traveler.getUserId());

        log.info("remittance:{}", remittance);

        try {
            executeRequest(traveler, remittance);
        } catch (MallowlinkNotFoundEnduserException e) {
            log.info("Mallowlink에 등록 되지 않은 traveler:{}", traveler.getId());

            enduserService.register(user, traveler);

            try {
                executeRequest(traveler, remittance);
            } catch (MallowlinkNotFoundEnduserException ex) {
                log.error("Mallowlink Enduser 등록 후 다시 송금시 실패", ex);
                throw new CashmallowException(INTERNAL_SERVER_ERROR, e.getMessage());
            }

        } catch (MallowlinkException e) {
            log.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, e.getStatus().getMessage());
        }
    }

    private boolean validateDuplicateRemittance(Remittance remittance) {
        RemittanceMallowlink remittanceMallowlink = remittanceMallowlinkRepositoryService.getRemittanceMallowlinkByRemitId(remittance.getId());

        if (ObjectUtils.isEmpty(remittanceMallowlink)) {
            return false;
        }

        return remittanceMallowlink.getStatus().equals(MallowlinkRemittanceStatus.REQUEST) ||
                remittanceMallowlink.getStatus().equals(MallowlinkRemittanceStatus.SUCCESS);
    }

    private void executeRequest(Traveler traveler, Remittance remittance) throws MallowlinkNotFoundEnduserException {
        log.info("remittance:{}", remittance);

        // 송금 상태 Remit Id로 검색해서 REQUEST 또는 SUCCESS이면 송금 금지
        if (validateDuplicateRemittance(remittance)) {
            throw new MallowlinkException(MallowlinkExceptionType.TRANSACTION_IN_PROGRESS);
        }

        // EUR 통화에서만 송금인 region 필수값. 영어로만 보내야 함.
        String currency = CountryInfo.valueOf(remittance.getToCountry().name()).getCurrency();
        RemittanceTravelerSnapshot snapshot = null;
        if (Currency.EUR.name().equals(currency)) {
            snapshot = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());
        }

        // mallowlink transaction 생성
        String transactionId = mallowlinkService.increaseAndGetTransactionId();
        log.info("mallowlink transactionId:{}", transactionId);
        RemittanceMallowlink remittanceMallowlink = RemittanceMallowlink.of(remittance, transactionId);
        remittanceMallowlinkRepositoryService.insertRemittanceMallowlink(remittanceMallowlink);

        // mallowlink 송금 요청
        RemittanceRequestV3 request = RemittanceRequestV3.of(transactionId, remittance, traveler, snapshot);
        remittanceClient.request(request);

        if (CountryCode.JP.getCode().equals(remittance.getFromCd())) {
            globalQueueService.sendMallowlinkTransactionId(TransactionRecord.RelatedTxnType.REMITTANCE, remittance.getId(), remittanceMallowlink.getTransactionId());
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Remittance reRegisterRemittance(long remitId, Remittance params) throws CashmallowException {
        String message = "송금 신청이 재등록 되었습니다(RC), [AML 조회후 진행 필요]";
        Remittance remittance = remittanceService.reRegisterRemittance(remitId, params);

        boolean isVerifiedAML = false;
        try {
            List<Map<String, Object>> resultMap = octaAmlService.validateRemittanceAmlList(remittance);
            if (resultMap.isEmpty()) {
                isVerifiedAML = true;
                message = "송금 신청이 재등록 되었습니다. (AML 문제없음)";
                remittance = remittanceService.updateRemittanceAmlVerified(remittance);
            }
            if (CountryCode.JP.getCode().equals(remittance.getFromCd())) {
                globalQueueService.reRegisterRemittance(remittance);
            }
        } catch (Exception e) {
            alarmService.e("AML 조회 오류", e.getMessage());
            log.error(e.getMessage(), e);
        }

        // 송금신청 재등록한 건에 대해서 메시지 보낸다.
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
        String sb = message +
                "\n국가:" + remittance.getFromCd() + ", 은행:" + remittance.getBankAccountId() +
                "\n유저ID:" + traveler.getUserId() + ", 은행:" + traveler.getBankName() + ", 이름:" + traveler.getAccountName() + ", 코드:" + traveler.getAccountNo() + ", 금액:" + remittance.getFromAmt() +
                "\n적용환율:" + remittance.getExchangeRate() +
                "\n국가:" + remittance.getFromCd() + ", 금액:" + remittance.getFromAmt() + ", 수수료:" + remittance.getFee() +
                "\n대상국가:" + remittance.getToCd() + ", 대상금액:" + remittance.getToAmt();
        alarmService.aAlert("송금신청 재등록(RC)", sb, userRepositoryService.getUserByUserId(traveler.getUserId()));

        // AML 통과시 자동 재송금
        if (isVerifiedAML) {
            requestRemittance(remittance);
        }

        return remittance;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public String webhookResult(WebhookResultRequest result) throws CashmallowException {
        return requestResult(result);
    }


    private String requestResult(WebhookResultRequest result) throws CashmallowException {
        String transactionId = result.clientTransactionId();
        RemittanceMallowlink remittanceMallowlink = remittanceMallowlinkRepositoryService.getRemittanceMallowlinkByTransactionId(transactionId);

        // 진행중인 송금건은 없지만, 과거의 이력이 남아있는지 확인.
        if (ObjectUtils.isEmpty(remittanceMallowlink)) {
            RemittanceMallowlinkStatus mallowlinkStatus = remittanceMallowlinkRepositoryService.getRecentRemittanceMallowlinkStatus(transactionId);
            if (ObjectUtils.isEmpty(mallowlinkStatus)) {
                throw new MallowlinkException(MallowlinkExceptionType.TRANSACTION_NOT_FOUND);
            }

            switch (mallowlinkStatus.getStatus()) {
                case SUCCESS, INVALID_RECEIVER, FAIL -> {
                    log.info("이미 처리된 송금: transactionId={}, remitId={}, status={}", transactionId, mallowlinkStatus.getRemitId(), mallowlinkStatus.getStatus());
                    return "SUCCESS";
                }
                case REQUEST -> {
                    log.error("비 정상적인 송금 이력: transactionId={}, remitId={}, status={}", transactionId, mallowlinkStatus.getRemitId(), mallowlinkStatus.getStatus());
                    return "SUCCESS";
                }
            }
        }

        log.info("result:{}", JsonStr.toJson(result));

        Remittance remittance = remittanceMallowlinkRepositoryService.getRemittanceByMallowlinkTransactionId(transactionId)
                .orElseThrow(() -> new MallowlinkException(MallowlinkExceptionType.TRANSACTION_NOT_FOUND));

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
        User user = userRepositoryService.getUserByUserId(traveler.getUserId());

        remittanceMallowlinkRepositoryService.updateRemittanceMallowlinkStatus(remittanceMallowlink, MallowlinkRemittanceStatus.valueOf(result.status().name()));

        String message = "";
        switch (result.status()) {
            case SUCCESS -> {
                if (remittance.getRemitStatus().equals(Remittance.RemittanceStatusCode.CF)) {
                    message = """
                            [ADMIN] 여행자 송금 확인 필요. 이미 완료 처리된 데이터 입니다.
                            현재 remit_status: %s, remit_id: %d, user_id: %d
                            ML-ClientTransactionId: %s
                            """.formatted(remittance.getRemitStatus(), remittance.getId(), user.getId(),
                            transactionId);
                    log.info("이미 완료된 송금:{}", message);
                    return "SUCCESS";
                } else if (!Remittance.RemittanceStatusCode.DP.equals(remittance.getRemitStatus())) {
                    message = """
                            [ADMIN] 여행자 송금 확인 필요. ML에서 송금 완료 노티를 받았으나 완료 처리할 수 있는 상태가 아닙니다.
                            현재 remit_status: %s, remit_id: %d,user_id: %d
                            ML-ClientTransactionId: %s
                            """.formatted(remittance.getRemitStatus(), remittance.getId(), user.getId(), transactionId);
                    alarmService.aAlert("Mallowlink", message, user);
                    return "SUCCESS";
                }

                // 송금 완료 처리
                remittance.setRemitStatus(Remittance.RemittanceStatusCode.CF);
                remittanceRepositoryService.updateRemittance(remittance);
                remittanceRepositoryService.insertRemitStatus(remittance.getId(), Remittance.RemittanceStatusCode.CF);

                Country fromCountry = countryService.getCountry(remittance.getFromCd());
                Country toCountry = countryService.getCountry(remittance.getToCd());

                alarmService.aAlert("Mallowlink", """
                        [ADMIN] 여행자 송금 완료
                         거래번호: %s
                         유저ID: %s
                         fromCountry: %s
                         toCountry: %s
                         ML-ClientTransactionId: %s
                        """.formatted(remittance.getId().toString(), user.getId().toString(), fromCountry.getKorName(), toCountry.getKorName(), transactionId), user);

                // CF 건 AML 거래 정보 전송(송금)
                sendRemittance2OctaAML(remittance, user, fromCountry, toCountry, traveler);
                if (fromCountry.getCode().equals(CountryCode.JP.getCode())) {
                    globalQueueService.sendRemittanceCompleted(remittance.getId());
                }

                notificationService.sendEmailConfirmRemittance(user, traveler, remittance, fromCountry, toCountry);
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.CF, 0L, "");
            }
            case INVALID_RECEIVER, FAIL -> {
                // 재송금이 가능한 케이스
                if (!Remittance.RemittanceStatusCode.DP.equals(remittance.getRemitStatus())) {
                    // Send Slack to Admin
                    message = """
                            [ADMIN] 여행자 송금 실패. DB의 현재 상태(%s)가 'RR'로 설정할 수 있는 상태가 아님. 확인 후 조치 필요
                            (fail_reason) : RECEIVER_ERROR (remit_id) : %d (user_id) : %d
                            ML-ClientTransactionId: %s
                            """.formatted(remittance.getRemitStatus(), remittance.getId(), user.getId(), transactionId);
                    alarmService.aAlert("Mallowlink", message, user);
                }

                updateRemitStatusRR(remittance);

                // Send FCM to traveler
                Locale locale = new Locale(user.getLangKey().substring(0, 2));

                // Send Slack to Admin
                message = """
                        [ADMIN] 여행자 송금 실패(RR). 확인 후 조치 필요
                        송금 거래번호: %d, 유저ID: %d
                        결과 상태: %s
                        실패 이유: %s
                        ML-ClientTransactionId: %s
                        """.formatted(remittance.getId(), user.getId(), result.status(), result.errorMessage(), transactionId);
                alarmService.aAlert("Mallowlink", message, user);

                message = messageSource.getMessage("REMITTANCE_RECEIVER_INFO_WRONG", null, "The beneficiary's information is incorrect.", locale);
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.RR, 0L, message);
            }
            // case FAIL -> {
            //     // 재송금이 불가능한 케이스
            //     message = "[ADMIN] 여행자 송금 실패. Admin에서 취소해야 함."
            //             + "\nclientTransactionId:" + transactionId + " status:" + result.status()
            //             + "\nremit_id: " + remittance.getId() + " user_id: " + user.getId()
            //             + "\nerror_message: " + result.errorMessage();
            //     alarmService.aAlert("Mallowlink", message);
            // }
            default -> throw new CashmallowException("Unexpected value: " + result.status());
        }

        return "SUCCESS";
    }

    /**
     * 송금 신청 완료시 거래내역 AML 전송
     *
     * @param remittance
     * @param user
     * @param fromCountry
     * @param toCountry
     * @param traveler
     */
    private void sendRemittance2OctaAML(Remittance remittance, User user, Country fromCountry, Country toCountry, Traveler traveler) {
        Map<String, CurrencyRate> rates = currencyService.getCurrencyRateByKrwAndUsd(fromCountry.getIso4217(), remittance.getCreatedDate());

        // AccountBase
        String prefix = "RM";
        AmlAccountBase amlAccountBase = AmlAccountBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + remittance.getId().toString()) // 송금 거래 번호
                .customerNo(user.getId().toString(), user.getCountryCode().name())
                .accountStsCd("01")
                .currencyCd(fromCountry.getIso4217())
                .prodCd("03")
                .prodRaCd("03")
                .prodRepCd("01")
                .accountOpenDd(remittance.getCreatedDate())
                // .closeDd("99991231") // default value is 99991231
                .accountOpenPurposeCd(remittance.getRemitPurpose())
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
                .accountNo(prefix + remittance.getId().toString()) // 송금 거래 번호
                // .prodCd("03") // 상품 코드 - default value is "03" mean "해외송금"
                // .prodNm("해외송금") // 상품명 - default value is "해외송금"
                .build();

        // AccountTranBase
        AmlAccountTranBase amlAccountTranBase = AmlAccountTranBase.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + remittance.getId().toString()) // 송금 거래 번호
                .prodCd("03")
                .tranDd(remittance.getCreatedDate()) // 거래 시간 (YYYYMMDDHHMMSS)
                .tranSeqNo(1) // 거래_일련_번호
                .customerNo(user.getId().toString(), user.getCountryCode().name())
                .tranTime(remittance.getCreatedDate())
                // .tranChannelCd("19") // 거래 방법 OR 경로 - default value is "19" mean "모바일"
                // .tranChannelNm("모바일") // default value is "모바일"
                // .tranWayCd("05") // 재화의 종류 - default value is "05" mean "외환(해외송금)"
                // .tranWayNm("외환(해외송금)") // default value is "외환(해외송금)"
                // .tranKindCd("05") // 거래 분류 - default value is "05" mean "송금(해외)"
                // .tranKindNm("송금, 이체영수") // default value is "해외송금"
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
                .tranAmt(remittance.getFromAmt().multiply(rates.get("KRW").getRate()).setScale(0, RoundingMode.DOWN)) // 거래금액(송금) - ex) HKD -> KRW, 원화라 소수점 제거
                .wonTranAmt(remittance.getToAmt()) // 원화환산금액 - ex) toAmt 화폐 고정, 소수점 제거
                .fexTranAmt(remittance.getFromAmt()) // 외화거래금액 - ex) HKD
                .usdExchangeAmt(remittance.getFromAmt().multiply(rates.get("USD").getRate())) // 달러환산금액 - ex) HKD -> USD
                .tranPurposeCd(remittance.getRemitPurpose()) // 거래의 목적코드 - default value is "03" mean "상속증여성 거래"
                // .tranPurposeNm("상속증여성 거래") // default value is "상속증여성 거래"
                .fexTranPurposeCd(remittance.getRemitPurpose()) // 외화거래의 목적코드 - default value is "01" mean "증여송금"
                // .fexTranPurposeNm("증여송금") // default value is "증여송금"
                .build();

        // AccountTranSendReceipt
        AmlAccountTranSendReceipt amlAccountTranSendReceipt = AmlAccountTranSendReceipt.builder()
                // .systemDiv("CASHMFT")
                .accountNo(prefix + remittance.getId().toString()) // 송금 거래 번호
                .prodCd("03") // 상품 코드 - default value is "03" mean "해외송금"
                .tranDd(remittance.getCreatedDate())
                .tranSeqNo(1) // 거래_일련_번호
                // .repayDivisionCd("01") // 파트너 구분(송금) - default value is "01" mean "계좌번호"
                .rltFinanOrgCountryCd(toCountry.getIso3166()) // 상대_은행_국가_코드 - 수취인
                .rltFinanOrgCd(remittance.getReceiverBankCode()) // 상대_은행_코드 - 수취인 - default value is "001" mean "한국은행"
                .rltFinanOrgNm(remittance.getReceiverBankName()) // 상대_은행_명 - 수취인 - default value is "한국은행"
                .rltaccNo(remittance.getReceiverBankAccountNo()) // 수취인 계좌번호
                .rltaccOwnerDiv("01") // 수취인 계좌주명 구분 - default value is "01" mean "개인"
                .rltaccOwnerDivNm("개인") // default value is "개인"
                .rltaccOwnerNm(remittance.getReceiverFirstName() + " " + remittance.getReceiverLastName()) // 수취인 계좌주명
                .rltaccOwnerPhoneNo(remittance.getReceiverPhoneNo()) // 수취인(소유자) 연락처
                .rltOpenPurposeCd(remittance.getRemitPurpose()) // 상대_계좌_개설_목적_코드 - default value is "99" mean "기타"
                // .rltOpenPurposeNm("기타") // default value is "기타"
                .build();

        OctaAMLKYCRequest request = new OctaAMLKYCRequest(amlAccountBase, amlProdBase, amlAccountTranBase, amlAccountTranSendReceipt);
        octaAMLKYCService.execute(request);
    }

    // 수신자 오류
    private void updateRemitStatusRR(Remittance remittance) throws CashmallowException {
        // Update remittance status
        remittance.setRemitStatus(Remittance.RemittanceStatusCode.RR);
        remittanceRepositoryService.updateRemittance(remittance);

        remittanceRepositoryService.insertRemitStatus(remittance.getId(), Remittance.RemittanceStatusCode.RR);

        if (CountryCode.JP.getCode().equals(remittance.getFromCd())) {
            globalQueueService.failRemittance(remittance.getId());
        }

    }
}
