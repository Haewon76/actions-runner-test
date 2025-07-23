package com.cashmallow.api.interfaces.scb.model.dto.inbound;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/*
In case of success to verify the status
{
    "result" : "OK"
}

400 Bad Request - In case of mandatory field
{
   "status": "Bad Request",
   "code": "400",
   "description": "{parameter name} : Missing required parameter"
}

404 Not Found - In case of invalid field value/ In case of Incorrect unique identifier received
{
   "status": "Not Found",
   "code": "404",
   "description": "{parameter name} : Invalid value received"
}
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SCBInboundResponse {
    private String result;
    private String status;
    private String code;
    private String description;

    public SCBInboundResponse ok() {
        this.result = "OK";
        return this;
    }

    public SCBInboundResponse badRequest(String param) {
        this.status = "Bad Request";
        this.code = "400";
        this.description = param + " : Missing required parameter";
        return this;
    }

    public SCBInboundResponse notFound() {
        this.status = "Not Found";
        this.code = "404";
        this.description = "args : Invalid value received";
        return this;
    }

    public SCBInboundResponse forbidden() {
        this.status = "FORBIDDEN";
        this.code = "403";
        this.description = "Invalid API Key";
        return this;
    }
}
