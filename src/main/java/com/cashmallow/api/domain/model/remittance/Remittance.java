package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceRelationship;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * Domain model for Remittance
 *
 * @author bongseok
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Remittance {

    public enum RemittanceStatusCode {
        OP,    // 신청
        DR,    // Deposit receipt Reregistration 영수증 재등록
        DP,    // 입금완료, 외부 인터페이스 시작
        CF,    // 송금 완료
        TC,    // 신청 후 입금전 취소
        CC,    // 신청 후 타임아웃으로 인한 취소
        RP,    // RR이후 환불 진행중
        RF,    // RP이후 환불 완료
        RR,    // 송금실패로 인해 재등록 요청상태
        RC     // 송금실패로 인해 재등록 완료상태
    }

    public enum AccountType {
        SAVING, CURRENT, CHECKING, INDIVIDUAL, CORPORATE
    }

    public enum RemittanceType {
        BANK, WALLET, CASH_PICKUP
    }

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

    private Long financePartnerId;
    private String receiverCountry;
    private String receiverBirthDate;
    private String receiverPhoneNo;
    private String receiverPhoneCountry;
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverBankName;
    private String receiverBankBranchName;
    private String receiverBankCode; // 앱에서 보내는 필드 receiverTypeCode 를 여기에 set 해줌. Remittance 객체와 DB 에서는 receiverBankCode 로 받음
    private String receiverBankAccountNo; // 앱에서 보내는 필드 receiverTypeNumber 를 여기에 set 해줌. Remittance 객체와 DB 에서는 receiverBankAccountNo 로 받음

    private String receiverEmail;
    private String receiverAddress;
    private String receiverAddressCountry;
    private String receiverAddressCity;
    private String receiverAddressSecondary;

    // start of v2
    private String receiverAddressStateProvince; // State/Province/Region (USD 만 필수)

    private String receiverTypeCode; // use v2, receiverBankCode
    private String receiverTypeNumber; // use v2, receiverBankAccountNo
    private RemittanceType remittanceType;
    private AccountType receiverAccountType; // EUR, USD
    private String receiverZipCode;
    private String receiverIbanCode; // EUR
    private String receiverSwiftCode; // EUR
    private String receiverRoutingNumber; // USD, BDT
    private String receiverIfscCode; // INR
    private String receiverCardNumber; // CNY UnionPay
    // end of v2

    private RemittanceStatusCode remitStatus;
    private String isConfirmedReceiverAml;
    private BigDecimal refundFee;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    private String rejectMessage;

    private List<String> receiptPhotos = new ArrayList<>();

    private Long couponUserId;

    private BigDecimal couponDiscountAmt;

    private int version;

    public boolean checkValidation() {
        return bankAccountId != null
                && fromCd != null && !fromCd.isEmpty()
                && fee != null
                && fromAmt != null
                && toCd != null && !toCd.isEmpty()
                && toAmt != null
                && exchangeRate != null;
    }


    // 탈퇴, 휴면으로 익명화
    public void anonymize() {
        receiverBirthDate = "*";
        receiverPhoneNo = "*";
        receiverFirstName = "*";
        receiverLastName = "*";
        receiverBankAccountNo = "*";
        receiverAddress = "*";
        receiverAddressSecondary = "*";
    }

    @JsonIgnore
    public CountryCode getToCountry() {
        return CountryCode.of(toCd);
    }

    public void updateExchangeRate(ExchangeCalcVO exchangeCalcVO) {
        this.exchangeRate = exchangeCalcVO.getExchange_rate();

        this.fee = exchangeCalcVO.getFee();
        this.feePerAmt = exchangeCalcVO.getFee_per_amt();
        this.feeRateAmt = exchangeCalcVO.getFee_rate_amt();

        this.fromAmt = exchangeCalcVO.getFee().add(exchangeCalcVO.getFrom_money());
        this.fromCd = exchangeCalcVO.getFrom_cd();

        this.toCd = exchangeCalcVO.getTo_cd();
        this.toAmt = exchangeCalcVO.getTo_money();

        this.couponUserId = exchangeCalcVO.getCouponUserId();
    }

    public String getFromCountry() {
        return CountryCode.of(fromCd).name();
    }
}