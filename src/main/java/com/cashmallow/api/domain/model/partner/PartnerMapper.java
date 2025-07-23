package com.cashmallow.api.domain.model.partner;

import org.apache.ibatis.annotations.Mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PartnerMapper {

    /**
     * Get storekeeper information by userId
     *
     * @param userId
     * @return
     */
    WithdrawalPartner getWithdrawalPartnerByUserId(Long userId);

    /**
     * Get storekeeper information by storekeeperId
     *
     * @param withdrawalPartnerId
     * @return
     */
    WithdrawalPartner getWithdrawalPartnerByWithdrawalPartnerId(Long withdrawalPartnerId);

    /**
     * Get storekeeper list by country, cashOutService
     *
     * @param country, cashOutService
     * @return
     */
    List<WithdrawalPartner> getWithdrawalPartnerListByCountry(Map<String, Object> params);

    /**
     * Get withdrawalPartner list by kindOfStorekeeper
     *
     * @param userId
     * @return
     */
    List<WithdrawalPartner> getWithdrawalPartnerListByKindOfStorekeeper(String kindOfStorekeeper);

    /**
     * Get storekeeper atm list nearby (Long storekeeperId, Double lat, Double lng)
     *
     * @param params
     * @return
     */
    List<WithdrawalPartnerCashpoint> getWithdrawalPartnerCashpointListNearby(Map<String, Object> params);

    /**
     * Get storekeeper confirm count by country
     *
     * @param country
     * @return
     */
    Map<String, Object> getConfirmCntByCountry(String country);

    /**
     * Update a WithdrawalPartner
     *
     * @param WithdrawalPartner
     * @return
     */
    int updateWithdrawalPartner(WithdrawalPartner withdrawalPartner);

    int insertWithdrawalPartnerMaintenance(long withdrawalPartnerId, ZonedDateTime startAt, ZonedDateTime endAt);

    List<WithdrawalPartnerMaintenance> selectWithdrawalPartnerMaintenances(Long withdrawalPartnerId);
}
