package com.cashmallow.api.domain.model.company;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionRecord {
    private Long id;
    private String relatedTxnType;
    private Long relatedTxnId;
    private String iso4217;
    private BigDecimal amount;
    private Long upperId;
    private Long rootId;
    private String description;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;
    private String fundingStatus;
    private String exchangeTxnId;
    private String sendMoneyTxnId;
    private String sendMoneyStatus;

    private boolean manual;

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public enum RelatedTxnType {
        EXCHANGE,   // 환전
        REMITTANCE, // 송금
        FUND,       // 자금이동
        REFUND,    // 환불
        CASH_OUT,    // 인출
        REPAYMENT  // 반환
    }

    public enum FundingStatus {
        PENDING,    // 보류 중(매핑 완료 && Paygate Ttransaction 대기 중)
        CONFIRM,    // 입금 확인(매핑 완료 && Paygate Transaction Confirm 완료) 25.04.10 페이게이트 관련로직 삭제로 인해 중복방지 UniqueKey로 사용
        WAITING,    // 페이게이트 풀링 대기 중(인출 완료 or 송금 완료)
        COMPLETE,   // 페이게이트 풀링 완료
        CANCEL,     // 환불이라던가 전액환불해서 Paygate API로 캔슬한 경우.
        FAILED,     // Paygate Transaction을 Confirm하는 과정에서 정상적으로 처리되지 못한 경우
        NA            // 해당 없음(기타 등등)
    }

    public enum Description {
        RE_EXCHANGE_REFUND,    // 유저 환불
        OTHER_REVENUE,        // 기타수익
        REFUND_CHARGE,        // 환불 수수료
        CANCELLED_REFUND,    // 거래취소 환불
        RETRACTION_REFUND,    // 거래철회 환불
        REJECTED_REFUND,    // 거래거부 환불
        EXCEED_REFUND,        // 차액 환불
        WRONG_DEPSIT        // 잘못된 입금
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRelatedTxnType() {
        return relatedTxnType;
    }

    public void setRelatedTxnType(String relatedTxnType) {
        this.relatedTxnType = relatedTxnType;
    }

    public Long getRelatedTxnId() {
        return relatedTxnId;
    }

    public void setRelatedTxnId(Long relatedTxnId) {
        this.relatedTxnId = relatedTxnId;
    }

    public String getIso4217() {
        return iso4217;
    }

    public void setIso4217(String iso4217) {
        this.iso4217 = iso4217;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getUpperId() {
        return upperId;
    }

    public void setUpperId(Long upperId) {
        this.upperId = upperId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public String getFundingStatus() {
        return fundingStatus;
    }

    public void setFundingStatus(String fundingStatus) {
        this.fundingStatus = fundingStatus;
    }

    public String getExchangeTxnId() {
        return exchangeTxnId;
    }

    public void setExchangeTxnId(String exchangeTxnId) {
        this.exchangeTxnId = exchangeTxnId;
    }

    public String getSendMoneyTxnId() {
        return sendMoneyTxnId;
    }

    public void setSendMoneyTxnId(String sendMoneyTxnId) {
        this.sendMoneyTxnId = sendMoneyTxnId;
    }

    public String getSendMoneyStatus() {
        return sendMoneyStatus;
    }

    public void setSendMoneyStatus(String sendMoneyStatus) {
        this.sendMoneyStatus = sendMoneyStatus;
    }

}
