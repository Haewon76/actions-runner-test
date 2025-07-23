package com.cashmallow.api.application.impl;

import com.cashmallow.api.domain.model.customercenter.Notice;
import com.cashmallow.api.domain.model.customercenter.NoticeContent;
import com.cashmallow.api.domain.model.customercenter.NoticeMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CustomerCenterServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(CustomerCenterServiceImpl.class);

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 현재 팝업 공지사항 조회
     *
     * @param deviceLangKey, today
     * @param userId
     * @return
     */
    public NoticeContent getPopupNotice(long userId, String deviceLangKey) {
        logger.info("getPopupNotice() : deviceLangKey={}", deviceLangKey);

        if (StringUtils.isEmpty(deviceLangKey)) {
            throw new InvalidParameterException();
        }

        return noticeMapper.getPopupNotice(userId, deviceLangKey);

    }

    /**
     * 공지사항 목록 검색 - admin
     *
     * @param languageType, today
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO searchNoticeContents(String languageType, Boolean isPosting, String fromDate, String toDate, String postingStatus,
                                               String searchValue, int page, int size, String sort) throws Exception {

        logger.info("getNoticeContents() : languageType={}, isPosting={}, fromDate={}, toDate={}", languageType, isPosting, fromDate, toDate);

        SearchResultVO searchResult = new SearchResultVO(page, size, sort);

        HashMap<String, Object> parameters = new HashMap<>();

        parameters.put("languageType", languageType);
        parameters.put("isPosting", isPosting);
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);

        parameters.put("postingStatus", postingStatus);
        parameters.put("searchValue", searchValue);

        parameters.put("startRow", searchResult.getPage() * searchResult.getSize());
        parameters.put("size", searchResult.getSize());
        parameters.put("sort", searchResult.getSort());

        int totalCount = noticeMapper.countNoticeContentsForAdmin(parameters);

        if (totalCount <= 0) {
            parameters.put("languageType", "en-US");
            totalCount = noticeMapper.countNoticeContentsForAdmin(parameters);
        }

        List<NoticeContent> noticeContentsList = noticeMapper.getNoticeContentsForAdmin(parameters); // Object : NoticeContents

        List<Object> list = new ArrayList<>(noticeContentsList);

        searchResult.setResult(list, totalCount, page);

        return searchResult;

    }

    /**
     * 공지사항 목록 조회 - traveler
     *
     * @param deviceLangKey, today
     * @param userId
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO getNoticeContents(String deviceLangKey, long userId, Boolean isPosting, String fromDate, String toDate, String postingStatus,
                                            String searchValue, int page, int size, String sort) {
        logger.info("getNoticeContents() : deviceLangKey={}, isPosting={}, fromDate={}, toDate={}", deviceLangKey, isPosting, fromDate, toDate);

        SearchResultVO searchResult = new SearchResultVO(page, size, sort);

        HashMap<String, Object> parameters = new HashMap<>();

        parameters.put("deviceLangKey", deviceLangKey);
        parameters.put("userId", userId);
        parameters.put("isPosting", isPosting);
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);

        parameters.put("postingStatus", postingStatus);
        parameters.put("searchValue", searchValue);

        parameters.put("startRow", searchResult.getPage() * searchResult.getSize());
        parameters.put("size", searchResult.getSize());
        parameters.put("sort", searchResult.getSort());

        int totalCount = noticeMapper.countNoticeContents(parameters);

        List<NoticeContent> noticeContentsList = noticeMapper.getNoticeContents(parameters); // Object : NoticeContents

        List<Object> list = new ArrayList<>(noticeContentsList);

        searchResult.setResult(list, totalCount, page);

        return searchResult;

    }

    /**
     * 개별 공지사항 조회. languageType이 정확한 경우만 리턴. - ADMIN
     *
     * @param id
     * @param languageType
     * @return
     */
    public NoticeContent getNoticeContentByExactLanguageType(Long noticeId, String languageType) {
        logger.info("getNoticeContentByExactLanguageType() : noticeId={}, languageType={}", noticeId, languageType);

        return noticeMapper.getNoticeContentAdmin(noticeId, languageType);

    }

    /**
     * 개별 공지사항 조회. languageType이 정확한 경우만 리턴. - Traveler
     *
     * @param id
     * @param languageType
     * @return
     */
    public NoticeContent getNoticeContentByExactLanguageTypeTraveler(Long noticeId, Long userId, String deviceLangKey) {
        logger.info("getNoticeContentByExactLanguageTypeTraveler() : noticeId={}, languageType={}", noticeId, deviceLangKey);
        HashMap<String, Object> parameters = new HashMap<>();

        parameters.put("noticeId", noticeId);
        parameters.put("deviceLangKey", deviceLangKey);
        parameters.put("userId", userId);
        return noticeMapper.getNoticeContentTraveler(parameters);

    }

    /**
     * 공지사항 추가/수정
     *
     * @param noticeContents
     * @param modifier       (수정자 ID)
     * @return
     * @throws Exception
     */
    @Transactional
    public NoticeContent addNoticeContent(NoticeContent noticeContent) throws Exception {
        logger.info("addNoticeContent() : noticeContent.title={}", noticeContent.getTitle());

        // 팝업 이면 해당 기간에 다른 팝업 공지가 없는지 체크 
        if (noticeContent.getIsPopup()) {
            List<NoticeContent> ncList = new ArrayList<NoticeContent>();

            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("isPopup", noticeContent.getIsPopup());
            parameters.put("checkDate", noticeContent.getBeginDate());

            parameters.put("sort", 1);
            parameters.put("startRow", 0);
            parameters.put("size", 10);

            ncList = noticeMapper.getNoticeContents(parameters);

            for (NoticeContent nc : ncList) {
                if (nc.getId() != noticeContent.getId()) {
                    throw new Exception("Please adjust the posting period. There is another popup notice.");
                }
            }

            parameters.put("isPopup", noticeContent.getIsPopup());
            parameters.put("checkDate", noticeContent.getEndDate());

            ncList = noticeMapper.getNoticeContents(parameters);

            for (NoticeContent nc : ncList) {
                if (nc.getId() != noticeContent.getId()) {
                    throw new Exception("Please adjust the posting period. There is another popup notice.");
                }
            }
        }

        Notice notice = new Notice();
        if (noticeContent.getId() != null) {
            Notice foundNotice = noticeMapper.getNotice(noticeContent.getId());
            if (foundNotice != null) {
                notice = foundNotice;
            }
        }
        notice.setBeginDate(noticeContent.getBeginDate());
        notice.setEndDate(noticeContent.getEndDate());
        notice.setIsPopup(noticeContent.getIsPopup());
        notice.setModifier(noticeContent.getModifier());

        int affectRows = noticeMapper.insertNotice(notice);

        if (affectRows == 0) {
            throw new Exception("INTERNAL_SERVER_ERROR");
        }

        // id가 없는 경우는 신규 공지임.
        if (noticeContent.getId() == null) {
            noticeContent.setId(noticeMapper.getLastInsertId());
        }

        affectRows = noticeMapper.insertNoticeContent(noticeContent);

        if (affectRows == 0) {
            throw new Exception("INTERNAL_SERVER_ERROR");
        }

        return noticeContent;

    }

    /**
     * 공지사항 삭제
     *
     * @param noticeId
     * @param languageType
     * @param modifier     (수정자 ID)
     * @return
     * @throws Exception
     */
    @Transactional
    public int deleteNotice(Notice notice) throws CashmallowException {
        logger.info("deletNotice() : noticeId={}", notice.getId());

        int affectRows = 0;

        if (notice.getId() > 0) {

            NoticeContent noticeContent = new NoticeContent();
            noticeContent.setId(notice.getId());
            noticeMapper.deleteNoticeContents(noticeContent);

            affectRows = noticeMapper.deleteNotice(notice);

            if (affectRows == 0) {
                throw new CashmallowException("INTERNAL_SERVER_ERROR");
            }

        } else {
            logger.error("deleteNotice(): noticeId={} message=\"Invalid parameter\"");
            throw new CashmallowException("INTERNAL_SERVER_ERROR");
        }

        return affectRows;

    }
}
