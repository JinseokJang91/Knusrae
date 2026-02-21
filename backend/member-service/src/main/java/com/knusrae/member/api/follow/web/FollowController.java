package com.knusrae.member.api.follow.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.member.api.follow.domain.service.FollowService;
import com.knusrae.member.api.follow.dto.FollowDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follows/{memberId}")
    public ResponseEntity<FollowDto> follow(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        try {
            Long followerId = AuthenticationUtils.extractMemberId(authentication);
            log.info("POST /api/follows/{} - 팔로우 요청: followerId={}", memberId, followerId);
            FollowDto followDto = followService.follow(followerId, memberId);
            return ResponseEntity.status(HttpStatus.CREATED).body(followDto);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/follows/{} - 잘못된 요청: {}", memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            log.error("POST /api/follows/{} - 이미 팔로우 중: {}", memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/follows/{} - 예상치 못한 오류 발생", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/follows/{memberId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        try {
            Long followerId = AuthenticationUtils.extractMemberId(authentication);
            log.info("DELETE /api/follows/{} - 언팔로우 요청: followerId={}", memberId, followerId);
            followService.unfollow(followerId, memberId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("DELETE /api/follows/{} - 팔로우 관계를 찾을 수 없음: {}", memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("DELETE /api/follows/{} - 예상치 못한 오류 발생", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/follows/{memberId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFollowing(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        try {
            if (authentication == null) {
                return ResponseEntity.ok(Map.of("isFollowing", false));
            }
            Long followerId = AuthenticationUtils.extractMemberId(authentication);
            log.info("GET /api/follows/{}/check - 팔로우 여부 확인: followerId={}", memberId, followerId);
            Map<String, Boolean> result = followService.isFollowing(followerId, memberId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GET /api/follows/{}/check - 예상치 못한 오류 발생", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/members/{memberId}/followers")
    public ResponseEntity<Map<String, Object>> getFollowers(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            log.info("GET /api/members/{}/followers - page={}, size={}", memberId, page, size);
            Map<String, Object> result = followService.getFollowers(memberId, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GET /api/members/{}/followers - 예상치 못한 오류 발생", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/members/{memberId}/followings")
    public ResponseEntity<Map<String, Object>> getFollowings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            log.info("GET /api/members/{}/followings - page={}, size={}", memberId, page, size);
            Map<String, Object> result = followService.getFollowings(memberId, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GET /api/members/{}/followings - 예상치 못한 오류 발생", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
