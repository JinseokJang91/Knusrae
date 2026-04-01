package com.knusrae.common.custom.storage;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 이미지 저장소 URL에서 스토리지 객체 키를 추출하는 유틸.
 * 로컬(/uploads/...), S3 직접 URL, CloudFront(public-base-url) 등 공통 형식을 지원.
 */
public final class StorageKeyUtils {

    private static final String UPLOADS_PREFIX = "/uploads/";

    private StorageKeyUtils() {}

    /**
     * 이미지 공개 URL에서 저장소 객체 키를 추출한다.
     * <ul>
     *   <li>로컬: http://host/uploads/profiles/1/2025-03-08/uuid.jpg → profiles/1/2025-03-08/uuid.jpg</li>
     *   <li>S3: https://bucket.s3.region.amazonaws.com/profiles/1/.../uuid.jpg → profiles/1/.../uuid.jpg</li>
     *   <li>CloudFront 등: https://cdn.example.com/profiles/1/.../uuid.jpg → profiles/1/.../uuid.jpg</li>
     * </ul>
     *
     * @param imageUrl 저장 시 반환된 공개 URL (null/blank 허용)
     * @return 객체 키, 추출 불가 시 null
     */
    public static String parseKeyFromImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(imageUrl.trim());
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                return null;
            }
            if (path.startsWith(UPLOADS_PREFIX)) {
                return path.substring(UPLOADS_PREFIX.length());
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
