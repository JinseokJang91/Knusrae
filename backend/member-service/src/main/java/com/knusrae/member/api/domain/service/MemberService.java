package com.knusrae.member.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.member.api.dto.MemberDto;
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

    @Transactional
    public MemberDto updateProfile(Long memberId, String name, String nickname, String bio, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));

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

        return MemberDto.builder()
                .id(updatedMember.getId())
                .name(updatedMember.getName())
                .nickname(updatedMember.getNickname())
                .email(updatedMember.getEmail())
                .phone(updatedMember.getPhone())
                .profileImage(updatedMember.getProfileImage())
                .bio(updatedMember.getBio())
                .followerCount(updatedMember.getFollowerCount())
                .followingCount(updatedMember.getFollowingCount())
                .build();
    }

    private String extractStorageKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        // URL에서 키 추출 (예: http://localhost:8082/uploads/profiles/1/2025-01-01/uuid.jpg -> profiles/1/2025-01-01/uuid.jpg)
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

