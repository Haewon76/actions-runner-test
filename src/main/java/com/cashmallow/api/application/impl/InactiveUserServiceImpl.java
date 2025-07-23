package com.cashmallow.api.application.impl;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.Exchange.ExStatus;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.inactiveuser.*;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser.InactiveType;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.remittance.RemitReceiverAml;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.remittance.RemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.model.withdrawalpartnercalc.WithdrawalPartnerCalc;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.aml.octa.OctaAmlService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.openbank.service.OpenbankServiceImpl;
import com.cashmallow.common.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;


@Service
@SuppressWarnings({"unused", "deprecation"})
public class InactiveUserServiceImpl {
    private final Logger logger = LoggerFactory.getLogger(InactiveUserServiceImpl.class);

    private static final String DEL_ACCOUNT_INACTIVE_USER = "DEL_ACCOUNT_INACTIVE_USER";
    private static final String DEL_ACCOUNT_REMITTANCE_IN_PROGRESS = "DEL_ACCOUNT_REMITTANCE_IN_PROGRESS";
    private static final String DEL_ACCOUNT_EXCHANGE_IN_PROGRESS = "DEL_ACCOUNT_EXCHANGE_IN_PROGRESS";
    private static final String DEL_ACCOUNT_CASHOUT_IN_PROGRESS = "DEL_ACCOUNT_CASHOUT_IN_PROGRESS";
    private static final String DEL_ACCOUNT_REFUND_IN_PROGRESS = "DEL_ACCOUNT_REFUND_IN_PROGRESS";
    private static final String DEL_ACCOUNT_REFUND_CALC_IN_PROGRESS = "DEL_ACCOUNT_REFUND_CALC_IN_PROGRESS";
    private static final String DEL_ACCOUNT_WALLET_NOT_EMPTY = "DEL_ACCOUNT_WALLET_NOT_EMPTY";
    private static final String DEL_ACCOUNT_PAYBACK_IN_PROGRESS = "DEL_ACCOUNT_PAYBACK_IN_PROGRESS";

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private PartnerServiceImpl storekeeperService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private PartnerCalcServiceImpl storekeeperCalcService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OctaAmlService octaAmlService;

    @Autowired
    private InactiveUserMapper inaUserMapper;

    @Autowired
    private OpenbankServiceImpl openbankService;

    @Autowired
    private GlobalQueueService globalQueueService;

