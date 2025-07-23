package com.cashmallow.api.interfaces.mallowlink.remittance;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
public class MallowlinkRemittanceBankServiceImpl {

    private final MallowlinkRemittanceBankClient remittanceBankClient;

    public void validationRemittanceBankAndBranch(Remittance remittance) throws CashmallowException {
        if ("JP".equalsIgnoreCase(remittance.getReceiverCountry())) {
            String fullBankCode = remittance.getReceiverBankCode();
            String[] split = fullBankCode.split("-");
            String bankCode = split[0];

            BankData remittanceBank = getBankList(CountryCode.JP, BigDecimal.TEN).stream().filter(b -> b.getBankCode().equals(bankCode))
                    .findAny().orElseThrow(() -> new CashmallowException(INTERNAL_SERVER_ERROR));

            List<BankBranchesData> bankBranches = getBankBranches(CountryCode.JP, remittanceBank.getBankId());
            boolean isEmptyBranch = CollectionUtils.isEmpty(bankBranches);
            if (isEmptyBranch) {
                // 브랜치가 없는 케이스 처리
                return;
            }

            // 브랜치가 있는 경우 처리
            if (split.length != 2) {
                log.error("Error 해당 은행은 bankCode가 존재하나 request에 포함되지 않았습니다.:{}", fullBankCode);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            String branchCode = split[1];
            bankBranches.stream().filter(b -> b.getBranchesCode().equals(branchCode))
                    .findAny()
                    .orElseThrow(() -> new CashmallowException(INTERNAL_SERVER_ERROR));

        }
    }

    public List<BankData> getBankList(CountryCode countryCode, BigDecimal amount) {
        String currency = CountryInfo.valueOf(countryCode.name()).getCurrency();

        RemittanceBankRequest request = new RemittanceBankRequest(
                countryCode,
                currency,
                amount
        );

        RemittanceBankResponse bank = remittanceBankClient.getBank(request).getData();
        return bank.getBanks().stream().map(BankData::of).collect(Collectors.toList());
    }

    public List<WalletData> getWalletList(CountryCode countryCode) {
        String currency = CountryInfo.valueOf(countryCode.name()).getCurrency();

        RemittanceWalletRequest request = new RemittanceWalletRequest(
                countryCode,
                currency
        );

        RemittanceWalletResponse wallet = remittanceBankClient.getWallet(request).getData();
        return wallet.wallets().stream().map(WalletData::of).collect(Collectors.toList());
    }

    public List<BankBranchesData> getBankBranches(CountryCode countryCode, String bankId) {

        RemittanceBankBranchesRequest request = new RemittanceBankBranchesRequest(
                countryCode,
                bankId
        );

        RemittanceBankBranchesResponse branches = remittanceBankClient.getBankBranches(request).getData();
        return branches.getBranches().stream().map(BankBranchesData::of).collect(Collectors.toList());
    }

}
