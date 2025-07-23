package com.cashmallow.api.interfaces.mallowlink.enduser;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.api.interfaces.mallowlink.enduser.dto.EndUserRegisterRequest;
import com.cashmallow.api.interfaces.mallowlink.enduser.dto.EndUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MallowlinkEnduserServiceImplTest {

    @Mock
    MallowlinkEnduserClient enduserClient;

    @InjectMocks
    MallowlinkEnduserServiceImpl enduserService;

    Traveler traveler;
    User user;
    long id = 2653L;

    @BeforeEach
    void init() {
        traveler = new Traveler();
        traveler.setId(id);
        traveler.setUserId(id);
        traveler.setEnFirstName("Naem");
        traveler.setEnLastName("Sur");

        user = new User();
        user.setId(id);
        user.setBirthDate("19990101");
        user.setCountry("003");
        user.setPhoneNumber("+82010123441234");
        user.setPhoneCountry("KOR");
        user.setEmail("naem@ruu.kr");

    }

    @Test
    void register_성공() throws CashmallowException {
        // given
        EndUserRegisterRequest request = EndUserRegisterRequest.of(user, traveler);

        EndUserResponse response = new EndUserResponse(
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.countryCode(),
                request.phoneNumber(),
                request.callingCode(),
                request.email(),
                request.requestTime()
        );
        MallowlinkBaseResponse<Void> mallowlinkBaseResponse = new MallowlinkBaseResponse<>("200", null, null, ZonedDateTime.now(), null);

        BDDMockito.given(enduserClient.register(any())).willReturn(mallowlinkBaseResponse);

        // when
        enduserService.register(user, traveler);

        // then
        // assertThat(register).isEqualTo(response);

    }

    @Test
    void register_실패() {
        // given
        BDDMockito.given(enduserClient.register(any())).willThrow(new MallowlinkException(MallowlinkExceptionType.USER_NOT_FOUND));

        // when
        Throwable thrown = catchThrowable(() -> enduserService.register(user, traveler));

        // then
        assertThat(thrown).isInstanceOf(MallowlinkException.class);
        // assertThat(thrown).hasMessageContaining(MallowlinkExceptionType.USER_NOT_FOUND.getMessage());
    }
}