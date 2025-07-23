package com.cashmallow.api.domain.model.edd;

import java.util.List;
import java.util.Map;

public interface UserEddMapper {

    List<UserEdd> getUserEddList(Map<String, Object> userEddParams);

    List<UserEdd> getUserEddJoinList(Map<String, Object> userEddParams);

    List<UserEddFromAmtHistory> getFromAmtHistory(Map<String, Object> userEddParams);

    Map<String, Object> getUserEddLimit(Map<String, Object> userEddParams);

    int getUserEddCount(Map<String, Object> userEddParams);

    int registerUserEdd(UserEdd UserEdd);

    int updateUserEdd(UserEdd UserEdd);

}
