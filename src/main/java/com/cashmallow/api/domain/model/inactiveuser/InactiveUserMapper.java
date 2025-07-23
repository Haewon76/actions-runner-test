package com.cashmallow.api.domain.model.inactiveuser;

import java.util.List;
import java.util.Map;

public interface InactiveUserMapper {

    void insertInactiveRemittance(InactiveRemittance inactiveRemittance);

    List<InactiveRemittance> getInactiveRemittanceList(Long travelerId);

    int deleteInactiveRemittanceList(Long travelerId);


    void insertInactiveRemittanceTravelerSnapshot(InactiveRemittanceTravelerSnapshot inactiveRemittanceTraveler);

    List<InactiveRemittanceTravelerSnapshot> getInactiveRemittanceTravelerSnapshotList(Long travelerId);

    int deleteInactiveRemittanceTravelerSnapshotList(Long travelerId);

    void insertInactiveRemitReceiverAml(InactiveRemitReceiverAml inactiveRemitReceiverAml);

    List<InactiveRemitReceiverAml> getInactiveRemitReceiverAml(Long travelerId);

    int deleteInactiveRemitReceiverAml(Long travelerId);

    void insertInactiveExchange(InactiveExchange inactiveExchange);

    List<InactiveExchange> getInactiveExchangeList(Long travelerId);

    int deleteInactiveExchangeList(Long travelerId);


    void insertInactiveRefund(InactiveRefund inactiveRefund);

    List<InactiveRefund> getInactiveRefundList(Long travelerId);

    int deleteInactiveRefundList(Long travelerId);


    void insertInactiveWithdrawalPartner(InactiveWithdrawalPartner inactiveWithdrawalPartner);

    InactiveWithdrawalPartner getInactiveWithdrawalPartner(Long userId);

    int deleteInactiveWithdrawalPartner(Long userId);


    void insertInactiveWithdrawalPartnerCalc(InactiveWithdrawalPartnerCalc inactiveWithdrawalPartnerCalc);

    List<InactiveWithdrawalPartnerCalc> getInactiveWithdrawalPartnerCalcList(Long withdrawalPartnerId);

    int deleteInactiveWithdrawalPartnerCalcList(Long withdrawalPartnerId);


    void insertInactiveTraveler(InactiveTraveler inactiveTraveler);

    InactiveTraveler getInactiveTraveler(Long userId);

    int deleteInactiveTraveler(Long userId);


    void insertInactiveUser(InactiveUser inactiveUser);

    InactiveUser getInactiveUser(Long userId);

    InactiveUser getInactiveUserByLoginNInactiveType(Map<String, Object> params);

    List<InactiveUser> getInactiveUserListByLogin(String login);

    int deleteInactiveUser(Long userId);

    List<InactiveTraveler> getInactiveTravelerListByIdentificationNumber(String encryptedIdentificationNumber);

    List<InactiveTraveler> getInactiveTravelerListByUserIds(List<InactiveUser> inactiveUser);
}
