package com.knusrae.user.api.web;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.user.api.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 정보 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    /**
     * 현재 로그인한 회원 정보 조회
     * JWT 토큰에서 회원 ID를 추출하여 Member 정보를 반환합니다.
     * 
     * @param authentication Spring Security Authentication 객체 (JWT에서 추출된 회원 ID 포함)
     * @return 회원 정보 (MemberDto)
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDto> retrieveCurrentMember(Authentication authentication) {
        try {
            // JWT 필터에서 설정한 Authentication의 principal에서 회원 ID 추출
            // JwtAuthenticationFilter에서 subject를 principal로 설정했으므로 String으로 받아서 Long으로 변환
            if (authentication == null || authentication.getPrincipal() == null) {
                log.error("GET /api/member/me - 인증 정보가 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String memberIdStr = authentication.getPrincipal().toString();
            Long memberId = Long.parseLong(memberIdStr);

            log.info("GET /api/member/me - 회원 ID: {}", memberId);

            // Member 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("GET /api/member/me - 회원를 찾을 수 없습니다. ID: {}", memberId);
                        return new RuntimeException("회원를 찾을 수 없습니다.");
                    });

            // DTO 변환
            MemberDto memberDto = MemberDto.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .phone(member.getPhone())
                    .build();

            return ResponseEntity.ok(memberDto);
        } catch (NumberFormatException e) {
            log.error("GET /api/member/me - 회원 ID 파싱 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("GET /api/member/me - 회원 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("GET /api/member/me - 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

