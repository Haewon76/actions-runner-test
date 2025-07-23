package com.cashmallow.api.domain.model.edd;

import java.util.List;
import java.util.Map;

public interface UserEddImageMapper {

    List<UserEddImage> getUserEddImageList(Map<String, Object> userEddImageParams);

    int registerUserEddImage(List<UserEddImage> userEddImageList);

    int updateUserEddImage(UserEddImage userEddImage);

    List<Long> getUserEddImageIdList(Long userEddId);
}
