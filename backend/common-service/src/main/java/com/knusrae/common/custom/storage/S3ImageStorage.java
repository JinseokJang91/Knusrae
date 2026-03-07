package com.knusrae.common.custom.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
@Slf4j
public class S3ImageStorage implements ImageStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String publicBaseUrl;

    public S3ImageStorage(
            S3Client s3Client,
            @Value("${app.storage.s3.bucket}") String bucket,
            @Value("${app.storage.s3.region:ap-northeast-2}") String region,
            @Value("${app.storage.s3.public-base-url:}") String publicBaseUrl
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region != null ? region : "ap-northeast-2";
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl;
    }

    @Override
    public UploadResponse upload(MultipartFile file, String relativePath) {
        try {
            String ext = extractExt(Objects.requireNonNull(file.getOriginalFilename()));
            String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            String safeRel = (relativePath == null || relativePath.isBlank()) ? "" : relativePath.trim();
            String key = (safeRel.isEmpty() ? filename : (safeRel.endsWith("/") ? safeRel : safeRel + "/") + filename);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build();

            try (InputStream in = file.getInputStream()) {
                s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
            }

            String url = buildPublicUrl(key);
            return new UploadResponse(key, url);
        } catch (IOException e) {
            throw new RuntimeException("이미지 S3 업로드 실패", e);
        }
    }

    @Override
    public void deleteByKey(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.error("S3 객체 삭제 실패: key={}", key, e);
        }
    }

    @Override
    public Resource loadByKey(String key) {
        try {
            String url = buildPublicUrl(key);
            return new UrlResource(URI.create(url));
        } catch (Exception e) {
            throw new RuntimeException("S3 이미지 로드 실패: " + key, e);
        }
    }

    private String buildPublicUrl(String key) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
            return base + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String extractExt(String name) {
        int i = name.lastIndexOf('.');
        return (i == -1) ? "" : name.substring(i + 1);
    }
}
