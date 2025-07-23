package com.cashmallow.api.interfaces.mallowlink.enduser;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.enduser.dto.EndUserRegisterRequest;
import com.cashmallow.api.interfaces.mallowlink.enduser.dto.EndUserUpdateRequest;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MallowlinkEnduserServiceImpl {

    private final MallowlinkEnduserClient enduserClient;
    private final JsonUtil jsonUtil;

    public void register(User user, Traveler traveler) throws CashmallowException {

        log.info("user:{}, traveler:{}", user, traveler);

        if (!traveler.getUserId().equals(user.getId())) {
            log.error("userId 불일치 traveler.userId:{}, user.id:{}", traveler.getUserId(), user.getId());
            throw new CashmallowException(MsgCode.INTERNAL_SERVER_ERROR);
        }

        try {
            enduserClient.register(EndUserRegisterRequest.of(user, traveler));
        } catch (MallowlinkException e) {
            log.warn("Mallowlink 가입 Error:{}", e.getStatus(), e);
            throw e;
        }
    }

    public void update(User user, Traveler traveler) throws CashmallowException {

        log.info("user:{}, traveler:{}", user, traveler);

        if (!traveler.getUserId().equals(user.getId())) {
            log.error("userId 불일치 traveler.userId:{}, user.id:{}", traveler.getUserId(), user.getId());
            throw new CashmallowException(MsgCode.INTERNAL_SERVER_ERROR);
        }

        try {
            EndUserUpdateRequest request = EndUserUpdateRequest.of(user, traveler);
            log.info("EndUser Update :{}", request);
            enduserClient.update(request);
        } catch (MallowlinkException e) {
            log.error("Mallowlink 업데이트 Error:{}", e.getStatus(), e);
            throw e;
        }
    }
}
