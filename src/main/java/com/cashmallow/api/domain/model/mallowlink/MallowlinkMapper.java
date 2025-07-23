package com.cashmallow.api.domain.model.mallowlink;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MallowlinkMapper {

    void insertMallowlinkLog(MallowlinkLog log);
}
