package com.knusrae.common.custom.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component
@Profile({"default", "local", "dev"})
@RequiredArgsConstructor
@Slf4j
public class LocalImageStorage implements ImageStorage {
    @Value("${app.storage.local.base-dir}")
    private String baseDir;

    @Value("${app.storage.local.public-base-url}")
    private String publicBaseUrl;

    @Override
    public UploadResponse upload(MultipartFile file, String relativePath) {
        try {
            String ext = extractExt(Objects.requireNonNull(file.getOriginalFilename()));
            String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

            Path targetDir = Paths.get(baseDir, publicBaseUrl).normalize();
            Files.createDirectories(targetDir);

            Path target = targetDir.resolve(filename).normalize();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // key는 /recipes/123/2025-10-10/xxx.jpg 같은 상대경로로 관리
            String key = Paths.get(publicBaseUrl, filename).toString().replace('\\', '/');
            String url = publicBaseUrl + "/" + key;

            return new UploadResponse(key, url);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 로컬 저장 실패", e);
        }
    }

    @Override
    public void deleteByKey(String key) {
        try {
            Path target = Paths.get(baseDir, key).normalize();
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // 필요시 로깅
            log.error("이미지 파일 삭제 실패", e);
        }
    }

    private String extractExt(String name) {
        int i = name.lastIndexOf('.');
        return (i == -1) ? "" : name.substring(i + 1);
    }
}
