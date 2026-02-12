package com.knusrae.member.api.domain.service;

import com.knusrae.common.domain.entity.Follow;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.FollowRepository;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.member.api.dto.FollowDto;
import com.knusrae.member.api.dto.FollowerDto;
import com.knusrae.member.api.dto.FollowingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    
    /**
     * 팔로우
     */
    @Transactional
    public FollowDto follow(Long followerId, Long followingId) {
        log.info("팔로우 요청: followerId={}, followingId={}", followerId, followingId);
        
        // 자기 자신을 팔로우할 수 없음
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
        
        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalStateException("이미 팔로우 중입니다.");
        }
        
        // 팔로우 대상 회원 존재 여부 확인
        Member followingMember = memberRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 회원을 찾을 수 없습니다."));
        
        Member followerMember = memberRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 회원을 찾을 수 없습니다."));
        
        // Follow 엔티티 생성
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        
        followRepository.save(follow);
        
        // Member의 팔로워/팔로잉 카운트 증가
        followingMember.increaseFollowerCount();
        followerMember.increaseFollowingCount();
        
        log.info("팔로우 완료: followId={}", follow.getId());
        
        return FollowDto.from(follow);
    }
    
    /**
     * 언팔로우
     */
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        log.info("언팔로우 요청: followerId={}, followingId={}", followerId, followingId);
        
        // 팔로우 관계 존재 여부 확인
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계를 찾을 수 없습니다."));
        
        // Member 조회
        Member followingMember = memberRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 회원을 찾을 수 없습니다."));
        
        Member followerMember = memberRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 회원을 찾을 수 없습니다."));
        
        // Follow 엔티티 삭제
        followRepository.delete(follow);
        
        // Member의 팔로워/팔로잉 카운트 감소
        followingMember.decreaseFollowerCount();
        followerMember.decreaseFollowingCount();
        
        log.info("언팔로우 완료: followerId={}, followingId={}", followerId, followingId);
    }
    
    /**
     * 팔로우 여부 확인
     */
    public Map<String, Boolean> isFollowing(Long followerId, Long followingId) {
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        return result;
    }
    
    /**
     * 팔로워 목록 조회 (페이징)
     */
    public Map<String, Object> getFollowers(Long memberId, int page, int size) {
        log.info("팔로워 목록 조회: memberId={}, page={}, size={}", memberId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followPage = followRepository.findByFollowingIdOrderByCreatedAtDesc(memberId, pageable);
        
        // 팔로워 ID 목록 추출
        List<Long> followerIds = followPage.getContent().stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
        
        // 배치로 회원 정보 조회 (N+1 방지)
        Map<Long, Member> memberMap = new HashMap<>();
        if (!followerIds.isEmpty()) {
            List<Member> members = memberRepository.findAllById(followerIds);
            memberMap = members.stream()
                    .collect(Collectors.toMap(Member::getId, member -> member));
        }
        
        // DTO 변환
        Map<Long, Member> finalMemberMap = memberMap;
        List<FollowerDto> followers = followPage.getContent().stream()
                .map(follow -> {
                    Member follower = finalMemberMap.get(follow.getFollowerId());
                    return FollowerDto.builder()
                            .followId(follow.getId())
                            .followerId(follow.getFollowerId())
                            .followerNickname(follower != null ? follower.getNickname() : null)
                            .followerProfileImage(follower != null ? follower.getProfileImage() : null)
                            .createdAt(follow.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", followers);
        result.put("totalElements", followPage.getTotalElements());
        result.put("totalPages", followPage.getTotalPages());
        result.put("currentPage", followPage.getNumber());
        
        return result;
    }
    
    /**
     * 팔로잉 목록 조회 (페이징)
     */
    public Map<String, Object> getFollowings(Long memberId, int page, int size) {
        log.info("팔로잉 목록 조회: memberId={}, page={}, size={}", memberId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followPage = followRepository.findByFollowerIdOrderByCreatedAtDesc(memberId, pageable);
        
        // 팔로잉 ID 목록 추출
        List<Long> followingIds = followPage.getContent().stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());
        
        // 배치로 회원 정보 조회 (N+1 방지)
        Map<Long, Member> memberMap = new HashMap<>();
        if (!followingIds.isEmpty()) {
            List<Member> members = memberRepository.findAllById(followingIds);
            memberMap = members.stream()
                    .collect(Collectors.toMap(Member::getId, member -> member));
        }
        
        // DTO 변환
        Map<Long, Member> finalMemberMap = memberMap;
        List<FollowingDto> followings = followPage.getContent().stream()
                .map(follow -> {
                    Member following = finalMemberMap.get(follow.getFollowingId());
                    return FollowingDto.builder()
                            .followId(follow.getId())
                            .followingId(follow.getFollowingId())
                            .followingNickname(following != null ? following.getNickname() : null)
                            .followingProfileImage(following != null ? following.getProfileImage() : null)
                            .createdAt(follow.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", followings);
        result.put("totalElements", followPage.getTotalElements());
        result.put("totalPages", followPage.getTotalPages());
        result.put("currentPage", followPage.getNumber());
        
        return result;
    }
    
    /**
     * 팔로워 수 조회
     */
    public long getFollowerCount(Long memberId) {
        return followRepository.countByFollowingId(memberId);
    }
    
    /**
     * 팔로잉 수 조회
     */
    public long getFollowingCount(Long memberId) {
        return followRepository.countByFollowerId(memberId);
    }
}
