package com.cashmallow.api.interfaces.traveler.web.cashout;

import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashoutAgencyService {

    private final AjCashoutAgencyServiceV2 ajCashoutAgencyServiceV2;
    private final QueenbeeCashoutAgencyService queenbeeCashoutAgencyService;
    private final RcbcCashoutAgencyService rcbcCashoutAgencyService;
    private final ScbCashoutAgencyService scbCashoutAgencyService;
    private final CoatmCashoutAgencyService coatmCashoutAgencyService;

    public List<CashoutAgencyV2> getV2Agencies(WithdrawalPartner.KindOfStorekeeper kindOfStorekeeper,
                                               BigDecimal cashoutAmount) {
        return switch (kindOfStorekeeper) {
            case AJ -> ajCashoutAgencyServiceV2.getAgencies(cashoutAmount);
            case M001 -> queenbeeCashoutAgencyService.getAgencies(cashoutAmount); // QUEENBEE
            case RCBC -> rcbcCashoutAgencyService.getAgencies(cashoutAmount);
            case SCB -> scbCashoutAgencyService.getAgencies(cashoutAmount);
            case M002 -> coatmCashoutAgencyService.getAgencies(cashoutAmount); // COATM
            default -> throw new IllegalArgumentException("Unexpected value: " + kindOfStorekeeper);
        };
    }
}
