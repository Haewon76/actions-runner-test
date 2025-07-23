package com.cashmallow.api.domain.model.bundle;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BundleMapper {
    List<Bundle> getBundleList();

    int registerBundle(Bundle bundle);

    Bundle getLatestActiveBundle(String platform, String version);

    void setIsActive(long id, String isActive, long userId, String description);

    Bundle getBundleInfo(long id);

    void deleteBundle(long id);
}
