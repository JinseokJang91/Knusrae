package com.knusrae.cook.api.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.RecipeDetailDto;
import com.knusrae.cook.api.domain.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipe")
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    
    public RecipeController(RecipeService recipeService, Validator validator) {
        this.recipeService = recipeService;
        this.validator = validator;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper = mapper;
    }

    /**
     * 레시피 생성 (이미지 포함)
     * 
     * @param recipeJson 레시피 정보 JSON 문자열
     * @param images 레시피 이미지 파일 목록
     * @param mainImageIndex 메인 이미지 인덱스
     * @return 생성된 레시피 DTO
     * @throws JsonProcessingException JSON 파싱 오류
     */
    // CREATE - 레시피 생성 (이미지 포함)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> createRecipe(
            @RequestPart(value = "recipe") String recipeJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "mainImageIndex", required = false) Integer mainImageIndex
    ) throws JsonProcessingException {
        log.debug("Creating recipe: images count={}, mainImageIndex={}", 
                images != null ? images.size() : 0, mainImageIndex);
        RecipeDto dto = objectMapper.readValue(recipeJson, RecipeDto.class);
        
        // DTO Validation
        validateRecipeDto(dto);
        
        log.debug("Parsed recipeDto: title={}, memberId={}", dto.getTitle(), dto.getMemberId());
        RecipeDto created = recipeService.createRecipe(dto, images, mainImageIndex);
        log.info("Recipe created successfully: id={}, title={}", created.getId(), created.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 특정 회원의 레시피 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 레시피 목록
     */
    // READ - 전체 레시피 목록 조회(로그인 유저용)
    @GetMapping("/list/member/{memberId}")
    public ResponseEntity<List<RecipeDto>> listMemberRecipes(@PathVariable Long memberId) {
        log.debug("Fetching recipes for member: {}", memberId);
        List<RecipeDto> recipeList = recipeService.listMemberRecipes(memberId);
        log.info("Retrieved {} recipes for member: {}", recipeList.size(), memberId);

        return ResponseEntity.ok(recipeList);
    }

    /**
     * 전체 공개 레시피 목록 조회
     * 
     * @return 공개된 레시피 목록
     */
    // READ - 전체 레시피 목록 조회(애플리케이션 사용자용)
    @GetMapping("/list/all")
    public ResponseEntity<List<RecipeDto>> listAllRecipes() {
        log.debug("Fetching all recipes");
        List<RecipeDto> recipeList = recipeService.listAllRecipes();
        log.info("Retrieved {} recipes", recipeList.size());

        return ResponseEntity.ok(recipeList);
    }

    /**
     * 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
     * 조회 시 조회수가 증가합니다.
     * 
     * @param id 레시피 ID
     * @return 레시피 상세 정보 DTO
     */
    // READ - 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecipeDetailDto> retrieveRecipeDetail(@PathVariable Long id) {
        log.debug("Fetching recipe detail for ID: {}", id);
        RecipeDetailDto recipeDetail = recipeService.retrieveRecipeDetail(id);
        log.info("Recipe detail retrieved successfully: id={}, title={}", id, recipeDetail.getTitle());
        return ResponseEntity.ok(recipeDetail);
    }

    /**
     * 레시피 수정 (이미지 포함)
     * 작성자만 수정 가능합니다.
     * 
     * @param id 레시피 ID
     * @param recipeJson 레시피 정보 JSON 문자열
     * @param images 새 이미지 파일 목록 (null인 경우 기존 이미지 유지)
     * @param mainImageIndex 메인 이미지 인덱스
     * @param authentication 인증 정보 (작성자 확인용)
     * @return 수정된 레시피 DTO
     * @throws JsonProcessingException JSON 파싱 오류
     */
    // UPDATE - 레시피 수정 (이미지 포함)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> updateRecipe(
            @PathVariable Long id,
            @RequestPart(value = "recipe") String recipeJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "mainImageIndex", required = false) Integer mainImageIndex,
            Authentication authentication
    ) throws JsonProcessingException {
        // 인증 정보에서 회원 ID 추출
        Long memberId = extractMemberId(authentication);
        
        log.debug("Updating recipe: id={}, memberId={}, images count={}, mainImageIndex={}", 
                id, memberId, images != null ? images.size() : 0, mainImageIndex);
        RecipeDto dto = objectMapper.readValue(recipeJson, RecipeDto.class);
        
        // DTO Validation
        validateRecipeDto(dto);
        
        log.debug("Parsed recipeDto: title={}", dto.getTitle());
        RecipeDto updated = recipeService.updateRecipe(id, dto, images, mainImageIndex, memberId);
        log.info("Recipe updated successfully: id={}, title={}", updated.getId(), updated.getTitle());

        return ResponseEntity.ok(updated);
    }

    /**
     * 레시피 삭제 (물리적 삭제)
     * 작성자만 삭제 가능합니다.
     * 
     * @param id 레시피 ID
     * @param authentication 인증 정보 (작성자 확인용)
     * @return 204 No Content
     */
    // DELETE - 레시피 삭제 (물리적 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id, Authentication authentication) {
        // 인증 정보에서 회원 ID 추출
        Long memberId = extractMemberId(authentication);
        
        log.info("Deleting recipe: id={}, memberId={}", id, memberId);
        recipeService.deleteRecipe(id, memberId);
        log.info("Recipe deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Authentication에서 회원 ID 추출
     */
    private Long extractMemberId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("인증 정보가 없습니다.");
        }
        
        try {
            String memberIdStr = authentication.getPrincipal().toString();
            return Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 회원 ID 형식입니다.");
        }
    }
    
    /**
     * RecipeDto 검증
     */
    private void validateRecipeDto(RecipeDto dto) {
        Set<ConstraintViolation<RecipeDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("입력값 검증 실패: " + errorMessage);
        }
        
        // 조리 단계 검증 (최소 1개 이상 필요)
        if (dto.getSteps() == null || dto.getSteps().isEmpty()) {
            throw new IllegalArgumentException("조리 단계는 최소 1개 이상 필요합니다.");
        }
    }
}
