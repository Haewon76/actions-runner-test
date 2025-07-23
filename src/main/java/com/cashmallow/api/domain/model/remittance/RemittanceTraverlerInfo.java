package com.cashmallow.api.domain.model.remittance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * Traverler의 은행정보가 포함된 송금정보
 *
 * @author bongseok
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RemittanceTraverlerInfo extends Remittance {

    private Long remitId;
    private String bankCode;
    private String bankName;
    private String accountNo;
    private String accountName;
}
