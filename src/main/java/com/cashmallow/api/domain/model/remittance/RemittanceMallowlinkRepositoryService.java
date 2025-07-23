package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkRemittanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RemittanceMallowlinkRepositoryService {

    private final RemittanceMallowlinkMapper remittanceMallowlinkMapper;

    private final RemittanceRepositoryService remittanceRepositoryService;

    private final RemittanceMallowlinkStatusRepositoryService statusRepositoryService;

    @Transactional
    public void insertRemittanceMallowlink(RemittanceMallowlink remittanceMallowlink) {
        remittanceMallowlinkMapper.insertRemittanceMallowlink(remittanceMallowlink);
        statusRepositoryService.insertRemittanceMallowlinkStatus(RemittanceMallowlinkStatus.of(remittanceMallowlink));
    }

    public RemittanceMallowlink getRemittanceMallowlinkByTransactionId(String transactionId) {
        return remittanceMallowlinkMapper.selectRemittanceMallowlinkByTransactionId(transactionId);
    }

    public RemittanceMallowlink getRemittanceMallowlinkByRemitId(Long remitId) {
        return remittanceMallowlinkMapper.getRemittanceMallowlinkByRemitId(remitId);
    }

    @Transactional
    public RemittanceMallowlink updateRemittanceMallowlinkStatus(RemittanceMallowlink remittanceMallowlink, MallowlinkRemittanceStatus status) {

        remittanceMallowlink.setStatus(status);
        remittanceMallowlinkMapper.updateStatusOfRemittanceMallowlink(remittanceMallowlink.getRemitId(), remittanceMallowlink.getStatus());

        statusRepositoryService.insertRemittanceMallowlinkStatus(RemittanceMallowlinkStatus.of(remittanceMallowlink));

        return remittanceMallowlink;
    }

    public Optional<Remittance> getRemittanceByMallowlinkTransactionId(String transactionId) {
        return remittanceMallowlinkMapper.getRemitIdByMallowlinkTransactionId(transactionId)
                .map(remittanceRepositoryService::getRemittanceByRemittanceId);
    }

    public RemittanceMallowlinkStatus getRecentRemittanceMallowlinkStatus(String transactionId) {
        return remittanceMallowlinkMapper.getRecentRemittanceMallowlinkStatus(transactionId);
    }
}
