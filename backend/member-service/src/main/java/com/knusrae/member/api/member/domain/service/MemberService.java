package com.knusrae.member.api.member.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.exception.ResourceNotFoundException;
import com.knusrae.member.api.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final ImageStorage imageStorage;

    /**
     * 현재 로그인한 회원 정보 조회 (전체 정보)
     */
    public MemberDto getCurrentMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId));
        return toFullMemberDto(member);
    }

    /**
     * 특정 회원 정보 조회 (공개 프로필, 민감 정보 제외)
     */
    public MemberDto getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId));
        return toPublicMemberDto(member);
    }

    private static MemberDto toFullMemberDto(Member m) {
        return MemberDto.builder()
                .id(m.getId())
                .name(m.getName())
                .nickname(m.getNickname())
                .email(m.getEmail())
                .phone(m.getPhone())
                .profileImage(m.getProfileImage())
                .bio(m.getBio())
                .followerCount(m.getFollowerCount())
                .followingCount(m.getFollowingCount())
                .build();
    }

    private static MemberDto toPublicMemberDto(Member m) {
        return MemberDto.builder()
                .id(m.getId())
                .name(m.getName())
                .nickname(m.getNickname())
                .profileImage(m.getProfileImage())
                .bio(m.getBio())
                .followerCount(m.getFollowerCount())
                .followingCount(m.getFollowingCount())
                .build();
    }

    @Transactional
    public MemberDto updateProfile(Long memberId, String name, String nickname, String bio, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId));

        String profileImageUrl = member.getProfileImage();

        // 프로필 이미지 업데이트
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지 삭제 (있는 경우)
            if (member.getProfileImage() != null && member.getProfileImage().contains("/")) {
                try {
                    String oldKey = extractStorageKey(member.getProfileImage());
                    if (oldKey != null) {
                        imageStorage.deleteByKey(oldKey);
                    }
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 삭제 실패: {}", e.getMessage());
                }
            }

            // 새 이미지 업로드
            String relativeDir = "profiles/%d/%s".formatted(memberId, LocalDate.now());
            ImageStorage.UploadResponse uploadResponse = imageStorage.upload(profileImage, relativeDir);
            profileImageUrl = uploadResponse.url();
        }

        // 프로필 정보 업데이트
        member.updateProfile(name, nickname, bio, profileImageUrl);
        Member updatedMember = memberRepository.save(member);
        return toFullMemberDto(updatedMember);
    }

    private String extractStorageKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        try {
            int lastSlash = imageUrl.lastIndexOf('/');
            if (lastSlash > 0) {
                String path = imageUrl.substring(imageUrl.indexOf("/uploads/") + 9);
                return path;
            }
        } catch (Exception e) {
            log.warn("이미지 URL에서 키 추출 실패: {}", imageUrl);
        }
        return null;
    }
}
