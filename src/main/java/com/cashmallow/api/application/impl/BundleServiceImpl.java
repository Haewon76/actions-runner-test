package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.BundleService;
import com.cashmallow.api.application.FileService;
import com.cashmallow.api.domain.model.bundle.Bundle;
import com.cashmallow.api.domain.model.bundle.BundleMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class BundleServiceImpl implements BundleService {

    @Autowired
    private FileService fileService;

    @Autowired
    private BundleMapper bundleMapper;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Bundle> getBundleList() {
        return bundleMapper.getBundleList();
    }

    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public int registerBundle(MultipartFile bundleFile, HttpServletRequest request, Long userId) throws NoSuchAlgorithmException, CashmallowException {
        Bundle bundle = new Bundle();
        bundle.setVersion(request.getParameter("version"));
        bundle.setPlatform(request.getParameter("platform"));
        bundle.setSize(bundleFile.getSize());
        bundle.setIsActive(request.getParameter("isActive"));
        bundle.setCreatedId(userId);
        bundle.setDescription(request.getParameter("description")); // 20250122 - 설명란 추가

        // Calculate SHA1 hash of the file
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream is = bundleFile.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        String hashSha1 = sb.toString();

        bundle.setHashSha1(hashSha1);

        // file upload
        String fileUrl = fileService.upload(bundleFile, Const.FILE_SERVER_BUNDLE);
        bundle.setFileName(fileUrl);

        return bundleMapper.registerBundle(bundle);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Bundle getBundleInfo(String platform, String version) {
        return bundleMapper.getLatestActiveBundle(platform, version);
    }

    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public void setIsActive(long id, String isActive, Long userId, String description) {
        bundleMapper.setIsActive(id, isActive, userId, description);
    }

    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public void deleteBundle(long id) throws CashmallowException  {
        // 삭제할 번들의 filename을 가져와서 cdn에서 삭제
        try {
            Bundle bundle = bundleMapper.getBundleInfo(id);
            fileService.deleteFile(Const.FILE_SERVER_BUNDLE, bundle.getFileName());
            bundleMapper.deleteBundle(id);
        } catch (Exception e) {
            throw new CashmallowException("번들 삭제에 실패하였습니다.");
        }
    }
}
