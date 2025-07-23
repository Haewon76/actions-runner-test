package com.cashmallow.api.domain.model.customercenter;

import java.util.List;
import java.util.Map;

public interface NoticeMapper {

    // for Traveler

    NoticeContent getPopupNotice(long userId, String deviceLangKey);

    int countNoticeContents(Map<String, Object> parameters);

    List<NoticeContent> getNoticeContents(Map<String, Object> parameters);

    NoticeContent getNoticeContentTraveler(Map<String, Object> parameters);

    // for Admin

    Long getLastInsertId();

    Notice getNotice(Long id);

    int countNoticeContentsForAdmin(Map<String, Object> parameters);

    List<NoticeContent> getNoticeContentsForAdmin(Map<String, Object> parameters);

    int insertNotice(Notice notice);

    int deleteNotice(Notice notice);

    int insertNoticeContent(NoticeContent noticeContent);

    int deleteNoticeContents(NoticeContent noticeContent);

    NoticeContent getNoticeContentAdmin(Long id, String languageType);
}
