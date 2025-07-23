package com.cashmallow.api.interfaces.fx;

import com.cashmallow.api.domain.model.fx.FxMapper;
import com.cashmallow.api.domain.model.fx.FxQuotationEntity;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.dbs.model.dto.CashmallowFxQuotationRequest;
import com.cashmallow.api.interfaces.dbs.model.dto.CashmallowFxQuotationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FxService {

    private final FxMapper fxMapper;

    public FxQuotationEntity getFxQuotoationResult(Long quotationId) {
        return fxMapper.getFxQuotation(quotationId);
    }

    public void updateFxQuotationStatus(Long quotationId, String status) {
        fxMapper.updateFxQuotationStatus(quotationId, status);
    }

    public Long addFxQuotation(CashmallowFxQuotationRequest request, CashmallowFxQuotationResponse dbsFxQuotation) throws CashmallowException {
        final FxQuotationEntity quotationEntity = new FxQuotationEntity(request, dbsFxQuotation);
        fxMapper.addFxQuotation(quotationEntity);
        return quotationEntity.getId();
    }

    public FxQuotationEntity getFxQuotationByTransactionId(String transactionId) {
        return fxMapper.getFxQuotationByTransactionId(transactionId);
    }

}
