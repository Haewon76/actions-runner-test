package com.cashmallow.api.interfaces.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class AdminDbsRemittanceAskVO {

    private String id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String relatedTxnType;
    private Long relatedTxnId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp beginCreatedDate;   // 생성일 또는 업데이트일에 대하여 range 검색(begin_created_date ~ end_created_date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp endCreatedDate;

    private Integer startRow;
    private Integer page;
    private Integer size;
    private String sort;
    private String searchValue;
}
