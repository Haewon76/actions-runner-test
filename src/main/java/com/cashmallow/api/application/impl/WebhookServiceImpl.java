package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.UserService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.user.dto.CountNewUsersAndTravelersByCountryVO;
import com.cashmallow.common.CustomStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WebhookServiceImpl {

    public enum Keyword {
        USER,
        CONFIRM,
        EXCHANGE,
        CASHOUT,
        PAYMENT,
        PAYBACK,
        REFUND,
        WALLET,
        EX_SERVICE,
        RM_SERVICE,
        COUNT_NEW_USERS
    }

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    private PartnerCalcServiceImpl partnerCalcService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private UserService userService;

    @Autowired
    private CountryService countryService;

    public String getHelp() {
        StringBuilder result = new StringBuilder();
        result.append("[여행자수 조회] -> user \n");
        result.append("[승인 조회] -> confirm \n");
        result.append("[환전 조회] -> exchange \n");
        result.append("[인출 조회] -> cashout \n");
        result.append("[결제 조회] -> payment \n");
        result.append("[가맹점 정산 조회] -> payback \n");
        result.append("[환불 조회] -> refund \n");
        result.append("[지갑 확인] -> wallet \n");
        result.append("[환전 서비스 확인] -> ex_service \n");
        result.append("[신규 회원 수 조회] -> count_new_users \n");
        result.append("[환전 대만-한국 열기] -> ex_service 002 003 Y \n");
        result.append("[환전 대만-한국 닫기] -> ex_service 002 003 N \n");
        result.append("[환전 한국-대만 열기] -> ex_service 003 002 Y \n");
        result.append("[환전 한국-대만 닫기] -> ex_service 003 002 N \n");
        result.append("[환전 홍콩-한국 열기] -> ex_service 001 003 Y \n");
        result.append("[환전 홍콩-한국 닫기] -> ex_service 001 003 N \n");
        result.append("[환전 한국-홍콩 열기] -> ex_service 003 001 Y \n");
        result.append("[환전 한국-홍콩 닫기] -> ex_service 003 001 N \n");
        result.append("[환전 대만-홍콩 열기] -> ex_service 002 001 Y \n");
        result.append("[환전 대만-홍콩 닫기] -> ex_service 002 001 N \n");
        result.append("[환전 홍콩-대만 열기] -> ex_service 001 002 Y \n");
        result.append("[환전 홍콩-대만 닫기] -> ex_service 001 002 N \n");
        result.append("[환전 일본-한국 열기] -> ex_service 004 003 Y \n");
        result.append("[환전 일본-한국 닫기] -> ex_service 004 003 N \n");
        result.append("[환전 한국-일본 열기] -> ex_service 003 004 Y \n");
        result.append("[환전 한국-일본 닫기] -> ex_service 003 004 N \n");
        result.append("[환전 일본-대만 열기] -> ex_service 004 002 Y \n");
        result.append("[환전 일본-대만 닫기] -> ex_service 004 002 N \n");
        result.append("[환전 대만-일본 열기] -> ex_service 002 004 Y \n");
        result.append("[환전 대만-일본 닫기] -> ex_service 002 004 N \n");
        result.append("[환전 일본-홍콩 열기] -> ex_service 004 001 Y \n");
        result.append("[환전 일본-홍콩 닫기] -> ex_service 004 001 N \n");
        result.append("[환전 홍콩-일본 열기] -> ex_service 001 004 Y \n");
        result.append("[환전 홍콩-일본 닫기] -> ex_service 001 004 N \n");
        result.append("   \n");
        result.append("[송금 서비스 확인] -> rm_service \n");
        result.append("[송금 대만-한국 열기] -> rm_service 002 003 Y \n");
        result.append("[송금 대만-한국 닫기] -> rm_service 002 003 N \n");
        result.append("[송금 한국-대만 열기] -> rm_service 003 002 Y \n");
        result.append("[송금 한국-대만 닫기] -> rm_service 003 002 N \n");
        result.append("[송금 홍콩-한국 열기] -> rm_service 001 003 Y \n");
        result.append("[송금 홍콩-한국 닫기] -> rm_service 001 003 N \n");
        result.append("[송금 한국-홍콩 열기] -> rm_service 003 001 Y \n");
        result.append("[송금 한국-홍콩 닫기] -> rm_service 003 001 N \n");
        result.append("[송금 대만-홍콩 열기] -> rm_service 002 001 Y \n");
        result.append("[송금 대만-홍콩 닫기] -> rm_service 002 001 N \n");
        result.append("[송금 홍콩-대만 열기] -> rm_service 001 002 Y \n");
        result.append("[송금 홍콩-대만 닫기] -> rm_service 001 002 N \n");
        result.append("[송금 일본-한국 열기] -> rm_service 004 003 Y \n");
        result.append("[송금 일본-한국 닫기] -> rm_service 004 003 N \n");
        result.append("[송금 한국-일본 열기] -> rm_service 003 004 Y \n");
        result.append("[송금 한국-일본 닫기] -> rm_service 003 004 N \n");
        result.append("[송금 일본-대만 열기] -> rm_service 004 002 Y \n");
        result.append("[송금 일본-대만 닫기] -> rm_service 004 002 N \n");
        result.append("[송금 대만-일본 열기] -> rm_service 002 004 Y \n");
        result.append("[송금 대만-일본 닫기] -> rm_service 002 004 N \n");
        result.append("[송금 일본-홍콩 열기] -> rm_service 004 001 Y \n");
        result.append("[송금 일본-홍콩 닫기] -> rm_service 004 001 N \n");
        result.append("[송금 홍콩-일본 열기] -> rm_service 001 004 Y \n");
        result.append("[송금 홍콩-일본 닫기] -> rm_service 001 004 N \n");

        return result.toString();
    }

    public String getExchangeServiceStatus() {
        StringBuilder result = new StringBuilder();

        List<ExchangeConfig> rates = countryService.getCanExchanageFeeRateList();
        for (ExchangeConfig r : rates) {
            result.append(r.getFromCd()).append(" to ").append(r.getToCd()).append(" = ").append(r.getEnabledExchange());
            result.append("\n");
        }

        return result.toString();
    }

    public String getRemittanceServiceStatus() {
        StringBuilder result = new StringBuilder();

        List<ExchangeConfig> rates = countryService.getCanExchanageFeeRateList();
        for (ExchangeConfig r : rates) {
            result.append(r.getFromCd()).append(" to ").append(r.getToCd()).append(" = ").append(r.getEnabledRemittance());
            result.append("\n");
        }

        return result.toString();
    }

    public String setExchangeServiceStatus(String fromCd, String toCd, String enabled) {
        StringBuilder result = new StringBuilder();

        try {
            int affectedRow = countryService.setExchangeServiceStatus(fromCd, toCd, enabled);

            if (affectedRow == 1) {
                result.append(affectedRow + " 개의 데이터가 업데이트 되었습니다.\n");
            } else {
                result.append("잘못된 명령입니다.\n명령 사용법은 ? 표를 사용하세요.\n");
            }
        } catch (CashmallowException e) {
            result.append(e.getMessage());
        }
        return result.toString();
    }

    public String setRemittanceServiceStatus(String fromCd, String toCd, String enabled) {
        StringBuilder result = new StringBuilder();

        try {
            int affectedRow = countryService.setRemittanceServiceStatus(fromCd, toCd, enabled);

            if (affectedRow == 1) {
                result.append(affectedRow + " 개의 데이터가 업데이트 되었습니다.\n");
            } else {
                result.append("잘못된 명령입니다.\n명령 사용법은 ? 표를 사용하세요.\n");
            }
        } catch (CashmallowException e) {
            result.append(e.getMessage());
        }
        return result.toString();
    }

    public String getWalletStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);

        // sumEMoney = eMoney + cMoney : 지급예정금액 확인이 목적임
        BigDecimal sumEMoney = travelerService.getSumEMoneyByCountry(country.getCode());
        String sumEMoneyText = "0";
        if (sumEMoney != null) {
            sumEMoneyText = CustomStringUtil.localizeNumberFormat(country.getMappingInc(), sumEMoney);
        }

        BigDecimal sumRMoney = travelerService.getSumRMoneyByCountry(country.getCode());
        String sumRMoneyText = "0";
        if (sumRMoney != null) {
            sumRMoneyText = CustomStringUtil.localizeNumberFormat(country.getMappingInc(), sumRMoney);
        }

        result.append(String.format("[%s화폐]______________________ %n", countryName));
        result.append(String.format("    e_money : %s", sumEMoneyText));
        result.append("\n");
        result.append(String.format("    r_money : %s", sumRMoneyText));
        result.append("\n\n");

        return result.toString();
    }

    public String getUserStatistics(Country country) {

        StringBuilder result = new StringBuilder();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);

        Map<String, Object> map = userService.getUserCntByCountry(country.getCode());

        result.append("[").append(countryName).append("]______________________\n");
        result.append("(사용자) \n");
        result.append(String.format(" 여행자 수 : %s (%s) | 가맹점 수 : %s (%s)",
                map.get("traveler"), map.get("ina_traveler"), map.get("storekeeper"), map.get("ina_storekeeper")));
        result.append("\n\n");

        return result.toString();
    }

    public String getConfirmStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);

        Map<String, Object> tMap = travelerService.getConfirmCntByCountry(country.getCode());
        Map<String, Object> sMap = partnerService.getConfirmCntByCountry(country.getCode());
        result.append("[").append(countryName).append("]______________________\n");
        result.append("(승인) \n");
        result.append("여행자 \n");
        result.append(String.format("완료 : %s (%s) | 대기 : %s (%s) | 보류 : %s (%s)",
                tMap.get("Y"), tMap.get("ina_Y"), tMap.get("N"), tMap.get("ina_N"), tMap.get("R"), tMap.get("ina_R")));
        result.append("\n\n");
        result.append("가맹점 \n");
        result.append(String.format("완료 : %s (%s) | 대기 : %s (%s) | 보류 : %s (%s)",
                sMap.get("Y"), sMap.get("ina_Y"), sMap.get("N"), sMap.get("ina_N"), sMap.get("R"), sMap.get("ina_R")));
        result.append("\n\n");

        return result.toString();
    }

    public String getExchangeStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        // Default로 최근 3개월 데이터를 조회. 대상 : exchange, cashout, payment . 다른 조회는 기간 적용 안함. 
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);

        Date beginDate = c.getTime();
        Date endDate = new Date();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);
        Map<String, Object> map = exchangeRepositoryService.getExchangeAmountByCountry(country.getCode(), beginDate, endDate);

        result.append("[").append(countryName).append("]______________________\n");
        if (map != null) {
            result.append("(환전) - 최근 3개월  \n");
            result.append(String.format("완료  : %s | 대기 : %s", map.get("comCnt"), map.get("reqCnt")));
        }

        result.append("\n\n");

        return result.toString();
    }

    public String getCashOutStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        // Default로 최근 3개월 데이터를 조회. 대상 : exchange, cashout, payment . 다른 조회는 기간 적용 안함. 
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);

        Date beginDate = c.getTime();
        Date endDate = new Date();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);

        Map<String, Object> map = cashOutService.getCashOutAmountByCountry(country.getCode(), beginDate, endDate);
        String reqAmt = CustomStringUtil.localizeNumberFormat(country.getMappingInc(), (BigDecimal) map.get("reqAmt"));

        result.append("[").append(countryName).append("]______________________\n");
        result.append("(인출) - 최근 3개월 \n");
        result.append(String.format("완료 : %s | 예약 : %s (%s)", map.get("comCnt"), map.get("reqCnt"), reqAmt));
        result.append("\n\n");

        return result.toString();
    }

    public String getPaymentStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        // Default로 최근 3개월 데이터를 조회. 대상 : exchange, cashout, payment . 다른 조회는 기간 적용 안함. 
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);

        Date beginDate = c.getTime();
        Date endDate = new Date();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);
        Map<String, Object> map = cashOutService.getPaymentAmountByCountry(country.getCode(), beginDate, endDate);

        result.append("[").append(countryName).append("]______________________\n");
        result.append("(결제) - 최근 3개월 \n");
        result.append(String.format("완료 : %s | 대기 : %s", map.get("comCnt"), map.get("reqCnt")));
        result.append("\n\n");

        return result.toString();
    }

    public String getPaybackStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);

        Map<String, Object> map = partnerCalcService.getPaybackAmountByCountry(country.getCode());

        result.append("[").append(countryName).append("]______________________\n");
        result.append("(정산) \n");
        result.append(String.format("완료 : %s | 대기 : %s | 보류 : %s", map.get("comCnt"), map.get("reqCnt"), map.get("canCnt")));
        result.append("\n\n");

        return result.toString();
    }

    public String getRefundStatistics(Country country) {
        StringBuilder result = new StringBuilder();

        String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);

        Map<String, Object> map = refundService.getRefundAmountByCountry(country.getCode());

        result.append("[").append(countryName).append("]______________________\n");
        result.append("(환불) \n");
        result.append(String.format("완료 : %s | 대기 : %s", map.get("comCnt"), map.get("reqCnt")));
        result.append("\n\n");

        return result.toString();
    }

    public String getCountNewUsers(String startDate, String endDate) {
        StringBuilder sb = new StringBuilder();

        List<CountNewUsersAndTravelersByCountryVO> userByCountry = userService.getCountNewUsersAndTravelersByCountry(startDate, endDate);

        // List가 비어있으면 적합한 문구로 종료
        if (userByCountry.isEmpty()) {
            return "해당 기간에는 신규/인증완료 회원이 없습니다.";
        }

        for (CountNewUsersAndTravelersByCountryVO countryCount : userByCountry) {
            Country country = countryService.getCountry(countryCount.getCountryCode());
            String countryName = new Locale("", country.getIso3166()).getDisplayCountry(Locale.KOREAN);
            sb.append(String.format("[%s] 신규회원 수 : %s, 인증완료 수 : %s\n", countryName, countryCount.getUserCount(), countryCount.getTravelerCount()));
        }

        return sb.toString();
    }

    public String getStatistics(Keyword keyword) {
        StringBuilder result = new StringBuilder();

        if (Keyword.EX_SERVICE.equals(keyword)) {
            result.append(getExchangeServiceStatus());

        } else if (Keyword.RM_SERVICE.equals(keyword)) {
            result.append(getRemittanceServiceStatus());

        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("service", "Y");
            List<Country> countries = countryService.getCountryList(params);

            if (Keyword.WALLET.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getWalletStatistics(country));
                }
            } else if (Keyword.USER.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getUserStatistics(country));
                }
                result.append("* 괄호 안은 탈퇴 또는 유효기간제 분리보관 회원수 \n");
            } else if (Keyword.CONFIRM.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getConfirmStatistics(country));
                }
                result.append("* 괄호 안은 탈퇴 또는 유효기간제 분리보관 회원수 \n");
            } else if (Keyword.EXCHANGE.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getExchangeStatistics(country));
                }
            } else if (Keyword.CASHOUT.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getCashOutStatistics(country));
                }
            } else if (Keyword.PAYMENT.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getPaymentStatistics(country));
                }
            } else if (Keyword.PAYBACK.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getPaybackStatistics(country));
                }
            } else if (Keyword.REFUND.equals(keyword)) {
                for (Country country : countries) {
                    result.append(getRefundStatistics(country));
                }
            } else {
                result.append("해당 키워드는 서비스되지 않습니다.");
            }
        }

        return result.toString();
    }

}
