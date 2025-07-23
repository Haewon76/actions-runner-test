package com.cashmallow.api.domain.model.edd;

import java.util.List;
import java.util.Map;

public interface UserEddLogMapper {

    List<UserEddLog> getUserEddLogList(Map<String, Object> userEddLogParams);

    List<UserEddLog> getUserEddLogJoinList(Map<String, Object> userEddLogParams);

    int registerUserEddLog(UserEddLog UserEddLog);


}
