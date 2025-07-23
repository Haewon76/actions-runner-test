package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.Currency;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceRelationship;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.cashmallow.common.CommonUtil.removeNonNumeric;

/**
 * Remittance 와 분리하기 위한 RequestVO
 */

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RemittanceRequestVO {

    private Long id;
    private Long travelerId;
    private Long bankAccountId;
    private String fromCd;
    private BigDecimal fromAmt;
    private String toCd;
    private BigDecimal toAmt;
    private BigDecimal exchangeRate;
    private BigDecimal fee;
    private BigDecimal feePerAmt;
    private BigDecimal feeRateAmt;
    private BigDecimal feeRate;
    private RemittancePurpose remitPurpose;
    private RemittanceFundSource remitFundSource;
    private RemittanceRelationship remitRelationship;

    // EUR 통화 송금시 송금보내는 사람의 State 정보 필수
    private String addressStateProvince;

    private Long financePartnerId;
    private String receiverCountry;
    private String receiverBirthDate;
    private String receiverPhoneNo;
    private String receiverPhoneCountry;
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverBankName;
    private String receiverBankBranchName;

    private String receiverEmail;
    private String receiverAddress;
    private String receiverAddressCountry;
    private String receiverAddressCity;
    private String receiverAddressSecondary;

    // start of v2
    private String receiverAddressStateProvince; // State/Province/Region (USD 만 필수)

    private String receiverTypeCode; // 앱에서 보내는 필드. Remittance 객체와 DB 에서는 receiverBankCode 로 받음
    private String receiverTypeNumber; // 앱에서 보내는 필드. Remittance 객체와 DB 에서는 receiverBankAccountNo 로 받음
    private Remittance.RemittanceType remittanceType;
    private Remittance.AccountType receiverAccountType; // EUR, USD
    private String receiverZipCode;
    private String receiverIbanCode; // EUR
    private String receiverSwiftCode; // EUR
    private String receiverRoutingNumber; // USD, BDT
    private String receiverIfscCode; // INR
    private String receiverCardNumber; // CNY UnionPay
    // end of v2

    private Remittance.RemittanceStatusCode remitStatus;
    private String isConfirmedReceiverAml;
    private BigDecimal refundFee;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    private String rejectMessage;
    private List<String> receiptPhotos = new ArrayList<>();

    private Long couponUserId;
    private BigDecimal couponDiscountAmt;

    public boolean checkValidationV2() {
        // 유렵(EUR) 송금
        // ES, IE, DK, CY, EE, FI, PT, LU, BE, BG, MC, SE, PL, AT, IT, NL, LT, FR, DE, LV, MT
        if (Currency.EUR.name().equals(CountryCode.of(getToCd()).getCurrency())) {
            return validationEUR();
        // 캐나다(CA, CAD) 송금
        } else if (Currency.CAD.name().equals(CountryCode.of(getToCd()).getCurrency())) {
            return validationCAD();
        // 미국(US, USD) 송금
        } else if (Currency.USD.name().equals(CountryCode.of(getToCd()).getCurrency())) {
            return validationUSD();
        // 네팔(NP, NPR) 송금
        } else if(Currency.NPR.name().equals(CountryCode.of(getToCd()).getCurrency())) {
            return validationNPR();
        // 파키스탄(PK, PKR) 송금
        // } else if(CountryCode.of(getToCd()).getCurrency().equals("PKR")) {
        //     return validationPKR();
        // 중국(CN, CNY) 송금 및 페이 - 현재 보류지만 일단 만들어 둠
        } else if(Currency.CNY.name().equals(CountryCode.of(getToCd()).getCurrency())) {
            return validationCNY();
        // 기타 국가들
        } else {
            if(remittanceType != Remittance.RemittanceType.CASH_PICKUP) {
                if(StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverTypeNumber)) {
                    log.error("Currency: {}, RemittanceType(송금방식)={}, CASH_PICKUP이 아닐때 필수값입니다. [receiver_type_code]={}, [receiver_type_number]={}"
                            , CountryCode.of(getToCd()).getCurrency(), getRemittanceType().name(), receiverTypeCode, receiverTypeNumber);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validationEUR() {
        if (StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverTypeNumber)
                || StringUtils.isBlank(receiverIbanCode) || StringUtils.isBlank(receiverSwiftCode)) {
            log.error("Currency: {}, 필수값입니다. [receiverTypeCode]={}, [receiverTypeNumber]={} [receiver_iban_code]={}, [receiver_swift_code]={}"
                    , CountryCode.of(getToCd()).getCurrency(), receiverTypeCode, receiverTypeNumber, receiverIbanCode, receiverSwiftCode);
            return false;
        }
        return true;
    }

    public boolean validationNPR() {
        // [RemittanceType.CASH_PICKUP]  캐시 픽업인 경우 수취인 정보는 필수가 아니므로 해당 조건에 포함되지 않음
        if (remittanceType == Remittance.RemittanceType.BANK || remittanceType == Remittance.RemittanceType.WALLET) {
            if (StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverTypeNumber)) {
                log.error("Currency: {}, RemittanceType(송금방식): {}, 필수값입니다. [receiverTypeCode]={}, [receiverTypeNumber]={}"
                        , CountryCode.of(getToCd()).getCurrency(), remittanceType.name(), receiverTypeCode, receiverTypeNumber);
                return false;
            }
        }
        return true;
    }

    public boolean validationCNY() {
        // 중국은 모든 페이류가 WALLET
        if (remittanceType == Remittance.RemittanceType.WALLET && ("Weixin".equals(receiverBankName) || "Alipay".equals(receiverBankName) || "UnionPay".equals(receiverBankName))) {
            // Alipay 일 때, receiverTypeNumber 필수 (Alipay ID)
            if ("Alipay".equals(receiverBankName) && StringUtils.isBlank(receiverTypeNumber)) {
                log.error("Currency: {}, Alipay 일 때, 필수값입니다. [receiver_type_number]={}", CountryCode.of(getToCd()).getCurrency(), receiverTypeNumber);
                return false;
                // UnionPay 일 때, receiverCardNumber 필수
            } else if ("UnionPay".equals(receiverBankName) && StringUtils.isBlank(receiverCardNumber)) {
                log.error("Currency: {}, UnionPay 일 때, 필수값입니다. [receiver_card_number]={}", CountryCode.of(getToCd()).getCurrency(), receiverCardNumber);
                return false;
            } else {
                log.error("중국의 Weixin, Alipay, UnionPay 결제는 remittance_type 이 WALLET 만 가능합니다.");
                return false;
            }
        } else if(StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverTypeNumber)) {
            log.error("Currency: {}, 필수값입니다. [receiver_type_code]={}, [receiver_type_number]={}"
                    , CountryCode.of(getToCd()).getCurrency(), receiverTypeCode, receiverTypeNumber);
            return false;
        }
        return true;
    }

    public boolean validationPKR() {
        log.error("Currency: {}", CountryCode.of(getToCd()).getCurrency());
        // receiverTypeCode, receiverIbanCode 필수
        if(StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverIbanCode)) {
            log.error("Currency: {}, 필수값입니다. [receiverTypeCode]={}, [receiverIbanCode]: {}"
                    , CountryCode.of(getToCd()).getCurrency(), receiverTypeCode, receiverIbanCode);
            return false;
        }
        return true;
    }

    public boolean validationUSD() {
        if (StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverTypeNumber) || StringUtils.isBlank(receiverRoutingNumber)
                || StringUtils.isBlank(receiverZipCode) || StringUtils.isBlank(receiverAddressCity) || StringUtils.isBlank(receiverAddressStateProvince)) {
            log.error("Currency: {}, 필수값입니다. [receiverTypeCode]={}, [receiverTypeNumber]={}, [receiverRoutingNumber]={}, [receiverZipCode]={}, [receiverAddressCity]={}, [receiverAddressStateProvince]={}"
                    , CountryCode.of(getToCd()).getCurrency(), receiverTypeCode, receiverTypeNumber, receiverRoutingNumber, receiverZipCode, receiverAddressCity, receiverAddressStateProvince);
            return false;
        }
        return true;
    }

    public boolean validationCAD() {
        if (StringUtils.isBlank(receiverTypeCode) || StringUtils.isBlank(receiverTypeNumber)
                || StringUtils.isBlank(receiverZipCode) || StringUtils.isBlank(receiverAddressCity)) {
            log.error("Currency: {}, 필수값입니다. [receiverTypeCode]={}, [receiverTypeNumber]={}, [receiverZipCode]={}, [receiverAddressCity]={}"
                    , CountryCode.of(getToCd()).getCurrency(), receiverTypeCode, receiverTypeNumber, receiverZipCode, receiverAddressCity);
            return false;
        }
        return true;
    }

    @JsonIgnore
    public CountryCode getToCountry() {
        return CountryCode.of(toCd);
    }

    public String getFromCountry() {
        return CountryCode.of(fromCd).name();
    }

    public Remittance.AccountType getReceiverAccountType() {
        if (isUseEuro()) {
            // 유로는 개인 송금만 하므로 별도 입력 받지 않도록 처리
            return Remittance.AccountType.INDIVIDUAL;
        }

        return receiverAccountType;
    }

    private boolean isUseEuro() {
        return getToCountry().getCurrency().equals("EUR");
    }

    public Remittance.RemittanceType getRemittanceType() {
        if (remittanceType == null) {
            return Remittance.RemittanceType.BANK;
        }
        return remittanceType;
    }

    public String getReceiverAddress() {
        return receiverAddressCountry + ", " + receiverAddress;
    }

    public String getReceiverZipCode() {
        if(StringUtils.isBlank(receiverZipCode)) {
            return "000000";
        }
        return removeNonNumeric(receiverZipCode);
    }

    // 필드 추가 후에 꼭 여기에도 set 해줘야 함!!
    public static Remittance of(RemittanceRequestVO remittanceRequestVO) {

        Remittance remittance = new Remittance();
        remittance.setTravelerId(remittanceRequestVO.getTravelerId());
        remittance.setBankAccountId(remittanceRequestVO.getBankAccountId());
        remittance.setFromCd(remittanceRequestVO.getFromCd());
        remittance.setFromAmt(remittanceRequestVO.getFromAmt());
        remittance.setToCd(remittanceRequestVO.getToCd());
        remittance.setToAmt(remittanceRequestVO.getToAmt());
        remittance.setExchangeRate(remittanceRequestVO.getExchangeRate());
        remittance.setFee(remittanceRequestVO.getFee());
        remittance.setFeePerAmt(remittanceRequestVO.getFeePerAmt());
        remittance.setFeeRateAmt(remittanceRequestVO.getFeeRateAmt());
        remittance.setFeeRate(remittanceRequestVO.getFeeRate());
        remittance.setRemitPurpose(remittanceRequestVO.getRemitPurpose());
        remittance.setRemitFundSource(remittanceRequestVO.getRemitFundSource());
        remittance.setRemitRelationship(remittanceRequestVO.getRemitRelationship());

        remittance.setFinancePartnerId(remittanceRequestVO.getFinancePartnerId());

        remittance.setReceiverCountry(remittanceRequestVO.getReceiverCountry());
        remittance.setReceiverBirthDate(remittanceRequestVO.getReceiverBirthDate());
        remittance.setReceiverPhoneNo(remittanceRequestVO.getReceiverPhoneNo());
        remittance.setReceiverPhoneCountry(remittanceRequestVO.getReceiverPhoneCountry());
        remittance.setReceiverFirstName(remittanceRequestVO.getReceiverFirstName());
        remittance.setReceiverLastName(remittanceRequestVO.getReceiverLastName());
        remittance.setReceiverBankName(remittanceRequestVO.getReceiverBankName());
        remittance.setReceiverBankBranchName(remittanceRequestVO.getReceiverBankBranchName());
        remittance.setReceiverBankCode(remittanceRequestVO.getReceiverTypeCode());
        remittance.setReceiverBankAccountNo(remittanceRequestVO.getReceiverTypeNumber());

        remittance.setReceiverEmail(remittanceRequestVO.getReceiverEmail());
        remittance.setReceiverAddress(remittanceRequestVO.getReceiverAddress());
        remittance.setReceiverAddressCountry(remittanceRequestVO.getReceiverAddressCountry());
        remittance.setReceiverAddressCity(remittanceRequestVO.getReceiverAddressCity());
        remittance.setReceiverAddressSecondary(remittanceRequestVO.getReceiverAddressSecondary());

        // start of v2
        remittance.setReceiverAddressStateProvince(remittanceRequestVO.getReceiverAddressStateProvince());

        remittance.setRemittanceType(Remittance.RemittanceType.valueOf(remittanceRequestVO.getRemittanceType().name()));

        if(remittanceRequestVO.getReceiverAccountType() != null) {
            remittance.setReceiverAccountType(Remittance.AccountType.valueOf(remittanceRequestVO.getReceiverAccountType().name()));
        }

        remittance.setReceiverZipCode(remittanceRequestVO.getReceiverZipCode());
        remittance.setReceiverIbanCode(remittanceRequestVO.getReceiverIbanCode());
        remittance.setReceiverSwiftCode(remittanceRequestVO.getReceiverSwiftCode());
        remittance.setReceiverRoutingNumber(remittanceRequestVO.getReceiverRoutingNumber());
        remittance.setReceiverIfscCode(remittanceRequestVO.getReceiverIfscCode());
        remittance.setReceiverCardNumber(remittanceRequestVO.getReceiverCardNumber());
        // end of v2

        if(remittanceRequestVO.getRemitStatus() != null) {
            remittance.setRemitStatus(Remittance.RemittanceStatusCode.valueOf(remittanceRequestVO.getRemitStatus().name()));
        }

        remittance.setIsConfirmedReceiverAml(remittanceRequestVO.getIsConfirmedReceiverAml());
        remittance.setRefundFee(remittanceRequestVO.getRefundFee());
        remittance.setCreatedDate(remittanceRequestVO.getCreatedDate());
        remittance.setUpdatedDate(remittanceRequestVO.getUpdatedDate());

        remittance.setRejectMessage(remittanceRequestVO.getRejectMessage());
        remittance.setReceiptPhotos(remittanceRequestVO.getReceiptPhotos());

        remittance.setCouponUserId(remittanceRequestVO.getCouponUserId());
        remittance.setCouponDiscountAmt(remittanceRequestVO.getCouponDiscountAmt());

        return remittance;
    }

}
