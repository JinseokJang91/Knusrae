package com.knusrae.cook.api.domain.constants;

/**
 * 레시피 관련 상수 정의
 */
public final class RecipeConstants {
    private RecipeConstants() {
        // 인스턴스 생성 방지
    }
    
    /** 레시피 이미지 경로 패턴 */
    public static final String RECIPE_IMAGE_PATH_PATTERN = "recipes/%d/%s";
    
    /** 레시피 이미지 최대 크기 (10MB) */
    public static final long MAX_RECIPE_IMAGE_SIZE = 10 * 1024 * 1024;
    
    /** 댓글 이미지 최대 크기 (5MB) */
    public static final long MAX_COMMENT_IMAGE_SIZE = 5 * 1024 * 1024;
    
    /** 허용된 이미지 확장자 */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
}

