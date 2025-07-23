package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.interfaces.admin.dto.AdminRemittanceAskVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface RemittanceMapper {

    Remittance getRemittanceByRemittanceId(Long id);

    RemittanceTravelerSnapshot getRemittanceTravelerSnapshotByRemittanceId(Long remitId);

    List<RemitReceiverAml> getRemitReceiverAml(Map<String, String> parmas);

    RemitReceiverAml getRemitReceiverAmlById(Long id);

    RemittancePaygate getRemittancePaygateByRemitId(Long remitId);

    /**
     * Get remittances in progress.
     *
     * @param travelerId
     * @return
     */
    Remittance getRemittanceInProgress(Long travelerId);

    /**
     * Count remittance list total by travelerId
     *
     * @param travelerId
     * @return
     */
    int countRemittanceListByTravelerId(long travelerId);

    /**
     * Get remittance list by travelerId
     *
     * @param params : travelerId, startRow, size, sort
     * @return
     */
    List<Remittance> getRemittanceListByTravelerId(Map<String, Object> params);
    int countRemittanceListByTravelerIds(List<Long> travelerIds);

    int insertRemittance(Remittance remittance);

    int updateRemittance(Remittance remittance);

    int insertRemittanceStatus(RemittanceStatus remittanceStatus);

    int insertRemitTravelerSnapshot(RemittanceTravelerSnapshot remittanceTrSnapshot);

    int updateRemitTravelerSnapshot(RemittanceTravelerSnapshot remittanceTrSnapshot);

    int insertRemittancePaygate(RemittancePaygate remittancePaygate);

    int updateRemittancePaygate(RemittancePaygate remittancePaygate);

    int updateRemittanceRR(Long id);

    int insertRemittancePaygateStatus(Map<String, Object> params);

    int insertRemitReceiverAml(RemitReceiverAml remitReceiverAml);

    int updateRemitReceiverAml(RemitReceiverAml remitReceiverAml);

    Map<String, Object> getRemittanceFromAmtSumByPeriod(Map<String, Object> params);

    List<Object> searchAdminRemittanceForMapping(AdminRemittanceAskVO pvo);

    List<Object> searchAdminRemittanceForReport(AdminRemittanceAskVO pvo);

    int countAdminRemittanceForReport(AdminRemittanceAskVO pvo);


    // RemittanceDepositReceipt

    /**
     * Get RemittanceDepositReceipt List
     *
     * @param remitId
     * @return
     */
    List<RemittanceDepositReceipt> getRemittanceDepositReceiptList(Long remitId);

    /**
     * Insert RemittanceDepositReceipt
     * After insert, remittanceDepositReceipt will have the Last Insert ID in id.
     *
     * @param remittanceDepositReceipt
     * @return id
     */
    int insertRemittanceDepositReceipt(RemittanceDepositReceipt remittanceDepositReceipt);

    /**
     * 진행 중(DR, RR, RC)인 송금건의 합계를 가져옴.
     *
     * @param fromCd 001(HK)
     * @return
     */
    List<RemittancesDoing> getAllRemittanceDoing(String fromCd);

    /**
     * 매핑 되지않은 송금건을 가져옴(OP, DR)
     *
     * @return
     */
    RemittanceTraverlerInfo getRemittanceBeforeMapping(long id);

    List<RemittanceTraverlerInfo> getRemittanceBeforeMappingList(String accountNo);

    List<Remittance> findReservedRemittances();

    Remittance getReservedRemittances(Long RemittanceId);

    @Select(" SELECT message " +
            " FROM remittance_status " +
            " WHERE remit_id = ${id} " +
            " AND remit_status = #{remitStatus} " +
            " AND message IS NOT NULL " +
            " ORDER BY created_date DESC" +
            " LIMIT 1 ")
    String getRemittanceRejectMessage(Long id, String remitStatus);

    int updateRemittanceBankAccountId(Remittance remittance);

    List<Remittance> getUnpaidListForGlobal(String fromCountryCode);
}
