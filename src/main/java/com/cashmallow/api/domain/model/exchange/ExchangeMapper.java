package com.cashmallow.api.domain.model.exchange;

import com.cashmallow.api.interfaces.admin.dto.AdminExchangeAskVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ExchangeMapper {

    /* Exchange table Read */

    /**
     * Get exchange information by exchange id
     *
     * @param exchangeId
     * @return
     */
    Exchange getExchangeByExchangeId(Long exchangeId);

    /**
     * Get exchange list on progress by traveler id
     *
     * @param travelerId
     * @return
     */
    List<Exchange> getLastestExchangeInProgressByTravelerId(Long travelerId);

    /**
     * Get exchange list on progress by traveler id
     *
     * @param travelerId
     * @return
     */
    List<Exchange> getExchangeOpListByTravelerId(Long travelerId);

    /**
     * Get exchange list by traveler id
     *
     * @param travelerId
     * @return
     */
    List<Exchange> getExchangeListByTravelerId(Long travelerId);

    /**
     * Get exchange list by traveler id
     *
     * @param travelerIds
     * @return
     */
    int countExchangeListByTravelerIds(List<Long> travelerIds);

    /**
     * Get exchange list by exchange ids
     *
     * @param exchangeIds
     * @return
     */
    List<Exchange> getExchangeListByExchangeIds(List<Long> exchangeIds);

    /**
     * 여행자 특정 기간 환전 금액 조회
     *
     * @param params : travelerId, fromCd, fromDate, toDate (yyyyMMdd)
     * @return (fromAmtSum, cnt)
     */
    Map<String, Object> getExchangeFromAmtSumByPeriod(Map<String, Object> params);

    /**
     * 여행자 특정 기간 환전 + 송금 금액 조회
     *
     * @param params
     * @return
     */
    Map<String, Object> getFromAmtSumByPeriod(Map<String, Object> params);


    /**
     * 여행자 특정 기간 환전 금액 조회 - toCd 기준
     *
     * @param params : travelerId, toCd, fromDate, toDate (yyyyMMdd)
     * @return (toAmtSum, cnt)
     */
    Map<String, Object> getExchangeToAmtSumByPeriod(Map<String, Object> params);

    /**
     * 여행자 특정 기간 환전 금액 조회 - toCd 기준
     *
     * @param params : travelerId, toCd, fromDate, toDate (yyyyMMdd)
     * @return (toAmtSum, cnt)
     */
    Map<String, Object> getToAmtSumByPeriod(Map<String, Object> params);

    Map<String, Object> getToAmtRemittanceSumByPeriod(Map<String, Object> params);

    /**
     * 여행자 특정 기간 인출 가능 금액 조회 - toCd 기준
     *
     * @param params : travelerId, toCd, fromDate, toDate (yyyyMMdd)
     * @return (toAmtSum, cnt)
     */
    Map<String, Object> getWithdrawalToAmtSumByPeriod(Map<String, Object> params);


    /**
     * traveler ID 환전 정보 읽기 (여행자 환전 히스토리) count
     *
     * @param travelerId
     * @return
     */
    int countExchangeByTravelerId(Map<String, Object> params);

    /**
     * traveler ID 환전 정보 읽기 (여행자 환전 히스토리)
     *
     * @param params
     * @return
     */
    List<Object> getExchangeByTravelerId(Map<String, Object> params);

    /**
     * Count for 'Find Exchange List'
     *
     * @param params
     * @return
     */
    int countAdminExchange(AdminExchangeAskVO adminExchangeAskVO);

    /**
     * Find Exchange List
     *
     * @param adminExchangeAskVO
     * @return
     */
    List<Object> findAdminExchange(AdminExchangeAskVO adminExchangeAskVO);

    /**
     * Get exchange amount statistics by country
     *
     * @param params (beginDate("yyyy-MM-dd HH:mm:dd"), endDate("yyyy-MM-dd HH:mm:dd"), country)
     * @return
     */
    Map<String, Object> getExchangeAmountByCountry(Map<String, Object> params);


    /* Exchange table Write */

    /**
     * Insert exchange table
     *
     * @param exchange
     * @return
     */
    int insertExchange(Exchange exchange);

    /**
     * Update exchange table
     *
     * @param exchange
     * @return
     */
    int updateExchange(Exchange exchange);

    int updateExchangeTrAccountInfo(Exchange exchange);

    int insertExchangeDepositReceipt(ExchangeDepositReceipt exchangeDepositReceipt);

    List<ExchangeDepositReceipt> getExchangeDepositReceiptList(Long exchangeId);

    @Insert(" INSERT INTO exchange_status (exchange_id, exchange_status, created_date, message) VALUES (#{exchangeId}, #{exchangeStatus}, NOW(), #{message}); ")
    void insertExchangeStatus(ExchangeStatus exchange);

    @Select(" SELECT message FROM exchange_status WHERE exchange_id = #{id} AND exchange_status = #{exStatus} ORDER BY created_date DESC LIMIT 1; ")
    String getExchangeRejectMessage(Long id, String exStatus);

    int updateExchangeBankAccountId(Exchange exchange);

    /**
     * Find admin exchange subquery 1.
     *
     * @param adminExchangeAskVO the AdminExchangeAskVO object.
     * @return the list of objects.
     */
    @MapKey(value = "getAdminExchangeSubQuery1") //@MapKey is required : warn
    List<Map<String, Object>> findAdminExchangeSubQuery1(AdminExchangeAskVO adminExchangeAskVO);

    /**
     * 진행중인 모든 환전 정보를 가져온다.(ex_status = 'OP')
     *
     * @return
     */
    Exchange getExchangeBeforeMapping(long id);

    List<Exchange> getExchangeBeforeMappingList(String accountNo);
}
