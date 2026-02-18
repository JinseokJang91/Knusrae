package com.knusrae.cook.api.creator.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.creator.domain.service.CreatorRecommendationService;
import com.knusrae.cook.api.creator.dto.CreatorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 크리에이터 추천 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final CreatorRecommendationService creatorRecommendationService;

    /**
     * 추천 크리에이터 목록 조회
     *
     * @param limit          반환할 크리에이터 수 (기본값: 6)
     * @param authentication 인증 정보 (선택적)
     * @return 추천 크리에이터 목록
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<CreatorDto>> getRecommendedCreators(
            @RequestParam(defaultValue = "6") int limit,
            Authentication authentication
    ) {
        try {
            log.info("GET /api/creators/recommended - limit={}", limit);

            Long currentMemberId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    currentMemberId = AuthenticationUtils.extractMemberId(authentication);
                    log.debug("Authenticated user: memberId={}", currentMemberId);
                } catch (Exception e) {
                    log.debug("Authentication failed, proceeding as guest");
                }
            }

            List<CreatorDto> creators = creatorRecommendationService.getRecommendedCreators(currentMemberId, limit);
            return ResponseEntity.ok(creators);

        } catch (Exception e) {
            log.error("GET /api/creators/recommended - 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
