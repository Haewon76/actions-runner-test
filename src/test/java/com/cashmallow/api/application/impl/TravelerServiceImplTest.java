package com.cashmallow.api.application.impl;

import com.cashmallow.api.domain.model.cashout.CashOutMapper;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.*;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.traveler.dto.RefundResponse;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.config.EnableDevLocal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@EnableDevLocal
@Slf4j
class TravelerServiceImplTest {
    @Autowired
    TravelerServiceImpl travelerService;
    @Autowired
    JsonUtil jsonUtil;

    @Autowired
    TravelerRepositoryService travelerRepositoryService;
    @Autowired
    WalletRepositoryService walletRepositoryService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;
    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    TravelerMapper travelerMapper;
    @Autowired
    CashOutMapper cashOutMapper;
    @Autowired
    Gson gson;
    @Autowired
    EnvUtil envUtil;
    @Autowired
    TravelerWalletMapper travelerWalletMapper;
    @Autowired
    ObjectMapper objectMapper;

    Long travelerId = 2L;

    @BeforeEach
    void 지갑_생성() {
        // 지갑 생성
        Long money = 1392L;
        TravelerWallet travelerWallet = new TravelerWallet();
        travelerWallet.setTravelerId(travelerId);
        travelerWallet.setRootCd("003");
        travelerWallet.seteMoney(BigDecimal.valueOf(money));
        travelerWallet.setCreator(1L);
        travelerWallet.setCountry("001");
        JSONObject exchangeIds = new JSONObject();
        JSONArray ja = new JSONArray();
        ja.put(0, 123L);
        travelerWallet.setExchangeIds(exchangeIds.toString());
        travelerMapper.insertTravelerWallet(travelerWallet);

        // eMoney -> cMoney - 인출 모사
        List<TravelerWallet> travelerWallets = travelerMapper.getTravelerWalletByTravelerId(travelerId);
        TravelerWallet travelerWallet1 = travelerWallets.get(travelerWallets.size() - 1);
        travelerWallet1.seteMoney(BigDecimal.ZERO);
        travelerWallet1.setcMoney(BigDecimal.valueOf(money));
        Long walletId = travelerWallet1.getId();

        travelerMapper.updateTravelerWallet(travelerWallet1);
    }

    @Test
    void 지갑_백업_테스트() {
        // given
        // 생성된 지갑 가져오기
        List<TravelerWallet> travelerWallets = travelerMapper.getTravelerWalletByTravelerId(travelerId);
        TravelerWallet travelerWallet1 = travelerWallets.get(travelerWallets.size() - 1);
        Long walletId = travelerWallet1.getId();

        // when
        walletRepositoryService.backupWallet(walletId);

        // then
        TravelerWallet inactiveTravelerWallet = travelerWalletMapper.getBackupWallet(walletId);
        assertThat(travelerWallet1.getId()).isEqualTo(inactiveTravelerWallet.getId());
        assertThat(travelerWallet1.getTravelerId()).isEqualTo(inactiveTravelerWallet.getTravelerId());
        assertThat(travelerWallet1.getRootCd()).isEqualTo(inactiveTravelerWallet.getRootCd());
        assertThat(travelerWallet1.geteMoney()).isEqualByComparingTo(inactiveTravelerWallet.geteMoney());
        assertThat(travelerWallet1.getcMoney()).isEqualByComparingTo(inactiveTravelerWallet.getcMoney());
        assertThat(travelerWallet1.getExchangeIds()).isEqualTo(inactiveTravelerWallet.getExchangeIds());
        assertThat(travelerWallet1.getCountry()).isEqualTo(inactiveTravelerWallet.getCountry());
        assertThat(travelerWallet1.getCreator()).isEqualTo(inactiveTravelerWallet.getCreator());
    }

    
    @Test
    void 지갑복원_walletId() {
        // given
        // 지갑 백업
        List<TravelerWallet> travelerWallets = travelerMapper.getTravelerWalletByTravelerId(travelerId);
        TravelerWallet travelerWallet = travelerWallets.get(travelerWallets.size() - 1);
        Long walletId = travelerWallet.getId();
        walletRepositoryService.backupWallet(walletId);
        String withdrawalRequestNo = "SCBTEST2099010100001";

        // 지갑 제거
        travelerMapper.deleteTravelerWallet(walletId);
        TravelerWallet deleteWallet = walletRepositoryService.getTravelerWallet(walletId);
        assertThat(deleteWallet).isNull();

        // when
        walletRepositoryService.restoreWallet(walletId);

        // then
        TravelerWallet revertWallet = walletRepositoryService.getTravelerWallet(walletId);
        assertThat(travelerWallet.getId()).isEqualTo(revertWallet.getId());
        assertThat(travelerWallet.getTravelerId()).isEqualTo(revertWallet.getTravelerId());
        assertThat(travelerWallet.getRootCd()).isEqualTo(revertWallet.getRootCd());
        assertThat(travelerWallet.geteMoney()).isEqualByComparingTo(revertWallet.geteMoney());
        assertThat(travelerWallet.getcMoney()).isEqualByComparingTo(revertWallet.getcMoney());
        assertThat(travelerWallet.getExchangeIds()).isEqualTo(revertWallet.getExchangeIds());
        assertThat(travelerWallet.getCountry()).isEqualTo(revertWallet.getCountry());
        assertThat(travelerWallet.getCreator()).isEqualTo(revertWallet.getCreator());
        TravelerWallet backupWallet = travelerWalletMapper.getBackupWallet(walletId);
        assertThat(backupWallet).isNull();
    }

