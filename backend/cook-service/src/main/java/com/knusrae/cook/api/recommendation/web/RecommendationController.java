package com.knusrae.cook.api.recommendation.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.recommendation.domain.service.RecommendationService;
import com.knusrae.cook.api.recommendation.dto.TodayRecommendationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * 오늘의 추천 레시피 조회
     * 
     * @param limit 반환할 레시피 개수 (기본값: 3)
     * @param refresh 캐시 무시 여부 (기본값: false) - 현재는 미사용
     * @param authentication 인증 정보 (선택적)
     * @return 추천 레시피 목록
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayRecommendations(
            @RequestParam(defaultValue = "3") int limit,
            @RequestParam(defaultValue = "false") boolean refresh,
            Authentication authentication
    ) {
        log.debug("Getting today's recommendations, limit: {}, refresh: {}", limit, refresh);
        
        try {
            // 인증 확인 (선택적)
            Long memberId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    memberId = AuthenticationUtils.extractMemberId(authentication);
                    log.debug("Authenticated user, memberId: {}", memberId);
                } catch (Exception e) {
                    // 인증 실패 시 비로그인 사용자로 처리
                    log.debug("Authentication failed, proceeding as guest user");
                }
            } else {
                log.debug("Guest user (not authenticated)");
            }
            
            // 추천 레시피 조회
            TodayRecommendationDto recommendations = recommendationService
                    .getTodayRecommendations(memberId, limit);
            
            // 성공 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", recommendations);
            
            log.info("Today's recommendations retrieved: {} recipes, type: {}", 
                    recommendations.getRecipes().size(), 
                    recommendations.getRecommendationType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting today's recommendations", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            Map<String, String> error = new HashMap<>();
            error.put("code", "INTERNAL_SERVER_ERROR");
            error.put("message", "추천 레시피를 불러오는데 실패했습니다.");
            errorResponse.put("error", error);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
