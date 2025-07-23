package com.cashmallow.api.interfaces.edd;

import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.edd.UserEdd;
import com.cashmallow.api.domain.model.edd.UserEddFromAmtHistory;
import com.cashmallow.api.domain.model.edd.UserEddImage;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.traveler.dto.TravelerEddValidationVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface UserEddService {

    TravelerEddValidationVO verificationUserEdd(Country fromCountry, BigDecimal fromMoney, Long userId, Long travelerId, ExchangeConfig exchangeConfig, Locale locale) throws CashmallowException;

    List<UserEdd> getUserEddList(Map<String, Object> eddParams);

    List<UserEdd> getUserEddJoinList(Map<String, Object> eddParams);

    List<UserEddFromAmtHistory> getFromAmtHistory(Long userEddId);

    void updateEdd(Long managerId, Long userEddId, String limited, String ip) throws CashmallowException;

    int registerUserEddImage(Long managerId, Long userEddId, List<MultipartFile> pictureLists) throws CashmallowException, IOException;

    List<Long> getUserEddImageList(Long userEddId);

    UserEddImage getUserEddImage(Long userEddImageId) throws Exception;

    int registerUserEdd(UserEdd userEdd, Long managerId, String ip) throws CashmallowException;
}