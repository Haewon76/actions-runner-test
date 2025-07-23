package com.cashmallow.api.domain.model.edd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEddLog {

    private Long id;

    private Long userEddId;

    private Long userId;

    private BigDecimal amount;

    private Integer count;

    private String limited;

    private String firstName;
    private String lastName;
    private String email;
    private String korName;

    private Long creatorId;

    private Timestamp initAt;

    private Timestamp createdAt;

    private Integer memoCount;

    private Integer imageCount;

    private String searchStartAt;
    private String searchEndAt;

    private String initIp;

}
