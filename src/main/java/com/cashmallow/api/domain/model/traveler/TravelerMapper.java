package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.api.domain.shared.DurationDateVO;
import com.cashmallow.api.interfaces.admin.dto.TravelerAskVO;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCreateCustomerResponse;
import com.cashmallow.api.interfaces.authme.dto.TravelerImage;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TravelerMapper {

    // Read

    // Traveler
    int countTravelerCertificationInfo(TravelerAskVO travelerAskVO);

    List<Map<String, Object>> getTravelerCertificationInfo(TravelerAskVO travelerAskVO);

    int countTravelerAccountInfo(TravelerAskVO travelerAskVO);

    List<Map<String, Object>> getTravelerAccountInfo(TravelerAskVO travelerAskVO);

    Traveler getTravelerByUserId(Long userId);

    Traveler getTravelerByTravelerId(Long travelerId);

    Map<String, Object> getLastTravelerVerificationStatus(Long travelerId);

    Map<String, Object> getConfirmCntByCountry(String country);

    // TravelerWallet
    List<TravelerWallet> getTravelerWalletByTravelerId(Long travelerId);

    // get wallet list by Exchange id
    List<TravelerWallet> getTravelerWalletByExchangeIds(String exchangeIds);

    List<TravelerWallet> getRelatedWalletsByWalletId(Long walletId);

    TravelerWallet getTravelerWallet(Long travelerWalletId);

    // Write

    // Traveler
    int registerTraveler(Map<String, Object> params);

    int updateTraveler(Traveler traveler);

    int updateTravelerAddressCheckAndReturnR(Long userId);

    int insertTraveler(Traveler traveler);


    // TravelerWallet

    /**
     * insert traveler_wallet
     *
     * @param travelerWallet
     * @return
     */
    int insertTravelerWallet(TravelerWallet travelerWallet);

    /**
     * update traveler_wallet
     *
     * @param travelerWallet
     * @return
     */
    int updateTravelerWallet(TravelerWallet travelerWallet);

    /**
     * delete traveler_wallet
     *
     * @param travelerWallet
     * @return
     */
    int deleteTravelerWallet(Long id);

    // 본인인증 or 통장인증 상태값 변경시 결과값 기록
    int insertTravelerVerificationStatus(TravelerVerificationStatusRequest request);
    List<TravelerVerificationStatusResponse> getTravelerVerificationStatuses(Long travelerId);

    /**
     * Get sum e_money by country
     *
     * @param country
     * @return
     */
    BigDecimal getSumEMoney(String country);

    /**
     * Get sum r_money by country
     *
     * @param country
     * @return
     */
    BigDecimal getSumRMoney(String country);

    int getCountNewTravelers(DurationDateVO date);

    Map<String, String> getNameByTravelerId(Long travelerId);

    List<Traveler> getTravelerLimit(Map<String, String> limit);

    void addTravelerVerificationHistory(TravelerVerificationHistory travelerVerificationHistory);
    void addTravelerImage(TravelerImage travelerImage);

    List<Traveler> getVerifiedTravelerById();

    List<Traveler> getVerifiedTravelerByPassport();

    TravelerVerificationHistory getTravelerFirstVerification(Long travelerId);

    List<GlobalTravelerWalletBalance> getTravelerBalance(Long travelerId);

    List<TravelerImage> getTravelerImages(Long travelerId);

    TravelerImage getTravelerImage(Long travelerId);

    void insertComplyadvantageCustomer(ComplyAdvantageCreateCustomerResponse customerResponse);

    String getComplyadvantageCustomerId(Long travelerId);

    int insertGlobalTravelerCertificationStep(GlobalTravelerCertificationStep traveler);

    int updateGlobalTravelerCertificationStep(GlobalTravelerCertificationStep traveler);

    List<GlobalTravelerCertificationStep> getActiveGlobalTravelerCertificationSteps(Long userId);

    List<GlobalTravelerCertificationStep> getGlobalTravelerCertificationSteps(Map<String, Object> param);

    GlobalTravelerCertificationStep getGlobalTravelerCertificationStepById(Long id);

    int timeoutGlobalTravelerCertificationStep(Long userId);

    List<Traveler> getTravelersByUserIds(@Param("userIds") List<Long> userIds);
}
