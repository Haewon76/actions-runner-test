package com.cashmallow.api.application.impl;

import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.aml.octa.OctaAmlService;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RemittanceAdminService {

    private final MallowlinkRemittanceServiceImpl mallowlinkRemittanceService;

    private final RemittanceRepositoryService remittanceRepositoryService;
    private final OctaAmlService octaAmlService;


    @Transactional
    public void confirmReceiverAmlByAdmin(Long remitId) throws CashmallowException {

        String method = "confirmReceiverAmlByAdmin()";
        log.info("{}: confirmReceiverAmlByAdmin. remitId={}", method, remitId);

        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);
        remittance.setIsConfirmedReceiverAml("Y");

        try {
            octaAmlService.validateRemittanceAmlList(remittance);
        } catch (Exception e) {
            log.info("{} : 해당 수취인의 AML리스트가 존재합니다. 확인 바랍니다.", method);
            throw new CashmallowException("해당 수취인의 AML리스트가 존재합니다. 확인 바랍니다.");
        }

        // 송금실패로(RC) 인해 재등록 완료상태일 경우 AML완료시 paygate로 요청
        if (Remittance.RemittanceStatusCode.RC.equals(remittance.getRemitStatus())) {
            log.info("송금실패로(RC) 인해 재등록 완료상태일 경우 AML완료시 송금 호출...");

            remittance.setRemitStatus(Remittance.RemittanceStatusCode.DP);
            mallowlinkRemittanceService.requestRemittance(remittance);
        }

        int updateRow = remittanceRepositoryService.updateRemittance(remittance);
        if (updateRow != 1) {
            log.error("{}: 어드민 요청에 의한 수취인 AML 승인에 실패했습니다. remitId={}, updateRow={}", method, remittance.getId(), updateRow);
            throw new CashmallowException("어드민 요청에 의한 수취인 AML 승인에 실패했습니다. remitId=" + remittance.getId());
        }
    }
}
