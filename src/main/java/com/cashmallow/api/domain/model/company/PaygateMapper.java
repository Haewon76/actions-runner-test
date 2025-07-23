package com.cashmallow.api.domain.model.company;

import com.cashmallow.api.interfaces.admin.dto.AdminDbsRemittanceAskVO;
import com.cashmallow.api.interfaces.admin.dto.PaygateRecordDeleteRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaygateMapper {


    /**
     * Find Cashmallow's Transaction List
     *
     * @param params
     * @return
     */
    List<PaygateRecord> getPaygateRecordList(Map<String, Object> params);

    List<PaygateRecord> getPaygateRecordListByIdList(Map<String, Object> params);

    PaygateRecord getPaygateRecordById(String id);

    int insertDbsRemittance(DbsRemittance dbsRemittance);

    int updateDbsRemittance(DbsRemittance dbsRemittance);

    DbsRemittance getDbsRemittance(String dbsRemittanceId);

    List<PaygateRecord> getNotMappingPaygateRecordId(Long bankAccountId);

    List<Object> getAdminDbsRemittanceList(AdminDbsRemittanceAskVO pvo);

    int countAdminDbsRemittanceList(AdminDbsRemittanceAskVO pvo);

    int insertPaygateRecord(PaygateRecord paygateRecord);

    int insertPaygateRecordBulk(List<PaygateRecord> paygateRecords);

    int updatePaygateRecord(PaygateRecord paygateRecord);

    int updatePaygateRecordId(String newId, String oldId);

    int deletePaygateRecord(PaygateRecordDeleteRequest record);

    List<PaygateRecord> findTempPaygateRecordsByMapped();

    List<DbsDto> findTempDbsRecordsByMapped(@Param("dbsAccountId") Long dbsAccountId);

    @Select("""
            SELECT IFNULL((
                           SELECT pr.balance
                           FROM paygate_record pr
                           WHERE pr.bank_account_id = #{bankAccountId}
                             AND pr.iso4217 = #{currency}
                           ORDER BY pr.created_date DESC, pr.balance DESC
                           LIMIT 1
                       ), 0) AS balance""")
    BigDecimal getPaygateRecordLastBalance(Integer bankAccountId, String currency);

    List<PaygateRecord> getPaygateRecordForRepayment(Long dbsAccountId);
}
