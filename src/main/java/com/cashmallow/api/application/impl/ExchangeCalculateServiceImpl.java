package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.company.BankAccount;
import com.cashmallow.api.domain.model.company.CompanyMapper;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CountryMapper;
import com.cashmallow.api.domain.model.exchange.Mapping;
import com.cashmallow.api.domain.model.exchange.MappingMapper;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.admin.dto.MappingPinRegVO;
import com.cashmallow.api.interfaces.admin.dto.MappingRegVO;
import com.cashmallow.common.CommDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;


/**
 * 환율 계산과 관련 된 기능을 모은 서비스
 * - 환율 계산
 * - PinValue 생성
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExchangeCalculateServiceImpl {

    private static final String EXCHANGE_NO_AVAILABLE_PIN = "EXCHANGE_NO_AVAILABLE_PIN";


    private final TravelerRepositoryService travelerRepositoryService;
    private final SecurityService securityService;

    private final CountryMapper countryMapper;
    private final CompanyMapper companyMapper;
    private final MappingMapper mappingMapper;


    // -------------------------------------------------------------------------------
    // 60. mapping
    // -------------------------------------------------------------------------------

    /**
     * 환전 신청 금액(원금 + 수수료)에 대한 PIN 값을 응답한다.
     *
     * @param userId
     * @param pvo
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Mapping generatePinValue(Long userId, MappingPinRegVO pvo) throws CashmallowException {
        try {

            String method = "generatePinValue()";

            Mapping mapping = null;

            log.info("{}: userId={}, pvo={}", method, userId, pvo);
            String timeZone = "GMT";

            if (pvo == null || !pvo.checkValidation()) {
                log.error("{}: MappingPinRegVO is Null or Not valid", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
            BigDecimal pinCandidate = pvo.getPin_value();

            // 1. PIN 값은 유효 통장에서만 찾으므로 use_yn를 "Y"로 강제 설정한다.
            pvo.setUse_yn("Y");

            // 2. 여행자 ID를 구한다.
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            if (traveler == null) {
                log.error("{}: Cannot find Traveler by User ID, userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            traveler.setIdentificationNumber(securityService.decryptAES256(traveler.getIdentificationNumber()));

            Long travelerId = traveler.getId();

            // 3. 서비스 국가의 mapping 처리 정보(upper, lower, inc 등)를 구한다.
            HashMap<String, Object> params = new HashMap<>();
            params.put("code", pvo.getCountry());
            List<Country> countries = countryMapper.getCountryList(params);

            if (countries == null || countries.isEmpty()) {
                log.error("{}: Cannot find Country Info by Country. country={}", method, pvo.getCountry());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            Country country = countries.get(0);
            BigDecimal pinValue = pvo.getPin_value();

            // pinValue를 소수점을 통화 기본 단위로 조정
            int scale = -1 * (int) Math.floor(Math.log10(country.getMappingInc().doubleValue()));
            pinValue = pinValue.setScale(scale, RoundingMode.HALF_UP);

            BigDecimal incValue = country.getMappingInc();

            if (incValue.compareTo(new BigDecimal(0)) <= 0) {
                log.error("{}: 서비스 국가 테이블의 PIN값 증가 컬럼의 값에 오류가 있습니다.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // 증가값 단위로 강제 조정함.
            BigDecimal pinValue1 = pinValue.divide(incValue, RoundingMode.HALF_UP);
            BigDecimal pinValue2 = pinValue1.multiply(incValue);

            if (pinValue.compareTo(pinValue2) != 0) {
                log.error("{}: Parameter PIN 요청 값 오류. pinValue={}, pinValue2={}", method, pinValue, pinValue2);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            BigDecimal minPinValue = pinValue.add(country.getMappingLowerRange());
            BigDecimal maxPinValue = pinValue.add(country.getMappingUpperRange());

            pvo.setTraveler_id(travelerId);
            pvo.setMin_pin_value(minPinValue);
            pvo.setMax_pin_value(maxPinValue);

            // 4. DB Server의 날짜를 가져온다.
            Timestamp dateTime = CommDateTime.getCurrentDateTime();
            ApiResultVO dvo = isDateEvenDay(dateTime, timeZone);

            if (!dvo.chkOk()) {
                // 국가별, 짝수/홀수 일 별 지점 통장 리스트 검색을 위해 체크 필요
                log.error("{}: dvo.getMessage()={}", method, dvo.getMessage());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            List<BankAccount> bankAccounts;

            if (ObjectUtils.isEmpty(pvo.getBank_account_id())) {
                // 5.1 국가별, 짝수/홀수 일 별 지점 통장 리스트 검색

                params.clear();
                params.put("country", pvo.getCountry());
                params.put("useYn", "Y");
                params.put("size", 10000);
                params.put("startRow", 0);
                params.put("sort", "id");

                bankAccounts = companyMapper.findBankAccount(params);

            } else {
                bankAccounts = new ArrayList<>();
                BankAccount vo = new BankAccount();
                vo.setId(pvo.getBank_account_id());
                bankAccounts.add(vo);
            }

            if (bankAccounts == null || bankAccounts.isEmpty()) {
                log.error("{}: 조건에 맞는 국가별 은행 정보를 찾을 수 없습니다.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // 6. DB Server의 현재 날짜를 구한다.
            Timestamp curDate = mappingMapper.getCurDate();

            if (curDate == null) {
                log.error("{}: DB Server의 현재 날짜를 읽을 수 없습니다.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // 6.1 은행정보 리스트를 VO에 담는다
            for (BankAccount bankAccount : bankAccounts) {
                Integer bankAccountId = bankAccount.getId();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // 7. 사용된 PIN을 구한다.
                params.clear();
                params.put("country", pvo.getCountry());
                params.put("bank_account_id", bankAccountId);
                params.put("valid_date", sdf.format(new Date(curDate.getTime())));
                params.put("min_pin_value", minPinValue);
                params.put("max_pin_value", maxPinValue);
                List<BigDecimal> vosValue = mappingMapper.getMappedPinValues(params);

                // 8. 사용된 PIN 값들을 참조하여 새 PIN 값을 구한다.
                // pinCandidate = getUnusedPinValue(vosValue, pinValue, minPinValue, maxPinValue, incValue);

                if (pinCandidate == Const.UNDEFINED) {
                    log.error("{}: 금일 발행할 수 있는 PIN이 모두 소진됐습니다.", method);
                    throw new CashmallowException(EXCHANGE_NO_AVAILABLE_PIN);
                }

                Integer refValue = -1;

                try {
                    refValue = (int) Math.round(Math.random() * Const.REF_VALUE_RANGE + Const.BEGIN_REF_VALUE);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                MappingRegVO mappingRegVO = new MappingRegVO();
                mappingRegVO.setCountry(pvo.getCountry());
                mappingRegVO.setBank_account_id(bankAccountId);
                mappingRegVO.setPin_value(pinCandidate);
                mappingRegVO.setRef_value(refValue.toString());
                mappingRegVO.setBegin_valid_date(curDate);
                mappingRegVO.setEnd_valid_date(curDate);
                mappingRegVO.setTraveler_id(pvo.getTraveler_id());

                // pin 값을 저장하고 bank_account별 ref_value를 1 증가한다.
                int affectedRow = mappingMapper.putPinValue(mappingRegVO);

                if (affectedRow == 1) {
                    params.put("min_pin_value", pinCandidate);
                    mapping = mappingMapper.findPinInfoByPinValue(params);
                    break;

                } else {
                    log.error("{}: PIN 값 저장 중 오류가 발생했습니다. pinCandidate={}", method, pinCandidate);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }

            }

            if (pinCandidate == Const.UNDEFINED) {
                log.warn("{}: 금일 발행할 수 있는 PIN이 모두 소진됐습니다.", method);
                throw new CashmallowException(EXCHANGE_NO_AVAILABLE_PIN);
            }

            return mapping;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    // 기능: 63.1. 오늘이 짝수일인지 여부를 응답한다.
    private ApiResultVO isDateEvenDay(Timestamp dateTime, String timeZone) {
        ApiResultVO result = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        if (timeZone == null || timeZone.isEmpty()) {
            timeZone = "GMT";
        }

        boolean flagSetResult = true;
        String method = "isDateEvenDay(): ";
        String error = "";
        Object obj = null;
        log.info(method);
        result = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (timeZone != null && !timeZone.isEmpty()) {
            try {
                long days = CommDateTime.getDays(dateTime, timeZone);

                obj = CommDateTime.isEvenDay(days);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                error = "짝수일/홀수일 확인 중 오류가 발생했습니다.";
            }
        } else {
            flagSetResult = false;
        }

        result.setSuccessOrFail(flagSetResult, obj, error);

        return result;
    }

    // 스프레드 적용시 차이나는 금액 계산
    public BigDecimal getFeeRateAmt(BigDecimal toMoney, BigDecimal baseRate, BigDecimal spreadRate, BigDecimal mappingInc) {
        BigDecimal baseFromMoney = getFromMoney(toMoney, baseRate, mappingInc);
        BigDecimal spreadFromMoney = getFromMoney(toMoney, spreadRate, mappingInc);

        return spreadFromMoney.subtract(baseFromMoney);
    }

    // 환전, 송금 모두 같은 계산식 사용
    public BigDecimal getFromMoney(BigDecimal toMoney, BigDecimal baseRate, BigDecimal mappingInc) {
        BigDecimal fromMoney;
        fromMoney = toMoney.multiply(baseRate).divide(mappingInc, 0, RoundingMode.HALF_UP)
                .multiply(mappingInc);
        return fromMoney;
    }

    public BigDecimal getFromMaxAmountToCurrency(BigDecimal feeRate, BigDecimal fromMaxAmount, BigDecimal unitScale,
                                                 BigDecimal currencyRate) {
        return fromMaxAmount.divide(BigDecimal.ONE.add(feeRate.multiply(BigDecimal.valueOf(0.01))), 9, RoundingMode.HALF_UP)
                .divide(currencyRate, 9, RoundingMode.HALF_UP).divide(unitScale, 0, RoundingMode.FLOOR)
                .multiply(unitScale);
    }

    public BigDecimal getSpreadRate(BigDecimal rate, BigDecimal spread) {
        BigDecimal result = rate.add(rate.multiply(spread.multiply(BigDecimal.valueOf(0.01))));
        return result.setScale(6, RoundingMode.HALF_UP);
    }

    public BigDecimal getRefundSpreadRate(BigDecimal rate, BigDecimal spread) {
        BigDecimal result = rate.subtract(rate.multiply(spread.multiply(BigDecimal.valueOf(0.01))));
        return result.setScale(6, RoundingMode.HALF_UP);
    }
}