    public InactiveUserServiceImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.application.impl.UserV2Service#deactivateUser(long,
     * long)
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public User deactivateUser(long userId, long lastModifier, InactiveType inactiveType) throws CashmallowException {
        String method = "deactivateUser()";
        int rowCnt;

        logger.info("{}: userId={}, lastModifier={}", method, userId, lastModifier);

        // user 조회
        User user = userRepositoryService.getUserByUserId(userId);

        if (Boolean.FALSE.equals(user.isActivated())) {
            logger.error("{}: The user is inactive. userId={}", method, userId);
            throw new CashmallowException(DEL_ACCOUNT_INACTIVE_USER);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler != null) {
            long travelerId = traveler.getId();

            // Stop deactivate if the remittance is in progress.
            Remittance remittance = remittanceRepositoryService.getRemittanceInprogress(travelerId);
            if (remittance != null) {
                logger.error("{}: Remittance is in progress. userId={}", method, userId);
                throw new CashmallowException(DEL_ACCOUNT_REMITTANCE_IN_PROGRESS);
            }

            // Get remittance list
            int count = remittanceRepositoryService.countRemittanceListByTravelerId(travelerId);
            List<Remittance> remitList = remittanceRepositoryService.getRemittanceListByTravelerId(travelerId, 0, count);

            // exchange 진행 중 상태가 있으면 탈퇴 불가
            List<Exchange> exchangeList = exchangeRepositoryService.getExchangeListByTravelerId(travelerId);

            for (Exchange ex : exchangeList) {
                if (ExStatus.OP.toString().equals(ex.getExStatus())) {
                    logger.error("{}: Exchange is in progress. userId={}", method, userId);
                    throw new CashmallowException(DEL_ACCOUNT_EXCHANGE_IN_PROGRESS);
                }
            }

            // cash_out 진행 중 상태가 있으면 탈퇴 불가
            List<CashOut> coList = cashOutService.getCashOutOpListByTravelerId(travelerId);

            if (!coList.isEmpty()) {
                // 조회되는 데이터가 있으면 진행 중인 환전이 있으므로 탈퇴 처리 불가
                logger.error("{}: Withdrawal is in progress. Check the withdrawals of the user and the store settlement. userId={}",
                        method, userId);
                throw new CashmallowException(DEL_ACCOUNT_CASHOUT_IN_PROGRESS);
            }

            // refund 진행 중 상태가 있으면 탈퇴 불가
            List<NewRefund> inProgressRefundList = refundRepositoryService.getNewRefundListInProgressByTravelerId(travelerId);

            if (!inProgressRefundList.isEmpty()) {
                logger.error("{}: There is a refund in progress. userId={}", method, traveler.getUserId());
                throw new CashmallowException("REFUND_ERROR_REQUEST_PROCESS_IN_REFUND");
            }

            // traveller wallet 잔액 있으면 탈퇴 불가
            List<TravelerWallet> twList = walletRepositoryService.getTravelerWalletList(userId);

            if (twList != null) {
                for (TravelerWallet tw : twList) {

                    if (tw.geteMoney().compareTo(new BigDecimal(0)) > 0) {
                        logger.error("{}: The traveler has e-money in wallet. userId={}", method, userId);
                        throw new CashmallowException(DEL_ACCOUNT_WALLET_NOT_EMPTY);
                    }

                    if (tw.getrMoney().compareTo(new BigDecimal(0)) > 0) {
                        logger.error("{}: The traveler has r-money in wallet. userId={}", method, userId);
                        throw new CashmallowException(DEL_ACCOUNT_REFUND_IN_PROGRESS);
                    }
                }
            }

            List<NewRefund> refundList = refundRepositoryService.getNewRefundListByTravelerId(travelerId);

            Map<String, String> params = new HashMap<String, String>();
            params.put("travelerId", String.valueOf(travelerId));

            List<RemitReceiverAml> rAmlList = remittanceRepositoryService.getRemitReceiverAml(params);

            // 여행자 관련 개인정보 분리 보관
            insertInactiveTraveler(user, traveler, remitList, exchangeList, refundList, rAmlList);

            // Refund table 개인정보 삭제
            for (NewRefund refund : refundList) {
                refund.setTrAccountNo("*");
                refund.setTrAccountName("*");
                rowCnt = refundRepositoryService.updateNewRefund(refund);

                if (rowCnt != 1) {
                    logger.error("{}: Cannot update the privacy info for Refund. userId={}", method, userId);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }

            // Exchange table 에 있는 개인정보 삭제
            for (Exchange ex : exchangeList) {
                ex.setIdentificationNumber("*");
                ex.setTrAccountNo("*");
                ex.setTrAccountName("*");
                ex.setTrAccountBankbookPhoto(null);
                ex.setTrPhoneNumber("*");
                ex.setTrAddress("*");
                ex.setTrAddressSecondary("*");
                ex.setTrAddressPhoto(null);
                rowCnt = exchangeRepositoryService.updateExchange(ex);

                if (rowCnt != 1) {
                    logger.error("{}: Cannot update the privacy info for Exchange. userId={}", method, userId);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }

            // remittance, remittance_traveler_snapshot table 에 있는 개인정보 삭제
            for (Remittance rm : remitList) {
                anonymizeRemittance(rm);
            }

            for (RemitReceiverAml receiverAml : rAmlList) {
                receiverAml.anonymize();
                remittanceRepositoryService.updateRemitReceiverAml(receiverAml);
            }

            // traveler table 개인정보 삭제
            traveler.setEnFirstName("*");
            traveler.setEnLastName("*");
            traveler.setLocalFirstName("*");
            traveler.setLocalLastName("*");
            traveler.setIdentificationNumber("*");
            traveler.setCertificationPhoto(null);
            traveler.setAccountNo("*");
            traveler.setAccountName("*");
            traveler.setContactId("*");
            traveler.setAddress("*");
            traveler.setAddressSecondary("*");
            traveler.setAddressPhoto(null);

            rowCnt = travelerRepositoryService.updateTraveler(traveler);
            if (rowCnt != 1) {
                logger.error("{}: Cannot update the privacy info for traveler. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // 오픈뱅킹 가입자는 오픈뱅킹 탈퇴도 같이 함.
            openbankService.closeUser(travelerId);
        }

        // storekeeper 개인정보 분리 보관
        WithdrawalPartner withdrawalPartner = storekeeperService.getWithdrawalPartnerByUserId(userId);

        if (withdrawalPartner != null) {

            long storekeeperId = withdrawalPartner.getId();

            // 가맹점에 신청된 인출 건이 있으면 탈퇴 처리 중단
            List<CashOut> coList = cashOutService.getCashOutOpListByStorekeeperId(storekeeperId);
            if (!coList.isEmpty()) {
                logger.error("{}: There is a withdrawal in progress. userId={}", method, userId);
                throw new CashmallowException(DEL_ACCOUNT_CASHOUT_IN_PROGRESS);
            }

            // retrieve storekeeper_calc in progress
            List<WithdrawalPartnerCalc> scOpList = storekeeperCalcService
                    .getWithdrawalPartnerCalcOpListByWithdrawalPartnerId(storekeeperId);
            if (!scOpList.isEmpty()) {
                logger.error("{}: There is a storekeeper calc in progress. userId={}", method, userId);
                throw new CashmallowException(DEL_ACCOUNT_PAYBACK_IN_PROGRESS);
            }

            // retrieve storekeeper_calc in progress
            List<WithdrawalPartnerCalc> scList = storekeeperCalcService.getWithdrawalPartnerCalcListByWithdrawalPartnerId(storekeeperId);

            // 가맹정 관련 개인정보 분리 보관
            insertInactiveStorekeeper(withdrawalPartner, scList);

            // storekeeper_calc 개인정보 삭제
            for (WithdrawalPartnerCalc sc : scList) {
                sc.setBankAccountNo("*");
                sc.setBankAccountName("*");
                rowCnt = storekeeperCalcService.updateWithdrawalPartnerCalc(sc);

                if (rowCnt != 1) {
                    logger.error("{}: Cannot update the privacy info for WithdrawalPartner Calc. userId={}", method, userId);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }

            // storekeeper 개인정보 삭제
            withdrawalPartner.setBusinessNo("*");
            withdrawalPartner.setBusinessPhoto(null); // file 경로 오류 가능성이 있으므로 null로 처리
            withdrawalPartner.setAccountNo("*");
            withdrawalPartner.setAccountName("*");
            withdrawalPartner.setShopContactNumber("*");
            rowCnt = storekeeperService.updateWithdrawalPartner(withdrawalPartner);

            if (rowCnt != 1) {
                logger.error("{}: Cannot update the privacy info for WithdrawalPartner. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        // (old) 사용자 접속 토큰 삭제
        authService.deleteAccessToken(user.getLogin());

        // delete refresh token
        authService.deleteRefreshToken(userId);

        // user 개인정보 분리 보관
        insertInactiveUser(user, inactiveType);

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dt = f.format(new Date());

        // user 개인정보 삭제
        // if 'DEL' then delete login and password.
        // else 'DOR' then do not delete login. Must to prevent to sign up with same
        // email
        if (InactiveType.DEL.equals(inactiveType)) {
            user.setLogin("*" + dt);
        }
        user.setEmail("*");
        user.setFirstName("*");
        user.setLastName("*");
        user.setProfilePhoto(null);
        user.setActivated(false);
        user.setLastModifier(lastModifier);
        user.setLastModifiedDate(new Timestamp(new Date().getTime()));
        user.setDeactivatedDate(new Timestamp(new Date().getTime()));
        user.setPhoneNumber("*");

        rowCnt = userRepositoryService.updateUser(user);

        if (rowCnt != 1) {
            logger.error("{}: Cannot update the privacy info for user. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // user 탈퇴시 탈퇴처리 큐전송
        globalQueueService.sendTravelerLeave(user);

        return user;
    }

    private void anonymizeRemittance(Remittance remittance) throws CashmallowException {
        remittance.anonymize();
        remittanceRepositoryService.updateRemittance(remittance);

        RemittanceTravelerSnapshot rmTr = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());
        rmTr.anonymize();
        remittanceRepositoryService.updateRemitTravelerSnapshot(rmTr);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.application.impl.UserV2Service#activateUser(long,
     * long)
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public User activateUser(long userId, long lastModifier) throws CashmallowException {

        int rows;

        // user
        InactiveUser inaUser = getInactiveUser(userId);

        if (inaUser == null) {
            String message = "Cannot find Deactivated User info by user ID (user ID : " + userId + ")";
            logger.info("activateUser(): message={}", message);
            throw new CashmallowException(message);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        user.setLogin(inaUser.getLogin());
        user.setFirstName(inaUser.getFirstName());
        user.setLastName(inaUser.getLastName());
        user.setEmail(inaUser.getEmail());
        user.setProfilePhoto(inaUser.getProfilePhoto());
        user.setActivated(true);
        user.setLastModifier(lastModifier);
        user.setLastModifiedDate(new Timestamp(new Date().getTime()));
        user.setDeactivatedDate(null);
        user.setPhoneNumber(inaUser.getPhoneNumber());

        rows = userRepositoryService.updateUser(user);

        if (rows != 1) {
            String msg = "Cannot update the privacy info for User (user ID : " + userId + ")";
            logger.error(msg);
            throw new CashmallowException(msg);
        }

        // traveler
        InactiveTraveler dTraveler = getInactiveTraveler(userId);

        if (dTraveler != null) {
            long travelerId = dTraveler.getId();

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            traveler.setId(dTraveler.getId());
            traveler.setUserId(dTraveler.getUserId());
            traveler.setIdentificationNumber(dTraveler.getIdentificationNumber());
            traveler.setEnFirstName(dTraveler.getEnFirstName());
            traveler.setEnLastName(dTraveler.getEnLastName());
            traveler.setLocalFirstName(dTraveler.getLocalFirstName());
            traveler.setLocalLastName(dTraveler.getLocalLastName());
            traveler.setCertificationPhoto(dTraveler.getCertificationPhoto());
            traveler.setAccountNo(dTraveler.getAccountNo());
            traveler.setAccountName(dTraveler.getAccountName());
            traveler.setAccountBankbookPhoto(dTraveler.getAccountBankbookPhoto());
            traveler.setContactType(dTraveler.getContactType());
            traveler.setContactId(dTraveler.getContactId());
            traveler.setAddress(dTraveler.getAddress());
            traveler.setAddressSecondary(dTraveler.getAddressSecondary());
            traveler.setAddressPhoto(dTraveler.getAddressPhoto());

            rows = travelerRepositoryService.updateTraveler(traveler);

            if (rows != 1) {
                String msg = "Cannot update the privacy info for Refund Calc (user ID : " + userId + ")";
                logger.error(msg);
                throw new CashmallowException(msg);
            }

            // remittance
            List<InactiveRemittance> inaRemitList = inaUserMapper.getInactiveRemittanceList(travelerId);
            for (InactiveRemittance ir : inaRemitList) {
                Remittance r = remittanceRepositoryService.getRemittanceByRemittanceId(ir.getId());
                BeanUtils.copyProperties(ir, r, "id", "travelerId", "createdDate");

                remittanceRepositoryService.updateRemittance(r);
            }

            // remittance_traveler_snapshot
            List<InactiveRemittanceTravelerSnapshot> inaRemitTrList = inaUserMapper.getInactiveRemittanceTravelerSnapshotList(travelerId);
            for (InactiveRemittanceTravelerSnapshot irts : inaRemitTrList) {
                RemittanceTravelerSnapshot rts = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(irts.getRemitId());
                BeanUtils.copyProperties(irts, rts, "remitId", "travelerId", "createdDate");

                remittanceRepositoryService.updateRemitTravelerSnapshot(rts);
            }

            List<InactiveRemitReceiverAml> inaRemitReceiverAmlList = inaUserMapper.getInactiveRemitReceiverAml(travelerId);
            for (InactiveRemitReceiverAml iRemitReceiverAml : inaRemitReceiverAmlList) {

                RemitReceiverAml remitReceiverAml = remittanceRepositoryService.getRemitReceiverAmlById(iRemitReceiverAml.getRemitReceiverAmlId());

                remitReceiverAml.setTravelerId(iRemitReceiverAml.getTravelerId());
                remitReceiverAml.setReceiverFirstName(iRemitReceiverAml.getReceiverFirstName());
                remitReceiverAml.setReceiverLastName(iRemitReceiverAml.getReceiverLastName());
                remitReceiverAml.setBirthDate(iRemitReceiverAml.getBirthDate());
                remitReceiverAml.setAmlSearchId(iRemitReceiverAml.getAmlSearchId());

                remittanceRepositoryService.updateRemitReceiverAml(remitReceiverAml);
            }

            // exchange
            List<InactiveExchange> dExchangeList = getInactiveExchangeList(travelerId);
            Exchange exchange = null;
            for (InactiveExchange dExchange : dExchangeList) {
                exchange = exchangeRepositoryService.getExchangeByExchangeId(dExchange.getId());
                exchange.setIdentificationNumber(dExchange.getIdentificationNumber());
                exchange.setTrAccountNo(dExchange.getTrAccountNo());
                exchange.setTrAccountName(dExchange.getTrAccountName());
                exchange.setTrAccountBankbookPhoto(dExchange.getTrAccountBankbookPhoto());
                exchange.setTrPhoneNumber(dExchange.getTrPhoneNumber());
                exchange.setTrAddress(dExchange.getTrAddress());
                exchange.setTrAddressSecondary(dExchange.getTrAddressSecondary());
                exchange.setTrAddressPhoto(dExchange.getTrAddressPhoto());

                rows = exchangeRepositoryService.updateExchange(exchange);

                if (rows != 1) {
                    String msg = "Cannot update the privacy info for Exchange (user ID : " + userId + ")";
                    logger.error(msg);
                    throw new CashmallowException(msg);
                }
            }

            // refund
            List<InactiveRefund> dRefundList = getInactiveRefundCalcList(travelerId);
            NewRefund refund = null;
            for (InactiveRefund dRefund : dRefundList) {
                refund = refundRepositoryService.getNewRefundById(dRefund.getId());
                refund.setId(dRefund.getId());
                refund.setTravelerId(dRefund.getTravelerId());
                refund.setTrAccountNo(dRefund.getAccountNo());
                refund.setTrAccountName(dRefund.getAccountName());

                rows = refundRepositoryService.updateNewRefund(refund);

                if (rows != 1) {
                    String msg = "Cannot update the privacy info for Refund (user ID : " + userId + ")";
                    logger.error(msg);
                    throw new CashmallowException(msg);
                }
            }
        }

        // storekeeper
        InactiveWithdrawalPartner dWithdrawalPartner = getInactiveStorekeeper(userId);

        if (dWithdrawalPartner != null) {
            long storekeeperId = dWithdrawalPartner.getId();

            WithdrawalPartner withdrawalPartner = storekeeperService.getWithdrawalPartnerByUserId(userId);
            withdrawalPartner.setId(dWithdrawalPartner.getId());
            withdrawalPartner.setUserId(dWithdrawalPartner.getUserId());
            withdrawalPartner.setBusinessNo(dWithdrawalPartner.getBusinessNo());
            withdrawalPartner.setBusinessPhoto(dWithdrawalPartner.getBusinessPhoto());
            withdrawalPartner.setAccountNo(dWithdrawalPartner.getAccountNo());
            withdrawalPartner.setAccountName(dWithdrawalPartner.getAccountName());
            withdrawalPartner.setShopContactNumber(dWithdrawalPartner.getShopContactNumber());

            rows = storekeeperService.updateWithdrawalPartner(withdrawalPartner);

            if (rows != 1) {
                String msg = "Cannot update the privacy info for Storekeeper (user ID : " + userId + ")";
                logger.error(msg);
                throw new CashmallowException(msg);
            }

            // storekeeper_calc
            List<InactiveWithdrawalPartnerCalc> dsCalcList = getInactiveStorekeeperCalcList(storekeeperId);
            WithdrawalPartnerCalc sCalc = null;
            for (InactiveWithdrawalPartnerCalc dsCalc : dsCalcList) {
                sCalc = storekeeperCalcService.getWithdrawalPartnerCalc(dsCalc.getId());
                sCalc.setId(dsCalc.getId());
                sCalc.setWithdrawalPartnerId(dsCalc.getWithdrawalPartnerId());
                sCalc.setBankAccountNo(dsCalc.getBankAccountNo());
                sCalc.setBankAccountName(dsCalc.getBankAccountName());

                storekeeperCalcService.updateWithdrawalPartnerCalc(sCalc);

                if (rows != 1) {
                    String msg = "Cannot update the privacy info for Storekeeper Calc (user ID : " + userId + ")";
                    logger.error(msg);
                    throw new CashmallowException(msg);
                }
            }
        }

        // 분리보관된 개인정보 삭제
        deleteInactiveInfo(userId);

        return user;
    }

    /**
     * 여행자 관련 개인정보 분리 보관
     *
     * @param traveler
     * @param exchangeList
     * @param refundList
     * @throws Exception
     */
    private void insertInactiveTraveler(User user,
                                        Traveler traveler,
                                        List<Remittance> remitList, List<Exchange> exchangeList,
                                        List<NewRefund> refundList, List<RemitReceiverAml> remitReceiverAmlList)
            throws CashmallowException {

        logger.info("insertDeactivatedTraveler() : 여행자 정보 분리 보관");

        InactiveTraveler inaTraveler = new InactiveTraveler();
        inaTraveler.setId(traveler.getId());
        inaTraveler.setUserId(traveler.getUserId());
        inaTraveler.setEnFirstName(traveler.getEnFirstName());
        inaTraveler.setEnLastName(traveler.getEnLastName());
        inaTraveler.setLocalFirstName(traveler.getLocalFirstName());
        inaTraveler.setLocalLastName(traveler.getLocalLastName());
        inaTraveler.setCertificationPhoto(traveler.getCertificationPhoto());
        inaTraveler.setIdentificationNumber(traveler.getIdentificationNumber());
        inaTraveler.setAccountNo(traveler.getAccountNo());
        inaTraveler.setAccountName(traveler.getAccountName());
        inaTraveler.setAccountBankbookPhoto(traveler.getAccountBankbookPhoto());
        inaTraveler.setContactType(traveler.getContactType());
        inaTraveler.setContactId(traveler.getContactId());
        inaTraveler.setCreatedDate(new Timestamp(new Date().getTime()));
        inaTraveler.setAddress(traveler.getAddress());
        inaTraveler.setAddressSecondary(traveler.getAddressSecondary());
        inaTraveler.setAddressPhoto(traveler.getAddressPhoto());

        inaUserMapper.insertInactiveTraveler(inaTraveler);

        // inactive_remittance
        for (Remittance r : remitList) {

            // inactive_remittance
            InactiveRemittance ir = new InactiveRemittance();
            BeanUtils.copyProperties(r, ir, "createdDate");

            ir.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));

            inaUserMapper.insertInactiveRemittance(ir);

            // inactive_remittance_traveler_snapshot
            RemittanceTravelerSnapshot rts = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(ir.getId());

            InactiveRemittanceTravelerSnapshot irts = new InactiveRemittanceTravelerSnapshot();
            BeanUtils.copyProperties(rts, irts, "createdDate");

            irts.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));

            inaUserMapper.insertInactiveRemittanceTravelerSnapshot(irts);
        }

        for (Exchange exchange : exchangeList) {
            InactiveExchange inaExchange = new InactiveExchange();
            inaExchange.setId(exchange.getId());
            inaExchange.setTravelerId(exchange.getTravelerId());
            inaExchange.setIdentificationNumber(exchange.getIdentificationNumber());
            inaExchange.setTrAccountNo(exchange.getTrAccountNo());
            inaExchange.setTrAccountName(exchange.getTrAccountName());
            inaExchange.setTrAccountBankbookPhoto(exchange.getTrAccountBankbookPhoto());
            inaExchange.setTrAddress(exchange.getTrAddress());
            inaExchange.setTrAddressSecondary(exchange.getTrAddressSecondary());
            inaExchange.setTrAddressPhoto(exchange.getTrAddressPhoto());
            inaExchange.setTrPhoneNumber(exchange.getTrPhoneNumber());
            inaExchange.setCreatedDate(new Timestamp(new Date().getTime()));

            inaUserMapper.insertInactiveExchange(inaExchange);
        }

        for (NewRefund refund : refundList) {
            InactiveRefund dRefund = new InactiveRefund();
            dRefund.setId(refund.getId());
            dRefund.setTravelerId(refund.getTravelerId());
            dRefund.setAccountNo(refund.getTrAccountNo());
            dRefund.setAccountName(refund.getTrAccountName());
            dRefund.setCreatedDate(new Timestamp(new Date().getTime()));

            inaUserMapper.insertInactiveRefund(dRefund);
        }

        for (RemitReceiverAml receiverAml : remitReceiverAmlList) {
            InactiveRemitReceiverAml dRemitReceiverAml = new InactiveRemitReceiverAml();

            dRemitReceiverAml.setRemitReceiverAmlId(receiverAml.getId());
            dRemitReceiverAml.setTravelerId(receiverAml.getTravelerId());
            dRemitReceiverAml.setReceiverFirstName(receiverAml.getReceiverFirstName());
            dRemitReceiverAml.setReceiverLastName(receiverAml.getReceiverLastName());
            dRemitReceiverAml.setBirthDate(receiverAml.getBirthDate());
            dRemitReceiverAml.setAmlSearchId(receiverAml.getAmlSearchId());

            inaUserMapper.insertInactiveRemitReceiverAml(dRemitReceiverAml);
        }

        // 트래블러 탈퇴에 대한 정보 JP로 전달
        globalQueueService.sendTravelerLeave(user);
    }

    /**
     * 가맹점 관련 개인정보 분리 보관
     *
     * @param storekeeper
     * @param storekeeperCalcList
     * @throws Exception
     */
    @Transactional
    public void insertInactiveStorekeeper(WithdrawalPartner storekeeper, List<WithdrawalPartnerCalc> storekeeperCalcList)
            throws CashmallowException {

        InactiveWithdrawalPartner dStorekeeper = new InactiveWithdrawalPartner();
        dStorekeeper.setId(storekeeper.getId());
        dStorekeeper.setUserId(storekeeper.getUserId());
        dStorekeeper.setBusinessNo(storekeeper.getBusinessNo());
        dStorekeeper.setBusinessPhoto(storekeeper.getBusinessPhoto());
        dStorekeeper.setAccountNo(storekeeper.getAccountNo());
        dStorekeeper.setAccountName(storekeeper.getAccountName());
        dStorekeeper.setShopContactNumber(storekeeper.getShopContactNumber());
        dStorekeeper.setCreatedDate(new Timestamp(new Date().getTime()));

        inaUserMapper.insertInactiveWithdrawalPartner(dStorekeeper);

        for (WithdrawalPartnerCalc sc : storekeeperCalcList) {
            InactiveWithdrawalPartnerCalc dsc = new InactiveWithdrawalPartnerCalc();
            dsc.setId(sc.getId());
            dsc.setWithdrawalPartnerId(sc.getWithdrawalPartnerId());
            dsc.setBankAccountNo(sc.getBankAccountNo());
            dsc.setBankAccountName(sc.getBankAccountName());
            dsc.setCreatedDate(new Timestamp(new Date().getTime()));

            inaUserMapper.insertInactiveWithdrawalPartnerCalc(dsc);
        }
    }

    /**
     * 사용자 개인정보 분리 보관
     *
     * @param user
     * @throws Exception
     */
    @Transactional
    public void insertInactiveUser(User user, InactiveType inactiveType) throws CashmallowException {

        InactiveUser inaUser = new InactiveUser();
        inaUser.setId(user.getId());
        inaUser.setLogin(user.getLogin());
        inaUser.setFirstName(user.getFirstName());
        inaUser.setLastName(user.getLastName());
        inaUser.setEmail(user.getEmail());
        inaUser.setProfilePhoto(user.getProfilePhoto());
        inaUser.setInactiveType(inactiveType);
        inaUser.setCreatedDate(new Timestamp(new Date().getTime()));
        inaUser.setPhoneNumber(user.getPhoneNumber());

        inaUserMapper.insertInactiveUser(inaUser);
    }

    /**
     * 분리보관된 사용자 정보 조회
     *
     * @param userId
     * @return
     */
    public InactiveUser getInactiveUser(long userId) {

        return inaUserMapper.getInactiveUser(userId);

    }

    /**
     * Find separated non-login user
     *
     * @param login
     * @return
     */
    public InactiveUser getSeparatedUserByLogin(String login) {

        Map<String, Object> params = new HashMap<>();
        params.put("login", login);
        params.put("inactiveType", InactiveType.DOR.toString());

        return inaUserMapper.getInactiveUserByLoginNInactiveType(params);

    }

    /**
     * 분리보관된 여행자 정보 조회
     *
     * @param userId
     * @return
     */
    public InactiveTraveler getInactiveTraveler(long userId) {

        return inaUserMapper.getInactiveTraveler(userId);

    }

    /**
     * 분리보관된 환전 정보 조회
     *
     * @param travelerId
     * @return
     */
    public List<InactiveExchange> getInactiveExchangeList(long travelerId) {

        return inaUserMapper.getInactiveExchangeList(travelerId);

    }

    /**
     * 분리보관된 환불 정보 조회
     *
     * @param travelerId
     * @return
     */
    public List<InactiveRefund> getInactiveRefundCalcList(long travelerId) {
        return inaUserMapper.getInactiveRefundList(travelerId);
    }

    /**
     * 분리보관된 가맹점 정보 조회
     *
     * @param userId
     * @return
     */
    public InactiveWithdrawalPartner getInactiveStorekeeper(long userId) {

        return inaUserMapper.getInactiveWithdrawalPartner(userId);

    }

    /**
     * 분리보관된 가맹점 정산 정보 조회
     *
     * @param storekeeperId
     * @return
     */
    public List<InactiveWithdrawalPartnerCalc> getInactiveStorekeeperCalcList(long storekeeperId) {

        return inaUserMapper.getInactiveWithdrawalPartnerCalcList(storekeeperId);

    }

    /**
     * 사용자 분리 보관 정보 삭제 탈퇴 회원 정보 복구 후 분리 보관된 데이터 삭제 시 사용
     *
     * @param userId
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void deleteInactiveInfo(long userId) throws CashmallowException {

        logger.info("deleteDeactivatedInfo() : 사용자 분리 보관 정보 삭제(userId={})", userId);

        InactiveWithdrawalPartner inaStorekeeper = inaUserMapper.getInactiveWithdrawalPartner(userId);
        if (inaStorekeeper != null) {
            long storekeeperId = inaStorekeeper.getId();
            inaUserMapper.deleteInactiveWithdrawalPartnerCalcList(storekeeperId);
            inaUserMapper.deleteInactiveWithdrawalPartner(userId);
        }

        InactiveTraveler inaTraveler = inaUserMapper.getInactiveTraveler(userId);
        if (inaTraveler != null) {
            long travelerId = inaTraveler.getId();
            inaUserMapper.deleteInactiveExchangeList(travelerId);
            inaUserMapper.deleteInactiveRefundList(travelerId);
            inaUserMapper.deleteInactiveTraveler(userId);
            inaUserMapper.deleteInactiveRemittanceList(travelerId);
            inaUserMapper.deleteInactiveRemittanceTravelerSnapshotList(travelerId);
            inaUserMapper.deleteInactiveRemitReceiverAml(travelerId);
        }

        inaUserMapper.deleteInactiveUser(userId);

    }
}
