package com.cashmallow.api.application.impl;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.partner.PartnerMapper;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerMaintenance;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.interfaces.devoffice.web.dto.AddPartnerMaintenancesRequest;
import com.cashmallow.api.interfaces.devoffice.web.dto.GetPartnerMaintenancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartnerServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(PartnerServiceImpl.class);

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private PartnerMapper partnerMapper;


    //-------------------------------------------------------------------------------
    // 30. 가맹점
    //-------------------------------------------------------------------------------

    @Transactional
    public int updateWithdrawalPartner(WithdrawalPartner withdrawalPartner) {
        return partnerMapper.updateWithdrawalPartner(withdrawalPartner);
    }

    //-------------------------------------------------------------------------------
    // 8. 가맹점
    //-------------------------------------------------------------------------------

    // 기능: 8.1. userId의 가맹점 정보 읽기
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public WithdrawalPartner getWithdrawalPartnerByUserId(Long userId) {
        WithdrawalPartner withdrawalPartner = partnerMapper.getWithdrawalPartnerByUserId(userId);

        return withdrawalPartner;
    }

    // 기능: 8.2. 가맹점 id의 가맹점 정보 읽기
    public WithdrawalPartner getWithdrawalPartnerByWithdrawalPartnerId(Long withdrawalPartnerId) {
        return partnerMapper.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartner> getStorekeeperListByKindOfStorekeeper(String kindOfStorekeeper) {
        return partnerMapper.getWithdrawalPartnerListByKindOfStorekeeper(kindOfStorekeeper);
    }

    // 기능: 8.2.2 가맹점 정보 상세 조회 API - 가맹점 ID.로 가맹점 상세 정보 조회.
    public Map<String, Object> getWithdrawalPartnerExtByWithdrawalPartnerId(Long withdrawalPartnerId) {
        Map<String, Object> mapExt = new HashMap<>();

        WithdrawalPartner withdrawalPartner = getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);

        if (withdrawalPartner != null) {
            User user = userRepositoryService.getUserByUserId(withdrawalPartner.getUserId());

            if (user != null) {

                // remove privacy
                user.setPasswordHash(null);
                user.setLogin(null);
                user.setBirthDate(null);
                withdrawalPartner.setBusinessNo(null);
                withdrawalPartner.setBusinessPhoto(null);
                withdrawalPartner.setAccountNo(null);
                withdrawalPartner.setAccountName(null);
                withdrawalPartner.setBankName(null);

                mapExt.put("withdrawalPartner", withdrawalPartner);
                mapExt.put("storekeeper", withdrawalPartner);
                mapExt.put("user", user);
            }
        }
        return mapExt;
    }

    /**
     * Get confirm count by country
     *
     * @param country
     * @return : total, Y, N, R
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getConfirmCntByCountry(String country) {
        return partnerMapper.getConfirmCntByCountry(country);
    }

    //-------------------------------------------------------------------------------
    // 10. 가맹점 찾기
    //-------------------------------------------------------------------------------

    // 국가별 가맹점 리스트
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartner> getWithdrawalPartnerListByCountry(String country, String cashOutService) {
        Map<String, Object> params = new HashMap<>();
        params.put("country", country);
        params.put("cashOutService", cashOutService);

        return partnerMapper.getWithdrawalPartnerListByCountry(params);
    }

    /**
     * Get storekeeper atm list
     *
     * @param withdrawalPartnerId
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartnerCashpoint> getWithdrawalPartnerCashpointListByWithdrawalPartnerId(Long withdrawalPartnerId, Double lat, Double lng) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("withdrawalPartnerId", withdrawalPartnerId);
        params.put("lat", lat);
        params.put("lng", lng);
        return partnerMapper.getWithdrawalPartnerCashpointListNearby(params);
    }

    //-------------------------------------------------------------------------------
    // 11. 가맹점 지급 가능 금액 API
    //-------------------------------------------------------------------------------

    // todo cache
    // @CacheEvict(value = "getPartnerMaintenances", key = "#request.kindOfStorekeeper")
    @Transactional
    public ResponseEntity<String> addPartnerMaintenance(AddPartnerMaintenancesRequest request) {
        String kindOfStorekeeper = request.getKindOfStorekeeper();

        List<WithdrawalPartner> withdrawalPartnerListByKindOfStorekeeper = partnerMapper.getWithdrawalPartnerListByKindOfStorekeeper(kindOfStorekeeper);
        if (withdrawalPartnerListByKindOfStorekeeper.size() != 1) {
            throw new RuntimeException("올바르지 않은 파트너 타입 kindOfStorekeeper:" + kindOfStorekeeper);
        }
        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new RuntimeException("종료시각은 시작시각 이후여야 합니다.");
        }
        if (request.getEndAt().isBefore(ZonedDateTime.now())) {
            throw new RuntimeException("종료시각은 현재시각 이후여야 합니다.");
        }

        WithdrawalPartner withdrawalPartner = withdrawalPartnerListByKindOfStorekeeper.get(0);
        Long withdrawalPartnerId = withdrawalPartner.getId();

        partnerMapper.insertWithdrawalPartnerMaintenance(withdrawalPartnerId, request.getStartAt(), request.getEndAt());

        return ResponseEntity.ok().body("OK");
    }

    // @Cacheable(value = "getPartnerMaintenances", key = "#kindOfStorekeeper")
    public GetPartnerMaintenancesResponse getPartnerMaintenances(String kindOfStorekeeper) {
        List<WithdrawalPartner> withdrawalPartnerListByKindOfStorekeeper = partnerMapper.getWithdrawalPartnerListByKindOfStorekeeper(kindOfStorekeeper);
        if (withdrawalPartnerListByKindOfStorekeeper.size() != 1) {
            throw new RuntimeException("올바르지 않은 파트너 타입 kindOfStorekeeper:" + kindOfStorekeeper);
        }

        WithdrawalPartner withdrawalPartner = withdrawalPartnerListByKindOfStorekeeper.get(0);
        return GetPartnerMaintenancesResponse.of(withdrawalPartner, getPartnerMaintenances(withdrawalPartner));
    }

    // @Cacheable(value = "getPartnerMaintenances", key = "#withdrawalPartner.kindOfStorekeeper")
    public List<WithdrawalPartnerMaintenance> getPartnerMaintenances(WithdrawalPartner withdrawalPartner) {
        List<WithdrawalPartnerMaintenance> withdrawalPartnerMaintenances = partnerMapper.selectWithdrawalPartnerMaintenances(withdrawalPartner.getId());

        String country = withdrawalPartner.getGmCountryCode();
        ZoneId zoneId = CountryCode.of(country).getZoneId();
        for (var wp : withdrawalPartnerMaintenances) {
            wp.setStartAt(wp.getStartAt().withZoneSameInstant(zoneId));
            wp.setEndAt(wp.getEndAt().withZoneSameInstant(zoneId));
            wp.setCreateAt(wp.getCreateAt().withZoneSameInstant(CountryCode.KR.getZoneId()));
        }

        return withdrawalPartnerMaintenances;

    }

}