    @Test
    void 인증완료수_조회() {
        // given

        // when
        String startDate = "2023-02-05";
        String endDate = "2023-03-09";
        int countNewTravelers = travelerService.getCountNewTravelers(startDate, endDate);

        // then

    }

    // @Disabled
    @Test
    void 연관된_지갑_백업_복원() {
        // given
        long walletId = 10617L;
        List<TravelerWallet> relatedWallets = walletRepositoryService.getRelatedWallets(walletId);
        System.out.println("relatedWallets = " + relatedWallets);

        // when
        walletRepositoryService.backupRelatedWallets(walletId);

        // then


    }

    @Test
    @Disabled
    void 기기리셋_테스트() throws CashmallowException {
        Locale locale = Locale.US;
        String tokenId = "dccd8069f013ebd7b82386d57e8aa1cbd888be1e33af5b9e8e58822bc35170ccf3e9883a3bafba18aa1a4193da2e94dd";
        String code = "816813";
        String fcmToken = "d6n3qpnpSbui-zGS4wVrem:APA91bEPcW56pmkEeFZZitRf10eaCNfE22BxEiO_QPcDP0nWni5ww2ecrAGmDp8oZqAmdnYxw56jJ9GDQUEShF6cgq-s9nDrBexi0M3TbSbtcJeTIKW2-xJoCkNvnhbzjMWWy2GdGRG1";

        final boolean deviceResetCodeValid = travelerService.isDeviceResetCodeValid(tokenId, code, fcmToken, locale, "A");
        log.info("deviceResetCodeValid = " + deviceResetCodeValid);
    }

    @Test
    void 인증_날짜_만료_테스트() throws CashmallowException {
        long userId = 1620L;
        Locale locale = Locale.KOREA;
        Map<String, Object> resultMap = travelerService.getTravelerMapByUserId(userId, locale);
        System.out.println("resultMap = " + jsonUtil.toJsonPretty(resultMap));
    }

    @Test
    @Disabled
    void 송금_환불_모델_테스트() {
        long remitId = 9452L;
        NewRefund newRefund = refundRepositoryService.getNewRefundNotCancelByRemitId(remitId, 951738L);
        newRefund.setExchangeId(28897L);

        RefundResponse rr = RefundResponse.of(newRefund);
        log.info(rr.toString());
    }

    @Test
    void 환불_모델_테스트() throws CashmallowException {
        refundService.receiptRefund(51L, "001");
    }

}