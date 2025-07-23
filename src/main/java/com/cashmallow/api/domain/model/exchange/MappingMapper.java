package com.cashmallow.api.domain.model.exchange;

import com.cashmallow.api.interfaces.admin.dto.MappingRegVO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface MappingMapper {

    /* Read */

    List<BigDecimal> getMappedPinValues(Map<String, Object> params);

    Mapping findPinInfoByPinValue(Map<String, Object> params);

    /**
     * Get Mapping data for mapping with CSV data
     *
     * @param params (country, bankAccountId, pinValue, refValue, travelerId)
     * @return
     */
    Mapping getMappingForMapping(Map<String, Object> params);

    /**
     * Get Mapping date by travelerId and remitId
     *
     * @param params (travelerId, remitId)
     * @return
     */
    Mapping getMappingByRemitId(Map<String, Object> params);

    /**
     * Get current date
     *
     * @return
     */
    Timestamp getCurDate();


    /* Write */

    /**
     * @param mappingRegVO
     * @return
     */
    Integer putPinValue(MappingRegVO mappingRegVO);

    /**
     * Update mapping. status, exchangeId, remitId
     *
     * @param mapping
     * @return
     */
    int updateMapping(Mapping mapping);

    /**
     * Update mapping table with a generated exchangeId after the request exchange. 환전 신청 후 생성된 exchangeId 를 mapping table 에 업데이트
     *
     * @param params (exchangeId, mappingId, travelerId)
     * @return
     */
    Integer updateExchangeIdAfterReqExchange(Map<String, Object> params);

    int updateRemitIdAfterReqRemittance(Map<String, Object> params);

    /**
     * Update mapping table cancel pin value
     *
     * @param params
     * @return
     */
    Integer cancelExchangePinValueByTravelerId(Long travelerId);

    int cancelRemittancePinValueByTravelerId(Long remitId);
}
