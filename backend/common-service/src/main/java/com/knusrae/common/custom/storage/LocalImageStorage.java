package com.knusrae.common.custom.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
    private String baseDir; // C:/JangJinSeok/test/uploads

    @Value("${app.storage.local.public-base-url}")
    private String publicBaseUrl; // http://localhost:8082/uploads

    @Override
    public UploadResponse upload(MultipartFile file, String relativePath) {
        try {
            String ext = extractExt(Objects.requireNonNull(file.getOriginalFilename()));
            String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

            String safeRel = (relativePath == null || relativePath.isBlank()) ? "" : relativePath;
            // 디스크 저장 폴더
            Path targetDir = Paths.get(baseDir, safeRel).normalize();
            Files.createDirectories(targetDir);

            // 실제 저장 파일 경로
            Path target = targetDir.resolve(filename).normalize();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // DB에는 "키"(상대경로)와 "공개 URL"을 함께 기록
            // 예) key: recipes/123/2025-10-10/xxx.jpg
            String key = Paths.get(safeRel, filename).toString().replace('\\', '/');
            String url = (publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length()-1) : publicBaseUrl)
                    + "/" + key;

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

    @Override
    public Resource loadByKey(String key) {
        try {
            Path target = Paths.get(baseDir, key).normalize();
            Resource resource = new UrlResource(target.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + key);
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 파일 로드 실패: " + key, e);
        }
    }

    private String extractExt(String name) {
        int i = name.lastIndexOf('.');
        return (i == -1) ? "" : name.substring(i + 1);
    }
}
