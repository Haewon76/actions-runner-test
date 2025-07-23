package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkRemittanceStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface RemittanceMallowlinkMapper {

    int insertRemittanceMallowlink(RemittanceMallowlink remittanceMallowlink);

    RemittanceMallowlink selectRemittanceMallowlinkByTransactionId(String transactionId);

    RemittanceMallowlink getRemittanceMallowlinkByRemitId(Long remitId);

    int updateStatusOfRemittanceMallowlink(Long remitId, MallowlinkRemittanceStatus status);

    int insertRemittanceMallowlinkStatus(RemittanceMallowlinkStatus remittanceMallowlinkStatus);

    Optional<Long> getRemitIdByMallowlinkTransactionId(String transactionId);

    RemittanceMallowlinkStatus getRecentRemittanceMallowlinkStatus(String transactionId);
}
