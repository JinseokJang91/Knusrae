package com.knusrae.common.custom.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    record UploadResponse(String key, String url) {}

    UploadResponse upload(MultipartFile file, String relativePath);

    void deleteByKey(String key);

    Resource loadByKey(String key);
}
