package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.bundle.Bundle;
import com.cashmallow.api.domain.shared.CashmallowException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface BundleService {
    List<Bundle> getBundleList();

    int registerBundle(MultipartFile bundleFile, HttpServletRequest request, Long userId) throws NoSuchAlgorithmException, CashmallowException;

    Bundle getBundleInfo(String platform, String version);

    void setIsActive(long id, String isActive, Long userId, String description);

    void deleteBundle(long id) throws CashmallowException;
}
