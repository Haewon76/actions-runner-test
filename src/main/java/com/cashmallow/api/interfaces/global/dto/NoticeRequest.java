package com.cashmallow.api.interfaces.global.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record NoticeRequest(
        Long id,
        LocalDate beginDate,
        LocalDate endDate,
        boolean popup,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt,
        // notice content
        Set<NoticeContentDto> noticeContents
) {
    @Getter
    public static class NoticeContentDto {
        @Setter private Long id;
        private String languageType;
        private String title;
        private String content;
        private Long noticeId;
    }
}
