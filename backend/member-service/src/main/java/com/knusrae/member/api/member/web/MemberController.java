package com.knusrae.member.api.member.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.member.api.member.domain.service.MemberService;
import com.knusrae.member.api.member.domain.service.MemberWithdrawalService;
import com.knusrae.member.api.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final MemberService memberService;
    private final MemberWithdrawalService memberWithdrawalService;

    @GetMapping("/me")
    public ResponseEntity<MemberDto> retrieveCurrentMember(Authentication authentication) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        log.info("GET /api/member/me - 회원 ID: {}", memberId);
        MemberDto memberDto = memberService.getCurrentMember(memberId);
        return ResponseEntity.ok(memberDto);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto> retrieveMemberById(@PathVariable Long memberId) {
        log.info("GET /api/member/{} - 회원 정보 조회", memberId);
        MemberDto memberDto = memberService.getMemberById(memberId);
        return ResponseEntity.ok(memberDto);
    }

    /**
     * 회원 탈퇴 — 관련 데이터 일괄 삭제 후 회원 행 삭제 (204)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawCurrentMember(Authentication authentication) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        log.info("DELETE /api/member/me - 회원 탈퇴 요청 ID: {}", memberId);
        memberWithdrawalService.withdrawMember(memberId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberDto> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile profileImage
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        log.info("PUT /api/member/profile - 회원 ID: {}", memberId);
        MemberDto updatedMember = memberService.updateProfile(memberId, name, nickname, bio, profileImage);
        return ResponseEntity.ok(updatedMember);
    }
}
