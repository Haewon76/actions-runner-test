package com.cashmallow.api.domain.model.memo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemoLog {

    private Long id;

    private Long memoId;

    private Long refId;

    private String type;

    private String memo;

    private Timestamp createdAt;

    private Long creatorId;


}
