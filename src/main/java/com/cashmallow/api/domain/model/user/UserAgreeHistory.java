package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.model.terms.TermsType;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserAgreeHistory {
    private Long id;
    private Long userId;
    private Integer version;
    private TermsType termsType;
    private Timestamp createdAt;
    private boolean agreed;
}
