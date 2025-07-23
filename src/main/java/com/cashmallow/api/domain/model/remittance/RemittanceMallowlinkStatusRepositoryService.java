package com.cashmallow.api.domain.model.remittance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Webhook으로 결과를 받아 remittance_mallowlink status 변경한 히스토리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RemittanceMallowlinkStatusRepositoryService {

    private final RemittanceMallowlinkMapper remittanceMallowlinkMapper;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void insertRemittanceMallowlinkStatus(RemittanceMallowlinkStatus remittanceMallowlinkStatus) {
        remittanceMallowlinkMapper.insertRemittanceMallowlinkStatus(remittanceMallowlinkStatus);
    }

}
