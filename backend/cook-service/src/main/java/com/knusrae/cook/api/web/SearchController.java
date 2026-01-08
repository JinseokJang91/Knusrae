package com.knusrae.cook.api.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.RecentSearchKeywordDto;
import com.knusrae.cook.api.domain.service.RecentSearchKeywordService;
import com.knusrae.cook.api.domain.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 검색 기능을 제공하는 컨트롤러
 * 메인 화면, 카테고리, 자유 게시판 등 여러 곳에서 사용할 수 있는 공통 검색 모듈
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final SearchService searchService;
    private final RecentSearchKeywordService recentSearchKeywordService;

    /**
     * 레시피 제목으로 검색
     * 로그인한 사용자의 경우 검색어를 최근 검색어에 자동 저장
     * 
     * @param keyword 검색어
     * @param authentication 인증 정보 (선택적)
     * @return 제목에 검색어가 포함된 공개 레시피 목록
     */
    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeDto>> searchRecipes(
            @RequestParam(required = false) String keyword,
            Authentication authentication) {
        log.debug("Searching recipes with keyword: {}", keyword);
        
        List<RecipeDto> recipeList = searchService.searchRecipesByTitle(keyword);
        log.info("Found {} recipes matching keyword: {}", recipeList.size(), keyword);
        
        // 로그인한 사용자의 경우 검색어를 최근 검색어에 저장
        if (keyword != null && !keyword.trim().isEmpty() && authentication != null) {
            try {
                Long memberId = AuthenticationUtils.extractMemberId(authentication);
                recentSearchKeywordService.saveSearchKeyword(memberId, keyword);
                log.debug("Saved search keyword for member: {}", memberId);
            } catch (Exception e) {
                // 인증 실패 시 무시 (비로그인 사용자는 저장하지 않음)
                log.debug("Could not save search keyword (user not authenticated): {}", e.getMessage());
            }
        }
        
        return ResponseEntity.ok(recipeList);
    }

    /**
     * 최근 검색어 목록 조회
     * 
     * @param authentication 인증 정보
     * @return 최근 검색어 목록 (최신순)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<RecentSearchKeywordDto>> getRecentSearchKeywords(Authentication authentication) {
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            log.debug("Getting recent search keywords for member: {}", memberId);
            
            List<RecentSearchKeywordDto> keywords = recentSearchKeywordService.getRecentSearchKeywords(memberId);
            return ResponseEntity.ok(keywords);
        } catch (org.springframework.security.authentication.BadCredentialsException | IllegalArgumentException e) {
            log.warn("Unauthenticated request for recent search keywords");
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * 검색어 저장
     * 
     * @param keyword 검색어
     * @param authentication 인증 정보
     * @return 성공 응답
     */
    @PostMapping("/recent")
    public ResponseEntity<Void> saveSearchKeyword(
            @RequestParam String keyword,
            Authentication authentication) {
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            log.debug("Saving search keyword: memberId={}, keyword={}", memberId, keyword);
            
            recentSearchKeywordService.saveSearchKeyword(memberId, keyword);
            return ResponseEntity.ok().build();
        } catch (org.springframework.security.authentication.BadCredentialsException | IllegalArgumentException e) {
            log.warn("Unauthenticated request to save search keyword");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 검색어 삭제
     * 
     * @param keywordId 검색어 ID
     * @param authentication 인증 정보
     * @return 성공 응답
     */
    @DeleteMapping("/recent/{keywordId}")
    public ResponseEntity<Void> deleteSearchKeyword(
            @PathVariable Long keywordId,
            Authentication authentication) {
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            log.debug("Deleting search keyword: memberId={}, keywordId={}", memberId, keywordId);
            
            recentSearchKeywordService.deleteSearchKeyword(memberId, keywordId);
            return ResponseEntity.ok().build();
        } catch (org.springframework.security.authentication.BadCredentialsException | IllegalArgumentException e) {
            log.warn("Unauthenticated request to delete search keyword");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 모든 최근 검색어 삭제
     * 
     * @param authentication 인증 정보
     * @return 성공 응답
     */
    @DeleteMapping("/recent")
    public ResponseEntity<Void> deleteAllSearchKeywords(Authentication authentication) {
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            log.debug("Deleting all search keywords for member: {}", memberId);
            
            recentSearchKeywordService.deleteAllSearchKeywords(memberId);
            return ResponseEntity.ok().build();
        } catch (org.springframework.security.authentication.BadCredentialsException | IllegalArgumentException e) {
            log.warn("Unauthenticated request to delete all search keywords");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
    }
}

