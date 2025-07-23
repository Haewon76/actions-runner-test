package com.cashmallow.api.interfaces.mallowlink.withdrawal;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.MallowlinkWithdrawalResponse;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.WithdrawalResponse;
import com.cashmallow.api.interfaces.traveler.web.cashout.CashoutAgencyService;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerCancelService {

    private final EnvUtil envUtil;
    private final JsonUtil jsonUtil;
    private final PartnerServiceImpl partnerService;
    private final SecurityService securityService;
    private final CashoutRepositoryService cashoutRepositoryService;
    private final CashoutAgencyService cashoutAgencyService;
    private final WalletRepositoryService walletRepositoryService;

    /**
     * 취소 기능이 없는 파트너인지 체크
     * (현재 AJ의 경우 해당)
     * 만약 취소 기능이 없는 파트너라도 QR코드 만료 되면 취소 가능
     *
     * @param cashout
     * @return
     */
    public boolean isNonCancelableStatePartner(CashOut cashout) {
        try {
            WithdrawalPartner wPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashout.getWithdrawalPartnerId());
            if (!wPartner.isCancelable()) {
                return getMlResponse(cashout.getQrCodeValue()) != null;
            }
        } catch (Exception ignore) {
        }

        return false;
    }

    /**
     * 캐싱된 응답값을 리턴한다
     * 만약 만료된 OTP인 경우 null을 리턴한다
     *
     * @param withdrawalPartner
     * @param walletId
     * @return
     */
    public WithdrawalResponse getCachedWithdrawalResponse(WithdrawalPartner withdrawalPartner, Long walletId) {
        try {
            WithdrawalPartner wPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartner.getPartnerId());
            if (!wPartner.isCancelable()) {
                CashOut opCashOut = cashoutRepositoryService.getOPCashOut(walletId, withdrawalPartner.getId());
                MallowlinkWithdrawalResponse mlResponse = getMlResponse(opCashOut.getQrCodeValue());
                return WithdrawalResponse.of(opCashOut.getId(), mlResponse);
            }
        } catch (Exception ignore) {
        }

        return null;
    }

    public BigDecimal getCashoutReservedAmt(Long walletId) {
        if(walletId == null) {
            return BigDecimal.ZERO;
        }

        try {
            return walletRepositoryService.getTravelerWallet(walletId).getcMoney();
        } catch (Exception ignore) {
            return BigDecimal.ZERO;
        }
    }

    public WithdrawalResponse getCachedWithdrawalResponse(WithdrawalPartner.KindOfStorekeeper kindOfStorekeeper, Long walletId) {
        try {
            WithdrawalPartner withdrawalPartner = partnerService.getStorekeeperListByKindOfStorekeeper(kindOfStorekeeper.name()).stream().findAny().orElseThrow(() -> new CashmallowException(INTERNAL_SERVER_ERROR));
            if (!withdrawalPartner.isCancelable()) {
                CashOut opCashOut = cashoutRepositoryService.getOPCashOut(walletId, withdrawalPartner.getId());
                MallowlinkWithdrawalResponse mlResponse = getMlResponse(opCashOut.getQrCodeValue());
                return WithdrawalResponse.of(opCashOut.getId(), mlResponse);
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return null;
    }


    /**
     * ML응답 캐싱된 값 리턴
     *
     * @param qrCode
     * @return
     */
    private MallowlinkWithdrawalResponse getMlResponse(String qrCode) {
        String qrCodeValue = securityService.decryptAES256(qrCode);
        if (StringUtils.isNotBlank(qrCodeValue)) {
            MallowlinkWithdrawalResponse response = jsonUtil.fromJson(qrCodeValue, MallowlinkWithdrawalResponse.class);
            return response.expireTime().isAfter(ZonedDateTime.now()) ? response : null;
        }

        return null;
    }
}
