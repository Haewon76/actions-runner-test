package com.cashmallow.api.domain.model.openbank;


import com.cashmallow.api.interfaces.openbank.dto.client.UserMeDetail;

import java.util.List;

public interface OpenbankMapper {
    Openbank getOpenbankByTravelerId(Long travelerId);

    int insertOpenbankUserToken(OpenbankToken openbankToken);

    /**
     * 토큰 갱신 API
     *
     * @param params (traveler_id, userSeqNo, accessToken, refreshToken, refreshTime)
     * @return
     */
    int updateOpenbankToken(OpenbankToken openbankToken);

    /**
     * @param openbankUserInfoVO
     * @return
     */
    int updateOpenbankAccount(UserMeDetail userMeDetail);

    /**
     * 오픈뱅킹 사용자 탈퇴 처리.
     *
     * @param travelerId
     * @return
     */
    int deleteOpenbankUserToken(Long travelerId);

    int deleteOpenbankAccount(Long travelerId);

    List<Openbank> getOpenbankByTokenIssueDate(OpenbankTokenDate date);

}
