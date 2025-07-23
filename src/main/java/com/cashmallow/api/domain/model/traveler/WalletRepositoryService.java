package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.system.JobPlan;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.traveler.dto.RequestCashOutVO;
import com.cashmallow.api.interfaces.traveler.dto.TravelerWalletVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static java.time.ZoneOffset.UTC;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class WalletRepositoryService {

    private final TravelerWalletMapper travelerWalletMapper;
    private final TravelerMapper travelerMapper;

    private final TravelerRepositoryService travelerRepositoryService;

    private final CountryService countryService;

    // -------------------------------------------------------------------------------
    // 7. 여행자 지갑
    // -------------------------------------------------------------------------------

    @Transactional(rollbackFor = CashmallowException.class)
    public void addTravelerWallet(Long travelerId, String rootCd, String countryCode, BigDecimal eMoney, Long creator, Long exchangeId)
            throws CashmallowException {

        try {
            if (creator == 0 || creator == null) {
                throw new CashmallowException("Invalid parameters");
            }

            JSONObject exchangeIds = new JSONObject();

            JSONArray ja = new JSONArray();

            ja.put(0, exchangeId);

            exchangeIds.put(Const.EXCHANGE_IDS, ja);

            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(rootCd, countryCode);

            BigDecimal toMaxCashOut = exchangeConfig.getToMaxWithdrawal();

            BigDecimal count = eMoney.divide(toMaxCashOut, 0, RoundingMode.UP);

            TravelerWallet travelerWallet = new TravelerWallet(travelerId, rootCd, countryCode, toMaxCashOut, creator);

            travelerWallet.setExchangeIds(exchangeIds.toString());

            for (int i = 0; count.intValue() > i; i++) {
                if (eMoney.compareTo(toMaxCashOut) > 0) {
                    travelerWallet.seteMoney(toMaxCashOut);

                    int affectedRows = travelerMapper.insertTravelerWallet(travelerWallet);

                    if (affectedRows != 1) {
                        throw new CashmallowException(INTERNAL_SERVER_ERROR);
                    }
                } else {
                    travelerWallet.seteMoney(eMoney);

                    int affectedRows = travelerMapper.insertTravelerWallet(travelerWallet);

                    if (affectedRows != 1) {
                        throw new CashmallowException(INTERNAL_SERVER_ERROR);
                    }
                }
                eMoney = eMoney.subtract(toMaxCashOut);
            }

        } catch (Exception e) {
            throw new CashmallowException(e.getMessage(), e);
        }
    }


    // 기능: 7.1. 여행자의 지갑 내역을 응답한다.
    public TravelerWallet getTravelerWallet(Long travelerWalletId) {
        String method = "getTravelerWallet()";
        log.info("{}: travelerWalletId={}", method, travelerWalletId);
        return travelerMapper.getTravelerWallet(travelerWalletId);
    }

    // 기능: 7.1. 여행자의 지갑 내역을 응답한다.
    public List<TravelerWallet> getTravelerWalletList(long userId) {
        String method = "getTravelerWallet()";
        log.info("{}: userId={}", method, userId);

        List<TravelerWallet> wallets = new ArrayList<>();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler != null) {
            Long travelerId = traveler.getId();
            wallets = getTravelerWalletListByTravelerId(travelerId);

            if (wallets == null || wallets.isEmpty()) {
                log.info("{}: 여행자 지갑 정보가 없습니다. userId={}", method, userId);
            }
        } else {
            log.info("{}: traveler 정보가 등록되지 않았습니다. userId={}", method, userId);
        }

        return wallets;
    }

    // 기능: 7.1. 여행자의 지갑 내역을 응답한다.
    public List<TravelerWallet> getTravelerWalletListByTravelerId(long travelerId) {
        log.info("getTravelerWalletListByTravelerId(): travelerId={}", travelerId);
        return travelerMapper.getTravelerWalletByTravelerId(travelerId);
    }

    public List<TravelerWalletVO> getTravelerWalletVoListByTravelerId(long travelerId) {
        log.info("getTravelerWalletVoListByTravelerId(): travelerId={}", travelerId);

        List<TravelerWallet> walletList = travelerMapper.getTravelerWalletByTravelerId(travelerId);

        if (walletList.isEmpty()) {
            return new ArrayList<>();
        }

        List<JobPlan> jobPlanList = countryService.getJobPlanListByFromCountry(walletList.get(0).getRootCd());

        List<TravelerWalletVO> walletVOs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        for (TravelerWallet wallet : walletList) {
            TravelerWalletVO walletVO = mapper.convertValue(wallet, TravelerWalletVO.class);

            long expiredCalendarDay = jobPlanList.stream().filter(jobPlan -> jobPlan.getToCountryCode().equals(wallet.getCountry()))
                    .findFirst().map(JobPlan::getWalletExpiredCalendarDay).orElse(9999L);

            walletVO.setExpired_date(getWalletExpireDate(wallet.getCreatedDate(), expiredCalendarDay, CountryCode.of(wallet.getRootCd())));
            walletVO.setExpired_day(expiredCalendarDay);
            walletVO.setWallet_id(wallet.getId());

            walletVOs.add(walletVO);
        }
        return walletVOs;
    }

    // 지갑생성일로부터 eMoney 만료일 계산, created/updated 날짜가 UTC이기 때문에 계산한 만기일도 UTC로 내려줌.
    private Timestamp getWalletExpireDate(Timestamp walletCreatedDate, long expiredCalendarDay, CountryCode rootCountry) {
        ZonedDateTime japanCreateTime = walletCreatedDate.toInstant().atZone(rootCountry.getZoneId());
        LocalDateTime japanExpireDate = japanCreateTime.toLocalDate().plusDays(expiredCalendarDay).atStartOfDay();
        LocalDateTime utcExpireDate = ZonedDateTime.of(japanExpireDate, rootCountry.getZoneId()).withZoneSameInstant(UTC).toLocalDateTime();

        return Timestamp.valueOf(utcExpireDate);
    }

    public List<TravelerWallet> getTravelerWalletByExchangeIds(String exchangeIds) {
        return travelerMapper.getTravelerWalletByExchangeIds(exchangeIds);
    }

    public void deleteTravelerWallet(Long walletId) {
        travelerMapper.deleteTravelerWallet(walletId);
    }

    /**
     * update Traveler Wallet for Withdrawalh
     *
     * @param pvo
     * @param travelerWallet
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void updateWalletForWithdrawal(RequestCashOutVO pvo, TravelerWallet travelerWallet)
            throws CashmallowException {

        String method = "updateWalletForCashOut()";

        // 4. 여행자 E-Money에서 차감
        BigDecimal eMoney = travelerWallet.geteMoney();
        eMoney = eMoney.subtract(pvo.getTraveler_total_cost());
        travelerWallet.setcMoney(pvo.getTraveler_total_cost());// cashout예약
        travelerWallet.seteMoney(eMoney);
        int affectedRow = updateTravelerWallet(travelerWallet);

        if (affectedRow != 1 || eMoney.compareTo(BigDecimal.valueOf(0)) < 0) {
            log.error("{}: 인출 요청 금액을 여행자 E-Money에서 차감할 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (travelerWallet.getcMoney().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("{}: 지갑의 인출 예정금액이 0원 일수 없습니다. traveler_total_cost={}", method, pvo.getTraveler_total_cost());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // updateWalletListCanRefundToSameExchange(travelerWallet.getExchangeIds());
    }


    @Transactional(rollbackFor = CashmallowException.class)
    public TravelerWallet updateWalletForWithdrawalV2(BigDecimal cashoutAmt, TravelerWallet travelerWallet)
            throws CashmallowException {

        String method = "updateWalletForCashOutV2()";

        // 4. 여행자 E-Money에서 차감
        BigDecimal eMoney = travelerWallet.geteMoney();
        eMoney = eMoney.subtract(cashoutAmt);
        travelerWallet.setcMoney(cashoutAmt);// cashout예약
        travelerWallet.seteMoney(eMoney);
        int affectedRow = updateTravelerWallet(travelerWallet);

        if (affectedRow != 1 || eMoney.compareTo(BigDecimal.valueOf(0)) < 0) {
            log.error("{}: 인출 요청 금액을 여행자 E-Money에서 차감할 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (travelerWallet.getcMoney().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("{}: 지갑의 인출 예정금액이 0원 일수 없습니다. cashoutAmt={}", method, cashoutAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // updateWalletListCanRefundToSameExchange(travelerWallet.getExchangeIds());

        return travelerWallet;
    }

    /**
     * Update Traveler Wallet for Refund
     *
     * @param walletId
     * @param refundAmt
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public TravelerWallet updateWalletForWithdrawalCancel(long walletId, BigDecimal cashOutAmt, String canRefund) throws CashmallowException {

        String method = "updateWalletByWithdrawalCancel()";

        // 1. 환불할 여행자 지갑을 가져온다. 이때 해당되는 지갑이 한개 검색되야 한다.
        TravelerWallet wallet = getTravelerWallet(walletId);

        // root_cd(환전한 국가코드와 환전할 국가가 다르면 에러 처리)
        if (wallet == null) {
            log.error("{}: 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (wallet.getcMoney().compareTo(cashOutAmt) != 0) {
            log.error("{}: 인출 취소 요청 금액이 지갑의 인출 금액과 다릅니다. travelerId={}, walletId={}, wallet.getcMoney()={}, cashOutAmt={}",
                    method, wallet.getTravelerId(), walletId, wallet.getcMoney(), cashOutAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (wallet.getCanRefund().equals("N") && canRefund.equals("Y")) {
            updateWalletListForCanRefund(wallet.getExchangeIds());
        }

        wallet.seteMoney(wallet.geteMoney().add(cashOutAmt));
        wallet.setcMoney(wallet.getcMoney().subtract(cashOutAmt));
        wallet.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRow = updateTravelerWallet(wallet);

        if (affectedRow != 1) {
            log.error("{}: 인출 취소한 금액을 업데이트할 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return wallet;
    }

    /**
     * Update traelerWallet
     *
     * @param travelerWallet
     * @return
     */
    public Integer updateTravelerWallet(TravelerWallet travelerWallet) {
        return travelerMapper.updateTravelerWallet(travelerWallet);
    }


    public List<TravelerWallet> getRelatedWallets(Long walletId) {
        return travelerMapper.getRelatedWalletsByWalletId(walletId);
    }

    public List<TravelerWallet> getRelatedWallets(TravelerWallet wallet) {
        return getRelatedWallets(wallet.getExchangeIds());
    }

    public List<TravelerWallet> getRelatedWallets(String exchangeIdsJson) {
        JSONObject jsonExchangeIds = new JSONObject(exchangeIdsJson);
        JSONArray exchangeArray = jsonExchangeIds.getJSONArray(Const.EXCHANGE_IDS);
        String exchangeId = exchangeArray.get(0).toString();

        List<TravelerWallet> walletLists = getTravelerWalletByExchangeIds(exchangeId);
        return walletLists;
    }

    @Transactional
    public void updateWalletListForCanRefund(String exchangeIds) throws CashmallowException {
        String method = "updateWalletListForCanRefund()";
        List<TravelerWallet> walletLists = getRelatedWallets(exchangeIds);

        if (walletLists.isEmpty()) {
            log.error("walletCanRefundEnabled() :  {}", exchangeIds);
            return;
        }

        for (TravelerWallet wallet : walletLists) {
            wallet.setCanRefund("Y");
            if (updateTravelerWallet(wallet) != 1) {
                log.error("{}: CanRefund 업데이트중 오류가 발생했습니다. exchangeIds={}", method, exchangeIds);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }
    }


    /**
     * 환불건과 관련된 모든 지갑 처리.
     *
     * @param walletId
     * @param refundAmt
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public TravelerWallet updateRelatedWalletForRefundCancel(long walletId, BigDecimal refundAmt) throws CashmallowException {

        String method = "updateWalletForRefund()";

        // 1. 환불할 여행자 지갑을 가져온다. 이때 해당되는 지갑이 한개 검색되야 한다.
        TravelerWallet wallet = getTravelerWallet(walletId);

        // root_cd(환전한 국가코드와 환전할 국가가 다르면 에러 처리)
        if (wallet == null) {
            log.error("{}: 환불을 위한 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (wallet.getrMoney().compareTo(refundAmt) != 0) {
            log.error("{}: 환불 취소 요청 금액이 지갑의 환불 금액과 다릅니다. travelerId={}, walletId={}, wallet.getrMoney()={}, refundAmt={}",
                    method, wallet.getTravelerId(), walletId, wallet.getrMoney(), refundAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        restoreRelatedWallets(walletId);

        return wallet;
    }


    @Transactional
    public Integer deleteWalletForCashOut(Long walletId, BigDecimal cashOutAmt) throws CashmallowException {
        String method = "deleteWalletForCashOut()";

        TravelerWallet wallet = getTravelerWallet(walletId);

        if (wallet == null) {
            log.error("{}: Cannot find wallet. walletId={}, cashOutAmt={}", method, walletId, cashOutAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        BigDecimal eMoney = wallet.geteMoney() == null ? BigDecimal.ZERO : wallet.geteMoney();
        BigDecimal cMoney = wallet.getcMoney() == null ? BigDecimal.ZERO : wallet.getcMoney();
        BigDecimal rMoney = wallet.getrMoney() == null ? BigDecimal.ZERO : wallet.getrMoney();

        if (cMoney.compareTo(cashOutAmt) == 0
                && eMoney.compareTo(BigDecimal.ZERO) == 0
                && rMoney.compareTo(BigDecimal.ZERO) == 0) {

            return travelerMapper.deleteTravelerWallet(walletId);

        } else {
            log.error("{}: Cannot delete wallet. travelerId={}, walletId={}, cashOutAmt={}",
                    method, wallet.getTravelerId(), walletId, cashOutAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void deleteWalletForRefund(Long walletId, BigDecimal refundAmt) throws CashmallowException {
        String method = "deleteWalletForCashOut()";

        TravelerWallet wallet = getTravelerWallet(walletId);

        if (wallet == null) {
            log.error("{}: Cannot find wallet. walletId={}, refundAmt={}", method, walletId, refundAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        BigDecimal eMoney = wallet.geteMoney() == null ? BigDecimal.ZERO : wallet.geteMoney();
        BigDecimal cMoney = wallet.getcMoney() == null ? BigDecimal.ZERO : wallet.getcMoney();
        BigDecimal rMoney = wallet.getrMoney() == null ? BigDecimal.ZERO : wallet.getrMoney();

        if (rMoney.compareTo(refundAmt) == 0
                && eMoney.compareTo(BigDecimal.ZERO) == 0
                && cMoney.compareTo(BigDecimal.ZERO) == 0) {

            int affectedRow = travelerMapper.deleteTravelerWallet(walletId);

            if (affectedRow != 1) {
                log.error("{}: Cannot delete wallet. travelerId={}, walletId={}, refundAmt={}, affectedRow={}",
                        method, wallet.getTravelerId(), walletId, refundAmt, affectedRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else {
            log.error("{}: Cannot delete wallet. travelerId={}, walletId={}, refundAmt={}",
                    method, wallet.getTravelerId(), walletId, refundAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 같은 환전건인 지갑들을 백업하고 합침.
     *
     * @param wallet
     * @param refundAmt
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public TravelerWallet updateRelatedWalletForRefund(long walletId, BigDecimal refundAmt) throws CashmallowException {
        TravelerWallet wallet = getTravelerWallet(walletId);
        // validation
        if (wallet == null) {
            log.error("환불을 위한 여행자 지갑을 찾지 못했습니다.");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        List<TravelerWallet> relatedWallets = getRelatedWallets(wallet);
        BigDecimal totalMoney = relatedWallets.stream().map(TravelerWallet::geteMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalMoney.compareTo(refundAmt) != 0) {
            log.error("지갑 총액이 환불 요청한 금액과 다릅니다. travelerId={}, walletId={}, totalMoney={}, refundAmt={}", wallet.getTravelerId(), walletId, totalMoney, refundAmt);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 모든 지갑을 백업하고, 신청한 지갑 이외의 지갑을 지움.
        backupRelatedWallets(walletId);
        deleteOtherRelatedWallets(walletId);

        // 환불 신청한 지갑에 총액을 모두 넣음.
        wallet.seteMoney(BigDecimal.ZERO);
        wallet.setrMoney(totalMoney);
        int affectedRow = updateTravelerWallet(wallet);
        if (affectedRow != 1) {
            log.error("from e_money에서 환불할 금액을 차감할 수 없습니다.");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return wallet;
    }


    //----------

    /**
     * 지정한 지갑을 백업
     *
     * @param walletId
     */
    public void backupWallet(long walletId) {
        travelerWalletMapper.insertBackupWallet(walletId);
    }

    /**
     * 지정한 지갑을 복원
     *
     * @param walletId
     */
    public void restoreWallet(long walletId) {
        travelerWalletMapper.insertRestoreWallet(walletId);
        travelerWalletMapper.deleteBackupWallet(walletId);
    }

    /**
     * 지정한 지갑과 연관된 지갑을 모두 백업(지정한 지갑 포함)
     *
     * @param walletId
     */
    public void backupRelatedWallets(long walletId) {
        travelerWalletMapper.deleteBackupRelatedWalletsByWalletId(walletId);
        travelerWalletMapper.insertBackupRelatedWalletsByWalletId(walletId);
    }

    /**
     * 지정한 지갑과 연관된 지갑을 모두 복원(지정한 지갑 포함)
     *
     * @param walletId
     */
    public void restoreRelatedWallets(long walletId) {
        travelerWalletMapper.insertRestoreRelatedWalletsByWalletId(walletId);
        travelerWalletMapper.deleteBackupRelatedWalletsByWalletId(walletId);
    }

    /**
     * 여행자 지갑 목록에서 지정한 지갑을 제외하고 연관된 지갑들을 제거
     *
     * @param walletId
     */
    public void deleteOtherRelatedWallets(long walletId) {
        travelerWalletMapper.deleteOtherRelatedWallets(walletId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<TravelerWallet> getUnpaidListForGlobal(String fromCountryCode) {
        return travelerWalletMapper.getUnpaidListForGlobal(fromCountryCode);
    }

    @Transactional
    public void expireWallet(Long walletId) {
        TravelerWallet travelerWallet = getTravelerWallet(walletId);
        travelerWallet.setExpired("Y");
        updateTravelerWallet(travelerWallet);
    }
}
