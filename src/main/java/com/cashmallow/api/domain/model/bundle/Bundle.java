package com.cashmallow.api.domain.model.bundle;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Bundle {
    private Long id;
    private String version;
    private String platform;
    private String hashSha1;
    private Long size;
    private String isActive;
    private String fileName;
    private Long createdId;
    private Timestamp createdAt;
    private Long updatedId;
    private Timestamp updatedAt;

    /**
     * 번들에 대한 설명을 나타내는 변수입니다.
     * 번들에 대한 추가적인 정보를 제공하는 데 사용됩니다.
     */
    private String description;
}
