package com.cashmallow.api.interfaces.admin;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.remittance.RemittancesDoing;
import com.cashmallow.api.domain.model.statistics.MoneyTransferStatistics;
import com.cashmallow.api.domain.model.statistics.MoneyTransferStatisticsMapper;
import com.cashmallow.api.domain.model.traveler.AllWallet;
import com.cashmallow.api.domain.model.traveler.TravelerWalletMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.CountryStatsDto;
import com.cashmallow.common.JsonStr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.model.statistics.MoneyTransferStatistics.Type.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AdminStatsServiceImpl {

    // 대사용 통계를 만드는 국가 리스트
    public static final List<String> SERVICE_COUNTRIES = List.of("001", "003", "004", "009", "010");

    private final MoneyTransferStatisticsMapper moneyTransferStatisticsMapper;
    private final TravelerWalletMapper travelerWalletMapper;
    private final RemittanceRepositoryService remittanceRepositoryService;

    private final ZonedDateTime HK_START_TIME = ZonedDateTime.of(2021, 6, 11, 00, 00, 00, 00, ZoneId.of("Asia/Hong_Kong"));
    private final ZonedDateTime KR_START_TIME = ZonedDateTime.of(2023, 1, 1, 00, 00, 00, 00, ZoneId.of("Asia/Seoul"));


    public List<CountryStatsDto> getReconciliation(CountryCode from, int year) throws CashmallowException {
        log.info("getReconciliation from={}, year={}", from, year);

        // 수집된 통계 데이터 합산
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        List<MoneyTransferStatistics> rangeTransferStatistics = moneyTransferStatisticsMapper.getRangeTransferStatistics(from.getCode(), startDate, endDate);

        // 당일 실시간분 합산
        List<AllWallet> allWallets = null;
        List<RemittancesDoing> remittancesDoingList = null;
        ZonedDateTime formToday = ZonedDateTime.now(from.getZoneId()).truncatedTo(ChronoUnit.DAYS);
        if (formToday.getYear() == year) {
            log.info("실시간 formToday:{}", formToday);
            List<MoneyTransferStatistics> moneyTransferStatistics = moneyTransferStatisticsMapper.getMoneyTransferStatistics(from.getCode(), formToday);

            log.info("실시간 moneyTransferStatistics:{}", JsonStr.toJson(moneyTransferStatistics));
            rangeTransferStatistics.addAll(moneyTransferStatistics);

            allWallets = getAllwallets(from);
            remittancesDoingList = remittanceRepositoryService.getAllRemittanceDoing(from);
        }
        log.info("rangeTransferStatistics:{}", JsonStr.toJson(rangeTransferStatistics));
        log.info("allWallets={}", allWallets);
        log.info("remittancesDoingList={}", remittancesDoingList);

        final List<CountryStatsDto> calculatedResults = getCalculatedResults(rangeTransferStatistics, allWallets, remittancesDoingList);
        log.info("calculatedResults={}", JsonStr.toJson(calculatedResults));

        return calculatedResults;
    }

    private List<AllWallet> getAllwallets(CountryCode from) {
        return travelerWalletMapper.getAllWallets(from.getCode());
    }

    private static String getCurrency(String countryCode) {
        return CountryInfo.valueOf(CountryCode.of(countryCode).name()).getCurrency();
    }

    /**
     * 대사 통계용 데이터를 List로 생성 0번, 1번은 FromCountry, 그 이후는 ToCountry
     *
     * @param mtsList
     * @param allWallets
     * @param remittancesDoingList
     * @return
     */
    private static List<CountryStatsDto> getCalculatedResults(List<MoneyTransferStatistics> mtsList, List<AllWallet> allWallets, List<RemittancesDoing> remittancesDoingList) {
        Set<String> fromCdSet = mtsList.stream().map(MoneyTransferStatistics::getFromCd).collect(Collectors.toUnmodifiableSet());
        if (fromCdSet.size() != 1) {
            throw new IllegalArgumentException("fromCd가 여러개 입니다. fromCd=" + fromCdSet);
        }

        log.info("Raw moneyTransferStatistics = {}", JsonStr.toJson(mtsList));

        List<CountryStatsDto> result = new ArrayList<>();

        // from 국가 계산
        String fromCd = mtsList.get(0).getFromCd();
        Map<MoneyTransferStatistics.Type, BigDecimal> sumFromAmtMap = mtsList.stream()
                .collect(Collectors.groupingBy(MoneyTransferStatistics::getType, Collectors.reducing(BigDecimal.ZERO, MoneyTransferStatistics::getFromAmt, BigDecimal::add)));

        BigDecimal totalAmount = sumFromAmtMap.getOrDefault(EXCHANGE, BigDecimal.ZERO).add(sumFromAmtMap.getOrDefault(REMITTANCE, BigDecimal.ZERO));
        BigDecimal totalMappedAmt = mtsList.stream()
                .filter(m -> m.getType().equals(EXCHANGE) || m.getType().equals(REMITTANCE))
                .map(MoneyTransferStatistics::getMappedAmt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overDeposits = totalMappedAmt.subtract(totalAmount);
        BigDecimal refundEx = sumFromAmtMap.getOrDefault(REFUND_EX, BigDecimal.ZERO);
        BigDecimal refundRemit = sumFromAmtMap.getOrDefault(REFUND_RM, BigDecimal.ZERO);

        result.add(new CountryStatsDto(
                getCurrency(fromCd).concat("+"),
                totalAmount,
                overDeposits,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        ));
        result.add(new CountryStatsDto(
                getCurrency(fromCd).concat("-"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                refundEx,
                refundRemit
        ));

        // to 국가 계산
        Map<String, Map<MoneyTransferStatistics.Type, BigDecimal>> toCdSumMap = mtsList.stream().collect(Collectors.groupingBy(MoneyTransferStatistics::getToCd,
                Collectors.groupingBy(MoneyTransferStatistics::getType, Collectors.reducing(BigDecimal.ZERO, MoneyTransferStatistics::getToAmt, BigDecimal::add))));
        log.info("toCdSumMap={}", JsonStr.toJson(toCdSumMap));

        if (allWallets == null) {
            allWallets = new ArrayList<>();
        }
        if (remittancesDoingList == null) {
            remittancesDoingList = new ArrayList<>();
        }

        Set<String> toCdSet = mtsList.stream().map(MoneyTransferStatistics::getToCd).collect(Collectors.toSet());
        toCdSet.addAll(SERVICE_COUNTRIES); // default to국가 추가.

        log.debug("toCdSet:{}", toCdSet);
        for (var toCd : toCdSet) {
            if (toCd.equals(fromCd)) {
                continue;
            }

            Map<MoneyTransferStatistics.Type, BigDecimal> typeBigDecimalMap = toCdSumMap.getOrDefault(toCd, new HashMap<>());

            BigDecimal remitDoing = remittancesDoingList.stream()
                    .filter(m -> m.getToCd().equals(toCd))
                    .map(RemittancesDoing::getToAmt)
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // 지급대기액

            BigDecimal wallets = allWallets.stream()
                    .filter(w -> w.getToCd().equals(toCd))
                    .map(AllWallet::getTotalPendingMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // 지갑

            result.add(new CountryStatsDto(getCurrency(toCd) + "+",
                    typeBigDecimalMap.getOrDefault(EXCHANGE, BigDecimal.ZERO).add(typeBigDecimalMap.getOrDefault(REMITTANCE, BigDecimal.ZERO)),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO));

            result.add(new CountryStatsDto(getCurrency(toCd) + "-",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    typeBigDecimalMap.getOrDefault(REMITTANCE_CF, BigDecimal.ZERO),
                    remitDoing,
                    typeBigDecimalMap.getOrDefault(CASH_OUT, BigDecimal.ZERO),
                    wallets,
                    typeBigDecimalMap.getOrDefault(REFUND_RM, BigDecimal.ZERO),
                    typeBigDecimalMap.getOrDefault(REFUND_EX, BigDecimal.ZERO)));

        }

        return result;
    }

}
