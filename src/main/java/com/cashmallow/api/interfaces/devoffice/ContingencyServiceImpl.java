package com.cashmallow.api.interfaces.devoffice;

import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ContingencyServiceImpl {
    private final SevenBankServiceImpl sevenBankService;
    private final CashoutRepositoryService cashoutRepositoryService;
    private final WalletRepositoryService walletRepositoryService;
    private final TravelerRepositoryService travelerRepositoryService;


    /**
     * 장애시 대응용으로 평상시에는 사용금지.
     * 강제로 Cashout을 OP로 Wallet을 인출 대기 상태로 만듬.
     *
     * @param remittanceId
     */
    @Transactional
    public void forceCashoutOp(String remittanceId) {
        // 강제 인출 OP
        Long cashoutId = sevenBankService.makeCashoutId(remittanceId);
        CashOut cashOut = cashoutRepositoryService.getCashOut(cashoutId);

        cashOut.setCoStatus(CashOut.CoStatus.OP.name());
        cashOut.setCoStatusDate(Timestamp.from(ZonedDateTime.now().toInstant()));

        int i = cashoutRepositoryService.updateCashOut(cashOut);
        if (i != 1) {
            log.error("Update CashOut Fail");
            throw new RuntimeException(INTERNAL_SERVER_ERROR);
        }

        // 강제 지갑 OP
        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(cashOut.getWalletId());
        try {
            walletRepositoryService.updateWalletForWithdrawalV2(cashOut.getTravelerCashOutAmt(), travelerWallet);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(INTERNAL_SERVER_ERROR, e);
        }
    }

    @Transactional
    public void updateTravelerAccountName(Long travelerId, String accountName) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        traveler.setAccountName(accountName);
        travelerRepositoryService.updateTraveler(traveler);
    }

}
