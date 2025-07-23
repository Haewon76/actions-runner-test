package com.cashmallow.api.domain.model.fx;

import org.apache.ibatis.annotations.Update;

public interface FxMapper {

    void addFxQuotation(FxQuotationEntity entity);

    FxQuotationEntity getFxQuotation(Long id);

    FxQuotationEntity getFxQuotationByTransactionId(String transactionId);

    @Update("""
                    UPDATE fx_quotation
                    SET status = #{status}, updated_at = NOW()
                    WHERE id = #{quotationId}
                    AND status != 'COMPLETED'
            """)
    void updateFxQuotationStatus(Long quotationId, String status);

}
