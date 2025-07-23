package com.cashmallow.api.interfaces.openbank.service;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.openbank.Openbank;
import com.cashmallow.api.domain.model.openbank.OpenbankMapper;
import com.cashmallow.api.domain.model.openbank.OpenbankToken;
import com.cashmallow.api.domain.model.openbank.OpenbankTokenDate;
import com.cashmallow.api.interfaces.openbank.dto.client.UserMeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * openbank의 민감정보 암호화,복호화
 */
@RequiredArgsConstructor
@Repository
public class OpenbankRepositoryImpl {

    private final SecurityService securityService;
    private final OpenbankMapper openbankMapper;

    public int insertOpenbankToken(OpenbankToken openbankToken) {
        OpenbankToken openbankTokenVO = new OpenbankToken(
                openbankToken.getTravelerId(),
                securityService.encryptAES256(openbankToken.getAccessToken()),
                securityService.encryptAES256(openbankToken.getRefreshToken()),
                openbankToken.getUserSeqNo(),
                openbankToken.getTokenIssueDate(),
                openbankToken.getSignDate());
        return openbankMapper.insertOpenbankUserToken(openbankTokenVO);
    }

    public int updateOpenbankToken(OpenbankToken openbankToken) {
        OpenbankToken openbankTokenVO = new OpenbankToken(
                openbankToken.getTravelerId(),
                securityService.encryptAES256(openbankToken.getAccessToken()),
                securityService.encryptAES256(openbankToken.getRefreshToken()),
                openbankToken.getUserSeqNo(),
                openbankToken.getTokenIssueDate(),
                openbankToken.getSignDate());
        return openbankMapper.updateOpenbankToken(openbankTokenVO);
    }

    public Openbank getOpenbank(Long travelerId) {
        Openbank openbank = openbankMapper.getOpenbankByTravelerId(travelerId);
        if (openbank == null) {
            return null;
        }

        openbank.setUserCi(securityService.decryptAES256(openbank.getUserCi()));
        openbank.setAccessToken(securityService.decryptAES256(openbank.getAccessToken()));
        openbank.setRefreshToken(securityService.decryptAES256(openbank.getRefreshToken()));
        openbank.setAccountHolderName(securityService.decryptAES256(openbank.getAccountHolderName()));
        openbank.setAccountNumMasked(securityService.decryptAES256(openbank.getAccountNumMasked()));
        return openbank;
    }

    public int deleteOpenbankAccount(Long travelerId) {
        return openbankMapper.deleteOpenbankAccount(travelerId);
    }

    public int deleteOpenbankUser(long travelerId) {
        return openbankMapper.deleteOpenbankUserToken(travelerId);
    }

    public int updateOpenbankAccount(String userCi, String travelerId, UserMeDetail userMeDetail) {
        userMeDetail.setUser_ci(securityService.encryptAES256(userCi));
        userMeDetail.setAccount_holder_name(securityService.encryptAES256(userMeDetail.getAccount_holder_name()));
        userMeDetail.setAccount_num_masked(securityService.encryptAES256(userMeDetail.getAccount_num_masked()));
        userMeDetail.setTraveler_id(travelerId);
        return openbankMapper.updateOpenbankAccount(userMeDetail);
    }

    public List<Openbank> getExpiredTokenUser() {
        OpenbankTokenDate openbankTokenDate = new OpenbankTokenDate(
                ZonedDateTime.now().minus(100, ChronoUnit.DAYS),
                ZonedDateTime.now().minus(90, ChronoUnit.DAYS));

        List<Openbank> openbankByTokenIssueDate = openbankMapper.getOpenbankByTokenIssueDate(openbankTokenDate);

        for (var openbank : openbankByTokenIssueDate) {
            openbank.setUserCi(securityService.decryptAES256(openbank.getUserCi()));
            openbank.setAccessToken(securityService.decryptAES256(openbank.getAccessToken()));
            openbank.setRefreshToken(securityService.decryptAES256(openbank.getRefreshToken()));
            openbank.setAccountHolderName(securityService.decryptAES256(openbank.getAccountHolderName()));
            openbank.setAccountNumMasked(securityService.decryptAES256(openbank.getAccountNumMasked()));
        }

        return openbankByTokenIssueDate;
    }

}
