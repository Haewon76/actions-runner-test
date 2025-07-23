package com.cashmallow.api.interfaces.scb.model.dto.request.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfile {
    private long customerProfileId;
    private String partnerUserId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String idNumber;
    private String idType;
    private String occupation;
    private Address presentAddress;
    private Address registrationAddress;
}
