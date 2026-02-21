package com.knusrae.member.api.follow.domain.service;

import com.knusrae.common.domain.entity.Follow;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.FollowRepository;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.member.api.follow.dto.FollowDto;
import com.knusrae.member.api.follow.dto.FollowerDto;
import com.knusrae.member.api.follow.dto.FollowingDto;
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

    @Transactional
    public FollowDto follow(Long followerId, Long followingId) {
        log.info("팔로우 요청: followerId={}, followingId={}", followerId, followingId);
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalStateException("이미 팔로우 중입니다.");
        }
        Member followingMember = memberRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 회원을 찾을 수 없습니다."));
        Member followerMember = memberRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 회원을 찾을 수 없습니다."));
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        followRepository.save(follow);
        followingMember.increaseFollowerCount();
        followerMember.increaseFollowingCount();
        log.info("팔로우 완료: followId={}", follow.getId());
        return FollowDto.from(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        log.info("언팔로우 요청: followerId={}, followingId={}", followerId, followingId);
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계를 찾을 수 없습니다."));
        Member followingMember = memberRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 회원을 찾을 수 없습니다."));
        Member followerMember = memberRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 회원을 찾을 수 없습니다."));
        followRepository.delete(follow);
        followingMember.decreaseFollowerCount();
        followerMember.decreaseFollowingCount();
        log.info("언팔로우 완료: followerId={}, followingId={}", followerId, followingId);
    }

    public Map<String, Boolean> isFollowing(Long followerId, Long followingId) {
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        return result;
    }

    public Map<String, Object> getFollowers(Long memberId, int page, int size) {
        log.info("팔로워 목록 조회: memberId={}, page={}, size={}", memberId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followPage = followRepository.findByFollowingIdOrderByCreatedAtDesc(memberId, pageable);
        List<Long> followerIds = followPage.getContent().stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = new HashMap<>();
        if (!followerIds.isEmpty()) {
            List<Member> members = memberRepository.findAllById(followerIds);
            memberMap = members.stream().collect(Collectors.toMap(Member::getId, member -> member));
        }
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

    public Map<String, Object> getFollowings(Long memberId, int page, int size) {
        log.info("팔로잉 목록 조회: memberId={}, page={}, size={}", memberId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followPage = followRepository.findByFollowerIdOrderByCreatedAtDesc(memberId, pageable);
        List<Long> followingIds = followPage.getContent().stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = new HashMap<>();
        if (!followingIds.isEmpty()) {
            List<Member> members = memberRepository.findAllById(followingIds);
            memberMap = members.stream().collect(Collectors.toMap(Member::getId, member -> member));
        }
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

    public long getFollowerCount(Long memberId) {
        return followRepository.countByFollowingId(memberId);
    }

    public long getFollowingCount(Long memberId) {
        return followRepository.countByFollowerId(memberId);
    }
}
