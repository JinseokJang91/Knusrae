package com.knusrae.member.api.web;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.member.api.domain.service.MemberService;
import com.knusrae.member.api.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 회원 정보 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

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
            Long memberId = AuthenticationUtils.extractMemberId(authentication);

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
                    .profileImage(member.getProfileImage())
                    .bio(member.getBio())
                    .followerCount(member.getFollowerCount())
                    .followingCount(member.getFollowingCount())
                    .build();

            return ResponseEntity.ok(memberDto);
        } catch (org.springframework.security.authentication.BadCredentialsException | IllegalArgumentException e) {
            log.error("GET /api/member/me - 인증 정보 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            log.error("GET /api/member/me - 회원 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("GET /api/member/me - 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 회원 정보 조회 (공개 프로필)
     *
     * @param memberId 조회할 회원 ID
     * @return 회원 정보 (민감 정보 제외)
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto> retrieveMemberById(@PathVariable Long memberId) {
        try {
            log.info("GET /api/member/{} - 회원 정보 조회", memberId);

            // Member 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("GET /api/member/{} - 회원을 찾을 수 없습니다.", memberId);
                        return new RuntimeException("회원을 찾을 수 없습니다.");
                    });

            // DTO 변환 (민감 정보 제외: email, phone)
            MemberDto memberDto = MemberDto.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .nickname(member.getNickname())
                    .profileImage(member.getProfileImage())
                    .bio(member.getBio())
                    .followerCount(member.getFollowerCount())
                    .followingCount(member.getFollowingCount())
                    .build();

            return ResponseEntity.ok(memberDto);
        } catch (RuntimeException e) {
            log.error("GET /api/member/{} - 회원 조회 실패: {}", memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("GET /api/member/{} - 예상치 못한 오류 발생", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 프로필 정보 업데이트
     *
     * @param authentication Spring Security Authentication 객체
     * @param name           이름
     * @param nickname       닉네임
     * @param bio            자기소개
     * @param profileImage   프로필 이미지 파일
     * @return 업데이트된 회원 정보
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberDto> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile profileImage
    ) {
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);

            log.info("PUT /api/member/profile - 회원 ID: {}", memberId);

            MemberDto updatedMember = memberService.updateProfile(memberId, name, nickname, bio, profileImage);

            return ResponseEntity.ok(updatedMember);
        } catch (org.springframework.security.authentication.BadCredentialsException | IllegalArgumentException e) {
            log.error("PUT /api/member/profile - 인증 정보 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            log.error("PUT /api/member/profile - 프로필 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("PUT /api/member/profile - 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

