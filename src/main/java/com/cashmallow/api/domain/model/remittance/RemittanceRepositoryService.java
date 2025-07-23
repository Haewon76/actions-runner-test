package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.REJECT_EXCHANGE_REMITTANCE_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class RemittanceRepositoryService {

    private final RemittanceMapper remittanceMapper;

    private final UserRepositoryService userRepositoryService;

    private final MessageSource messageSource;
    private final TravelerMapper travelerMapper;

    public Remittance getRemittanceByRemittanceId(Long remittanceId) {
        return remittanceMapper.getRemittanceByRemittanceId(remittanceId);
    }


    public RemittanceTravelerSnapshot getRemittanceTravelerSnapshotByRemittanceId(Long remittanceId) {
        return remittanceMapper.getRemittanceTravelerSnapshotByRemittanceId(remittanceId);
    }


    public List<RemitReceiverAml> getRemitReceiverAml(Map<String, String> params) {
        return remittanceMapper.getRemitReceiverAml(params);
    }


    public RemitReceiverAml getRemitReceiverAmlById(Long id) {
        return remittanceMapper.getRemitReceiverAmlById(id);
    }

    public RemittancePaygate getRemittancePaygateByRemittanceId(Long remitId) {
        return remittanceMapper.getRemittancePaygateByRemitId(remitId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public RemittanceTraverlerInfo getRemittanceBeforeMapping(long remitId) {
        return remittanceMapper.getRemittanceBeforeMapping(remitId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<RemittanceTraverlerInfo> getRemittanceBeforeMappingList(String accountNo) {
        return remittanceMapper.getRemittanceBeforeMappingList(accountNo);
    }

    public Remittance getRemittanceInprogress(Long travelerId) throws CashmallowException {
        final String method = "getRemittanceInprogress() ";

        log.info("{} : travelerId={}", method, travelerId);

        Remittance remittance = null;

        try {
            remittance = remittanceMapper.getRemittanceInProgress(travelerId);
            if (remittance == null) {
                return null;
            }

            List<RemittanceDepositReceipt> depositReceipts = remittanceMapper.getRemittanceDepositReceiptList(remittance.getId());
            List<String> receiptPhotos = remittance.getReceiptPhotos();
            depositReceipts.forEach(dr -> receiptPhotos.add(dr.getReceiptPhoto()));

            // 앱을 수정하지 않고 작업하기 위한 trick
            if (Remittance.RemittanceStatusCode.RC.equals(remittance.getRemitStatus())) {
                remittance.setRemitStatus(Remittance.RemittanceStatusCode.DP);
            }

            // DR 상태인경우 reject_message를 추가한다
            if (Remittance.RemittanceStatusCode.DR.equals(remittance.getRemitStatus())) {
                final String remittanceRejectMessage = remittanceMapper.getRemittanceRejectMessage(remittance.getId(), remittance.getRemitStatus().name());
                remittance.setRejectMessage(remittanceRejectMessage);

                if (StringUtils.isEmpty(remittanceRejectMessage)) {
                    User user = userRepositoryService.getUserByTravelerId(travelerId);
                    Locale locale = user.getCountryLocale();
                    String message = messageSource.getMessage(REJECT_EXCHANGE_REMITTANCE_ERROR, null, "Please contact the cs-center.", locale);
                    remittance.setRejectMessage(message);
                }
            }

            log.info("RemitId={}, Receipts count={}, list={}, searchCount={}", remittance.getId(), receiptPhotos.size(), receiptPhotos, depositReceipts.size());
        } catch (MyBatisSystemException e) {
            if (e.getCause() instanceof TooManyResultsException) {
                log.error("{}: 진행 중인 송금 건이 1건 이상입니다. travelerId={}, remittanceCount={}", method, travelerId, remittance);
            } else {
                log.error("{}: travelerId={}. error={}", method, travelerId, e.getMessage());
            }
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return remittance;
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public int countRemittanceListByTravelerId(long travelerId) {
        log.info("countRemittanceListByTravelerId(): travelerId={}", travelerId);
        return remittanceMapper.countRemittanceListByTravelerId(travelerId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Remittance> getRemittanceListByTravelerId(long travelerId, int page, int size) {
        log.info("getRemittanceListByTravelerId(): travelerId={}, page={}, size={}", travelerId, page, size);

        Map<String, Object> params = new HashMap<>();
        params.put("travelerId", travelerId);
        params.put("startRow", page * size);
        params.put("size", size);

        return remittanceMapper.getRemittanceListByTravelerId(params);
    }


    @Transactional
    public int insertRemitTravelerSnapshot(Traveler traveler, User user, Long remitId, String addressStateProvince, String addressStateProvinceEn) {
        RemittanceTravelerSnapshot remitTrSnapshot = new RemittanceTravelerSnapshot();

        remitTrSnapshot.setRemitId(remitId);
        remitTrSnapshot.setTravelerId(traveler.getId());
        remitTrSnapshot.setIdentificationNumber(traveler.getIdentificationNumber());
        remitTrSnapshot.setBankName(traveler.getBankName());
        remitTrSnapshot.setAccountName(traveler.getAccountName());
        remitTrSnapshot.setAccountNo(traveler.getAccountNo());
        remitTrSnapshot.setAddress(traveler.getAddress());
        remitTrSnapshot.setAddressCity(traveler.getAddressCity());
        remitTrSnapshot.setAddressCountry(traveler.getAddressCountry());
        remitTrSnapshot.setAddressStateProvince(addressStateProvince);
        remitTrSnapshot.setAddressStateProvinceEn(addressStateProvinceEn);
        remitTrSnapshot.setAddressSecondary(traveler.getAddressSecondary());
        remitTrSnapshot.setPhoneNumber(user.getPhoneNumber());
        remitTrSnapshot.setPhoneCountry(user.getPhoneCountry());

        return remittanceMapper.insertRemitTravelerSnapshot(remitTrSnapshot);
    }

    // 713 브랜치 나가면 나중에 v2 쪽처럼 추가
    @Transactional
    public int insertRemitTravelerSnapshotVCoupon(Traveler traveler, User user, Long remitId) {
        RemittanceTravelerSnapshot remitTrSnapshot = new RemittanceTravelerSnapshot();

        remitTrSnapshot.setRemitId(remitId);
        remitTrSnapshot.setTravelerId(traveler.getId());
        remitTrSnapshot.setIdentificationNumber(traveler.getIdentificationNumber());
        remitTrSnapshot.setBankName(traveler.getBankName());
        remitTrSnapshot.setAccountName(traveler.getAccountName());
        remitTrSnapshot.setAccountNo(traveler.getAccountNo());
        remitTrSnapshot.setAddress(traveler.getAddress());
        remitTrSnapshot.setAddressCity(traveler.getAddressCity());
        remitTrSnapshot.setAddressCountry(traveler.getAddressCountry());
        remitTrSnapshot.setAddressSecondary(traveler.getAddressSecondary());
        remitTrSnapshot.setPhoneNumber(user.getPhoneNumber());
        remitTrSnapshot.setPhoneCountry(user.getPhoneCountry());

        return remittanceMapper.insertRemitTravelerSnapshot(remitTrSnapshot);
    }

    @Transactional
    public int insertRemitStatus(Long remitId, Remittance.RemittanceStatusCode remitStatusCode, String message) throws CashmallowException {

        int affectedRow = 0;

        RemittanceStatus remitStatus = new RemittanceStatus(remitId, remitStatusCode.name(), message);

        try {
            affectedRow = remittanceMapper.insertRemittanceStatus(remitStatus);
        } catch (MyBatisSystemException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

        if (affectedRow != 1) {
            log.error("insertRemitStatus(): Failed to insert remit_status table. remitId={}, remitStatusCode={}",
                    remitId, remitStatusCode);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return affectedRow;
    }

    @Transactional
    public int insertRemitStatus(Long remitId, Remittance.RemittanceStatusCode remitStatusCode) throws CashmallowException {
        return insertRemitStatus(remitId, remitStatusCode, null);
    }

    /**
     * @param remitId
     * @param paygateStatus
     * @param kycRefId
     * @param smaRefId
     * @param refId
     * @param remarks
     * @return
     */
    @Transactional
    public int insertRemittancePaygateStatus(Long remitId, String paygateStatus, String kycRefId, String smaRefId, String remarks) {
        Map<String, Object> statusParams = new HashMap<>();

        statusParams.put("remitId", remitId);
        statusParams.put("status", paygateStatus);
        statusParams.put("kycRefId", kycRefId);
        statusParams.put("smaRefId", smaRefId);
        statusParams.put("remarks", remarks);

        return remittanceMapper.insertRemittancePaygateStatus(statusParams);
    }

    @Transactional
    public int insertRemittancePaygate(RemittancePaygate remittancePaygate) {
        return remittanceMapper.insertRemittancePaygate(remittancePaygate);
    }

    @Transactional
    public int updateRemittancePaygate(RemittancePaygate remittancePaygate) {
        return remittanceMapper.updateRemittancePaygate(remittancePaygate);
    }

    @Transactional
    public int updateRemittanceRR(Long id) {
        return remittanceMapper.updateRemittanceRR(id);
    }

    @Transactional
    public int insertRemitReceiverAml(RemitReceiverAml remitReceiverAml) {
        return remittanceMapper.insertRemitReceiverAml(remitReceiverAml);
    }

    @Transactional
    public int updateRemitReceiverAml(RemitReceiverAml remitReceiverAml) {
        return remittanceMapper.updateRemitReceiverAml(remitReceiverAml);
    }

    @Transactional
    public int updateRemittance(Remittance remittance) throws CashmallowException {
        int rows = remittanceMapper.updateRemittance(remittance);
        if (rows != 1) {
            log.error("updateRemittance(): remittance table update failure. remitId={}, travelerId={}",
                    remittance.getId(), remittance.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return rows;
    }

    @Transactional
    public int updateRemitTravelerSnapshot(RemittanceTravelerSnapshot remitTrSnapshot) throws CashmallowException {
        int rows = remittanceMapper.updateRemitTravelerSnapshot(remitTrSnapshot);
        if (rows != 1) {
            log.error("updateRemitTravelerSnapshot(): remittance_traveler_snapshot table update failure. remitId={}, travelerId={}",
                    remitTrSnapshot.getRemitId(), remitTrSnapshot.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return rows;
    }

    public List<RemittancesDoing> getAllRemittanceDoing(CountryCode countryCode) {
        return remittanceMapper.getAllRemittanceDoing(countryCode.getName());
    }

    public List<Remittance> getAllReserveRemittances() {
        return remittanceMapper.findReservedRemittances();
    }

    public Remittance getReservedRemittance(Long remitId) {
        return remittanceMapper.getReservedRemittances(remitId);
    }

    @Transactional
    public void updateRemittanceBankAccountId(Remittance remittance) {
        remittanceMapper.updateRemittanceBankAccountId(remittance);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getRemittanceFromAmtSumByPeriod(Map<String, Object> params) {
        return remittanceMapper.getRemittanceFromAmtSumByPeriod(params);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Remittance> getUnpaidListForGlobal(String fromCountryCode) {
        return remittanceMapper.getUnpaidListForGlobal(fromCountryCode);
    }

    public int countRemittanceListByTravelerIds(List<Long> travelerIds) {
        return remittanceMapper.countRemittanceListByTravelerIds(travelerIds);
    }
}
